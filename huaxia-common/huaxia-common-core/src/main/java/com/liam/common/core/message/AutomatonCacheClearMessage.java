package com.liam.common.core.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 自动机缓存清理消息
 */

@Data
public class AutomatonCacheClearMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long glossaryId;
    private Long userId;
    private String action;


    public AutomatonCacheClearMessage(Long glossaryId, Long userId, String action) {
        this.glossaryId = glossaryId;
        this.userId = userId;
        this.action = action;
    }
}