package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDTO {
    private String accessToken;
    private String refreshToken;
    @JsonIgnore
    private String uuid;
}
