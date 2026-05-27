package com.campus.qa.config;

import com.campus.qa.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf().disable()
            // 配置CORS
            .cors().and()
            // 配置会话管理
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 配置授权规则
                .authorizeRequests()
                // 允许登录注册接口访问（/auth/login, /auth/register 允许匿名，/auth/current 需要认证）
                .antMatchers("/auth/login", "/auth/register").permitAll()
                .antMatchers("/auth/current").authenticated()
                   // 允许问答接口匿名访问
                   .antMatchers("/qa/ask").permitAll()
                   // 允许诊断接口匿名访问
                   .antMatchers("/diagnostic/**").permitAll()
                   // 允许访问上传的静态资源（头像等）
                   .antMatchers("/uploads/**").permitAll()
                   // 允许 Rasa Action Server 调用的接口（无需认证）
                   .antMatchers("/qa-corpus/search").permitAll()
                   .antMatchers("/news/page").permitAll()
                   .antMatchers("/activity/page").permitAll()
                   .antMatchers("/content/page").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            .and()
            // 禁用表单登录
            .formLogin().disable()
            // 禁用HTTP Basic
            .httpBasic().disable();
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

