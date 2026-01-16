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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

            // 设置字体
            Font font = new Font("微软雅黑", Font.PLAIN, template.getFontSize());
            g2d.setFont(font);

            // 设置字体颜色
            g2d.setColor(Color.decode(template.getFontColor()));

            // 计算文字位置（居中）
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            int x = template.getTextX() + (template.getTextWidth() - textWidth) / 2;
            int y = template.getTextY() + (template.getTextHeight() + textHeight) / 2 - fm.getDescent();

            // 绘制文字
            g2d.drawString(text, x, y);
            g2d.dispose();

            // 保存生成的图片
            String fileName = IdUtil.simpleUUID() + ".png";
            String outputPath = uploadPath + fileName;
            File outputFile = new File(outputPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            ImageIO.write(image, "png", outputFile);

            // 记录使用明细
            saveUsageRecord(1);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("生成图片失败：" + e.getMessage());
        }
    }

    @Override
    public List<String> batchGeneratePhotos(List<String> textList, Long templateId) {
        List<String> imagePaths = new ArrayList<>();
        for (String text : textList) {
            if (text != null && !text.trim().isEmpty()) {
                String imagePath = generatePhoto(text, templateId);
                imagePaths.add(imagePath);
            }
        }

        // 记录批量生成使用明细
        saveUsageRecord(2, textList.size());

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
    public List<PhotoTemplate> getActiveTemplates() {
        LambdaQueryWrapper<PhotoTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhotoTemplate::getStatus, 1);
        return list(wrapper);
    }

    /**
     * 保存使用记录
     */
    private void saveUsageRecord(int operationType) {
        saveUsageRecord(operationType, 1);
    }

    /**
     * 保存使用记录
     */
    private void saveUsageRecord(int operationType, int count) {
        UsageRecord record = new UsageRecord();
        record.setUserId(UserContext.getUserId());
        record.setUsername(UserContext.getUsername());
        record.setOperationType(operationType);
        record.setCount(count);
        usageRecordMapper.insert(record);
    }
}