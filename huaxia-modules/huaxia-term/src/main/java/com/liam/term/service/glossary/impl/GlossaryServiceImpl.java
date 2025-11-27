package com.liam.term.service.glossary.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.liam.common.core.enums.ResultCode;
import com.liam.term.domain.glossary.Glossary;
import com.liam.term.domain.glossary.dto.GlossaryAddDTO;
import com.liam.term.domain.glossary.dto.GlossaryEditDTO;
import com.liam.term.domain.glossary.dto.GlossaryQueryDTO;
import com.liam.term.domain.glossary.vo.GlossaryDetailVO;
import com.liam.term.domain.glossary.vo.GlossaryVO;
import com.liam.term.domain.term.TermDict;
import com.liam.term.domain.term.vo.TermVO;
import com.liam.term.elasticsearch.TermDictRepository;
import com.liam.term.manager.glossary.GlossaryCacheManager;
import com.liam.term.manager.term.TermCacheManager;
import com.liam.term.mapper.glossary.GlossaryMapper;
import com.liam.term.mapper.term.TermDictMapper;
import com.liam.term.service.glossary.IGlossaryService;
import com.liam.term.utils.UserRoleUtils;
import exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class GlossaryServiceImpl implements IGlossaryService {

    @Autowired
    private GlossaryMapper glossaryMapper;

    @Autowired
    private TermDictMapper termDictMapper;

    @Autowired
    private GlossaryCacheManager glossaryCacheManager;

    @Autowired
    private TermDictRepository termDictRepository;

    @Autowired
    private TermCacheManager termCacheManager;

    @Autowired
    private UserRoleUtils userRoleUtils;

    @Override
    public List<GlossaryVO> list(GlossaryQueryDTO glossaryQueryDTO) {
        PageHelper.startPage(glossaryQueryDTO.getPageNum(), glossaryQueryDTO.getPageSize());
        
        // 获取当前用户信息
        Long currentUserId = userRoleUtils.getCurrentUserId();
        Integer currentUserRole = userRoleUtils.getCurrentUserRole();
        
        // 根据用户身份和查询条件过滤术语库
        return glossaryMapper.selectGlossaryList(glossaryQueryDTO, currentUserId, currentUserRole);
    }

    @Override
    public String create(GlossaryAddDTO glossaryAddDTO) {
        // 获取当前登录用户ID
        Long currentUserId = userRoleUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        Integer currentUserRole = userRoleUtils.getCurrentUserRole();
        
        // 检查标题重复性（同一用户下）
        checkGlossarySaveParams(glossaryAddDTO, null, currentUserId);
        
        Glossary glossary = new Glossary();
        BeanUtil.copyProperties(glossaryAddDTO, glossary);
        glossary.setUserId(currentUserId);
        
        // 根据用户角色设置公开状态
        if (UserRoleUtils.ROLE_ADMIN.equals(currentUserRole)) {
            // 管理员创建的术语库默认公开
            glossary.setIsPublic(1);
        } else {
            // 普通用户创建的术语库默认私有
            glossary.setIsPublic(0);
        }
        
        glossaryMapper.insert(glossary);
        log.info("创建术语库成功: glossaryId={}, userId={}, isPublic={}", 
            glossary.getGlossaryId(), currentUserId, glossary.getIsPublic());
        
        return glossary.getGlossaryId().toString();
    }

    @Override
    public GlossaryDetailVO detail(Long glossaryId) {
        Glossary glossary = getGlossary(glossaryId);
        
        // 检查访问权限
        Long currentUserId = userRoleUtils.getCurrentUserId();
        Integer currentUserRole = userRoleUtils.getCurrentUserRole();
        
        if (!userRoleUtils.canAccessGlossary(glossary.getUserId(), glossary.getIsPublic())) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        GlossaryDetailVO glossaryDetailVO = new GlossaryDetailVO();
        BeanUtil.copyProperties(glossary, glossaryDetailVO);
        
        // 设置权限标识
        glossaryDetailVO.setCanEdit(userRoleUtils.canEditGlossary(glossary.getUserId(), glossary.getIsPublic()));
        glossaryDetailVO.setCanDelete(userRoleUtils.canEditGlossary(glossary.getUserId(), glossary.getIsPublic()));
        
        // 查询术语库中的术语
        List<TermDict> termDictList = termDictMapper
                .selectList(new LambdaQueryWrapper<TermDict>()
                        .eq(TermDict::getGlossaryId, glossaryId)
                        .orderByDesc(TermDict::getCreateTime));
        
        if (CollectionUtil.isNotEmpty(termDictList)) {
            List<TermVO> termVOList = BeanUtil.copyToList(termDictList, TermVO.class);
            glossaryDetailVO.setGlossaryTermList(termVOList);
        }
        
        return glossaryDetailVO;
    }

    @Override
    public int edit(GlossaryEditDTO glossaryEditDTO) {
        Glossary glossary = getGlossary(glossaryEditDTO.getGlossaryId());
        
        // 检查编辑权限
        if (!userRoleUtils.canEditGlossary(glossary.getUserId(), glossary.getIsPublic())) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        // 检查标题重复性
        Long currentUserId = userRoleUtils.getCurrentUserId();
        checkGlossarySaveParams(glossaryEditDTO, glossaryEditDTO.getGlossaryId(), currentUserId);
        
        glossary.setTitle(glossaryEditDTO.getTitle());
        glossary.setSourceLanguage(glossaryEditDTO.getSourceLanguage());
        glossary.setTargetLanguage(glossaryEditDTO.getTargetLanguage());
        glossary.setDescription(glossaryEditDTO.getDescription());
        
        return glossaryMapper.updateById(glossary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long glossaryId) {
        Glossary glossary = getGlossary(glossaryId);
        
        // 检查删除权限（只有编辑权限的用户才能删除）
        if (!userRoleUtils.canEditGlossary(glossary.getUserId(), glossary.getIsPublic())) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }
        
        // 查询术语库下的所有术语
        List<TermDict> termDictList = termDictMapper.selectList(
            new LambdaQueryWrapper<TermDict>()
                .eq(TermDict::getGlossaryId, glossaryId)
        );
        
        if (CollectionUtil.isNotEmpty(termDictList)) {
            log.info("删除术语库，需要删除{}个术语: glossaryId={}", termDictList.size(), glossaryId);
            
            // 删除ES中的术语数据
            for (TermDict termDict : termDictList) {
                try {
                    termDictRepository.deleteById(termDict.getTermId());
                    termCacheManager.deleteCache(termDict.getTermId());
                    log.debug("删除ES和缓存中的术语: termId={}, sourceTerm={}", 
                        termDict.getTermId(), termDict.getSourceTerm());
                } catch (Exception e) {
                    log.warn("删除ES或缓存中的术语失败: termId={}, error={}", 
                        termDict.getTermId(), e.getMessage());
                }
            }
        }
        
        // 删除MySQL中的术语数据
        int termDeleteCount = termDictMapper.delete(
            new LambdaQueryWrapper<TermDict>()
                .eq(TermDict::getGlossaryId, glossaryId)
        );
        
        log.info("删除术语库完成: glossaryId={}, 删除术语数量={}", glossaryId, termDeleteCount);
        
        // 删除术语库
        return glossaryMapper.deleteById(glossaryId);
    }



    private void checkGlossarySaveParams(GlossaryAddDTO glossaryAddDTO, Long glossaryId, Long userId) {
        List<Glossary> glossaryList = glossaryMapper.selectList(new LambdaQueryWrapper<Glossary>()
                .eq(Glossary::getUserId, userId)
                .eq(Glossary::getTitle, glossaryAddDTO.getTitle())
                .ne(glossaryId != null, Glossary::getGlossaryId, glossaryId));
        if (CollectionUtil.isNotEmpty(glossaryList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
    }

    private Glossary getGlossary(Long glossaryId) {
        Glossary glossary = glossaryMapper.selectById(glossaryId);
        if (glossary == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return glossary;
    }
}
