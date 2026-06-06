package com.sky.service;

import com.sky.dto.UserDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    User login(UserLoginDTO userLoginDTO);

    void register(UserDTO userDTO);

    void update(UserDTO userDTO);
}
