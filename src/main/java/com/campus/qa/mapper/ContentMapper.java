package com.campus.qa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.qa.entity.Content;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容管理Mapper接口
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {
}


