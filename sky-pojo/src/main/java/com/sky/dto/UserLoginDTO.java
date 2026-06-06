package com.sky.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {

    private String username;

    private String password;
}
