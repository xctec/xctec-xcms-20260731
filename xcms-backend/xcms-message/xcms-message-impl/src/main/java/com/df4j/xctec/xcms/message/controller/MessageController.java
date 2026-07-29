package com.df4j.xctec.xcms.message.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.MessageDTO;
import com.df4j.xctec.xcms.message.api.dto.request.MessageInboxQuery;
import com.df4j.xctec.xcms.message.api.dto.request.MessageQuery;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息（业务面）。全 POST 风格。
 */
@RestController
@RequestMapping("/api/message")
@Tag(name = "消息 Message", description = "业务面：消息发送/收件箱/已读")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public ApiResponse<MessageDTO> send(@RequestBody SendMessageCommand command) {
        return ApiResponse.success(messageService.send(command));
    }

    @Operation(summary = "查询收件箱", description = "分页查询当前用户收件箱")
    @PostMapping("/inbox")
    public ApiResponse<PageResult<MessageDTO>> inbox(@RequestBody MessageInboxQuery query) {
        return ApiResponse.success(messageService.inbox(query));
    }

    @Operation(summary = "查询消息详情")
    @PostMapping("/detail")
    public ApiResponse<MessageDTO> detail(@RequestBody IdRequest request) {
        return ApiResponse.success(messageService.getDetail(request.getId()));
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/mark-read")
    public ApiResponse<Void> markRead(@RequestBody IdRequest request) {
        messageService.markRead(request.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "查询已发送消息", description = "分页查询当前用户已发送消息")
    @PostMapping("/sent")
    public ApiResponse<PageResult<MessageDTO>> sent(@RequestBody MessageQuery query) {
        return ApiResponse.success(messageService.sent(query));
    }

    @Operation(summary = "撤回消息")
    @PostMapping("/revoke")
    public ApiResponse<Void> revoke(@RequestBody IdRequest request) {
        messageService.revoke(request.getId());
        return ApiResponse.success();
    }
}
