package com.campus.qa.controller;

import com.campus.qa.common.Result;
import com.campus.qa.entity.User;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 用户头像上传控制器
 */
@RestController
@RequestMapping("/user")
public class UserAvatarController {

    private static final Logger log = LoggerFactory.getLogger(UserAvatarController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            // 获取当前用户
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 确保上传目录存在
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + File.separator + fileName;

            // 保存文件
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + fileName;

            // 更新用户头像
            user.setAvatar(avatarUrl);
            userService.updateById(user);

            log.info("用户 {} 上传头像成功: {}", username, avatarUrl);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}


