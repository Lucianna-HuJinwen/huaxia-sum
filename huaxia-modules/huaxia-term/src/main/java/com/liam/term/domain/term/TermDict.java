package com.liam.term.domain.term;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.liam.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("term_dict")
public class TermDict extends BaseEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long termId;

    private Long glossaryId;

    private String sourceTerm;

    private String targetTerm;
} 