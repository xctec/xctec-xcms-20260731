package com.df4j.xctec.xcms.message.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageDTO {
    private Long id;
    private String msgCode;
    private String title;
    private String content;
    private Long senderId;
    private String senderName;
    private String msgType;
    private String priority;
    private String status;
    private LocalDateTime sendTime;
    private boolean read;
    private List<String> recipientNames;
    private List<AttachmentDTO> attachments;
}
