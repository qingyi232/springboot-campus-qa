package com.campus.qa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.qa.entity.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * 校园资讯Mapper接口
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
}


