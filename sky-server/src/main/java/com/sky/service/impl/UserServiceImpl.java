package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.UserDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.exception.UsernameExistsException;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        User user = userMapper.getByUsername(username);

        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 1. 获取原始密码和数据库密码
        String rawPassword = userLoginDTO.getPassword();
        String dbPassword = user.getPassword();

// 2. 【核心调试】手动对输入的明文进行加密，看看它到底变成了什么
// 注意：BCrypt每次生成的哈希都是不同的（因为盐值随机），但matches应该能识别
        String reEncryptedPassword = passwordEncoder.encode(rawPassword);

        log.info(">>> [调试] 用户输入明文: [{}]", rawPassword);
        log.info(">>> [调试] 数据库存储哈希: [{}]", dbPassword);
        log.info(">>> [调试] 当前明文重新加密后的样子: [{}]", reEncryptedPassword);

// 3. 执行原本的比对逻辑
        boolean isMatch = passwordEncoder.matches(rawPassword, dbPassword);
        log.info(">>> [调试] 最终比对结果: {}", isMatch);

        if (!isMatch) {
            // 额外检查：确认数据库里的哈希是否真的对应 "123456"
            boolean checkStandard = passwordEncoder.matches("123456", dbPassword);
            if (!checkStandard) {
                log.error(">>> [严重错误] 验证失败！数据库中的哈希值根本不是由 '123456' 生成的！");
                log.error(">>> [排查建议] 请检查注册/修改密码接口，当时存入数据库的密码可能不是 '123456'，或者被二次加密了。");
            }
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 用数据库中存储的 BCrypt 密文进行密码校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 推荐写法：利用 Objects.equals 避免空指针，或者确保常量不为 null
        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        user.setPassword(null);
        return user;
    }

    @Override
    public void register(UserDTO userDTO) {
        String username = userDTO.getUsername();

        // 检查用户名是否已存在
        User existUser = userMapper.getByUsername(username);
        if (existUser != null) {
            throw new UsernameExistsException("用户名已存在");
        }

        // 创建新用户，加密密码
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setStatus(StatusConstant.ENABLE);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userMapper.insert(user);
        log.info("用户注册成功：{}", username);
    }

    @Override
    public void update(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 如果有传新密码，则加密
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userMapper.updateById(user);
        log.info("用户信息更新成功：{}", userDTO.getId());
    }
}
