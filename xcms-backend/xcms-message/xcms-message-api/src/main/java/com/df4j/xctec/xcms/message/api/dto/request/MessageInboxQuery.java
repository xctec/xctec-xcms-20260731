package com.df4j.xctec.xcms.message.api.dto.request;

import lombok.Data;

@Data
public class MessageInboxQuery {
    private Integer page = 1;
    private Integer size = 20;
    private Boolean read;
    private String msgType;
}
