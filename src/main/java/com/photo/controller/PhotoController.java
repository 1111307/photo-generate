package com.photo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.common.Result;
import com.photo.entity.PhotoTemplate;
import com.photo.service.PhotoService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片控制器
 */
@RestController
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

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
}