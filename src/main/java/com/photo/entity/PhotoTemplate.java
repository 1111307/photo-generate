package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 图片模板实体类
 */
@Data
@TableName("photo_template")
public class PhotoTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID（多对一关系）
     */
    private String userId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板图片路径
     */
    private String imagePath;

    /**
     * 文字区域X坐标（相对坐标：0-1之间的比例）
     */
    private BigDecimal textX;

    /**
     * 文字区域Y坐标（相对坐标：0-1之间的比例）
     */
    private BigDecimal textY;

    /**
     * 文字区域宽度（相对坐标：0-1之间的比例）
     */
    private BigDecimal textWidth;

    /**
     * 文字区域高度（相对坐标：0-1之间的比例）
     */
    private BigDecimal textHeight;

    /**
     * 覆盖颜色（十六进制）
     */
    private String coverColor;

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