package com.liam.term.domain.template;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.liam.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("template")
public class Template extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;                     // 模板ID

    private Long userId;

    private String templateName; // 模板名称

    private String era;          // 时代背景
    private String author;       // 作者
    private String textType;     // 文本类型
    private String style;        // 风格
    private String purpose;      // 用途
    private String audience;     // 受众
    private String translatorRole; // 译者角色
    private String scene;        // 场景
    private String customRules;  // 自定义规则（长文本）
}
