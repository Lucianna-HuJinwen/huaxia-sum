package com.liam.term.mapper.template;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liam.term.domain.template.Template;
import com.liam.term.domain.template.dto.TemplateQueryDTO;
import com.liam.term.domain.template.vo.TemplateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TemplateMapper extends BaseMapper<Template> {

    List<TemplateVO> selectTemplateList(@Param("query") TemplateQueryDTO templateQueryDTO,
                                        @Param("currentUserId") Long currentUserId);
}
