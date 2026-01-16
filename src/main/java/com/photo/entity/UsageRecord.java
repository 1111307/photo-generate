package com.photo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 使用记录实体类
 */
@Data
@TableName("usage_record")
public class UsageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型：1-单次生成，2-批量生成
     */
    private Integer operationType;

    /**
     * 生成数量
     */
    private Integer count;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}