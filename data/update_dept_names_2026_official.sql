USE `biz`;
SET NAMES utf8mb4;

/*
 * 按学校官网机构设置更新部门名称
 *
 * 说明：
 * 1. dept_id 保持不变，只更新 dept_name。
 * 2. 119、120、124 按要求保持原名不变。
 * 3. 107 和 137 合并：只保留 107。
 *    - 137 下的人员改到 107。
 *    - 历史任务、绩效、成果、提交记录中 dept_id=137 的归口也改到 107，
 *      避免 137 停用后统计看不到这部分数据。
 *    - 137 部门停用并逻辑删除。
 *    - 成果展示专用账号 990137 停用。
 */

START TRANSACTION;

-- 1. 更新部门名称
UPDATE `sys_dept` SET `dept_name` = '党委保卫部、保卫处' WHERE `dept_id` = 101;
UPDATE `sys_dept` SET `dept_name` = '党委办公室、院长办公室' WHERE `dept_id` = 105;
UPDATE `sys_dept` SET `dept_name` = '党委教师工作部、人事处' WHERE `dept_id` = 106;
UPDATE `sys_dept` SET `dept_name` = '党委宣传部、网络思想政治工作中心、互联网信息办公室' WHERE `dept_id` = 107;
UPDATE `sys_dept` SET `dept_name` = '党委学生工作部、党委武装部、学工处' WHERE `dept_id` = 108;
UPDATE `sys_dept` SET `dept_name` = '党委组织部、党委统战部、党校' WHERE `dept_id` = 109;
UPDATE `sys_dept` SET `dept_name` = '发展规划处、质量监督与评估处、高等职业教育研究中心' WHERE `dept_id` = 110;
UPDATE `sys_dept` SET `dept_name` = '工会、退休工作处、关工委办公室' WHERE `dept_id` = 112;
UPDATE `sys_dept` SET `dept_name` = '数字经济学院' WHERE `dept_id` = 113;
UPDATE `sys_dept` SET `dept_name` = '国际教育学院' WHERE `dept_id` = 114;
UPDATE `sys_dept` SET `dept_name` = '国有资产管理处、招投标办公室' WHERE `dept_id` = 115;
UPDATE `sys_dept` SET `dept_name` = '化工与制药学院' WHERE `dept_id` = 117;
UPDATE `sys_dept` SET `dept_name` = '继续教育与培训学院' WHERE `dept_id` = 122;
UPDATE `sys_dept` SET `dept_name` = '智慧城建与低空经济学院' WHERE `dept_id` = 123;
UPDATE `sys_dept` SET `dept_name` = '科技与产业部、服务产业研究院、大学科技园管理办公室' WHERE `dept_id` = 125;
UPDATE `sys_dept` SET `dept_name` = '智能交通学院' WHERE `dept_id` = 127;
UPDATE `sys_dept` SET `dept_name` = '图书馆、档案馆' WHERE `dept_id` = 129;
UPDATE `sys_dept` SET `dept_name` = '信息化建设与管理处' WHERE `dept_id` = 132;
UPDATE `sys_dept` SET `dept_name` = '人工智能学院' WHERE `dept_id` = 134;
UPDATE `sys_dept` SET `dept_name` = '审查调查室' WHERE `dept_id` = 135;

-- 2. 107/137 合并：人员和业务归口从 137 调整到 107
UPDATE `sys_user`
SET `dept_id` = 107,
    `update_time` = NOW()
WHERE `dept_id` = 137
  AND `user_id` <> 990137;

UPDATE `biz_task`
SET `dept_id` = 107,
    `update_time` = NOW()
WHERE `dept_id` = 137;

UPDATE `biz_level4_task`
SET `dept_id` = 107,
    `update_time` = NOW()
WHERE `dept_id` = 137;

UPDATE `biz_performance`
SET `dept_id` = 107,
    `update_time` = NOW()
WHERE `dept_id` = 137;

UPDATE `biz_achievement`
SET `dept_id` = 107
WHERE `dept_id` = 137;

UPDATE `biz_material_submission`
SET `submit_dept_id` = 107
WHERE `submit_dept_id` = 137;

UPDATE `biz_material_submission`
SET `manage_dept_id` = 107
WHERE `manage_dept_id` = 137;

-- 3. 停用 137 和 137 对应的成果展示账号
UPDATE `sys_user`
SET `status` = '1',
    `is_delete` = 1,
    `update_time` = NOW()
WHERE `user_id` = 990137;

UPDATE `sys_dept`
SET `status` = '1',
    `is_delete` = 1,
    `leader_id` = NULL,
    `update_time` = NOW()
WHERE `dept_id` = 137;

COMMIT;

-- 查看最终部门和成果展示账号
SELECT
    d.`dept_id` AS `部门ID`,
    d.`dept_name` AS `部门名称`,
    990000 + d.`dept_id` AS `登录账号`,
    CAST(990000 + d.`dept_id` AS CHAR) AS `密码`
FROM `sys_dept` d
WHERE d.`is_delete` = 0
ORDER BY d.`dept_id`;
