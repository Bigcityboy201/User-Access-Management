package com.r2s.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserProfileDTO {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private List<String> roleNames;
}
