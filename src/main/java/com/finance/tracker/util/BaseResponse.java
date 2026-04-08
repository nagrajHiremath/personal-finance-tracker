package com.finance.tracker.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable{
    private static final long serialVersionUID = -6697518836477283253L;

    private boolean success;
    private Long timestamp;
    private String message;
    private T payload;

}
