package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 标志性成果表实体类
 * 对应表：biz_achievement
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizAchievement {
    private Long achId; // 成果ID(主键)

    /**
     * 成果类别枚举：
     * 1:落实立德树人根本任务 2:创新产教融合机制 3:打造高水平专业群
     * 4:建设一流核心课程 5:开放优质新形态教材 6:建设高水平双师队伍
     * 7:建设产教融合实训基地 8:构建数字化教学新生态 9:拓展国际交流与合作
     * 10:打造一流区域型高端智库
     */
    private Integer category; // 类别

    /**
     * 成果级别：国家级/省级/市级
     */
    private String level; // 级别
    private String achName; // 成果名称
    private String department; // 组织部门(如：教育部办公厅等，区别于校内部门)
    private Date gotTime; // 颁发时间
    private Long deptId; // 部门ID
    private Long fileId; // 佐证材料文件ID
    private String comment; // 备注

    /**
     * 是否竞赛：0:不是竞赛 1:是竞赛
     */
    private Integer isCompetition; // 是否竞赛

    private Integer teDengJiang; // 特等奖数量
    private Integer yiDengJiang; // 一等奖数量
    private Integer erDengJiang; // 二等奖数量
    private Integer sanDengJiang; // 三等奖数量
    private Integer jinJiang; // 金奖数量
    private Integer yinJiang; // 银奖数量
    private Integer tongJiang; // 铜奖数量
    private Integer youShengJiang; // 优胜奖数量
    private Integer budDengDengCi; // 不定等次数量

    private Long createBy; // 创建人ID(关联sys_user表userId)
    private Integer isDelete; // 0:存在 1:删除
    private Date createTime; // 创建时间(默认当前时间)
}