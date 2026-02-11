package com.photo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.Result;
import com.photo.entity.PhotoTemplate;
import com.photo.entity.UsageRecord;
import com.photo.entity.User;
import com.photo.mapper.UsageRecordMapper;
import com.photo.service.PhotoService;
import com.photo.service.UserService;
import com.photo.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UsageRecordMapper usageRecordMapper;

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public Result<Page<User>> getUsers(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            Page<User> userPage = new Page<>(page, size);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(User::getCreateTime);
            
            Page<User> result = userService.page(userPage, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取使用明细
     */
    @GetMapping("/usage-records")
    public Result<Page<UsageRecord>> getUsageRecords(@RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam(required = false) String userId) {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            Page<UsageRecord> recordPage = new Page<>(page, size);
            LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
            
            if (userId != null) {
                wrapper.eq(UsageRecord::getUserId, userId);
            }
            
            wrapper.orderByDesc(UsageRecord::getCreateTime);
            
            Page<UsageRecord> result = usageRecordMapper.selectPage(recordPage, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            Map<String, Object> statistics = new HashMap<>();
            
            // 用户总数
            long userCount = userService.count();
            statistics.put("userCount", userCount);
            
            // 总生成次数
            LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(UsageRecord::getCount);
            List<UsageRecord> records = usageRecordMapper.selectList(wrapper);
            int totalCount = records.stream().mapToInt(UsageRecord::getCount).sum();
            statistics.put("totalCount", totalCount);
            
            // 今日生成次数
            LambdaQueryWrapper<UsageRecord> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.apply("DATE(create_time) = CURDATE()");
            List<UsageRecord> todayRecords = usageRecordMapper.selectList(todayWrapper);
            int todayCount = todayRecords.stream().mapToInt(UsageRecord::getCount).sum();
            statistics.put("todayCount", todayCount);
            
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传模板图片
     */
    @PostMapping("/template/upload")
    public Result<String> uploadTemplate(@RequestParam("file") MultipartFile file) {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            String imagePath = photoService.uploadTemplate(file);
            return Result.success("上传成功", imagePath);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存模板配置
     */
    @PostMapping("/template/save")
    public Result<String> saveTemplate(@RequestBody PhotoTemplate template) {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            boolean success = photoService.saveTemplate(template);
            if (success) {
                return Result.success("保存成功");
            } else {
                return Result.error("保存失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有模板
     */
    @GetMapping("/templates")
    public Result<List<PhotoTemplate>> getAllTemplates() {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            List<PhotoTemplate> templates = photoService.list();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/template/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id) {
        try {
            // 检查是否是管理员
            User currentUser = UserContext.getUser();
            if (currentUser == null || currentUser.getRole() != 1) {
                return Result.error("无权限访问");
            }

            PhotoTemplate template = photoService.getById(id);
            if (template == null) {
                return Result.error("模板不存在");
            }

            // 删除模板图片文件
            try {
                deleteTemplateImageFile(template.getImagePath());
            } catch (IOException e) {
                return Result.error("删除模板图片失败: " + e.getMessage());
            }

            boolean success = photoService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private void deleteTemplateImageFile(String imagePath) throws IOException {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }
        String normalized = imagePath.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        Path path = Paths.get(normalized);
        if (!path.isAbsolute()) {
            String projectRoot = System.getProperty("user.dir");
            path = Paths.get(projectRoot).resolve(normalized);
        }
        Files.deleteIfExists(path);
    }
}
