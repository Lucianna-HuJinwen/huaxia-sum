package com.liam.term.domain.term.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TermVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long termId;

    private String sourceTerm;

    private String targetTerm;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
