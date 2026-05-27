package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.Content;
import com.campus.qa.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容管理控制器
 */
@RestController
@RequestMapping("/content")
@CrossOrigin
public class ContentController {
    
    @Autowired
    private ContentService contentService;
    
    /**
     * 分页查询内容列表
     */
    @GetMapping("/page")
    public Result<IPage<Content>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        Page<Content> page = new Page<>(current, size);
        IPage<Content> result = contentService.pageContents(page, keyword, category, status);
        return Result.success(result);
    }
    
    /**
     * 根据分类获取内容
     */
    @GetMapping("/category/{category}")
    public Result<List<Content>> getByCategory(@PathVariable String category) {
        List<Content> contents = contentService.getContentsByCategory(category);
        return Result.success(contents);
    }
    
    /**
     * 获取内容详情
     */
    @GetMapping("/{id}")
    public Result<Content> getById(@PathVariable Long id) {
        Content content = contentService.getById(id);
        if (content != null) {
            contentService.increaseViewCount(id);
            return Result.success(content);
        }
        return Result.error("内容不存在");
    }
    
    /**
     * 发布内容
     */
    @PostMapping
    public Result<String> publish(@RequestBody Content content) {
        boolean success = contentService.publishContent(content);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }
    
    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Content content) {
        content.setId(id);
        boolean success = contentService.updateById(content);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除内容
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = contentService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 更新内容状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
        boolean success = contentService.updateContentStatus(id, data.get("status"));
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
}


