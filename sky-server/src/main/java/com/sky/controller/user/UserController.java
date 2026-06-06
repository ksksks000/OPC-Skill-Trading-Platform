package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * 提供用户端的登录、注册、信息更新接口。
 * 登录和注册接口不需要 JWT 认证（已在拦截器中排除）。
 *
 * JWT 令牌说明：
 * - 登录成功后返回 token，前端需将其存入本地存储
 * - 后续请求需在请求头 "token" 中携带此 token
 * - 拦截器 JwtTokenUserInterceptor 会自动校验并提取用户ID
 */
@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户登录
     *
     * 接收用户名和密码，验证通过后生成 JWT 令牌返回。
     * 此接口不需要认证（已在 WebMvcConfiguration 中排除）。
     *
     * @param userLoginDTO 包含 username 和 password
     * @return 用户信息 + JWT token
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO.getUsername());

        // 验证用户名密码
        User user = userService.login(userLoginDTO);

        // 生成 JWT 令牌，将用户ID写入 claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // 构建返回对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .name(user.getName())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     *
     * 此接口不需要认证（已在 WebMvcConfiguration 中排除）。
     *
     * @param userDTO 包含 username、password、name 等
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        log.info("用户注册：{}", userDTO.getUsername());
        userService.register(userDTO);
        return Result.success("注册成功");
    }

    /**
     * 更新用户信息
     *
     * 需要认证，从 JWT 中提取用户ID。
     *
     * @param userDTO 包含要更新的字段
     * @return 更新结果
     */
    @PutMapping
    public Result<String> update(@RequestBody UserDTO userDTO) {
        log.info("更新用户信息：{}", userDTO);
        // 从JWT中获取当前用户ID，防止前端篡改
        userDTO.setId(com.sky.context.BaseContext.getCurrentId());
        userService.update(userDTO);
        return Result.success("更新成功");
    }
}
