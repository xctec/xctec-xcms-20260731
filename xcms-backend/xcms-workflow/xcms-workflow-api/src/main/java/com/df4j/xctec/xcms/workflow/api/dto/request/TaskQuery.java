package com.df4j.xctec.xcms.workflow.api.dto.request;

import lombok.Data;

@Data
public class TaskQuery {
    private String status;
    private int page = 1;
    private int size = 20;
}
