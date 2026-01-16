package com.photo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.photo.entity.PhotoTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 图片服务接口
 */
public interface PhotoService extends IService<PhotoTemplate> {

    /**
     * 单次生成图片
     */
    String generatePhoto(String text, Long templateId);

    /**
     * 批量生成图片
     */
    List<String> batchGeneratePhotos(List<String> textList, Long templateId);

    /**
     * 导出图片
     */
    void exportPhotos(List<String> imagePaths, HttpServletResponse response);

    /**
     * 上传模板图片
     */
    String uploadTemplate(MultipartFile file);

    /**
     * 保存模板配置
     */
    boolean saveTemplate(PhotoTemplate template);

    /**
     * 获取启用的模板列表
     */
    List<PhotoTemplate> getActiveTemplates();
}