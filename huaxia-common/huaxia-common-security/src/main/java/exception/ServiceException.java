package exception;

import com.liam.common.core.enums.ResultCode;
import lombok.Getter;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-28
 * @Description:
 * @Version: 1.0
 */

@Getter
public class ServiceException extends RuntimeException {

    private ResultCode resultCode;

     public ServiceException(ResultCode resultCode) {
         this.resultCode = resultCode;
     }
}
