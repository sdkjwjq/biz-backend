package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.BizAchievement;
import org.example.entity.vo.ErrorVO;
import org.example.service.AchievementService;
import org.example.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标志性成果控制层
 * 处理标志性成果基础增删查改HTTP请求
 */
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    /**
     * 根据成果ID查询单条标志性成果信息
     * @param achId 成果ID
     * @return 成果实体/错误信息
     */
    @GetMapping("/{achId}")
    public Object getAchievementById(@PathVariable Long achId) {
        try {
            return achievementService.getAchievementById(achId);
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 成果实体列表/错误信息
     */
    @GetMapping("/")
    public Object listAllAchievements() {
        try {
            List<BizAchievement> achievementList = achievementService.listAllAchievements();
            return achievementList;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 新增标志性成果
     * @param achievement 成果实体（JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/add")
    public Object addAchievement(@RequestBody BizAchievement achievement, HttpServletRequest  request) {
        try {
            Long achId = achievementService.addAchievement(achievement, JWTUtil.getUserIdFromToken(request.getHeader("Authorization")));
            return "标志性成果「" + achievement.getAchName() + "」添加成功，成果ID：" + achId;
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 修改标志性成果信息
     * @param achievement 成果实体（含成果ID，JSON请求体）
     * @return 操作结果/错误信息
     */
    @PostMapping("/update/{id}")
    public Object updateAchievement(@PathVariable("id") Long id,@RequestBody BizAchievement achievement) {
        try {
            achievementService.updateAchievement(id,achievement);
            return "标志性成果「" + achievement.getAchName() + "」修改成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }

    /**
     * 逻辑删除标志性成果
     * @param achId 成果ID（路径参数）
     * @return 操作结果/错误信息
     */
    @PostMapping("/delete/{achId}")
    public Object deleteAchievement(@PathVariable("achId") Long achId) {
        try {
            // 查询成果名称，用于返回友好提示
            BizAchievement achievement = achievementService.getAchievementById(achId);
            achievementService.deleteAchievement(achId);
            return "标志性成果「" + achievement.getAchName() + "」删除成功";
        } catch (Exception e) {
            return new ErrorVO(e.getMessage(), 500);
        }
    }
}