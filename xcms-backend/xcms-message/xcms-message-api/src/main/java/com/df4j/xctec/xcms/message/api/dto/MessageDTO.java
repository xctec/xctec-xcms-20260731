package com.df4j.xctec.xcms.message.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageDTO {
    @Schema(description = "消息ID")
    private Long id;
    @Schema(description = "消息编码/模板编码")
    private String msgCode;
    @Schema(description = "消息标题")
    private String title;
    @Schema(description = "消息内容")
    private String content;
    @Schema(description = "发送者ID")
    private Long senderId;
    @Schema(description = "发送者姓名")
    private String senderName;
    @Schema(description = "消息类型（NOTICE/EMAIL/SMS）")
    private String msgType;
    @Schema(description = "优先级（LOW/NORMAL/HIGH/URGENT）")
    private String priority;
    @Schema(description = "状态（DRAFT/SENT/READ）")
    private String status;
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;
    @Schema(description = "是否已读")
    private boolean read;
    @Schema(description = "接收人姓名列表")
    private List<String> recipientNames;
    @Schema(description = "附件列表")
    private List<AttachmentDTO> attachments;
}
