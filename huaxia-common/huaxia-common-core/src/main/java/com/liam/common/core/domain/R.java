package com.liam.common.core.domain;
import com.liam.common.core.enums.ResultCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-07-18
 * @Description:
 * @Version: 1.0
 */

@Getter
@Setter
public class R<T> {

    private int code;  //定义一些固定的code，前后端商量好的   0  1  请求成功  常量  2  3  枚举

    private String msg;  //?  通常是code的辅助说明  一个code  对应一个msg

    private T data;  //请求某个接口返回的数据list  SysUser  泛型


    public static <T> R<T> ok() {
        return assembleResult(null, ResultCode.SUCCESS);
    }

    public static <T> R<T> ok(T data) {
        return assembleResult(data, ResultCode.SUCCESS);
    }

    public static <T> R<T> fail() {
        return assembleResult(null, ResultCode.FAILED);
    }

    public static <T> R<T> fail(int code, String msg) {
        return assembleResult(code, msg, null);
    }

    /**
     * 指定错误码
     *
     * @param resultCode 指定错误码
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return assembleResult(null, resultCode);
    }

    private static <T> R<T> assembleResult(T data, ResultCode resultCode) {
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setData(data);
        r.setMsg(resultCode.getMsg());
        return r;
    }

    private static <T> R<T> assembleResult(int code, String msg, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }
}