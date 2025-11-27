package com.liam.term.domain.glossary.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GlossaryVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long glossaryId;

    private String title;
    
    private String sourceLanguage;
    
    private String targetLanguage;
    
    /**
     * 创建者用户ID（用于权限判断）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 是否公开：0-私有，1-公开
     */
    private Integer isPublic;
    
    /**
     * 是否可以编辑
     */
    private Boolean canEdit;
    
    /**
     * 是否可以删除
     */
    private Boolean canDelete;
}
