package com.liam.term.domain.glossary.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.liam.term.domain.term.vo.TermVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GlossaryDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long glossaryId;

    private String title;

    private String sourceLanguage;

    private String targetLanguage;

    private String description;

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

    /**
     * 术语库中的术语列表
     */
    private List<TermVO> glossaryTermList;
}
