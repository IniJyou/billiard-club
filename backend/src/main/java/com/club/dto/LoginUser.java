package com.club.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {
    private Long id;
    private String username;
    private String realName;
    private Integer role;
}
