package com.liam.translate.service;

import com.liam.translate.domain.dto.CustomTranslateDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ITranslateService {

    Flux<String> customTranslateFlux(CustomTranslateDTO customTranslateDTO);

    String customTranslate(CustomTranslateDTO customTranslateDTO);

    String createAutomaton(List<String> glossaryIdList);
    
    /**
     * 清理指定用户的所有自动机缓存
     */
    void clearUserAutomatonCache(Long userId);
    
    /**
     * 清理指定术语库相关的自动机缓存
     */
    void clearGlossaryAutomatonCache(Long glossaryId);
}
