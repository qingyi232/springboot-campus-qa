package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.News;
import com.campus.qa.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 校园资讯控制器
 */
@RestController
@RequestMapping("/news")
@CrossOrigin
public class NewsController {
    
    @Autowired
    private NewsService newsService;
    
    /**
     * 分页查询资讯列表
     */
    @GetMapping("/page")
    public Result<IPage<News>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        Page<News> page = new Page<>(current, size);
        IPage<News> result = newsService.pageNews(page, keyword, category, status);
        return Result.success(result);
    }
    
    /**
     * 获取置顶资讯
     */
    @GetMapping("/top")
    public Result<List<News>> getTop() {
        List<News> newsList = newsService.getTopNews();
        return Result.success(newsList);
    }
    
    /**
     * 获取资讯详情
     */
    @GetMapping("/{id}")
    public Result<News> getById(@PathVariable Long id) {
        News news = newsService.getById(id);
        if (news != null) {
            newsService.increaseViewCount(id);
            return Result.success(news);
        }
        return Result.error("资讯不存在");
    }
    
    /**
     * 发布资讯
     */
    @PostMapping
    public Result<String> publish(@RequestBody News news) {
        boolean success = newsService.publishNews(news);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }
    
    /**
     * 更新资讯
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody News news) {
        news.setId(id);
        boolean success = newsService.updateById(news);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    /**
     * 删除资讯
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = newsService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    /**
     * 设置置顶
     */
    @PutMapping("/{id}/top")
    public Result<String> setTop(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
        boolean success = newsService.setTop(id, data.get("isTop"));
        return success ? Result.success("设置成功") : Result.error("设置失败");
    }
    
    /**
     * 点赞
     */
    @PutMapping("/{id}/like")
    public Result<String> like(@PathVariable Long id) {
        boolean success = newsService.like(id);
        return success ? Result.success("点赞成功") : Result.error("点赞失败");
    }
}

