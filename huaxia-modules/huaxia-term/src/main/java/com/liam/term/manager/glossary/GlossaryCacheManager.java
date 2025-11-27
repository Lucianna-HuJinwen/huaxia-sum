package com.liam.term.manager.glossary;

import com.liam.common.core.constants.CacheConstants;
import com.liam.redis.service.RedisService;
import com.liam.term.domain.glossary.Glossary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlossaryCacheManager {

    @Autowired
    private RedisService redisService;

        /**
     * 将术语添加到缓存中
     *
     * @param glossary 术语对象，包含需要缓存的术语信息
     */
    public void addCache(Glossary glossary) {
        // 将术语ID添加到术语列表的左侧
        redisService.leftPushForList(getGlossaryListKey(), glossary.getGlossaryId());
        // 缓存术语详细信息，以术语ID作为键
        redisService.setCacheObject(getDetailKey(glossary.getGlossaryId()), glossary);
    }


        /**
     * 删除术语表相关的缓存数据
     *
     * @param glossaryId 术语表ID，用于标识需要删除缓存的具体术语表
     */
    public void deleteCache(Long glossaryId) {
        // 删除术语表列表缓存中的指定术语表项
        redisService.removeForList(getGlossaryListKey(), glossaryId);
        // 删除术语表详情缓存
        redisService.deleteObject(getDetailKey(glossaryId));
        // 删除术语表对应的术语列表缓存
        redisService.deleteObject(getGlossaryTermListKey(glossaryId));
    }


        /**
     * 获取词汇表未完成列表的缓存键值
     *
     * @return 返回缓存键值字符串，用于标识词汇表未完成列表的缓存数据
     */
    private String getGlossaryListKey() {
        return CacheConstants.GLOSSARY_UNFINISHED_LIST;
    }


        /**
     * 获取词汇表详情的缓存键
     *
     * @param glossaryId 词汇表ID
     * @return 缓存键字符串，格式为缓存前缀加上词汇表ID
     */
    private String getDetailKey(Long glossaryId) {
        return CacheConstants.GLOSSARY_DETAIL + glossaryId;
    }


        /**
     * 获取词汇表术语列表的缓存键
     *
     * @param glossaryId 词汇表ID
     * @return 缓存键字符串，格式为缓存前缀加上词汇表ID
     */
    private String getGlossaryTermListKey(Long glossaryId) {
        return CacheConstants.GLOSSARY_TERM_LIST + glossaryId;
    }

}
