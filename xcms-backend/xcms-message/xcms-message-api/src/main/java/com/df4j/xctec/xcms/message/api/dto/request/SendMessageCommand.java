package com.df4j.xctec.xcms.message.api.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SendMessageCommand {
    private String title;
    private String content;
    private List<Long> recipientIds;
    private List<Long> attachmentFileIds;
    private String msgType;
    private String priority;
}
