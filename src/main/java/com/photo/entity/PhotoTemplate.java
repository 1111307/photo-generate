package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图片模板实体类
 */
@Data
@TableName("photo_template")
public class PhotoTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板图片路径
     */
    private String imagePath;

    /**
     * 文字区域X坐标
     */
    private Integer textX;

    /**
     * 文字区域Y坐标
     */
    private Integer textY;

    /**
     * 文字区域宽度
     */
    private Integer textWidth;

    /**
     * 文字区域高度
     */
    private Integer textHeight;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 字体颜色（十六进制）
     */
    private String fontColor;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}