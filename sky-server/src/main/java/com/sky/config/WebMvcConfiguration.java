package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置
 *
 * 核心职责：
 * 1. 注册 JWT 拦截器（管理端 + 用户端）
 * 2. 配置 CORS 跨域（使用 CorsFilter，在 Servlet 层处理，优先于拦截器）
 * 3. 扩展 Jackson 消息转换器
 * 4. 配置 OpenAPI 文档
 *
 * 重要：CORS 必须使用 CorsFilter 而非 addCorsMappings，
 * 因为 addCorsMappings 在拦截器之后执行，会导致 OPTIONS 预检请求
 * 被 JWT 拦截器拦截返回 401，从而阻止浏览器发送实际请求。
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册拦截器
     *
     * 拦截规则：
     * - /admin/**  → JwtTokenAdminInterceptor（读取 "token" 请求头）
     * - /user/**   → JwtTokenUserInterceptor（读取 "authentication" 请求头）
     *
     * 排除路径：
     * - /admin/employee/login → 管理员登录
     * - /user/user/login      → 用户登录
     *
     * 注意：/user/ai/** 现在受 JwtTokenUserInterceptor 保护，
     * 需要前端在请求头中携带 authentication 字段。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        // 管理端拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        // 用户端拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login", "/user/user/register");
    }

    /**
     * CORS 跨域过滤器
     *
     * 使用 FilterRegistrationBean 注册 CorsFilter，
     * 确保在 Servlet 层处理 CORS，优先于 Spring MVC 拦截器。
     *
     * 解决的问题：
     * 当前端发送带有自定义请求头（如 authentication）的请求时，
     * 浏览器会先发送 OPTIONS 预检请求。如果 CORS 处理在拦截器之后，
     * JWT 拦截器会因为 OPTIONS 请求没有 token 而返回 401，
     * 导致浏览器认为跨域被拒绝，实际请求永远不会发出。
     *
     * 使用 CorsFilter 后，OPTIONS 预检请求在 Servlet 层就被正确响应，
     * 不会到达拦截器，从而解决了 401 问题。
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的来源（开发环境允许所有，生产环境应限制具体域名）
        config.addAllowedOriginPattern("*");
        // 允许的 HTTP 方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        // 允许的请求头（必须包含 authentication 和 token，否则 JWT 无法传递）
        config.addAllowedHeader("*");
        // 允许携带凭证（Cookie 等）
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CorsFilter(source));
        // 设置最高优先级，确保在所有拦截器之前执行
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OPC技能交易平台接口文档")
                        .version("1.0")
                        .description("OPC技能交易平台接口文档"));
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始扩展消息转换器");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }

}
