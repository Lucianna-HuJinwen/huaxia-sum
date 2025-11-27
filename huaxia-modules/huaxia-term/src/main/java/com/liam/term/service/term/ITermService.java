package com.liam.term.service.term;

import com.liam.common.core.domain.TableDataInfo;
import com.liam.term.domain.term.dto.*;
import com.liam.term.domain.term.vo.TermBatchAddVO;
import com.liam.term.domain.term.vo.TermBatchDeleteVO;


public interface ITermService {

    boolean add(TermAddDTO termAddDTO);

    int edit(TermEditDTO termEditDTO);

    int delete(Long termId);

    TableDataInfo list(TermQueryDTO termQueryDTO);

    TermBatchAddVO batchAdd(TermBatchAddDTO termBatchAddDTO);

    TermBatchDeleteVO batchDelete(TermBatchDeleteDTO termBatchDeleteDTO);
}
