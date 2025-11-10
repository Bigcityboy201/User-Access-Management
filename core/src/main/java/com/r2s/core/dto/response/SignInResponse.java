package com.r2s.core.dto.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInResponse {
    private String token;
    private Date expiredDate;
}































