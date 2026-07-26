package com.df4j.xctec.xcms.task.api;

import java.util.Map;

/**
 * 任务处理器 SPI。各业务模块通过实现该接口并注册为 Spring Bean 来提供具体的任务逻辑，
 * {@code getHandlerName()} 需与 {@code task_schedule.handler_class} 配置的值一致。
 */
public interface TaskHandler {

    /** 处理器名称，对应 task_schedule.handler_class */
    String getHandlerName();

    /** 执行任务；params 为 handler_params(JSON) 解析后的 Map */
    void execute(Map<String, Object> params) throws Exception;

    /** 分组，用于调度分类，默认 default */
    default String getGroup() {
        return "default";
    }
}
