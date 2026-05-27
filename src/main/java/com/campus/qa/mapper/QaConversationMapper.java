package com.campus.qa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.qa.entity.QaConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答对话记录Mapper接口
 */
@Mapper
public interface QaConversationMapper extends BaseMapper<QaConversation> {
}


