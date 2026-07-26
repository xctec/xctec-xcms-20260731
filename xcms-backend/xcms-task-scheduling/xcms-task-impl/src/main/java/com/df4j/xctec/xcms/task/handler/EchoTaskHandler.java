package com.df4j.xctec.xcms.task.handler;

import com.df4j.xctec.xcms.task.api.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内置演示处理器：仅打印参数，用于验证调度引擎与 handler 注册链路。
 * 业务模块应实现各自的 TaskHandler 并以 handler_name 注册。
 */
@Component
public class EchoTaskHandler implements TaskHandler {

    private static final Logger log = LoggerFactory.getLogger(EchoTaskHandler.class);

    @Override
    public String getHandlerName() {
        return "ECHO";
    }

    @Override
    public String getGroup() {
        return "system";
    }

    @Override
    public void execute(Map<String, Object> params) {
        log.info("[task][ECHO] executed with params={}", params);
    }
}
