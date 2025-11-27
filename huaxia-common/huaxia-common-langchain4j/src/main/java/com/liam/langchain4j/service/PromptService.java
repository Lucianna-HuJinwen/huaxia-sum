package com.liam.langchain4j.service;

import cn.hutool.core.util.StrUtil;
import com.liam.langchain4j.domain.dto.TemplatePromptDTO;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    /**
     * 根据模板配置和术语匹配结果拼装翻译 prompt
     *
     * @param template      模板配置信息，包含翻译上下文相关的元数据（如风格、用途等），可为 null
     * @param sourceText    待翻译的原始文本内容，不可为空
     * @param glossaryTerms 术语匹配结果字符串，用于指导翻译中特定词汇的处理，可为空
     * @return 拼装完成的翻译 prompt 字符串
     */
    public String buildTranslatePrompt(TemplatePromptDTO template, String sourceText, String glossaryTerms) {
        StringBuilder prompt = new StringBuilder();

//        // 基础角色设定
//        prompt.append("你是一位专业的翻译专家，请根据以下要求，对输入文本进行准确、符合语境的翻译：\n\n");

        // 拼接模板信息部分（如果提供了模板）
        if (template != null) {
            prompt.append("### 翻译模板信息：\n");
            if (StrUtil.isNotBlank(template.getTemplateName())) {
                prompt.append("- 模板名称：").append(template.getTemplateName()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getEra())) {
                prompt.append("- 时代背景：").append(template.getEra()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getAuthor())) {
                prompt.append("- 作者/来源：").append(template.getAuthor()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getTextType())) {
                prompt.append("- 目标文本类型：").append(template.getTextType()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getStyle())) {
                prompt.append("- 翻译风格：").append(template.getStyle()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getPurpose())) {
                prompt.append("- 翻译用途：").append(template.getPurpose()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getAudience())) {
                prompt.append("- 目标受众：").append(template.getAudience()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getTranslatorRole())) {
                prompt.append("- 译者角色：").append(template.getTranslatorRole()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getScene())) {
                prompt.append("- 应用场景：").append(template.getScene()).append("\n");
            }
            if (StrUtil.isNotBlank(template.getCustomRules())) {
                prompt.append("- 自定义翻译规则：\n  ").append(template.getCustomRules().trim()).append("\n");
            }
        }

        // 添加术语解释部分（如果存在术语）
        if (StrUtil.isNotBlank(glossaryTerms)) {
            prompt.append("\n### 术语解释（请严格使用如下术语）：\n").append(glossaryTerms).append("\n");
        }

        // 添加待翻译文本内容
        prompt.append("\n### 待翻译文本：\n").append(sourceText).append("\n");

        return prompt.toString();
    }

}
