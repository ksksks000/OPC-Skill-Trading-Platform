package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册 DTO
 */
@Data
public class UserDTO implements Serializable {

    private Long id;

    private String username;

    private String password;

    private String name;

    private String phone;

    private String sex;

}