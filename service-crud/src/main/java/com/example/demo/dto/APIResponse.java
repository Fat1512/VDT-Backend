package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Builder
@Setter @Getter
public class APIResponse {
    private String message;
    private Object data;
    private HttpStatus status;
}
