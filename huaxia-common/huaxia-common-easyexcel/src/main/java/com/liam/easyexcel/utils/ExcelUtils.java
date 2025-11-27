package com.liam.easyexcel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Excel工具类
 * 
 * @author liam
 */
@Slf4j
public class ExcelUtils {

    /**
     * 读取Excel文件，动态匹配列头
     *
     * @param inputStream 文件输入流
     * @param requiredHeaders 必需的列头列表
     * @param dataConsumer 数据消费者
     * @return 解析结果
     */
    public static <T> ExcelParseResult<T> readExcelWithDynamicHeaders(
            InputStream inputStream,
            List<String> requiredHeaders,
            Consumer<Map<String, String>> dataConsumer) {
        
        ExcelParseResult<T> result = new ExcelParseResult<>();
        
        try {
            DynamicHeaderListener listener = new DynamicHeaderListener(requiredHeaders, dataConsumer, result);
            EasyExcel.read(inputStream, listener).sheet().doRead();
        } catch (Exception e) {
            log.error("Excel解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage("Excel解析失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 动态头部监听器
     */
    private static class DynamicHeaderListener extends AnalysisEventListener<Map<Integer, String>> {
        
        private final List<String> requiredHeaders;
        private final Consumer<Map<String, String>> dataConsumer;
        private final ExcelParseResult<?> result;
        
        private Map<Integer, String> headerIndexMap = new HashMap<>();
        private Map<String, Integer> headerNameMap = new HashMap<>();
        private boolean headerParsed = false;
        private int successCount = 0;
        private int failCount = 0;
        
        public DynamicHeaderListener(List<String> requiredHeaders, 
                                   Consumer<Map<String, String>> dataConsumer,
                                   ExcelParseResult<?> result) {
            this.requiredHeaders = requiredHeaders;
            this.dataConsumer = dataConsumer;
            this.result = result;
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            log.info("解析Excel头部: {}", headMap);
            if (!headerParsed) {
                parseHeadersFromStringMap(headMap);
                headerParsed = true;
                log.info("头部解析完成，匹配到的列: {}", headerNameMap);
            }
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
//            log.info("处理Excel数据行: {}", data);
            if (!headerParsed) {
                log.warn("头部未解析，跳过数据行");
                return;
            }
            
            try {
                Map<String, String> rowData = new HashMap<>();
                for (Map.Entry<String, Integer> entry : headerNameMap.entrySet()) {
                    String headerName = entry.getKey();
                    Integer columnIndex = entry.getValue();
                    String cellValue = data.get(columnIndex);
                    rowData.put(headerName, cellValue != null ? cellValue.trim() : "");
                }
                
//                log.info("解析后的行数据: {}", rowData);
                dataConsumer.accept(rowData);
                successCount++;
            } catch (Exception e) {
                log.error("处理Excel数据行失败", e);
                failCount++;
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            result.setSuccess(failCount == 0);
            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setTotalCount(successCount + failCount);
            
            if (failCount > 0) {
                result.setErrorMessage(String.format("处理失败%d行数据", failCount));
            }
        }

        private void parseHeadersFromStringMap(Map<Integer, String> headMap) {
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                Integer columnIndex = entry.getKey();
                String headerValue = entry.getValue();
                
                if (headerValue != null) {
                    headerValue = headerValue.trim();
                    headerIndexMap.put(columnIndex, headerValue);
                    
                    // 匹配必需的列头
                    for (String requiredHeader : requiredHeaders) {
                        if (requiredHeader.equalsIgnoreCase(headerValue)) {
                            headerNameMap.put(requiredHeader.toLowerCase(), columnIndex);
                            break;
                        }
                    }
                }
            }
            
            // 验证是否找到所有必需的列头
            List<String> missingHeaders = new ArrayList<>();
            for (String requiredHeader : requiredHeaders) {
                if (!headerNameMap.containsKey(requiredHeader.toLowerCase())) {
                    missingHeaders.add(requiredHeader);
                }
            }
            
            if (!missingHeaders.isEmpty()) {
                throw new RuntimeException("缺少必需的列头: " + String.join(", ", missingHeaders));
            }
        }
    }

    /**
     * Excel解析结果
     */
    @Data
    public static class ExcelParseResult<T> {
        private boolean success = true;
        private String errorMessage;
        private int totalCount = 0;
        private int successCount = 0;
        private int failCount = 0;
    }
} 