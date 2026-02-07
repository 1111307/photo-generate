package com.photo.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.PhotoTemplate;
import com.photo.entity.UsageRecord;
import com.photo.mapper.PhotoTemplateMapper;
import com.photo.mapper.UsageRecordMapper;
import com.photo.service.PhotoService;
import com.photo.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 图片服务实现类
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoTemplateMapper, PhotoTemplate> implements PhotoService {

    @Autowired
    private UsageRecordMapper usageRecordMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.export-path}")
    private String exportPath;

    @Value("${file.template-path}")
    private String templatePath;

    @Override
    public String generatePhoto(String text, Long templateId) {
        String imagePath = generatePhotoInternal(text, templateId);
        // 获取模板信息用于记录
        PhotoTemplate template = getById(templateId);
        String templateName = template != null ? template.getTemplateName() : "";
        // 记录使用明细
        saveUsageRecord(1, 1, templateId, templateName, text, imagePath);
        return imagePath;
    }

    /**
     * 内部方法：生成图片（不保存记录）
     */
    private String generatePhotoInternal(String text, Long templateId) {
        try {
            // 获取模板
            PhotoTemplate template = getById(templateId);
            if (template == null) {
                throw new RuntimeException("模板不存在");
            }

            // 读取模板图片
            File templateFile = new File(template.getImagePath());
            if (!templateFile.exists()) {
                throw new RuntimeException("模板图片不存在");
            }

            BufferedImage image = ImageIO.read(templateFile);
            Graphics2D g2d = image.createGraphics();

            // 设置抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 清除原有文字区域（用白色填充）
            g2d.setColor(Color.WHITE);
            g2d.fillRect(template.getTextX(), template.getTextY(), template.getTextWidth(), template.getTextHeight());

            // 设置字体
            Font font = new Font("微软雅黑", Font.PLAIN, template.getFontSize());
            g2d.setFont(font);

            // 设置字体颜色
            g2d.setColor(Color.decode(template.getFontColor()));

            // 获取字体度量信息
            FontMetrics fm = g2d.getFontMetrics();
            int lineHeight = fm.getHeight();
            int ascent = fm.getAscent();
            
            // 计算每行最大宽度（留出一些边距）
            int maxWidth = template.getTextWidth() - 20;
            
            // 分割文字为多行
            List<String> lines = new ArrayList<>();
            String[] paragraphs = text.split("\n");
            
            for (String paragraph : paragraphs) {
                if (paragraph.isEmpty()) {
                    lines.add("");
                    continue;
                }
                
                int start = 0;
                while (start < paragraph.length()) {
                    // 找到当前行能容纳的最大字符数
                    int end = start;
                    while (end < paragraph.length()) {
                        String testLine = paragraph.substring(start, end + 1);
                        if (fm.stringWidth(testLine) > maxWidth) {
                            break;
                        }
                        end++;
                    }
                    
                    if (end == start) {
                        // 单个字符就超宽，强制换行
                        lines.add(paragraph.substring(start, start + 1));
                        start++;
                    } else {
                        lines.add(paragraph.substring(start, end));
                        start = end;
                    }
                }
            }
            
            // 计算起始Y坐标（垂直居中）
            int totalHeight = lines.size() * lineHeight;
            int startY = template.getTextY() + (template.getTextHeight() - totalHeight) / 2 + ascent;
            
            // 绘制每一行文字（左对齐）
            int x = template.getTextX() + 30; // 左边距30像素
            for (int i = 0; i < lines.size(); i++) {
                int y = startY + i * lineHeight;
                g2d.drawString(lines.get(i), x, y);
            }
            g2d.dispose();

            // 保存生成的图片
            String fileName = IdUtil.simpleUUID() + ".png";
            String outputPath = uploadPath + fileName;
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            ImageIO.write(image, "png", outputFile);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("生成图片失败：" + e.getMessage());
        }
    }

    @Override
    public List<String> batchGeneratePhotos(List<String> textList, Long templateId) {
        List<String> imagePaths = new ArrayList<>();
        PhotoTemplate template = getById(templateId);
        String templateName = template != null ? template.getTemplateName() : "";
        String textContent = String.join(", ", textList);
        
        for (String text : textList) {
            if (text != null && !text.trim().isEmpty()) {
                // 使用内部方法生成图片，不保存记录
                String imagePath = generatePhotoInternal(text, templateId);
                imagePaths.add(imagePath);
            }
        }

        // 记录批量生成使用明细（只记录一次，包含所有图片路径）
        saveUsageRecord(2, textList.size(), templateId, templateName, textContent, imagePaths);

        return imagePaths;
    }

    @Override
    public void exportPhotos(List<String> imagePaths, HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=photos.zip");

            // 创建ZIP输出流
            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                for (String imagePath : imagePaths) {
                    // 去掉路径前缀
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    File file = new File(uploadPath + fileName);

                    if (file.exists()) {
                        ZipEntry entry = new ZipEntry(fileName);
                        zos.putNextEntry(entry);
                        Files.copy(file.toPath(), zos);
                        zos.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public String uploadTemplate(MultipartFile file) {
        try {
            // 创建模板目录
            File templateDir = new File(templatePath);
            if (!templateDir.exists()) {
                templateDir.mkdirs();
            }

            // 保存文件
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = IdUtil.simpleUUID() + extension;
            String filePath = templatePath + fileName;

            file.transferTo(new File(filePath));

            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("上传失败：" + e.getMessage());
        }
    }

    @Override
    public boolean saveTemplate(PhotoTemplate template) {
        return saveOrUpdate(template);
    }

    @Override
    public PhotoTemplate createTemplate(PhotoTemplate template, MultipartFile file) {
        try {
            // 上传模板图片
            String imagePath = uploadTemplate(file);
            template.setImagePath(imagePath);
            
            // 设置所属用户ID
            template.setUserId(UserContext.getUserId());
            
            // 保存模板
            save(template);
            
            return template;
        } catch (Exception e) {
            throw new RuntimeException("创建模板失败：" + e.getMessage());
        }
    }

    @Override
    public List<PhotoTemplate> getActiveTemplates() {
        LambdaQueryWrapper<PhotoTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTemplate::getStatus, 1)
               .eq(PhotoTemplate::getUserId, UserContext.getUserId());
        return list(wrapper);
    }

    /**
     * 保存使用记录
     */
    private void saveUsageRecord(int operationType, int count, Long templateId, String templateName, String textContent, Object imagePaths) {
        UsageRecord record = new UsageRecord();
        record.setUserId(UserContext.getUserId());
        record.setUsername(UserContext.getUsername());
        record.setOperationType(operationType);
        record.setCount(count);
        record.setTemplateId(templateId);
        record.setTemplateName(templateName);
        record.setTextContent(textContent);
        
        // 保存图片路径
        if (imagePaths != null) {
            if (imagePaths instanceof String) {
                // 单个图片路径
                record.setImagePaths((String) imagePaths);
            } else if (imagePaths instanceof List) {
                // 多个图片路径，转换为JSON字符串
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    record.setImagePaths(mapper.writeValueAsString(imagePaths));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        record.setCreateTime(LocalDateTime.now());
        usageRecordMapper.insert(record);
    }
}