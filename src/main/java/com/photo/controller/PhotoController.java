package com.photo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.photo.common.Result;
import com.photo.entity.PhotoTemplate;
import com.photo.entity.UsageRecord;
import com.photo.mapper.UsageRecordMapper;
import com.photo.service.PhotoService;
import com.photo.util.UserContext;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * 图片控制器
 */
@RestController
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UsageRecordMapper usageRecordMapper;

    /**
     * 获取启用的模板列表
     */
    @GetMapping("/templates")
    public Result<List<PhotoTemplate>> getTemplates() {
        try {
            List<PhotoTemplate> templates = photoService.getActiveTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建模板
     */
    @PostMapping("/create-template")
    public Result<PhotoTemplate> createTemplate(@RequestParam("file") MultipartFile file,
                                                @RequestParam("templateName") String templateName,
                                                @RequestParam(value = "textX", defaultValue = "0") Integer textX,
                                                @RequestParam(value = "textY", defaultValue = "0") Integer textY,
                                                @RequestParam(value = "textWidth", defaultValue = "0") Integer textWidth,
                                                @RequestParam(value = "textHeight", defaultValue = "0") Integer textHeight,
                                                @RequestParam(value = "fontSize", defaultValue = "24") Integer fontSize,
                                                @RequestParam(value = "fontColor", defaultValue = "#000000") String fontColor) {
        try {
            PhotoTemplate template = new PhotoTemplate();
            template.setTemplateName(templateName);
            template.setTextX(textX);
            template.setTextY(textY);
            template.setTextWidth(textWidth);
            template.setTextHeight(textHeight);
            template.setFontSize(fontSize);
            template.setFontColor(fontColor);
            template.setStatus(1); // 默认启用

            PhotoTemplate createdTemplate = photoService.createTemplate(template, file);
            return Result.success("创建模板成功", createdTemplate);
        } catch (Exception e) {
            return Result.error("创建模板失败：" + e.getMessage());
        }
    }

    /**
     * 单次生成图片
     */
    @PostMapping("/generate")
    public Result<String> generatePhoto(@RequestBody Map<String, Object> params) {
        try {
            String text = (String) params.get("text");
            Long templateId = Long.valueOf(params.get("templateId").toString());

            if (text == null || text.trim().isEmpty()) {
                return Result.error("文字内容不能为空");
            }

            String imagePath = photoService.generatePhoto(text, templateId);
            return Result.success("生成成功", imagePath);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量生成图片
     */
    @PostMapping("/batch-generate")
    public Result<List<String>> batchGeneratePhotos(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<String> textList = (List<String>) params.get("textList");
            Long templateId = Long.valueOf(params.get("templateId").toString());

            if (textList == null || textList.isEmpty()) {
                return Result.error("文字列表不能为空");
            }

            List<String> imagePaths = photoService.batchGeneratePhotos(textList, templateId);
            return Result.success("批量生成成功", imagePaths);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出图片
     */
    @PostMapping("/export")
    public void exportPhotos(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        try {
            @SuppressWarnings("unchecked")
            List<String> imagePaths = (List<String>) params.get("imagePaths");
            photoService.exportPhotos(imagePaths, response);
        } catch (Exception e) {
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 上传Excel批量生成
     */
    @PostMapping("/upload-excel")
    public Result<List<String>> uploadExcel(@RequestParam("file") MultipartFile file, 
                                            @RequestParam("templateId") Long templateId) {
        try {
            List<String> textList = new ArrayList<>();
            
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String text = row.getCell(0).getStringCellValue();
                    if (text != null && !text.trim().isEmpty()) {
                        textList.add(text);
                    }
                }
            }
            
            workbook.close();
            
            if (textList.isEmpty()) {
                return Result.error("Excel中没有有效数据");
            }
            
            List<String> imagePaths = photoService.batchGeneratePhotos(textList, templateId);
            return Result.success("批量生成成功", imagePaths);
        } catch (Exception e) {
            return Result.error("处理Excel失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/user-stats")
    public Result<Map<String, Object>> getUserStats() {
        try {
            String userId = UserContext.getUserId();
            Map<String, Object> stats = new HashMap<>();

            // 今日生成数
            LambdaQueryWrapper<UsageRecord> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(UsageRecord::getUserId, userId)
                    .apply("DATE(create_time) = CURDATE()");
            List<UsageRecord> todayRecords = usageRecordMapper.selectList(todayWrapper);
            int todayCount = todayRecords.stream().mapToInt(UsageRecord::getCount).sum();
            stats.put("todayCount", todayCount);

            // 本月生成数
            LambdaQueryWrapper<UsageRecord> monthWrapper = new LambdaQueryWrapper<>();
            monthWrapper.eq(UsageRecord::getUserId, userId)
                    .apply("YEAR(create_time) = YEAR(NOW()) AND MONTH(create_time) = MONTH(NOW())");
            List<UsageRecord> monthRecords = usageRecordMapper.selectList(monthWrapper);
            int monthCount = monthRecords.stream().mapToInt(UsageRecord::getCount).sum();
            stats.put("monthCount", monthCount);

            // 总生成数
            LambdaQueryWrapper<UsageRecord> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(UsageRecord::getUserId, userId);
            List<UsageRecord> totalRecords = usageRecordMapper.selectList(totalWrapper);
            int totalCount = totalRecords.stream().mapToInt(UsageRecord::getCount).sum();
            stats.put("totalCount", totalCount);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户使用记录
     */
    @GetMapping("/user-records")
    public Result<Map<String, Object>> getUserRecords(@RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        try {
            String userId = UserContext.getUserId();

            Page<UsageRecord> recordPage = new Page<>(page, size);
            LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UsageRecord::getUserId, userId)
                    .orderByDesc(UsageRecord::getId);

            Page<UsageRecord> result = usageRecordMapper.selectPage(recordPage, wrapper);

            Map<String, Object> response = new HashMap<>();
            response.put("records", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());

            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}