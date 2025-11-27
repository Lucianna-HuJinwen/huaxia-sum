import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.liam.common.core.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import com.liam.common.core.utils.ThreadLocalUtil;

import java.time.LocalDateTime;

/**
 * @Author: LiamLMK
 * @CreateTime: 2025-03-28
 * @Description:
 * @Version: 1.0
 */

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 创建人待修改
        this.strictInsertFill(metaObject, "createBy", Long.class,
                ThreadLocalUtil.get(Constants.USER_ID, Long.class));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 更新人待修改
        this.strictUpdateFill(metaObject, "updateBy", Long.class,
                ThreadLocalUtil.get(Constants.USER_ID, Long.class));

    }
}
