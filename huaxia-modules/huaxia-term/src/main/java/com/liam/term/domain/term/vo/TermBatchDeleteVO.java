package com.liam.term.domain.term.vo;

import lombok.Data;

/**
 * 术语批量删除结果VO
 *
 * @author liam
 */
@Data
public class TermBatchDeleteVO {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 总数
     */
    private int totalCount;

    /**
     * 成功数量
     */
    private int successCount;

    /**
     * 失败数量
     */
    private int failCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    public static TermBatchDeleteVO success(int totalCount, int successCount, int failCount) {
        TermBatchDeleteVO vo = new TermBatchDeleteVO();
        vo.setSuccess(failCount == 0);
        vo.setTotalCount(totalCount);
        vo.setSuccessCount(successCount);
        vo.setFailCount(failCount);
        return vo;
    }

    public static TermBatchDeleteVO error(String errorMessage) {
        TermBatchDeleteVO vo = new TermBatchDeleteVO();
        vo.setSuccess(false);
        vo.setErrorMessage(errorMessage);
        return vo;
    }
} 