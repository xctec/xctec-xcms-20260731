package com.df4j.xctec.xcms.message.listener;

import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import com.df4j.xctec.xcms.task.api.event.TaskFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 监听任务调度模块发布的 {@link TaskFailedEvent}，发送失败告警通知。
 * <p>
 * 通过事件监听而非被 task 模块直接调用，使 message（上层业务）依赖 task-api（下层基础设施），
 * 依赖方向正确，解除了原先 task-impl → message-api 的反向依赖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskFailedEventListener {

    private final MessageService messageService;

    @EventListener
    public void onTaskFailed(TaskFailedEvent event) {
        TenantContext.TenantInfo original = event.getTenantId() != null
                ? TenantContext.switchTo(event.getTenantId()) : null;
        try {
            SendMessageCommand cmd = new SendMessageCommand();
            cmd.setMsgType("NOTICE");
            cmd.setTitle("定时任务执行失败: " + event.getTaskName());
            cmd.setContent("任务[" + event.getTaskName() + "/" + event.getTaskCode() + "]执行失败: "
                    + event.getErrorMessage());
            cmd.setRecipientIds(new ArrayList<>());
            messageService.send(cmd);
        } catch (Exception ex) {
            log.warn("[message] send task-failed alert failed, taskCode={}", event.getTaskCode(), ex);
        } finally {
            TenantContext.restore(original);
        }
    }
}
