package com.liam.term.mapper.glossary;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liam.term.domain.glossary.Glossary;
import com.liam.term.domain.glossary.dto.GlossaryQueryDTO;
import com.liam.term.domain.glossary.vo.GlossaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GlossaryMapper extends BaseMapper<Glossary> {

    List<GlossaryVO> selectGlossaryList(@Param("query") GlossaryQueryDTO glossaryQueryDTO,
                                        @Param("currentUserId") Long currentUserId,
                                        @Param("currentUserRole") Integer currentUserRole);
}
