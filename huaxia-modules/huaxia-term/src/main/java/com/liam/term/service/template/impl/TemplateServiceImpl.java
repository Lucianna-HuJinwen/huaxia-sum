package com.liam.term.service.template.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.liam.common.core.constants.Constants;
import com.liam.common.core.enums.ResultCode;
import com.liam.common.core.utils.ThreadLocalUtil;
import com.liam.term.domain.template.Template;
import com.liam.term.domain.template.dto.TemplateAddDTO;
import com.liam.term.domain.template.dto.TemplateEditDTO;
import com.liam.term.domain.template.dto.TemplateQueryDTO;
import com.liam.term.domain.template.vo.TemplateDetailVO;
import com.liam.term.domain.template.vo.TemplateVO;
import com.liam.term.manager.template.TemplateCacheManager;
import com.liam.term.mapper.template.TemplateMapper;
import com.liam.term.service.template.ITemplateService;
import exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TemplateServiceImpl implements ITemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private TemplateCacheManager templateCacheManager;

    @Override
    public List<TemplateVO> list(TemplateQueryDTO templateQueryDTO) {
        PageHelper.startPage(templateQueryDTO.getPageNum(), templateQueryDTO.getPageSize());
        Long currentUserId = getCurrentUserId();
        log.info("currentUserId:{}", currentUserId);

        return templateMapper.selectTemplateList(templateQueryDTO, currentUserId);
    }

    @Override
    public String create(TemplateAddDTO templateAddDTO) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }

        checkTemplateSaveParams(templateAddDTO, null, currentUserId);

        Template template = new Template();
        BeanUtil.copyProperties(templateAddDTO, template);
        template.setUserId(currentUserId);

        templateMapper.insert(template);
        
        // 缓存新创建的模板
        templateCacheManager.setCache(template.getId(), template);
        
        log.info("创建模板成功: templateId={}, userId={}, templateName={}", 
            template.getId(), currentUserId, template.getTemplateName());
        
        return template.getId().toString();
    }

    @Override
    public TemplateDetailVO detail(Long templateId) {
        // 先从缓存获取
        Template template = templateCacheManager.getCache(templateId);
        if (template == null) {
            // 缓存未命中，从数据库获取
            template = getTemplate(templateId);
            // 缓存模板详情
            templateCacheManager.setCache(templateId, template);
        }

        Long currentUserId = getCurrentUserId();
        if (!template.getUserId().equals(currentUserId)) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }

        TemplateDetailVO templateDetailVO = new TemplateDetailVO();
        BeanUtil.copyProperties(template, templateDetailVO);
        return templateDetailVO;
    }

    @Override
    public int edit(TemplateEditDTO templateEditDTO) {
        Template template = getTemplate(templateEditDTO.getTemplateId());

        if (!template.getUserId().equals(getCurrentUserId())) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }

        checkTemplateSaveParams(templateEditDTO, templateEditDTO.getTemplateId(), template.getUserId());

        BeanUtil.copyProperties(templateEditDTO, template);
        template.setUpdateTime(LocalDateTime.now());

        int result = templateMapper.updateById(template);
        
        // 更新缓存
        templateCacheManager.setCache(template.getId(), template);
        
        log.info("编辑模板成功: templateId={}, userId={}, templateName={}", 
            template.getId(), template.getUserId(), template.getTemplateName());
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long templateId) {
        Template template = getTemplate(templateId);
        if (!template.getUserId().equals(getCurrentUserId())) {
            throw new ServiceException(ResultCode.FAILED_UNAUTHORIZED);
        }

        int result = templateMapper.deleteById(templateId);
        
        // 删除缓存
        templateCacheManager.deleteCache(templateId);
        
        log.info("删除模板成功: templateId={}, userId={}, templateName={}", 
            templateId, template.getUserId(), template.getTemplateName());
        
        return result;
    }

    private Long getCurrentUserId() {
        return ThreadLocalUtil.get(Constants.USER_ID, Long.class);
    }

    private void checkTemplateSaveParams(TemplateAddDTO templateAddDTO, Long templateId, Long userId) {
        List<Template> list = templateMapper.selectList(new LambdaQueryWrapper<Template>()
                .eq(Template::getUserId, userId)
                .eq(Template::getTemplateName, templateAddDTO.getTemplateName())
                .ne(templateId != null, Template::getId, templateId));

        if (CollectionUtil.isNotEmpty(list)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
    }

    private Template getTemplate(Long templateId) {
        Template template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return template;
    }
}
