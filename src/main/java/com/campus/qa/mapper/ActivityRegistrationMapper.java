package com.campus.qa.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.qa.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动报名Mapper接口
 */
@Mapper
public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {

    @Select("SELECT ar.*, a.title as activity_name FROM activity_registration ar LEFT JOIN activity_management a ON ar.activity_id = a.id ${ew.customSqlSegment}")
    IPage<ActivityRegistration> findPage(IPage<ActivityRegistration> page, @Param(Constants.WRAPPER) Wrapper<ActivityRegistration> wrapper);
}


