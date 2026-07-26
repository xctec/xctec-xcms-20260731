package com.df4j.xctec.xcms.message.controller;

import com.df4j.xctec.xcms.kernel.common.ApiResponse;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.common.dto.IdRequest;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.MessageDTO;
import com.df4j.xctec.xcms.message.api.dto.request.MessageInboxQuery;
import com.df4j.xctec.xcms.message.api.dto.request.MessageQuery;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息（业务面）。全 POST 风格。
 */
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ApiResponse<MessageDTO> send(@RequestBody SendMessageCommand command) {
        return ApiResponse.success(messageService.send(command));
    }

    @PostMapping("/inbox")
    public ApiResponse<PageResult<MessageDTO>> inbox(@RequestBody MessageInboxQuery query) {
        return ApiResponse.success(messageService.inbox(query));
    }

    @PostMapping("/detail")
    public ApiResponse<MessageDTO> detail(@RequestBody IdRequest request) {
        return ApiResponse.success(messageService.getDetail(request.getId()));
    }

    @PostMapping("/mark-read")
    public ApiResponse<Void> markRead(@RequestBody IdRequest request) {
        messageService.markRead(request.getId());
        return ApiResponse.success();
    }

    @PostMapping("/sent")
    public ApiResponse<PageResult<MessageDTO>> sent(@RequestBody MessageQuery query) {
        return ApiResponse.success(messageService.sent(query));
    }

    @PostMapping("/revoke")
    public ApiResponse<Void> revoke(@RequestBody IdRequest request) {
        messageService.revoke(request.getId());
        return ApiResponse.success();
    }
}
