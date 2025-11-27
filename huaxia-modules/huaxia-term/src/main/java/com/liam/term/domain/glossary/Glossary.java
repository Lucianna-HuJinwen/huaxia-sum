package com.liam.term.domain.glossary;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.liam.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("glossary")
public class Glossary extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long glossaryId;

    private String title;

    private String sourceLanguage;

    private String targetLanguage;

    private Long userId;

    private String description;

    private Integer isPublic;
}
