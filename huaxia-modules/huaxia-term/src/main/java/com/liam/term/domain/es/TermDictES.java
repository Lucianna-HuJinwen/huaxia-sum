package com.liam.term.domain.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "term_dict")
public class TermDictES {

    @Id
    private Long termId;

    @Field(type = FieldType.Long)
    private Long glossaryId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String sourceTerm;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String targetTerm;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
} 