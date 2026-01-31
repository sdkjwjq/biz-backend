package org.example.service;

import org.example.entity.BizAchievement;
import org.example.mapper.AchievementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 标志性成果业务服务类
 * 处理标志性成果的基础增删查改业务逻辑
 */
@Service
public class AchievementService {

    /**
     * 注入成果Mapper接口，操作数据库
     */
    @Autowired
    private AchievementMapper achievementMapper;

    /**
     * 根据成果ID查询未删除的标志性成果
     * @param achId 成果ID
     * @return 标志性成果实体
     */
    public BizAchievement getAchievementById(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 查询成果
        BizAchievement achievement = achievementMapper.getAchievementById(achId);
        // 成果不存在校验
        if (achievement == null) {
            throw new RuntimeException("标志性成果不存在或已被删除");
        }
        return achievement;
    }

    /**
     * 查询所有未删除的标志性成果列表
     * @return 标志性成果实体列表
     */
    public List<BizAchievement> listAllAchievements() {
        try {
            return achievementMapper.listAllAchievements();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增标志性成果
     * 自动填充创建时间、默认删除状态，返回自增成果ID
     * @param achievement 标志性成果实体（含核心业务字段）
     * @return 自增的成果ID
     */
    public Long addAchievement(BizAchievement achievement,Long createBy) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("新增成果信息不能为空");
        }
        if (achievement.getCategory() == null) {
            throw new RuntimeException("成果类别不能为空");
        }
        if (achievement.getLevel() == null || achievement.getLevel().trim().isEmpty()) {
            throw new RuntimeException("成果级别不能为空");
        }
        if (achievement.getAchName() == null || achievement.getAchName().trim().isEmpty()) {
            throw new RuntimeException("成果名称不能为空");
        }
        if (achievement.getGotTime() == null) {
            throw new RuntimeException("成果颁发时间不能为空");
        }
        // 自动填充公共字段
        achievement.setIsDelete(0); // 默认未删除
        achievement.setCreateTime(new Date()); // 填充当前创建时间
        try {
            // 调用Mapper新增，返回自增ID
            achievementMapper.addAchievement(achievement);
            return achievementMapper.getLatestAchievement().getAchId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改标志性成果信息
     * 仅修改未删除的成果，根据成果ID更新
     * @param achievement 标志性成果实体（含成果ID和需修改字段）
     */
    public void updateAchievement(Long id,BizAchievement achievement) {
        // 核心参数非空校验
        if (achievement == null) {
            throw new RuntimeException("修改成果信息不能为空");
        }
        if (achievementMapper.getAchievementById(id) == null) {
            throw new RuntimeException("成果ID不存在，无法修改");
        }
        try {
            achievementMapper.updateAchievement(achievement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 逻辑删除标志性成果
     * 置is_delete=1，并非物理删除
     * @param achId 成果ID
     */
    public void deleteAchievement(Long achId) {
        // 参数非空校验
        if (achId == null) {
            throw new RuntimeException("成果ID不能为空");
        }
        // 校验成果是否存在
        if (achievementMapper.getAchievementById(achId) == null) {
            throw new RuntimeException("标志性成果不存在或已被删除，无法删除");
        }
        try {
            achievementMapper.deleteAchievement(achId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}