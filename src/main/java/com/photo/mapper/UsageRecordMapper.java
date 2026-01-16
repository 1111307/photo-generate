package com.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.photo.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 使用记录Mapper接口
 */
@Mapper
public interface UsageRecordMapper extends BaseMapper<UsageRecord> {
}