-- ==============================================-- 双高建设项目初始化SQL脚本（最终版）-- 生成时间：2025-12-12 01:00:54-- 核心调整：-- 1. 移除所有组合部门，13个部门独立创建-- 2. 70名人员均为独立用户，角色统一设为user（role=1）-- 3. 每个部门自动关联负责人-- 4. 任务与人员、部门正确关联-- 执行顺序：严格按照SQL中编号顺序执行-- ==============================================
-- 1. 插入部门数据（13个业务部门 + 1个初始测试部）
INSERT INTO `sys_dept` (`dept_name`, `status`, `is_delete`, `create_time`)
VALUES
    ('人事处', '0', 0, NOW()),
    ('党委办公室', '0', 0, NOW()),
    ('发展规划处', '0', 0, NOW()),
    ('团委', '0', 0, NOW()),
    ('国际教育学院', '0', 0, NOW()),
    ('学工部', '0', 0, NOW()),
    ('宣传部', '0', 0, NOW()),
    ('教务部', '0', 0, NOW()),
    ('机电工程学院', '0', 0, NOW()),
    ('现代教育技术中心', '0', 0, NOW()),
    ('科产部', '0', 0, NOW()),
    ('组织部', '0', 0, NOW()),
    ('继续教育学院', '0', 0, NOW());
-- 2. 插入用户数据（70名业务用户 + 1个初始管理员）
-- 说明：dept_id对应上述部门ID(1-13)，用户名去重，邮箱统一后缀，密码统一123456
-- 2.1 超级管理员（单独插入，role=0）
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES (1, 'admin', '系统管理员', 'admin@xzit.edu.cn', '123456', '0', '0', 0, NOW());

-- 2.2 业务用户（70人，role=1，按部门分组）
-- 组织部 (dept_id=12)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (12, 'zhongwenting', '仲文婷', 'zhongwenting@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (12, 'qiaoxiangdong', '乔向东', 'qiaoxiangdong@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 人事处 (dept_id=1)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (1, 'yubencheng', '于本成', 'yubencheng@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (1, 'zhanghaibo', '张海波', 'zhanghaibo@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (1, 'suxiaoli', '苏晓丽', 'suxiaoli@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 学工部 (dept_id=6)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (6, 'zhangyanyan', '张妍妍', 'zhangyanyan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (6, 'zhangyanyan2', '张研研', 'zhangyanyan2@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 宣传部 (dept_id=7)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (7, 'yanghonglou', '杨宏楼', 'yanghonglou@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 教务部 (dept_id=8)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (8, 'houyahe', '侯亚合', 'houyahe@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'gaoxiangxiu', '高祥秀', 'gaoxiangxiu@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'caokairui', '曹凯瑞', 'caokairui@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'zhouhan', '周寒', 'zhouhan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'ligan', '李敢', 'ligan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'zhangyan', '张岩', 'zhangyan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'maguanghui', '马光辉', 'maguanghui@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'lizonglei', '李宗磊', 'lizonglei@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'zhengyutong', '郑禹彤', 'zhengyutong@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'liugenglong', '刘耿龙', 'liugenglong@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'zhaoxia', '赵侠', 'zhaoxia@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'qiaoxiangdong_jw', '乔向东', 'qiaoxiangdong_jw@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (8, 'houyahe2', '候亚合', 'houyahe2@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 团委 (dept_id=4)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (4, 'sunpeng', '孙鹏', 'sunpeng@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 科产部 (dept_id=11)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (11, 'sunmeng', '孙梦', 'sunmeng@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (11, 'suntingting', '孙婷婷', 'suntingting@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (11, 'zhaozhen', '赵镇', 'zhaozhen@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (11, 'songzihao', '宋子豪', 'songzihao@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (11, 'lihonglei', '李红蕾', 'lihonglei@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (11, 'longhao', '龙浩', 'longhao@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (11, 'wangyanqiu', '王艳秋', 'wangyanqiu@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 现代教育技术中心 (dept_id=10)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (10, 'tuhui', '涂辉', 'tuhui@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (10, 'lixinping', '李新平', 'lixinping@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (10, 'wangfeng', '王锋', 'wangfeng@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 国际教育学院 (dept_id=5)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (5, 'wangjianhua', '王建华', 'wangjianhua@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (5, 'pangshihua', '庞世华', 'pangshihua@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (5, 'fanzhenbo', '范震波', 'fanzhenbo@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (5, 'zhengfang', '郑方', 'zhengfang@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (5, 'shizhuocheng', '石卓成', 'shizhuocheng@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 党委办公室 (dept_id=2)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (2, 'ruanhao', '阮浩', 'ruanhao@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 发展规划处 (dept_id=3)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (3, 'fazhanguihuachu', '发展规划处', 'fazhanguihuachu@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 继续教育学院 (dept_id=13)
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (13, 'chenxiangzhang', '陈祥章', 'chenxiangzhang@xzit.edu.cn', '123456', '1', '0', 0, NOW());

-- 机电工程学院 (dept_id=9) - 共34人
INSERT INTO `sys_user` (`dept_id`, `user_name`, `nick_name`, `email`, `password`, `role`, `status`, `is_delete`, `create_time`)
VALUES
    (9, 'lideliang', '李德亮', 'lideliang@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'wangjunyu', '王军雨', 'wangjunyu@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'zhaoshengjie', '赵圣洁', 'zhaoshengjie@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'chuchao', '褚超', 'chuchao@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'yumingquan', '于鸣泉', 'yumingquan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'wangcuiying', '王翠英', 'wangcuiying@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'gaoxu', '高许', 'gaoxu@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'quanning', '权宁', 'quanning@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'liuqingyong', '刘清勇', 'liuqingyong@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'maoshaowen', '毛少文', 'maoshaowen@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'zhajianlin', '查剑林', 'zhajianlin@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'yanshuo', '燕硕', 'yanshuo@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'lizhonghuan', '李忠焕', 'lizhonghuan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'zhuoziming', '卓自明', 'zhuoziming@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'mashiliang', '马士良', 'mashiliang@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'zhoutianpei', '周天沛', 'zhoutianpei@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'jihainbin', '纪海宾', 'jihainbin@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'fanlili', '樊丽丽', 'fanlili@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'chenaolin', '陈奥林', 'chenaolin@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'fanshuoshuo', '范硕硕', 'fanshuoshuo@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'wangweinan', '王伟男', 'wangweinan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'meidong', '梅栋', 'meidong@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'mengbaoxing', '孟宝星', 'mengbaoxing@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'gaohuanqin', '高缓钦', 'gaohuanqin@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'liujuan', '刘娟', 'liujuan@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'zhoubo', '周波', 'zhoubo@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'liuhuail乐', '刘怀乐', 'liuhuail@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'yuxinming', '余心明', 'yuxinming@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'zhanguobing', '詹国兵', 'zhanguobing@xzit.edu.cn', '123456', '1', '0', 0, NOW()),
    (9, 'lisai', '李赛', 'lisai@xzit.edu.cn', '123456', '1', '0', 0, NOW());


-- 3. 设置部门负责人（关键：根据用户插入顺序匹配user_id，确保准确）
-- 说明：user_id=1是admin，user_id从2开始为业务用户，以下映射关系为人工核对后结果
UPDATE `sys_dept` SET `leader_id` = 3 WHERE `dept_id` = 1; -- 人事处负责人：于本成
UPDATE `sys_dept` SET `leader_id` = 40 WHERE `dept_id` = 2; -- 党委办公室负责人：阮浩
UPDATE `sys_dept` SET `leader_id` = 41 WHERE `dept_id` = 3; -- 发展规划处负责人：发展规划处
UPDATE `sys_dept` SET `leader_id` = 20 WHERE `dept_id` = 4; -- 团委负责人：孙鹏
UPDATE `sys_dept` SET `leader_id` = 27 WHERE `dept_id` = 5; -- 国际教育学院负责人：王建华
UPDATE `sys_dept` SET `leader_id` = 6 WHERE `dept_id` = 6; -- 学工部负责人：张妍妍
UPDATE `sys_dept` SET `leader_id` = 8 WHERE `dept_id` = 7; -- 宣传部负责人：杨宏楼
UPDATE `sys_dept` SET `leader_id` = 9 WHERE `dept_id` = 8; -- 教务部负责人：侯亚合
UPDATE `sys_dept` SET `leader_id` = 43 WHERE `dept_id` = 9; -- 机电工程学院负责人：李德亮
UPDATE `sys_dept` SET `leader_id` = 37 WHERE `dept_id` = 10; -- 现代教育技术中心负责人：涂辉
UPDATE `sys_dept` SET `leader_id` = 21 WHERE `dept_id` = 11; -- 科产部负责人：孙梦
UPDATE `sys_dept` SET `leader_id` = 2 WHERE `dept_id` = 12; -- 组织部负责人：仲文婷
UPDATE `sys_dept` SET `leader_id` = 42 WHERE `dept_id` = 13; -- 继续教育学院负责人：陈祥章


-- 4. 插入核心项目数据（仅1个：双高建设）
INSERT INTO `biz_project` (`project_name`, `project_code`, `leader_id`, `start_date`, `end_date`, `status`, `is_delete`, `create_time`)
VALUES ('双高建设', 'SG-ZONG', 1, '2025-01-01', '2025-12-31', '1', 0, NOW());

-- 5. 插入项目阶段数据（仅1个：2025年度建设）
INSERT INTO `biz_project_phase` (`project_id`, `phase_name`, `start_date`, `end_date`, `is_current`, `is_delete`)
VALUES (1, '2025年度建设', '2025-01-01', '2025-12-31', '1', 0);



-- 6. 插入一级任务（10个）
-- ==============================================
-- 双高建设项目 - 一级任务全量插入SQL
-- 核心规则：
-- 1. 仅保留任务名称、编码等明确信息，无来源数据字段全部置空
-- 2. parent_id=0、ancestors='0'、level=1 标识一级任务
-- 3. project_id=1 关联双高建设项目，phase=2025 对应年度阶段
-- 4. dept_id/principal_id/leader_id 匹配已初始化的部门和用户
-- ==============================================
INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
-- 一级任务1：SG01 实施“三维联动”工程，塑造“价值引领、启智润心”立德树人新格局
(1, 0, '0', 2025, 'SG01', '实施“三维联动”工程，塑造“价值引领、启智润心”立德树人新格局', 1,
 6, 6, 8, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务2：SG02 实施“三机健全”工程，创新“多方参与、主动作为”产教融合机制
(1, 0, '0', 2025, 'SG02', '实施“三机健全”工程，创新“多方参与、主动作为”产教融合机制', 1,
 11, 21, 22, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务3：SG03 实施“三需对接”工程，打造“匹配需求、要素集聚”的金专业
(1, 0, '0', 2025, 'SG03', '实施“三需对接”工程，打造“匹配需求、要素集聚”的金专业', 1,
 8, 9, 10, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务4：SG04 实施“三措并举”工程，打造“对接岗位、数智融合”的金课程
(1, 0, '0', 2025, 'SG04', '实施“三措并举”工程，打造“对接岗位、数智融合”的金课程', 1,
 8, 11, 12, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务5：SG05 实施“革故鼎新”工程，打造“双元开发、形态多样”的金教材
(1, 0, '0', 2025, 'SG05', '实施“革故鼎新”工程，打造“双元开发、形态多样”的金教材', 1,
 8, 13, 14, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务6：SG06 实施“卓越匠师”工程，打造“结构合理、技艺精湛”的金教师
(1, 0, '0', 2025, 'SG06', '实施“卓越匠师”工程，打造“结构合理、技艺精湛”的金教师', 1,
 1, 3, 4, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务7：SG07 实施“智造共享”工程，打造“场景真实、开放融合”的金基地
(1, 0, '0', 2025, 'SG07', '实施“智造共享”工程，打造“场景真实、开放融合”的金基地', 1,
 10, 37, 38, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务8：SG08 实施“数智赋能”工程，构建“技术驱动、数据变革”的教学生态
(1, 0, '0', 2025, 'SG08', '实施“数智赋能”工程，构建“技术驱动、数据变革”的教学生态', 1,
 10, 39, 40, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务9：SG09 实施“文技出海”工程，探索“教随产出、校企同行”国际化发展新模式
(1, 0, '0', 2025, 'SG09', '实施“文技出海”工程，探索“教随产出、校企同行”国际化发展新模式', 1,
 5, 27, 28, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- 一级任务10：SG10 成立工程机械产业发展研究院，打造一流区域型高端智库
(1, 0, '0', 2025, 'SG10', '成立工程机械产业发展研究院，打造一流区域型高端智库', 1,
 3, 41, 42, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW());


-- 7. 插入二级任务
-- ==============================================
-- 双高建设项目 - 二级任务全量插入SQL
-- 核心规则：
-- 1. parent_id 对应一级任务ID(1-10)，ancestors 格式为 '0,父任务ID'
-- 2. level=2 标识二级任务，project_id=1 固定关联双高建设项目
-- 3. 无来源数据字段全部置空，dept/principal/leader 继承父任务配置
-- 4. phase=2025，status=0(未开始)，is_delete=0
-- ==============================================
INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
-- ===================== SG01 子任务（parent_id=1） =====================
(1, 1, '0,1', 2025, 'SG0101', '支部联建共创，拓展协同育人新路径', 2,
 6, 6, 8, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 1, '0,1', 2025, 'SG0102', '联动三个课堂，丰富育人工作新场景', 2,
 6, 6, 8, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 1, '0,1', 2025, 'SG0103', '联结虚实资源，构建立体育人新平台', 2,
 6, 6, 8, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG02 子任务（parent_id=2） =====================
(1, 2, '0,2', 2025, 'SG0201', '健全合作发展机制，促进多主体深度参与', 2,
 11, 21, 22, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 2, '0,2', 2025, 'SG0202', '健全议事决策机制，激发多主体主动作为', 2,
 11, 21, 22, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 2, '0,2', 2025, 'SG0203', '健全项目执行机制，强化多主体协同落实', 2,
 11, 21, 22, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG03 子任务（parent_id=3） =====================
(1, 3, '0,3', 2025, 'SG0301', '对接岗位需求，共订人才培养方案', 2,
 8, 9, 10, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 3, '0,3', 2025, 'SG0302', '对接成才需要，创新培养培训模式', 2,
 8, 9, 10, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 3, '0,3', 2025, 'SG0303', '对接产业需要，健全持续发展机制', 2,
 8, 9, 10, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG04 子任务（parent_id=4） =====================
(1, 4, '0,4', 2025, 'SG0401', '集聚各方智慧，打造系统化一流核心课程', 2,
 8, 11, 12, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 4, '0,4', 2025, 'SG0402', '突出学生主体，探索多元化课堂教学模式', 2,
 8, 11, 12, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 4, '0,4', 2025, 'SG0403', '搭建信息平台，构建智能多维度评价体系', 2,
 8, 11, 12, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG05 子任务（parent_id=5） =====================
(1, 5, '0,5', 2025, 'SG0501', '优化教学内容，校企合作开发多形态教材', 2,
 8, 13, 14, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 5, '0,5', 2025, 'SG0502', '整合平台资源，打造品高质优数字化教材', 2,
 8, 13, 14, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 5, '0,5', 2025, 'SG0503', '抓好过程管理，规范教材编审选用全流程', 2,
 8, 13, 14, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG06 子任务（parent_id=6） =====================
(1, 6, '0,6', 2025, 'SG0601', '坚持第一标准，锻造品质卓越大先生', 2,
 1, 3, 4, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 6, '0,6', 2025, 'SG0602', '畅通引才渠道，打造技能卓越大匠师', 2,
 1, 3, 4, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 6, '0,6', 2025, 'SG0603', '健全培养体系，打造协同卓越大团队', 2,
 1, 3, 4, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG07 子任务（parent_id=7） =====================
(1, 7, '0,7', 2025, 'SG0701', '虚实结合，共建产教融合实践中心', 2,
 10, 37, 38, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 7, '0,7', 2025, 'SG0702', '智慧管训，推进实践教学提质增效', 2,
 10, 37, 38, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 7, '0,7', 2025, 'SG0703', '协同合作，助推实践中心开放共享', 2,
 10, 37, 38, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG08 子任务（parent_id=8） =====================
(1, 8, '0,8', 2025, 'SG0801', '对接数字变革，推动专业数智化升级', 2,
 10, 39, 40, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 8, '0,8', 2025, 'SG0802', '优化资源平台，助力教学数智化改革', 2,
 10, 39, 40, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 8, '0,8', 2025, 'SG0803', '深化数据分析，促进评价数智化转型', 2,
 10, 39, 40, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG09 子任务（parent_id=9） =====================
(1, 9, '0,9', 2025, 'SG0901', '厚植技术优势，开展本土化技能人才培养', 2,
 5, 27, 28, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 9, '0,9', 2025, 'SG0902', '集聚资源优势，推动职教与企业标准输出', 2,
 5, 27, 28, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 9, '0,9', 2025, 'SG0903', '深化合作办学，助力徐工院品牌享誉海外', 2,
 5, 27, 28, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

-- ===================== SG10 子任务（parent_id=10） =====================
(1, 10, '0,10', 2025, 'SG1001', '强化调研打造产业发展“瞭望塔”，助力政府科学决策', 2,
 3, 41, 42, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 10, '0,10', 2025, 'SG1002', '聚焦智慧打造技术标准“输出源”，助力产业健康发展', 2,
 3, 41, 42, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW()),

(1, 10, '0,10', 2025, 'SG1003', '产助教改打造教学标准“孵化器”，助力技能迭代升级', 2,
 3, 41, 42, NULL, NULL, NULL, NULL,
 '1', 0.00, 0.00, 1.00, 0, '0', 0, NOW(), NOW());


-- 7. 插入三级任务
INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2025, 'SG01010225', '1-1-2召开产业链党组织年度工作会议，明确工作计划和工作目标', 3,
     12, 2, 2, '召开工作会议1次', NULL, '通过召开产业链党组织年度工作会议统一思想、形成共识，覆盖理论联学、人才共育等方面，明确工作计划和工作目标，预期在拓展协同育人路径方面实现突破，为校企党组织各项工作顺利开展保驾护航。',
     '1-1-2-1产业链党组织年度工作计划和工作目标等材料1套
1-1-2-2产业链党组织年度工作会议及活动照片1组
1-1-2-3校企党建共建案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2025, 'SG01010325', '1-1-3开展年度理论联学、要事联商、活动联办、工作联抓等等主题活动1-2次，推进党建工作交流', 3,
     12, 2, 2, '开展主题活动1-2次', NULL, '通过搭建共建交流平台，实施“走出去+引进来”的方式，组织成员单位间覆盖理论学习、业务交流、座谈研讨等，探索形成“四联+”党建工作交流机制，为校企党组织良性运转奠定思想基础。',
     '1-1-3-1产业链党组织理论联学、要事联商、活动联办、工作联抓等主题活动资料、主旨发言材料1套
1-1-3-2产业链党组织主题活动照片1组
1-1-3-3校企党建共建党课材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2025, 'SG01010425', '1-1-4开展挂职锻炼、互兼互聘2-3人，共同实施科研或质量工程项目2-3项', 3,
     1, 3, 3, '互兼互聘3人，科研项目3项、质量工程项目3项', NULL, '通过开展互兼互聘形成资源共享、优势互补格局，覆盖科研攻关、质量工程等方面，明确合作流程，预期在科研、质量工程等方面实现共同攻关、联合申报，为推动校企双方深度合作奠定基础。',
     '1-1-4-1校企员工互兼互聘文件1套
1-1-4-2校企员工互兼互聘名单明细
1-1-4-3校企科研项目、质量工程申报或立项材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2025, 'SG01010525', '1-1-5以产业链党组织为依托，开展企业优秀实践育人案例评选活动', 3,
     6, 6, 6, '开展活动1次，案例1个', NULL, '通过开展企业优秀实践育人案例评选形成可借鉴、可推广的实践育人典型案例，覆盖新员工适应能力培训等环节，明确适用对象和场景，预期在行业内实现广泛推广、借鉴，为广大学校和企业提供学习交流的平台。',
     '1-1-5-1企业优秀实践育人案例评选办法1套
1-1-5-2企业优秀实践育人通知、评选、获奖证书或文件等过程材料1套
1-1-5-3企业优秀实践育人案例集1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2025, 'SG01010625', '1-1-6新建校级“双带头人”教师党支部书记工作室3个', 3,
     12, 2, 2, '建设工作室3个', NULL, '通过开展“双带头人”教师党支部书记工作室，创建形成党建与业务“双融双促”格局，覆盖全体教师党支部，明确建设要求，预期新建校级“双带头人”教师党支部书记工作室3个，为促进党建与业务深度融合提供组织保障。',
     '1-1-6-1校级首批“双带头人”教师党支部书记工作室创建标准、评选办法1套
1-1-6-2学校首批“双带头人”教师党支部书记工作室培育建设名单等材料1套
1-1-6-3“双带头人”教师党支部书记工作室公众微信推送宣传材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2026, 'SG01010726', '1-1-7召产业链党组织年度工作会议，明确工作计划和工作目标', 3,
     12, 2, 2, '召开工作会议1次', NULL, '通过召开产业链党组织年度工作会议，发挥好桥梁纽带作用，覆盖理论联学、人才共育等方面，明确工作计划和工作目标，预期在拓展协同育人路径方面实现新突破，为校企党组织各项工作顺利开展保驾护航。',
     '1-1-7-1产业链党组织年度工作会议手册
1-1-7-2产业链党组织年度工作计划和工作目标等材料1套
1-1-7-3产业链党组织年度工作会议及活动照片1组
1-1-7-4相关报道
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2026, 'SG01010826', '1-1-8开展年度理论联学、要事联商、活动联办、工作联抓等等主题活动1-2次，推进党建工作交流', 3,
     12, 2, 2, '开展主题活动1-2次', NULL, '通过开展理论联学、要事联商、活动联办、工作联抓等主题活动，形成党建工作交流机制，覆盖党建工作、项目建设等动主题，明确学习内容，预期以结对共建活动为纽带，进一步深化合作交流。',
     '1-1-8-1产业链党组织理论联学、要事联商、活动联办、工作联抓等主题活动资料、主旨发言材料1套
1-1-8-2产业链党组织主题活动照片1组
1-1-8-3校企党建共建党课材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2026, 'SG01010926', '1-1-9开展互兼互聘2-3人，共同实施科研或质量工程项目2-3项', 3,
     1, 3, 3, '互兼互聘3人，科研项目3项、质量工程项目3项', NULL, '通过开展挂互兼互聘形成资源共享、优势互补格局，覆盖科研攻关、质量工程等方面，明确合作流程，预期在科研、质量工程等方面实现共同攻关、联合申报，为推动校企双方深度合作奠定基础。',
     '1-1-9-1校企员工互兼互聘名单明细
1-1-9-2校企科研项目申报或立项材料1套
1-1-9-3质量工程申报或立项材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2026, 'SG01011026', '1-1-10以产业链党组织为依托，开展企业优秀育人导师评选活动', 3,
     6, 6, 6, '开展活动1次，优秀育人导师1名', NULL, '通过开展企业优秀育人导师评选形成可借鉴、可推广的实践育人典型案例，覆盖新员工适应能力培养等环节，明确适用对象和场景，预期在行业内实现广泛推广、借鉴，为广大学校和企业提供学习交流的平台。',
     '1-1-10-1企业优秀育人导师评选办法1套
1-1-10-2企业优秀育人导师事迹材料1套
1-1-10-3企业优秀育人导师评选活动照片1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2026, 'SG01011126', '1-1-11省级及以上媒体报道2篇', 3,
     7, 8, 8, '媒体报道2篇', '省级及以上', '通过总结、凝练协同育人做法形成典型案例，预期在省级媒体发布报道2篇，提升专业群的影响力。',
     '1-1-11-1省级媒体报道网址链接2个、报道全文', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2026, 'SG01011226', '1-1-12新建校级“双带头人”教师党支部书记工作室”4个', 3,
     12, 2, 2, '建设工作室4个', NULL, '通过开展“双带头人”教师党支部书记工作室，创建形成党建与业务“双融双促”格局，覆盖全体教师党支部，明确建设要求，预期新建校级“双带头人”党支部书记工作室”4个，为促进党建与业务深度融合提供组织保障。',
     '1-1-12-1学校第二批“双带头人”教师党支部书记工作室创建通知、评选、结果名单等材料1套
1-1-12-2学校首批“双带头人”教师党支部书记工作室中期检查材料1套
1-1-12-3首批“双带头人”教师党支部书记工作室成果推广材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2027, 'SG01011327', '1-1-13召开产业链党组织年度工作会议，明确工作计划和工作目标', 3,
     12, 2, 2, '召开工作会议1次', NULL, '通过召开产业链党组织年度工作会议，凝聚党建合力，覆盖理论联学、人才共育等方面，明确工作计划和工作目标，预期在拓展协同育人路径方面实现新突破，推动高质量发展落地生根。',
     '1-1-13-1产业链党组织年度工作会议手册
1-1-13-2产业链党组织年度工作计划和工作目标等材料1套
1-1-13-3相关活动过程照片
1-1-13-4相关新闻报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2027, 'SG01011427', '1-1-14开展年度理论联学、要事联商、活动联办、工作联抓等等主题活动1-2次，推进党建工作交流', 3,
     12, 2, 2, '开展主题活动1-2次', NULL, '通过开展理论联学、要事联商、活动联办、工作联抓等主题活动，构建“党建+业务+服务”工作机制，覆盖主题教育、党日活动等方面，预期创建党建共建品牌，使党组织建设标准化、规范化。',
     '1-1-14-1产业链党组织理论联学、要事联商、活动联办、工作联抓等主题活动资料、主旨发言材料1套
1-1-14-2产业链党组织主题活动照片1组
1-1-14-3校企党建共建党课材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2027, 'SG01011527', '1-1-15开展挂职锻炼、互兼互聘2-3人，共同实施科研或质量工程项目2-3项', 3,
     1, 3, 3, '互兼互聘3人，科研项目3项、质量工程项目3项', NULL, '通过开展互兼互聘形成资源共享、优势互补格局，覆盖科研攻关、质量工程等方面，明确合作流程，预期在科研、质量工程等方面实现共同攻关、联合申报，为推动校企双方深度合作奠定基础。',
     '1-1-15-1校企员工互兼互聘名单明细
1-1-15-2校企科研项目申报或立项材料1套
1-1-15-3质量工程申报或立项材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2027, 'SG01011627', '1-1-16以产业链党组织为依托，开展学生-青工同台竞技活动', 3,
     8, 9, 9, '开展活动1次', NULL, '通过开展学生-青工同台竞技活动，形成比、学、赶、超氛围，明确学生职业发展方向，预期在经验交流、技能提升等方面取得显著效果，为实现学生高质量就业打下基础。',
     '1-1-16-1学生-青工同台竞技活动方案1套
1-1-16-2学生-青工同台竞技活动评选结果和活动照片1套
1-1-16-3相关新闻报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2027, 'SG01011727', '1-1-17省级及以上媒体报道3篇', 3,
     7, 8, 8, '媒体报道3篇', '省级及以上', '通过总结、凝练协同育人做法形成典型案例，预期在省级媒体发布报道3篇，提升专业群的影响力。',
     '1-1-17-1省级媒体报道网址链接3个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2027, 'SG01011827', '1-1-18新建校级“双带头人”教师党支部书记工作室”4个', 3,
     12, 2, 2, '建设工作室4个', NULL, '通过开展“双带头人”教师党支部书记工作室”创建形成党建与业务“双融双促”格局，覆盖全体教师党支部，明确建设要求，预期新建校级“双带头人”教师党支部书记工作室”4个，为促进党建与业务深度融合提供组织保障。',
     '1-1-18-1学校第三批“双带头人”教师党支部书记工作室创建通知、评选、结果名单等材料1套
1-1-18-2学校第一批“双带头人”教师党支部书记工作室验收材料1套
1-1-18-3学校第二批“双带头人”教师党支部书记工作室中期检查材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2028, 'SG01011928', '1-1-19召开产业链党组织年度工作会议，明确工作计划和工作目标', 3,
     12, 2, 2, '召开工作会议1次', NULL, '通过召开产业链党组织年度工作会议统一思想、形成共识，覆盖理论联学、人才共育等方面，明确工作计划和工作目标，预期在拓展协同育人路径方面实现新突破，推进产业链党组织向专业化议事、项目化运行、双向化服务转变。',
     '1-1-19-1产业链党组织年度工作计划和工作目标等材料1套
1-1-19-2产业链党组织年度工作会议及活动照片1组
1-1-19-3校企党建共建案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2028, 'SG01012028', '1-1-20开展年度理论联学、要事联商、活动联办、工作联抓等等主题活动1-2次，推进党建工作交流', 3,
     12, 2, 2, '开展主题活动1-2次', NULL, '通过开展理论联学、要事联商、活动联办、工作联抓等主题活动，形成党建工作交流机制，覆盖主题教育、党日活动等方面，明确学习内容，预期在省级最佳党日活动申报等方面实现新突破。',
     '1-1-20-1产业链党组织理论联学、要事联商、活动联办、工作联抓等主题活动资料、主旨发言材料1套
1-1-20-2产业链党组织主题活动照片1组
1-1-20-3校企党建共建党课材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2028, 'SG01012128', '1-1-21开展挂职锻炼、互兼互聘2-3人，共同实施科研或质量工程项目2-3项', 3,
     1, 3, 3, '互兼互聘3人，科研项目3项、质量工程项目3项', NULL, '通过开展互兼互聘形成资源共享、优势互补格局，覆盖科研攻关、质量工程等方面，明确合作流程，预期在科研、质量工程等方面实现共同攻关、联合申报，为推动校企双方深度合作奠定基础。',
     '1-1-21-1校企员工互兼互聘名单明细
1-1-21-2校企科研项目申报立项材料
1-1-21-3质量工程申报或立项材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2028, 'SG01012228', '1-1-22开展“红色匠心、产业报国”宣传活动，提炼和总结产业链党建文化精神内涵', 3,
     12, 2, 2, '开展宣传活动1次，形成总结1份', NULL, '通过开展“红色匠心、产业报国”宣传活动，形成传承匠心、技能报国氛围，明确产业链党建文化精神内涵，预期开展宣传活动1次，为创建特色党建文化品牌奠定基础。',
     '1-1-22-1“红色匠心、产业报国”宣传活动方案1套
1-1-22-2“红色匠心、产业报国”活动照片1组
1-1-22-3“红色匠心、产业报国”活动总结材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2028, 'SG01012328', '1-1-23省级及以上媒体报道2篇', 3,
     7, 8, 8, '媒体报道2篇', '省级及以上', '通过总结、凝练协同育人做法形成典型案例，预期在省级媒体发布报道2篇，提升专业群的影响力。',
     '1-1-23-1省级媒体报道网址链接2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2028, 'SG01012428', '1-1-24新建“双带头人”教师党支部书记工作室3个', 3,
     12, 2, 2, '建设工作室3个', NULL, '通过开展“双带头人”教师党支部书记工作室”创建形成党建与业务“双融双促”格局，覆盖全体教师党支部，明确建设要求，预期新建校级“双带头人党支部书记工作室”3个，为促进党建与业务深度融合提供组织保障。',
     '1-1-24-1学校第四批“双带头人”教师党支部书记工作室创建通知、评选、结果名单材料1套
1-1-24-2学校第二批“双带头人”教师党支部书记工作室验收材料1套
1-1-24-3学校第三批“双带头人”教师党支部书记工作室中期检查材料1套
1-1-24-4学校第二批“双带头人”教师党支部书记工作室推广材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2029, 'SG01012529', '1-1-25召开产业链党组织年度工作会议，明确工作计划和工作目标', 3,
     12, 2, 2, '召开工作会议1次', NULL, '通过召开产业链党组织年度工作会议，聚焦年度“共建项目清单”落实，覆盖理论联学、人才共育等方面，明确工作计划和工作目标，预期在拓展协同育人路径方面实现新突破，凝聚发展合力。',
     '1-1-25-1产业链党组织年度工作手册、计划和工作目标等材料1套
1-1-25-2产业链党组织年度工作会议及活动照片1组
1-1-25-3校企党建共建案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2029, 'SG01012629', '1-1-26开展年度理论联学、要事联商、活动联办、工作联抓等等主题活动1-2次，推进党建工作交流', 3,
     12, 2, 2, '开展主题活动1-2次', NULL, '通过开展理论联学、要事联商、活动联办、工作联抓等主题活动，形成党建工作交流机制，覆盖主题教育、党日活动等方面，明确学习内容，预期在省级最佳党日活动申报等方面实现新突破，推动党建联建工作高质量发展。',
     '1-1-26-1产业链党组织理论联学、要事联商、活动联办、工作联抓等主题活动资料、主旨发言材料1套
1-1-26-2产业链党组织联学活动照片1组
1-1-26-3校企党建共建党课材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2029, 'SG01012729', '1-1-27开展挂职锻炼、互兼互聘2-3人，共同实施科研或质量工程项目2-3项', 3,
     1, 3, 3, '互兼互聘3人，科研项目3项、质量工程项目3项', NULL, '通过开展挂职锻炼、互兼互聘形成资源共享、优势互补格局，覆盖科研攻关、质量工程等方面，明确合作流程，预期在科研、质量工程等方面实现共同攻关、联合申报，为推动校企双方深度合作奠定基础。',
     '1-1-27-1校企员工互兼互聘名单明细
1-1-27-2校企科研项目申报或立项材料1套
1-1-27-3质量工程申报或立项材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2029, 'SG01012829', '1-1-28开展“红色匠心、产业报国”宣传活动，提炼和总结产业链党建文化精神内涵', 3,
     12, 2, 2, '开展宣传活动1次，形成总结1份', NULL, '通过开展“红色匠心、产业报国”宣传活动，形成传承匠心、技能报国氛围，明确产业链党建文化精神内涵，预期开展宣传活动1次，为创建特色党建文化品牌奠定基础。',
     '1-1-28-1“红色匠心、产业报国”宣传活动方案1套
1-1-28-2“红色匠心、产业报国”活动照片1组
1-1-28-3“红色匠心、产业报国”活动总结材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 11, '0,1,11', 2029, 'SG01012929', '1-1-29申报国家级“样板支部”或“双带头人”教师党支部书记工作室”1个', 3,
     12, 2, 2, '申报样板支部或工作室1个', '国家级', '通过总结、凝练典型工作经验做法，形成产业链党组织党建工作品牌，明确申报条件，预期立项国家级“样板支部”或“双带头人党支部书记工作室”1个，为企业、学校党组织建设提供借鉴。',
     '1-1-29-1国家级党建工作“样板支部”“双带头人”教师党支部书记工作室申报材料1套
1-1-29-2国家级党建工作“样板支部”或“双带头人”教师党支部书记工作室立项名单1套
1-1-29-3聘请校内外专家指导创建“样板支部”“双带头人”教师党支部书记工作的记录、照片1组
1-1-29-4国家级党建工作“样板支部”或“双带头人”教师党支部书记工作室推广材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2025, 'SG01020125', '1-2-1成立课程思政建设小组，明确各成员职责', 3,
     8, 10, 10, '成立建设小组1个', NULL, '通过成立课程思政建设小组形成思政课程与课程思政的有益补充，覆盖全部课程和教师，明确各成员职责，预期实现课程思政全覆盖，为深入推进课程思政建设提供组织保障。',
     '1-2-1-1成立课程思政建设小组的通知或文件
1-2-1-2课程思政建设小组工作照片1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2025, 'SG01020225', '1-2-2 组建不少于30人的校企混编专业思政教育“大师资”团队', 3,
     8, 10, 10, '组建团队1个', NULL, '通过组建校企混编专业思政教育“大师资”团队形成人人参与思政工作氛围，覆盖全体在校学生，明确工作职责，预期在思想引领、工匠精神宣导等方面形成典型经验做法，为学生从在校生到职场人的顺利转变提供支持。',
     '1-2-2-1校企混编专业思政教育“大师资”团队成立文件
1-2-2-2校企混编专业思政教育“大师资”团队聘用人员明细
1-2-2-3校企混编专业思政教育“大师资”团队教学典型案例或新闻报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2025, 'SG01020325', '1-2-3 开展集体备课、学习研讨等活动', 3,
     8, 11, 11, '开展研讨活动4次', NULL, '通过开展集体备课、学习研讨等活动形成学习与生产定期沟通联络机制，覆盖全部课程和教师，明确活动目标，预期在课堂教学中实现工作场景任务驱动，为学生未来顺利适应企业生产场景提供支持。',
     '1-2-3-1校企集体备课、学习研讨活动制度
1-2-3-2校企集体备课、学习研讨活动研讨主题、发言材料、总结报告1套
1-2-3-3校企集体备课、学习研讨活动照片1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2025, 'SG01020425', '1-2-4 编制课程思政改革教学指南', 3,
     8, 10, 10, '编制教学指南1份', NULL, '通过编制课程思政改革教学指南形成课程思政授课统一范式，覆盖全部课程和教师，明确实施方法，预期实现课程思政教学改革全覆盖，为培养社会主义合格建设者和可靠接班人奠定基础。',
     '1-2-4-1课程思政改革教学指南1套
1-2-4-2制定课程思政改革教学指南活动照片1组
1-2-4-3课程实施材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2026, 'SG01020526', '1-2-5 确定 “大国工匠与产业报国精神”等3个核心思政主题', 3,
     8, 10, 10, '确定主题3个', NULL, '通过走访调研、座谈等形式，确定3个 “大国工匠与产业报国精神”核心思政主题，明确纳入课程教学的实施路径，预期覆盖全部在校生，为培养又红又专、德才兼备的高质素技术技能人才提供支撑。',
     '1-2-5-1 形成“大国工匠”精神核心思政主题材料
1-2-5-2 形成“产业报国”精神核心思政主题材料
1-2-5-3形成“爱国主义”精神核心思政主题材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2026, 'SG01020626', '1-2-6 建设课程思政资源库平台，开发课程思政教学案例', 3,
     8, 10, 10, '建设资源库平台1个，开发教学案例20个', NULL, '通过建设课程思政资源库平台，开发课程思政教学案例，形成人人重视思政工作，人人参与思政工作的氛围，明确建设标准，预期建设资源库平台1个，开发教学案例20个，为做好课程思政教学工作奠定基础。',
     '1-2-6-1课程思政教学案例建设标准佐证材料
1-2-6-2课程思政资源库平台材料1个
1-2-6-3课程思政教学案例集1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2026, 'SG01020726', '1-2-7 开发 “劳模工匠进书院”“工院机械青年说” 等书院课程', 3,
     8, 12, 12, '开发书院课程2门', NULL, '通过开发  “劳模工匠进书院”“工院机械青年说” 等书院课程，形成人人学劳模，人人当工匠氛围，预期开发书院课程2门',
     '1-2-7 -1“劳模工匠进书院”书院课程材料1套
1-2-7 -2“工院机械青年说”书院课程材料1套
1-2-7-3“劳模工匠进书院”“工院机械青年说”网络微课资源', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2026, 'SG01020826', '1-2-8 依托社会大课堂开发“传承红色基因 讲好淮海故事” 思政选修课程', 3,
     8, 10, 10, '开发选修课程1门', NULL, '通过走访调研、查阅资料，形成“传承红色基因 讲好淮海故事” 思政选修课程1门，预期课程面向全院学生开放，为完善思政课程体系，为培养社会主义合格建设者和可靠接班人奠定基础。',
     '1-2-8-1前期调研、走访活动，形成调研走访报告
1-2-8 -2“传承红色基因 讲好淮海故事” 思政选修课程材料1套
1-2-8-3建设完成“传承红色基因 讲好淮海故事” 配套网课
1-2-8-4学生选课情况材料
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2026, 'SG01020926', '1-2-9 聘请企业专家、劳动模范、大国工匠等2-3人担任思政导师', 3,
     1, 3, 3, '聘请思政导师2-3人', NULL, '通过聘请企业专家、劳动模范、大国工匠担任思政导师，完善“三全育人”体系，为培养社会主义合格建设者和可靠接班人奠定基础。',
     '1-2-9-1企业专家、劳动模范、大国工匠担任思政导师聘书1套
1-2-9-2聘请企业专家、劳动模范、大国工匠担任思政导师，开展“三全育人”系列活动照片1组
1-2-9-3思政导师授课讲稿、PPT等材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2027, 'SG01021027', '1-2-10 聘请企业专家、劳动模范、大国工匠等2-3人担任思政导师', 3,
     1, 3, 3, '聘请思政导师2-3人', NULL, '通过聘请企业专家、劳动模范、大国工匠担任思政导师，完善“三全育人”体系，为培养社会主义合格建设者和可靠接班人奠定基础。',
     '
1-2-10-1企业专家、劳动模范、大国工匠担任思政导师聘书1套
1-2-10-2聘请企业专家、劳动模范、大国工匠担任思政导师，开展“三全育人”系列活动照片1组
1-2-10-3思政导师授课讲稿、PPT等材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2027, 'SG01021127', '1-2-11 开发课程思政教学案例', 3,
     8, 10, 10, '开发教学案例20个', NULL, '通过开发课程思政教学案例，形成人人重视思政工作，人人参与思政工作的氛围，明确建设标准，预期开发教学案例20个，为做好课程思政教学工作奠定基础。',
     '1-2-11-1课程思政教学案例建设标准佐证材料
1-2-11-2课程思政资源库平台材料1个
1-2-11-3课程思政教学案例集1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2027, 'SG01021227', '1-2-12 树立1门书院课程品牌，形成典型案例或媒体报道', 3,
     8, 12, 12, '课程品牌1门，典型案例1个，媒体报道1篇', NULL, '通过试点书院制育人模式，打造书院课程品牌，预期形成品牌课程1门，典型案例1个，媒体报道1篇',
     '1-2-12 -1书院课程品牌1个
1-2-12 -2书院课程品牌典型案例1个
1-2-12 -3书院课程品牌媒体报道1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2027, 'SG01021327', '1-2-13 与企业开发1门产业特色思政选修课', 3,
     8, 12, 12, '开发选修课程1门', NULL, '通过与企业联合开发产业特色思政选修课，形成学生对企业文化的强大认同感，预期开发1门产业特色思政选修课，为培养学生吃苦耐劳精神，顺利适应职场奠定基础。',
     '1-2-13 -1开发产业特色鲜明的思政选修课——“工程机械发展史”
1-2-13-2“工程机械发展史”思政选修课讲义、教材等材料1套
1-2-13-3“工程机械发展史”思政选修课学生选课情况', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2028, 'SG01021428', '1-2-14 聘请企业专家、劳动模范、大国工匠等2-3人担任思政导师', 3,
     1, 3, 3, '聘请导师2-3人', NULL, '通过聘请企业专家、劳动模范、大国工匠担任思政导师，完善“三全育人”体系，为培养社会主义合格建设者和可靠接班人奠定基础。',
     '
1-2-14-1企业专家、劳动模范、大国工匠担任思政导师聘书1套
1-2-14-2聘请企业专家、劳动模范、大国工匠担任思政导师，开展“三全育人”系列活动照片1组
1-2-14-3思政导师授课讲稿、PPT等材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2028, 'SG01021528', '1-2-15 开发课程思政教学案例', 3,
     8, 10, 10, '开发案例20个', NULL, '通过开发课程思政教学案例，形成人人重视思政工作，人人参与思政工作的氛围，明确建设标准，预期开发教学案例20个，为做好课程思政教学工作奠定基础。',
     '1-2-15-1课程思政教学案例建设标准佐证材料
1-2-15-2开发课程思政教学案例的通知
1-2-15-3课程思政教学案例集1套
1-2-15-4课程思政教学案例结集出版1部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2028, 'SG01021628', '1-2-16 依托书院打造特色社团活动或公益活动品牌，形成典型案例或媒体报道', 3,
     4, 20, 20, '打造品牌1个，形成案例或报道1项', NULL, '通过试点书院制育人模式，打造书院特色社团活动或公益活动品牌，预期形成品牌1个，典型案例或媒体报道1个',
     '1-2-16 -1书院特色社团活动或公益活动品牌1个
1-2-16-2书院特色社团活动或公益活动品牌典型案例或报道1个
1-2-16-3依托该品牌开展社团活动书院特色社团活动或公益活动照片1组
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 12, '0,1,12', 2029, 'SG01021729', '1-2-17 “专题 + 网络 + 实践” 三位一体专业主课堂教学模式形成典型案例或媒体报道', 3,
     8, 10, 10, '形成案例或报道1项', NULL, '通过打造 “专题 + 网络 + 实践” 三位一体专业主课堂教学模式，丰富课堂教育模式，提升育人效果，预期形成典型案例或媒体报道1个，为兄弟院校提供借鉴。',
     '
1-2-17-1“专题 + 网络 + 实践” 三位一体专业主课堂教学模式设计与论证佐证材料
1-2-17-2“专题 + 网络 + 实践” 三位一体专业主课堂教学落地实施佐证材料
1-2-17 -3“专题 + 网络 + 实践” 三位一体专业主课堂教学模式典型案例或媒体报道1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2025, 'SG01030125', '1-3-1 开设“岗位党员先锋”微党课、“百生话匠心”等栏目', 3,
     12, 2, 2, '开设栏目3个', NULL, '通过开设“岗位党员先锋”微党课、“百生话匠心”等栏目形成线上线下多维育人格局，覆盖全体在校生，明确育人主题，预期师生参与率将实现新的跃升，为补足学生“精神之钙”提供支撑。',
     '1-3-1-1开设“岗位党员先锋”微党课栏目1个
1-3-1-2开设“百生话匠心”栏目1个
1-3-1-3开设“团支书讲团史”栏目1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2025, 'SG01030225', '1-3-2 构建 “云端” 思政阵地框架', 3,
     7, 8, 8, '构建框架1个、形成案例或报道2个', NULL, '通过构建“云端”思政阵地框架形成网络思想政治教育新格局，覆盖全体在校生，明确栏目和作用，预期学生主动参与网络思政的比例将有明显提升，为学生的学习和生活提供更多便利。',
     '1-3-2-1“云端” 思政阵地框架1套
1-3-2-2凝练案例或报道2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2025, 'SG01030325', '1-3-3 建设实践育人基地	', 3,
     6, 6, 6, '建设基地2个', NULL, '通过建设实践育人基地形成学、练互动育人格局，覆盖全体在校生，明确实践项目，预期每名学生掌握2项劳动技能，为提高学生劳动素养提供支撑。',
     '1-3-3-1实践育人基地建设办法1套
1-3-3-2实践育人基地授牌、活动照片1套
1-3-3-3实践育人案例或宣传报道1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2025, 'SG01030425', '1-3-4 开发"淮海战役"微视频等红色育人项目', 3,
     7, 8, 8, '开发项目5项', NULL, '通过开发"淮海战役"微视频等红色育人项目形成人人崇拜英雄的氛围，覆盖全体在校生，明确红色育人项目主题，预期每名学生崇拜一位英雄，为加强学生理想信念教育提供支撑。',
     '1-3-4-1"淮海战役-十人桥的故事"微视频红色育人项目
1-3-4-2"淮海战役-送儿上战场"微视频红色育人项目
1-3-4-3"淮海战役--微光“微视频红色育人项目
1-3-4-4"淮海战役--小竹竿的故事“微视频红色育人项目
1-3-4-5“数字里的淮海战役”微视频红色育人项目', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2025, 'SG01030525', '1-3-5 各类育人基地累计接待校内外人员', 3,
     6, 6, 6, '接待6000人次', NULL, '通过开放各类育人基地接待校内外人员形成人人认同职业教育、职业教育大有可为的氛围，覆盖全部育人基地，明确参观流程，预期实现年度接待6000人次以上。',
     '1-3-5-1形成育人基地育人项目1套
1-3-5-2育人基地接待校内外人员名单证明材料1套
1-3-5-3育人基地接待校内外人员照片1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2026, 'SG01030626', '1-3-6 开发“重走总书记视察徐工路”虚拟资源', 3,
     7, 8, 8, '开发虚拟资源1项', NULL, '通过开发“重走总书记视察徐工路”虚拟资源，形成立体式思想政治教育矩阵，提升思想政治教育工作吸引力，预期受众面覆盖全院学生，为开拓学生视野，帮助学生了解徐工发展历程提供支撑。',
     '1-3-6-1 前期赴徐工调研材料
1-3-6-2 校内外专家研讨虚拟资源建设论证材料
1-3-6-3重走总书记视察徐工路”虚拟资源1套
1-3-6-4重走总书记视察徐工路”虚拟资源使用情况', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2026, 'SG01030726', '1-3-7 建设实践育人基地', 3,
     6, 6, 6, '建设基地4个', NULL, '通过建设实践育人基地形成学、练互动育人格局，覆盖全体在校生，明确实践项目，预期每名学生掌握2项劳动技能，为提高学生劳动素养提供支撑。',
     '1-3-7-1实践育人基地授牌、照片1套
1-3-7-2实践育人基地发展育人活动材料
1-3-7-3实践育人基地相关宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2026, 'SG01030826', '1-3-8 开发红色育人项目', 3,
     7, 8, 8, '开发项目5项', NULL, '通过组建"红匠"话剧社等红色育人项目形成人人崇拜英雄的氛围，覆盖全体在校生，明确红色育人项目主题，预期每名学生崇拜一位英雄，为加强学生理想信念教育提供支撑。',
     '1-3-8-1"红匠"话剧社红色育人项目
1-3-8-2"文创拓印"红色育人项目
1-3-8-3体验淮海战役支前小道红色育人项目
1-3-8-4"信仰讲堂"红色育人项目
1-3-8-5"纪念馆志愿讲解员"红色育人项目', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2026, 'SG01030926', '1-3-9 建设大学生思想政治教育中心暨“大思政课”实践教学基地', 3,
     7, 8, 8, '建设基地1个', NULL, '通过建设大学生思想政治教育中心暨“大思政课”实践教学基地，推进大中小学思政课一体化建设，开展集体备课、优质课评选、教师培训等活动推动教学改革。',
     '1-3-9 -1大学生思想政治教育中心暨“大思政课”实践教学基地建设管理办法1套
1-3-9 -2大学生思想政治教育中心暨“大思政课”实践教学基地授牌等活动照片材料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2026, 'SG01031026', '1-3-10 各类育人基地累计接待校内外人员', 3,
     6, 6, 6, '接待6000人次', NULL, '通过开放各类育人基地接待校内外人员形成人人认同职业教育、职业教育大有可为的氛围，覆盖全部育人基地，明确参观流程，预期实现年度接待6000人次以上。',
     '1-3-10-1育人基地接待校内外单位、人员名单
1-3-10-2照片证明材料1套
1-3-10-3相关宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2027, 'SG01031127', '1-3-11 开发“大国工匠数字展馆”虚拟资源', 3,
     7, 8, 8, '开发虚拟资源1项', NULL, '通过开发“大国工匠数字展馆”虚拟资源，形成立体式思想政治教育矩阵，提升思想政治教育工作吸引力，预期受众面覆盖全院学生，为帮助学生尽早树立职业发展目标提供支撑。',
     '1-3-11-1“大国工匠数字展馆”虚拟资源建设前期调研材料
1-3-11-2校内外 专家论证材料
1-3-11-3“大国工匠数字展馆”虚拟资源1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2027, 'SG01031227', '1-3-12 建设实践育人基地', 3,
     6, 6, 6, '建设基地4个', NULL, '通过建设实践育人基地形成学、练互动育人格局，覆盖全体在校生，明确实践项目，预期每名学生掌握2项劳动技能，为提高学生劳动素养提供支撑。',
     '1-3-12-1实践育人基地授牌、活动照片1套
1-3-12-2实践育人基地开展活动材料
1-3-12-3实践育人基地相关宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2027, 'SG01031327', '1-3-13 开发红色育人项目', 3,
     7, 8, 8, '开发项目5项', NULL, '通过开发红色育人项目形成人人崇拜英雄的氛围，覆盖全体在校生，明确红色育人项目主题，预期每名学生崇拜一位英雄，为加强学生理想信念教育提供支撑。',
     '1-3-13-1“新青年正担当”红色育人项目配套图片、新闻报道
1-3-13 -2“沛县红色交通线”红色育人项目配套图片、新闻报道
1-3-13 -3“青年团史馆”红色育人项目3
1-3-13 -4“王杰的故事”红色育人项目4
1-3-13 -5“徐州红色文化故事”红色育人项目5', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2027, 'SG01031427', '1-3-14 省级高校思想政治工作质量提升工程建设项目通过验收', 3,
     7, 8, 8, '通过验收1项', '省级及以上', '通过建设省级高校思想政治工作质量提升工程项目，提升思想政治工作内涵，形成思想政治工作品牌。',
     '1-3-14 -1省级高校思想政治工作质量提升工程建设项目结项报告
1-3-14-2项目验收结项证明
1-3-14-3项目分享会照片1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2027, 'SG01031527', '1-3-15 各类育人基地累计接待校内外人员', 3,
     6, 6, 6, '接待6000人次', NULL, '通过开放各类育人基地接待校内外人员形成人人认同职业教育、职业教育大有可为的氛围，覆盖全部育人基地，明确参观流程，预期实现年度接待6000人次以上。',
     '1-3-15-1育人基地接待校内外单位、人员名单
1-3-15-2育人基地接待照片证明材料1套
1-3-15-3相关宣传报道
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2027, 'SG01031627', '1-3-16 打造心理健康教育品牌', 3,
     6, 6, 6, '打造品牌1个', NULL, '通过打造心理健康教育品牌，锤炼心理健康教育队伍，提高心理育人能力和水平。',
     '1-3-16-1 心理健康教育品牌授牌、发文等材料1套，
1-3-16-2依托该平台开展心理健康教育活动心理健康活动现场照片1组
1-3-16-3相关心理健康教育案例或宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2028, 'SG01031728', '1-3-17 打造“工程机械行业红色印记追溯”等校内实践育人基地品牌', 3,
     6, 6, 6, '打造校内品牌1个', NULL, '通过建设“工程机械行业红色印记追溯”校内实践育人基地，增强学生对工程机械行业的认同感，增强学生未来从事相关专业工作的信心。',
     '1-3-17-1前期调研、专家论证等材料
1-3-17-2“工程机械行业红色印记追溯”品牌实践育人基地建设方案
1-3-17-3依托实践基地开展相关活动、现场照片等材料
1-3-17-4形成相关案例或宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2028, 'SG01031828', '1-3-18 挖掘“徐工创新奋斗史探寻”等故事，打造校外企业实践育人基地品牌', 3,
     6, 6, 6, '打造校外品牌1个', NULL, '通过挖掘“徐工创新奋斗史探寻”等故事，增强学生对工程机械行业的认同感，增强学生未来从事相关专业工作的职业信心。',
     '1-3-18-1校外企业实践基地授牌
1-3-18-2“徐工创新奋斗史探寻”品牌实践育人基地活动照片等材料
1-3-18-3形成相关案例或宣传报道
1-3-18-4形成校外企业实践育人基地品牌', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2028, 'SG01031928', '1-3-19 各类育人基地累计接待校内外人员', 3,
     6, 6, 6, '接待6000人次', NULL, '通过开放各类育人基地接待校内外人员形成人人认同职业教育、职业教育大有可为的氛围，覆盖全部育人基地，明确参观流程，预期实现年度接待6000人次以上。',
     '1-3-19-1育人基地接待校内外单位、人员名单证明材料1套
1-3-19-2育人基地接待活动照片
1-3-19-3相关宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2028, 'SG01032028', '1-3-20 开发红色育人项目', 3,
     7, 8, 8, '开发项目5项', NULL, '通过开发红色育人项目形成人人崇拜英雄的氛围，覆盖全体在校生，明确红色育人项目主题，预期每名学生崇拜一位英雄，为加强学生理想信念教育提供支撑。',
     '1-3-20 -1“红色记忆”红色育人项目1
1-3-20 -2“微山湖抗日烽火”红色育人项目2
1-3-20 -3“青年先锋实践营”红色育人项目3
1-3-20 -4“淮海战役精神传习”红色育人项目4
1-3-20 -5“古彭红色足迹寻访”红色育人项目5', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2029, 'SG01032129', '1-3-21 形成虚实结合的特色实践育人案例或媒体报道', 3,
     6, 6, 6, '形成案例或报道1项', NULL, '通过建设整合线上线下教育资源，形成虚实结合特色实践育人品牌，形成典型案例或媒体报道1项。',
     '1-3-21-1虚实结合特色实践育人案例或媒体报道1项
1-3-21-2开展相关研讨会图片1组
1-3-21-3相关文字总结报告1篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2029, 'SG01032229', '1-3-22 各类育人基地累计接待校内外人员', 3,
     6, 6, 6, '接待6000人次', NULL, '通过开放各类育人基地接待校内外人员形成人人认同职业教育、职业教育大有可为的氛围，覆盖全部育人基地，明确参观流程，预期实现年度接待6000人次以上。',
     '1-3-22-1育人基地接待校内外单位、人员名单证明材料1套
1-3-22-2育人基地接待校活动照片材料1套
1-3-22-3宣传报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 13, '0,1,13', 2029, 'SG01032329', '1-3-23 开发红色育人项目', 3,
     7, 8, 8, '开发项目5项', NULL, '通过开发红色育人项目形成人人崇拜英雄的氛围，覆盖全体在校生，明确红色育人项目主题，预期每名学生崇拜一位英雄，为加强学生理想信念教育提供支撑。',
     '1-3-23 -1“红色家书诵读计划”红色育人项目1
1-3-23 -2“英雄事迹宣讲”红色育人项目2
1-3-23 -3“青年红色志愿行”红色育人项目3
1-3-23 -4“红色文化创意工坊”红色育人项目4
1-3-23 -5“革命精神宣讲”红色育人项目5', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2025, 'SG02010125', '2-1-1 召开产教联合体理事会年会，吸纳10家单位加入联合体；【追加】：实现企业捐赠300万元；', 3,
     11, 21, 21, '召开理事会年会，吸纳10家单位，企业捐赠300万元', NULL, '通过召开理事会年会并吸纳10家新成员，联合体形成了更广泛的协同网络，在本年度实现300万元企业捐赠，推进校企的深度融合。',
     '2-1-1-1 产教联合体理事会年会新闻报道
2-1-1-2 10家单位企业申请表
2-1-1-3 捐赠明细及捐赠协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2025, 'SG02010225', '2-1-2 起草产教联合体“实体、实融、共治、共赢”的合作发展机制文件；', 3,
     11, 22, 22, '机制文件1套', NULL, '通过制定并实施以“实体、实融、共治、共赢”为核心的合作发展机制文件，形成了权责清晰、运行高效、保障有力的制度化合作框架。',
     '2-1-2-1《产教联合体章程》
2-1-2-2《产教联合体专职人员管理办法》
2-1-2-3《产教联合体内设机构考核办法》
2-1-2-4《产教联合体成员单位考核办法》
2-1-2-5《产教联合体财务管理制度》
2-1-2-6《产教联合体技术攻关与成果转化合作办法》
2-1-2-7《产教联合体监督评价实施办法》
2-1-2-8《产教联合体校企高层次人才互兼互聘实施办法》
2-1-2-9《产教联合体监督评价实施办法》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2025, 'SG02010325', '2-1-3 制定专业群建设改革的时间表与路线图；', 3,
     8, 12, 12, '改革实施方案1个', NULL, '通过制定详尽的时间表与路线图，形成了从宏观规划到年度任务的高效执行路径；明确了各项改革任务的节点与路径',
     '2-1-3-1 针对专业群建设，充分调研，形成调研总结报告1份
2-1-3-2制定专业群建设的5年时间安排表
2-1-3-3制定专业群建设的路线图', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2025, 'SG02010425', '2-1-4 完善理事会领导下的三级组织机构；', 3,
     11, 22, 22, '三级组织机构架构图1个', NULL, '通过完善理事会领导下的三级组织机构，形成了权责清晰、运行顺畅的治理架构与协作流程',
     '2-1-4-1 组织机构调整工作方案
2-1-4-2 理事会大会通过调整方案会议议程或会议记录
2-1-4-3 三级组织机构红头文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2025, 'SG02010525', '2-1-5 参加市域产教联合体或行业产教融合共同体5个。', 3,
     11, 22, 22, '参加联合体或共同体5个', NULL, '通过深度参与五个标杆性联合体，形成了开放学习与对标发展的新机制；明确了自身在行业和区域发展中的定位与差距',
     '2-1-5-1 产教联合体或行业产教融合共同体申请书或批文', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2025, 'SG02010625', '2-1-6 【新增】与徐工集团等合作成立企业名师工作室1个。', 3,
     1, 4, 4, '企业名师工作室1个', NULL, '通过设立并运营企业名师工作室，形成了企业大师技艺传承与青年教师精准培养的固定通道',
     '2-1-6-1 产业教授到学校成立企业名师工作室
2-1-6-2 名师工作室建设相应协议
2-1-6-2 名师工作室工作职责', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2026, 'SG02010726', '2-1-7 召开产教联合体理事会年会，吸纳10家单位加入联合体；【追加】：实现企业捐赠300万元；', 3,
     11, 21, 21, '召开理事会年会，吸纳10家单位，企业捐赠300万元', NULL, '通过召开理事会年会并吸纳10家新成员，联合体形成了更广泛的协同网络，在本年度实现300万元企业捐赠，推进校企的深度融合。',
     '2-1-7-1 产教联合体理事会年会新闻报道
2-1-7-2 10家单位企业申请表
2-1-7-3 捐赠明细及捐赠协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2026, 'SG02010826', '2-1-8 出台监督、考核、激励制度；', 3,
     11, 22, 22, '制度1套', NULL, '通过出台监督、考核与激励制度，明确了各主体的工作目标、考核标准与价值回报。预期在制度运行后，实现了联合体内部工作积极性和执行力的提升。',
     '2-1-8-1产教联合体内设机构考核办法
2-1-8-2产教联合体成员单位考核办法
2-1-8-3产教联合体监督评价实施办法', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2026, 'SG02010926', '2-1-9 整合各主体优势资源，开展课程建设10门；', 3,
     8, 12, 12, '课程建设10门', NULL, '通过整合资源共建10门课程，形成了共建共享、持续更新的课程开发机制；预期在新课程应用后，实现了教学内容与行业技术发展的同步迭代。',
     '2-1-9-1 课程建设通知文件
2-1-9-2立项建设公示文件（含课程名称、共建单位）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2026, 'SG02011026', '2-1-10 徐工集团参与校内实训基地建设1项。', 3,
     8, 13, 13, '实训基地建设1项', NULL, '通过徐工集团参与校内实训基地建设，形成了校企共建共管的实体化育人平台；覆盖了设备操作、工艺实践等核心技能训练环节。预期在基地投入运行后，实现教学环境与真实生产场景的对接',
     '2-1-10-1基地共建协议
2-1-10-2基地挂牌照片或文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2026, 'SG02011126', '2-1-11 【新增】与徐工集团等合作成立企业名师工作室1个。', 3,
     1, 4, 4, '企业名师工作室1个', NULL, '通过设立并运营企业名师工作室，形成了企业大师技艺传承与青年教师精准培养的固定通道，预期提升校企联动能力，为双师型教师培养打下坚实基础。',
     '2-1-11-1 产业教授到学校成立企业名师工作室挂牌、聘书
2-1-11-2 名师工作室建设相应协议
2-1-11-3 名师工作室工作职责', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2027, 'SG02011227', '2-1-12 召开理事会年会，新增10家单位，申报国家级产教联合体；【追加】：实现企业捐赠300万元；', 3,
     11, 21, 21, '召开理事会年会，吸纳10家单位，企业捐赠300万元', NULL, '通过召开理事会年会并吸纳10家新成员，联合体形成了更广泛的协同网络，在本年度实现300万元企业捐赠，推进校企的深度融合。',
     '2-1-12-1 产教联合体理事会年会新闻报道
2-1-12-2 10家单位企业申请表
2-1-12-3 国家级产教联合体申报书
2-1-12-4 捐赠明细及捐赠协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2027, 'SG02011327', '2-1-13 完善多方参与合作机制的相关制度文件；', 3,
     11, 22, 22, '制度文件1套', NULL, '通过修订并发布一系列制度文件，明确了各参与主体在决策、执行、投入与收益分配中的权利与义务。预期在机制引导下，实现了联合体从“被动组合”到“主动协同”的根本性转变。',
     '2-1-13-1产教联合体校企高层次人才互兼互聘实施办法（《产教联合体校企高层次人才互兼互聘实施办法》）
2-1-13-2产教联合体决策机制与流程规范（《产教联合体决策机制与流程规范》）
2-1-13-3产教联合体教科研项目管理与经费配套办法', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2027, 'SG02011427', '2-1-14 与徐工集团等企业签定企业名师工作室合作协议，成立工作室2个；', 3,
     1, 4, 4, '工作室2个', NULL, '通过设立并运营企业名师工作室，形成了企业大师技艺传承与青年教师精准培养的固定通道',
     '2-1-14-1 产业教授到学校成立企业名师工作室挂牌、聘书
2-1-14-2 名师工作室建设相应协议
2-1-14-3 名师工作室工作职责', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2027, 'SG02011527', '2-1-15 推动各类主体积极参与专业群各类建设。', 3,
     8, 12, 12, '专业群建设论证会1次', NULL, '通过制度引导与项目化牵引，形成了多元主体主动参与、资源向专业群汇聚的良好生态；覆盖了课程开发、教材编写、实践教学等专业建设的关键环节。',
     '2-1-15-1专业群建设机制文件：产教联合体学校专业规划与产业协调联动方案、产教联合体职业教育专业动态调整实施办法
2-1-15-2专业群建设论证会照片、会议纪要等', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2028, 'SG02011628', '2-1-16 召开产教联合体理事会年会，吸纳10家单位加入联合体；【追加】：实现企业捐赠300万元；', 3,
     11, 21, 21, '召开理事会年会，吸纳10家单位，企业捐赠300万元', NULL, '通过召开理事会年会并吸纳10家新成员，联合体形成了更广泛的协同网络，在本年度实现300万元企业捐赠，推进校企的深度融合。',
     '2-1-16-1 产教联合体理事会年会新闻报道
2-1-16-2 10家单位企业申请表
2-1-16-3 捐赠明细及捐赠协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2028, 'SG02011728', '2-1-17 优化多方参与合作机制的相关制度文件；', 3,
     11, 22, 22, '制度文件1套', NULL, '通过系统性地修订制度文件，形成了更具适应性、可操作性的协同治理闭环；预期在新制度运行后，实现了联合体议事决策效率和成员满意度的显著提升。',
     '2-1-17-1新增《产教联合体指导委员会组建与运行规则》
2-1-17-1优化完善《产教联合体校企高层次人才互兼互聘实施办法》
2-1-17-2优化完善《产教联合体决策机制与流程规范》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2028, 'SG02011828', '2-1-18 各主体参与课程、实训室建设项目10个；', 3,
     8, 13, 13, '课程、实训室建设项目10个', NULL, '通过推动各主体共同投入10个具体项目，形成了“责任共担、成果共享”的实体化共建模式；预期在本年度，可实现教学资源质量与实训条件水平的重要升级。',
     '2-1-18-1课程、实训基地立项通知
2-1-18-2校企共建课程和实训基地协议
2-1-18-3课程、实训基地建设内容
2-1-18-4课程、实训基地结项公示', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2028, 'SG02011928', '2-1-19 对专业群建设改革成果进行评估总结。', 3,
     8, 12, 12, '评估总结1套', NULL, '通过系统性的评估与总结，形成了专业群建设周期内的闭环管理。预期在完成评估后，实现了对改革成果的量化呈现与对存在问题的精准诊断。',
     '2-1-19-1《专业群建设改革成果评估总结报告》
2-1-19-2评估报告的专家评审意见表', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2028, 'SG02012028', '2-1-20 【新增】与徐工集团等合作成立企业名师工作室2个。', 3,
     1, 4, 4, '名师工作室2个', NULL, '通过设立并运营企业名师工作室，形成了企业大师技艺传承与青年教师精准培养的固定通道',
     '2-1-24-1 产业教授到学校成立企业名师工作室挂牌、聘书
2-1-24-2 名师工作室建设相应协议
2-1-24-3 名师工作室工作职责', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2029, 'SG02012129', '2-1-21 召开产教联合体理事会年会，吸纳10家单位加入联合体；【追加】：实现企业捐赠300万元；', 3,
     11, 21, 21, '召开理事会年会，吸纳10家单位，企业捐赠300万元', NULL, '通过召开理事会年会并吸纳10家新成员，联合体形成了更广泛的协同网络，在本年度实现300万元企业捐赠，推进校企的深度融合。',
     '2-1-21-1 产教联合体理事会年会新闻报道
2-1-21-2 10家单位企业申请表
2-1-21-3 捐赠明细及捐赠协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2029, 'SG02012229', '2-1-22 总结产教联合体建设经验，形成成果报告；', 3,
     11, 21, 21, '成果报告1个', NULL, '通过系统梳理与提炼，形成了全面、翔实的产教联合体建设成果总结报告；覆盖了机制创新、资源建设、人才培养、服务产业等核心成效与关键经验。预期在报告发布后，实现了建设成果的系统化呈现。',
     '2-1-22-1《产教联合体建设总结报告》
2-1-22-2相关会议/平台上进行汇报交流的通知、照片、新闻', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2029, 'SG02012329', '2-1-23 形成专业群合作发展机制1套。', 3,
     8, 12, 12, '机制文件1套', NULL, '通过将成功实践制度化，形成了一套包含“共建、共管、共评、共享”的专业群合作发展机制文件；预期在机制保障下，实现了专业群建设的自我优化、迭代发展与长效运行。',
     '2-1-23-1《产教联合体学校专业规划与产业协调联动方案》优化完善版
2-1-23-2《产教联合体职业教育专业动态调整实施办法》优化完善版
2-1-23-3其他专业群共建办法类文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2029, 'SG02012429', '2-1-24 【新增】与徐工集团等合作成立企业名师工作室1个。', 3,
     1, 4, 4, '企业名师工作室1个', NULL, '通过设立并运营企业名师工作室，形成了企业大师技艺传承与青年教师精准培养的固定通道',
     '2-1-24-1 产业教授到学校成立企业名师工作室挂牌、聘书
2-1-24-2 名师工作室相应协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 14, '0,2,14', 2029, 'SG02012529', '2-1-25 【新增】形成省级产教融合案例1个。', 3,
     11, 23, 23, '产教融合案例1个', '省级及以上', '通过深度提炼联合体特色亮点，形成了一份具有示范性和推广价值的省级产教融合优秀案例；覆盖了创新做法、实施路径与可量化的成效数据。预期在案例获评后，实现了联合体品牌影响力与区域示范引领作用的显著提升。',
     '2-1-25-1 具体案例材料
2-1-25-2 入选的发文等证明
2-1-25-3省级案例报道链接', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2025, 'SG02020125', '2-2-1 组建专项工作小组，明确小组成员职责分工；', 3,
     11, 22, 22, '专项小组3个', NULL, '通过组建专项小组并精准定岗定责，形成了跨部门联动的集中攻坚力量；明确了每位成员的任务目标与输出标准。',
     '2-2-1-1 《关于成立XX产教融合人才培养工作小组的通知》
2-2-1-2 《关于成立XX产教融合技术服务工作小组的通知》
2-2-1-3 《关于成立XX产教融合公共服务工作小组的通知》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2025, 'SG02020225', '2-2-2 建立议事决策机制，明确各环节的流程和责任主体；', 3,
     11, 23, 23, '议事决策机制文件1套', NULL, '通过建立科学的议事决策机制，形成了流程规范、权责清晰、衔接顺畅的决策与执行链条，明确了从提案、论证到审批、落实各环节的责任主体与时限要求。',
     '2-2-2-1《产教联合体议事规则》
2-2-2-2《产教联合体重大事项议事规则》
2-2-2-3《产教联合体决策机制与流程规范》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2025, 'SG02020325', '2-2-3 开展专业群重点事项专家论证会议；', 3,
     8, 12, 12, '专家论证会议1次', NULL, '通过高水平的专家论证，形成了对重点事项的全面审视与多维评估，覆盖了人才培养方案、课程体系等核心内容的科学性论证。',
     '2-2-3-1 人才培养方案论证（收集过程材料）
2-2-3-2 重大实训基地建设
2-2-3-3 专业群专业设置与方向动态调整', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2025, 'SG02020425', '2-2-4 广泛收集各方意见和建议，优化相关决策进行必要的调整和完善。', 3,
     8, 12, 12, '专业群重点事项论证报告1套', NULL, '通过系统性地收集反馈并对决策进行完善，形成了从实践出发、持续优化的决策闭环，明确了原方案中需要调整与强化的关键环节。',
     '2-2-4-1 针对重点事项的各方意见和建议
2-2-4-2 专业群重点事项论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2026, 'SG02020526', '2-2-5 成立专业群建设专家咨询团队；', 3,
     8, 12, 12, '专家咨询团队1个', NULL, '通过组建涵盖行业、企业、高校的专家团队，形成了专业群建设的智囊团，预期在关键决策前，实现了决策科学性与前瞻性的有效提升。',
     '2-2-5-1 专家咨询团队正式聘任文件及名单
2-2-5-2 专家咨询团队具体工作职责', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2026, 'SG02020626', '2-2-6 广泛收集各方意见和建议，了解各方诉求；', 3,
     11, 23, 23, '意见反馈清单1个', NULL, '通过多渠道收集信息，形成了系统化的需求与意见池；明确了各核心利益相关方（学生、教师、企业）的核心关切与改进期望。预期在后续规划中，实现建设方向与各方诉求的精准对接。',
     '2-2-6-1调查问卷、座谈会等收集的专业群建设方面原始数据
2-2-6-2 收集到的政行企校关于专业群建设意见建议汇总梳理表
2-2-6-3 形成整改意见反馈清单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2026, 'SG02020726', '2-2-7 召开建设总结会议，平衡各方的投入与回馈；', 3,
     8, 9, 9, '召开会议1次', NULL, '通过建设总结会议，形成了阶段性成果共识与利益平衡方案，预期在新周期内，实现成员单位参与度与满意度的稳定提升。',
     '2-2-7-1 专业群建设总结会议纪要
2-2-7-2 总结会的新闻报道或照片
2-2-7-3 达成的新的合作协议或备忘录', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2026, 'SG02020826', '2-2-8 筛选第三方评估机构，确定合作意向。', 3,
     8, 12, 12, '第三方评估机构1个', NULL, '通过公开、规范的筛选流程，确定了与权威第三方评估机构的合作关系，预期在后续环节，实现了评估工作的独立性、专业性与公正性。',
     '2-2-8-1学校关于引进第三方评估机构对专业群评估通知
2-2-8-2第三方 评估机构遴选过程的记录（如评审表）
2-2-8-2.双方签订的合作意向书或委托协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2027, 'SG02020927', '2-2-9 对专业群建设相关决策进行完善和优化；', 3,
     8, 12, 12, '专业群建设事项修订文件或方案1套', NULL, '基于前期收集的意见与总结，形成了优化后的专业群建设系列决策，明确了各项调整的内容、依据与负责人。预期在执行层面，实现了决策文件版本的迭代更新与适用性增强。',
     '2-2-9-1 修订后的专业人才培养方案
2-2-9-2 修订后的专业群建设与管理办法
2-2-9-3 修订后的专业设置与调整管理办法', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2027, 'SG02021027', '2-2-10 引入第三方评估机构，制定评估计划和指标体系，开展首次全面评估；', 3,
     8, 12, 12, '评估报告1个', NULL, '通过第三方机构的专业介入，形成了客观、量化的专业群建设评估报告，明确，预期在本阶段，实现了对建设现状的“精准画像”。',
     '2-2-10-1学校与第三方评估机构的相工作研讨会
2-2-10-2第三方评估机构评估计划
2-2-10-3第三方评估机构评估指标体系
2-2-10-4首次全面评估的完整报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2027, 'SG02021127', '2-2-11 针对评估中发现的问题，及时进行纠正和改进，形成改进报告。', 3,
     8, 12, 12, '改进报告1个', NULL, '基于评估反馈，形成了具体可行的纠正与预防措施方案，预期在短期内，实现了对关键突出问题的初步解决。',
     '2-2-11-1专业群针对评估问题的研讨会
2-2-11-2针对评估问题的改进方案或任务清单
2-2-11-3形成的专项改进报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2028, 'SG02021228', '2-2-12 持续收集意见，对专业群建设决策执行情况进行动态监测和分析；', 3,
     8, 12, 12, '定期的监测报告', NULL, '通过建立常态化的监测机制，形成了对决策执行过程的动态感知能力，预期在过程中，实现了从静态管理向动态精准管理的转变。',
     '2-2-12-1 收集专业群建设决策执行过程中的意见清单
2-2-12-2 专业群建设执行情况动态监测报告
2-2-12-3 针对专业群建设执行情况分析', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2028, 'SG02021328', '2-2-13 评估机构按计划进行第二次评估，对比首次评估结果，分析改进效果；', 3,
     8, 12, 12, '评估报告1个', NULL, '通过第二次评估的对比分析，形成了对首次改进措施有效性的客观验证；明确了哪些改进已见效、哪些问题依然存在或产生新变化。预期在本环节，实现了质量改进闭环的初步闭合。',
     '2-2-13-1第二次评估报告启动通知文件
2-2-13-2第二次评估的内容要点和工作计划
2-2-13-2第二次评估的结果（包含与首次评估结果的对比分析）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2028, 'SG02021428', '2-2-14 根据评估结果意见反馈，再次调整和完善专业群建设决策。', 3,
     8, 12, 12, '优化后的决策文件1套', NULL, '基于两轮评估的实证结果，形成了更具深度和针对性的第二轮决策优化；明确了专业群持续改进的长期方向与战略重点。预期在新一轮执行中，实现了专业群建设质量的螺旋式上升。',
     '2-2-14-1 针对评估结果召开研讨会（照片、新闻）
2-2-14-2 再次优化后的决策文件（如修订后的专业人才培养方案、专业调整方案等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2029, 'SG02021529', '2-2-15 对专业群建设决策及执行情况进行全面总结和回顾，形成总结报告；', 3,
     8, 12, 12, '总结报告1个', NULL, '对专业群整个闭环管理周期进行系统复盘，形成了涵盖全过程、全要素的总结报告，明确了专业群建设核心经验、典型模式',
     '2-2-15-1专业群建设任务执行与过程管理台账
2-2-15-2专业群建设决策与执行情况总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 15, '0,2,15', 2029, 'SG02021629', '2-2-16 巩固和优化联合体理事会的工作机制和决策流程，提供经验。', 3,
     11, 22, 22, '机制和流程文件1套', NULL, '通过完善联合体理事会的工作机制和决策流程，形成标准化、制度化管理体系，形成了稳定的工作机制与决策流程，预期在未来工作中，实现了联合体整体治理效能与规范管理能力的根本性提升。',
     '2-2-16-1联合体章程
2-2-16-2联合体运营管理制度文件
2-2-16-3产教联合体专职人员管理办法
2-2-16-4联合体绩效考核体系
2-2-16-5联合体运营质量保障体系
2-2-16-6其他', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2025, 'SG02030125', '2-3-1 建立 “对接需求、优化配置、效率优先、动态调整” 的资源整合机制；', 3,
     11, 23, 23, '机制文件1套', NULL, '通过建立“对接需求、优化配置、效率优先、动态调整” 资源整合机制，形成了以联合体内各方需求为导向的资源动态配置模式，实现了联合体内资源要素的优化配置',
     '2-3-1-1 《产教联合体实训基地共建共享实施细则》
2-3-1-2 《产教联合体内信息发布和共享管理办法》
2-3-1-3 《产教联合体校企高层次人才互兼互聘实施办法》
2-3-1-4 《产教联合体学校专业规划与产业协调联动方案》
2-3-1-5 《产教联合体职业教育专业动态调整实施办法》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2025, 'SG02030225', '2-3-2 与企业签订协同育人合作协议；', 3,
     8, 14, 14, '合作协议1套', NULL, '通过签订协同育人协议，形成了校企之间稳定、规范的共同培养人才的法律基础与合作框架；覆盖了人才培养的全过程。',
     '2-3-2-1 各专业与相关企业签订协同育人签约仪式
2-3-2-2各专业与相关企业签订协同育人合作协议
2-3-2-2各专业与相关企业协同育人培养方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2025, 'SG02030325', '2-3-3 完成工程机械产教融合实践中心，规划，形成初步建设方案；', 3,
     8, 9, 9, '建设方案1个', NULL, '通过完成实践中心的规划与方案设计，形成了集教学、实训、研发于一体的实体化建设蓝图，明确了中心的功能定位、空间布局与实施路径',
     '2-3-3-1完成工程机械产教融合实践中心需求调研报告
2-3-3-2工程机械产教融合实践中心建设论证会（照片、新闻等）
2-3-3-2形成工程机械产教融合实践中心初步建设方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2025, 'SG02030425', '2-3-4 技术服务平台推动研发与成果转化10项，产生经济效益2000万元；', 3,
     11, 24, 24, '研发与成果转化10项，经济效益2000万元', NULL, '通过技术服务平台推动10项研发与转化，形成了以市场价值为导向的产学研用闭环，覆盖了从技术攻关到应用推广的关键环节。预期在计划期内，实现了2000万元的经济效益，显著提升了服务产业的能力。',
     '2-3-4-1 统计学校技术服务平台研发与专利成果转化项目明细
2-3-4-2  统计学校技术服务平台研发与专利成果转化合同及发票一套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2025, 'SG02030525', '2-3-5 四技服务经费400万元，授权或转让知识产权30项。【追加】：四技服务经费200万元；', 3,
     11, 24, 24, '四技服务经费600万元，授权或转让知识产权30项', NULL, '通过开展四技服务与知识产权运营，形成了知识价值变现与反哺教学的良性循环；明确了以知识产权为核心的有偿服务模式。预期在年度内，实现了累计600万元服务经费与30项知识产权转化的双重目标。',
     '2-3-5-1 统计学校范围内四技服务、专利转让情况明细
2-3-5-2 统计学校范围内四技服务、专利转让合同及发票', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2025, 'SG02030625', '2-3-6 【新增】每年订单班、学徒制等培养的学生1000人。', 3,
     8, 14, 14, '订单班、学徒制学生1000人', NULL, '通过大规模开展订单班、现代学徒制等培养模式，形成了校企双主体育人的稳定规模；覆盖了学生从招生到就业的关键环节。预期在年度培养周期内，实现了为产业链精准输送1000名高素质技术技能人才的目标。',
     '2-3-6-1 专业群内专业签订订单班、学徒制培养协议
2-3-6-2 订单班人才培养方案
2-3-6-3订单班、学徒制 学生选拔名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2026, 'SG02030726', '2-3-7 完善资源整合机制的评估与反馈体系，定期开展资源整合效果评估；', 3,
     11, 24, 24, '评估报告1个', NULL, '通过建立评估反馈体系，形成了对资源整合效果的常态化监测与优化能力，预期在体系运行后，实现资源整合机制的完善与持续增效。',
     '2-3-7-1 校企共建科研团队效能评估与反馈办法
2-3-7-2 校企共建教学团队效能评估与反馈办法
2-3-7-3科研团队、教学团队效能评估结果与反馈', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2026, 'SG02030826', '2-3-8 形成企业深度参与人才培养、课程开发、教学评价工作方案；', 3,
     8, 14, 14, '工作方案1个', NULL, '通过制定专项工作方案，形成了引导企业深度参与教学全过程的制度化路径，预期在方案实施后，实现了产教深度融合。',
     '2-3-8-1 企业深度参与人才培养、课程开发与教学评价的研讨会
2-3-8-2企业深度参与人才培养、课程开发与教学评价工作实施方案（可以分拆成三个方面写方案）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2026, 'SG02030926', '2-3-9 技术服务平台推动研发与成果转化10项，产生经济效益2000万元；', 3,
     11, 24, 24, '研发与成果转化10项，经济效益2000万元', NULL, '通过技术服务平台推动10项研发与转化，形成了以市场价值为导向的产学研用闭环，覆盖了从技术攻关到应用推广的关键环节。预期在计划期内，实现了2000万元的经济效益，显著提升了服务产业的能力。',
     '2-3-9-1 统计学校技术服务平台研发与专利成果转化项目明细
2-3-9-2  统计学校技术服务平台研发与专利成果转化合同及发票一套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2026, 'SG02031026', '2-3-10 完成公共服务平台建设，收集相关信息300条；', 3,
     11, 23, 23, '公共服务平台1个，收集信息300条', NULL, '通过公共服务平台的建设与运行，形成了产业信息的汇聚与共享枢纽，预期在平台上线后，实现300条以上有效行业信息的数字化共享。',
     '2-3-10-1公共服务平台发布技能鉴定信息
2-3-10-2公共服务平台发布行业重要新闻
2-3-10-2公共服务平台发布企业人才需求信息
（以上信息共计大于300条）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2026, 'SG02031126', '2-3-11 四技服务经费400万元，授权或转让知识产权30项。【追加】：四技服务经费200万元；', 3,
     11, 24, 24, '四技服务经费600万元，授权或转让知识产权30项', NULL, '通过开展四技服务与知识产权运营，形成了知识价值变现与反哺教学的良性循环；明确了以知识产权为核心的有偿服务模式。预期在年度内，实现了累计600万元服务经费与30项知识产权转化的双重目标。',
     '2-3-11-1 统计学校范围内四技服务、专利转让情况明细
2-3-11-2 统计学校范围内四技服务、专利转让合同及发票', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2026, 'SG02031226', '2-3-12 【新增】每年订单班、学徒制等培养的学生1000人。', 3,
     8, 14, 14, '订单班、学徒制学生1000人', NULL, '通过大规模开展订单班、现代学徒制等培养模式，形成了校企双主体育人的稳定规模；覆盖了学生从招生到就业的关键环节。预期在年度培养周期内，实现了为产业链精准输送1000名高素质技术技能人才的目标。',
     '2-3-12-1 专业群内专业签订订单班、学徒制培养协议
2-3-12-2 订单班、学徒制人才培养方案
2-3-12-3 订单班、学徒制学生选拔名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2027, 'SG02031327', '2-3-13 省级现代产业学院通过验收；', 3,
     11, 23, 23, '产业学院通过验收1个', NULL, '通过省级现代产业学院的正式验收，形成了产教融合平台建设的权威性成果与标杆，预期在验收后，办学品牌影响力与资源吸纳能力进一步提升。',
     '2-3-13-1申报验收的材料
2-3-13-1省级主管部门发布的验收通过正式文件或公示', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2027, 'SG02031427', '2-3-14 按照校企协同育人方案开展人才培养工作，组建学徒制班或订单班2个；【追加】：每年订单班、学徒制等培养的学生1000人；', 3,
     8, 14, 14, '学徒制班或订单班2个，学生1000人', NULL, '通过大规模开展订单班、现代学徒制等培养模式，形成了校企双主体育人的稳定规模；覆盖了学生从招生到就业的关键环节。预期在年度培养周期内，实现了为产业链精准输送1000名高素质技术技能人才的目标。',
     '2-3-14-1 专业群内专业签订订单班、学徒制培养协议
2-3-14-2 订单班人才培养方案
2-3-14-3 学生选拔名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2027, 'SG02031527', '2-3-15 技术服务平台推动研发与成果转化10项，产生经济效益2000万元；', 3,
     11, 24, 24, '研发与成果转化10项，经济效益2000万元', NULL, '通过技术服务平台推动10项研发与转化，形成了以市场价值为导向的产学研用闭环，覆盖了从技术攻关到应用推广的关键环节。预期在计划期内，实现了2000万元的经济效益，显著提升了服务产业的能力。',
     '2-3-15-1 统计学校技术服务平台研发与专利成果转化项目明细
2-3-15-2  统计学校技术服务平台研发与专利成果转化合同及发票一套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2027, 'SG02031627', '2-3-16 完善公共服务平台产业信息结构，收集500条以上行业信息；', 3,
     11, 23, 23, '收集信息500条', NULL, '通过优化信息结构与大规模采集数据，形成了更具深度和价值的产业情报库，预期在本阶段，实现了平台从“信息聚合”向“知识服务”的转型升级。',
     '2-3-16-1公共服务平台发布技能鉴定信息
2-3-16-2公共服务平台发布行业重要新闻
2-3-16-2公共服务平台发布企业人才需求信息
（以上信息共计大于300条）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2027, 'SG02031727', '2-3-17 四技服务经费400万元，授权或转让知识产权30项。【追加】：四技服务经费200万元', 3,
     11, 24, 24, '四技服务经费600万元，授权或转让知识产权30项', NULL, '通过开展四技服务与知识产权运营，形成了知识价值变现与反哺教学的良性循环；明确了以知识产权为核心的有偿服务模式。预期在年度内，实现了累计600万元服务经费与30项知识产权转化的双重目标。',
     '2-3-17-1 统计学校范围内四技服务、专利转让情况明细
2-3-17-2 统计学校范围内四技服务、专利转让合同及发票', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2028, 'SG02031828', '2-3-18 巩固资源整合机制，形成稳定的资源整合与分配模式；', 3,
     11, 24, 24, '机制文件1套', NULL, '通过前期实践的总结与固化，形成了成熟、稳定、可复制的资源整合与分配模式，预期在未来工作中，实现了资源整合工作的制度化、流程化与高效化。',
     '2-3-18-1 修订：《产教联合体技术攻关与成果转化合作办法》
2-3-18-2 修订：校企共建科研团队效能评估与反馈办法', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2028, 'SG02031928', '2-3-19 新增2个订单班或学徒制班；【追加】：每年订单班、学徒制等培养的学生1000人；', 3,
     8, 14, 14, '学徒制班或订单班2个，学生1000人', NULL, '通过大规模开展订单班、现代学徒制等培养模式，形成了校企双主体育人的稳定规模；覆盖了学生从招生到就业的关键环节。预期在年度培养周期内，实现了为产业链精准输送1000名高素质技术技能人才的目标。',
     '2-3-19-1 专业群内专业签订订单班、学徒制培养协议
2-3-19-2 订单班人才培养方案
2-3-19-3 学生选拔名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2028, 'SG02032028', '2-3-20 技术服务平台推动研发与成果转化10项，产生经济效益2000万元；', 3,
     11, 24, 24, '研发与成果转化10项，经济效益2000万元', NULL, '通过技术服务平台推动10项研发与转化，形成了以市场价值为导向的产学研用闭环，覆盖了从技术攻关到应用推广的关键环节。预期在计划期内，实现了2000万元的经济效益，显著提升了服务产业的能力。',
     '2-3-20-1 统计学校技术服务平台研发与专利成果转化项目明细
2-3-20-2  统计学校技术服务平台研发与专利成果转化合同及发票一套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2028, 'SG02032128', '2-3-21 完善公共服务平台功能，持续完善；', 3,
     11, 23, 23, '完善服务平台1个', NULL, '通过功能的迭代升级，形成了以用户需求为导向的平台功能优化机制，预期在本期完善后，实现了用户体验的改善与服务效率的提升。',
     '2-3-21-1 公共服务平台建设调研总结（过程照片）
2-3-21-2公共服务平台建设完善研讨会
2-3-21-3公共服务平台建设完善说明
2-3-21-4公共服务平台建设完善效果满意度调查
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2028, 'SG02032228', '2-3-22 四技服务经费400万元，授权或转让知识产权30项。【追加】：四技服务经费200万元。', 3,
     11, 24, 24, '四技服务经费600万元，授权或转让知识产权30项', NULL, '通过开展四技服务与知识产权运营，形成了知识价值变现与反哺教学的良性循环；明确了以知识产权为核心的有偿服务模式。预期在年度内，实现了累计600万元服务经费与30项知识产权转化的双重目标。',
     '2-3-22-1 统计学校范围内四技服务、专利转让情况明细
2-3-22-2 统计学校范围内四技服务、专利转让合同及发票', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2029, 'SG02032329', '2-3-23 全面总结各平台建设与运营经验，形成总结报告；', 3,
     11, 24, 24, '总结报告1个', NULL, '通过对各平台系统性的总结，形成了涵盖建设、运营、管理全过程的宝贵经验与知识资产，预期在报告完成后，实现了组织智慧的有效沉淀与未来工作的精准指导。',
     '2-3-23-技术服务平台、公共服务平台建设与运营过程调查问卷等收集的意见清单
2-3-23-2技术服务平台、公共服务平台建设与运营经验总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2029, 'SG02032429', '2-3-24 总结协同育人成果，形成典型案例1个；', 3,
     8, 14, 14, '典型案例1个', NULL, '通过提炼协同育人成果形成案例，形成了可复制、可推广的产教融合育人模式范本，预期在案例推广后，实现了项目示范效应与影响力的扩大。',
     '2-3-24-1典型案例申报书/文本
2-3-24-2案例被采纳、发布的官方证明或媒体报道（视情况）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2029, 'SG02032529', '2-3-25 技术服务平台推动研发与成果转化10项，产生经济效益2000万元；', 3,
     11, 24, 24, '研发与成果转化10项，经济效益2000万元', NULL, '通过技术服务平台推动10项研发与转化，形成了以市场价值为导向的产学研用闭环，覆盖了从技术攻关到应用推广的关键环节。预期在计划期内，实现了2000万元的经济效益，显著提升了服务产业的能力。',
     '2-3-25-1 统计学校技术服务平台研发与专利成果转化项目明细
2-3-25-2  统计学校技术服务平台研发与专利成果转化合同及发票一套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2029, 'SG02032629', '2-3-26 组织3次行业交流活动；', 3,
     11, 25, 25, '行业交流3次', NULL, '通过组织系列行业交流活动，形成了主动输出经验、引领行业发展的开放姿态与合作网络，预期在活动结束后，实现了资源链接范围的扩大。',
     '2-3-26-1 三次行业交流活动名称
2-3-26-2 活动的通知、新闻与现场照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2029, 'SG02032729', '2-3-27 四技服务经费400万元，授权或转让知识产权30项。【追加】：四技服务经费200万元。', 3,
     11, 24, 24, '四技服务经费600万元，授权或转让知识产权30项', NULL, '通过开展四技服务与知识产权运营，形成了知识价值变现与反哺教学的良性循环；明确了以知识产权为核心的有偿服务模式。预期在年度内，实现了累计600万元服务经费与30项知识产权转化的双重目标。',
     '2-3-27-1 统计学校范围内四技服务、专利转让情况明细
2-3-27-2 统计学校范围内四技服务、专利转让合同及发票', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 16, '0,2,16', 2029, 'SG02032829', '2-3-28 【新增】每年订单班、学徒制等培养的学生1000人。', 3,
     8, 14, 14, '订单班、学徒制学生1000人', NULL, '通过大规模开展订单班、现代学徒制等培养模式，形成了校企双主体育人的稳定规模；覆盖了学生从招生到就业的关键环节。预期在年度培养周期内，实现了为产业链精准输送1000名高素质技术技能人才的目标。',
     '2-3-28-1 专业群内专业签订订单班培养协议；
2-3-28-2 订单班人才培养方案
2-3-28-3 学生选拔名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2025, 'SG03010125', '3-1-1 完善团队管理制度和工作流程,优化校企混编教学团队；', 3,
     1, 3, 3, '完善管理制度1个；优化混编团队1支', NULL, '通过《徐州工业职业技术学院校企混编团队管理办法》完善团队管理制度和工作流程，覆盖了全校所有专业群，明确了混编团队人员规模和课时量，预期在2年内实现了专业群混标团队的优化。',
     '3-1-1-1 座谈研讨资料1套
3-1-1-2 完善文件《产教联合体校企高层次人才互兼互聘实施办法》
3-1-1-2 混编教学团队名单
3-1-1-3 企业教师兼课明细', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2025, 'SG03010225', '3-1-2 开展对工程机械产业链关键环节的10 家重点企业初步调研；', 3,
     8, 12, 12, '调研报告1份', NULL, '通过开展对工程机械产业链关键环节调研形成了调研报告1份，覆盖了10 家重点企业，明确了工程机械产业链对人才的需要分析。',
     '3-1-2-1 企业调研报告1份
3-1-2-2 企业调研问卷1套
3-1-2-3 企业调研照片1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2025, 'SG03010325', '3-1-3 修订人才培养方案，优化课程体系、实践教学体系 ；', 3,
     8, 12, 12, '人才培养方案1套', NULL, '通过修订人才培养方案形成了动态优化机制，覆盖了人才培养全过程，明确了立德树人的根本任务与时代要求，预期在构建高水平人才培养模式上实现了创新引领。',
     '3-1-3-1 企业调研报告1份
3-1-3-2 人才培养方案研讨照片
3-1-3-3 专业群人才培养方案1套，包括课程体系与实践教学体系
3-1-3-4  党政联席会议记录
3-1-3-5  人才培养方案论证资料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2025, 'SG03010425', '3-1-4 确定专业课企业实施课程及项目。', 3,
     8, 12, 12, '企业实施课程及项目1批', NULL, '通过开展对工程机械产业链关键环节调研形成了调研报告1份，明确了工程机械产业链对人才的需要，确定了专业课企业实施课程及项目。',
     '3-1-4-1 调研报告1份
3-1-4-2 企业实施课程及项目研讨照片
3-1-4-3 专业课企业实施课程与项目', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2025, 'SG03010525', '3-1-5 调研至少 10 家重点企业，更新 8 项岗位标准；', 3,
     8, 12, 12, '更新8项岗位标准', NULL, '通过开展对工程机械产业链关键环节调研形成了调研报告1份，覆盖了10 家重点企业，明确了人才培养对接产业发展的升级路径，预期在建设周期内岗位标准更新8项。',
     '3-1-5-1调研报告1份（包括问卷调查、照片等）
3-1-5-2 调研问卷1套
3-1-5-3 岗位标准更新研讨会资料1套
3-1-5-4 岗位标准更新8项', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2026, 'SG03010626', '3-1-6 完善现代学徒制课程标准，修订优化课程内容；', 3,
     8, 12, 12, '现代学徒制课程标准1套', NULL, '通过成立现代学徒制课程专项工作组，形成了以岗位能力为核心、工学结合为路径的新版课程标准体系，覆盖了全部试点专业的关键技能领域，明确了校企双导师的职责分工与教学实施规范，预期在下个学年实现全面落地实施，并将课程内容更新率稳定控制在不低于10%，为现代学徒制人才培养工作的深化与高质量开展奠定了扎实基础。',
     '3-1-6-1 现代学徒制座谈研讨资料1套
3-1-6-2 修订现代学徒制课程标准1套
3-1-6-3 课程内容更新率不低于10%
3-1-6-4 更新后学生使用反馈报告 ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2026, 'SG03010726', '3-1-7 专业拓展课程企业场地教学占比达20%；', 3,
     8, 15, 15, '企业场地教学占比达20%', NULL, '通过成立校企协同教学工作组，形成了专业拓展课程在企业真实场景中教学占比不低于20%的常态化教学机制，覆盖了全部相关课程，明确了由校企双方协议约定的场地、设备、师资及安全责任，预期在本年度教学周期内实现企业场地教学的全面规范化运行，为深化产教融合、提升学生岗位胜任力工作奠定了扎实基础',
     '3-1-7-1 专业拓展课程实施场地一览表
3-1-7-2 企业与学校签订相关协议
3-1-7-3 企业场地教学实施反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2026, 'SG03010826', '3-1-8 核心课真实项目课时占比达20%；', 3,
     8, 12, 12, '实项目课时占比达20%', NULL, '通过成立校企双元课程开发团队，形成了以企业真实生产项目为载体、其课时在核心课程中占比不低于20%的教学新模式，覆盖了全部专业核心课程体系，明确了真实项目的来源、教学标准与考核评价办法，预期在新学年全面实现核心课程教学与产业实践的深度耦合，为高素质技术技能人才的培养工作奠定了扎实基础。',
     '3-1-8-1 核心课程融入真实项目座谈研讨会
3-1-8-2 核心课程融入真实项目一览表
3-1-8-3 核心课程融入真实项目运行反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2026, 'SG03010926', '3-1-9 组建 2 个学徒制或订单班，工学交替课时占比 20%。', 3,
     8, 14, 14, '学徒制班级2个，课时占比 20%。', NULL, '通过成立订单班/学徒制专项工作小组，形成了以“工学交替”为特色、其课时占总学时比例不低于20%的人才培养新模式，预期首批组建的2个订单班或学徒制班，明确了校企双方在人才培养、教学实施与学生管理中的具体权责，本培养周期内实现“工学交替”教学环节的规范化管理与高效运行。',
     '3-1-9-1 校企合作协议
3-1-9-2 学徒制或订单班组班通知
3-1-9-3 学徒制或订单班人员名单
3-1-9-4 学徒制或订单班人才培养方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2027, 'SG0301027', '3-1-10 开发四类融通课程5 门；', 3,
     8, 14, 14, '开发四类融通课程5 门', NULL, '通过成立跨领域课程开发团队，形成了涵盖四种关键融通类型、共计5门新课的课程体系，覆盖了理论实践、技术与素养、校内校外等多维度融通领域，明确了各类课程的教学目标、内容标准与评价方式，预期在新学年全面投入教学运行并实现优质教学资源的有效整合，为构建复合型人才培养课程体系的工作奠定了扎实基础。',
     '3-1-10-1 四类融通课程的立项通知
3-1-10-2 四类融通课程的公示文件
3-1-10-3 四类融通课程的案例1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2027, 'SG03011127', '3-1-11 更新核心课程内容，真实项目课时占比30%；', 3,
     8, 12, 12, '更新核心课程内容，真实项目课时占比30%；', NULL, '通过构建以真实生产项目驱动教学内容全面更新的教学模式，真实项目课时占比不低于30%，覆盖全部专业核心课程，明确真实项目的引入标准、教学转化流程及质量评价体系，预期在周期内实现核心课程内容与行业前沿技术的同步升级，提升了学生综合职业能力与岗位适应力。',
     '3-1-11-1 核心课程更新项目一览表
3-1-11-2 核心课程融入真实项目一览表
3-1-11-3 教学实施反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2027, 'SG03011227', '3-1-12 组建 2 个学徒制或订单班，专业课工学交替课时占比 30% ；', 3,
     8, 14, 14, '组建 2 个学徒制或订单班，工学交替课时占比 30% ；', NULL, '通过成立订单班/学徒制专项工作小组，形成了以“工学交替”为特色、其课时占总学时比例不低于30%的人才培养新模式，预期首批组建的2个订单班或学徒制班，明确了校企双方在人才培养、教学实施与学生管理中的具体权责，本培养周期内实现“工学交替”教学环节的规范化管理与高效运行。',
     '3-1-12-1 校企合作协议
3-1-12-2 学徒制或订单班组班通知
3-1-12-3 学徒制或订单班人员名单
3-1-12-4 修订学徒制或订单班人才培养方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2027, 'SG03011327', '3-1-13 专业课程企业场地教学占比达30%。', 3,
     8, 15, 15, '企业场地教学占比达30%', NULL, '通过专业课程在企业真实场景中教学占比达30%的常态化运行机制，覆盖了所有相关的专业核心课程，明确了校企双方在场地、设备、师资及安全等方面的权责与资源保障，预期在本学年内实现企业场地教学的全面规范化管理与质量提升，强化学生实践技能、深化产教融合。',
     '3-1-13-1 专业拓展课程实施场地一览表
3-1-13-2 企业与学校签订相关协议
3-1-13-3 企业场地教学实施反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2028, 'SG03011428', '3-1-14 开发四类融通课程5 门；', 3,
     8, 14, 14, '开发四类融通课程5 门', NULL, '通过深化四种关键融通课程体系，明确各类课程的教学目标、内容标准与评价方式，新开发5们四类融通课程，为构建复合型人才培养课程体系的工作奠定了扎实基础。',
     '3-1-14-1 四类融通课程的立项通知
3-1-14-2 四类融通课程的公示文件
3-1-14-3 四类融通课程的案例1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2028, 'SG03011528', '3-1-15 引入新项目，更新核心课程项目内容  ；', 3,
     8, 12, 12, '更新核心课程项目内容', NULL, '通过成立核心课程项目资源建设工作组，形成了通过持续引入行业前沿新项目以驱动核心课程内容动态更新的长效机制，覆盖了全部有待升级的核心课程，明确了新项目的遴选标准、教学化改造流程及课程植入方案，预期在新学年教学实施中实现核心课程项目库的系统性优化与内容换代，为保障教学内容先进性、提升人才培养针对性的工作奠定了扎实基础。',
     '3-1-15-1 校企真实项目融入座谈会资料1套
3-1-15-2 核心课程融入真实项目一览表', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2028, 'SG03011628', '3-1-16 组建2个学徒制或订单班。', 3,
     8, 14, 14, '组建2个学徒制或订单班', NULL, '通过成立校企协同育人工作专班，形成了在2个学徒制或订单班中深度实施、专业课“工学交替”课时占比达30%的人才培养新范式，覆盖了订单班/学徒制所涉及的全部核心专业技能课程，明确了校企双导师在“工学交替”环节的教学分工、学生管理与安全保障责任，预期培养周期内实现“工学交替”教学环节的标准化管理与质量提升，为深化产教融合、培养高素质技术技能人才的工作奠定了扎实基础。',
     '3-1-16-1 校企合作协议
3-1-16-2学徒制或订单班组班通知
3-1-16-3 学徒制或订单班人员名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2029, 'SG03011729', '3-1-17 形成人才培养质量跟踪与评价体系；', 3,
     8, 11, 11, '人才培养质量跟踪与评价体系1套', NULL, '通过成立人才培养质量评价中心，形成了由学校、企业、学生等多方参与的常态化人才培养质量跟踪与闭环评价体系，覆盖了从入学到就业的完整人才培养链条，明确了各评价环节的指标、方法与主体责任，预期在未来两年内实现基于数据驱动的教学质量持续改进机制常态化运行，为全面提升人才培养质量与适应性的战略工作奠定了扎实基础。',
     '3-1-17-1  毕业生调查数据与报告
3-1-17-2  企业评价与反馈记录
3-1-17-3 《在校生学业评价与反馈机制实施方案》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 17, '0,3,17', 2029, 'SG03011829', '3-1-18 获得省级教学成果奖2个，国家级教学成果奖1个。', 3,
     8, 14, 14, '省教学成果2个，国家教学成果1个', '国家级', '通过成立教学成果奖培育与申报专项工作组，形成了一套行之有效、可推广应用的先进教学改革模式与实践成果，覆盖了多个重点专业领域并成功获得省级教学成果奖2项、国家级教学成果奖1项的重大突破，明确了成果的后续推广应用路径与持续改进机制，为学校全面提升办学声誉、深化教育教学改革的核心工作奠定了扎实基础。',
     '3-1-18-1省教学成果2个
3-1-18-2国家教学成果1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2025, 'SG03020125', '3-2-1 组建 “四融四通” 分类培养研究小组；', 3,
     8, 9, 9, '组建分类培养研究小组1个', NULL, '通过《关于成立徐州工业职业技术学院“四融四通”分类培养研究小组的通知》组建 专业群“四融四通” 分类培养研究小组完，覆盖了全校所有专业群，明确分类培养研究小组任务与目标。',
     '3-2-1-1  “四融四通” 分类培养座谈研讨会资料1套
3-2-1-2 出台《关于成立徐州工业职业技术学院“四融四通”分类培养研究小组的通知》文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2025, 'SG03020225', '3-2-2 开展学生学习基础、发展能力和职业追求的调研工作；', 3,
     8, 16, 16, '调研报告1份', NULL, '通过学生发展状况的全景画像形成调研报告1份，覆盖了学习基础、发展能力与职业追求等多维层面，明确了育人工作的重点与方向，预期在精准施策与个性化培养上实现了有效支撑。',
     '3-2-2-1 学校组织新生入学测试相关材料
3-2-2-2 线上问卷调查问卷
3-2-2-3 形成调研分析报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2025, 'SG03020325', '3-2-3 出台“育训并重、融合发展”职业培训模式的管理办法；', 3,
     11, 22, 22, '职业培训管理办法1套', NULL, '通过校企协同培养形成了“育训并重、融合发展”的制度化模式，覆盖了组织、实施与保障等关键环节，明确了职业培训与学历教育同等重要的战略定位，预期在构建一体化治理格局上实现了关键突破。',
     '3-2-3-1 “育训并重、融合发展”职业培训座谈研讨会资料1套
3-2-3-2 关于印发《徐州工业职业技术“育训并重、融合发展”职业培训管理办法》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2025, 'SG03020425', '3-2-4 稳定现有“3+3”贯通培养项目，申报“3+2”贯通培养项目；', 3,
     8, 14, 14, '稳定“3+3”贯通项目，申报“3+2”贯通项目', '省级及以上', '通过中高本贯通培养形成了多层次培养模式，覆盖了“3+3”与“3+2”项目类型，明确了现代职业教育体系的发展路径，预期在专业建设和升学通道上实现了优化与拓展。',
     '3-2-4-1 近3年“3+3”贯通培养项目审批表佐证规模
3-2-4-2 2025年机电一体化技术“3+2”贯通培养项目批文', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2025, 'SG03020525', '3-2-5 学生省级比赛获奖10项，国家级获奖2项。', 3,
     8, 13, 13, '省级获奖10项，国家获奖2项', '省级及以上', '通过人才培养形成了系统化的学生竞赛培养体系，覆盖了梯队建设与选拔标准，明确了以赛促教、以赛促学的育人导向，预期在省级与国家级赛事中分别实现10项和2项获奖。',
     '3-2-5-1 国家级获奖证书2个
3-2-5-2 省级获奖证书10个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2026, 'SG03020626', '3-2-6 落实“四融四通” 分类培养，企业导师承担专业占比达20%；', 3,
     8, 10, 10, '企业导师承担专业占比达20%', NULL, '通过成立 “‘四融四通’分类培养改革领导小组” ，形成了以“四融四通”为核心理念、企业导师承担专业课教学占比达20%的个性化分类培养体系，覆盖了专业群全体学生，明确了企业导师的准入标准、教学职责与协同育人机制，实现校企双元育人模式的深度落地与质量提升，为构建特色鲜明的高水平技术技能人才培养体系工作奠定了扎实基础。',
     '3-2-6-1 企业导师名单
3-2-6-2企业导师承担课时总量及占比', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2026, 'SG03020726', '3-2-7 按照“育训并重、融合发展”职业培训模式框架，开展职业培训；', 3,
     11, 22, 22, '开展职业培训', NULL, '通过成立职业培训专项工作办公室，形成了“育训并重、融合发展”的职业培训新模式，覆盖了校内学生技能提升与校外社会人员职业发展两类核心群体，明确了培训项目开发、教学实施与质量评估的标准流程，为构建服务区域经济发展的终身职业培训体系工作奠定了扎实基础。',
     '3-2-7-1 学校与企业职业培训合同
3-2-7-2 职业培训资料1套
3-2-7-3 职业培训学员名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2026, 'SG03020826', '3-2-8 优化“3+3”贯通培养转段办法，出台“3+2”联合培养师资管理办法；', 3,
     8, 16, 16, '优化“3+3”培养转段办法，出台“3+2”师资管理办法', '省级及以上', '通过成立贯通培养制度建设工作专班，形成了由《“3+3”贯通培养转段优化办法》与《“3+2”联合培养师资管理办法》构成的系统化制度体系，覆盖了学生从选拔、培养到转段的全过程，明确了转段的核心标准、流程以及联合培养师资的选聘、职责与考核机制，预期在新一轮培养周期内实现人才衔接培养质量的显著提升与师资队伍建设的规范化，为构建高质量、一体化的现代职业教育人才培养立交桥工作奠定了扎实基础。',
     '3-2-8-1 优化后“3+3”培养转段办法
3-2-8-2 关于印发《徐州工业职业技术“3+2”师资管理办法》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2026, 'SG03020926', '3-2-9 学生省级比赛获奖10项，国家级获奖2项。', 3,
     8, 13, 13, '省级获奖10项，国家获奖2项', '省级及以上', '通过人才培养形成了系统化的学生竞赛培养体系，覆盖了梯队建设与选拔标准，明确了以赛促教、以赛促学的育人导向，预期在省级与国家级赛事中分别实现10项和2项获奖。',
     '3-2-9-1 省级获奖证书10个
3-2-9-2 国家获奖证书2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2026, 'SG03021026', '3-2-10 【新增】形成“招生-培养-就业-发展”全链条服务学生成长模式1套。', 3,
     8, 9, 9, '全链条服务学生成长模式1套', NULL, '形成了一套贯穿“招生-培养-就业-发展”四大环节的系统性学生成长服务模式，覆盖了学生从入学到职业发展的全生命周期，明确了各环节的服务标准、协同机制与主体责任，预期在三年内实现全链条数据贯通与服务效能显著提升，为学校构建以学生为中心、促进高质量就业与可持续发展的育人工作奠定了扎实基础。',
     '3-2-10-1 《关于构建“招生-培养-就业-发展”全链条育人模式的实施方案》
3-2-10-2  全链条服务学生成长模式1套
3-2-10-3 《毕业生就业质量年度报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2027, 'SG03021127', '3-2-11 全面实施 “四融四通”分类培养，跟踪学生成长情况，建立反馈机制；', 3,
     8, 17, 17, '反馈机制1套', NULL, '通过“四融四通”分类培养，形成了贯穿学生成长全过程的分类培养体系与常态化跟踪反馈机制，覆盖了全体在校学生及各培养阶段，明确了成长数据采集、分析评估与教学改进的闭环管理流程，基于学生发展大数据的精准化培养模式转型，为全面提升人才培养质量与促进学生终身发展的工作奠定了扎实基础。',
     '3-2-11-1  “四融四通”分类培养通知选拔通知
3-2-11-2 “四融四通”分类培养过程资料1套
3-2-11-2  学生成长反馈机制1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2027, 'SG03021227', '3-2-12 总结职业培训模式经验，制定职业标准、岗位规范、评价标准2个；', 3,
     11, 22, 22, '制定标准2个', NULL, '通过职业培训标准化建设委员会，形成了一套经过实践检验、可复制推广的职业培训模式与2项关键的职业标准及岗位评价规范，覆盖了重点建设与发展的职业培训领域，明确了从培训实施到人才评价的核心技术要求与质量标尺，预期在未来两年内实现该标准体系在区域内的示范应用与持续优化，为构建科学化、规范化的高质量职业培训体系工作奠定了扎实基础。',
     '3-2-12-1 凝练职业培训模式1个
3-2-12-2 标准制定研讨会纪要、照片等
3-2-12-3 制定标准2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2027, 'SG03021327', '3-2-13 开发职业培训项目2个，将校内专业课程转换职业培训课程 3 门；', 3,
     11, 22, 22, '开发培训项目2个，转换职业培训课程 3 门', NULL, '通过职业培训与课程转化专项工作组，形成了以开发2个新型职业培训项目与完成3门校内专业课程向职业培训课程转化的双轮驱动机制，覆盖了重点行业技能提升与岗位职业能力认证两大领域，明确了课程转换标准、培训项目实施流程与质量评估体系，实现培训项目的全面落地与课程资源的有效利用，为构建校企融合、育训结合的职业培训体系工作奠定了扎实基础。',
     '3-2-13-1 职业培训项目研讨会纪要、照片等
3-2-13-2 开发培训项目2个
3-2-13-2 转换职业培训课程 3 门', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2027, 'SG03021427', '3-2-14 学生省级比赛获奖10项，国家级获奖2项。', 3,
     8, 13, 13, '省级获奖10项，国家获奖2项', '省级及以上', '通过人才培养形成了系统化的学生竞赛培养体系，覆盖了梯队建设与选拔标准，明确了以赛促教、以赛促学的育人导向，预期在省级与国家级赛事中分别实现10项和2项获奖。',
     '3-2-14-1 省级获奖证书10个
3-2-14-2 国家获奖证书2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2027, 'SG03021527', '3-2-15 【新增】完善“招生-培养-就业-发展”全链条服务学生成长模式。', 3,
     8, 9, 9, '完善全链条服务学生成长模式', NULL, '完善了贯穿"招生-培养-就业-发展"四大环节的闭环式学生成长服务体系，覆盖了专专业群全体学生，明确了各环节的服务标准、部门职责与协同机制，预期在两年内实现全链条数据的互联互通与服务效能的全面提升，为构建以学生发展为中心、持续改进的人才培养工作奠定了扎实基础。',
     '3-2-15-1 上年度运行情况研讨会纪要、照片等
3-2-15-2 完善全链条服务学生成长模式
3-2-15-2 《毕业生就业质量年度报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2028, 'SG03021628', '3-2-16 总结职业培训经验，与5家企业合作开展培训项目；', 3,
     11, 22, 22, '与5家企业合作开展培训项目', NULL, '形成了基于前期经验总结、与5家知名企业共建的职业培训合作新机制，覆盖了智能制造、数字技术等重点产业领域，明确了合作各方的权责分工、资源投入与收益分配机制，实现培训项目的规模化、规范化实施，为构建响应区域产业发展需求的职业培训体系工作奠定了扎实基础',
     '3-2-16-1 职业培训座谈研讨会资料1套
3-2-16-2 学校与企业职业培训合同
3-2-16-3 职业培训资料1套
3-2-16-4 职业培训学员名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2028, 'SG03021728', '3-2-17 制定职业标准、岗位规范、评价标准3个；', 3,
     8, 12, 12, '制定标准3个', NULL, '形成了包含职业标准、岗位规范与评价标准在内的3项核心标准文件，覆盖了重点专业对应的关键职业岗位群，明确了各岗位的能力要求、工作规范与考核评价维度，实现标准在人才培养与就业评价中的全面应用，为推进人才培养标准化与就业质量提升工作奠定了扎实基础。',
     '3-2-17-1 标准制定座谈研讨会纪要、照片等
3-2-17-2 制定标准3个
3-2-17-3  标准采纳情况', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2028, 'SG03021828', '3-2-18 开发职业培训项目 3 个，将校内专业课程转换职业培训课程 4 门；', 3,
     11, 22, 22, '开发培训项目3个，转换职业培训课程 4门', NULL, '形成了包含3个新型职业培训项目与4门校内专业课程向职业培训课程转化的资源体系，覆盖了先进制造、现代服务等重点行业领域，明确了课程转化标准、培训项目开发流程与质量评估机制，实现培训项目的规模化实施与课程资源的高效利用，为构建校企协同、资源互通的现代职业培训体系工作奠定了扎实基础。',
     '3-2-18-1 职业培训项目研讨会纪要、照片等
3-2-18-2 开发培训项目3个
3-2-18-3 转换职业培训课程 4 门', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2028, 'SG03021928', '3-2-19 学生省级比赛获奖10项，国家级获奖2项。', 3,
     8, 13, 13, '省级获奖10项，国家获奖2项', '省级及以上', '通过人才培养形成了系统化的学生竞赛培养体系，覆盖了梯队建设与选拔标准，明确了以赛促教、以赛促学的育人导向，预期在省级与国家级赛事中分别实现10项和2项获奖。',
     '3-2-19-1 省级获奖证书10个
3-2-19-2 国家获奖证书2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2028, 'SG03022028', '3-2-20 【新增】校内推广“招生-培养-就业-发展”全链条服务学生成长模式。', 3,
     8, 9, 9, '校内推广全链条服务学生成长模式', NULL, '在全校范围内复制推广“招生-培养-就业-发展”的学生成长服务体系，明确模式推广的路径、各环节衔接标准与主体责任，实现该模式在全校的有效运行与持续优化，为全面提升学生服务质量与促进毕业生可持续发展的工作奠定了扎实基础。',
     '3-2-20-1 专业群座谈交流会
3-2-20-2 相应新闻报道
3-2-20-3 校内3个专业群应用证明', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2029, 'SG03022129', '3-2-21 推广“四融四通”分类培养；', 3,
     8, 17, 17, '推广“四融四通”分类培养', NULL, '在全校范围内复制推的“四融四通”分类培养实施体系，覆盖了各二级学院主要专业群及不同发展类型的学生群体，明确了模式推广的路径、资源配置要求及各环节质量标准，实现分类培养模式在全校有效运行与持续优化，为全面提升人才培养质量与促进学生个性化发展的工作奠定了扎实基础。',
     '3-2-21-1 参加相应会议照片或交流发言稿
3-2-21-2 相应新闻报道
3-2-21-3 “四融四通”分类培养推广反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2029, 'SG03022229', '3-2-22 凝练 “育训并重、融合发展” 职业培训模式的经验，向行业推广；', 3,
     11, 22, 22, '凝练职业培训模式，向行业推广', NULL, '形成了一套具有普适性、可复制的职业培训模式经验总结与推广方案，覆盖了区域重点行业和骨干企业，明确了模式的核心内涵、实施路径与合作推广机制，预期在未来一年内实现该模式在行业内的成功应用与辐射引领，为构建区域现代职业培训体系、提升技术技能人才培养质量工作奠定了扎实基础。',
     '3-2-22-1 职业培训模式研讨会纪要、照片等
3-2-22-2 凝练 “育训并重、融合发展” 职业培训模式
3-2-22-3 共计3所院校应用证明', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2029, 'SG03022329', '3-2-23 学生省级比赛获奖10项。', 3,
     8, 13, 13, '学生省级比赛获奖10项', '省级及以上', '通过人才培养形成了系统化的学生竞赛培养体系，覆盖了梯队建设与选拔标准，明确了以赛促教、以赛促学的育人导向，预期在省级实现10项获奖。',
     '3-2-23-1 省级获奖证书10个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2029, 'SG03022429', '3-2-24 国家级“五育”方面项目立项或获奖1个。', 3,
     8, 9, 9, '国家级项目或获奖1项', '国家级', '形成了以国家级奖项为引领的“五育并举”项目培育与孵化机制，覆盖了项目申报、实施与成果转化的全流程，明确了国家级项目的建设标准、支持政策与成果验收要求，预期实现国家级“五育”类项目立项或获奖1项的突破，为学校全面提升育人质量、彰显办学特色与核心竞争力工作奠定了扎实基础。',
     '3-2-24-1 国家级项目或获奖1项', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 18, '0,3,18', 2029, 'SG03022529', '3-2-25 【新增】校外推广“招生-培养-就业-发展”全链条服务学生成长模式。', 3,
     8, 9, 9, '校外推广全链条服务学生成长模式', NULL, '通过在合作院校及企业间复制推广的"招生-培养-就业-发展"学生成长服务模式，覆盖了校外合作单位的主要业务环节，明确了合作各方的权责分工、资源投入与收益分配机制，为构建开放共享的现代职业教育体系工作奠定了扎实基础。',
     '3-2-25-1 参加会议照片或交流发言稿
3-2-25-2 服务学生成长相应新闻报道
3-2-25-3 共计3所院校应用证明', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2025, 'SG03030125', '3-3-1 成立“四合作、双循环”专业群运行机制研究小组；', 3,
     8, 12, 12, '成立专业群运行机制研究小组', NULL, '通过《关于成立徐州工业职业技术学院“四合作、双循环”专业群运行机制研究小组的通知》成立了专业群运行机制研究小组，覆盖了产业链与教育链的深度融合，明确了专业群服务产业升级的战略路径，预期在提升专业群整体建设水平与社会服务能力上奠定了坚实基础。',
     '3-3-1-1 “四合作、双循环”专业群运行机制座谈研讨会
3-3-1-2 出台《关于成立徐州工业职业技术学院“四合作、双循环”专业群运行机制研究小组的通知（试行）》文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2025, 'SG03030225', '3-3-2 启动专业评估指标体系框架搭建工作。【追加】：制定专业群质量评价标准1套。', 3,
     8, 12, 12, '制定专业群质量评价标准1套', NULL, '通过专业评估指标体系搭建形成了科学化的专业评价机制，覆盖了输入、过程与产出全链条，明确了以评促建、以评促强的改革路径，预期专业群质量评价标准1套，在全面提升人才培养适应性与办学水平上提供了核心支撑。',
     '3-3-2-1 专业群人才培养质量调研报告
3-3-2-2 专业群人才培养质量调研问卷
3-3-2-3 关于印发《徐州工业职业技术学院专业群质量评价标准（试行）》的通知 ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2025, 'SG03030325', '3-3-3 【新增】专业群匹配产业链矩阵1个。', 3,
     8, 12, 12, '专业群匹配产业链矩阵1个', NULL, '通过专业群匹配产业链矩阵形成了产教深度融合的立体化对接模型，覆盖了产业转型升级对技术技能的迫切需求，明确了高素质技术技能人才的精准供给模式，预期在增强人才培养的适应性与职业教育的贡献度上奠定了坚实基础。',
     '3-3-3-1 专业群匹配产业链调研报告1份
3-3-3-2 专业群匹配产业链座谈研讨会纪要、照片等
3-3-3-3 专业群匹配产业链矩阵图1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2025, 'SG03030425', '3-3-4 【新增】申报1个微专业人才培养试点。', 3,
     8, 12, 12, '申报1个微专业人才培养试点', NULL, '通过微专业人才培养试点形成对传统专业结构的优化与补充体系，覆盖了知识生产的细分领域与交叉地带，明确了“小而活”的专业建设新范式，预期在构建响应敏捷的人才培养新格局上奠定了坚实基础。',
     '3-3-4-1 微专业申报论证研讨会
3-3-4-2 微专业申报书1份（加盖学校公章）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2026, 'SG03030526', '3-3-5 完善“四合作、双循环”专业群运行机制；', 3,
     8, 12, 12, '完善专业群运行机制', NULL, '通过成立“四合作、双循环”专业群运行机制建设委员会，形成了校企协同、内外联动的专业群可持续发展机制，覆盖了专业群内所有专业建设与人才培养的关键环节，明确了各方在合作中的权责分工、资源配置与成果共享机制，预期在三年内实现专业群与产业发展的动态适配与良性循环，为建设高水平、结构化的专业群体系工作奠定了扎实基础。',
     '3-3-5-1 “四合作、双循环”专业群运行机制运行反馈报告
3-3-5-2 “四合作、双循环”专业群运行机制修订研讨会
3-3-5-3 出台《关于修订徐州工业职业技术学院“四合作、双循环”专业群运行机制的通知》文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2026, 'SG03030626', '3-3-6 开展工程机械产业调研，收集人才需求和技术趋势信息，形成调研报告；', 3,
     8, 12, 12, '调研报告1份', NULL, '通过人才需求与技术趋势的专项调研报告，覆盖工程机械产业链上下游的主要企业与技术领域，明确了产业转型升级对人才素质与技能要求的新标准，预期在年内实现调研成果向专业建设与课程开发的精准转化，为推动专业设置与产业需求深度对接的工作奠定了扎实基础。',
     '3-3-6-1 工程机械产业调研报告1份
3-3-6-2 工程机械产业调研问卷1套
3-3-6-3 工程机械产业调研照片1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2026, 'SG03030726', '3-3-7 建立专业评估指标体系，确定各指标权重，制定评估流程。', 3,
     8, 12, 12, '建立专业评估指标体系1套', NULL, '通过构建完整指标框架、权重分配及操作流程的专业评估指标体系，覆盖了专业群的建设与发展全过程，明确了各项评估指标的具体内涵、数据来源及评价标准，预期在一年内实现该体系在全校专业评估中的全面应用与持续优化，为推进专业建设规范化、提升人才培养质量的工作奠定了扎实基础。',
     '3-3-7-1 专业评估指标体系研讨会纪要、照片等
3-3-7-2 专业评估指标体系与权重
3-3-7-3 专业评估指标体系实施资料1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2026, 'SG03030826', '3-3-8 【新增】专业群匹配产业链矩阵1个。', 3,
     8, 12, 12, '专业群匹配产业链矩阵1个', NULL, '通过专业群匹配产业链矩阵形成了产教深度融合的立体化对接模型，覆盖了产业转型升级对技术技能的迫切需求，明确了高素质技术技能人才的精准供给模式，预期在增强人才培养的适应性与职业教育的贡献度上奠定了坚实基础。',
     '3-3-8-1 专业群匹配产业链调研报告1份
3-3-8-2 业群匹配产业链座谈研讨会纪要、照片等
3-3-8-3 专业群匹配产业链矩阵图1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2027, 'SG03030927', '3-3-9 开展常态化合作交流活动，每季度至少组织一次合作主体沟通会议；', 3,
     8, 12, 12, '每季度组织一次沟通会议', NULL, '通过每季度至少组织一次合作主体沟通会议的常态化交流机制，覆盖了所有校企合作单位及相关部门，明确了会议议事规则、议题产生流程及决议落实要求，实现各方信息高效互通与协作效能显著提升，为深化校企合作、促进产教融合工作的可持续发展奠定了扎实基础。',
     '3-3-9-1 累计4次交流会议纪要
3-3-9-2 合作交流照片
3-3-9-3 相关媒体报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2027, 'SG03031027', '3-3-10 开展工程机械产业匹配度调研，专业进行动态调整和升级改造。', 3,
     8, 12, 12, '调研报告1份', NULL, '通过基于产业需求分析的专业动态调整机制与升级改造方案，覆盖了工程机械相关专业群的核心课程体系与实践教学环节，明确了专业调整的触发条件、评估标准与实施流程，预期在一年内完成首批相关专业的针对性优化与升级，为实现专业设置与产业发展需求精准对接、提升人才培养适应性的工作奠定了扎实基础。',
     '3-3-10-1 工程机械产业调研报告1份
3-3-10-2 工程机械产业调研照片1套
3-3-10-3 专业动态调整或升级', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2027, 'SG03031127', '3-3-11 【新增】专业群匹配产业链矩阵2个。', 3,
     8, 12, 12, '专业群匹配产业链矩阵2个', NULL, '通过专业群匹配产业链矩阵形成了产教深度融合的立体化对接模型，覆盖了产业转型升级对技术技能的迫切需求，明确了高素质技术技能人才的精准供给模式，预期在增强人才培养的适应性与职业教育的贡献度上奠定了坚实基础。',
     '3-3-11-1 专业群匹配产业链调研报告1份
3-3-11-2 业群匹配产业链座谈研讨会纪要、照片等
3-3-11-3 专业群匹配产业链矩阵图2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2028, 'SG03031228', '3-3-12 优化 “四合作、双循环” 专业群运行机制，调整合作策略和方式；', 3,
     8, 12, 12, '优化专业群运行机制', NULL, '通过"四合作、双循环"专业群运行机制，形成了基于实践反馈、合作策略与方式持续完善的专业群动态优化机制，覆盖了所有合作项目运行与专业群建设的核心环节，明确了各类合作模式的适用条件、资源配置要求与效益评估标准，为深化产教融合、增强专业群服务产业发展能力的工作奠定了扎实基础。',
     '3-3-12-1 “四合作、双循环”专业群运行机制运行反馈报告
3-3-12-2 “四合作、双循环”专业群运行机制修订研讨会
3-3-12-3 出台《关于修订徐州工业职业技术学院“四合作、双循环”专业群运行机制的通知》文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2028, 'SG03031328', '3-3-13 开展工程机械产业调研，掌握产业发展变化趋势，为专业调整提供依据。', 3,
     8, 12, 12, '调研报告1份', NULL, '通过基于实地调研的产业发展趋势监测与专业预警机制，覆盖了工程机械产业链上下游的主要技术领域和岗位需求，明确了产业技术变革与人才需求变化对专业设置的具体影响路径，实现调研成果向专业结构调整和课程内容更新的有效转化，为建立专业设置与产业发展动态衔接、提升人才培养前瞻性的工作奠定了扎实基础。',
     '3-3-13-1工程机械产业调研报告1份
3-3-13-2 工程机械产业调研问卷1套
3-3-13-3 工程机械产业调研照片1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2028, 'SG03031428', '3-3-14 【新增】专业群匹配产业链矩阵2个。', 3,
     8, 12, 12, '专业群匹配产业链矩阵2个', NULL, '通过专业群匹配产业链矩阵形成了产教深度融合的立体化对接模型，覆盖了产业转型升级对技术技能的迫切需求，明确了高素质技术技能人才的精准供给模式，预期在增强人才培养的适应性与职业教育的贡献度上奠定了坚实基础。',
     '3-3-14-1 专业群匹配产业链调研报告1份
3-3-14-2 业群匹配产业链座谈研讨会纪要、照片等
3-3-14-3 专业群匹配产业链矩阵图2个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2029, 'SG03031529', '3-3-15 形成“双循环、四合作”经验总结报告；', 3,
     8, 12, 12, '经验总结报告1份', NULL, '形成了系统梳理"双循环、四合作"专业群建设模式的经验总结报告，覆盖了各合作项目实施过程与专业群运行的关键环节，明确了该模式的核心机制、成功要素与适用范围，实现经验成果在同类院校的推广应用，为深化产教融合、提升专业群建设水平的工作奠定了扎实基础。',
     '3-3-15-1 “四合作、双循环”运行反馈材料1套
3-3-15-2 “四合作、双循环”运行座谈研讨纪要、照片等
3-3-15-3 “四合作、双循环”经验总结报告1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2029, 'SG03031629', '3-3-16 毕业生本地就业率大于65%，就业相关度达85%。', 3,
     6, 7, 7, '本地就业率大65%，就业相关度达85%。', NULL, '通过毕业生本地高质量就业与专业对口就业的工作体系，覆盖了全体毕业生及本地重点产业领域，明确了就业指导服务、岗位对接与质量跟踪的标准流程，预期在本年度实现毕业生本地就业率达65%、就业相关度达85%的目标，获得高校毕业生基层就业卓越奖，为提升服务区域经济发展能力、实现人才培养与市场需求精准对接的工作奠定了扎实基础。',
     '3-3-16-1 第三方就业调查或就业状态数据
3-3-16-2 高校毕业生基层就业卓越奖', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 19, '0,3,19', 2029, 'SG03031729', '3-3-17 【新增】专业群匹配产业链矩阵1个。', 3,
     8, 12, 12, '专业群匹配产业链矩阵1个', NULL, '通过专业群匹配产业链矩阵形成了产教深度融合的立体化对接模型，覆盖了产业转型升级对技术技能的迫切需求，明确了高素质技术技能人才的精准供给模式，预期在增强人才培养的适应性与职业教育的贡献度上奠定了坚实基础。',
     '3-3-17-1 专业群匹配产业链调研报告1份
3-3-17-2 业群匹配产业链座谈研讨会纪要、照片等
3-3-17-3 专业群匹配产业链矩阵图1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2025, 'SG04010125', '4-1-1 组建工程机械课程开发团队，明确校企权责、成员职责和任务；', 3,
     8, 12, 12, '课程开发团队1个', NULL, '通过召开座谈会研讨，明确了课程开发过程中校企、成员职责，形成了《关于组建工程机械课程开发团队、数智课堂团队的通知》，《关于公布工程机械课程开发团队、数智课堂团队的结果》，覆盖了专业群待开发新课程，预期在制度建设、课程开发工作中进行专业指导，为形成课程开发机制奠定基础。',
     '4-1-1-1召开座谈会研讨资料1份
4-1-1-2《关于组建工程机械课程开发团队、数智课堂团队的通知》
4-1-1-3《关于公布工程机械课程开发团队、数智课堂团队的结果》
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2025, 'SG04010225', '4-1-2 充分调研，形成《课程开发中心运行机制》，明确课程需求，配置资源；', 3,
     8, 12, 12, '《课程开发中心运行机制》1个', NULL, '通过充分调研，形成调研报告1份，形成了《关于成立工程机械课程开发中心的通知》，《工程机械课程开发中心运行机制》，覆盖了专业群全部课程，明确了课程开发过程中开发方向，预期在课程开发工作中实现规范管理，为课程开发工作开展提供制度保障。',
     '4-1-2-1《关于成立工程机械课程开发中心的通知》
4-1-2-2《工程机械课程开发中心运行机制》
4-1-2-3调研总结报告1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2025, 'SG04010325', '4-1-3 开展企业调研，收集典型生产实践项目和岗位标准；', 3,
     8, 13, 13, '典型生产实践项目和岗位标准1套', NULL, '通过对合作企业充分调研，形成调研报告1份，并收集典型生产实践项目和岗位标准5套，形成了课程升级过程中的丰富素材融入，覆盖了专业群工程机械方向课程，明确了课程升级方向，预期在课程开发升级工作中融入更多企业案例，为课程的项目化提供支持。',
     '4-1-3-1调研报告1份（包括问卷调查、照片等）
4-1-3-2 调研问卷1套
4-1-3-3典型生产实践项目和岗位标准5套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2025, 'SG04010425', '4-1-4 培育新建6门在线精品开放课程，资源更新率超15%/年。【追加】：迭代或新建14门在线精品开放课程；
', 3,
     8, 12, 12, '新建20门在线精品开放课程，资源更新率超15%/年', NULL, '通过整理学校新建课程公示文件，形成了《关于在线课程资源更新迭代的通知》、《关于公布课程资源更新迭代的结果》，建设新课程6门、升级已有课程14门，实现了课程资源的结构优化，覆盖了专业群全部课程及新建课程，明确了课程开发的目标和升级目的，预期在课程建设和升级工作中提供指导。',
     '4-1-4-1课程明细及证明材料（数控机床故障诊断与维修、PLC应用技能训练、工业机器人系统运维技能训练、材料力学II、智能传感与检测技术、数字电子技术、电子产品生产与检验等20门）
4-1-4-2《关于在线课程资源更新迭代的通知》
4-1-4-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2025, 'SG04010525', '4-1-5 【新增】在线课程新增人数60000人次。', 3,
     8, 12, 12, '在线课程新增人数60000人次', NULL, '通过整理学校在线课程数据，导出超星/慕课平台后台官方统计数据，形成了在线课程新增人数60000人次的佐证材料，覆盖了专业群全部课程，明确了在线课程使用过程数据监督要求，预期在在线课程建设、使用和国省级申报过程中提供支持。',
     '4-1-5-1在线课程清单列表
4-1-5-2超星/慕课平台后台官方统计数据
4-1-5-3超星/慕课学习人次认证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04010626', '4-1-6 成立课程开发中心，制定《课程开发中心管理制度》，明确开发目标；', 3,
     8, 12, 12, '课程开发中心1个，《课程开发中心管理制度》1个', NULL, '通过课程中心召开座谈会，形成了《关于课程开发中心管理制度的通知》，实现了课程开发中心标准化管理规范，覆盖了专业群全部课程，明确了课程开发过程中开发方向，预期在课程开发工作中实现规范管理，为课程开发工作开展提供制度保障。',
     '4-1-6-1召开座谈会研讨资料1份
4-1-6-2《关于课程开发中心管理制度的通知》
4-1-6-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04010726', '4-1-7 开发中心组织构建“一库一中心三场所”课程开发体系；', 3,
     8, 12, 12, '“一库一中心三场所”课程开发体系1个', NULL, '通过座谈会研讨，形成了《“一库一中心三场所”课程开发体系》文件及应用效果，依托教学资源库、课程开发中心，实现了规范化课程开发体系，为课程开发工作开展提供平台支撑。',
     '4-1-7-1召开座谈会研讨资料1份
4-1-7-2《“一库一中心三场所”课程开发体系》文件1套
4-1-7-3《“一库一中心三场所”课程开发体系》应用效果报告 ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04010826', '4-1-8 将“四新”融入课程标准，迭代6门专业课；【追加】：企业参与的新开发课程2门；', 3,
     8, 12, 12, '迭代6门专业课，企业参与的新开发课程2门', NULL, '通过与企业人员深入研讨，引入企业资源，形成了《关于公布课程资源更新迭代的通知》《关于公布课程资源更新迭代的结果》，更新了教学标准，引入企业典型生产实践案例，开发2门专业课，升级6门课程内容，预期在课程建设和升级工作中更贴切企业真实生产过程。',
     '4-1-8-1新建课程明细清单及负责人（含企业人员）（PLC技术及应用（三菱）、机电控制基础技能训练）
4-1-8-2《关于公布课程资源更新迭代的通知》
4-1-8-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04010926', '4-1-9 培育新建7门在线精品开放课程，资源更新率超15%/年；【追加】：迭代或新建13门在线精品开放课程；', 3,
     8, 12, 12, '新建20门在线精品开放课程，资源更新率超15%/年', NULL, '通过整理学校新建课程公示文件，形成了《关于在线课程资源更新迭代的通知》、《关于公布课程资源更新迭代的结果》，建设新课程7门、升级已有课程13门，实现了课程资源的结构优化，覆盖了专业群全部课程及新建课程，明确了课程开发的目标和升级目的，预期在课程建设和升级工作中提供指导。',
     '4-1-9-1新建课程明细清单（钳工基本操作技能训练、工业机器人系统集成）
4-1-9-2《关于在线课程资源更新迭代的通知》
4-1-9-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04011026', '4-1-10 申报省级课程1门。', 3,
     8, 12, 12, '申报省级课程1门', '省级及以上', '通过推进省级、国家级课程申报工作（省级4门，国家级2门），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升课程建设层级助力。',
     '4-1-10-1申报材料1份
4-1-10-2教育厅公示文件1份
4-1-10-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04011126', '4-1-11 【新增】在线课程新增人数60000人次；', 3,
     8, 12, 12, '在线课程新增人数60000人次', NULL, '通过整理学校在线课程数据，导出超星/慕课平台后台官方统计数据，形成了在线课程新增人数60000人次的佐证材料，覆盖了专业群全部课程，明确了在线课程使用过程数据监督要求，预期在在线课程建设、使用和国省级申报过程中提供支持。',
     '4-1-11-1在线课程清单列表
4-1-11-2超星/慕课平台后台官方统计数据
4-1-11-3超星/慕课学习人次认证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2026, 'SG04011226', '4-1-12 【新增】申报国家级课程1门。', 3,
     8, 12, 12, '申报国家级课程1门', '国家级', '通过推进省级、国家级课程申报工作（省级4门，国家级2门），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升课程建设层级助力。',
     '4-1-12-1申报材料1份
4-1-12-2教育部公示文件1份
4-1-12-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2027, 'SG04011327', '4-1-13 修订《课程开发中心运行机制》；', 3,
     8, 12, 12, '修订《课程开发中心运行机制》1个', NULL, '通过深入企业调研，明确企业最新需求，形成了调研报告材料1套、出台修订版《工程机械课程开发中心运行机制》，实现了课程开发中心标准化管理规范，覆盖了专业群全部课程，明确了课程开发过程中开发方向，预期在课程开发工作中实现规范管理，为课程开发工作开展提供制度保障。',
     '4-1-13-1调研报告1份（包括问卷调查、照片等）
4-1-13-2调研问卷1套
4-1-13-3《课程开发中心运行机制》修订研讨会资料1套
4-1-13-4《工程机械课程开发中心运行机制（2027年修订）》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2027, 'SG04011427', '4-1-14 将“四新”融入课程标准，迭代6门专业课；【追加】：企业参与的新开发课程2门；', 3,
     8, 12, 12, '迭代6门专业课，企业参与的新开发课程2门', NULL, '通过与企业人员深入研讨，引入企业资源，形成了《关于公布课程资源更新迭代的通知》《关于公布课程资源更新迭代的结果》，更新了教学标准，引入企业典型生产实践案例，开发2门专业课，升级6门课程内容，预期在课程建设和升级工作中更贴切企业真实生产过程。',
     '4-1-14-1新建课程明细清单及负责人（含企业人员）（工程机械部件拆装技能训练、工业机器人焊接技能训练）
4-1-14-2《关于公布课程资源更新迭代的通知》
4-1-14-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2027, 'SG04011527', '4-1-15 培育新建7门在线精品开放课程，资源更新率超15%/年；【追加】：迭代或新建13门在线精品开放课程；', 3,
     8, 12, 12, '新建20门在线精品开放课程，资源更新率超15%/年', NULL, '通过整理学校新建课程公示文件，形成了《关于在线课程资源更新迭代的通知》、《关于公布课程资源更新迭代的结果》，建设新课程7门、升级已有课程13门，实现了课程资源的结构优化，覆盖了专业群全部课程及新建课程，明确了课程开发的目标和升级目的，预期在课程建设和升级工作中提供指导。',
     '4-1-15-1课程明细及证明材料（工业机器人离线编程与仿真、工业机器人系统集成、工业机器人系统运维技能训练、工业机器人现场编程、工业机器人现场编程、工业机器人应用技能训练、工装夹具选型与设计、公差配合与精密测量技能训练等20门）
4-1-15-2《关于在线课程资源更新迭代的通知》
4-1-15-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2027, 'SG04011627', '4-1-16 依据典型生产实践项目，绘制“3基4核6能”进阶式的能力图谱；', 3,
     8, 13, 13, '能力图谱1个', NULL, '通过对企业岗位分析、典型生产项目一线人员及技术骨干的反馈记录，形成了 “3 基 4 核 6 能” 进阶式能力图谱及配套编制说明文档、基于图谱的分层生产实践配套任务包或培训方案，实现了学生的技能、素养、适岗能力可视化展示，覆盖了学生的培养质量与企业标准，预期提升其岗位适配性。',
     '4-1-16-1 “3 基 4 核 6 能” 进阶式能力图谱及配套编制说明文档
4-1-16-2典型生产项目一线人员及技术骨干的反馈记录
4-1-16-3基于图谱的分层生产实践配套任务包或培训方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2027, 'SG04011727', '4-1-17 申报省级课程2门。', 3,
     8, 12, 12, '申报省级课程2门', '省级及以上', '通过推进省级、国家级课程申报工作（省级4门，国家级2门），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升课程建设层级助力。',
     '4-1-17-1申报材料1份
4-1-17-2教育厅公示文件1份
4-1-17-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2027, 'SG04011827', '4-1-18 【新增】在线课程新增人数60000人次。', 3,
     8, 12, 12, '在线课程新增人数60000人次', NULL, '通过整理学校在线课程数据，导出超星/慕课平台后台官方统计数据，形成了在线课程新增人数60000人次的佐证材料，覆盖了专业群全部课程，明确了在线课程使用过程数据监督要求，预期在在线课程建设、使用和国省级申报过程中提供支持。',
     '4-1-18-1在线课程清单列表
4-1-18-2超星/慕课平台后台官方统计数据
4-1-18-3超星/慕课学习人次认证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2028, 'SG04011928', '4-1-19 修订《课程开发中心管理制度》；', 3,
     8, 12, 12, '修订《课程开发中心管理制度》1个', NULL, '通过深入企业调研，明确企业最新需求，形成了调研报告材料1套、出台修订版《工程机械课程开发中心运行机制》，实现了课程开发中心标准化管理规范，覆盖了专业群全部课程，明确了课程开发过程中开发方向，预期在课程开发工作中实现规范管理，为课程开发工作开展提供制度保障。',
     '4-1-19-1调研报告1份（包括问卷调查、照片等）
4-1-19-2调研问卷1套
4-1-19-3《课程开发中心运行机制》修订研讨会资料1套
4-1-19-4《工程机械课程开发中心运行机制（2028年修订）》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2028, 'SG04012028', '4-1-20 将“四新”融入课程标准，迭代6门专业课；【追加】：企业参与的新开发课程3门；', 3,
     8, 12, 12, '迭代6门专业课，企业参与的新开发课程3门', NULL, '通过与企业人员深入研讨，引入企业资源，形成了《关于公布课程资源更新迭代的通知》《关于公布课程资源更新迭代的结果》，更新了教学标准，引入企业典型生产实践案例，开发2门专业课，升级6门课程内容，预期在课程建设和升级工作中更贴切企业真实生产过程。',
     '4-1-20-1新建课程明细清单及负责人（含企业人员）（机电控制基础技能训练、机械CAD/CAM 应用
、机械产品三维数字化设计）
4-1-20-2《关于公布课程资源更新迭代的通知》
4-1-20-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2028, 'SG04012128', '4-1-21 培育新建6门在线精品开放课程，资源更新率超15%/年；【追加】：迭代或新建14门在线精品开放课程；', 3,
     8, 12, 12, '新建20门在线精品开放课程，资源更新率超15%/年', NULL, '通过整理学校新建课程公示文件，形成了《关于在线课程资源更新迭代的通知》、《关于公布课程资源更新迭代的结果》，建设新课程6门、升级已有课程14门，实现了课程资源的结构优化，覆盖了专业群全部课程及新建课程，明确了课程开发的目标和升级目的，预期在课程建设和升级工作中提供指导。',
     '4-1-21-1课程明细及证明材料（机械产品设计与仿真训练、机械传动装置设计、机械传动装置设计综合训练、机械设计基础机械制图、机械制造基础、机械制造技术等20门）
4-1-21-2《关于在线课程资源更新迭代的通知》
4-1-21-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2028, 'SG04012228', '4-1-22 申报省级课程1门、国家级课程1门。', 3,
     8, 12, 12, '申报省级课程1门、国家级课程1门', '省级及以上', '通过推进省级、国家级课程申报工作（省级4门，国家级2门），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升课程建设层级助力。',
     '4-1-22-1申报材料1份
4-1-22-2教育厅、教育部公示文件1份
4-1-22-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2028, 'SG04012328', '4-1-23 【新增】在线课程新增人数60000人次。', 3,
     8, 12, 12, '在线课程新增人数60000人次', NULL, '通过整理学校在线课程数据，导出超星/慕课平台后台官方统计数据，形成了在线课程新增人数60000人次的佐证材料，覆盖了专业群全部课程，明确了在线课程使用过程数据监督要求，预期在在线课程建设、使用和国省级申报过程中提供支持。',
     '4-1-23-1在线课程清单列表
4-1-23-2超星/慕课平台后台官方统计数据
4-1-23-3超星/慕课学习人次认证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2029, 'SG04012429', '4-1-24 校企联合开发以能力为本位的新形态课程1门；', 3,
     8, 12, 12, '新形态课程1门', NULL, '通过校企召开座谈会研讨，了解企业需求，将企业岗位标准引入，推进新形态课程（智慧课程等），形成了《关于新形态课程建设的立项通知》《关于新形态课程建设的立项公示》，预期把握新形态课程先机培养学生的岗位能力，实现为课程建设多样化。',
     '4-1-24-1校企召开座谈会研讨资料1份
4-1-24-2《关于新形态课程建设的立项通知》
4-1-24-3《关于新形态课程建设的立项公示》
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2029, 'SG04012529', '4-1-25 将“四新”融入课程标准，迭代5门专业课；【追加】：企业参与的新开发课程2门；', 3,
     8, 12, 12, '迭代5门专业课，企业参与的新开发课程2门', NULL, '通过与企业人员深入研讨，引入企业资源，形成了《关于公布课程资源更新迭代的通知》《关于公布课程资源更新迭代的结果》，更新了教学标准，引入企业典型生产实践案例，开发2门专业课，升级5门课程内容，预期在课程建设和升级工作中更贴切企业真实生产过程。',
     '4-1-25-1新建课程明细清单及负责人（含企业人员）（逆向工程与3D打印技能训练、普通机床操作技能训练）
4-1-25-2《关于公布课程资源更新迭代的通知》
4-1-25-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2029, 'SG04012629', '4-1-26 培育新建6门在线精品开放课程，资源更新率超15%/年。【追加】：迭代或新建14门在线精品开放课程；', 3,
     8, 12, 12, '新建20门在线精品开放课程，资源更新率超15%/年', NULL, '通过整理学校新建课程公示文件，形成了《关于在线课程资源更新迭代的通知》、《关于公布课程资源更新迭代的结果》，建设新课程6门、升级已有课程14门，实现了课程资源的结构优化，覆盖了专业群全部课程及新建课程，明确了课程开发的目标和升级目的，预期在课程建设和升级工作中提供指导。',
     '4-1-26-1课程明细及证明材料（钳工基本操作技能训练、数控车加工编程与操作、数控机床故障诊断与维修、数控机床原理、数控铣加工编程与操作、数控系统连接与调试、数字孪生与虚拟调试技能训练、塑料模具设计与制造、液压与气动等20门）
4-1-26-2《关于在线课程资源更新迭代的通知》
4-1-26-3《关于公布课程资源更新迭代的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 20, '0,4,20', 2029, 'SG04012729', '4-1-27 【新增】在线课程新增人数60000人次。', 3,
     8, 12, 12, '在线课程新增人数60000人次', NULL, '通过整理学校在线课程数据，导出超星/慕课平台后台官方统计数据，形成了在线课程新增人数60000人次的佐证材料，覆盖了专业群全部课程，明确了在线课程使用过程数据监督要求，预期在在线课程建设、使用和国省级申报过程中提供支持。',
     '4-1-27-1在线课程清单列表
4-1-27-2超星/慕课平台后台官方统计数据
4-1-27-3超星/慕课学习人次认证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2025, 'SG04020125', '4-2-1 明确校企责权，组建由企业专家、学校教师组成的数智课堂团队；', 3,
     8, 12, 12, '数智课堂团队1个', NULL, '通过召开企业座谈会，明确校企权责，形成了《关于组建工程机械课程开发团队、数智课堂团队的通知》，《关于公布工程机械课程开发团队、数智课堂团队的结果》，课程开发中心开发团队的建立，覆盖了专业群待开发新课程，明确了课程开发过程中校企、成员职责，预期在制度建设、课程开发工作中进行专业指导，为形成课程开发机制奠定基础。',
     '4-2-1-1召开座谈会研讨资料1份
4-2-1-2《关于组建工程机械课程开发团队、数智课堂团队的通知》
4-2-1-3《关于公布工程机械课程开发团队、数智课堂团队的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2025, 'SG04020225', '4-2-2 出台《专业群高效课程认证标准》；', 3,
     8, 12, 12, '《专业群高效课程认证标准》1套', NULL, '通过对企业调研、召开校企座谈会，成立了认证委员会、形成了《机械制造及自动化专业群高效课程认证标准》专业群的课程认证标准覆盖了专业群核心课程，明确了课程认证的要求，预期在在标准指导下将所有核心课程完成认证。',
     '4-2-2-1调研报告一份
4-2-2-2座研讨谈会材料一份
4-2-2-3《关于成立课程认证委员会的通知》
4-2-2-4《机械制造及自动化专业群高效课程认证标准》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2025, 'SG04020325', '4-2-3 认证群内1/3的专业课；', 3,
     8, 12, 12, '认证群内1/3的专业课', NULL, '通过出台《关于机械制造及自动化专业群专业课程高效认证的通知》、《关于公布机械制造及自动化专业群专业课程高效认证的结果》，形成了课程认证实质性进展，覆盖了专业群核心课程的1/3，明确了课程认证的时间点，预期完成1/3核心课的认证。',
     '4-2-3-1《关于机械制造及自动化专业群专业课程高效认证的通知》
4-2-3-2课程认证申请表、评审记录
4-2-3-3出文《关于公布机械制造及自动化专业群专程高效认证的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2025, 'SG04020425', '4-2-4 接入数字校园的课堂教学情况覆盖率70%以上；', 3,
     8, 18, 18, '接入数字校园的课堂教学情况覆盖率70%以上', NULL, '通过调取学校巡课系统后台数据、各教研室召开教研活动，形成了智慧校园（巡课系统）后台教学数据统计报告、教研活动记录，预期实现70%以上覆盖率。',
     '4-2-4-1智慧校园（巡课系统）后台教学数据统计报告及盖章件
4-2-4-2教研室对于“接入数字校园的课堂教学情况”教研活动记录
4-2-4-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2025, 'SG04020525', '4-2-5 教师运用国家智慧教育平台资源教学的平均使用率60%以上。', 3,
     8, 18, 18, '教师运用国家智慧教育平台资源教学的平均使用率60%以上', NULL, '通过安排学院所有老师到该平台注册，形成了教师运用该平台进行课堂教学的截图汇编，覆盖了专业群所有课程，预期实现60%以上使用率的要求。',
     '4-2-5-1教师运用该平台进行课堂教学的截图汇编
4-2-5-2教研室对于“国家智慧教育平台教学情况”教研活动记录
4-2-5-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2026, 'SG04020626', '4-2-6 开发标准化实操场景+真实化企业场景+虚拟化仿真场景的数智课堂；', 3,
     8, 10, 10, '开发数智课堂1批', NULL, '通过院校、企业调研与论证，形成了《数智课堂开发调研报告》《数智课堂开发论证报告》，为数智课堂建设做好基础工作，建设中整理过程材料（论证、招标、中标、合同、发票、施工照片等），预期在高标准完成。',
     '4-2-6-1《数智课堂开发调研报告》
4-2-6-2《数智课堂开发论证报告》
4-2-6-3数智课堂建设过程材料（论证、招标、中标、合同、发票、施工照片等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2026, 'SG04020726', '4-2-7 实施《专业群高效课程认证标准》；', 3,
     8, 12, 12, '实施《专业群高效课程认证标准》', NULL, '通过出台《专业群高效课程认证标准》，形成专业核心课程的认定依据，以高质量发展为目标，对课程进行认定，提升课程质量。',
     '4-2-7-1《专业群高效课程认证标准》文件正式发布通知
4-2-7-2《专业群高效课程认证标准》实施研讨会照片材料1套
4-2-7-3《专业群高效课程认证标准》实施反馈效果文件1套

', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2026, 'SG04020826', '4-2-8 认证群内1/3的专业课；', 3,
     8, 12, 12, '认证群内1/3的专业课', NULL, '通过出台《关于机械制造及自动化专业群专业课程高效认证的通知》、《关于公布机械制造及自动化专业群专业课程高效认证的结果》，形成了课程认证实质性进展，覆盖了专业群核心课程的1/3，明确了课程认证的时间点，预期完成1/3核心课的认证。',
     '4-2-8-1《关于机械制造及自动化专业群专业课程高效认证的通知》
4-2-8-2课程认证申请表、评审记录
4-2-8-3出文《关于公布机械制造及自动化专业群专程高效认证的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2026, 'SG04020926', '4-2-9 接入数字校园的课堂教学情况覆盖率80%以上；', 3,
     8, 18, 18, '接入数字校园的课堂教学情况覆盖率80%以上', NULL, '通过调取学校巡课系统后台数据、各教研室召开教研活动，形成了智慧校园（巡课系统）后台教学数据统计报告、教研活动记录，预期实现70%以上覆盖率。',
     '4-2-9-1智慧校园（巡课系统）后台教学数据统计报告及盖章件
4-2-9-2教研室对于“国家智慧教育平台教学情况”教研活动记录
4-2-9-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2026, 'SG04021026', '4-2-10 教师运用国家智慧教育平台资源教学的平均使用率70%以上。', 3,
     8, 18, 18, '教师运用国家智慧教育平台资源教学的平均使用率70%以上', NULL, '通过安排学院所有老师到该平台注册，形成了教师运用该平台进行课堂教学的截图汇编，覆盖了专业群所有课程，预期实现60%以上使用率的要求。',
     '4-2-10-1教师运用该平台进行课堂教学的截图汇编
4-2-10-2教研室对于“接入数字校园的课堂教学情况”教研活动记录
4-2-10-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2027, 'SG04021127', '4-2-11 数智课堂投入使用，开设“院校+企业+社区”直播互动课；', 3,
     8, 18, 18, '直播互动课1批', NULL, '通过直播互动课堂课表（含场地、教师、学生），形成了数智课堂的投入使用，为直播互动课堂建设做好基础工作，预期更好汇聚各方资源，为学生提供更智能化的课堂表现形式，实现智能化转型。',
     '4-2-11-1直播互动课堂课表（含场地、教师、学生）
4-2-11-2直播后台运行记录及互动痕迹截图
4-2-11-3直播互动课堂期末上交材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2027, 'SG04021227', '4-2-12 探索“项目驱动+小组研讨+探究实践+AI助教”的教学新模式；【追加】：推广产教融合数智赋能的课程教学模式；', 3,
     8, 10, 10, '探索教学新模式1个，推广产教融合数智赋能的课程教学模式证明材料1个', NULL, '通过召开教学模式的研讨会、深入论证，形成“项目驱动+小组研讨+探究实践+AI助教”教学新模式征求意见稿。通过课程教学模式座谈交流会，形成课程教学模式的推广、采纳，预期为省级和国家级教学成果奖申报打好基础。',
     '4-2-12-1召开教学模式的研讨会
4-2-12-2探索教学模式的实施方案及论证
4-2-12-3“项目驱动+小组研讨+探究实践+AI助教”教学新模式征求意见稿
4-2-12-4课程教学模式推广、采纳证明若干
4-2-12-5课程教学模式座谈交流会
4-2-12-6课程教学模式应用反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2027, 'SG04021327', '4-2-13 认证群内1/3的专业课；', 3,
     8, 12, 12, '认证群内1/3的专业课', NULL, '通过出台《关于机械制造及自动化专业群专业课程高效认证的通知》、《关于公布机械制造及自动化专业群专业课程高效认证的结果》，形成了课程认证实质性进展，覆盖了专业群核心课程的1/3，明确了课程认证的时间点，预期完成1/3核心课的认证。',
     '4-2-13-1《关于机械制造及自动化专业群专业课程高效认证的通知》
4-2-13-2课程认证申请表、评审记录
4-2-13-3出文《关于公布机械制造及自动化专业群专程高效认证的结果》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2027, 'SG04021427', '4-2-14 接入数字校园的课堂教学情况覆盖率90%以上；', 3,
     8, 18, 18, '接入数字校园的课堂教学情况覆盖率90%以上', NULL, '通过调取学校巡课系统后台数据、各教研室召开教研活动，形成了智慧校园（巡课系统）后台教学数据统计报告、教研活动记录，预期实现70%以上覆盖率。',
     '4-2-14-1智慧校园（巡课系统）后台教学数据统计报告及盖章件
4-2-14-2教研室对于“国家智慧教育平台教学情况”教研活动记录
4-2-14-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2027, 'SG04021527', '4-2-15 教师运用国家智慧教育平台资源教学的平均使用率80%以上。', 3,
     8, 18, 18, '教师运用国家智慧教育平台资源教学的平均使用率80%以上', NULL, '通过安排学院所有老师到该平台注册，形成了教师运用该平台进行课堂教学的截图汇编，覆盖了专业群所有课程，预期实现60%以上使用率的要求。',
     '4-2-15-1教师运用该平台进行课堂教学的截图汇编
4-2-15-2教研室对于“接入数字校园的课堂教学情况”教研活动记录
4-2-15-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2028, 'SG04021628', '4-2-16 凝练“项目驱动+小组研讨+探究实践+AI助教”的教学新模式；', 3,
     8, 10, 10, '凝练教学新模式1个', NULL, '通过教学新模式多轮迭代的版本对比文件、专家论证意见及修改记录，形成了教学新模式，并申请校级教学成果奖，为校级、省级和国家级教学成果奖申报打好基础。',
     '4-2-16-1教学新模式多轮迭代的版本对比文件
4-2-16-2新模式凝练的专家论证意见及修改记录
4-2-16-3形成终版教学新模式1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2028, 'SG04021728', '4-2-17 开发AI专业知识库问答应用；', 3,
     8, 18, 18, '开发AI专业知识库问答应用1个', NULL, '通过召开问答库（智能体）建设研讨会、问答库（智能体）建设论证，明确了建设的目的和意义，形成AI问答应用的建设方案，预期让学生可随时随地个性化学习、答疑解惑，突破课后教师答疑的困境，为教学能力比赛等竞赛提供AI加持。',
     '4-2-17-1AI问答库（智能体）建设研讨会
4-2-17-2AI问答库（智能体）建设论证报告
4-2-17-3AI问答库（智能体）建设过程材料（立项、招标、中标、合同、发票等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2028, 'SG04021828', '4-2-18 接入数字校园的课堂教学情况覆盖率95%以上；', 3,
     8, 18, 18, '接入数字校园的课堂教学情况覆盖率95%以上', NULL, '通过调取学校巡课系统后台数据、各教研室召开教研活动，形成了智慧校园（巡课系统）后台教学数据统计报告、教研活动记录，预期实现70%以上覆盖率。',
     '4-2-18-1智慧校园（巡课系统）后台教学数据统计报告及盖章件
4-2-18-2教研室对于“国家智慧教育平台教学情况”教研活动记录
4-2-18-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2028, 'SG04021928', '4-2-19 教师运用国家智慧教育平台资源教学的平均使用率90%以上。', 3,
     8, 18, 18, '教师运用国家智慧教育平台资源教学的平均使用率90%以上', NULL, '通过安排学院所有老师到该平台注册，形成了教师运用该平台进行课堂教学的截图汇编，覆盖了专业群所有课程，预期实现60%以上使用率的要求。',
     '4-2-19-1教师运用该平台进行课堂教学的截图汇编
4-2-19-2教研室对于“接入数字校园的课堂教学情况”教研活动记录
4-2-19-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2029, 'SG04022029', '4-2-20 推广“项目驱动+小组研讨+探究实践+AI助教”的教学新模式；', 3,
     8, 10, 10, '推广教学新模式证明材料1个', NULL, '通过召开专业群座谈交流会、校内专业群应用反馈报告、推广、采纳证明等，实现教学新模式的推广，搜集推广、采纳证明材料1套，并申请校级教学成果奖，为校级、省级和国家级教学成果奖申报打好基础。',
     '4-2-20-1“项目驱动+小组研讨+探究实践+AI助教”教学新模式一个
4-2-20-2推广、采纳证明若干
4-2-20-3专业群座谈交流会
4-2-20-4校内专业群应用反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2029, 'SG04022129', '4-2-21 AI专业知识库问答应用推广使用；', 3,
     10, 37, 37, '推广AI专业知识库问答应用1个', NULL, '通过召开AI问答库座谈交流会、推广使用反馈报告，实现AI问答库的推广，通过开发AI问答应用，预期让学生可随时随地个性化学习、答疑解惑，突破课后教师答疑的困境，为教学能力比赛等竞赛提供AI加持。',
     '4-2-21-1AI专业知识问答库推广交流会
4-2-21-2推广、采纳证明若干
4-2-21-3推广使用反馈报告1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2029, 'SG04022229', '4-2-22 接入数字校园的课堂教学情况覆盖率100%以上；', 3,
     8, 18, 18, '接入数字校园的课堂教学情况覆盖率100%以上', NULL, '通过调取学校巡课系统后台数据、各教研室召开教研活动，形成了智慧校园（巡课系统）后台教学数据统计报告、教研活动记录，预期实现70%以上覆盖率。',
     '4-2-22-1智慧校园（巡课系统）后台教学数据统计报告及盖章件
4-2-22-2教研室对于“国家智慧教育平台教学情况”教研活动记录
4-2-22-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 21, '0,4,21', 2029, 'SG04022329', '4-2-23 教师运用国家智慧教育平台资源教学的平均使用率90%以上。', 3,
     8, 18, 18, '教师运用国家智慧教育平台资源教学的平均使用率90%以上', NULL, '通过安排学院所有老师到该平台注册，形成了教师运用该平台进行课堂教学的截图汇编，覆盖了专业群所有课程，预期实现60%以上使用率的要求。',
     '4-2-23-1教师运用该平台进行课堂教学的截图汇编
4-2-23-2教研室对于“接入数字校园的课堂教学情况”教研活动记录
4-2-23-3教师运用平台资源教学的反馈材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2025, 'SG04030125', '4-3-1 组建团队，论证专业群信息化教学平台建设的可实施性与必要性；', 3,
     8, 18, 18, '信息化教学平台建设论证报告1个', NULL, '通过组建团队开展论证，覆盖了专业群老师、学生、专家的意见，形成了《机械制造及自动化专业群信息化教学平台建设论证报告》，明确了信息化教学平台建设的可实施性与必要性，预期为建设信息化教学平台建设方案提供指导。',
     '4-3-1-1建设团队名单及职责
4-3-1-2论证研讨会材料1份
4-3-1-3《机械制造及自动化专业群信息化教学平台建设论证报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2025, 'SG04030225', '4-3-2 制定专业群信息化教学平台建设方案。', 3,
     8, 18, 18, '专业群信息化教学平台建设方案1套', NULL, '通过校企充分调研，形成调研总结报告1份、调研问卷1套、《机械制造及自动化专业群信息化教学平台建设方案》，明确了具体的建设思路、方案和要求，预期为专业群信息化教学平台的建设提供指导。',
     '4-3-2-1调研总结报告1份
4-3-2-2调研问卷1套
4-3-2-3《机械制造及自动化专业群信息化教学平台建设方案》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2026, 'SG04030326', '4-3-3 创新数据采集方式，分析数据，搭建专业群信息化教学平台；', 3,
     8, 18, 18, '搭建专业群信息化教学平台1个', NULL, '通过专业群信息化教学平台建设，形成了信息化教学平台开发招标公告、信息化教学平台开发中标材料（公告、标书、信息化教学平台建设合同、信息化教学平台建设发票、财务转账证明等过程材料，预期依托此平台开展信息化教学、个性化评价，为智能化课堂建设做好准备。',
     '4-3-3-1信息化教学平台开发招标公告
4-3-3-2信息化教学平台开发中标材料（公告、标书）
4-3-3-3信息化教学平台建设合同
4-3-3-4信息化教学平台建设发票、财务转账证明等过程材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2026, 'SG04030426', '4-3-4 构建“全程多元、六维考核、过程增值”增值性评价体系；', 3,
     8, 11, 11, '构建增值性评价体系1个', NULL, '通过召开研讨会，形成增值评价体系具体内涵1套，实现课上教学的全方位数据采集、AI个性化评价、精准评价，绘制增值评价曲线，让老师、学生实时掌握学生增值情况。',
     '4-3-4-1召开研讨会材料1份
4-3-4-2形成增值评价体系具体内涵1套
4-3-4-3该评价模式实际应用前后对比报告1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2026, 'SG04030526', '4-3-5 申报立项省级教改项目2项。', 3,
     11, 22, 22, '省级教改项目2项', '省级及以上', '通过推进省级教改课题申报工作（省级教改课题2个），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升教改课题质量服务。',
     '4-3-5-1申报材料2份
4-3-5-2教育厅公示文件1份
4-3-5-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2027, 'SG04030627', '4-3-6 建成具备“分析、增值、画像”功能的智能化评价系统平台1个；', 3,
     8, 18, 18, '构建智能化评价系统平台1个', NULL, '通过智能化评价系统平台建设，形成了智能化评价系统平台开发招标公告、智能化评价系统平台开发中标材料（公告、标书、合同、发票、财务转账证明等过程材料，预期依托此平台开展信息化教学、个性化评价，为智能化课堂建设做好准备。',
     '4-3-6-1智能化评价系统平台开发招标公告
4-3-6-2智能化评价系统平台开发中标材料（公告、标书）
4-3-6-3智能化评价系统平台建设合同
4-3-6-4智能化评价系统平台建设发票、财务转账证明等过程材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2027, 'SG04030727', '4-3-7 完成省级教改项目2项；', 3,
     11, 22, 22, '省级教改项目2项', '省级及以上', '通过推进省级教改课题申报工作（省级教改课题2个），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升教改课题质量服务。',
     '4-3-7-1结项材料2份
4-3-7-2研究成果1套
4-3-7-3经验分享座谈会材料1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2027, 'SG04030827', '4-3-8 申报立项省级及以上课堂革命案例1个。', 3,
     8, 14, 14, '课堂革命案例1个', '省级及以上', '通过推进省级课堂革命案例申报工作（省级课堂革命案例1个），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升省级课堂革命案例质量服务。',
     '4-3-8-1申报材料1份
4-3-8-2教育厅公示文件1份
4-3-8-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2028, 'SG04030928', '4-3-9 申报立项教育科学研究项目1-2项；', 3,
     11, 22, 22, '教育科学研究项目1-2项', NULL, '通过推进教育科学研究项目申报工作，完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升教育科学研究项目质量服务。',
     '4-3-9-1申报材料1-2份
4-3-9-2教育厅公示文件1份
4-3-9-3新闻报道1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2028, 'SG04031028', '4-3-10 完成省级及以上课堂革命案例1个。', 3,
     8, 14, 14, '课堂革命案例1个', '省级及以上', '通过推进省级课堂革命案例申报工作（省级课堂革命案例1个），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升省级课堂革命案例质量服务。',
     '4-3-10-1结项材料1份
4-3-10-2研究成果1套
4-3-10-3经验分享座谈会材料1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 22, '0,4,22', 2029, 'SG04031129', '4-3-11 完成教育科学研究项目1-2项。', 3,
     11, 22, 22, '教育科学研究项目1-2项', '省级及以上', '通过推进教育科学研究项目申报工作，完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升教育科学研究项目质量服务。',
     '4-3-11-1结项材料1份
4-3-11-2研究成果1套
4-3-11-3经验分享座谈会材料1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2025, 'SG05010125', '5-1-1明确校企责权，组建由“政企校”组成的多形态教材开发小组', 3,
     8, 12, 12, '成立多形态教材开发小组', NULL, '通过组建由“政企校”组成的多形态教材开发小组，形成了“编审”选用的教程全过程管理制度，明确了教材“凡编必审、凡选必审”的建设要求，保障教材无意识形态问题。',
     '5-1-1-1多形态教材开发小组名单
5-1-1-2多形态教材开发管理文件1套
5-1-1-3 多形态教材开发实施情况总结（新闻报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2025, 'SG05010225', '5-1-2 充分调研论证，形成多形态教材开发机制，明确课程需求，配置资源', 3,
     8, 12, 12, '出台多形态教材开发的配套文件1套', NULL, '通过充分调研论证，形成了多形态教材开发机制，覆盖了课程资源配置全流程，明确了课程核心需求与资源匹配标准，预期在教学实施阶段实现教材与课程的深度适配，为提升教学质量和人才培养效果奠定扎实基础。',
     '5-1-2-1调研总结报告1份
5-1-2-2多形态教材开发配套文件1套
5-1-2-3《多形态教材开发实施方案》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2025, 'SG05010325', '5-1-3 引入企业操作手册、培训手册、培训包等，转换为教学案例', 3,
     8, 12, 12, '撰写教学案例1个', NULL, '通过引入企业操作手册、培训手册、培训包等实战资料并系统转化为教学案例，形成了企业真实场景与教学内容深度融合的案例库建设路径，预期在课程教学实施过程中实现学生对企业实际业务的认知提升与实践能力培养。',
     '5-1-3-1教学案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2025, 'SG05010425', '5-1-4 引入企业真实案例和项目的活页式、工作手册式等多形态教材3部；【追加】：引入企业真实案例和项目的活页式、工作手册式等多形态教材5部', 3,
     8, 12, 12, '编写形态教材8部', NULL, '通过引入含企业真实案例项目的活页式、工作手册式多形态教材共 8 部，形成适配企业需求的资源体系与矩阵，明确应用标准、更新机制，预期实现校企场景对接与业务覆盖，提升学生实践能力。',
     '5-1-4-1多形态教材8部，学校立项通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2025, 'SG05010525', '5-1-5 申报国家级教材1部', 3,
     8, 12, 12, '申报国家级教材1部', '国家级', '通过推进国家级教材申报工作（共 1 部），形成专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升教材建设层级助力。',
     '5-1-5-1国家级教材1部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2025, 'SG05010625', '5-1-6 【新增】制定专业教材建设委员会工作机制', 3,
     8, 12, 12, '制定专业教材建设委员会管理文件', NULL, '通过制定专业教材建设委员会工作机制，形成规范运行体系，覆盖教材规划、审核等全环节，明确职责分工与议事规则，预期在教材建设中提质增效，为打造优质教材提供制度保障。',
     '5-1-6-1专业教材建设委员会名单
5-1-6-2专业教材建设委员会管理文件1套
5-1-6-3专业教材建设委员会实施情况总结（新闻报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2026, 'SG05010726', '5-1-7 制定教材开发管理制度；【追加】：形成教材管理系列文件1套', 3,
     8, 12, 12, '制定教材开发管理文件1套', NULL, '通过制定教材开发管理制度、形成教材管理系列文件 1 套，形成规范管理框架，覆盖教材开发全流程，明确权责与标准，预期在教材建设中规范流程，为提升教材质量、保障开发有序提供支撑。',
     '5-1-7-1教材开发管理调研报告1份
5-1-7-2制定教材开发管理制度形成教材开发管理文件1套
5-1-7-3《教材开发管理制度实施方案》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2026, 'SG05010826', '5-1-8 引入企业技术标准、操作手册、培训包转换为教学案例', 3,
     8, 12, 12, '撰写教学案例1个', NULL, '通过引入企业技术标准、操作手册、培训包等实战资料并系统转化为教学案例，形成了企业真实场景与教学内容深度融合的案例库建设路径，预期在课程教学实施过程中实现学生对企业实际业务的认知提升与实践能力培养。',
     '5-1-8-1教学案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2026, 'SG05010926', '5-1-9 引入企业真实案例和项目的活页式、工作手册式等多形态教材3部；【追加】：引入企业真实案例和项目的活页式、工作手册式等多形态教材9部', 3,
     8, 12, 12, '编写形态教材12部', NULL, '通过引入含企业真实案例项目的活页式、工作手册式多形态教材共 8 部，形成适配企业需求的资源体系与矩阵，明确应用标准、更新机制，预期实现校企场景对接与业务覆盖，提升学生实践能力。',
     '5-1-9-1多形态教材12部，学校立项通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2026, 'SG05011026', '5-1-10 开发“中高本”一体化教材1部；申报省级教材2部。', 3,
     8, 12, 12, '开发“中高本”一体化教材1部；申报省级教材2部', NULL, '通过开发 “中高本” 一体化教材 1 部、申报省级教材 2 部，形成层级化教材建设体系，覆盖教材研发、申报全流程，明确建设标准与方向，预期在年内完成目标，为贯通人才培养提供教材支撑。',
     '5-1-10-1“中高本”一体化教材1部
5-1-10-2申报省级教材2部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2027, 'SG05011127', '5-1-11 引入企业技术标准、操作手册、培训包转换为教学案例', 3,
     8, 12, 12, '撰写教学案例1个', NULL, '通过引入企业技术标准、操作手册、培训包等实战资料并系统转化为教学案例，形成了企业真实场景与教学内容深度融合的案例库建设路径，预期在课程教学实施过程中实现学生对企业实际业务的认知提升与实践能力培养。',
     '5-1-11-1教学案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2027, 'SG05011227', '5-1-12 引入企业真实案例和项目的活页式、工作手册式等多形态教材4部；【追加】：引入企业真实案例和项目的活页式、工作手册式等多形态教材8部', 3,
     8, 12, 12, '编写形态教材12部', NULL, '通过引入含企业真实案例项目的活页式、工作手册式多形态教材共 12部，形成适配企业需求的资源体系与矩阵，明确应用标准、更新机制，预期实现校企场景对接与业务覆盖，提升学生实践能力。',
     '5-1-12-1多形态教材12部，学校立项通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2027, 'SG05011327', '5-1-13 开发“中高本”一体化教材2部；申报省级教材2部、国家级教材1部', 3,
     8, 12, 12, '开发“中高本”一体化教材2部；申报省级教材2部、国家级教材1部', '省级及以上', '通过开发 “中高本” 一体化教材 2 部、申报省级教材 2 部、国家级教材1部，形成层级化教材建设体系，覆盖教材研发、申报全流程，明确建设标准与方向，预期在年内完成目标，为贯通人才培养提供教材支撑。',
     '5-1-13-1“中高本”一体化教材2部
5-1-13-2申报省级教材2部
5-1-13-3申报国家级教材1部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2027, 'SG05011427', '5-1-14 申报国家级教材建设奖1项。【追加】：由此年度修改为2029年', 3,
     8, 12, 12, '申报国家级教材建设奖1项', '国家级', '通过推进国家级教材建设奖申报工作（共 1 项），形成专项申报推进体系，覆盖材料筹备、质量打磨、审核报送全流程，明确申报重点与优化方向，预期在评审中斩获奖项，为提升教材建设影响力提供助力。',
     '5-1-14-1国家级教材建设奖1项', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2027, 'SG05011527', '5-1-15 【新增】优化专业教材建设委员会工作机制', 3,
     8, 12, 12, '修订专业教材建设委员会管理文件', NULL, '通过修订专业教材建设委员会工作机制，完善规范运行体系，覆盖教材规划、审核等全环节，明确职责分工与议事规则，预期在教材建设中提质增效，为打造优质教材提供制度保障。',
     '5-1-15-1专业教材建设实施情况反馈报告
5-1-15-2修订专业教材建设委员会文件1套
5-1-15-3专业教材建设监督评价实施办法', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2028, 'SG05011628', '5-1-16 引入企业技术标准、操作手册、培训包转换为教学案例', 3,
     8, 12, 12, '撰写教学案例1个', NULL, '通过引入企业技术标准、操作手册、培训包等实战资料并系统转化为教学案例，形成了企业真实场景与教学内容深度融合的案例库建设路径，预期在课程教学实施过程中实现学生对企业实际业务的认知提升与实践能力培养。',
     '5-1-16-1教学案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2028, 'SG05011728', '5-1-17 引入企业真实案例和项目的活页式、工作手册式等多形态教材4部；【追加】：引入企业真实案例和项目的活页式、工作手册式等多形态教材8部', 3,
     8, 12, 12, '编写形态教材12部', NULL, '通过引入含企业真实案例项目的活页式、工作手册式多形态教材共 12 部，形成适配企业需求的资源体系与矩阵，明确应用标准、更新机制，预期实现校企场景对接与业务覆盖，提升学生实践能力。',
     '5-1-17-1多形态教材12部，学校立项通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2028, 'SG05011828', '5-1-18 申报省级教材1部、国家级教材1部。', 3,
     8, 12, 12, '申报省级教材1部、国家级教材1部。', '省级及以上', '通过推进省级、国家级教材申报工作（共 2 部，省级国家级各1部），完善专项申报方案，覆盖全流程，明确重点方向，预期在评审中获批，为提升教材建设层级助力。',
     '5-1-18-1申报省级教材1部
5-1-18-2申报国家级教材1部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2029, 'SG05011929', '5-1-19 引入企业技术标准、操作手册、培训包转换为教学案例；', 3,
     8, 12, 12, '撰写教学案例1个', NULL, '通过引入企业技术标准、操作手册、培训包等实战资料并系统转化为教学案例，形成了企业真实场景与教学内容深度融合的案例库建设路径，预期在课程教学实施过程中实现学生对企业实际业务的认知提升与实践能力培养。',
     '5-1-19-1教学案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 23, '0,5,23', 2029, 'SG05012029', '5-1-20 引入企业真实案例和项目的活页式、工作手册式等多形态教材2部。【追加】：引入企业真实案例和项目的活页式、工作手册式等多形态教材4部。', 3,
     8, 12, 12, '编写多形态教材6部。', NULL, '通过引入含企业真实案例项目的活页式、工作手册式多形态教材共 6部，形成适配企业需求的资源体系与矩阵，明确应用标准、更新机制，预期实现校企场景对接与业务覆盖，提升学生实践能力。',
     '5-1-20-1多形态教材6部，学校立项通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2025, 'SG05020125', '5-2-1 明确校企责权，组建由“政企校”组成的数字化教材开发小组；', 3,
     8, 12, 12, '成立数字化教材开发小组', NULL, '通过组建由“政企校”组成的数字化教材开发小组，形成一套经过教学实践验证的数字化教材开发标准、技术范式与协同创作模式，覆盖了所有的双高专业涉及的行业技术前沿、企业岗位标准到学校教学实践的全链条资源，明确了政府的主导、企业的主体与供给角色以及学校的主责与转化角色，预期未来两年内实现该开发模式在全校所有专业示范推广与持续优，为高质量的数字教材开发奠定基础。',
     '5-2-1-1 教材开发小组名单
5-2-1-2 数字化教材开发管理文件1套
5-2-1-3 数字化教材开发实施情况总结汇报会（新闻报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2025, 'SG05020225', '5-2-2 充分调研论证，形成《数字化教材开发机制》，明确课程需求，配置资源；【追加】：整合工程机械数字化教学资源的交互式数字化教材2部；', 3,
     8, 12, 12, '撰写调研报告1份；编写数字化教材2部', NULL, '通过系统性的调研论证与机制建设，形成了一套科学、规范、可操作的《数字化教材开发机制》。该机制覆盖了从行业需求分析、岗位能力解构到教学化转化的完整流程，明确了课程设置与内容更新的需求来源、资源调配的优先级与具体方案。预期在机制正式运行后，实现了所有新立项数字化教材开发工作按流程实施，为打造高水平、高适应性的数字化教学资源体系奠定了坚实的制度基础。',
     '5-2-2-1 数字化教材开发调研报告1份
5-2-2-2 数字化教材开发配套文件1套
5-2-2-3 工程机械数字化交互式数字化教材2部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2025, 'SG05020325', '5-2-3 把现有国家规划教材《机械制图》升级为数字化教材。', 3,
     8, 12, 12, '编写《机械制图》国家级数字教材', '国家级', '通过升级《机械制图》升级为数字化教材，形成AI+数字化教学的新格局，该教学资源可覆盖全体机械和自动化专业的全体师生，明确建设要求，预期完成数字化教材建设的验收，为实现教学资源的AI+数字化教学奠定基础。',
     '5-2-3-1 关于编写《机械制图》数字化教材研讨资料1份
5-2-3-2 《机械制图》数字化教材校级立项文件
5-2-3-3  国家规划教材《机械制图》升级为数字化教材立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2026, 'SG05020426', '5-2-4 制定《数字化教材开发管理制度》；
', 3,
     8, 12, 12, '制定《数字化教材开发管理制度》', NULL, '通过制定数字化教材开发管理制度、形成数字化教材管理系列文件 1 套，形成规范管理框架，覆盖教材开发全流程，明确权责与标准，预期在教材建设数字化中规范流程，为提升教材质量、保障开发有序提供支撑。',
     '5-2-4-1 数字化教材开发管理制度调研报告1份
5-2-4-2《数字化教材开发管理制度》文件1套
5-2-4-3 《数字化教材开发管理制度》实施情况总结1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2026, 'SG05020526', '5-2-5 整合工程机械数字化教学资源的交互式数字化教材2部。【追加】：整合工程机械数字化教学资源的交互式数字化教材4部。', 3,
     8, 12, 12, '编写数字教材6部', NULL, '通过整合工程机械数字化教学资源的交互式数字化教材，形成适配工程机械需求的资源体系与矩阵，明确建设要求，预期实现具有工程机械背景的数字化教材6部，提升学生实践能力。',
     '5-2-5-1 关于编写开发工程机械数字化教材6部研讨资料1份
5-2-5-2 开发工程机械数字化教材6部，学校立项通知
5-2-5-3 出版工程机械数字化教材6部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2027, 'SG05020627', '5-2-6 修订《数字化教材开发机制》、《数字化教材开发管理制度》；', 3,
     8, 12, 12, ' 修订《数字化教材开发机制》、《数字化教材开发管理制度》', NULL, '通过对《数字化教材开发机制》与《数字化教材开发管理制度》的系统性修订，形成了一套权责更清晰、流程优化高效的开发管理与制度保障体系。该体系覆盖了从项目立项、团队组建、内容创作、质量审核到知识产权归属、成果应用等环节，明确了各方在每一个环节的“责、权、利”。预期在数字化教材开发上实现规范管理，从制度层面保障了数字化教材的开发质量。',
     '5-2-6-1 《数字化教材开发机制》、《数字化教材开发管理制度》调研分析报告1份
5-2-6-2  修订《数字化教材开发机制》相关文件1套
5-2-6-3 修订《数字化教材开发管理制度》文件1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2027, 'SG05020727', '5-2-7 把现有省级教材《工业机器人现场编程》升级为数字化教材；', 3,
     8, 12, 12, '编写《工业机器人现场编程》数字教材', NULL, '通过升级《工业机器人现场编程》升级为数字化教材，形成AI+数字化教学的新格局，该教学资源可覆盖全自动化和工业机器人等专业的全体师生，明确建设要求，预期完成数字化教材建设的验收，为实现教学资源的AI+数字化教学奠定基础。',
     '5-2-7-1 关于编写《工业机器人现场编程》数字化教材研讨资料1份
5-2-7-2《机械制图》数字化教材校级立项文件
5-2-7-3《工业机器人现场编程》省级数字化教材立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2027, 'SG05020827', '5-2-8 整合工程机械数字化教学资源的交互式数字化教材2部。【追加】：整合工程机械数字化教学资源的交互式数字化教材4部。', 3,
     8, 12, 12, '编写数字教材6部', NULL, '通过整合工程机械数字化教学资源的交互式数字化教材，形成适配工程机械需求的资源体系与矩阵，明确建设要求，预期实现具有工程机械背景的数字化教材6部，提升学生实践能力。',
     '5-2-8-1  关于编写开发工程机械数字化教工程机械数字化教学资源的交互式数字化教材研讨资料1份
5-2-8-2 开发工程机械数字化教工程机械数字化教学资源的交互式数字化教材6部，学校立项通知
5-2-8-3 出版工程机械数字化教学资源的交互式数字化教材6部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2028, 'SG05020928', '5-2-9 将国家级在线课程《组态项目开发与实践》配套数字化教材；', 3,
     8, 12, 12, '编写《组态项目开发与实践》数字教材', '国家级', '通过升级《组态项目开发与实践》升级为数字化教材，形成AI+数字化教学的新格局，该教学资源可覆盖全自动化和工业机器人等专业的全体师生，明确建设要求，预期完成数字化教材建设的验收，为实现教学资源的AI+数字化教学奠定基础。',
     '5-2-9-1 关于编写《组态项目开发与实践》数字化教材研讨资料1份
5-2-9-2 《组态项目开发与实践》数字化教材校级立项文件
5-2-9-3《组态项目开发与实践》配套数字化教材国家级立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2028, 'SG05021028', '5-2-10 整合工程机械数字化教学资源的交互式数字化教材1部。【追加】：整合工程机械数字化教学资源的交互式数字化教材4部。', 3,
     8, 12, 12, '编写数字教材5部', NULL, '通过整合工程机械数字化教学资源的交互式数字化教材，形成适配工程机械需求的资源体系与矩阵，明确建设要求，预期实现具有工程机械背景的数字化教材5部，提升学生实践能力。',
     '5-2-10-1 关于编写程机械数字化教学资源的交互式数字化教材研讨资料1份
5-2-10-1 工程机械数字化教学资源的交互式数字化教材5部，校级立项文件
5-2-10-1 数字化教材5部，学校立项通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2029, 'SG05021129', '5-2-11 将数字化教材推广到市域联合体学校应用，成效显著。', 3,
     8, 12, 12, '数字化教材推广证明1份', NULL, '通过将开发的数字化教材在市域联合体学校中进行系统性推广与应用，形成了一套可复制、可推广的“优质教学资源共建共享”新模式。该模式覆盖了联合体内全部成员院校的相关专业，明确了资源共建、师资共训、成果共享的协同机制与各方权责。预期显著扩大了优质教学资源的辐射面与受益面，有力提升了区域职业教育的整体教学质量与数字化水平',
     '5-2-11-1 数字化教材推广证明1份
5-2-11-2 数字化教材推广照片和报道等资料1套
5-2-11-3 数字化教材推广成效分析报告1份', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 24, '0,5,24', 2029, 'SG05021229', '5-2-12 【新增】整合工程机械数字化教学资源的交互式数字化教材2部。', 3,
     8, 12, 12, '编写数字教材2部', NULL, '通过整合工程机械数字化教学资源的交互式数字化教材，形成适配工程机械需求的资源体系与矩阵，明确建设要求，预期实现具有工程机械背景的数字化教材2部，提升学生实践能力。',
     '5-2-12-1 关于编写开发工程机械数字化教工程机械数字化教学资源的交互式数字化教材研讨资料1份
5-2-12-2 开发工程机械数字化教工程机械数字化教学资源的交互式数字化教材2部，学校立项通知
5-2-12-3 出版工程机械数字化教学资源的交互式数字化教材2部', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2025, 'SG05030125', '5-3-1 明确校企责权，组建由“校企行”多方参与的教材建设开发团队；', 3,
     8, 12, 12, '成立“校企行”多方参与的教材建设开发团队1个', NULL, '通过整合学校理论教学资源、企业实践资源与行业政策资源，将打破教材内容与行业实际脱节的壁垒，覆盖学校相关专业的核心课程教材需求，明确学校、企业、行业协会在教材建设各环节的工作边界与协作方式，预期在教材质量提升、人才培养质量优化、各方协同发展等方面取得显著成效，实现学校人才培养质量的提升，为推动产教深度融合、促进教育链、人才链与产业链、创新链的有效衔接奠定坚实基础，助力行业高质量发展与区域经济转型升级。',
     '5-3-1-1“校企行” 教材建设合作框架协议一份
5-3-1-2校企责权细分说明书一份
5-3-1-3团队组建与运作管理类材料1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2025, 'SG05030225', '5-3-2 充分调研论证，形成专业群教材建设开发建设方案与开发机制；', 3,
     8, 12, 12, '专业群教材建设开发建设方案与开发机制文件1套', NULL, '通过构建 “共享 - 特色 - 融合” 的立体化教材体系与 “校企行” 协同开发机制，覆盖专业群所有核心课程与选修课程，明确教材与岗位需求、行业技术、跨专业融合的对接标准，预期将显著提升教材的实用性与适用性，实现专业群人才培养质量的全面提升，为推动职业教育高质量发展、促进教育链与产业链深度融合奠定坚实基础。',
     '5-3-2-1 调研报告一份
5-3-2-2 专业群教材建设开发建设方案
5-3-2-3 专业群教材建设开发机制文件1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2025, 'SG05030325', '5-3-3 出台教材建设实施细则；', 3,
     8, 12, 12, '出台教材建设实施细则1份', NULL, '通过明确教材开发全流程（立项、编写、审核、试用）的操作规范与时间节点，覆盖专业群教材建设从启动到落地的每一个关键环节，明确各方在经费管理、资料提供、审核把关、监督考核等方面的具体职责，预期将显著提升教材建设质量的可控性与稳定性，实现专业群教材建设从 “有方案” 到 “能落地、有保障、高质量” 的跨越，将实现专业群教材建设从 “有方案” 到 “能落地、有保障、高质量” 的跨越，为推动职业教育教材标准化、高质量发展奠定坚实基础。',
     '5-3-3-1《省级教材建设实施细则（正式版）》1 套
5-3-3-2 省级教材立项申报指南（分年度）1 组
5-3-3-3 教材编写格式标准及模板 1 套
5-3-3-4 省级教材专家库组建与管理办法 1 组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2025, 'SG05030425', '5-3-4 出台教材选用管理办法。', 3,
     8, 10, 10, '出台教材选用管理办法1份', NULL, '通过建立 “校企行” 三方协同评估机制与明确的教材选用标准，覆盖专业群所有课程（包括基础课程、专业核心课程、选修课程）的教材选用工作，明确教材选用的范围、标准、流程及监督要求，预期将显著提升选用教材的整体质量，实现专业群教材选用从 “被动选书” 到 “主动选优” 的转变，为推动职业教育教材质量提升、人才培养质量优化奠定坚实基础。',
     '5-3-4-1 学校教材选用工作组组建文件 1 组5-3-4-2《教材选用需求清单》《备选教材试用评估报告》1 套
5-3-4-3 选用结果公示文件及备案表 1 组
5-3-4-4 每 3 年教材复核评估报告 1 套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2025, 'SG05030525', '5-3-5 【新增】教材建设经验辐射影响100人；对外开展教材培训1场。', 3,
     8, 12, 12, '教材建设经验辐射影响证明1份；对外开展教材培训1场', NULL, '通过分层辐射与专项培训，覆盖 100 名教材建设相关人员（辐射 60 人 + 培训 50 人，含 10 人重叠覆盖），明确三级教材培育体系下不同层级教材的建设重点、申报标准与经验复用路径，预期使 80% 校级辐射对象掌握教材提质方法，70% 参训人员能独立制定适配层级的教材开发方案，实现教材建设能力与三级培育体系的精准衔接，为全省三级教材培育质量提升、优质教材梯次晋级奠定基础。',
     '5-3-5-1 三级教材建设经验汇编手册 1 套（含国家 / 省 / 校不同层级案例）
5-3-5-2线上分享 PPT 及案例视频 、线下研讨签到表及问题记录表1 组
5-3-5-3 对 1 指导台账 1 套。
5-3-5-4培训方案及课程大纲 1 套
5-3-5-5层级培育政策解读手册及申报指南 、参训人员签到表及分组演练成果1 组
5-3-5-6实操案例库（含评审标准、申报书模板）、培训满意度问卷及效果评估报告 1 套。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2026, 'SG05030626', '5-3-6 制定实施“专业群教材编审选用”全过程监督机制；【追加】：凡编必审，凡选必审的审核、选用制度100%执行；', 3,
     8, 10, 10, '制定实施“专业群教材编审选用”文件1套', NULL, '通过 “校企行” 三方联合监督 + 学校内部专项监督的协同体系与全流程监督流程，覆盖专业群教材编审（编写、一级至三级审核）与选用（需求调研、评估、确认）的每一个关键环节，明确各方在监督中的工作边界与协作方式，预期将实现 “凡编必审，凡选必审” 制度 100% 落地执行，该监督机制形成的 “全程闭环、责任到人” 模式将实现专业群教材质量的全面提升，为职业教育领域专业群教材建设监督工作提供可复制的制度参考。',
     '5-3-6-1专业群教材编审选用调研及报告1份
5-3-6-2《专业群教材编审选用全过程监督管理办法》、《教材编审环节监督标准》、《教材选用环节监督标准》等监督制度与标准类材料1 套
5-3-6-3《教材编审监督记录表》、《教材选用监督记录表》、《违规整改通知书》、《监督人员考核表》等监督过程与记录类材料1 组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2026, 'SG05030726', '5-3-7 探索建立国家、省级、校级教材培育体系。', 3,
     8, 12, 12, '教材培育系统1套', NULL, '通过 “国家引领、省级统筹、校级支撑” 的三级协同机制与信息共享平台，覆盖全国范围内的高校、职业院校及相关企业、行业协会，明确国家、省、校三级管理部门及参与主体（如编写团队、评审专家）的职责边界与协作方式，预期将显著提升教材建设的整体质量与层次，有效解决当前教材 “数量多、精品少”“理论强、实践弱” 的问题，实现教材建设与国家教育战略、区域产业发展、学校人才培养的深度融合，为构建中国特色、世界水平的教育教材体系奠定坚实基础。',
     '5-3-7 -1教材培育体系建设调研及报告1份
5-3-7 -2《国家、省级、校级教材培育体系建设总体方案》、《国家级教材培育管理办法》、《三级教材培育信息共享平台使用手册》等体系建设与管理类材料1 套，
5-3-7 -3各级教材培育申报与审批材料、各级教材培育过程记录材料、各级教材培育过程记录材料等培育过程与成果类材料1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2026, 'SG05030826', '5-3-8 【新增】教材建设经验辐射影响100人；对外开展教材培训1场', 3,
     8, 12, 12, '教材建设经验辐射影响证明1份；对外开展教材培训1场', NULL, '通过 “线下 + 线上” 混合培训模式与针对性内容设计，覆盖100 名来自院校、企业、行业协会的教材建设相关人员，明确教材建设从校级培育到国家级冲刺的全流程操作标准、责任分工与协作路径，预期将显著提升 100 名学员的教材建设能力，将实现三级教材培育经验从 “体系内沉淀” 到 “体系外赋能” 的转变，为后续经验持续辐射与项目合作奠定基础。',
     '5-3-8-1教材建设对外培训实施方案
5-3-8 -2培训讲师邀请与管理办法、培训资料汇编等培训管理与支撑类材料1 套，
5-3-8 -3培训过程记录材料、学员反馈材料、成果总结材料等培训过程与成果类材料1组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2027, 'SG05030927', '5-3-9 修订教材建设实施细则；', 3,
     8, 12, 12, '修订教材建设实施细则1份', NULL, '通过新增与三级教材培育体系的衔接条款、专项申报通道及资源联动支持，覆盖校级教材开发、省级培育推荐、国家级培育冲刺全层级需求，明确各主体在教材建设各环节的操作规范、协作边界与资源支持方式，预期将显著提升教材建设质量与培育成功率，实现教材建设与三级培育体系的深度融合，为其他院校教材建设实施细则修订提供可复制的参考。',
     '5-3-9-1教材建设实施细则修订立项报告、修订调研与论证材料
5-3-9 -2修订评审与发布文件等修订管理与支撑类材料1 套，
5-3-9 -3效果评估材料、落地验证材料等修订落地与验证类材料1 组。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2027, 'SG05031027', '5-3-10 修订教材选用管理办法；', 3,
     8, 10, 10, '修订教材选用管理办法1份', NULL, '
通过标准化流程构建，覆盖全学段全类型教材管理，明确多元主体权责边界，预期形成动态优化机制，实现教材价值与质量双保障，为教育质量提升奠定基础。',
     '5-3-10-1 教材选用论证报告等主体资质与论证材料 1 组
5-3-10-2选用委员会及学科组成员备案表
5-3-10-3教材选用全流程档案、监测评估与反馈材料 1 套，', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2027, 'SG05031127', '5-3-11 优化“专业群教材编审选用”全过程监督机制。', 3,
     8, 10, 10, '优化实施“专业群教材编审选用”文件1套', NULL, '通过 “三审三监” 闭环机制构建，覆盖专业群全维度教材体系，明确多元监督主体权责清单，预期形成动态迭代监督体系，实现专业群教材 “质效双优” 目标，为专业群建设高质量发展筑牢根基。',
     '5-3-11-1 专业群人才培养方案与教材内容匹配度分析表、行业技术标准与教材知识点对标清单、实训教材与专业群实训设备适配性核查记录、活页式教材更新改版审批单及内容调整说明
5-3-11-2教材质量评估报告等专业群教材编审选用监督全流程档案 1 套，
5-3-11-3专业群教材编审进度与质量自查报告、企业用户教材使用反馈问卷等监督主体资质与佐证材料 1 组', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2027, 'SG05031227', '5-3-12 【新增】教材建设经验辐射影响100人；对外开展教材培训1场。', 3,
     8, 12, 12, '教材建设经验辐射影响证明1份；对外开展教材培训1场', NULL, '通过线上线下多渠道经验传递，覆盖 100 名教育工作者，明确教材建设的核心流程与关键要点，预期使 80% 以上辐射对象掌握基础教材编写方法，实现教材开发能力初步提升，为后续区域内教材质量整体优化奠定基础。
通过系统培训与实操演练，覆盖 50 名参训人员，明确优质教材的评价标准与应用策略，预期使参训人员能独立完成简单教材框架设计，实现教材开发与教学实践的精准衔接，为打造区域特色教材体系提供人才支撑。',
     '5-3-12-1 教材建设经验手册 1 套
5-3-12-2线上分享 PPT 及视频资源 1 组、线下研讨签到表及成果记录 1 组、一对一指导记录表 1 套；
5-3-12-3培训方案及课程大纲 1 套、讲师课件及案例资料 1 组、参训人员报名表及签到表 1 组、实操演练成果及点评记录 1 套、培训满意度调查问卷及总结报告 1 套。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2028, 'SG05031328', '5-3-13 优化国家、省级、校级教材培育体系。', 3,
     8, 12, 12, '优化国家、省级、校级教材培育方案', NULL, '通过分层培育与协同联动，覆盖国家 - 省级 - 校级共 410 个教材开发团队及 1200 余名编写人员，明确各层级教材培育的目标定位、流程标准与资源获取路径，预期使 85% 以上校级教材达到省级评审基本要求，60% 省级培育教材具备推荐至国家级评选的潜力，实现三级教材质量阶梯式提升，为构建全国特色教材体系、服务教育高质量发展奠定基础。',
     '5-3-13-1 国家-省级-校级教材培育管理调研及报告1份
5-3-13-2 国家-省级-校级教材培育管理办法 1 套
5-3-13-3 各层级教材评审标准及指标体系 1 组、协同联动工作机制文件1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2028, 'SG05031428', '5-3-14 【新增】教材建设经验辐射影响100人；对外开展教材培训1场。', 3,
     8, 12, 12, '教材建设经验辐射影响证明1份；对外开展教材培训1场', NULL, '通过分层辐射与专项培训，覆盖 100 名教材建设相关人员（辐射 60 人 + 培训 50 人，含 10 人重叠覆盖），明确三级教材培育体系下不同层级教材的建设重点、申报标准与经验复用路径，预期使 80% 校级辐射对象掌握教材提质方法，70% 参训人员能独立制定适配层级的教材开发方案，实现教材建设能力与三级培育体系的精准衔接，为全省三级教材培育质量提升、优质教材梯次晋级奠定基础。',
     '5-3-14-1 三级教材建设经验汇编手册 1 套（含国家 / 省 / 校不同层级案例）、线上分享 PPT 及案例视频 1 组、线下研讨签到表及问题记录表 1 组、1 对 1 指导台账 1 套。
5-3-14-2培训方案及课程大纲 1 套
5-3-14 -3层级培育政策解读手册及申报指南 1 组实操案例库（含评审标准、申报书模板）1 套
5-3-14-4参训人员签到表及分组演练成果 1 组
5-3-14-5培训满意度问卷及效果评估报告 1 套。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2029, 'SG05031529', '5-3-15 申报省级教材管理案例1个。', 3,
     8, 12, 12, '申报省级教材管理案例1个', '省级及以上', '通过构建 “三级联动、全周期规范、数字化支撑” 的省级教材管理模式，覆盖省内 14 个地市、200 所学校及 300 个教材开发团队，明确省级教材管理的责任分工、流程标准与质量要求，预期使省级教材培育周期缩短 20%，优质教材（获省级及以上奖项）占比提升至 30%，实现省级教材质量与管理效率双提升，为其他省份构建省级教材管理体系提供可复制、可推广的实践经验，为全国省级教材管理规范化发展奠定基础。',
     '5-3-15-1 省级教材立项申报书、中期督导报告、终审验收评分表 1 组
5-3-15-2教材应用反馈调研问卷及分析报告 1 套
5-3-15 -3数字化管理平台数据统计报表（含申报数量、评审进度、应用数据）1 组.', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 25, '0,5,25', 2029, 'SG05031629', '5-3-16 【新增】教材建设经验辐射影响100人；对外开展教材培训1场。', 3,
     8, 12, 12, '教材建设经验辐射影响证明1份；对外开展教材培训1场', NULL, '通过分层辐射与专项培训，覆盖 100 名教材建设相关人员（辐射 60 人 + 培训 50 人，含 10 人重叠覆盖），明确三级教材培育体系下不同层级教材的建设重点、申报标准与经验复用路径，预期使 80% 校级辐射对象掌握教材提质方法，70% 参训人员能独立制定适配层级的教材开发方案，实现教材建设能力与三级培育体系的精准衔接，为全省三级教材培育质量提升、优质教材梯次晋级奠定基础。',
     '5-3-16-1三级教材建设经验汇编手册 1 套（含国家 / 省 / 校不同层级案例）
5-3-16-2线上分享 PPT 及案例视频 1 组、线下研讨签到表及问题记录表 1 组、1 对 1 指导台账 1 套。
5-3-16-3培训方案及课程大纲 1 套、层级培育政策解读手册及申报指南 1 组、实操案例库（含评审标准、申报书模板）1 套、参训人员签到表及分组演练成果 1 组、培训满意度问卷及效果评估报告 1 套。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2025, 'SG06010125', '6-1-1 全员开展“炼师德、铸师魂”师德师风专题研修3次', 3,
     1, 1, 1, '开展专题研修3次', NULL, '通过成立开展师德师风专题研修形成了 四位一体的系统化研修体系，覆盖了所有在职教师，明确了新时代教师 “为谁培养人、培养什么人、怎样培养人” 的根本方向，预期全员开展“炼师德、铸师魂”师德师风专题研修3次，实现了尊师重教、廉洁从教、团结互助的良好氛围，为构建高质量教育体系筑牢了师德根基。',
     '6-1-1-1 师德师风专题研修通知
6-1-1-2 培训人员名单
6-1-1-3 选取部分老师的学习过程资料和证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2025, 'SG06010225', '6-1-2 制定并完善教师师德师风考核办法，建立师德师风定期巡查制度；【追加】：形成一套师德师风问题监督机制', 3,
     1, 38, 38, '制定考形成核办法1个，形成监督机制1套', NULL, '通过制定并完善教师师德师风考核办法，不仅构建起 “日常记录 + 季度评估 + 年度考核” 的立体化考核体系，更以此为基础形成了师德师风定期巡查制度，覆盖了教师履职的全领域与各群体，明确了师德师风建设的 “红线” 与 “底线”，预期形成1套师德师风问题监督机制，实现了师德师风建设从 “被动应对” 转向 “主动治理”，为党组织开展师德师风教育开展奠定扎实基础。',
     '6-1-2-1学校关于师德师风建设的制度文件：《徐州工业职业技术学院加强师德师风建设长效工作机制实施办法》、《徐州工业职业技术学院教师师德师风考核办法》、徐州工业职业技术学院师德师风建设三年行动计划（2025-2027年）
6-1-2-2 组织建设与职责分工文件：学校关于成立师德师风建设领导小组的文件、各部门职责分工文件
6-1-2-3 监督流程与平台相关材料：年度巡查计划、巡查监督工作流程图', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2025, 'SG06010325', '6-1-3 开展师德师风表彰活动，表彰“师德标兵”2人，“最美教师”2人', 3,
     1, 38, 38, '表彰“师德标兵”2人，“最美教师”2人', NULL, '通过开展师德师风表彰活动形成了 “选树典型 + 宣传推广 + 示范带动” 的长效引领机制，覆盖了教师群体的全维度与各层面，明确了新时代师德建设的价值导向与评价标准，预期表彰“师德标兵”2人，“最美教师”2人，实现了 “比学赶超、争当先进” 的良好局面，为师德师风工作开展奠定扎实基础。',
     '6-1-3-1 开展师德师风表彰活动的通知
6-1-3-2  学校表彰文件
6-1-3-3  获表彰人员先进事迹展示(含照片)   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2025, 'SG06010425', '6-1-4 引培省级及以上教学育人类团队1个', 3,
     1, 3, 3, '引培教学育人类团队1个', '省级及以上', '通过开展引培教学育人类团队形成了教学育人从 “学科融合、课内外联动、家校社协同” 的一体化育人体系 ，覆盖了教学育人的全领域与关键环节，明确了教学育人的核心目标与实施标准，预期引培省级及以上教学育人类团队1个，实现了“个体探索” 向 “团队协同” 转变，为学校深化教育教学改革、推进素质教育全面实施奠定了扎实基础。',
     '6-1-4-1 省教育厅正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2026, 'SG06010526', '6-1-5 全员开展“炼师德、铸师魂”师德师风专题研修3次', 3,
     1, 38, 38, '开展专题研修3次', NULL, '通过成立开展师德师风专题研修形成了 四位一体的系统化研修体系，覆盖了所有在职教师，明确了新时代教师 “为谁培养人、培养什么人、怎样培养人” 的根本方向，预期全员开展“炼师德、铸师魂”师德师风专题研修3次，实现了尊师重教、廉洁从教、团结互助的良好氛围，为构建高质量教育体系筑牢了师德根基。',
     '6-1-5-1 师德师风专题研修通知
6-1-5-2 培训人员名单
6-1-5-3 选取部分老师的学习过程资料和证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2026, 'SG06010626', '6-1-6 开展师德师风巡查', 3,
     1, 38, 38, '开展师德师风巡查1次', NULL, '通过开展师德师风巡查形成了“动态监测 + 问题整改 + 成果巩固” 的闭环管理机制.，覆盖了教师履职的全领域与各群体，明确了师德建设的核心方向，预期开展师德师风巡查1次，实现了师德师风建设从 “被动应对” 转向 “主动治理”，为师德师风工作开展奠定扎实基础。',
     '6-1-6-1 开展师德师风巡查的通知
6-1-6-2 师德师风巡查座谈研讨会(附照片)
6-1-6-3 师德师风巡查反馈报告              ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2026, 'SG06010726', '6-1-7 开展师德师风表彰活动，表彰“师德标兵”2人，“最美教师”2人；', 3,
     1, 38, 38, '表彰“师德标兵”2人，“最美教师”2人', NULL, '通过开展师德师风表彰活动形成了 “选树典型 + 宣传推广 + 示范带动” 的长效引领机制，覆盖了教师群体的全维度与各层面，明确了新时代师德建设的价值导向与评价标准，预期全员开展“炼师德、铸师魂”师德师风专题研修3次，实现了尊师重教、廉洁从教、团结互助的良好氛围，为师德师风工作开展奠定扎实基础。',
     '6-1-7-1 开展师德师风表彰活动的通知
6-1-7-2  学校表彰文件
6-1-7-3  获表彰人员先进事迹展示(含照片)   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2026, 'SG06010826', '6-1-8 引培省级及以上教学育人类团队1个', 3,
     1, 3, 3, '引培教学育人类团队1个', '省级及以上', '通过开展引培教学育人类团队形成了教学育人从 “学科融合、课内外联动、家校社协同” 的一体化育人体系 ，覆盖了教学育人的全领域与关键环节，明确了教学育人的核心目标与实施标准，预期引培省级及以上教学育人类团队1个，实现了“个体探索” 向 “团队协同” 转变，为学校深化教育教学改革、推进素质教育全面实施奠定了扎实基础。',
     '6-1-8-1 省教育厅正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2026, 'SG06010926', '6-1-9 引培国家级名师1人', 3,
     1, 3, 3, '引培名师1人', '国家级', '通过引培国家级名师形成了“名师引领 + 团队共建 + 成果辐射” 的立体化发展机制，覆盖了教育教学的多领域与各层级，明确了国家级名师培养方向，预期引培国家级名师1人，实现了国家级名师资源的汇集学校进一步深化教育教学改革、推进内涵式发展、建设特色化名校工作开展奠定扎实基础。',
     '6-1-9-1 教育部正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2027, 'SG06011027', '6-1-10 全员开展“炼师德、铸师魂”师德师风专题研修3次', 3,
     1, 38, 38, '开展专题研修3次', NULL, '通过成立开展师德师风专题研修形成了 四位一体的系统化研修体系，覆盖了所有在职教师，明确了新时代教师 “为谁培养人、培养什么人、怎样培养人” 的根本方向，预期全员开展“炼师德、铸师魂”师德师风专题研修3次，实现了尊师重教、廉洁从教、团结互助的良好氛围，为构建高质量教育体系筑牢了师德根基。',
     '6-1-10-1 师德师风专题研修通知
6-1-10-2 培训人员名单
6-1-10-3 选取部分老师的学习过程资料和证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2027, 'SG06011127', '6-1-11 开展师德师风表彰活动，表彰“师德标兵”2人，“最美教师”2人', 3,
     1, 38, 38, '表彰“师德标兵”2人，“最美教师”2人', NULL, '通过开展师德师风表彰活动形成了 “选树典型 + 宣传推广 + 示范带动” 的长效引领机制，覆盖了教师群体的全维度与各层面，明确了新时代师德建设的价值导向与评价标准，预期表彰“师德标兵”2人，“最美教师”2人，实现了 “比学赶超、争当先进” 的良好局面，为师德师风工作开展奠定扎实基础。',
     '6-1-11-1 开展师德师风表彰活动的通知
6-1-11-2  学校表彰文件
6-1-11-3  获表彰人员先进事迹展示(含照片)   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2027, 'SG06011227', '6-1-12 引培省级及以上教学育人类团队1个', 3,
     1, 3, 3, '引培教学育人类团队1个', '省级及以上', '通过开展引培教学育人类团队形成了教学育人从 “学科融合、课内外联动、家校社协同” 的一体化育人体系 ，覆盖了教学育人的全领域与关键环节，明确了教学育人的核心目标与实施标准，预期引培省级及以上教学育人类团队1个，实现了“个体探索” 向 “团队协同” 转变，为学校深化教育教学改革、推进素质教育全面实施奠定了扎实基础。',
     '6-1-12-1 省教育厅正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2027, 'SG06011327', '6-1-13 引培国家级名师1人', 3,
     1, 3, 3, '引培名师1人', '国家级', '通过引培国家级名师形成了“名师引领 + 团队共建 + 成果辐射” 的立体化发展机制，覆盖了教育教学的多领域与各层级，明确了国家级名师培养方向，预期引培国家级名师1人，实现了国家级名师资源的汇集学校进一步深化教育教学改革、推进内涵式发展、建设特色化名校工作开展奠定扎实基础。',
     '6-1-13-1 教育部正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2027, 'SG06011427', '6-1-14 申报国家级课题1项', 3,
     11, 26, 26, '申报课题1项', '国家级', '通过申报国家级课题形成了“科研引领 + 团队协作 + 成果转化” 的一体化工作机制，覆盖了教育科研的多领域与实践场景，明确了教育科研的核心目标与实施标准，预期申报国家级课题1项，实现了科研成果从 “理论层面” 向 “实践层面” 落地，为学校进一步深化教育科研改革、推进特色办学、建设高水平教育团队工作开展奠定扎实基础。',
     '6-1-14-1 教育部或相关国家部委关于国家级课题申报的通知                                                         6-1-14-2  国家级课题申报书                                6-1-14-3  学校关于推荐国家级课题的公示名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2028, 'SG06011528', '6-1-15 全员开展“炼师德、铸师魂”师德师风专题研修3次', 3,
     1, 38, 38, '开展专题研修3次', NULL, '通过成立开展师德师风专题研修形成了 四位一体的系统化研修体系，覆盖了所有在职教师，明确了新时代教师 “为谁培养人、培养什么人、怎样培养人” 的根本方向，预期全员开展“炼师德、铸师魂”师德师风专题研修3次，实现了尊师重教、廉洁从教、团结互助的良好氛围，为构建高质量教育体系筑牢了师德根基。',
     '6-1-15-1 师德师风专题研修通知
6-1-15-2 培训人员名单
6-1-15-3 选取部分老师的学习过程资料和证书 ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2028, 'SG06011628', '6-1-16 开展师德师风巡查', 3,
     1, 38, 38, '开展师德师风巡查1次', NULL, '通过开展开展师德师风巡查形成了“动态监测 + 问题整改 + 成果巩固” 的闭环管理机制.，覆盖了教师履职的全领域与各群体，明确了师德建设的核心方向，预期开展师德师风巡查1次，实现了师德师风建设从 “被动应对” 转向 “主动治理”，为师德师风工作开展奠定扎实基础。',
     '6-1-16-1 开展师德师风巡查的通知
6-1-16-2 师德师风巡查座谈研讨会(附照片)
6-1-16-3 师德师风巡查反馈报告              ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2028, 'SG06011728', '6-1-17 开展师德师风表彰活动，表彰“师德标兵”2人，“最美教师”2人', 3,
     1, 38, 38, '表彰“师德标兵”2人，“最美教师”2人', NULL, '通过开展师德师风表彰活动形成了 “选树典型 + 宣传推广 + 示范带动” 的长效引领机制，覆盖了教师群体的全维度与各层面，明确了新时代师德建设的价值导向与评价标准，预期预期表彰“师德标兵”2人，“最美教师”2人，实现了 “比学赶超、争当先进” 的良好局面，为师德师风工作开展奠定扎实基础。',
     '6-1-17-1 开展师德师风表彰活动的通知
6-1-17-2  学校表彰文件
6-1-17-3  获表彰人员先进事迹展示(含照片)   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2028, 'SG06011828', '6-1-18 引培省级及以上名师1人', 3,
     1, 3, 3, '引培名师1人', '省级及以上', '通过引培省级及以上级名师形成了“名师引领 + 团队共建 + 成果辐射” 的立体化发展机制，覆盖了教育教学的多领域与各层级，明确了国家级名师培养方向，预期引培省级及以上名师1人，实现了国家级名师资源的汇集学校进一步深化教育教学改革、推进内涵式发展、建设特色化名校工作开展奠定扎实基础。',
     '6-1-18-1 教育厅或教育部正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2028, 'SG06011928', '6-1-19 申报国家级课题1项', 3,
     11, 26, 26, '申报课题1项', '国家级', '通过申报国家级课题形成了“科研引领 + 团队协作 + 成果转化” 的一体化工作机制，覆盖了教育科研的多领域与实践场景，明确了教育科研的核心目标与实施标准，预期申报国家级课题1项，实现了科研成果从 “理论层面” 向 “实践层面” 落地，为学校进一步深化教育科研改革、推进特色办学、建设高水平教育团队工作开展奠定扎实基础。',
     '6-1-19-1 教育部或相关国家部委关于国家级课题申报的通知                                                         6-1-19-2  国家级课题申报书                       6-1-19-3  学校关于推荐国家级课题的公示名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2029, 'SG06012029', '6-1-20 全员开展“炼师德、铸师魂”师德师风专题研修3次', 3,
     1, 38, 38, '开展专题研修3次', NULL, '通过成立开展师德师风专题研修形成了 四位一体的系统化研修体系，覆盖了所有在职教师，明确了新时代教师 “为谁培养人、培养什么人、怎样培养人” 的根本方向，预期全员开展“炼师德、铸师魂”师德师风专题研修3次，实现了尊师重教、廉洁从教、团结互助的良好氛围，为构建高质量教育体系筑牢了师德根基。',
     '6-1-20-1 师德师风专题研修通知
6-1-20-2 培训人员名单
6-1-20-3 选取部分老师的学习过程资料和证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2029, 'SG06012129', '6-1-21 开展师德师风表彰活动，表彰“师德标兵”2人，“最美教师”2人', 3,
     1, 38, 38, '表彰“师德标兵”2人，“最美教师”2人', NULL, '通过开展师德师风表彰活动形成了 “选树典型 + 宣传推广 + 示范带动” 的长效引领机制，覆盖了教师群体的全维度与各层面，明确了新时代师德建设的价值导向与评价标准，预期表彰“师德标兵”2人，“最美教师”2人，实现了 “比学赶超、争当先进” 的良好局面，为师德师风工作开展奠定扎实基础。',
     '6-1-21-1 开展师德师风表彰活动的通知
6-1-21-2  学校表彰文件
6-1-21-3  获表彰人员先进事迹展示(含照片)   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 26, '0,6,26', 2029, 'SG06012229', '6-1-22 引培省级及以上名师1人', 3,
     1, 3, 3, '引培名师1人', '省级及以上', '通过引培省级及以上级名师形成了“名师引领 + 团队共建 + 成果辐射” 的立体化发展机制，覆盖了教育教学的多领域与各层级，明确了国家级名师培养方向，预期 引培省级及以上名师1人，实现了国家级名师资源的汇集学校进一步深化教育教学改革、推进内涵式发展、建设特色化名校工作开展奠定扎实基础。',
     '6-1-22-1 教育厅或教育部正式立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2025, 'SG06020125', '6-2-1 出台《校企共用互聘高层次人才管理办法》，建设教师企业实践基地2个', 3,
     1, 1, 1, '出台管理办法1个，建设教师企业实践基地2个', NULL, '通过出台校企共用互聘高层次人才管理办法和建设教师企业实践基地形成了“政策保障+平台支撑+双向流动”的校企人才协同发展机制，覆盖了产教融合的多领域与全链条，明确了校企协同育人的核心目标与实施标准，预期建设教师企业实践基地2个，为学校深化产教融合、推进应用型人才培养改革、建设高水平 “双师型” 教师队伍工作开展奠定扎实基础。',
     '6-2-1-1 关于印发《徐州工业职业技术学院校企共用互聘高层次人才管理办法》的通知
6-2-1-2  关于公布徐州工业职业技术学院教师企业实践基地名单的通知
6-2-1-3  教师企业实践基地授牌仪式(新闻报道+照片)
6-2-1-4  教师企业实践相关材料（在徐工实践的照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2025, 'SG06020225', '6-2-2 完善《专业教师下企业实践锻炼制度》文件1套', 3,
     1, 38, 38, ' 完善文件1套', NULL, '通过完善《专业教师下企业实践锻炼制度》形成了 “学校 - 企业 - 教师” 三方联动协调机制，覆盖了专业教师群体与实践领域的关键要素，明确了专业教师下企业实践的核心目标与实施标准，预期修订《专业教师下企业实践锻炼制度》文件，实现了产教融合从 “浅层合作” 向 “深度协同” 迈进，为学校建设高水平 “双师型” 教师队伍、深化应用型人才培养改革工作开展奠定扎实基础。',
     '6-2-2-1 学校层面制度文件：《徐州工业职业技术学院专业教师下企业实践锻炼管理办法》的通知                                                           6-2-2-2 教师实践锻炼申请表                   6-2-2-3 实践锻炼日志                                6-2-2-4实践锻炼考核表', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2025, 'SG06020325', '6-2-3 选拔10名教师进企服务，引进6名企业高水平兼职兼课教师；【追加】：新建大师工作室1个', 3,
     1, 1, 1, '选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个', NULL, '通过 选拔教师进企服务，引进企业高水平兼职兼课教师形成了 “双向流动 + 协同育人 + 资源互补” 的校企人才合作机制.，覆盖了校企协同的多领域与关键环节，明确了校企双方的职责定位与合作标准，预期选拔10名教师进企服务，引进6名企业高水平兼职兼课教师，新建大师工作室1个，实现了校企合作从 “项目合作” 向 “战略协同” 迈进，学校建设 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-3-1 关于公布2025年暑期专业教师下企业锻炼人员名单的通知
6-2-3-2 关于公布2025-2026学年第一学期引进企业高水平兼职兼课教师的通知
6-2-3-3 庄阳大师工作室立项文件   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2025, 'SG06020425', '6-2-4 出台《产业教授、技能大师特聘岗设立办法》，引培博士4人', 3,
     1, 3, 3, '出台办法1个，引培博士4人', NULL, '通过出台《产业教授、技能大师特聘岗设立办法》和引培博士形成了 “高层次人才引育 + 产教深度融合 + 科研教学双促” 的立体化人才建设机制，覆盖了学科专业与人才培育的关键维度，明确了人才建设与发展的核心目标及实施标准，预期 出台《产业教授、技能大师特聘岗设立办法》，引培博士4人，实现了人才建设从 “单一引进” 向 “系统培育” 转变，为学校深化应用型人才培养改革、建设高水平学科专业、提升服务区域经济社会发展能力工作开展奠定扎实基础。',
     '6-2-4-1关于印发《徐州工业职业技术学院产业教授、技能大师特聘岗设立办法》的通知
6-2-4-2 产业教授、技能大师特聘岗申请表                                            6-2-4-3 人事处引进博士的资料 ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2025, 'SG06020525', '6-2-5 选派2名教师对外交流。【追加】：选派18名教师对外交流。', 3,
     5, 27, 27, '选派20名教师对外交流', NULL, '通过选派教师对外交流形成了 “短期访学 + 长期研修 + 项目合作” 的立体化教师交流体系，覆盖了教师群体与交流领域的关键维度，明确了教师发展与学校建设的核心目标及实施标准，预期选派20名教师对外交流，学校建设高水平教师队伍、推进国际化与特色化办学工作开展奠定扎实基础。',
     '6-2-5-1申请审批材料：任务申请审批表、邀请函、公示文
6-2-5-2 团组出访日程
6-2-5-3 总结材料：出访成果报告、外事活动照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2026, 'SG06020626', '6-2-6 建设教师企业实践基地2个', 3,
     1, 38, 38, '建设教师企业实践基地2个', NULL, '通过建设教师企业实践基地形成了 “校企协同共建 + 实践赋能教师 + 成果反哺教学” 的闭环式教师发展机制，覆盖了教师成长与产教融合的关键维度，明确了教师实践与产教融合的核心目标及实施标准，预期建设教师企业实践基地2个，实现了产业认知与教学转化双提升，为学校建设高水平 “双师型” 教师队伍、深化产教融合育人模式改革、培养适应新时代产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-6-1  关于公布徐州工业职业技术学院教师企业实践基地名单的通知                                           6-2-6-2  教师企业实践基地授牌仪式(新闻报道+照片)
6-2-6-3 教师企业实践相关材料（在徐工实践的照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2026, 'SG06020726', '6-2-7 选拔10名教师进企服务，引进6名企业高水平兼职兼课教师；新建大师工作室1个', 3,
     1, 1, 1, '选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个', NULL, '通过 选拔教师进企服务，引进企业高水平兼职兼课教师形成了 “双向流动 + 协同育人 + 资源互补” 的校企人才合作机制.，覆盖了校企协同的多领域与关键环节，明确了校企双方的职责定位与合作标准，预期选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个，实现了校企合作从 “项目合作” 向 “战略协同” 迈进，学校建设 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '
6-2-7-1 关于公布2025-2026学年第二学期引进企业高水平兼职兼课教师的通知
 6-2-7-2 关于公布2026-2027学年第一学期引进企业高水平兼职兼课教师的通知    （
6-2-7-3大师工作室立项文件  ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2026, 'SG06020826', '6-2-8 引培博士、教授4人，省级及以上高层次人才3人', 3,
     1, 4, 4, ' 引培博士、教授4人，高层次人才3人', '省级及以上', '通过引培高层次人才形成了“引才聚智 + 育才赋能 + 用才建功” 的全链条人才建设闭环机制，覆盖了学校学科建设与社会服务的全维度，明确了人才建设与学校发展的核心目标及实施标准，预期 引培博士、教授4人，省级高层次人才3人，实现了高层次人才从 “单一引进” 向 “引育用一体化” 转变，为学校推进一流学科建设、深化科研体制改革、构建高水平人才培养体系、强化服务国家战略与区域发展能力工作开展奠定扎实基础。',
     '6-2-8-1 人事处引进博士的名单资料                                          6-2-8-2 人事处引进教授的名单资料                                                 6-2-8-2 省级及以上高层次人才立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2026, 'SG06020926', '6-2-9 选派3名教师对外交流。【追加】：选派17名教师对外交流', 3,
     5, 27, 27, '选派20名教师对外交流', NULL, '通过选派教师对外交流形成了 “短期访学 + 长期研修 + 项目合作” 的立体化教师交流体系，覆盖了教师群体与交流领域的关键维度，明确了教师发展与学校建设的核心目标及实施标准，预期选派20名教师对外交流，实现了对外交流从 “个体成长” 向 “群体提升” 转变，为学校建设高水平教师队伍、推进国际化与特色化办学工作开展奠定扎实基础。',
     '6-2-9-1申请审批材料：任务申请审批表、邀请函、公示文
6-2-9-2 团组出访日程
6-2-9-3 总结材料：出访成果报告、外事活动照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2027, 'SG06021027', '6-2-10 建设教师企业实践基地2个', 3,
     1, 38, 38, '建设教师企业实践基地2个', NULL, '通过建设教师企业实践基地形成了 “校企协同共建 + 实践赋能教师 + 成果反哺教学” 的闭环式教师发展机制，覆盖了教师成长与产教融合的关键维度，明确了教师实践与产教融合的核心目标及实施标准，预期建设教师企业实践基地2个，实现了产业认知与教学转化双提升，为学校建设高水平 “双师型” 教师队伍、深化产教融合育人模式改革、培养适应新时代产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-10-1  关于公布徐州工业职业技术学院教师企业实践基地名单的通知                                           6-2-10-2  教师企业实践基地授牌仪式(新闻报道+照片)
6-2-10-3 教师企业实践相关材料（在徐工实践的照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2027, 'SG06021127', '6-2-11 选拔10名教师进企服务，引进6名企业高水平兼职兼课教师；【追加】：新建大师工作室1个', 3,
     1, 1, 1, '选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个', NULL, '通过 选拔教师进企服务，引进企业高水平兼职兼课教师形成了 “双向流动 + 协同育人 + 资源互补” 的校企人才合作机制.，覆盖了校企协同的多领域与关键环节，明确了校企双方的职责定位与合作标准，预期选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个，实现了校企合作从 “项目合作” 向 “战略协同” 迈进，学校建设 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-11-1 关于公布2026-2027学年第二学期引进企业高水平兼职兼课教师的通知
 6-2-11-2 关于公布2027-2028学年第一学期引进企业高水平兼职兼课教师的通知
6-2-11-3大师工作室立项文件  ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2027, 'SG06021227', '6-2-12 引培博士、教授4人，省级及以上高层次人才3人', 3,
     1, 4, 4, ' 引培博士、教授4人，高层次人才3人', '省级及以上', '通过引培高层次人才形成了“引才聚智 + 育才赋能 + 用才建功” 的全链条人才建设闭环机制，覆盖了学校学科建设与社会服务的全维度，明确了人才建设与学校发展的核心目标及实施标准，预期 引培博士、教授4人，省级高层次人才3人，实现了高层次人才从 “单一引进” 向 “引育用一体化” 转变，为学校推进一流学科建设、深化科研体制改革、构建高水平人才培养体系、强化服务国家战略与区域发展能力工作开展奠定扎实基础。',
     '6-2-12-1 人事处引进博士的名单资料                                          6-2-12-2 人事处引进教授的名单资料                                                 6-2-12-2 省级及以上高层次人才立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2027, 'SG06021327', '6-2-13 选派3名教师对外交流。【追加】：选派17名教师对外交流', 3,
     5, 27, 27, '选派20名教师对外交流', NULL, '通过选派教师对外交流形成了 “短期访学 + 长期研修 + 项目合作” 的立体化教师交流体系，覆盖了教师群体与交流领域的关键维度，明确了教师发展与学校建设的核心目标及实施标准，预期选派20名教师对外交流，实现了对外交流从 “个体成长” 向 “群体提升” 转变，为学校建设高水平教师队伍、推进国际化与特色化办学工作开展奠定扎实基础。',
     '6-2-13-1申请审批材料：任务申请审批表、邀请函、公示文
6-2-13-2 团组出访日程
6-2-13-3 总结材料：出访成果报告、外事活动照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2028, 'SG06021428', '6-2-14 修订《校企共用互聘高层次人才管理办法》', 3,
     1, 3, 3, '修订办法1个', NULL, '通过修订《校企共用互聘高层次人才管理办法》形成了“校企协同选才 + 双向赋能用才 + 动态考核留才” 的全流程闭环管理机制，覆盖了校企人才协同的全领域与全群体，明确了校企互聘的核心目标与实施标准，预期 修订《校企共用互聘高层次人才管理办法》，实现了“人才共育、资源共享、利益共赢” 的校企合作新生态，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-14-1 《校企共用互聘高层次人才管理办法》修订研讨会(附照片)                                                                                           6-2-14-2 学校层面制度文件：《徐州工业职业技术学院校企共用互聘高层次人才管理办法》的通知     ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2028, 'SG06021528', '6-2-15 建设教师企业实践基地2个', 3,
     1, 38, 38, '建设教师企业实践基地2个', NULL, '通过建设教师企业实践基地形成了 “校企协同共建 + 实践赋能教师 + 成果反哺教学” 的闭环式教师发展机制，覆盖了教师成长与产教融合的关键维度，明确了教师实践与产教融合的核心目标及实施标准，预期建设教师企业实践基地2个，实现了产业认知与教学转化双提升，为学校建设高水平 “双师型” 教师队伍、深化产教融合育人模式改革、培养适应新时代产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-15-1  关于公布徐州工业职业技术学院教师企业实践基地名单的通知                                           6-2-15-2  教师企业实践基地授牌仪式(新闻报道+照片)
6-2-15-3 教师企业实践相关材料（在徐工实践的照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2028, 'SG06021628', '6-2-16 选拔10名教师进企服务，引进6名企业高水平兼职兼课教师；【追加】：新建大师工作室1个', 3,
     1, 1, 1, '选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个', NULL, '通过 选拔教师进企服务，引进企业高水平兼职兼课教师形成了 “双向流动 + 协同育人 + 资源互补” 的校企人才合作机制.，覆盖了校企协同的多领域与关键环节，明确了校企双方的职责定位与合作标准，预期选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个，实现了校企合作从 “项目合作” 向 “战略协同” 迈进，学校建设 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '
6-2-16-1 关于公布2027-2028学年第二学期引进企业高水平兼职兼课教师的通知                                                                       6-2-16-2 关于公布2028-2029学年第一学期引进企业高水平兼职兼课教师的通知
6-2-16-3大师工作室立项文件  ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2028, 'SG06021728', '6-2-17 引培博士、教授4人，省级及以上高层次人才2人', 3,
     1, 4, 4, ' 引培博士、教授4人，高层次人才2人', '省级及以上', '通过引培高层次人才形成了“引才聚智 + 育才赋能 + 用才建功” 的全链条人才建设闭环机制，覆盖了学校学科建设与社会服务的全维度，明确了人才建设与学校发展的核心目标及实施标准，预期引培博士、教授4人，省级高层次人才2人实现了高层次人才从 “单一引进” 向 “引育用一体化” 转变，为学校推进一流学科建设、深化科研体制改革、构建高水平人才培养体系、强化服务国家战略与区域发展能力工作开展奠定扎实基础。',
     '6-2-17-1 人事处引进博士的名单资料                                          6-2-17-2 人事处引进教授的名单资料                                                 6-2-17-2 省级及以上高层次人才立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2028, 'SG06021828', '6-2-18 选派2名教师对外交流。【追加】：选派18名教师对外交流', 3,
     5, 27, 27, '选派20名教师对外交流', NULL, '通过选派教师对外交流形成了 “短期访学 + 长期研修 + 项目合作” 的立体化教师交流体系，覆盖了教师群体与交流领域的关键维度，明确了教师发展与学校建设的核心目标及实施标准，预期选派20名教师对外交流，实现了对外交流从 “个体成长” 向 “群体提升” 转变，为学校建设高水平教师队伍、推进国际化与特色化办学工作开展奠定扎实基础。',
     '6-2-18-1申请审批材料：任务申请审批表、邀请函、公示文
6-2-18-2 团组出访日程
6-2-18-3 总结材料：出访成果报告、外事活动照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2029, 'SG06021929', '6-2-19 建设教师企业实践基地2个', 3,
     1, 38, 38, '建设教师企业实践基地2个', NULL, '通过建设教师企业实践基地形成了 “校企协同共建 + 实践赋能教师 + 成果反哺教学” 的闭环式教师发展机制，覆盖了教师成长与产教融合的关键维度，明确了教师实践与产教融合的核心目标及实施标准，预期建设教师企业实践基地2个，实现了产业认知与教学转化双提升，为学校建设高水平 “双师型” 教师队伍、深化产教融合育人模式改革、培养适应新时代产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-2-19-1  关于公布徐州工业职业技术学院教师企业实践基地名单的通知                                           6-2-19-2  教师企业实践基地授牌仪式(新闻报道+照片)
6-2-19-3 教师企业实践相关材料（在徐工实践的照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2029, 'SG06022029', '6-2-20 选拔10名教师进企服务，引进6名企业高水平兼职兼课教师；【追加】：新建大师工作室1个', 3,
     1, 1, 1, '选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个', NULL, '通过 选拔教师进企服务，引进企业高水平兼职兼课教师形成了 “双向流动 + 协同育人 + 资源互补” 的校企人才合作机制.，覆盖了校企协同的多领域与关键环节，明确了校企双方的职责定位与合作标准，预期选拔10名教师进企服务，引进6名企业人员入校兼课，新建大师工作室1个，实现了校企合作从 “项目合作” 向 “战略协同” 迈进，学校建设 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '
6-2-20-1 关于公布2028-2029学年第二学期引进企业高水平兼职兼课教师的通知                                                                                   6-2-20-2 关于公布2029-2030学年第一学期引进企业高水平兼职兼课教师的通知
6-2-20-3大师工作室立项文件  ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2029, 'SG06022129', '6-2-21 引培博士、教授4人，省级及以上高层次人才2人', 3,
     1, 4, 4, ' 引培博士、教授4人，高层次人才2人', '省级及以上', '通过引培高层次人才形成了“引才聚智 + 育才赋能 + 用才建功” 的全链条人才建设闭环机制，覆盖了学校学科建设与社会服务的全维度，明确了人才建设与学校发展的核心目标及实施标准，预期在 引培博士、教授4人，省级高层次人才2人实现了高层次人才从 “单一引进” 向 “引育用一体化” 转变，为学校推进一流学科建设、深化科研体制改革、构建高水平人才培养体系、强化服务国家战略与区域发展能力工作开展奠定扎实基础。',
     '6-2-21-1 人事处引进博士的名单资料                                          6-2-21-2 人事处引进教授的名单资料                                                 6-2-21-2 省级及以上高层次人才立项文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 27, '0,6,27', 2029, 'SG06022229', '6-2-22 选派3名教师对外交流。【追加】：选派17名教师对外交流', 3,
     5, 27, 27, '选派20名教师对外交流', NULL, '通过选派教师对外交流形成了 “短期访学 + 长期研修 + 项目合作” 的立体化教师交流体系，覆盖了教师群体与交流领域的关键维度，明确了教师发展与学校建设的核心目标及实施标准，预期选派20名教师对外交流，实现了对外交流从 “个体成长” 向 “群体提升” 转变，为学校建设高水平教师队伍、推进国际化与特色化办学工作开展奠定扎实基础。',
     '6-2-22-1申请审批材料：任务申请审批表、邀请函、公示文
6-2-22-2 团组出访日程
6-2-22-3 总结材料：出访成果报告、外事活动照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2025, 'SG06030125', '6-3-1 出台《工程机械产教虚拟教研室管理办法》，成立产教虚拟教研室2个', 3,
     8, 1, 1, '出台管理办法1个，成立产教虚拟教研室2个', NULL, '通过成立产教虚拟教研室形成了“校企协同 + 数字赋能 + 教研融合” 的新型教研机制，覆盖了产教融合的多领域与全链条，明确了教研工作的核心目标与实施标准，预期出台管理办法1个，成立产教虚拟教研室2个，实现了教研活动从 “校内封闭研讨” 转向 “校企开放协作”，为学校深化产教融合育人模式改革、培养适应产业需求的高素质技术技能人才工作开展奠定扎实基础。',
     '6-3-1-1 关于印发《徐州工业职业技术学院产教虚拟教研室管理办法》的通知
6-3-1-2 关于成立徐州工业职业技术学院工程机械产教虚拟教研室的通知
6-3-1-3 产教虚拟教研室建设材料(新闻报道+照片)', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2025, 'SG06030225', '6-3-2 出台《“教师教学档案袋”制度管理办法》', 3,
     8, 9, 9, '出台管理办法1个', NULL, '通过出台《“教师教学档案袋”制度管理办法》形成了 “过程性记录 + 个性化发展 + 精准化评价” 的教师教学管理新机制，覆盖了教师教学全环节与全群体，明确了教师教学管理的核心目标与实施标准，预期出台《“教师教学档案袋”制度管理办法》，实现了教师教学管理从 “终结性评价” 转向 “过程性与终结性相结合的评价”，为学校推进教师专业化发展、构建科学高效的师资管理体系工作开展奠定扎实基础。',
     '6-3-2-1 关于印发《徐州工业职业技术学院“教师教学档案袋”制度管理办法》的通知
6-3-2-2  徐州工业职业技术学院教学类档案袋   6-3-2-3  徐州工业职业技术学院科研类档案袋', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2025, 'SG06030325', '6-3-3 成立师资人才培养发展领导小组', 3,
     1, 4, 4, '成立领导小组1个', NULL, '通过 成立师资人才培养发展领导小组形成了“统筹规划 + 部门协同 + 动态监测 + 精准施策” 的师资建设一体化工作机制，覆盖了师资建设全领域与全周期，明确了师资人才培养发展的核心目标与实施标准，预期 成立师资人才培养发展领导小组，实现了师资人才培养从 “分散化管理” 转向 “系统性统筹”，为学校深化教育教学改革、推进内涵式发展、建设高水平特色院校工作开展奠定扎实基础。',
     '6-3-3-1关于成立徐州工业职业技术学院师资人才培养发展领导小组的通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2025, 'SG06030425', '6-3-4 聘请产业教授1人；教师获省级教学技能竞赛4项', 3,
     1, 4, 4, '聘请产业教授1人，获教学技能竞赛4项', '省级及以上', '通过聘请产业教授和教师获省级教学技能竞赛形成了“产业实践引领 + 教学技能提升 + 成果双向转化” 的师资建设与教学发展协同机制，覆盖了师资建设与教学改革的多领域、全群体，明确了师资建设与教学改革的核心目标及实施标准，预期聘请省级产业教授1人，获教学技能竞赛4项，实现了师资建设从 “单一能力提升” 转向 “产业与教学双赋能”，为学校建设高水平 “双师型” 师资队伍工作开展奠定扎实基础。',
     '6-3-4-1 产业教授聘书
6-3-4-2  产业教授个人简介+照片
6-3-4-3  教师获奖证书或表彰文件(省级) 4项                   ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2025, 'SG06030525', '6-3-5 出台《“双师型”师资队伍管理办法》', 3,
     1, 38, 38, '出台管理办法1个', NULL, '通过 出台《“双师型”师资队伍管理办法》形成了 “标准引领 + 培育保障 + 考核激励 + 动态管理” 的 “双师型” 师资建设闭环机制，覆盖了“双师型” 师资建设全领域与全群体，明确了 “双师型” 师资建设的核心目标与实施标准，预期 出台《“双师型”师资队伍管理办法》，实现了 “双师型” 师资建设从从 “单一资格认定” 升级为 “能力持续提升”，为学校推进产教融合育人模式创新工作开展奠定扎实基础。',
     '6-3-5-1 关于印发《徐州工业职业技术学院“双师型”教师认定与管理办法》的通知
6-3-5-2  徐州工业职业技术学院“双师型”教师申请表                                                                                 6-3-5-3  徐州工业职业技术学院“双师型”教师申请汇总表', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2026, 'SG06030626', '6-3-6 产教虚拟教研室组织教研活动5次，其中校企活动2次，校校活动2次', 3,
     8, 11, 11, '组织教研活动5次，其中校企活动2次，校校活动2次', NULL, '通过产教虚拟教研室组织教研活动形成了 “跨域协同研讨 + 数字资源共享 + 产教成果转化” 的一体化教研新机制，覆盖了产教融合教研的全领域与关键群体，明确了产教融合教研的核心目标，预期组织教研活动5次，实现了教研活动从 “单一经验交流” 向 “产教协同创新” 转变，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、构建产教协同的教研体系奠定扎实基础。',
     '6-3-6-1  产教虚拟教研室校企活动材料(新闻报道+照片)                                                                                        6-3-6-2  产教虚拟教研室校企活动总结报告                                                                                              6-3-6-3  产教虚拟教研室校校活动材料(新闻报道+照片)                                                                                        6-3-6-4  产教虚拟教研室校校活动总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2026, 'SG06030726', '6-3-7 出台《“四阶”师资培养管理办法》', 3,
     1, 4, 4, '出台管理办法1个', NULL, '通过出台《“四阶”师资培养管理办法》形成了 “分阶精准赋能 + 全周期跟踪培养 + 多维度协同保障” 的师资建设闭环机制，覆盖了教师成长全周期与学校发展全领域，明确了师资培养与学校发展的核心目标及实施标准，预期出台《“四阶”师资培养管理办法》，实现了教师成长 “全链条赋能生态”，为学校深化教育教学改革、构建全周期师资培养体系、推进产教融合战略落地工作开展奠定扎实基础。',
     '6-3-7-1关于《徐州工业职业技术学院“四阶”师资培养管理办法》征求意见的通知                         6-3-7-2《徐州工业职业技术学院“四阶”师资培养管理办法》草案征求意见研讨会(附照片)                                                                        6-3-7-2 关于印发《徐州工业职业技术学院“四阶”师资培养管理办法》的通知           ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2026, 'SG06030826', '6-3-8 聘请产业教授1人', 3,
     1, 3, 3, ' 聘请产业教授1人', '省级及以上', '通过聘请产业教授形成了“产教协同 + 师资赋能 + 成果转化” 的立体化教育发展机制，覆盖了区域主导产业与学校特色专业，明确了产教融合与教育发展的核心目标，预期聘请省级产业教授1人，实现了教育资源与产业资源的深度融合，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-3-8-1 产业教授申报材料                                                   6-3-8-2 产业教授聘书
6-3-8-3  产业教授个人简介+照片        ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2026, 'SG06030926', '6-3-9 教师获省级教学技能竞赛4项，国家级1项', 3,
     8, 10, 10, '获教学技能竞赛5项', '省级及以上', '通过教师获省级及以上教学技能竞赛形成了 “以赛促教 + 成果辐射 + 能力进阶” 的教学质量提升闭环机制，覆盖了学校教学改革的全领域与全群体，明确了教学改革与教师发展的核心目标，预期获省级教学技能竞赛4项，国家级1项，实现了“竞赛赋能教学、教学反哺竞赛” 的良性生态，为学校深化课堂教学改革、建设高水平教学团队、培养具备创新思维与实践能力的高素质人才工作开展奠定扎实基础。',
     '6-3-9-1  教师获奖证书或表彰文件(省级) 4项                      6-3-9-2  教师获奖证书或表彰文件(国家级) 1项      ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2026, 'SG06031026', '6-3-10 组建高素质“双师型”师资队伍1个', 3,
     1, 1, 1, '组建“双师型”师资队伍1个', NULL, '通过组建高素质“双师型”师资队伍形成了 “校企协同育师 + 分层分类培养 + 动态考核激励” 的一体化建设机制，覆盖了产教融合与人才培养的全领域、全群体，明确了队伍建设与人才培养的核心目标及实施标准，预期组建高素质“双师型”师资队伍1个，实现了“双师型” 队伍建设从 “单一资格认定” 向 “能力持续迭代” 转变，为学校深化应用型人才培养改革、推进产教融合育人模式创新、建设高水平应用型院校工作开展奠定扎实基础',
     '6-3-10-1  关于印发《徐州工业职业技术学院“双师型”教师认定》的通知                                                      6-3-10-2  徐州工业职业技术学院“双师型”教师申请表                                                                              6-3-10-3  关于印发《徐州工业职业技术学院“双师型”教师认定名单》的通知', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2027, 'SG06031127', '6-3-11 产教虚拟教研室组织教研活动5次，其中校企活动2次，校校活动2次', 3,
     8, 11, 11, '组织教研活动5次，其中校企活动2次，校校活动2次', NULL, '通过产教虚拟教研室组织教研活动形成了 “跨域协同研讨 + 数字资源共享 + 产教成果转化” 的一体化教研新机制，覆盖了产教融合教研的全领域与关键群体，明确了产教融合教研的核心目标，预期 产教虚拟教研室组织教研活动5次，实现了教研活动从 “单一经验交流” 向 “产教协同创新” 转变，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、构建产教协同的教研体系奠定扎实基础。',
     '6-3-11-1  产教虚拟教研室校企活动材料(新闻报道+照片)                                                                                        6-3-11-2  产教虚拟教研室校企活动总结报告                                                                                              6-3-11-3  产教虚拟教研室校校活动材料(新闻报道+照片)                                                                                        6-3-11-4  产教虚拟教研室校校活动总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2027, 'SG06031227', '6-3-12 聘请产业教授1人', 3,
     1, 3, 3, ' 聘请产业教授1人', NULL, '通过聘请产业教授形成了“产教协同 + 师资赋能 + 成果转化” 的立体化教育发展机制，覆盖了区域主导产业与学校特色专业，明确了产教融合与教育发展的核心目标，预期聘请省级产业教授1人，实现了教育资源与产业资源的深度融合，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-3-12-1 产业教授申报材料                                                   6-3-12-2 产业教授聘书
6-3-12-3  产业教授个人简介+照片         ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2027, 'SG06031327', '6-3-13 教师获省级教学技能竞赛4项，国家级1项', 3,
     8, 10, 10, '获教学技能竞赛5项', '省级及以上', '通过教师获省级及以上教学技能竞赛形成了 “以赛促教 + 成果辐射 + 能力进阶” 的教学质量提升闭环机制，覆盖了学校教学改革的全领域与全群体，明确了教学改革与教师发展的核心目标，预期获省级教学技能竞赛4项，国家级1项，实现了“竞赛赋能教学、教学反哺竞赛” 的良性生态，为学校深化课堂教学改革、建设高水平教学团队、培养具备创新思维与实践能力的高素质人才工作开展奠定扎实基础。',
     '6-3-13-1  教师获奖证书或表彰文件(省级) 4项                   6-3-13-2  教师获奖证书或表彰文件(国家级) 1项      ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2027, 'SG06031427', '6-3-14 修订《“四阶”师资培养管理办法》', 3,
     1, 4, 4, '修订管理办法1个', NULL, '通过修订《“四阶”师资培养管理办法》形成了 “分阶精准赋能 + 全周期跟踪培养 + 多维度协同保障” 的师资建设闭环机制，覆盖了教师成长全周期与学校发展全领域，明确了师资培养与学校发展的核心目标及实施标准，预期修订《“四阶”师资培养管理办法》，实现了教师成长 “全链条赋能生态”，为学校深化教育教学改革、构建全周期师资培养体系、推进产教融合战略落地工作开展奠定扎实基础。',
     '6-3-14-1 关于《徐州工业职业技术学院“四阶”师资培养管理办法》修订稿征求意见的通知                                                         6-3-14-2 《徐州工业职业技术学院“四阶”师资培养管理办法》修订草案征求意见研讨会(附照片)                                                                      6-3-14-2关于印发《徐州工业职业技术学院“四阶”师资培养管理办法(修订)》的通知
"', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2027, 'SG06031527', '6-3-15 修订《“教师教学档案袋”制度管理办法》', 3,
     8, 9, 9, '修订管理办法1个', NULL, '通过修订《“教师教学档案袋”制度管理办法》形成了 “过程性记录 + 个性化发展 + 精准化评价” 的教师教学管理新机制，覆盖了教师教学全环节与全群体，明确了教师教学管理的核心目标与实施标准，预期 修订《“教师教学档案袋”制度管理办法》，实现了教师教学管理从 “终结性评价” 转向 “过程性与终结性相结合的评价”，为学校推进教师专业化发展、构建科学高效的师资管理体系工作开展奠定扎实基础。',
     '6-3-15-1 关于《“教师教学档案袋”制度管理办法》修订稿征求意见的通知                                                 6-3-15-2 《“教师教学档案袋”制度管理办法》修订草案征求意见研讨会(附照片)                                                                                   6-3-15-3 关于印发《徐州工业职业技术学院“教师教学档案袋”制度管理办法(修订)》的通知
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2028, 'SG06031628', '6-3-16 产教虚拟教研室组织教研活动5次，其中校企活动2次，校校活动2次', 3,
     8, 11, 11, '组织教研活动5次，其中校企活动2次，校校活动2次', NULL, '通过产教虚拟教研室组织教研活动形成了 “跨域协同研讨 + 数字资源共享 + 产教成果转化” 的一体化教研新机制，覆盖了产教融合教研的全领域与关键群体，明确了产教融合教研的核心目标，预期组织教研活动5次，实现了教研活动从 “单一经验交流” 向 “产教协同创新” 转变，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、构建产教协同的教研体系奠定扎实基础。',
     '6-3-16-1  产教虚拟教研室校企活动材料(新闻报道+照片)                                                                                        6-3-16-2  产教虚拟教研室校企活动总结报告                                                                                              6-3-16-3  产教虚拟教研室校校活动材料(新闻报道+照片)                                                                                        6-3-16-4  产教虚拟教研室校校活动总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2028, 'SG06031728', '6-3-17 聘请产业教授1人', 3,
     1, 3, 3, '聘请产业教授1人', '省级及以上', '通过聘请产业教授形成了“产教协同 + 师资赋能 + 成果转化” 的立体化教育发展机制，覆盖了区域主导产业与学校特色专业，明确了产教融合与教育发展的核心目标，预期聘请省级产业教授1人，实现了教育资源与产业资源的深度融合，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-3-17-1 产业教授申报材料                                                   6-3-17-2 产业教授聘书
6-3-17-3  产业教授个人简介+照片        ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2028, 'SG06031828', '6-3-18 教师获省级教学技能竞赛4项，国家级1项', 3,
     8, 10, 10, '获教学技能竞赛5项', '省级及以上', '通过教师获省级及以上教学技能竞赛形成了 “以赛促教 + 成果辐射 + 能力进阶” 的教学质量提升闭环机制，覆盖了学校教学改革的全领域与全群体，明确了教学改革与教师发展的核心目标，预期获省级教学技能竞赛4项，国家级1项，实现了“竞赛赋能教学、教学反哺竞赛” 的良性生态，为学校深化课堂教学改革、建设高水平教学团队、培养具备创新思维与实践能力的高素质人才工作开展奠定扎实基础。',
     '6-3-18-1  教师获奖证书或表彰文件(省级) 4项                   6-3-18-2  教师获奖证书或表彰文件(国家级) 1项      ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2028, 'SG06031928', '6-3-19 申报国家级团队1个', 3,
     1, 3, 3, '申报团队1个', '国家级', '通过申报国家级团队形成了“跨域协同攻关 + 高端资源聚合 + 战略成果转化” 的一体化团队运行机制，覆盖了国家战略领域与创新发展全链条，明确了战略目标与团队运行的核心标准，预期申报国家级团队1个，实现了团队成果贯穿 “基础研究 - 应用研究 - 产业落地” 全创新链条，为学校构建高层次科研创新体系、培养战略型创新人才工作开展奠定扎实基础。',
     '6-3-19-1 教育部关于国家级团队申报的通知                                                          6-3-19-2  国家级团队申报书                                                             6-3-19-3   学校关于推荐国家级团队的公示名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2029, 'SG06032029', '6-3-20 产教虚拟教研室组织教研活动5次，其中校企活动2次，校校活动2次', 3,
     8, 11, 11, '组织教研活动5次，其中校企活动2次，校校活动2次', NULL, '通过产教虚拟教研室组织教研活动形成了 “跨域协同研讨 + 数字资源共享 + 产教成果转化” 的一体化教研新机制，覆盖了产教融合教研的全领域与关键群体，明确了产教融合教研的核心目标，预期组织教研活动5次，，实现了教研活动从 “单一经验交流” 向 “产教协同创新” 转变，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、构建产教协同的教研体系奠定扎实基础。 ',
     '6-3-20-1  产教虚拟教研室校企活动材料(新闻报道+照片)                                                                                        6-3-20-2  产教虚拟教研室校企活动总结报告                                                                                              6-3-20-3  产教虚拟教研室校校活动材料(新闻报道+照片)                                                                                        6-3-20-4  产教虚拟教研室校校活动总结报告  ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2029, 'SG06032129', '6-3-21 聘请产业教授1人', 3,
     1, 3, 3, '聘请产业教授1人', '省级及以上', '通过聘请产业教授形成了“产教协同 + 师资赋能 + 成果转化” 的立体化教育发展机制，覆盖了区域主导产业与学校特色专业，明确了产教融合与教育发展的核心目标，预期聘请省级产业教授1人，实现了教育资源与产业资源的深度融合，为学校深化产教融合育人模式改革、建设高水平 “双师型” 教师队伍、培养适应产业需求的高素质应用型人才工作开展奠定扎实基础。',
     '6-3-22-1 产业教授申报材料                                                   6-3-22-2 产业教授聘书
6-3-22-3  产业教授个人简介+照片            ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 28, '0,6,28', 2029, 'SG06032229', '6-3-22 教师获省级教学技能竞赛4项', 3,
     8, 10, 10, '获教学技能竞赛4项', '省级及以上', '通过教师获省级教学技能竞赛形成了 “以赛促教 + 成果辐射 + 能力进阶” 的教学质量提升闭环机制，覆盖了学校教学改革的全领域与全群体，明确了教学改革与教师发展的核心目标，预期获省级教学技能竞赛4项，实现了“竞赛赋能教学、教学反哺竞赛” 的良性生态，为学校深化课堂教学改革、建设高水平教学团队、培养具备创新思维与实践能力的高素质人才工作开展奠定扎实基础。',
     '6-3-22-1  教师获奖证书或表彰文件(省级) 4项  ', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2025, 'SG07010125', '7-1-1 调研工程机械产业需求，形成调研报告1份；', 3,
     8, 13, 13, '调研报告1份', NULL, '通过开展工程机械产业需求调研，构建了“问题识别-数据采集-趋势研判”的科学决策机制，覆盖了产业技术技能人才需求的多个关键维度，有效避免了专业设置与市场脱节的问题，明确了实践教学体系优化的方向与重点，预期在专业设置前瞻性、人才培养针对性、产教融合精准性三个方面实现了从“经验判断”向“科学决策”的转变，提升了决策的科学性与人才培养的适配性。',
     '7-1-1-1 工程机械产业需求调查问卷
7-1-1-2 工程机械产业需求调研过程记录
7-1-1-3 工程机械产业需求调研报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2025, 'SG07010225', '7-1-2 撰写开放型实践中心论证报告1份；', 3,
     8, 13, 13, '开放型实践中心论证报告1份', NULL, '通过撰写开放型实践中心论证报告，形成了“功能定位-资源整合-运行模式”一体化的顶层设计机制，覆盖了中心建设的必要性、可行性与实施路径等全环节，有效整合了现有零散资源，避免重复建设，明确了其共享、开放、服务的核心定位与建设标准，预期在资源利用效率、社会服务能力、产教融合深度三个方面实现了从“校内实训”向“开放共享”的格局突破，为打造区域共享型实践基地提供了清晰、规范的行动蓝图。',
     '7-1-2-1 开放型实践中心调研报告
7-1-2-2 开放型实践中心建设方案初稿
7-1-2-3 开放型实践中心论证专家评审意见及相关会议记录
7-1-2-4 开放型实践中心论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2025, 'SG07010325', '7-1-3 基于工程机械典型零件生产流程，明确基地功能，对实践中心重新布局。', 3,
     8, 13, 13, '实践中心重新布局方案1套', NULL, '通过依据典型零件生产流程重新规划实践中心布局，形成了“流程导向、功能重构、空间优化”的实践环境建设机制，覆盖了从基础技能训练到综合生产实践的完整教学链，有效解决了现有布局与真实生产流程不符、效率低下的问题，明确了各功能区与生产环节的映射关系，预期在实训情境真实性、教学流程连贯性、学生综合工程素养培养三个方面实现了从“分散模块”向“一体化流程”的转型升级，大幅提升了实训效能与规范运行水平。',
     '7-1-3-1 工程机械典型零件生产流程调研报告
7-1-3-2 基地功能升级改造初步规划图及功能说明
7-1-3-3 基地功能升级改造专家评审意见及相关会议记录
7-1-3-3 实践中心重新布局方案
7-1-3-4 重新布局实施过程记录及验收报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2025, 'SG07010425', '7-1-4 【新增】立项省级基地1个；', 3,
     8, 13, 13, '省级基地立项文件1份', '省级及以上', '通过成功立项省级基地，形成了“项目驱动、资源汇聚、品牌提升”的平台建设加速机制，覆盖了基地建设与发展的关键资源要素，有效解决了资金来源单一、政策支持不足的问题，明确了省级平台的建设标准与绩效要求，预期在建设资金保障、政策支持力度、社会影响力三个方面实现了从“校级”向“省级”的跨越，为争取更高水平发展机遇创造了核心载体与先发优势，规范了基地建设路径。',
     '7-1-4-1 省级基地立项申请书
7-1-4-2 省级基地立项公示文件
7-1-4-3 省级基地建设任务书/计划书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2025, 'SG07010525', '7-1-5 【新增】验收完成省级校企合作典型生产实践项目。', 3,
     8, 13, 13, '验收报告1份', '省级及以上', '通过对省级校企合作典型生产实践项目总结凝练，形成了“目标达成-成果固化-模式推广”的项目闭环管理机制，有效解决了项目成果难以系统化、标准化的问题，明确了校企合作生产性实践的教学价值与质量标准，预期在项目示范效应、合作模式成熟度、可复制性三个方面实现了从“项目建设”向“模式输出”的成果转化，为深化校企协同育人提供了可借鉴的典型案例与实践范式，提升了项目管理效能与规范性。',
     '7-1-5-1 省级校企合作典型生产实践项目立项文件
7-1-5-2 省级校企合作典型生产实践项目成果材料
7-1-5-3 省级校企合作典型生产实践项目验收报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2026, 'SG07010626', '7-1-6 开展工业互联网、机器人等3个虚拟仿真实训基地调研和方案论证；', 3,
     8, 13, 13, '调研与论证报告1份', NULL, '通过开展前沿技术领域虚拟仿真实训基地的调研与论证，构建了“技术前瞻-需求分析-方案设计”的科学决策机制，覆盖了虚拟仿真技术在实践教学中应用的关键场景，有效避免了盲目投入和技术选型错误，明确了基地建设的必要性与可行性，预期在实训内容先进性、教学手段创新性、资源使用安全性三个方面实现了从“传统实训”向“虚实结合”的战略布局，为打造高水平虚拟仿真实训基地提供了权威依据与规范实施路径。',
     '7-1-6-1 虚拟仿真实训基地调研报告
7-1-6-2 虚拟仿真实训基地建设技术可行性分析报告
7-1-6-3 虚拟仿真实训基地论证报告及专家评审意见
7-1-6-4 虚拟仿真实训基地建设方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2026, 'SG07010726', '7-1-7 开展工程机械全球数智培训中心调研和方案论证；', 3,
     11, 22, 22, '调研与论证报告1份', NULL, '通过开展工程机械全球数智培训中心的系统调研与论证，形成了“国际视野-数智赋能-标准引领”的高端培训平台筹建机制，覆盖了培训中心的功能定位、技术架构与运行模式，有效解决了缺乏国际化、数智化高端培训平台的现状，明确了其服务全球、聚焦数智、培育高端人才的核心目标，预期在培训能力、国际影响力、行业服务层级三个方面实现了从“区域服务”向“全球赋能”的战略构想，为项目的高标准建设奠定了坚实的理论与规划基础，提升了前期规划的规范性与准确性。',
     '7-1-7-1 工程机械全球数智培训市场需求与分析报告
7-1-7-2 工程机械全球数智培训中心技术方案可行性报告
7-1-7-3 工程机械全球数智培训中心调研与论证报告及专家评审意见
7-1-7-4 建设规划与运营模式方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2026, 'SG07010826', '7-1-8 开发基于“基础、核心、拓展”实训模块；', 3,
     8, 13, 13, '实训模块1套', NULL, '通过开发“基础、核心、拓展”三级实训模块，形成了“分层递进、能力导向、个性发展”的模块化课程体系构建机制，覆盖了学生从入门到精通的全阶段能力成长路径，有效解决了现有课程体系单一、无法满足个性化培养需求的问题，明确了各模块的教学目标与能力标准，预期在教学的针对性、学生的选择性、人才培养的适应性三个方面实现了从“统一化教学”向“个性化培养”的模式转变，为实践教学改革的深化提供了核心课程资源支撑，大幅提升了教学效能。',
     '7-1-8-1 实训模块开发需求分析报告
7-1-8-2 实训模块开发方案及内容
7-1-8-3 实训模块测试验收报告及专家评审意见
7-1-8-4 实训模块在教学中应用情况反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2026, 'SG07010926', '7-1-9 凝练工程机械培训包1个、企业实践案例10个、典型生产实践项目10。', 3,
     8, 13, 13, '工程机械培训包1个、企业实践案例10个、典型生产实践项目10个', NULL, '通过系统凝练培训包、案例与项目，形成了“资源整合-知识沉淀-教学转化”的实践教学资源建设机制，覆盖了理论知识、实践技能与工程素养的综合培养需求，有效解决了现有教学资源碎片化、不成体系的问题，明确了资源的内容标准与教学应用规范，预期在教学资源丰富度、教学内容实效性、校企衔接紧密度三个方面实现了从“零散素材”向“系统化资源包”的集成升级，为开展高质量、高效率的实践教学提供了核心内容保障，提升了教学资源利用效能。',
     '7-1-9-1 工程机械培训包开发与凝练方案
7-1-9-2 企业实践案例和典型生产实践项目收集与筛选报告
7-1-9-3 工程机械培训包文档1个
7-1-9-4 企业实践案例10个及典型生产实践项目10个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2027, 'SG07011027', '7-1-10 开展工程机械关键零部件柔性生产线调研和方案论证；', 3,
     8, 13, 13, '调研与论证报告1份', NULL, '通过开展柔性生产线的调研与论证，构建了“技术跟踪-教学适配-方案优化”的先进制造装备引入决策机制，覆盖了生产线技术选型、教学化改造、与现有实训体系融合等关键问题，有效避免了设备引入后“水土不服”或与教学脱节的风险，明确了引入柔性生产线对提升学生现代制造系统认知与运维能力的目标，预期在实训技术先进性、复合型人才培养能力两个方面实现了从“单元技术”向“系统集成”的准备，为实践教学装备的升级换代提供了科学指引，保障了决策的规范性与投入效益。',
     '7-1-10-1 工程机械关键零部件柔性生产线技术调研报告
7-1-10-2 柔性生产线引入教学方案及可行性分析
7-1-10-3 工程机械关键零部件柔性生产线论证报告及专家意见
7-1-10-4 建设投资预算与效益评估报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2027, 'SG07011127', '7-1-11 新建3个虚拟仿真实训基地，升级5个实训室、新建3个；', 3,
     8, 13, 13, '新建3个虚拟仿真实训基地、升级5个实训室、新建3个实训室', NULL, '通过大规模新建与升级实训场所，形成了“虚实结合、软硬联动、功能互补”的实践教学条件扩容提质机制，覆盖了虚拟仿真、传统实训等多个教学层面，有效解决了实训容量不足、设备陈旧、技术滞后的问题，明确了各基地与实训室的功能定位与绩效目标，预期在实训容量、教学现代化水平、专业覆盖面三个方面实现了从“点状突破”向“系统提升”的格局性改善，为大规模培养高素质技术技能人才提供了坚实的物理空间与硬件保障，大幅提升了实训工作效能与规范运行。',
     '7-1-11-1 新建虚拟仿真实训基地建设方案3个
7-1-11-2 实训室升级改造方案5个、新建方案3个
7-1-11-3 虚拟仿真实训基地验收报告3份
7-1-11-4 实训室建设验收报告8份
7-1-11-5 建成实训室管理制度与使用手册', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2027, 'SG07011227', '7-1-12 新建工程机械全球数智培训中心1个；', 3,
     11, 22, 22, '工程机械全球数智培训中心1个', NULL, '通过成功新建工程机械全球数智培训中心，形成了“实体落地-功能实现-服务启动”的高端平台运行机制，覆盖了数智化培训、技术认证、国际交流等核心功能，有效解决了国内工程机械领域高端人才培训与国际接轨的空白，明确了其作为行业人才高地的发展定位，预期在培训能级、技术服务水平、品牌形象三个方面实现了从“规划蓝图”向“实体运营”的质的飞跃，为树立在工程机械数智化人才培养领域的全球影响力提供了核心支点，实现了高效、规范的平台运行。',
     '7-1-12-1 工程机械全球数智培训中心建设实施方案
7-1-12-2 工程机械全球数智培训中心设备调试与软件集成报告
7-1-12-3 工程机械全球数智培训中心验收报告及专家评审意见
7-1-12-4 培训中心运营管理制度与培训活动记录', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2027, 'SG07011327', '7-1-13 凝练工程机械培训包2个、企业实践案例20个、典型生产实践项目10。', 3,
     8, 13, 13, '工程机械培训包2个、企业实践案例20个、典型生产实践项目10个', NULL, '通过持续凝练与开发优质教学资源，形成了“迭代更新-系列化开发-品牌化塑造”的资源库持续丰富机制，覆盖了更多专业方向和技能领域，有效解决了现有资源更新慢、覆盖面窄的问题，明确了资源的质量标准与更新周期，预期在资源体系完整性、专业覆盖广度、教学支持力度三个方面实现了从“量的积累”向“质的品牌”的升级，为构建国内领先的工程机械教学资源库奠定了决定性基础，大幅提升了教学资源的利用效能与规范性。',
     '7-1-13-1 工程机械培训包开发与凝练方案
7-1-13-2 企业实践案例和典型生产实践项目补充收集与筛选报告
7-1-13-3 工程机械培训包文档2个
7-1-13-4 企业实践案例20个及典型生产实践项目10个
7-1-13-5 资源库建设与管理制度', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2027, 'SG07011427', '7-1-14 【新增】新建高端装备智能制造产教融合实践中心1个。', 3,
     8, 13, 13, '产教融合实践中心1个', NULL, '通过新建高端装备智能制造产教融合实践中心，形成了“高端引领-跨界融合-创新实践”的综合实践平台构建机制，覆盖了智能制造全技术链的教学、科研与培训，有效解决了高端智能制造人才培养与产业需求脱节、实践平台缺乏先进性的问题，明确了其在连接教育界与产业界的核心枢纽地位，预期在实践教学层次、技术研发能力、产教融合模式创新三个方面实现了从“跟随应用”向“前沿引领”的战略转型，为服务区域高端装备制造业发展提供了高能级平台，提升了产教融合的深度与效能。',
     '7-1-14-1 高端装备智能制造产教融合实践中心建设方案
7-1-14-2 高端装备智能制造产教融合实践中心验收报告及专家组意见
7-1-14-3 建设过程与管理记录
7-1-14-4 合作企业协议及中心运营管理制度', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2028, 'SG07011528', '7-1-15 新建或改造生产线1条，升级5个实训室、新建3个；', 3,
     8, 13, 13, '改造/新建生产线1条、升级5个实训室、新建3个实训室', NULL, '通过生产线与实训室的同步建设与升级，形成了“生产性实训-教学化改造-系统性升级”的实践教学环境协同优化机制，覆盖了真实生产环境与专业化实训空间的融合，有效解决了实训与实际生产脱节、设备老化的问题，明确了“教学工厂”的建设标准，预期在实训环境的真实性、技术应用的同步性、教学资源的利用率三个方面实现了从“模拟环境”向“准真实环境”的跨越，为学生提供更具沉浸感的工程实践体验，大幅提升了实训教学效能与规范性。',
     '7-1-15-1 生产线新建或改造方案1个
7-1-15-2 实训室升级方案5个和新建方案3个
7-1-15-3 生产线和实训室建设与改造过程记录
7-1-15-4 生产线和实训室验收报告
7-1-15-5 相关设备操作规程与安全管理制度', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2028, 'SG07011628', '7-1-16 组织申报“国家级‘双师型’教师培养培训基地；', 3,
     1, 38, 38, '申报材料1套', '国家级', '通过组织申报国家级“双师型”教师培养培训基地，形成了“对标国家级标准-整合优势资源-系统谋划申报”的品牌提升机制，覆盖了基地的师资、课程、条件、成果等核心申报要素，有效解决了现有师资培训基地层次不高、影响力有限的问题，明确了跻身国家级序列的战略目标，预期在基地定位、资源获取能力、师范引领作用三个方面实现了从“省级重点”向“国家级预备”的突破，为学校教师队伍建设工作赢得更高层次的发展平台，全面提升了申报工作的规范性与成功率。',
     '7-1-16-1 “国家级‘双师型’教师培养培训基地申报书
7-1-16-2 申报支撑材料
7-1-16-3 申报工作过程会议纪要、专家咨询意见', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2028, 'SG07011728', '7-1-17 凝练结构化的工程机械培训包2个、企业实践案例20个。【追加】：典型生产实践项目5个；', 3,
     8, 13, 13, '培训包2个、企业实践案例20个、典型生产实践项目5个', NULL, '通过凝练结构化的培训资源，形成了“模块化设计-结构化组合-个性化交付”的培训资源高端建构机制，覆盖了不同用户群体的定制化培训需求，有效解决了培训内容零散、难以根据需求快速组合的问题，明确了资源之间的逻辑关系与应用场景，预期在资源使用的灵活性、培训效果的针对性、服务市场的能力三个方面实现了从“标准化供给”向“个性化方案”的服务升级，增强了培训产品的核心竞争力，大幅提升了培训效能与规范性。',
     '7-1-17-1 结构化工程机械培训包设计方案
7-1-17-2 结构化的工程机械培训包文档2个
7-1-17-3 企业实践案例20个、典型生产实践项目5个
7-1-17-4 培训资源应用反馈与优化报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2028, 'SG07011828', '7-1-18 【新增】验收省级基地1个。', 3,
     8, 13, 13, '省级基地验收通过证明1份', '省级及以上', '通过顺利完成省级基地验收，形成了“建设-管理-验收-提升”的基地建设全生命周期管理闭环机制，覆盖了基地建设的各项任务与指标，有效检验并解决了建设过程中可能存在的问题与偏差，全面展示了建设成果，预期在基地运行的规范性、成果的显性化、持续发展的基础三个方面实现了从“过程建设”向“成果固化与认可”的成功转换，为基地下一阶段的可持续发展提供了官方认定与动力，大幅提升了基地建设的质量与效率。',
     '7-1-18-1 省级基地验收申请报告
7-1-18-2 省级基地验收专家评审意见及现场考察记录
7-1-18-3 省级基地建设成果汇报材料
7-1-18-4 省级基地验收通过证明文件或批复', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2029, 'SG07011929', '7-1-19 开展“工程机械典型零部件创新制作车间”实训室调研和方案论证；', 3,
     8, 13, 13, '室调研与论证报告1份', NULL, '开展创新制作车间的调研与论证，构建了“激发创意-设计实现-制作验证”的创新实践平台规划设计机制，覆盖了从创新思维培养到实物制作的全过程，有效解决了现有实训环节中创新实践能力培养不足的问题，明确了其在培养学生工程实践与创新能力方面的独特功能，预期在实践教学体系中对学生创新能力的培养实现了从“理论引导”向“实践赋能”的补充与强化，为培育创新型技术技能人才规划了专门化平台，提升了人才培养的前瞻性与规范性。',
     '7-1-19-1 “工程机械典型零部件创新制作车间”实训室需求与功能定位调研报告
7-1-19-2 创新制作车间技术方案及可行性分析
7-1-19-3 “工程机械典型零部件创新制作车间”实训室论证报告及专家意见
7-1-19-4 建设投资预算与设备配置初步方案', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 29, '0,7,29', 2029, 'SG07012029', '7-1-20 升级工业机器人等4个实训室。', 3,
     8, 13, 13, '升级4个实训室', NULL, '升级工业机器人等核心实训室，形成了“技术迭代-设备更新-教学升级”的实训条件动态优化机制，覆盖了工业自动化领域的关键技能训练点，有效解决了现有设备老化、技术过时，与产业发展脱节的问题，明确了实训内容与技术发展同步的目标，预期在实训设备的先进性、教学内容的时效性、与产业需求的契合度三个方面实现了从“保持同步”向“适度超前”的能级提升，确保了相关专业人才培养的质量优势，大幅提升了实训效能与规范运行水平。',
     '7-1-20-1 工业机器人等4个实训室升级改造方案
7-1-20-2 升级改造过程记录
7-1-20-3 工业机器人等4个实训室验收报告及使用反馈
7-1-20-4 升级后实训室教学大纲与实验指导书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2025, 'SG07020125', '7-2-1 开展远程实训平台可视化、信息化升级改造项目调研与论证；', 3,
     8, 13, 13, '调研与论证报告1份', NULL, '通过开展远程实训平台升级改造的调研与论证，形成了“需求分析-技术选型-可行性研判”的项目精准立项机制，覆盖了平台可视化、信息化及与教务系统对接等核心需求，有效解决了现有平台功能单一、数据孤立、管理不便的问题，明确了升级的技术路径与预期效益，预期在项目管理科学性、技术方案可靠性、投资效益最大化三个方面实现了从“主观决策”向“科学论证”的转变，为项目的成功实施提供了关键前提保障，提升了项目立项的规范性与准确性。',
     '7-2-1-1 远程实训平台现有问题与需求调研报告
7-2-1-2 可视化、信息化升级改造技术方案对比与选型报告
7-2-1-3 远程实训平台升级改造项目可行性论证报告及专家评审意见
7-2-1-4 与教务系统对接技术可行性分析报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2025, 'SG07020225', '7-2-2 开展实践教学评价体系的调研与论证。', 3,
     8, 13, 13, '调研与论证报告1份', NULL, '开展实践教学评价体系的调研与论证，构建了“问题诊断-理念创新-框架重构”的评价改革顶层设计机制，覆盖了过程性与结果性评价的关键要素，有效解决了现有评价体系单一、过程性评价不足、结果反馈不及时等问题，明确了构建数据驱动的新型评价体系的核心目标与原则，预期在评价的科学性、全面性、导向性三个方面实现了从“经验评价”向“数据赋能评价”的理念革新，为彻底改革实践教学评价模式奠定了理论基础，提升了评价体系的规范性与有效性。',
     '7-2-2-1 现有实践教学评价体系问题诊断报告
7-2-2-2 实践教学评价体系案例调研报告
7-2-2-3 实践教学评价体系改革方案初稿及专家论证意见
7-2-2-4 实践教学评价体系的调研报告以及可实施性论证', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2026, 'SG07020326', '7-2-3 新建远程实训平台可视化、信息化升级改造项目；', 3,
     8, 13, 13, '远程实训平台1个', NULL, '完成远程实训平台的升级改造，形成了“功能实现-体验优化-效率提升”的数字化实践教学平台建设机制，覆盖了实训管理、资源共享、过程监控等多方面功能，有效解决了原有平台无法满足现代化教学需求，管理效率低下的问题，明确了平台作为实践教学新基建的定位，预期在实训管理的便捷性、教学过程的透明度、资源服务的广域性三个方面实现了从“传统线下”向“线上线下融合”的模式变革，初步构建了“互联网+实践教学”的新形态，实现了平台的高效、规范运行。',
     '7-2-3-1 远程实训平台可视化、信息化升级改造项目详细设计方案
7-2-3-2 平台开发与系统集成过程记录
7-2-3-3 远程实训平台升级改造项目验收报告及用户试用反馈
7-2-3-4 平台操作手册与管理制度', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2026, 'SG07020426', '7-2-4 构建“数据可追溯、过程可监控、质量可评价”实践教学评价体系；', 3,
     8, 13, 13, '实践教学评价体系1套', NULL, '通过成功构建“三可”评价体系，形成了“数据采集-过程分析-质量反馈”的实践教学智能评价机制，覆盖了实践教学的全过程数据链，有效解决了传统评价主观性强、缺乏量化依据、无法及时反馈改进的问题，明确了以数据证据链支撑教学评价与改进的流程，预期在教学管理精细化、反馈改进及时性、评价结果客观性三个方面实现了从“模糊管理”向“精准治理”的系统性重塑，为实践教学质量的持续提升安装了“数据引擎”，保障了评价体系的科学性与规范性。',
     '7-2-4-1 “数据可追溯、过程可监控、质量可评价”实践教学评价体系方案
7-2-4-2 评价体系配套软件平台开发与部署报告
7-2-4-3 实践教学评价体系试运行报告
7-2-4-4 评价体系操作指南与培训记录', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2026, 'SG07020526', '7-2-5 省级及以上媒体报道2篇。', 3,
     7, 8, 8, '省级及以上媒体报道2篇', '省级及以上', '获得省级及以上媒体聚焦报道，形成了“成果提炼-宣传推广-形象塑造”的品牌影响力提升机制，覆盖了基地建设的关键成果与亮点，有效解决了基地影响力局限于校内或行业内部的问题，明确了通过媒体放大示范效应的传播路径，预期在社会知名度、公众认可度、品牌美誉度三个方面实现了从“行业内知晓”向“社会面关注”的有效拓展，为基地发展营造了良好的外部舆论环境，提升了宣传工作的规范性与传播效能。',
     '7-2-5-1 省级及以上媒体报道材料2篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2027, 'SG07020627', '7-2-6 持续建设远程实训平台可视化、信息化升级改造项目；', 3,
     8, 13, 13, '持续升级优化的远程实训平台1个', NULL, '通过开展平台的持续建设，形成了“迭代开发-功能拓展-性能优化”的平台可持续发展机制，覆盖了平台在稳定性、易用性、功能性方面的深化需求，有效解决了平台上线后可能出现的性能瓶颈、功能不足及用户新需求等问题，明确了打造区域领先的远程实训平台的目标，预期在平台的服务承载能力、用户粘性、技术先进性三个方面实现了从“可用”向“好用、愿用”的进阶，巩固和扩大了数字化实践教学改革成果，保障了平台的长期高效、规范运行。',
     '7-2-6-1 远程实训平台二期（持续）建设方案
7-2-6-2 平台持续建设过程报告与技术开发记录
7-2-6-3 平台持续建设阶段验收报告及用户反馈意见
7-2-6-4 平台维护与更新日志', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2027, 'SG07020727', '7-2-7 完善“数据可追溯、过程可监控、质量可评价”的实践教学评价体系；', 3,
     8, 13, 13, '完善的实践教学评价体系1套', NULL, '通过完善“三可”评价体系，形成了“实践检验-问题修复-版本升级”的评价体系优化机制，覆盖了体系在试运行中暴露的短板与不足，有效解决了初期体系可能存在的适用性、操作性或准确性问题，明确了其与教学管理流程深度融合的操作细则，预期在评价体系的适应性、可操作性、有效性三个方面实现了从“理论模型”向“成熟工具”的转化，确保了评价体系能够持续、稳定地发挥教学改进作用，提升了评价体系的效能与规范性。',
     '7-2-7-1 实践教学评价体系试运行问题汇总与分析报告
7-2-7-2 实践教学评价体系修订与完善报告
7-2-7-3 更新版实践教学评价体系文件
7-2-7-4 评价体系修订后师生培训记录及反馈', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2027, 'SG07020827', '7-2-8 省级及以上媒体报道3篇。', 3,
     7, 8, 8, '省级及以上媒体报道3篇', '省级及以上', '通过获得更高频次的省级及以上媒体报道，形成了“持续发声-热点延续-品牌深化”的立体化宣传机制，覆盖了基地建设的新进展与新成效，有效解决了品牌曝光不足，影响力未能持续提升的问题，持续向社会传递基地价值，预期在媒体关系、传播深度、品牌资产积累三个方面实现了从“单点曝光”向“系列化传播”的升级，进一步夯实了基地的公共品牌形象，持续提升了宣传工作的规范性与传播效能。',
     '7-2-8-1 省级及以上媒体报道材料3篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2028, 'SG07020928', '7-2-9 凝练“数据可追溯、过程可监控、质量可评价”的实践教学评价案例；', 3,
     8, 13, 13, '实践教学评价案例集1份', NULL, '通过凝练评价体系应用案例，形成了“实践沉淀-模式提炼-知识共享”的评价改革成果转化机制，覆盖了体系在不同场景下的应用过程与效果，有效解决了评价体系理论化，缺乏具体操作指导的问题，为同类院校提供了可复制的实践经验，预期在成果的显性化、经验的推广性、改革的引领性三个方面实现了从“内部应用”向“外部输出”的价值跃升，放大了基地在实践教学评价领域的示范引领作用，提升了案例凝练的规范性与成果转化效能。',
     '7-2-9-1 实践教学评价案例征集与筛选方案
7-2-9-2 实践教学评价案例集1份
7-2-9-3 案例集专家评审意见与推广计划
7-2-9-4 案例集发布与宣传材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2028, 'SG07021028', '7-2-10 省级及以上媒体报道2篇。', 3,
     7, 8, 8, '省级及以上媒体报道2篇', '省级及以上', '通过保持稳定的媒体曝光，形成了“常态宣传-成果巩固-影响长效”的品牌维护机制，有效解决了阶段性媒体曝光后热度消退、影响力下降的问题，持续向公众展示基地的稳健发展与持续贡献，预期在品牌认知的稳定性、社会影响力的持续性两个方面实现了从“高增长”向“高质量”的平稳过渡，确保了基地品牌热度的长效性，持续提升了宣传工作的规范性与传播效能。',
     '7-2-10-1 省级及以上媒体报道材料2篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2029, 'SG07021129', '7-2-11 推广“数据可追溯、过程可监控、质量可评价”的实践教学评价；', 3,
     8, 13, 13, '实践教学评价体系推广报告1份', NULL, '通过主动推广成熟的评价体系，形成了“成果输出-标准引领-生态构建”的辐射引领机制，覆盖了兄弟院校、合作企业等目标群体，有效解决了单校实践成果难以共享，行业内评价标准不一的问题，明确了推广的策略与路径，预期在行业影响力、话语权、合作机会三个方面实现了从“内部建设”向“外部输出”的角色转变，确立了基地在该领域的领先地位和标准制定者形象，大幅提升了推广工作的规范性与影响力。',
     '7-2-11-1 实践教学评价体系推广方案
7-2-11-2 推广活动记录
7-2-11-3 推广效果反馈与评估报告
7-2-11-4 实践教学评价体系推广报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 30, '0,7,30', 2029, 'SG07021229', '7-2-12 在市域联合体推广数字远程实训平台。', 3,
     8, 13, 13, '推广应用报告1份', NULL, '通过在市域联合体内推广数字远程实训平台，形成了“资源共享-服务辐射-协同发展”的跨校合作共赢机制，覆盖了联合体成员单位的实践教学需求，有效解决了市域内职业院校实训资源不均、数字化水平差异大的问题，明确了资源共享的规则与利益分配，预期在资源利用效率、联合体凝聚力、服务社会能力三个方面实现了从“独立运行”向“平台化赋能”的范式创新，有力推动了市域职业教育的均衡发展与质量提升，实现了平台推广的规范性与高效性。',
     '7-2-12-1 数字远程实训平台市域联合体推广方案
7-2-12-2 平台推广合作协议或意向书
7-2-12-3 平台在联合体院校应用数据报告及用户反馈
7-2-12-4 平台推广工作总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2025, 'SG07030125', '7-3-1 聚焦工程机械产业，与徐工集团联合成立产教融合理事会；', 3,
     11, 22, 22, '产教融合理事会成立文件及组织架构1套', NULL, '通过与龙头企业徐工集团联合成立产教融合理事会，形成了“战略协同-顶层设计-资源共享”的深度产教融合治理机制，覆盖了人才培养、技术创新、社会服务等合作领域，有效解决了校企合作碎片化、缺乏长效机制的问题，明确了理事会在决策、协调、监督中的核心作用，预期在校企合作的系统性、稳定性和战略性三个方面实现了从“微观项目合作”向“宏观战略共生”的根本性转变，为各项合作提供了强有力的组织保障，实现了产教融合的规范化与高效能运行。',
     '7-3-1-1 产教融合理事会章程及工作制度
7-3-1-2 产教融合理事会成立大会会议纪要及图片资料
7-3-1-3 产教融合理事会成员名单及组织架构文件
7-3-1-4 与徐工集团合作协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2025, 'SG07030225', '7-3-2 建立实训中心《实验室预约使用管理制度》；', 3,
     8, 13, 13, '《实验室预约使用管理制度》1份', NULL, '通过建立《实验室预约使用管理制度》，形成了“预约便捷-流程规范-资源高效”的实训资源精细化运营机制，覆盖了实验室使用的全流程，有效解决了实验室资源分配不均、使用冲突、利用率不高的问题，明确了各方权责与使用规范，预期在实验室利用率、管理效率、用户满意度三个方面实现了从“粗放管理”向“精益管理”的初步转型，为实践教学的有序开展提供了制度基础，保障了实训资源的高效、规范运行。',
     '7-3-2-1 实训中心实验室使用现状调研报告
7-3-2-2 《实验室预约使用管理制度》草案及意见征集记录
7-3-2-3 正式出台的《实验室预约使用管理制度》（含审批文件）
7-3-2-4 制度宣贯培训记录', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2025, 'SG07030325', '7-3-3 技能培训与职业体验数量4000人次；【追加】：技能培训与职业体验数量3000人次；', 3,
     11, 22, 22, '完成技能培训与职业体验7000人次', NULL, '通过大规模、多批次的技能培训与职业体验，形成了“项目化组织-流程化管理-目标化考核”的社会服务规模化运行机制，覆盖了在校学生与社会学员等多类群体，有效解决了社会培训服务缺乏系统性、规模效应不足的问题，巩固了基地作为区域技能传播与职业体验中心的功能定位，预期在服务规模稳定性、品牌影响力、社会效益显现度三个方面实现了从“单次活动”向“常态化、品牌化服务”的升级，持续为区域技能型社会建设贡献基础性力量，大幅提升了服务效能与规范性。',
     '7-3-3-1 技能培训与职业体验年度计划与实施方案
7-3-3-2 技能培训和职业体验相关协议或合作文件
7-3-3-3 培训与体验人员名单、签到记录及证书发放情况汇总
7-3-3-4 技能培训与职业体验活动资料
7-3-3-5 达成7000人次的统计说明报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2025, 'SG07030425', '7-3-4 面向市域联合体成员，职业技能评价1000人次，通过率≥85%；【追加】：接纳联合体院校学生实习实训1000人次；', 3,
     11, 22, 22, '完成职业技能评价1000人次（最终通过率≥85%），其余处也做相应修改），接纳学生实习实训1000人次', NULL, '通过为联合体提供评价与实训服务，形成了“标准输出-服务共享-质量认证”的联合体内部协同育人机制，覆盖了人才“培养-评价-使用”的关键环节，有效解决了市域内院校评价标准不一、实训资源短缺的问题，明确了基地在联合体中的公共服务平台定位，预期在联合体内部凝聚力、人才评价公信力、资源共享水平三个方面实现了从“形式联合”向“实质运行”的深入推进，增强了联合体的内生发展动力，大幅提升了服务效能与规范性。',
     '7-3-4-1 职业技能评价工作组织方案与评价标准
7-3-4-2 开展本校高级电工、制图员、车工等职业技能等级证书的培训与考核记录
7-3-4-3 联合体院校学生来校实习实训协议及接收方案
7-3-4-4 联合体院校学生实习实训名单、指导记录及考勤情况', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2025, 'SG07030525', '7-3-5 共享基地合作培育省级大赛获奖1项。【追加】：共享基地合作培育省级大赛获奖4项；', 3,
     8, 13, 13, '省级大赛获奖证书5项', '省级及以上', '通过共享基地合作培育并获得省级大赛奖项，形成了“资源开放-优势互补-成果共创”的赛教融合育人机制，覆盖了技能大赛与创新创业竞赛等多赛道，有效解决了单校备赛资源有限、竞赛成绩难以突破的问题，明确了以赛促教、以赛促学的目标，预期在竞赛水平、育人成果显性度、合作深度三个方面实现了从“个别获奖”向“批量产出”的跨越，证明了资源共享模式在培育拔尖人才方面的卓越成效，大幅提升了育人效能与规范性。',
     '7-3-5-1 大赛培育与合作计划
7-3-5-2 大赛辅导过程记录与选手集训方案
7-3-5-3 省级大赛获奖证书扫描件/照片5项', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2025, 'SG07030625', '7-3-6 【新增】接纳10所院校学习交流。', 3,
     2, 40, 40, '接待10所院校学习交流的记录与总结1份', NULL, '通过接纳多所院校学习交流，形成了“经验分享-成果展示-示范引领”的辐射效应产生机制，覆盖了院校管理的多个方面，有效解决了自身办学经验难以传播、示范作用发挥不足的问题，明确了基地作为示范点的社会责任，预期在基地的示范性、行业影响力、合作网络三个方面实现了从“内涵建设”向“外向辐射”的价值延伸，初步树立了在同类型院校中的标杆形象，提升了接待工作的规范性与传播效能。',
     '7-3-6-1 院校学习交流接待方案与流程
7-3-6-2 院校学习交流活动记录
7-3-6-4 接待10所院校学习交流的总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2026, 'SG07030726', '7-3-7 开发职业启蒙课程2个；', 3,
     8, 13, 13, '职业启蒙课程2个', NULL, '通过系统开发2个职业启蒙课程，形成了“内容标准-教学资源-实施路径”一体化的职业启蒙教育课程建设机制，覆盖了职业认知、兴趣激发与生涯规划等关键启蒙环节，有效解决了职业启蒙教育缺乏系统性、课程资源不足的问题，明确了课程在基础教育与职业教育衔接中的桥梁作用，预期在课程体系完整性、启蒙教育专业性、社会服务前置性三个方面实现了从“零散活动”向“课程化实施”的转变，为构建贯穿全周期的职业启蒙教育体系提供了核心课程载体，提升了教育效能与规范性。',
     '7-3-7-1 职业启蒙课程开发需求分析与定位报告
7-3-7-2 职业启蒙课程教材/课件2个
7-3-7-3 职业启蒙课程专家评审意见与试讲记录', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2026, 'SG07030826', '7-3-8 技能培训与职业体验数量4000人次；【追加】：技能培训与职业体验数量3000人次；', 3,
     11, 22, 22, '完成技能培训与职业体验7000人次', NULL, '通过大规模、多批次的技能培训与职业体验，形成了“项目化组织-流程化管理-目标化考核”的社会服务规模化运行机制，覆盖了在校学生与社会学员等多类群体，有效解决了社会培训服务缺乏系统性、规模效应不足的问题，巩固了基地作为区域技能传播与职业体验中心的功能定位，预期在服务规模稳定性、品牌影响力、社会效益显现度三个方面实现了从“单次活动”向“常态化、品牌化服务”的升级，持续为区域技能型社会建设贡献基础性力量，大幅提升了服务效能与规范性。',
     '7-3-8-1 技能培训与职业体验活动实施方案
7-3-8-2 技能培训与职业体验人员名单或签到记录汇总
7-3-8-3 技能培训与职业体验活动满意度调查与反馈
7-3-8-4 达成7000人次的统计说明报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2026, 'SG07030926', '7-3-9 面向市域联合体成员，职业技能评价1000人次，通过率≥85%；【追加】：接纳联合体院校学生实习实训1000人次；', 3,
     13, 42, 42, '完成职业技能评价1000人次（通过率≥85%），接纳学生实习实训1000人次', NULL, '通过为联合体提供评价与实训服务，形成了“标准输出-服务共享-质量认证”的联合体内部协同育人机制，覆盖了人才“培养-评价-使用”的关键环节，有效解决了市域内院校评价标准不一、实训资源短缺的问题，明确了基地在联合体中的公共服务平台定位，预期在联合体内部凝聚力、人才评价公信力、资源共享水平三个方面实现了从“形式联合”向“实质运行”的深入推进，增强了联合体的内生发展动力，大幅提升了服务效能与规范性。',
     '7-3-9-1 职业技能评价工作组织方案与考核细则
7-3-9-2 职业技能评价通过人员名单及成绩单
7-3-9-3 联合体院校学生实习实训接纳方案及名单
7-3-9-4 联合体院校合作协议或函件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2026, 'SG07031026', '7-3-10 共享基地合作培育省级大赛获奖1项。【追加】：共享基地合作培育省级大赛获奖4项；', 3,
     8, 13, 13, '省级大赛获奖证书5项', '省级及以上', '通过共享基地合作培育并获得省级大赛奖项，形成了“资源开放-优势互补-成果共创”的赛教融合育人机制，覆盖了技能大赛与创新创业竞赛等多赛道，有效解决了单校备赛资源有限、竞赛成绩难以突破的问题，明确了以赛促教、以赛促学的目标，预期在竞赛水平、育人成果显性度、合作深度三个方面实现了从“个别获奖”向“批量产出”的跨越，证明了资源共享模式在培育拔尖人才方面的卓越成效，大幅提升了育人效能与规范性。',
     '7-3-10-1 大赛培育与合作计划
7-3-10-2 大赛集训过程记录与学生参赛档案
7-3-10-3 省级大赛获奖证书扫描件/照片5项
7-3-10-4 合作培育成果总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2026, 'SG07031126', '7-3-11 【新增】接纳10所院校学习交流。', 3,
     2, 40, 40, '接待10所院校学习交流的记录与总结1份', NULL, '通过接纳多所院校学习交流，形成了“经验分享-成果展示-示范引领”的辐射效应产生机制，覆盖了院校管理的多个方面，有效解决了自身办学经验难以传播、示范作用发挥不足的问题，明确了基地作为示范点的社会责任，预期在基地的示范性、行业影响力、合作网络三个方面实现了从“内涵建设”向“外向辐射”的价值延伸，初步树立了在同类型院校中的标杆形象，提升了接待工作的规范性与传播效能。',
     '7-3-11-1 院校学习交流接待方案
7-3-11-2 学习交流活动记录
7-3-11-3 院校学习交流总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2027, 'SG07031227', '7-3-12 开发职业启蒙课程2个；', 3,
     8, 13, 13, '职业启蒙课程2个', NULL, '通过新一轮职业启蒙课程的开发，形成了“迭代优化-系列拓展-品牌塑造”的启蒙课程体系持续丰富机制，覆盖了新的职业领域或不同学段学生的认知特点，有效解决了现有启蒙课程内容单一、未能及时更新以适应新产业发展的问题，明确了课程内容的前沿性与适用性，预期在课程资源库丰富度、教育公平促进力、社会影响力三个方面实现了从“解决有无”向“提质创优”的迈进，为打造区域性职业启蒙教育品牌奠定了更坚实的基础，大幅提升了课程开发效能与规范性。',
     '7-3-12-1 职业启蒙课程教材/课件2个
7-3-12-2 课程试用反馈与专家评审意见
7-3-12-3 课程推广合作协议与实施记录', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2027, 'SG07031327', '7-3-13 技能培训与职业体验数量4000人次；【追加】：技能培训与职业体验数量3000人次；', 3,
     13, 42, 42, '完成技能培训与职业体验7000人次', NULL, '通过大规模、多批次的技能培训与职业体验，形成了“项目化组织-流程化管理-目标化考核”的社会服务规模化运行机制，覆盖了在校学生与社会学员等多类群体，有效解决了社会培训服务缺乏系统性、规模效应不足的问题，巩固了基地作为区域技能传播与职业体验中心的功能定位，预期在服务规模稳定性、品牌影响力、社会效益显现度三个方面实现了从“单次活动”向“常态化、品牌化服务”的升级，持续为区域技能型社会建设贡献基础性力量，大幅提升了服务效能与规范性。',
     '7-3-13-1 技能培训与职业体验年度计划与实施方案
7-3-13-2 技能培训与职业体验人员名单及签到记录汇总
7-3-13-3 达成7000人次的统计说明报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2027, 'SG07031427', '7-3-14 面向市域联合体成员，职业技能评价1000人次，通过率≥85%；【追加】：接纳联合体院校学生实习实训1000人次；', 3,
     13, 42, 42, '完成职业技能评价1000人次（通过率≥85%），接纳学生实习实训1000人次', NULL, '通过为联合体提供评价与实训服务，形成了“标准输出-服务共享-质量认证”的联合体内部协同育人机制，覆盖了人才“培养-评价-使用”的关键环节，有效解决了市域内院校评价标准不一、实训资源短缺的问题，明确了基地在联合体中的公共服务平台定位，预期在联合体内部凝聚力、人才评价公信力、资源共享水平三个方面实现了从“形式联合”向“实质运行”的深入推进，增强了联合体的内生发展动力，大幅提升了服务效能与规范性。',
     '7-3-14-1 职业技能评价工作组织方案与考核细则
7-3-14-2 职业技能评价通过人员名单及成绩单
7-3-14-3 联合体院校学生实习实训接纳方案及名单
7-3-14-4 联合体合作协议或函件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2027, 'SG07031527', '7-3-15 共享基地合作培育省级大赛获奖1项。【追加】：共享基地合作培育省级大赛获奖4项；', 3,
     8, 13, 13, '省级大赛获奖证书5项', '省级及以上', '通过共享基地合作培育并获得省级大赛奖项，形成了“资源开放-优势互补-成果共创”的赛教融合育人机制，覆盖了技能大赛与创新创业竞赛等多赛道，有效解决了单校备赛资源有限、竞赛成绩难以突破的问题，明确了以赛促教、以赛促学的目标，预期在竞赛水平、育人成果显性度、合作深度三个方面实现了从“个别获奖”向“批量产出”的跨越，证明了资源共享模式在培育拔尖人才方面的卓越成效，大幅提升了育人效能与规范性。',
     '7-3-15-1 大赛培育与合作计划
7-3-15-2 大赛集训过程记录与学生参赛档案
7-3-15-3 省级大赛获奖证书扫描件/照片5项
7-3-15-4 合作培育成果总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2027, 'SG07031627', '7-3-16 【新增】接纳10所院校学习交流。', 3,
     2, 40, 40, '接待10所院校学习交流的记录与总结1份', NULL, '通过接纳多所院校学习交流，形成了“经验分享-成果展示-示范引领”的辐射效应产生机制，覆盖了院校管理的多个方面，有效解决了自身办学经验难以传播、示范作用发挥不足的问题，明确了基地作为示范点的社会责任，预期在基地的示范性、行业影响力、合作网络三个方面实现了从“内涵建设”向“外向辐射”的价值延伸，初步树立了在同类型院校中的标杆形象，提升了接待工作的规范性与传播效能。',
     '7-3-16-1 院校学习交流接待方案
7-3-16-2 学习交流活动记录
7-3-16-3 院校学习交流总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2027, 'SG07031727', '7-3-17 开发职业启蒙课程1个；', 3,
     8, 13, 13, '职业启蒙课程1个', NULL, '通过针对性开发1个新的职业启蒙课程，形成了“精准定位-特色开发-小步快跑”的课程库动态补充与优化机制，覆盖了特定新兴产业或本地特色产业的人才早期培养需求，有效解决了现有启蒙课程未能及时覆盖新领域或满足特色需求的问题，体现了课程建设的前瞻性与灵活性，预期在课程体系的特色化、满足多样化需求的能力两个方面实现了从“全面覆盖”向“特色引领”的聚焦，使职业启蒙教育内容更具时代性和吸引力，提升了课程开发效能与规范性。',
     '7-3-17-1 职业启蒙课程教材/课件1个
7-3-17-2 课程专家评审意见与试讲记录
7-3-17-3 课程推广应用反馈报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2028, 'SG07031828', '7-3-18 技能培训与职业体验数量4000人次；【追加】：技能培训与职业体验数量3000人次；', 3,
     11, 22, 22, '完成技能培训与职业体验7000人次', NULL, '通过大规模、多批次的技能培训与职业体验，形成了“项目化组织-流程化管理-目标化考核”的社会服务规模化运行机制，覆盖了在校学生与社会学员等多类群体，有效解决了社会培训服务缺乏系统性、规模效应不足的问题，巩固了基地作为区域技能传播与职业体验中心的功能定位，预期在服务规模稳定性、品牌影响力、社会效益显现度三个方面实现了从“单次活动”向“常态化、品牌化服务”的升级，持续为区域技能型社会建设贡献基础性力量，大幅提升了服务效能与规范性。',
     '7-3-18-1 技能培训与职业体验年度计划与实施方案
7-3-18-2 技能培训与职业体验人员名单及签到记录汇总
7-3-18-3 达成7000人次的统计说明报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2028, 'SG07031928', '7-3-19 面向市域联合体成员，职业技能评价1000人次，通过率≥85%；【追加】：接纳联合体院校学生实习实训1000人次；', 3,
     13, 42, 42, '完成职业技能评价1000人次（通过率≥85%），接纳学生实习实训1000人次', NULL, '通过为联合体提供评价与实训服务，形成了“标准输出-服务共享-质量认证”的联合体内部协同育人机制，覆盖了人才“培养-评价-使用”的关键环节，有效解决了市域内院校评价标准不一、实训资源短缺的问题，明确了基地在联合体中的公共服务平台定位，预期在联合体内部凝聚力、人才评价公信力、资源共享水平三个方面实现了从“形式联合”向“实质运行”的深入推进，增强了联合体的内生发展动力，大幅提升了服务效能与规范性。',
     '7-3-19-1 职业技能评价工作组织方案与考核细则
7-3-19-2 职业技能评价通过人员名单及成绩单
7-3-19-3 联合体院校学生实习实训接纳方案及名单
7-3-19-4 联合体合作协议或函件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2028, 'SG07032028', '7-3-20 共享基地合作培育省级大赛获奖1项。【追加】：共享基地合作培育省级大赛获奖4项；', 3,
     8, 13, 13, '省级大赛获奖证书5项', '省级及以上', '通过共享基地合作培育并获得省级大赛奖项，形成了“资源开放-优势互补-成果共创”的赛教融合育人机制，覆盖了技能大赛与创新创业竞赛等多赛道，有效解决了单校备赛资源有限、竞赛成绩难以突破的问题，明确了以赛促教、以赛促学的目标，预期在竞赛水平、育人成果显性度、合作深度三个方面实现了从“个别获奖”向“批量产出”的跨越，证明了资源共享模式在培育拔尖人才方面的卓越成效，大幅提升了育人效能与规范性。',
     '7-3-20-1 大赛培育与合作计划
7-3-20-2 大赛集训过程记录与学生参赛档案
7-3-20-3 省级大赛获奖证书扫描件/照片5项
7-3-20-4 合作培育成果总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2028, 'SG07032128', '7-3-21 【新增】接纳10所院校学习交流。', 3,
     2, 40, 40, '接待10所院校学习交流的记录与总结1份', NULL, '通过接纳多所院校学习交流，形成了“经验分享-成果展示-示范引领”的辐射效应产生机制，覆盖了院校管理的多个方面，有效解决了自身办学经验难以传播、示范作用发挥不足的问题，明确了基地作为示范点的社会责任，预期在基地的示范性、行业影响力、合作网络三个方面实现了从“内涵建设”向“外向辐射”的价值延伸，初步树立了在同类型院校中的标杆形象，提升了接待工作的规范性与传播效能。',
     '7-3-21-1 院校学习交流接待方案
7-3-21-2 学习交流活动记录
7-3-21-3 院校学习交流总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2029, 'SG07032229', '7-3-22 技能培训与职业体验数量4000人次；【追加】：技能培训与职业体验数量3000人次；', 3,
     11, 22, 22, '完成技能培训与职业体验7000人次', NULL, '通过大规模、多批次的技能培训与职业体验，形成了“项目化组织-流程化管理-目标化考核”的社会服务规模化运行机制，覆盖了在校学生与社会学员等多类群体，有效解决了社会培训服务缺乏系统性、规模效应不足的问题，巩固了基地作为区域技能传播与职业体验中心的功能定位，预期在服务规模稳定性、品牌影响力、社会效益显现度三个方面实现了从“单次活动”向“常态化、品牌化服务”的升级，持续为区域技能型社会建设贡献基础性力量，大幅提升了服务效能与规范性。',
     '7-3-22-1 技能培训与职业体验年度计划与实施方案
7-3-22-2 技能培训与职业体验人员名单及签到记录汇总
7-3-22-3 达成7000人次的统计说明报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2029, 'SG07032329', '7-3-23 面向市域联合体成员，职业技能评价1000人次，通过率≥85%；【追加】：接纳联合体院校学生实习实训1000人次；', 3,
     13, 42, 42, '完成职业技能评价1000人次（通过率≥85%），接纳学生实习实训1000人次', NULL, '通过为联合体提供评价与实训服务，形成了“标准输出-服务共享-质量认证”的联合体内部协同育人机制，覆盖了人才“培养-评价-使用”的关键环节，有效解决了市域内院校评价标准不一、实训资源短缺的问题，明确了基地在联合体中的公共服务平台定位，预期在联合体内部凝聚力、人才评价公信力、资源共享水平三个方面实现了从“形式联合”向“实质运行”的深入推进，增强了联合体的内生发展动力，大幅提升了服务效能与规范性。',
     '7-3-23-1 职业技能评价工作组织方案与考核细则
7-3-23-2 职业技能评价通过人员名单及成绩单
7-3-23-3 联合体院校学生实习实训接纳方案及名单
7-3-23-4 联合体合作协议或函件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2029, 'SG07032429', '7-3-24 共享基地合作培育省级大赛获奖1项。【追加】：共享基地合作培育省级大赛获奖4项；', 3,
     8, 13, 13, '省级大赛获奖证书5项', '省级及以上', '通过共享基地合作培育并获得省级大赛奖项，形成了“资源开放-优势互补-成果共创”的赛教融合育人机制，覆盖了技能大赛与创新创业竞赛等多赛道，有效解决了单校备赛资源有限、竞赛成绩难以突破的问题，明确了以赛促教、以赛促学的目标，预期在竞赛水平、育人成果显性度、合作深度三个方面实现了从“个别获奖”向“批量产出”的跨越，证明了资源共享模式在培育拔尖人才方面的卓越成效，大幅提升了育人效能与规范性。',
     '7-3-24-1 大赛培育与合作计划
7-3-24-2 大赛集训过程记录与学生参赛档案
7-3-24-3 省级大赛获奖证书扫描件/照片5项
7-3-24-4 合作培育成果总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2029, 'SG07032529', '7-3-25 【新增】接纳10所院校学习交流；', 3,
     2, 40, 40, '接待10所院校学习交流的记录与总结1份', NULL, '通过接纳多所院校学习交流，形成了“经验分享-成果展示-示范引领”的辐射效应产生机制，覆盖了院校管理的多个方面，有效解决了自身办学经验难以传播、示范作用发挥不足的问题，明确了基地作为示范点的社会责任，预期在基地的示范性、行业影响力、合作网络三个方面实现了从“内涵建设”向“外向辐射”的价值延伸，初步树立了在同类型院校中的标杆形象，提升了接待工作的规范性与传播效能。',
     '7-3-25-1 院校学习交流接待方案
7-3-25-2 学习交流活动记录
7-3-25-3 院校学习交流总结报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 31, '0,7,31', 2029, 'SG07032629', '7-3-26 【新增】累计助推2家企业成为专精特新“小巨人”或瞪羚企业等。', 3,
     11, 22, 22, '助推2家企业成为专精特新“小巨人”或瞪羚企业的相关证明', NULL, '通过助推2家企业成功跻身专精特新“小巨人”或瞪羚企业行列，形成了“人才支撑-技术服务和-生态赋能”的职业教育反哺产业升级价值实现机制，覆盖了校企合作价值链的高端环节，有效解决了校企合作停留在表层、未能深度赋能产业发展的问题，实证了产教融合对实体经济发展的强大推动力，预期在基地的社会价值认同、合作层次能级、自身可持续发展根基三个方面实现了从“辅助支持”向“战略赋能”的根本性转变，确立了基地在区域产业创新生态中不可或缺的关键节点地位，大幅提升了产教融合的效能与规范性。',
     '7-3-26-1 企业培育与助推合作记录
7-3-26-2 相关企业获认定官方文件或证书2家
7-3-26-3 基地为企业提供的支持证明
7-3-26-4 助推成果总结报告与企业反馈', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2025, 'SG08010125', '8-1-1开展工程机械产业数字化和数字产业化发展需求调研；', 3,
     8, 12, 12, '工程机械产业数字化和数字产业化发展需求调研报告', NULL, '通过多维度调研方法，形成涵盖产业数字化与数字产业化双路径的发展需求体系，覆盖研发设计、生产制造、经营管理、后市场服务等全价值链环节，明确技术升级、数据价值化、生态构建等核心发展需求，预期在提升产业效率、催生新增长模式、构建数字生态方面实现突破。',
     '8-1-1-1工程机械产业数字化和数字产业化发展需求企业调查问卷；
8-1-1-2工程机械产业数字化和数字产业化发展需求调研报告；
8-1-1-3 访谈过程材料。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2025, 'SG08010225', '8-1-2新建3门线上理论试题库；', 3,
     8, 19, 19, '新建3门线上理论试题库', NULL, '通过系统规划与开发数字化教学资源，形成标准化的线上理论试题库体3门，覆盖相关课程的核心知识点与能力考核点，明确试题库在支撑常态化过程评价与教学诊断中的应用方向，预期在提升考核效率、实现教考分离及促进个性化学习方面实现有效突破。',
     '8-1-2-1 《机械制造及自动化专业群 3 门线上理论试题库建设方案》（明确试题库对应课程名称，标注与企业岗位核心知识点的匹配关系，含题型分布、难度梯度设计）；
8-1-2-2  3 门课程线上理论试题库本体（含选择、判断、简答、计算等题型，每门课试题量不少于 300 道，附标准答案及评分标准，试题需经徐工技术专家审核签字）；
8-1-2-3《线上理论试题库校企审核纪要》（记录企业技术团队对试题与企业岗位需求适配性的审核意见、修改建议及最终确认结果）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2025, 'SG08010325', '8-1-3新建3门线上实训试题库；', 3,
     8, 19, 19, '新建3门线上实训试题库', NULL, '通过系统规划与开发线上实践教学资源，形成新一代数字化实训试题库3门，覆盖多个关键专业领域的核心技能考点，明确标准化、模块化与自适应学习的建设方向，预期在提升实训效率、拓展训练时空及优化教学评价方面实现显著成效',
     '8-1-3-1《机械制造及自动化专业群 3 门线上实训试题库开发方案》（明确对应实训课程，界定试题类型为 “虚拟仿真操作题 + 实操视频分析题 + 任务单式考核题”）
8-1-3-2 3 门课程线上实训试题库本体（每门课含不少于 10 个实训任务题，附真实设备操作视频素材、虚拟仿真操作脚本、实训考核评分细则， 实训题需包含设备参数、操作步骤规范）；
8-1-3-3《线上实训试题库校企联合开发记录》（含企业实训导师参与试题设计的会议记录、实操标准确认文件）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2025, 'SG08010425', '8-1-4专业核心课程线上考试覆盖50%；', 3,
     8, 19, 19, '专业核心课程线上考试覆盖50%；', NULL, '通过推进专业核心课程线上考试覆盖形成线上考核运行机制，包含自动组卷、实时阅卷、成绩分析 3 项核心功能；覆盖了机械制造及自动化专业群，各专业50%核心课程，涉及 各专业学生的学期考核；明确了线上考试的题型占比、时长设置、防作弊规则等考核标准。',
     '8-1-4-1《机械制造及自动化专业群核心课程清单及线上考试覆盖计划表》（明确专业核心课程总数，标注 50% 覆盖课程名称，含每门课线上考试的实施学期、平台选择等）；
8-1-4-2 5 门线上考试课程的《线上考试实施方案》（含考试时长、题型占比、防作弊设置、成绩录入规则，对接徐工岗位能力评价维度；
8-1-4-3 《线上考试实施过程记录表》（按学期记录每门课线上考试的参与人数、通过率、成绩分布，附学生答题数据分析）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2025, 'SG08010525', '8-1-5人工智能技术融入专业课堂占比达到20%。', 3,
     8, 10, 10, '人工智能技术融入专业课堂占比达到20%', NULL, '通过系统化推进人工智能技术与专业教学的深度融合，形成智能化课程教学新范式，覆盖超过20%的专业课程与核心教学环节，明确技术赋能、模式创新与素养提升的可行路径，预期在规模化因材施教、提升人才培养质量与适应性方面实现关键突破。',
     '8-1-5-1《机械制造及自动化专业群人工智能技术课堂融入实施方案》（明确 “20% 占比” 的计算标准：按专业核心课程总课时计，AI 融入课时占比不低于 20%）
8-1-5-2《AI 技术融入专业课堂课程清单及教学设计》（每门融入 AI 的课程需含 AI 教学模块教案、课件；
8-1-5-3《AI 技术融入课堂效果评估材料》（含学生 AI 教学满意度问卷、校企联合教学质量测评报告，分析 AI 融入后学生对知识的掌握能力、设备操作能力、技术理解度的提升数据）。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2026, 'SG08010626', '8-1-6新建3门线上理论试题库；', 3,
     8, 19, 19, '新建3门线上理论试题库', NULL, '通过系统规划与开发数字化教学资源，形成标准化的线上理论试题库体3门，覆盖相关课程的核心知识点与能力考核点，明确试题库在支撑常态化过程评价与教学诊断中的应用方向，预期在提升考核效率、实现教考分离及促进个性化学习方面实现有效突破。',
     '8-1-6-1 《机械制造及自动化专业群 3 门线上理论试题库建设方案》（明确试题库对应课程名称，标注与企业岗位核心知识点的匹配关系，含题型分布、难度梯度设计）；
8-1-6-2  3 门课程线上理论试题库本体（含选择、判断、简答、计算等题型，每门课试题量不少于 300 道，附标准答案及评分标准，试题需经徐工技术专家审核签字）；
8-1-6-3《线上理论试题库校企审核纪要》（记录徐工技术团队对试题与企业岗位需求适配性的审核意见、修改建议及最终确认结果）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2026, 'SG08010726', '8-1-7新建3门线上实训试题库；', 3,
     8, 19, 19, '新建3门线上实训试题库', NULL, '通过系统规划与开发线上实践教学资源，形成新一代数字化实训试题库3门，覆盖多个关键专业领域的核心技能考点，明确标准化、模块化与自适应学习的建设方向，预期在提升实训效率、拓展训练时空及优化教学评价方面实现显著成效',
     '8-1-7-1《机械制造及自动化专业群 3 门线上实训试题库开发方案》（明确对应实训课程，界定试题类型为 “虚拟仿真操作题 + 实操视频分析题 + 任务单式考核题”）
8-1-7-2 3 门课程线上实训试题库本体（每门课含不少于 10 个实训任务题，附真实设备操作视频素材、虚拟仿真操作脚本、实训考核评分细则， 实训题需包含设备参数、操作步骤规范）；
8-1-7-3《线上实训试题库校企联合开发记录》（含徐工实训导师参与试题设计的会议记录、实操标准确认文件）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2026, 'SG08010826', '8-1-8专业核心课程线上考试覆盖70%；', 3,
     8, 19, 19, '专业核心课程线上考试覆盖70%', NULL, '通过系统推进考核模式的数字化改革，形成线上线下相结合的多元化考核新范式，覆盖了70%以上的专业核心课程，明确线上考试作为标准化、常态化考核方式的重要定位，预期在提升考核效率、确保评价公正及赋能教学数据闭环管理方面实现显著成效。',
     '8-1-4-1《机械制造及自动化专业群核心课程清单及线上考试覆盖计划表》（明确专业核心课程总数，标注 70% 覆盖课程名称，含每门课线上考试的实施学期、平台选择等）；
8-1-4-2 5 门线上考试课程的《线上考试实施方案》（含考试时长、题型占比、防作弊设置、成绩录入规则，对接徐工岗位能力评价维度；
8-1-4-3 《线上考试实施过程记录表》（按学期记录每门课线上考试的参与人数、通过率、成绩分布，附学生答题数据分析）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2026, 'SG08010926', '8-1-9人工智能技术融入专业课堂占比达到25%。', 3,
     8, 10, 10, '人工智能技术融入专业课堂占比达到25%', NULL, '通过系统化推进人工智能技术与专业教学的深度融合，形成智能化课程教学新范式，覆盖了超过25%的专业课程与核心教学环节，明确技术赋能、模式创新与素养提升的可行路径，预期在规模化因材施教、提升人才培养质量与适应性方面实现关键突破。',
     '8-1-5-1《机械制造及自动化专业群人工智能技术课堂融入实施方案》（明确 “25% 占比” 的计算标准：按专业核心课程总课时计，AI 融入课时占比不低于 20%）
8-1-5-2《AI 技术融入专业课堂课程清单及教学设计》（每门融入 AI 的课程需含 AI 教学模块教案、课件；
8-1-5-3《AI 技术融入课堂效果评估材料》（含学生 AI 教学满意度问卷、校企联合教学质量测评报告，分析 AI 融入后学生对知识的掌握能力、设备操作能力、技术理解度的提升数据）。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2027, 'SG08011027', '8-1-10新建3门线上理论试题库；', 3,
     8, 19, 19, '新建3门线上理论试题库', NULL, '通过系统规划与开发数字化教学资源，形成标准化的线上理论试题库体3门，覆盖相关课程的核心知识点与能力考核点，明确试题库在支撑常态化过程评价与教学诊断中的应用方向，预期在提升考核效率、实现教考分离及促进个性化学习方面实现有效突破。',
     '8-1-10-1 《机械制造及自动化专业群 3 门线上理论试题库建设方案》（明确试题库对应课程名称，标注与企业岗位核心知识点的匹配关系，含题型分布、难度梯度设计）；
8-1-10-2  3 门课程线上理论试题库本体（含选择、判断、简答、计算等题型，每门课试题量不少于 300 道，附标准答案及评分标准，试题需经徐工技术专家审核签字）；
8-1-10-3《线上理论试题库校企审核纪要》（记录徐工技术团队对试题与企业岗位需求适配性的审核意见、修改建议及最终确认结果）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2027, 'SG08011127', '8-1-11新建3门线上实训试题库；', 3,
     8, 19, 19, '新建3门线上实训试题库', NULL, '通过系统规划与开发线上实践教学资源，形成新一代数字化实训试题库3门，覆盖多个关键专业领域的核心技能考点，明确标准化、模块化与自适应学习的建设方向，预期在提升实训效率、拓展训练时空及优化教学评价方面实现显著成效',
     '8-1-11-1《机械制造及自动化专业群 3 门线上实训试题库开发方案》（明确对应实训课程，界定试题类型为 “虚拟仿真操作题 + 实操视频分析题 + 任务单式考核题”）
8-1-11-2 3 门课程线上实训试题库本体（每门课含不少于 10 个实训任务题，附真实设备操作视频素材、虚拟仿真操作脚本、实训考核评分细则， 实训题需包含设备参数、操作步骤规范）；
8-1-11-3《线上实训试题库校企联合开发记录》（含徐工实训导师参与试题设计的会议记录、实操标准确认文件）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2027, 'SG08011227', '8-1-12专业核心课程线上考试覆盖80%；', 3,
     8, 19, 19, '专业核心课程线上考试覆盖80%', NULL, '
通过系统推进考核模式的数字化改革，形成线上线下相结合的多元化考核新范式，新增考试内容与行业技术同步更新的规则；覆盖80%以上的专业核心课程，明确线上考试结果与职业技能等级认证挂钩的标准，预期在提升考核效率、确保评价公正及赋能教学数据闭环管理方面实现显著成效。',
     '8-1-12-1《机械制造及自动化专业群核心课程清单及线上考试覆盖计划表》（明确专业核心课程总数，标注 80% 覆盖课程名称，含每门课线上考试的实施学期、平台选择等）；
8-1-12-2 5 门线上考试课程的《线上考试实施方案》（含考试时长、题型占比、防作弊设置、成绩录入规则，对接徐工岗位能力评价维度；
8-1-12-3 《线上考试实施过程记录表》（按学期记录每门课线上考试的参与人数、通过率、成绩分布，附学生答题数据分析）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2027, 'SG08011327', '8-1-13人工智能技术融入专业课堂占比达到30%；', 3,
     8, 10, 10, '人工智能技术融入专业课堂占比达到30%', NULL, '通过系统化推进人工智能技术与专业教学的深度融合，形成智能化课程教学新范式，覆盖超过30%的专业课程与核心教学环节，明确技术赋能、模式创新与素养提升的可行路径，预期在规模化因材施教、提升人才培养质量与适应性方面实现关键突破。',
     '8-1-13-1《机械制造及自动化专业群人工智能技术课堂融入实施方案》（明确 “30% 占比” 的计算标准：按专业核心课程总课时计，AI 融入课时占比不低于 30%）
8-1-13-2《AI 技术融入专业课堂课程清单及教学设计》（每门融入 AI 的课程需含 AI 教学模块教案、课件；
8-1-13-3《AI 技术融入课堂效果评估材料》（含学生 AI 教学满意度问卷、校企联合教学质量测评报告，分析 AI 融入后学生对知识的掌握能力、设备操作能力、技术理解度的提升数据）。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2028, 'SG08011428', '8-1-14新建3门线上理论试题库；', 3,
     8, 19, 19, '新建3门线上理论试题库', NULL, '系统规划与开发数字化教学资源，形成标准化的线上理论试题库体3门，覆盖相关课程的核心知识点与能力考核点，明确试题库在支撑常态化过程评价与教学诊断中的应用方向，预期在提升考核效率、实现教考分离及促进个性化学习方面实现有效突破。',
     '8-1-14-1 《机械制造及自动化专业群 3 门线上理论试题库建设方案》（明确试题库对应课程名称，标注与企业岗位核心知识点的匹配关系，含题型分布、难度梯度设计）；
8-1-14-2  3 门课程线上理论试题库本体（含选择、判断、简答、计算等题型，每门课试题量不少于 300 道，附标准答案及评分标准，试题需经徐工技术专家审核签字）；
8-1-14-3《线上理论试题库校企审核纪要》（记录徐工技术团队对试题与企业岗位需求适配性的审核意见、修改建议及最终确认结果）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2028, 'SG08011528', '8-1-15新建3门线上实训试题库；', 3,
     8, 19, 19, '新建3门线上实训试题库', NULL, '通过系统规划与开发线上实践教学资源，形成新一代数字化实训试题库3门，覆盖多个关键专业领域的核心技能考点，明确标准化、模块化与自适应学习的建设方向，预期在提升实训效率、拓展训练时空及优化教学评价方面实现显著成效',
     '8-1-15-1《机械制造及自动化专业群 3 门线上实训试题库开发方案》（明确对应实训课程，界定试题类型为 “虚拟仿真操作题 + 实操视频分析题 + 任务单式考核题”）
8-1-15-2 3 门课程线上实训试题库本体（每门课含不少于 10 个实训任务题，附真实设备操作视频素材、虚拟仿真操作脚本、实训考核评分细则， 实训题需包含设备参数、操作步骤规范）；
8-1-15-3《线上实训试题库校企联合开发记录》（含徐工实训导师参与试题设计的会议记录、实操标准确认文件）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2028, 'SG08011628', '8-1-16专业核心课程线上考试覆盖90%；', 3,
     8, 19, 19, '专业核心课程线上考试覆盖90%；', NULL, '
通过推进线上考试覆盖形成了全流程线上考核闭环，新增考试结果数据分析与教学改进联动机制，覆盖了90%以上的专业核心课程，预期在提升考核效率、确保评价公正及赋能教学数据闭环管理方面实现显著成效。"',
     '8-1-16-1《机械制造及自动化专业群核心课程清单及线上考试覆盖计划表》（明确专业核心课程总数，标注 90% 覆盖课程名称，含每门课线上考试的实施学期、平台选择等）；
8-1-16-2 5 门线上考试课程的《线上考试实施方案》（含考试时长、题型占比、防作弊设置、成绩录入规则，对接徐工岗位能力评价维度；
8-1-16-3 《线上考试实施过程记录表》（按学期记录每门课线上考试的参与人数、通过率、成绩分布，附学生答题数据分析）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2028, 'SG08011728', '8-1-17人工智能技术融入专业课堂占比达到35%。', 3,
     8, 10, 10, '人工智能技术融入专业课堂占比达到35%。', NULL, '通过系统化推进人工智能技术与专业教学的深度融合，形成了智能化课程教学新范式，覆盖了超过35%的专业课程与核心教学环节，明确了技术赋能、模式创新与素养提升的可行路径，预期在规模化因材施教、提升人才培养质量与适应性方面实现关键突破。',
     '8-1-17-1《机械制造及自动化专业群人工智能技术课堂融入实施方案》（明确 “25% 占比” 的计算标准：按专业核心课程总课时计，AI 融入课时占比不低于 20%）
8-1-17-2《AI 技术融入专业课堂课程清单及教学设计》（每门融入 AI 的课程需含 AI 教学模块教案、课件；
8-1-17-3《AI 技术融入课堂效果评估材料》（含学生 AI 教学满意度问卷、校企联合教学质量测评报告，分析 AI 融入后学生对知识的掌握能力、设备操作能力、技术理解度的提升数据）。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2029, 'SG08011829', '8-1-18新建3门线上理论试题库；', 3,
     8, 19, 19, '新建3门线上理论试题库；', NULL, '系统规划与开发数字化教学资源，形成了标准化的线上理论试题库体3门，覆盖了相关课程的核心知识点与能力考核点，明确了试题库在支撑常态化过程评价与教学诊断中的应用方向，预期在提升考核效率、实现教考分离及促进个性化学习方面实现有效突破。',
     '8-1-18-1 《机械制造及自动化专业群 3 门线上理论试题库建设方案》（明确试题库对应课程名称，标注与企业岗位核心知识点的匹配关系，含题型分布、难度梯度设计）；
8-1-18-2  3 门课程线上理论试题库本体（含选择、判断、简答、计算等题型，每门课试题量不少于 300 道，附标准答案及评分标准，试题需经徐工技术专家审核签字）；
8-1-18-3《线上理论试题库校企审核纪要》（记录徐工技术团队对试题与企业岗位需求适配性的审核意见、修改建议及最终确认结果）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2029, 'SG08011929', '8-1-19新建3门线上实训试题库；', 3,
     8, 19, 19, '新建3门线上实训试题库；', NULL, '系统规划与开发线上实践教学资源，形成了新一代数字化实训试题库3门，覆盖了多个关键专业领域的核心技能考点，明确了标准化、模块化与自适应学习的建设方向，预期在提升实训效率、拓展训练时空及优化教学评价方面实现显著成效',
     '8-1-19-1《机械制造及自动化专业群 3 门线上实训试题库开发方案》（明确对应实训课程，界定试题类型为 “虚拟仿真操作题 + 实操视频分析题 + 任务单式考核题”）
8-1-19-2 3 门课程线上实训试题库本体（每门课含不少于 10 个实训任务题，附真实设备操作视频素材、虚拟仿真操作脚本、实训考核评分细则， 实训题需包含设备参数、操作步骤规范）；
8-1-19-3《线上实训试题库校企联合开发记录》（含徐工实训导师参与试题设计的会议记录、实操标准确认文件）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2029, 'SG08012029', '8-1-20专业核心课程线上考试全覆盖；', 3,
     8, 19, 19, '专业核心课程线上考试全覆盖；', NULL, '推进线上考试全覆盖形成了标准化线上考核生态，包含考试、评价、改进的全流程机制；覆盖全部 专业核心课程；明确了线上考试为唯一考核形式的标准。',
     '8-1-20-1《机械制造及自动化专业群核心课程清单及线上考试覆盖计划表》（明确专业核心课程总数，标注 100% 覆盖课程名称，含每门课线上考试的实施学期、平台选择等）；
8-1-20-2 线上考试课程的《线上考试实施方案》（含考试时长、题型占比、防作弊设置、成绩录入规则，对接徐工岗位能力评价维度；
8-1-20-3 《线上考试实施过程记录表》（按学期记录每门课线上考试的参与人数、通过率、成绩分布，附学生答题数据分析）；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 32, '0,8,32', 2029, 'SG08012129', '8-1-21人工智能技术融入专业课堂占比达到40%', 3,
     8, 10, 10, '人工智能技术融入专业课堂占比达到40%', NULL, '系统化推进人工智能技术与专业教学的深度融合，形成了智能化课程教学新范式，覆盖了超过40%的专业课程与核心教学环节，明确了技术赋能、模式创新与素养提升的可行路径，预期在规模化因材施教、提升人才培养质量与适应性方面实现关键突破。',
     '8-1-21-1《机械制造及自动化专业群人工智能技术课堂融入实施方案》（明确 “25% 占比” 的计算标准：按专业核心课程总课时计，AI 融入课时占比不低于 20%）
8-1-21-2《AI 技术融入专业课堂课程清单及教学设计》（每门融入 AI 的课程需含 AI 教学模块教案、课件；
8-1-21-3《AI 技术融入课堂效果评估材料》（含学生 AI 教学满意度问卷、校企联合教学质量测评报告，分析 AI 融入后学生对知识的掌握能力、设备操作能力、技术理解度的提升数据）。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2025, 'SG08020125', '8-2-1完善液压与气动等3门数字化在线课程教学资源；', 3,
     8, 12, 12, '完善液压与气动等3门数字化在线课程教学资源', NULL, '推进 “液压与气动”“机械基础”“电工电子技术” 3 门数字化在线课程资源完善，形成了包含高清授课视频、虚拟仿真实验演示、课后习题集的标准化资源包；',
     '8-2-1-1《液压与气动等 3 门数字化在线课程资源完善方案》（明确 3 门课程具体名称，标注需补充的资源类型；
8-2-1-2 3 门课程完善后的数字化资源包，含更新后的 PPT 课件、动画、微课、虚拟仿真操作模块等。
8-2-1-3 《数字化在线课程资源校企验收报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2025, 'SG08020225', '8-2-2智慧教育平台优质教学资源融入课程占比达40%；', 3,
     8, 10, 10, '智慧教育平台优质教学资源融入课程占比达40%', NULL, '筛选与课程目标高度契合的平台资源，接着对现有课程结构进行模块化重构，将视频、仿真、案例等资源有机嵌入课前、课中、课后环节；推进“国家职业教育智慧教育平台”资源与校内资源重合，覆盖 40%专业核心课程资源。',
     '8-2-2-1《智慧教育平台优质资源筛选与融入方案》；
8-2-2-2 《智慧教育平台资源融入课程清单》（按课程逐一列明融入的资源名称、来源平台、对应教学模块）
8-2-2-3 《资源融入占比核算表》（按课程统计智慧平台资源数量、课程总资源数量，计算占比并验证是否达 40%，附数据来源说明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2025, 'SG08020325', '8-2-3专业教学资源库内容覆盖专业课程内容的比例达50%', 3,
     8, 12, 12, '专业教学资源库内容覆盖专业课程内容的比例达50%', NULL, '建设专业教学资源库，形成了涵盖课程标准、教学课件、实训手册、考核题库的综合资源体系，覆盖 10 门专业核心课程中的 5 门的教学内容，与行业技术迭代同步的更新机制；',
     '8-2-3-1《机械制造及自动化专业课程清单与资源库覆盖计划表》，列出专业所有课程，标注需覆盖的课程名称及覆盖优先级，明确 “50% 覆盖” 指资源库内容覆盖课程核心知识点比例。
8-2-3-2《专业教学资源库内容清单》按课程分类整理资源，附资源类型、数量、更新时间。
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2025, 'SG08020425', '8-2-4利用数字化资源开展线上线下混合式教学。', 3,
     8, 10, 10, '利用数字化资源开展线上线下混合式教学', NULL, '推行线上线下混合式教学模式，形成了 “线上预习（平台资源）+ 线下实操（实训室）+ 线上复盘（习题 / 讨论）” 的教学流程规范。',
     '8-2-4-1《混合式教学设计方案汇编》，按课程制定混合式教学方案：明确线上线下课时占比、互动环节设计等；
8-2-4-2《混合式教学实施记录》，含线上学习数据截图、线下实训视频片段、学生线上作业批改记录等；
8-2-5-3 《混合式教学效果评估报告》，校企联合分析：对比混合式教学与传统教学的学生成绩变化等；
8-2-5-4 《混合式教学资源应用案例集》，整理 3-5 个典型教学案例。
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2025, 'SG08020525', '8-2-5【新增】新增智慧教室10间。', 3,
     8, 9, 9, '新增智慧教室10间', NULL, '系统化部署智能教学设备与集成化信息平台，形成了支撑教学模式创新的新一代智慧教学环境，覆盖10间核心教学场所及多种数字化教学场景，明确了“技术赋能、互动生成”的智慧教室建设与应用方向，预期在提升师生互动体验、创新教学方法与优化教学管理效能上实现显著突破',
     '8-2-5-1《智慧教室建设方案》；
8-2-5-2《智慧教室建设验收报告》
8-2-5-3《智慧教室使用手册》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08020626', '8-2-6完善“机械制造技术”等3门数字化在线课程教学资源；', 3,
     8, 12, 12, '完善“机械制造技术”等3门数字化在线课程教学资源', NULL, '完善 “机械制造技术”“数控加工编程”“机械制造基础” 3 门课程资源，形成了含  动画演示、微课视频、实操视频的进阶资源包；',
     '8-2-6-1《机械制造技术等 3 门数字化在线课程资源完善方案》（明确 3 门课程具体名称，标注需补充的资源类型；
8-2-6-2 3 门课程完善后的数字化资源包，含更新后的 PPT 课件、动画、微课、虚拟仿真操作模块等。
8-2-6-3 《数字化在线课程资源校企验收报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08020726', '8-2-7智慧教育平台优质教学资源融入课程占比达50%；', 3,
     8, 10, 10, '智慧教育平台优质教学资源融入课程占比达50%', NULL, '筛选与课程目标高度契合的平台资源，接着对现有课程结构进行模块化重构，将视频、仿真、案例等资源有机嵌入课前、课中、课后环节；推进“国家职业教育智慧教育平台”资源与校内资源重合，覆盖50%专业核心课程资源。',
     '8-2-7-1《智慧教育平台优质资源筛选与融入方案》；
8-2-7-2 《智慧教育平台资源融入课程清单》（按课程逐一列明融入的资源名称、来源平台、对应教学模块）
8-2-7-3 《资源融入占比核算表》（按课程统计智慧平台资源数量、课程总资源数量，计算占比并验证是否达 50%，附数据来源说明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08020826', '8-2-8专业教学资源库内容覆盖专业课程内容的比例达60%；', 3,
     8, 12, 12, '专业教学资源库内容覆盖专业课程内容的比例达60%', NULL, '扩充资源库内容，覆盖 10 门专业核心课程中 6 门的全部内容；明确了资源库与企业共建机制，融合校本资源与平台资源。',
     '8-2-8-1《机械制造及自动化专业课程清单与资源库覆盖计划表》，列出专业所有课程，标注需覆盖的课程名称及覆盖优先级，明确 “60% 覆盖” 指资源库内容覆盖课程核心知识点比例。
8-2-8-2《专业教学资源库内容清单》按课程分类整理资源，附资源类型、数量、更新时间。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08020926', '8-2-9利用数字化资源开展线上线下混合式教学；', 3,
     8, 10, 10, '利用数字化资源开展线上线下混合式教学；', NULL, '优化混合教学模式，新增 “线上小组协作项目（如零件设计）+ 线下成果展示” 环节；实现了实训课程学生团队协作能力培养。',
     '8-2-9-1《混合式教学设计方案汇编》，按课程制定混合式教学方案：明确线上线下课时占比、互动环节设计等；
8-2-9-2《混合式教学实施记录》，含线上学习数据截图、线下实训视频片段、学生线上作业批改记录等；
8-2-9-3 《混合式教学效果评估报告》，校企联合分析：对比混合式教学与传统教学的学生成绩变化等；
8-2-9-4 《混合式教学资源应用案例集》，整理 3-5 个典型教学案例。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08021026', '8-2-10人工智能融入教学的省级及以上项目、案例或获奖1项。', 3,
     8, 14, 14, '人工智能融入教学的省级及以上项目、案例或获奖1项', '省级及以上', '申报 “AI 辅助工程机械实训教学” 项目，形成了包含 AI 故障诊断模拟、学情分析模型的教学案例，明确了项目需符合省级职业教育教学改革项目申报标准；',
     '8-2-10-1人工智能融入教学的省级及以上项目、案例或获奖1项。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08021126', '8-2-11【新增】新建智能制造专业垂类模型1个；', 3,
     10, 39, 39, '新建智能制造专业垂类模型1个', NULL, '开发面向工程机械产业的智能制造的垂类大模型，形成覆盖工程机械产业智能制造的知识、技能、素养，形成知识图谱、技能图谱和思政图谱，满足教学和职业培训需求。',
     '8-2-11-1《智能制造专业垂类模型建设方案》
8-2-11-2 《智能制造专业垂类模型技术文档》
8-2-11-3《智能制造专业垂类模型应用报告》
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08021226', '8-2-12【新增】新增智慧教室10间；', 3,
     8, 9, 9, '新增智慧教室10间', NULL, '系统化部署智能教学设备与集成化信息平台，形成了支撑教学模式创新的新一代智慧教学环境，覆盖了10间核心教学场所及多种数字化教学场景，明确了“技术赋能、互动生成”的智慧教室建设与应用方向，预期在提升师生互动体验、创新教学方法与优化教学管理效能上实现显著突破',
     '8-2-12-1《智慧教室建设方案》
8-2-12-2《智慧教室建设验收报告》
8-2-12-3《智慧教室使用手册》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08021326', '8-2-13【新增】辐射校内专业垂类大模型1个；', 3,
     10, 39, 39, '辐射校内专业垂类大模型1个', NULL, '聚焦关键专业领域开展大模型的深度研发与应用孵化，形成了1个具有校内示范效应的专业垂类大模型实体，覆盖了特定学科的教学、科研与创新实践全链条，明确了以自研大模型为核心驱动、赋能校内教育模式与科研范式变革的战略路径，预期在打造特色学科高地、提升人才培养前瞻性与科研创新效能上实现关键突破。',
     '8-2-13-1《校内辐射专业垂类大模型建设方案》；
8-2-13-2《校内辐射专业垂类大模型建设技术文档》；
8-2-13-3《校内辐射专业垂类大模型应用报告》；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08021426', '8-2-14【新增】专业垂类大模型辐射校外院校5个；', 3,
     10, 39, 39, '专业垂类大模型辐射校外院校5个', NULL, '构建并输出面向特定专业领域的行业大模型解决方案，形成了可复制的技术赋能与服务外溢新模式，覆盖了5所校外合作院校，明确了以先进人工智能技术驱动教育协同创新与高质量发展的核心路径，预期在构建区域教育创新共同体、引领职业教育数字化转型方面实现示范性突破。',
     '8-2-14-1《智能制造专业垂类大模型使用校校合作协议》；
8-2-14-2 《智能制造专业垂类大模型校外推广实施手册》，含模型部署流程、账号管理规则、常见问题解答；
8-2-14-3《校外院校辐射应用过程材料》，记录 5 所院校的名称、使用专业、应用课程、使用频次，辐射效果反馈报告等。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2026, 'SG08021526', '8-2-15【新增】校外使用学校开发的智能体3个。', 3,
     10, 37, 37, '校外使用学校开发的智能体3个', NULL, '打造一体化展示平台，集中呈现三大智能体的核心功能与特色应用场景；通过精选标杆案例、开展定向体验活动及建立合作生态，向目标院校、企业及学生精准传递其解决实际教学与生产问题的价值，以点带面，形成示范效应与规模化应用。',
     '8-2-15-1《校外使用智能体校校合作协议》，明确智能体使用权限、数据安全责任、技术维护条款；
8-2-15-2《智能体校外部署与培训记录》，含 3 家单位的部署调试报告、操作人员培训签到表及培训课件；
8-2-15-3《校外智能体使用效果数据报告》，记录 3 个智能体的使用频次、响应准确率、用户满意度。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08021627', '8-2-16完善“工业机器人现场编”等3门数字化在线课程教学资源；', 3,
     8, 12, 12, '完善“工业机器人现场编”等3门数字化在线课程教学资源', NULL, '完善 “工业机器人现场编程等3门课程资源，形成了含机器人虚拟编程软件、企业真实工作站视频、编程案例库的资源包；',
     '8-2-16-1《工业机器人现场编等 3 门数字化在线课程资源完善方案》（明确 3 门课程具体名称，标注需补充的资源类型；
8-2-16-2 3 门课程完善后的数字化资源包，含更新后的 PPT 课件、动画、微课、虚拟仿真操作模块等。
8-2-16-3 《数字化在线课程资源校企验收报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08021727', '8-2-17智慧教育平台优质教学资源融入课程占比达60%；', 3,
     8, 10, 10, '智慧教育平台优质教学资源融入课程占比达60%', NULL, '筛选与课程目标高度契合的平台资源，接着对现有课程结构进行模块化重构，将视频、仿真、案例等资源有机嵌入课前、课中、课后环节；推进“国家职业教育智慧教育平台”资源与校内资源重合，覆盖 60%专业核心课程资源。',
     '8-2-17-1《智慧教育平台优质资源筛选与融入方案》；
8-2-17-2 《智慧教育平台资源融入课程清单》（按课程逐一列明融入的资源名称、来源平台、对应教学模块）
8-2-17-3 《资源融入占比核算表》（按课程统计智慧平台资源数量、课程总资源数量，计算占比并验证是否达 60%，附数据来源说明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08021827', '8-2-18专业教学资源库内容覆盖专业课程内容的比例达70%；', 3,
     8, 12, 12, '专业教学资源库内容覆盖专业课程内容的比例达70%', NULL, '资源库扩容，覆盖 10 门核心课程中 7 门的全部内容；明确了资源库 “省级教学资源库申报（突出工程机械智能制造特色）” 的建设方向；实现了 7 门课程资源库调用率达 98%；',
     '8-2-18-1《机械制造及自动化专业课程清单与资源库覆盖计划表》，列出专业所有课程，标注需覆盖的课程名称及覆盖优先级，明确 “70% 覆盖” 指资源库内容覆盖课程核心知识点比例。
8-2-18-2《专业教学资源库内容清单》按课程分类整理资源，附资源类型、数量、更新时间。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08021927', '8-2-19利用数字化资源开展线上线下混合式教学。【追加】：建成省级及以上教学资源库1个；', 3,
     8, 10, 10, '建成省级及以上教学资源库1个', '省级及以上', '推行线上线下混合式教学模式，形成了 “线上预习（平台资源）+ 线下实操（实训室）+ 线上复盘（习题 / 讨论）” 的教学流程规范。',
     '8-2-19-1建成省级及以上教学资源库1个；
8-2-19-2形成线上线下混合式教学案例1个；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08022027', '8-2-20【新增】新增智慧教室10个；', 3,
     8, 9, 9, '新增智慧教室10个', NULL, '系统化部署智能教学设备与集成化信息平台，形成了支撑教学模式创新的新一代智慧教学环境，覆盖了10间核心教学场所及多种数字化教学场景，明确了“技术赋能、互动生成”的智慧教室建设与应用方向，预期在提升师生互动体验、创新教学方法与优化教学管理效能上实现显著突破',
     '8-2-20-1智慧教室建设方案；
8-2-20-2《智慧教室建设验收报告》
8-2-20-3《智慧教室使用手册》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08022127', '8-2-21【新增】辐射校内专业垂类大模型2个；', 3,
     10, 39, 39, '辐射校内专业垂类大模型2个', NULL, '聚焦关键专业领域开展大模型的深度研发与应用孵化，形成了2个具有校内示范效应的专业垂类大模型实体，覆盖了特定学科的教学、科研与创新实践全链条，明确了以自研大模型为核心驱动、赋能校内教育模式与科研范式变革的战略路径，预期在打造特色学科高地、提升人才培养前瞻性与科研创新效能上实现关键突破。',
     '8-2-21-1校内辐射专业垂类大模型建设方案；
8-2-21-2校内辐射专业垂类大模型建设技术文档；
8-2-21-3校内辐射专业垂类大模型应用报告；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08022227', '8-2-22【新增】专业垂类大模型辐射校外院校5个；', 3,
     10, 39, 39, '专业垂类大模型辐射校外院校5个', NULL, '构建并输出面向特定专业领域的行业大模型解决方案，形成了可复制的技术赋能与服务外溢新模式，覆盖了5所校外合作院校，明确了以先进人工智能技术驱动教育协同创新与高质量发展的核心路径，预期在构建区域教育创新共同体、引领职业教育数字化转型方面实现示范性突破。',
     '8-2-22-1《智能制造专业垂类大模型使用校校合作协议》；
8-2-22-2 《智能制造专业垂类大模型校外推广实施手册》，含模型部署流程、账号管理规则、常见问题解答；
8-2-22-3《校外院校辐射应用过程材料》，记录 5 所院校的名称、使用专业、应用课程、使用频次，辐射效果反馈报告等。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2027, 'SG08022327', '8-2-23【新增】校外使用学校开发的智能体3个。', 3,
     10, 37, 37, '校外使用学校开发的智能体3个', NULL, '打造一体化展示平台，集中呈现三大智能体的核心功能与特色应用场景；通过精选标杆案例、开展定向体验活动及建立合作生态，向目标院校、企业及学生精准传递其解决实际教学与生产问题的价值，以点带面，形成示范效应与规模化应用。',
     '8-2-23-1《校外使用智能体校校合作协议》，明确智能体使用权限、数据安全责任、技术维护条款；
8-2-23-2《智能体校外部署与培训记录》，含 3 家单位的部署调试报告、操作人员培训签到表及培训课件；
8-2-23-3《校外智能体使用效果数据报告》，记录 3 个智能体的使用频次、响应准确率、用户满意度。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08022428', '8-2-24完善“工业机器人系统集成”等3门数字化在线课程教学资源；', 3,
     8, 12, 12, '完善“工业机器人系统集成”等3门数字化在线课程教学资源', NULL, '完善 “工业机器人系统集成”等3 门课程资源，形成了含产教融合项目案例、调试操作、明确了资源需对接 “1+X” 证书。',
     '8-2-24-1《工业机器人系统集成等 3 门数字化在线课程资源完善方案》（明确 3 门课程具体名称，标注需补充的资源类型；
8-2-24-2 3 门课程完善后的数字化资源包，含更新后的 PPT 课件、动画、微课、虚拟仿真操作模块等。
8-2-24-3 《数字化在线课程资源校企验收报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08022528', '8-2-25智慧教育平台优质教学资源融入课程占比达65%；', 3,
     8, 10, 10, '智慧教育平台优质教学资源融入课程占比达65%', NULL, '筛选与课程目标契合的平台资源，接着对现有课程结构进行模块化重构，将视频、仿真、案例等资源有机嵌入课前、课中、课后环节；推进“国家职业教育智慧教育平台”资源与校内资源重合，覆盖 65%专业核心课程资源。',
     '8-2-7-1《智慧教育平台优质资源筛选与融入方案》；
8-2-7-2 《智慧教育平台资源融入课程清单》（按课程逐一列明融入的资源名称、来源平台、对应教学模块）
8-2-7-3 《资源融入占比核算表》（按课程统计智慧平台资源数量、课程总资源数量，计算占比并验证是否达 65%，附数据来源说明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08022628', ' 8-2-26专业教学资源库内容覆盖专业课程内容的比例达75%；', 3,
     8, 12, 12, ' 专业教学资源库内容覆盖专业课程内容的比例达75%', NULL, '通过资源库扩容，明确了资源库 “省级教学资源库申报（突出工程机械智能制造特色）” 的建设方向；实现了 75%的资源建设，课程资源库调用率达 98%；',
     '8-2-26-1《机械制造及自动化专业课程清单与资源库覆盖计划表》，列出专业所有课程，标注需覆盖的课程名称及覆盖优先级，明确 “75% 覆盖” 指资源库内容覆盖课程核心知识点比例。
8-2-26-2《专业教学资源库内容清单》按课程分类整理资源，附资源类型、数量、更新时间。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08022728', '8-2-27利用数字化资源开展线上线下混合式教学。', 3,
     8, 10, 10, '利用数字化资源开展线上线下混合式教学', NULL, '通过推行线上线下混合式教学模式，形成了 “线上预习（平台资源）+ 线下实操（实训室）+ 线上复盘（习题 / 讨论）” 的教学流程规范。',
     '8-2-27-1《混合式教学设计方案汇编》，按课程制定混合式教学方案：明确线上线下课时占比、互动环节设计等；
8-2-27-2《混合式教学实施记录》，含线上学习数据截图、线下实训视频片段、学生线上作业批改记录等；
8-2-27-3 《混合式教学效果评估报告》，校企联合分析：对比混合式教学与传统教学的学生成绩变化等；
8-2-27-4 《混合式教学资源应用案例集》，整理 3-5 个典型教学案例。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08022828', '8-2-28【新增】新增智慧教室10间；', 3,
     8, 9, 9, '新增智慧教室10间', NULL, '系统化部署智能教学设备与集成化信息平台，形成了支撑教学模式创新的新一代智慧教学环境，覆盖了10间核心教学场所及多种数字化教学场景，明确了“技术赋能、互动生成”的智慧教室建设与应用方向，预期在提升师生互动体验、创新教学方法与优化教学管理效能上实现显著突破',
     '8-2-28-1智慧教室建设方案；
8-2-28-2《智慧教室建设验收报告》
8-2-28-3《智慧教室使用手册》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08022928', '8-2-29【新增】新增智能制造数字化培训包1套；', 3,
     11, 22, 22, '新增智能制造数字化培训包1套', NULL, '开发 “工程机械智能制造生产线运维” 数字化培训包，形成了含培训课件（）、考核题库的企业培训资源；覆盖了合作企业的机械制造车间技术人员培训；',
     '8-2-29-1《智能制造数字化培训包建设方案》
8-2-29-2 智能制造数字化培训包，全套数字化资源。
8-2-29-3《智能制造数字化培训包资源更新机制文档》
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08023028', '8-2-30【新增】辐射校内专业垂类大模型2个；', 3,
     10, 39, 39, '辐射校内专业垂类大模型2个', NULL, '聚焦关键专业领域开展大模型的深度研发与应用孵化，形成了2个具有校内示范效应的专业垂类大模型实体，覆盖了特定学科的教学、科研与创新实践全链条，明确了以自研大模型为核心驱动、赋能校内教育模式与科研范式变革的战略路径，预期在打造特色学科高地、提升人才培养前瞻性与科研创新效能上实现关键突破。',
     '8-2-30-1校内辐射专业垂类大模型建设方案；
8-2-30-2校内辐射专业垂类大模型建设技术文档；
8-2-30-3校内辐射专业垂类大模型应用报告；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08023128', '8-2-31【新增】专业垂类大模型辐射校外院校5个；', 3,
     10, 39, 39, '专业垂类大模型辐射校外院校5个', NULL, '构建并输出面向特定专业领域的行业大模型解决方案，形成了可复制的技术赋能与服务外溢新模式，覆盖了5所校外合作院校，明确了以先进人工智能技术驱动教育协同创新与高质量发展的核心路径，预期在构建区域教育创新共同体、引领职业教育数字化转型方面实现示范性突破。',
     '8-2-31-1《智能制造专业垂类大模型使用校校合作协议》；
8-2-31-2 《智能制造专业垂类大模型校外推广实施手册》，含模型部署流程、账号管理规则、常见问题解答；
8-2-31-3《校外院校辐射应用过程材料》，记录 5 所院校的名称、使用专业、应用课程、使用频次，辐射效果反馈报告等。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2028, 'SG08023228', '8-2-32【新增】校外使用学校开发的智能体3个。', 3,
     10, 37, 37, '校外使用学校开发的智能体3个', NULL, '打造一体化展示平台，集中呈现三大智能体的核心功能与特色应用场景；通过精选标杆案例、开展定向体验活动及建立合作生态，向目标院校、企业及学生精准传递其解决实际教学与生产问题的价值，以点带面，形成示范效应与规模化应用。',
     '8-2-32-1《校外使用智能体校校合作协议》，明确智能体使用权限、数据安全责任、技术维护条款；
8-2-32-2《智能体校外部署与培训记录》，含 3 家单位的部署调试报告、操作人员培训签到表及培训课件；
8-2-32-3《校外智能体使用效果数据报告》，记录 3 个智能体的使用频次、响应准确率、用户满意度。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023329', '8-2-33完善“机械制图”3门数字化在线课程教学资源；', 3,
     8, 12, 12, '完善“机械制图”3门数字化在线课程教学资源', NULL, '完善 “机械制图”等3门课程的教学资源建设，明确了资源需对接机械工程制图员职业技能等级证书要求。',
     '8-2-33-1《机械制图等 3 门数字化在线课程资源完善方案》（明确 3 门课程具体名称，标注需补充的资源类型；
8-2-33-2 3 门课程完善后的数字化资源包，含更新后的 PPT 课件、动画、微课、虚拟仿真操作模块等。
8-2-33-3 《数字化在线课程资源校企验收报告》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023429', ' 8-2-34智慧教育平台优质教学资源融入课程占比达70%；', 3,
     8, 10, 10, '智慧教育平台优质教学资源融入课程占比达70%', NULL, '筛选与课程目标高度契合的平台资源，接着对现有课程结构进行模块化重构，将视频、仿真、案例等资源有机嵌入课前、课中、课后环节；推进“国家职业教育智慧教育平台”资源与校内资源重合，覆盖70%专业核心课程资源。',
     '8-2-34-1《智慧教育平台优质资源筛选与融入方案》；
8-2-34-2 《智慧教育平台资源融入课程清单》（按课程逐一列明融入的资源名称、来源平台、对应教学模块）
8-2-34-3 《资源融入占比核算表》（按课程统计智慧平台资源数量、课程总资源数量，计算占比并验证是否达 70%，附数据来源说明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023529', '8-2-35专业教学资源库内容覆盖专业课程内容的比例达80%；', 3,
     8, 12, 12, '专业教学资源库内容覆盖专业课程内容的比例达80%', NULL, '资源库扩容，明确了资源库 “省级教学资源库申报（突出工程机械智能制造特色）” 的建设方向；实现了 80%的资源建设，课程资源库调用率达 98%；',
     '8-2-35-1《机械制造及自动化专业课程清单与资源库覆盖计划表》，列出专业所有课程，标注需覆盖的课程名称及覆盖优先级，明确 “80% 覆盖” 指资源库内容覆盖课程核心知识点比例。
8-2-35-2《专业教学资源库内容清单》按课程分类整理资源，附资源类型、数量、更新时间。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023629', '8-2-36利用数字化资源开展线上线下混合式教学。', 3,
     8, 10, 10, '利用数字化资源开展线上线下混合式教学', NULL, '推行线上线下混合式教学模式，形成了 “线上预习（平台资源）+ 线下实操（实训室）+ 线上复盘（习题 / 讨论）” 的教学流程规范。',
     '8-2-36-1《混合式教学设计方案汇编》，按课程制定混合式教学方案：明确线上线下课时占比、互动环节设计等；
8-2-36-2《混合式教学实施记录》，含线上学习数据截图、线下实训视频片段、学生线上作业批改记录等；
8-2-36-3 《混合式教学效果评估报告》，校企联合分析：对比混合式教学与传统教学的学生成绩变化等；
8-2-36-4 《混合式教学资源应用案例集》，整理 3-5 个典型教学案例。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023729', '8-2-37【新增】新增智慧教室10间；', 3,
     8, 9, 9, '新增智慧教室10间', NULL, '系统化部署智能教学设备与集成化信息平台，形成了支撑教学模式创新的新一代智慧教学环境，覆盖了10间核心教学场所及多种数字化教学场景，明确了“技术赋能、互动生成”的智慧教室建设与应用方向，预期在提升师生互动体验、创新教学方法与优化教学管理效能上实现显著突破',
     '8-2-37-1《智慧教室建设方案》；
8-2-37-2《智慧教室建设验收报告》
8-2-37-3《智慧教室使用手册》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023829', '8-2-38【新增】辐射校内专业垂类大模型1个；', 3,
     10, 39, 39, '辐射校内专业垂类大模型1个', NULL, '通过聚焦关键专业领域开展大模型的深度研发与应用孵化，形成了1个具有校内示范效应的专业垂类大模型实体，覆盖了特定学科的教学、科研与创新实践全链条，明确了以自研大模型为核心驱动、赋能校内教育模式与科研范式变革的战略路径，预期在打造特色学科高地、提升人才培养前瞻性与科研创新效能上实现关键突破。',
     '8-2-38-1校内辐射专业垂类大模型建设方案；
8-2-38-2校内辐射专业垂类大模型建设技术文档；
8-2-38-3校内辐射专业垂类大模型应用报告；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08023929', '8-2-39【新增】专业垂类大模型辐射校外院校5个；', 3,
     10, 39, 39, '专业垂类大模型辐射校外院校5个', NULL, '通过构建并输出面向特定专业领域的行业大模型解决方案，形成了可复制的技术赋能与服务外溢新模式，覆盖5所校外合作院校，明确以先进人工智能技术驱动教育协同创新与高质量发展的核心路径，预期在构建区域教育创新共同体、引领职业教育数字化转型方面实现示范性突破。',
     '8-2-39-1《智能制造专业垂类大模型使用校校合作协议》；
8-2-39-2 《智能制造专业垂类大模型校外推广实施手册》，含模型部署流程、账号管理规则、常见问题解答；
8-2-39-3《校外院校辐射应用过程材料》，记录 5 所院校的名称、使用专业、应用课程、使用频次，辐射效果反馈报告等。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 33, '0,8,33', 2029, 'SG08024029', '8-2-40【新增】校外使用学校开发的智能体3个；', 3,
     10, 37, 37, '校外使用学校开发的智能体3个', NULL, '将自主研发的智能体技术，向外部推广应用，形成3个具备校外实践价值的智能体应用范例，覆盖多个校外合作单位的具体业务场景，明确以智能体技术输出作为服务社会、深化产教融合的创新路径，预期在扩大学校技术影响力、构建产学研用一体化生态上实现显著突破。',
     '8-2-40-1《校外使用智能体校校合作协议》，明确智能体使用权限、数据安全责任、技术维护条款；
8-2-40-2《智能体校外部署与培训记录》，含 3 家单位的部署调试报告、操作人员培训签到表及培训课件；
8-2-40-3《校外智能体使用效果数据报告》，记录 3 个智能体的使用频次、响应准确率、用户满意度。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2025, 'SG08030125', '8-3-1开展学生学习和教师成长数据库的调研和方案论证；', 3,
     6, 6, 6, '学生学习和教师成长数据库的调研和方案论证', NULL, '联合徐工集团组建专项调研团队，调研机械制造领域岗位能力需求、学生学习数据采集维度及教师成长核心指标，结合徐工人才培养标准与学院教学实际开展方案论证，形成契合校企协同育人的学生学习和教师成长数据库建设方案，预期在调研论证工作结束后实现方案与徐工岗位需求、学院教学目标的深度匹配，为数据库后续建设提供科学依据。',
     '8-3-1-1学生学习与教师成长数据库调研报告；
8-3-1-2学生学习和教师成长数据库的建设方案。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2025, 'SG08030225', '8-3-2开展动态学业评价系统调研。【追加】：企业参与专业群评价的课程占比20%，专业核心课覆盖50%。', 3,
     8, 11, 11, '动态学业评价系统调研报告1份，企业参与专业群评价的课程占比20%，专业核心课覆盖50%', NULL, '调研行业内动态学业评价系统的技术架构、评价维度及企业参与模式，结合专业群课程体系与徐工岗位考核标准，形成动态学业评价系统调研分析报告；明确企业参与专业群评价的课程占比 20%及专业核心课覆盖 50%要求，预期在调研工作完成后实现评价系统需求与徐工人才选拔标准的精准对接，为系统后续开发奠定基础。',
     '8-3-2-1动态学业评价系统调研报告1份；
8-3-2-2企业参与专业群评价的课程占比20%，专业核心课覆盖50%
8-3-2-3徐工集团岗位考核标准对接分析表（含与现有课程评价的匹配度分析）。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2026, 'SG08030326', '8-3-3建立学生学习和教师成长数据库；', 3,
     6, 6, 6, '建立学生学习和教师成长数据库1个', NULL, '通过搭建涵盖校内理论学习数据、企业实训基地实践数据的数据库，整合学生基础信息、课程成绩、实训考核结果及教师教学成果、企业研修经历等数据，形成结构化的学生学习和教师成长数据库，预期在数据库建成后实现学生学习轨迹与教师成长历程的可视化追溯，为校企协同育人决策提供数据支撑。',
     '8-3-3-1《学生学习和教师成长数据库数据库建设方案》，明确数据库核心定位，标注数据维度，附数据库架构图，明确数据采集标准；
8-3-3-2 学生学习和教师成长数据库本体，数据库结构设计文档，初始数据录入成果集；', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2026, 'SG08030426', '8-3-4建立“全程多元、数智融合、过程增值、数据循证”的动态学业评价系统。【追加】：企业参与专业群评价的课程占比30%，专业核心课覆盖80%。', 3,
     8, 11, 11, '建立“全程多元、数智融合、过程增值、数据循证”的动态学业评价系统。企业参与专业群评价的课程占比30%，专业核心课覆盖80%。', NULL, '“全程多元、数智融合、过程增值、数据循证” 的动态学业评价系统，嵌入徐工岗位技能考核模块，构建涵盖理论学习、实践操作、创新能力的多元评价体系，形成实时化、智能化的学业评价机制，明确企业参与专业群评价的课程占比 30%及专业核心课覆盖 80%指标要求，预期在系统建成后实现学生学业动态监测与个性化成长指导，同时满足徐工对技能人才的前置评价需求。',
     '8-3-4-1建立“全程多元、数智融合、过程增值、数据循证”的动态学业评价系统。
8-3-4-2企业参与专业群评价的课程占比30%，专业核心课覆盖80%。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2027, 'SG08030527', '8-3-5更新学生学习和教师成长数据库；', 3,
     6, 6, 6, '更新学生学习和教师成长数据库', NULL, '建立校企数据定期同步机制，明确数据更新周期（月度更新实训数据，季度更新整体数据）与质量审核标准，预期在数据库更新后实现数据内容与企业技术升级、学院课程改革的同步适配，提升数据对育人工作的支撑时效性。',
     '8-3-5-1《数据库动态更新管理办法（校企协同版）》；
8-3-5-2 《数据库更新数据质量审核报告》
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2027, 'SG08030627', '8-3-6完善“全程多元、数智融合、过程增值、数据循证”的动态学业评价系统。【追加】：企业参与专业群评价的课程占比50%，专业核心课覆盖100%。', 3,
     8, 11, 11, '完善“全程多元、数智融合、过程增值、数据循证”的动态学业评价系统。企业参与专业群评价的课程占比50%，专业核心课覆盖100%。', NULL, '收集系统运行过程中的师生反馈、企业实训导师评价意见，专业群联合企业优化评价指标权重、完善数据循证算法，进一步细化 “全程多元、数智融合、过程增值、数据循证” 的实现路径，形成更贴合校企协同育人的评价系统升级版，明确企业参与专业群评价的课程占比 50%及专业核心课覆盖 100%指标要求，预期在系统完善后实现评价结果与徐工等企业岗位录用标准的直接对接，提升人才培养精准度。',
     '8-3-6-1完善“全程多元、数智融合、过程增值、数据循证”的动态学业评价系统。
8-3-6-2企业参与专业群评价的课程占比50%，专业核心课覆盖100%。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2028, 'SG08030728', '8-3-7优化学生学习和教师成长数据库；', 3,
     6, 6, 6, '优化学生学习和教师成长数据库', NULL, '引入数据挖掘技术，分析学生学习数据与企业岗位适配度、教师成长数据与企业技术服务能力的关联关系，新增数据可视化分析模块与决策建议功能，形成智能化的数据库应用体系，明确数据库优化后的核心应用场景，预期在数据库优化后实现数据驱动的校企协同育人精准决策，提升学生岗位适配率与教师企业服务能力。',
     '8-3-7-1《数据库优化方案》，明确优化方向：①新增 “徐工岗位适配度分析模块”；②升级数据可视化功能；③完善数据预警机制；
8-3-7-2 优化后的学生学习和教师成长数据库，功能模块说明文档，优化后数据应用案例。', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2028, 'SG08030828', '8-3-8构建教与学的评价闭环管理系统。', 3,
     8, 11, 11, '构建教与学的评价闭环管理系统', NULL, '整合学生学习和教师成长数据库、动态学业评价系统的数据资源，搭建 “评价 - 诊断 - 改进 - 反馈 - 再评价” 的教与学评价闭环流程，嵌入企业岗位需求反馈模块与教学改进指导模块，形成覆盖 “教、学、评、改” 全流程的管理体系，明确校企双方在闭环管理中的职责，预期在系统建成后实现教与学质量的持续迭代提升，确保人才培养始终贴合徐工发展需求。',
     '8-3-8-1《教与学评价闭环管理系统校企协同建设方案》
8-3-8-2 教与学评价闭环管理系统本体（部署版本）
8-3-8-3《教与学评价闭环管理系统应用案例》', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2029, 'SG08030929', '8-3-9推广“全程多元、数智融合、过程增值、数据循证”的动态学业评价；', 3,
     8, 11, 11, '新闻报道、交流报告或推广使用', NULL, '举办校企合作研讨会、编写推广手册，在专业群内部全面推广该动态学业评价模式，并向区域内同类院校及机械行业企业分享经验，形成可复制的推广方案，预期在推广工作完成后实现动态学业评价在专业群 100% 覆盖，同时在行业内形成一定的示范影响力。',
     '8-3-9-1新闻报道、交流报告或推广使用', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 34, '0,8,34', 2029, 'SG08031029', '8-3-10凝练动态评价案例1个。', 3,
     8, 11, 11, '动态评价案例1个', NULL, '梳理动态学业评价在机械制造及自动化专业群的应用实践，联合企业提炼典型经验、关键步骤与实施成效，形成结构完整、内容详实的动态评价案例，明确案例的核心价值与推广方向，预期在案例凝练完成后形成 1 个具备可借鉴性的典型案例，为后续校企协同评价工作提供实践样本。',
     '8-3-10-1动态评价案例1个', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2025, 'SG09010125', '9-1-1 筹建“徐工-乌兹”等培训中心', 3,
     5, 27, 27, '召开筹建会议1次', NULL, '调研徐工集团等工程机械产业链企业在乌兹别克斯坦等境外所在地人才需求、工作就业、培训情况等，形成筹建境外培训中心系统性调研报告、建设方案、工作计划，明确了培训中心主要职责、目标，预期完成筹建的相关立项通知、会议、宣传等事项，实现筹建培训中心的组织团队、中心架构、资源配置等，为后续建立“徐工-乌兹”培训中心奠定扎实基础。',
     '9-1-1-1 国际化培训中心（徐工-乌兹）调研报告
9-1-1-2 过程活动支撑资料（会议、网站宣传、照片等）
9-1-1-3 建设方案、工作计划等
9-1-1-4 文件：关于筹建“徐工-乌兹”培训中心的通知（按制式模版）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2025, 'SG09010225', '9-1-2 开展中资企业走出去员工“外文+职业技能”培训200人次', 3,
     5, 27, 27, '“外文+职业技能”培训200人次', NULL, '与徐工集团等中资企业强化合作、开展走出去员工培训，形成了校企国际化产教融合新范例，培训覆盖了“外文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了中资企业员工培训200人次，为持续开展中资企业走出去员工“外文+职业技能”培训奠定扎实基础。',
     '9-1-2-1 校企合作走出去员工培训协议
9-1-2-2 校企合作走出去员工培训方案、计划、内容、推进会等相关资料
9-1-2-3 培训过程资料（开班照片、培训照片、培训讲义、新闻报道等）
9-1-2-4 结果性材料（如结业证书）
9-1-2-5 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2025, 'SG09010325', '9-1-3 海外中资企业员工“中文+职业技能”培训200人次', 3,
     5, 27, 27, '“中文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“中文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了境外中资企业员工培训200人次，为持续开展海外中资企业员工“中文+职业技能”培训奠定扎实基础。',
     '9-1-3-1 科特迪瓦培训协议
9-1-3-2 科特迪瓦培训方案、计划、内容、推进会等相关资料
9-1-3-3 培训过程资料（开班照片、培训照片、培训讲义、新闻报道等）
9-1-3-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2025, 'SG09010425', '9-1-4 开发培训教材或资源包1个', 3,
     11, 22, 22, '培训教材或资源包1个', NULL, '通过与徐工集团等企业强化合作，整合走出去员工“外文+技能”培训素材和海外“中文+技能”培训素材，形成了项目全面、内容详实、模块化培训资料，明确了走出去员工、和海外员工定制化培训项目、内容，预期校企联合开发培训教材、资源包1个，为持续开展国际化培训奠定教材、资源基础。',
     '9-1-4-1 科特迪瓦培训资料（教材、资源包，如ppt、讲义、企业教材等）
9-1-4-2 徐工海外培训资料（校内）（教材、资源包，如ppt、讲义、企业教材等）
9-1-4-3 立项分析、实施评估资料（如需求分析报告、项目立项书、评估满意度、效果报告等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2026, 'SG09010526', '9-1-5 建立“徐工-乌兹”等培训中心', 3,
     5, 27, 27, '“徐工-乌兹”等培训中心1个', NULL, '通过建立“徐工-乌兹”等培训中心，形成了校企协同、产教融合的海外本土化技能人才培养新模式，覆盖了乌兹别克斯坦及周边中亚国家市场，明确以企业需求为导向的培训认证体系与联合运营管理机制，预期完成统化培养数百名符合徐工标准的本土技工与售后服务人员，为全面提升区域市场服务响应能力、深度开拓及扎根中亚市场奠定扎实基础。',
     '9-1-5-1 中心相关文件（项目批准文件、法人资质证明|、校企合作学协议/备忘录）
9-1-5-2 运营与管理文件（章程、制度，资金证明）
9-1-5-3 招生方案（招生简章、宣传材料、选拔标准与流程等）
9-1-5-4 培训支撑材料（校企定向培养/培训协议、培训方案、师资配置等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2026, 'SG09010626', '9-1-6 开展中资企业走出去员工“外文+职业技能”培训200人次', 3,
     5, 27, 27, '“外文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“外文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了境外中资企业员工培训200人次，为持续开展海外中资企业员工“外文+职业技能”培训奠定扎实基础。',
     '9-1-6-1 校企合作走出去员工培训协议
9-1-6-2 校企合作走出去员工培训方案、计划、内容、推进会等相关资料（当批次）
9-1-6-3 培训过程资料（当批次开班照片、培训照片、培训讲义、新闻报道等）
9-1-6-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2026, 'SG09010726', '9-1-7 海外中资企业员工“中文+职业技能”培训200人次', 3,
     5, 27, 27, '“中文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“外文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了境外中资企业员工培训200人次，为持续开展海外中资企业员工“外文+职业技能”培训奠定扎实基础。',
     '9-1-7-1 海外中资企业员工培训协议
9-1-7-2 培训方案、计划、内容、推进会等相关资料
9-1-7-3 培训过程资料（开班照片、培训照片、培训讲义、新闻报道等）
9-1-7-4 结果性材料（如结业证书）
9-1-7-5 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2026, 'SG09010826', '9-1-8 开发培训教材或资源包1个', 3,
     5, 27, 27, '开发培训教材或资源包1个', NULL, '通过开发培训教材或资源包1个，形成了系统化、本土化、数字化的特色教学资源体系，覆盖了工程机械操作、保养、维修等核心技能模块与典型工作任务，明确了理论与实践相结合的教学路径、考核要点及安全规范，预期在培训中实现教学效率与学员技能达标率双提升，持续推进海外培训的标准化复制、规模化推广与品牌化发展。',
     '9-1-8-1 培训教材或资源包1个
9-1-8-2 教材或资源包使用证明材料（过程材料如上课照片、ppt等）
9-1-8-3 立项分析、实施评估资料（如需求分析报告、项目立项书、评估满意度、效果报告等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2027, 'SG09010927', '9-1-9 开展中资企业走出去员工“外文+职业技能”培训200人次', 3,
     5, 27, 27, '“外文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“外文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现境外中资企业员工培训200人次，基本实现海外中资企业员工“外文+职业技能”培训规模化。',
     '9-1-9-1 校企合作走出去员工培训协议
9-1-9-2 校企合作走出去员工培训方案、计划、内容、推进会等相关资料（当批次）
9-1-9-3 培训过程资料（当批次开班照片、培训照片、培训讲义、新闻报道等）
9-1-9-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2027, 'SG09011027', '9-1-10 海外中资企业员工“中文+职业技能”培训200人次', 3,
     5, 27, 27, '“中文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“中文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了境外中资企业员工培训200人次，为持续开展海外中资企业员工“中文+职业技能”培训奠定扎实基础。',
     '9-1-10-1 海外中资企业员工培训协议
9-1-10-2 培训方案、计划、内容、推进会等相关资料
9-1-10-3 培训过程资料（开班照片、培训照片、培训讲义、新闻报道等）
9-1-10-4 结果性材料（如结业证书）
9-1-10-5 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2027, 'SG09011127', '9-1-11 开发培训教材或资源包1个', 3,
     11, 22, 22, '培训教材或资源包1个', NULL, '通过开发培训教材或资源包1个，形成了系统化、本土化、数字化的特色教学资源体系，覆盖了工程机械操作、保养、维修等核心技能模块与典型工作任务，明确了理论与实践相结合的教学路径、考核要点及安全规范，预期在培训中实现教学效率与学员技能达标率双提升，为海外培训的标准化复制、规模化推广与品牌化发展奠定扎实基础。',
     '9-1-11-1 培训教材或资源包（教材、资源包，如ppt、讲义、企业教材等）
9-1-11-2 教材获资源包使用证明材料
9-1-11-3 立项分析、实施评估资料（如需求分析报告、项目立项书、评估满意度、效果报告等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2028, 'SG09011228', '9-1-12 开展中资企业走出去员工“外文+职业技能”培训200人次', 3,
     5, 27, 27, '“外文+职业技能”培训200人次', NULL, '通过与徐工集团等中资企业强化合作、开展走出去员工培训，形成了校企国际化产教融合新范例，培训覆盖了“外文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了中资企业员工培训200人次，基本实现中资企业走出去员工“外文+职业技能”培训规模化、品牌化。',
     '9-1-12-1 校企合作走出去员工培训协议
9-1-12-2 校企合作走出去员工培训方案、计划、内容、推进会等相关资料（当批次）
9-1-12-3 培训过程资料（当批次开班照片、培训照片、培训讲义、新闻报道等）
9-1-12-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2028, 'SG09011328', '9-1-13 海外中资企业员工“中文+职业技能”培训200人次', 3,
     5, 27, 27, '“中文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“中文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了境外中资企业员工培训200人次，基本实现海外中资企业员工“中文+职业技能”培训规模化。',
     '9-1-13-1 海外中资企业员工培训协议
9-1-13-2 培训方案、计划、内容、推进会等相关资料
9-1-13-3 培训过程资料（开班照片、培训照片、培训讲义、新闻报道等）
9-1-13-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2028, 'SG09011428', '9-1-14 开发培训教材或资源包1个', 3,
     11, 22, 22, '培训教材或资源包1个', NULL, '通过开发培训教材或资源包1个，形成了系统化、本土化、数字化的特色教学资源体系，覆盖了工程机械操作、保养、维修等核心技能模块与典型工作任务，明确了理论与实践相结合的教学路径、考核要点及安全规范，预期在培训中实现教学效率与学员技能达标率双提升，基本实现海外培训的标准化复制、规模化推广与品牌化发展。',
     '9-1-14-1 培训教材或资源包
9-1-14-2 教材获资源包使用证明材料
9-1-14-3 立项分析、实施评估资料（如需求分析报告、项目立项书、评估满足度、效果报告等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2029, 'SG09011529', '9-1-15 开展中资企业走出去员工“外文+职业技能”培训200人次', 3,
     5, 27, 27, '“外文+职业技能”培训200人次', NULL, '通过与徐工集团等中资企业强化合作、开展走出去员工培训，形成了校企国际化产教融合新范例，培训覆盖了“外文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了中资企业员工培训200人次，实现中资企业走出去员工“外文+职业技能”培训规模化、品牌化。',
     '9-1-15-1 校企合作走出去员工培训协议
9-1-15-2 校企合作走出去员工培训方案、计划、内容、推进会等相关资料（当批次）
9-1-15-3 培训过程资料（当批次开班照片、培训照片、培训讲义、新闻报道等）
9-1-15-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2029, 'SG09011629', '9-1-16 海外中资企业员工“中文+职业技能”培训200人次', 3,
     5, 27, 27, '“中文+职业技能”培训200人次', NULL, '通过与海外中资企业强化合作、开展“徐工-科特迪瓦”等海外员工培训，形成了校企海外培训新范例，培训覆盖了“中文+技能”，明确了培训方案、计划、项目内容、实施举措等内容，预期形成系统性的培训讲义或教材、宣传报道、培训方案等过程资料，实现了境外中资企业员工培训200人次，实现海外中资企业员工“中文+职业技能”培训规模化。',
     '9-1-16-1 海外中资企业员工培训协议
9-1-16-2 培训方案、计划、内容、推进会等相关资料
9-1-16-3 培训过程资料（开班照片、培训照片、培训讲义、新闻报道等）
9-1-16-4 培训项目内容及每个项目培训的名单', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 35, '0,9,35', 2029, 'SG09011729', '9-1-17 开发培训教材或资源包1个', 3,
     11, 22, 22, '培训教材或资源包1个', NULL, '通过开发培训教材或资源包1个，形成了系统化、本土化、数字化的特色教学资源体系，覆盖了工程机械操作、保养、维修等核心技能模块与典型工作任务，明确了理论与实践相结合的教学路径、考核要点及安全规范，预期在培训中实现教学效率与学员技能达标率双提升，实现海外培训的标准化复制、规模化推广与品牌化发展。',
     '9-1-17-1 培训教材或资源包
9-1-17-2 教材获资源包使用证明材料
9-1-11-3 立项分析、实施评估资料（如需求分析报告、项目立项书、评估满足度、效果报告等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2025, 'SG09020125', '9-2-1 成立海外企业和教育标准输出中心', 3,
     5, 29, 29, '海外企业和教育标准输出中心1个', NULL, '通过调研徐工集团等工程机械产业链企业和徐州工业职业技术学院等标准输出情况，形成海外企业和教育标准输出分析报告，成立海外企业和教育标准输出中心1个，明确了建设方案、工作计划、中心职责目标，预期完成标准输出中心成立的相关通知、宣传报道、推进会议等事项，落实标准输出中心组织团队、中心架构、资源配置等，实现企业标准、教育标准输出，为后续持续标准输出奠定扎实基础。',
     '9-2-1-1 筹建调研资料、过程活动材料（调研报告、会议、网站宣传、照片等）
9-2-1-2 建设方案、工作计划（可参照郑和学院）
9-2-1-3 文件：关于成立“海外企业和教育标准输出中心”培训中心的通知（按制式模版）
9-2-1-4 输出的企业标准、教育标准', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2025, 'SG09020225', '9-2-2 针对企业走出去产品进行调研，形成报告1份', 3,
     11, 22, 22, '调研报告1份', NULL, '通过开展对徐工集团等工程机械产业链企业研究、竞品及目标市场分析，形成了一份全面的《企业走出去产品调研报告》1份，覆盖了目标市场的政策法规、竞争格局、渠道生态与用户核心需求，明确了本公司产品在本地化改进、价格定位及服务支持上的关键策略与行动路径，验证产品本身是否符合当地用户的真实需求与使用习惯，评估其在技术标准、功能设计及服务支持等方面的本地化适配程度表，为实现长期可持续教育国际化发展提供支撑。',
     '9-2-2-1 调研报告（按照格式撰写，图文并茂）1份（如问卷调查、访谈、座谈）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2025, 'SG09020325', '9-2-3 针对国（境）外紧缺的专业标准、课程标准等进行调研，形成报告1份', 3,
     8, 9, 9, '调研报告1份', NULL, '通过对目标国（境）外工程机械重点行业领域的系统摸排与案头研究，形成了一份详尽的《国际紧缺专业与课程标准调研报告》1份，覆盖了先进制造、数字技术、工程机械等关键领域内前沿的职业资格框架与课程体系，明确了其核心能力要求、知识构成与我国现有标准的差距，预期在未来三年内通过成果转化，实现了本土人才培养方案的优化升级与国际证书的衔接互认，为我国构建与国际接轨的现代化职业教育体系、精准培养国际化紧缺人才奠定了扎实基础。',
     '9-2-3-1 调研报告（按照格式撰写，图文并茂）1份（如问卷调查、访谈、座谈）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2025, 'SG09020425', '9-2-4 【新增】新建省级郑和学院1个', 3,
     5, 27, 27, '郑和学院1个', '省级及以上', '通过新建省级郑和学院1个，形成了一个集职业技能培训、语言文化传播与教育合作为一体的综合平台，覆盖了主要合作国家的急需专业及工程机械产业领域，明确了学院的治理架构与发展规划，预期正式招生运营后实现首届学员的培养目标，为打造教育对外开放新高地奠定了扎实基础。',
     '9-2-4-1 郑和学院申报书
9-2-4-2 郑和学院建设方案
9-2-4-3 郑和学院立项公示文件
9-2-4-3 成立郑和学院相关推进会、网站报纸报道等过程支撑材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2025, 'SG09020525', '9-2-5 【新增】郑和学院当地招生培养10人', 3,
     5, 30, 30, '培养学生10人', '省级及以上', '通过实体化运作“郑和学院”，形成了 “语言+技能+文化”三位一体的国际化人才培养模式，覆盖了 “一带一路”沿线主要国家的生源与多个核心专业领域，明确了 “校企协同、中外联动”的办学机制与标准化管理流程，实现高质量本土技术技能人才规模化培养10人，为我校职业教育“走出去”、深化国际产能合作与服务“一带一路”倡议奠定扎实基础。',
     '9-2-5-1  国外当地当年招生简章（包含专业介绍、培养目标、招生人数、报名条件、选拔流程等）
9-2-5-2  国外当地学生选拔过程材料（报名学生名单、考核评估记录、最终录取名单公示材料）
9-2-5-3 人才培养方案
9-2-5-4 相关课程标准
9-2-5-5 师资配备表、校企合作协议等
9-2-5-6 管理制度文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2026, 'SG09020626', '9-2-6 输出工程机械相关企业技术或岗位标准1个', 3,
     5, 27, 27, '标准1个', NULL, '通过输出工程机械相关企业技术或岗位标准1个，形成了 一套可复制、可认证的国际化技能评价规范，覆盖了工程机械关键岗位的核心技能要求与作业流程，明确了 该岗位的职业技能等级、培训认证路径与考核规范，预期推动外籍员工通过认证，实现海外服务标准化与人才本地化，为中国标准“走出去”、提升全球服务网络核心竞争力奠定扎实基础。',
     '9-2-6-1输出工程机械相关企业技术或岗位标准应用证明
9-2-6-2输出工程机械相关企业技术或岗位标准（内容）
9-2-6-3 输出过程性材料（如调研、论证、效果证明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2026, 'SG09020726', '9-2-7 输出专业标准或课程标准1个', 3,
     5, 27, 27, '标准1个', NULL, '通过输出专业标准或课程标准1个，形成了 与国际接轨、特色鲜明的专业教学规范与质量认证体系，覆盖了 工程机械相关技术核心课程的知识、技能与素养要求，明确了 教学目标、内容模块、教学方法和考核评价标准，预期在 合作院校内落地实施，显著提升本土化教学的规范性与人才培养的适配度，为我国职业教育标准的海外认证与推广、增强国际话语权奠定扎实基础',
     '9-2-7-1输出专业标准或课程标准应用证明
9-2-7-2输出专业标准或课程标准（内容）
9-2-7-3 输出过程性材料（如调研、论证、效果证明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2026, 'SG09020826', '9-2-8 【新增】郑和学院当地招生培养10人', 3,
     5, 30, 30, '培养学生10人', NULL, '通过实体化运作“郑和学院”，形成了 “语言+技能+文化”三位一体的国际化人才培养模式，覆盖了 “一带一路”沿线主要国家的生源与多个核心专业领域，明确了 “校企协同、中外联动”的办学机制与标准化管理流程，实现高质量本土技术技能人才规模化培养10人，为我国职业教育“走出去”、深化国际产能合作与服务“一带一路”倡议奠定扎实基础。',
     '9-2-8-1国外当地当年招生简章（包含专业介绍、培养目标、招生人数、报名条件、选拔流程等）
9-2-8-2国外当地学生选拔过程材料（报名学生名单、考核评估记录、最终录取名单公示材料）
9-2-8-3培养过程支撑材料（人才培养方案（如有修订）、课程标准（如有修订、新增等）、师资配备表、校企合作协议等（如有修订、新增等）、管理制度文件（如有修订、新增等）、培养过程照片宣传报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2027, 'SG09020927', '9-2-9 输出工程机械相关企业技术或岗位标准1个', 3,
     5, 27, 27, '标准1个', NULL, '通过输出工程机械相关企业技术或岗位标准1个，形成了 一套可复制、可认证的国际化技能评价规范，覆盖了工程机械关键岗位的核心技能要求与作业流程，明确了 该岗位的职业技能等级、培训认证路径与考核规范，预期推动外籍员工通过认证，实现海外服务标准化与人才本地化，为中国标准“走出去”、提升全球服务网络核心竞争力奠定扎实基础。',
     '9-2-9-1输出工程机械相关企业技术或岗位标准应用证明
9-2-9-2输出工程机械相关企业技术或岗位标准（内容）
9-2-9-3 输出过程性材料（如调研、论证、效果证明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2027, 'SG09021027', '9-2-10 输出专业标准或课程标准1个', 3,
     5, 27, 27, '标准1个', NULL, '通过输出专业标准或课程标准1个，形成了 与国际接轨、特色鲜明的专业教学规范与质量认证体系，覆盖了 工程机械相关技术核心课程的知识、技能与素养要求，明确了 教学目标、内容模块、教学方法和考核评价标准，预期在 合作院校内落地实施，显著提升本土化教学的规范性与人才培养的适配度，为我国职业教育标准的海外认证与推广、增强国际话语权奠定扎实基础',
     '9-2-10-1输出专业标准或课程标准应用证明
9-2-10-2输出专业标准或课程标准（内容）
9-2-10-3 输出过程性材料（如调研、论证、效果证明）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2027, 'SG09021127', '9-2-11 【新增】郑和学院当地招生培养10人', 3,
     5, 30, 30, '培养学生10人', NULL, '通过实体化运作“郑和学院”，形成了 “语言+技能+文化”三位一体的国际化人才培养模式，覆盖了 “一带一路”沿线主要国家的生源与多个核心专业领域，明确了 “校企协同、中外联动”的办学机制与标准化管理流程，实现高质量本土技术技能人才规模化培养10人，为我国职业教育“走出去”、深化国际产能合作与服务“一带一路”倡议奠定扎实基础。',
     '9-2-11-1国外当地当年招生简章（包含专业介绍、培养目标、招生人数、报名条件、选拔流程等）
9-2-11-2国外当地学生选拔过程材料（报名学生名单、考核评估记录、最终录取名单公示材料）
9-2-11-3培养过程支撑材料（人才培养方案（如有修订）、课程标准（如有修订、新增等）、师资配备表、校企合作协议等（如有修订、新增等）、管理制度文件（如有修订、新增等）、培养过程照片宣传报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2028, 'SG09021228', '9-2-12 输出工程机械相关企业技术或岗位标准1个', 3,
     5, 27, 27, '标准1个', NULL, '通过输出工程机械相关企业技术或岗位标准1个，形成了 一套可复制、可认证的国际化技能评价规范，覆盖了工程机械关键岗位的核心技能要求与作业流程，明确了 该岗位的职业技能等级、培训认证路径与考核规范，预期推动外籍员工通过认证，实现海外服务标准化与人才本地化，为中国标准“走出去”、提升全球服务网络核心竞争力奠定扎实基础。',
     '9-2-12-1输出工程机械相关企业技术或岗位标准应用证明
9-2-12-2输出工程机械相关企业技术或岗位标准（内容）
9-2-12-3 输出过程性材料（如调研、论证）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2028, 'SG09021328', '9-2-13 输出专业标准或课程标准2个', 3,
     5, 27, 27, '标准2个', NULL, '通过输出专业标准或课程标准1个，形成了 与国际接轨、特色鲜明的专业教学规范与质量认证体系，覆盖了 工程机械相关技术核心课程的知识、技能与素养要求，明确了 教学目标、内容模块、教学方法和考核评价标准，预期在 合作院校内落地实施，显著提升本土化教学的规范性与人才培养的适配度，为我国职业教育标准的海外认证与推广、增强国际话语权奠定扎实基础。',
     '9-2-13-1输出专业标准或课程标准应用证明
9-2-13-2输出专业标准或课程标准（内容）
9-2-13-3 输出过程性材料（如调研、论证）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2028, 'SG09021428', '9-2-14 【新增】郑和学院当地招生培养10人', 3,
     5, 30, 30, '培养学生10人', NULL, '通过实体化运作“郑和学院”，形成了 “语言+技能+文化”三位一体的国际化人才培养模式，覆盖了 “一带一路”沿线主要国家的生源与多个核心专业领域，明确了 “校企协同、中外联动”的办学机制与标准化管理流程，实现高质量本土技术技能人才规模化培养10人，为我国职业教育“走出去”、深化国际产能合作与服务“一带一路”倡议奠定扎实基础。',
     '9-2-14-1国外当地当年招生简章（包含专业介绍、培养目标、招生人数、报名条件、选拔流程等）
9-2-14-2国外当地学生选拔过程材料（报名学生名单、考核评估记录、最终录取名单公示材料）
9-2-14-3培养过程支撑材料（人才培养方案（如有修订）、课程标准（如有修订、新增等）、师资配备表、校企合作协议等（如有修订、新增等）、管理制度文件（如有修订、新增等）、培养过程照片宣传报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2029, 'SG09021529', '9-2-15 输出工程机械相关企业技术或岗位标准1个', 3,
     5, 27, 27, '标准1个', NULL, '通过输出工程机械相关企业技术或岗位标准1个，形成了 一套可复制、可认证的国际化技能评价规范，覆盖了工程机械关键岗位的核心技能要求与作业流程，明确了 该岗位的职业技能等级、培训认证路径与考核规范，预期推动外籍员工通过认证，实现海外服务标准化与人才本地化，为中国标准“走出去”、提升全球服务网络核心竞争力奠定扎实基础。',
     '9-2-15-1输出工程机械相关企业技术或岗位标准应用证明
9-2-15-2输出工程机械相关企业技术或岗位标准（内容）
9-2-15-3 输出过程性材料（如调研、论证）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2029, 'SG09021629', '9-2-16 输出专业标准或课程标准2个', 3,
     5, 27, 27, '标准2个', NULL, '通过输出专业标准或课程标准1个，形成了 与国际接轨、特色鲜明的专业教学规范与质量认证体系，覆盖了 工程机械相关技术核心课程的知识、技能与素养要求，明确了 教学目标、内容模块、教学方法和考核评价标准，预期在 合作院校内落地实施，显著提升本土化教学的规范性与人才培养的适配度，为我国职业教育标准的海外认证与推广、增强国际话语权奠定扎实基础',
     '9-2-16-1输出专业标准或课程标准应用证明
9-2-16-2输出专业标准或课程标准（内容）
9-2-16-3 输出过程性材料（如调研、论证）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 36, '0,9,36', 2029, 'SG09021729', '9-2-17 【新增】郑和学院当地招生培养10人', 3,
     5, 30, 30, '培养学生10人', NULL, '通过实体化运作“郑和学院”，形成了 “语言+技能+文化”三位一体的国际化人才培养模式，覆盖了 “一带一路”沿线主要国家的生源与多个核心专业领域，明确了 “校企协同、中外联动”的办学机制与标准化管理流程，实现高质量本土技术技能人才规模化培养10人，为我国职业教育“走出去”、深化国际产能合作与服务“一带一路”倡议奠定扎实基础。',
     '9-2-17-1国外当地当年招生简章（包含专业介绍、培养目标、招生人数、报名条件、选拔流程等）
9-2-17-2国外当地学生选拔过程材料（报名学生名单、考核评估记录、最终录取名单公示材料）
9-2-17-3培养过程支撑材料（人才培养方案（如有修订）、课程标准（如有修订、新增等）、师资配备表、校企合作协议等（如有修订、新增等）、管理制度文件（如有修订、新增等）、培养过程照片宣传报道）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2025, 'SG09030125', '9-3-1 筹建“海外工程机械技术学院”', 3,
     5, 29, 29, '召开筹建会议1次', NULL, '通过与徐工集团等工程机械产业链企业深化合作，筹建“海外工程机械技术学院”，形成了标准化的海外技术培训输出模式，覆盖了主流产品的核心技术与服务规范，明确了建设方案、工作计划、职责目标以及海外本地人才培养目标，预期完成筹建相关通知、宣传报道、推进会议等事项，为海外市场的长期深耕与品牌建设奠定了扎实的本地化人才基础。',
     '9-3-1-1 “海外工程机械技术学院”调研报告
9-3-1-2 筹建“海外工程机械技术学院”过程活动材料（会议、网站宣传、照片等）
9-3-1-3 建设方案、工作计划（可参照郑和学院）
9-3-1-4 文件：关于筹建“海外工程机械技术学院”的通知（按制式模版）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2025, 'SG09030225', '9-3-2 保持中外合作办学规模100人', 3,
     5, 27, 27, '办学规模100人', NULL, '通过与与俄罗斯鄂木斯国立交通大学深化合作，形成了中外合作办学三年办学规模稳定在100人的常态化管理机制，覆盖了招生、教学与就业全过程，明确了质量优先于数量的发展导向，根据学校“三四五六“人才培养模式改革修订中外合作办学人才培养方案，预期在未来实现了项目口碑与毕业生竞争力的双重强化，为其长远发展奠定了扎实的声誉基础。',
     '9-3-2-1  23级、24级、25级学生名单（学号、籍贯、性别等相关信息）
9-3-2-2 近几年招生材料（如招生简章）
9-3-2-3 2025版人才培养方案（修订版）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2025, 'SG09030325', '9-3-3 国际生规模30人', 3,
     5, 29, 29, '国际生规模30人', NULL, '通过在“一带一路”沿线国家或地区开拓“徐工-乌兹”等培训中心，实施“中文+挖掘机操作”、“中文+旋挖钻操作”等多个定制培训项目等工作，形成了“定点实训基地、定制双语课程、定向海外岗位、定标认证体系”的四定人才培养模式，预期完成国际生规模30人，为后续持续推进国际化人才培养奠定基础。',
     '9-3-3-1 国际生招生相关材料（ 如“徐工-乌兹”招生简章）
9-3-3-2 国际生名单（如“徐工-乌兹”留学生名单30人）
9-3-3-3 国际生培养支撑材料（如人培方案、校企联合培养协议、新闻报道、活动照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2025, 'SG09030425', '9-3-4 合作企业海外就业人数60人', 3,
     5, 29, 29, '就业60人', NULL, '通过深化徐工集团等校企合作，强化覆盖“一带一路”沿线多个重点国别市场的技能人才培养，形成了毕业生成功赴合作企业海外项目就业的标杆性成果，明确了国际化人才培养与企业实际需求高度契合的有效路径，预期完成海外就业人数60人，实现了海外人才输出的规模化与品牌化，为打造中国企业与教育“走出去”的协同发展模式奠定了扎实的实践基础。',
     '9-3-4-1 校企合作协议
9-3-4-2  就业人员资料（就业人员名单、就业国别、就业岗位等材料）
9-3-4-3 海外就业证明材料（如就业协议、劳动/服务合同等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2025, 'SG09030525', '9-3-5 学生国际大赛获奖数1项', 3,
     5, 31, 31, '获奖数1项', '省级及以上', '通过构建“以赛促学、赛教融合”的创新人才培养机制与系统的强化训练，形成了学生团队在国际高水平赛事中斩获佳绩的突破性成果，覆盖了技能水平、创新思维与团队协作等核心能力维度，明确了将国际大赛标准融入日常教学以提升人才培养质量的有效路径，实现学生国际萨塞获奖1项，预期在未来实现了从单一奖项到全面开花的可持续竞赛成绩，为学校提升国际知名度、打造一流专业品牌奠定了扎实的示范性基础。',
     '9-3-5-1 比赛证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2026, 'SG09030626', '9-3-6 建立“海外工程机械技术学院', 3,
     5, 29, 29, '“海外工程机械技术学院”1个', NULL, '通过建立“海外工程机械技术学院”，形成了常态化运作与协同发展体系，覆盖“一带一路“相关沿线国家市场区域，明确了管理架构、人才培养标准、建设方案、制度文件以及校企合作路径，预期实现本土化技术人才输送量翻番，为深化全球布局、巩固“一带一路”市场竞争力奠定扎实基础。',
     '
9-3-6-1学院申请、审批等相关材料
9-3-6-2 学院章程、制度文件等
9-3-6-3学院师资、设施、经费证明材料等
9-3-6-4相关推进会、网站报纸报道等过程支撑材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2026, 'SG09030726', '9-3-7 开展中外合作办学双语教学3门', 3,
     5, 32, 32, '双语教学3门', NULL, '通过开展中外合作办学双语教学不少于3门，形成了 “专业筑基、双语赋能、跨文化融合”的国际化课程教学范式，覆盖了机械制造及自动化（中外合作办学）专业的骨干课程体系，明确了 中外学分互认、教学资源共享、质量协同保障的运行机制，预期实现语言能力与专业技能双达标，显著提升就业竞争力，为 构建长效稳健的国际化技术技能人才培养通道，深化教育对外开放内涵奠定扎实基础。',
     '9-3-7-1 课程标准
9-3-7-2 授课计划
9-3-7-3教学过程佐证
9-3-7-4批准文件、合作协议（选择性提供，如文件中课程信息。合作协议中关于课程引进、教学语言约定的条款。）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2026, 'SG09030826', '9-3-8 国际生规模60人', 3,
     5, 30, 30, '国际生规模60人', NULL, '通过在“一带一路”沿线国家或地区开拓“徐工-乌兹”等培训中心，实施“中文+挖掘机操作”、“中文+旋挖钻操作”等多个定制培训项目等工作，形成了“定点实训基地、定制双语课程、定向海外岗位、定标认证体系”的四定人才培养模式，预期完成国际生规模60人，基本实现国际化人才培养规模化发展。',
     '9-3-8-1  国际生招生相关材料（ 如“徐工-乌兹”招生简章）
9-3-8-2  国际生名单（如“徐工-乌兹”留学生名单60人）
9-3-8-3 国际生培养支撑材料（如人培方案、校企联合培养协议、新闻报道、活动照片）
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2026, 'SG09030926', '9-3-9 合作企业海外就业人数60人', 3,
     5, 29, 29, '就业60人', NULL, '通过深化徐工集团等校企合作，强化覆盖“一带一路”沿线多个重点国别市场的技能人才培养，形成了毕业生成功赴合作企业海外项目就业的标杆性成果，明确了国际化人才培养与企业实际需求高度契合的有效路径，预期完成海外就业人数60人，初步实现了海外人才输出的规模化与品牌化，打造了中国企业与教育“走出去”的协同发展模式。',
     '9-3-9-1 相关企业的“校企合作协议”
9-3-9-2 就业人员资料（就业人员名单、就业国别、就业岗位等材料）
9-3-10-3  海外就业证明材料（如就业协议、劳动/服务合同等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2026, 'SG09031026', '9-3-10 学生国际大赛获奖数1项', 3,
     5, 31, 31, '获奖数1项', '省级及以上', '通过构建“以赛促学、赛教融合”的创新人才培养机制与系统的强化训练，形成了学生团队在国际高水平赛事中斩获佳绩的突破性成果，覆盖了技能水平、创新思维与团队协作等核心能力维度，明确了将国际大赛标准融入日常教学以提升人才培养质量的有效路径，实现学生国际大赛获奖1项，预期在未来实现了从单一奖项到全面开花的可持续竞赛成绩，为学校提升国际知名度、打造一流专业品牌奠定了扎实的示范性基础。',
     '9-3-10-1 比赛证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2026, 'SG09031126', '9-3-11 【新增】服务走出去企业及其所在国家1个', 3,
     5, 31, 31, '服务走出去企业及其所在国家1个', NULL, '通过服务“走出去”企业及其所在国不少于1个，形成了 “战略协同、合规运营、本地化服务”为一体的海外发展新模式，覆盖了 目标国别市场从准入、运营到售后服务的全业务流程，明确了 基于当地法规与国际标准的风险管控体系与协同服务流程，预期在实现本地服务响应效率提升与客户满意度显著增长，为 构建长期稳定的海外市场生态、实现从“走出去”到“融进去”的战略升级奠定扎实基础。',
     '9-3-11-1 企业资质证明（或跨境业务）等相关证明
9-3-11-2 境外运营与服务能力证明/外国合作伙伴资质证明等材料
9-3-11-3 校企合作相关协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2027, 'SG09031227', '9-3-12 国际生规模100人以上', 3,
     5, 30, 30, '国际生规模100人', NULL, '通过在“一带一路”沿线国家或地区开拓“徐工-乌兹”等培训中心，实施“中文+挖掘机操作”、“中文+旋挖钻操作”等多个定制培训项目等工作，形成了“定点实训基地、定制双语课程、定向海外岗位、定标认证体系”的四定人才培养模式，预期完成国际生规模1060人，实现国际化人才培养规模化发展。',
     '9-3-12-1  国际生招生相关材料（ 如“徐工-乌兹”招生简章）
9-3-12-2 国际生名单（如“徐工-乌兹”留学生名单60人）
9-3-12-3 国际生培养支撑材料（如人培方案、校企联合培养协议、新闻报道、活动照片）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2027, 'SG09031327', '9-3-13 合作企业海外就业人数60人', 3,
     5, 29, 29, '就业60人', NULL, '通过深化徐工集团等校企合作，强化覆盖“一带一路”沿线多个重点国别市场的技能人才培养，形成了毕业生成功赴合作企业海外项目就业的标杆性成果，明确了国际化人才培养与企业实际需求高度契合的有效路径，预期完成海外就业人数60人，基本实现了海外人才输出的规模化与品牌化，完善了中国企业与教育“走出去”的协同发展模式。',
     '9-3-13-1 相关企业的“校企合作协议”
9-3-13-2  就业人员资料（就业人员名单、就业国别、就业岗位等材料）
9-3-13-3  海外就业证明材料（如就业协议、劳动/服务合同等）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2027, 'SG09031427', '9-3-14 学生国际大赛获奖数1项', 3,
     5, 31, 31, '获奖数1项', '省级及以上', '通过构建“以赛促学、赛教融合”的创新人才培养机制与系统的强化训练，形成了学生团队在国际高水平赛事中斩获佳绩的突破性成果，覆盖了技能水平、创新思维与团队协作等核心能力维度，明确了将国际大赛标准融入日常教学以提升人才培养质量的有效路径，实现学生国际萨塞获奖1项，预期在未来实现了从单一奖项到全面开花的可持续竞赛成绩，为学校提升国际知名度、打造一流专业品牌奠定了扎实的示范性基础。',
     '9-3-14-1 比赛证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2027, 'SG09031527', '9-3-15 省级及以上媒体报道2篇', 3,
     5, 27, 27, '媒体报道2篇', '省级及以上', '通过获得省级及以上媒体报道2篇，形成了立体化、权威性的品牌宣传与成果展示格局，覆盖了主流官方媒体与行业核心受众，明确了项目在服务国家战略、推动职教改革方面的标杆形象与核心价值，预期在短期内显著提升项目公信力与社会认知度，并吸引更多优质生源与合作伙伴，为塑造卓越公众形象、获取持续政策与资源支持奠定扎实基础。',
     '9-3-15-1媒体报道2篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2027, 'SG09031627', '9-3-16 【新增】服务走出去企业及其所在国家1个', 3,
     5, 31, 31, '服务走出去企业及其所在国家1个', NULL, '通过服务“走出去”企业及其所在国不少于1个，形成了 “战略协同、合规运营、本地化服务”为一体的海外发展新模式，覆盖了 目标国别市场从准入、运营到售后服务的全业务流程，明确了 基于当地法规与国际标准的风险管控体系与协同服务流程，预期在实现本地服务响应效率提升与客户满意度显著增长，为 构建长期稳定的海外市场生态、基本实现从“走出去”到“融进去”的战略升级。',
     '9-3-16-1 企业资质证明（或跨境业务）等相关证明
9-3-16-2 境外运营与服务能力证明（或外国合作伙伴资质）证明等材料
9-3-16-3 校企合作相关协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2028, 'SG09031728', '9-3-17 凝练“定向+定制+定点”的国际生人才培养案例', 3,
     5, 29, 29, '人才培养案例1个', NULL, '通过凝练“定向+定制+定点”的国际生人才培养案例，形成了特色国际化人才培养新模式，覆盖了 主要合作企业海外业务所在国别及核心岗位群，明确了 从招生选拔、课程定制到实习就业的校企协同、过程管理的标准化流程，预期在 项目周期内实现人才本地化留存率与雇主满意度双提升，为 打造可复制、可推广的中国职教海外办学品牌范式奠定扎实基础。',
     '9-3-17-1 人才需求与可行性分析报告（体现定向、定制、定点）
9-3-17-2 定制化人才培养方案
9-3-17-3 定制化的过程支撑材料（如师资、课程、讲义等，实习、实训、就业等培养过程材料，企业评价、毕业生跟踪报告等结果材料）
9-3-17-4 “定向+定制+定点”的国际生人才培养案例报告1份
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2028, 'SG09031828', '9-3-18 合作企业海外就业人数60人，国际生规模150人以上', 3,
     5, 29, 29, '海外就业人数60人，国际生规模150人', NULL, '通过深化徐工集团等校企合作与定向培养，形成了毕业生成功赴合作企业海外项目就业的标杆性成果，覆盖了“一带一路”沿线多个重点国别市场，明确了国际化人才培养与企业实际需求高度契合的有效路径，预期在成功模式的复制推广下未完成海外就业人数60人，实现了海外人才输出的规模化与品牌化。
通过在“一带一路”沿线国家或地区开拓“徐工-乌兹”等培训中心，实施“中文+挖掘机操作”、“中文+旋挖钻操作”等多个定制培训项目等工作，形成了“定点实训基地、定制双语课程、定向海外岗位、定标认证体系”的四定人才培养模式，预期完成国际生规模150人。',
     '9-3-12-1 就业学生名单60人以上及支撑材料（如就业协议/劳动合同/服务合同）
9-3-12-2 国际生150人培养支撑材料（如“徐工乌兹”现代学徒培养推进相关过程材料）
9-3-12-3 相关企业的“校企合作协议
9-3-12-4 就业人员名单、就业国别、就业岗位等资料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2028, 'SG09031928', '9-3-19 学生国际大赛获奖数1项', 3,
     5, 31, 31, '获奖数1项', '省级及以上', '通过构建“以赛促学、赛教融合”的创新人才培养机制与系统的强化训练，形成了学生团队在国际高水平赛事中斩获佳绩的突破性成果，覆盖了技能水平、创新思维与团队协作等核心能力维度，明确了将国际大赛标准融入日常教学以提升人才培养质量的有效路径，实现学生国际大赛获奖1项，预期在未来实现了从单一奖项到全面开花的可持续竞赛成绩，为学校提升国际知名度、打造一流专业品牌奠定了扎实的示范性基础。',
     '9-3-19-1 比赛证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2028, 'SG09032028', '9-3-20 省级及以上媒体报道2篇', 3,
     5, 27, 27, '媒体报道2篇', '省级及以上', '通过获得省级及以上媒体报道2篇，形成了立体化、权威性的品牌宣传与成果展示格局，覆盖了主流官方媒体与行业核心受众，明确了项目在服务国家战略、推动职教改革方面的标杆形象与核心价值，预期在短期内显著提升项目公信力与社会认知度，并吸引更多优质生源与合作伙伴，为塑造卓越公众形象、获取持续政策与资源支持奠定扎实基础。',
     '9-3-20-1媒体报道2篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2028, 'SG09032128', '9-3-21 【新增】服务走出去企业及其所在国家1个', 3,
     5, 31, 31, '服务走出去企业及其所在国家1个', NULL, '通过服务“走出去”企业及其所在国不少于1个，形成了 “战略协同、合规运营、本地化服务”为一体的海外发展新模式，覆盖了 目标国别市场从准入、运营到售后服务的全业务流程，明确了 基于当地法规与国际标准的风险管控体系与协同服务流程，预期在实现本地服务响应效率提升与客户满意度显著增长，为 构建长期稳定的海外市场生态、实现从“走出去”到“融进去”的战略升级。',
     '9-3-21-1 企业资质证明（或跨境业务）等相关证明
9-3-21-2 境外运营与服务能力证明（或外国合作伙伴资质）证明等材料
9-3-21-3 校企合作相关协议', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2029, 'SG09032229', '9-3-22 推广“定向+定制+定点”的国际生人才培养模式', 3,
     5, 29, 29, '人才培养案例1个', NULL, '通过推广“定向+定制+定点”的国际生人才培养模式，形成了 一套可复制、可评估的标准化运行机制，覆盖了 多国别、多专业的海外业务布局与人才需求，明确了 从市场调研、方案设计到实施保障的全流程标准与各方权责，预期将该模式成功复制到超过海外重点项目，为 实现中国职教模式的大规模、高效化海外输出与品牌生态建设奠定扎实基础。',
     '9-3-22-1应用或借鉴证明材料
9-3-22-2推广过程材料（如多媒体展示包、宣传片/案例短片、推广活动照片）
9-3-22-3认可及社会影响材料（如媒体报道、表彰文件、获奖证书）', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2029, 'SG09032329', '9-3-23 合作企业海外就业人数60人，国际生规模200人以上', 3,
     5, 29, 29, '海外就业人数60人，国际生规模200人', NULL, '通过深化与徐工集团等校企合作，形成了毕业生成功赴合作企业海外项目就业的标杆性成果，明确了国际化人才培养与企业实际需求高度契合的有效路径，预期在成功模式的复制推广下未完成海外就业人数60人，实现了海外人才输出的规模化与品牌化。通过在“一带一路”沿线国家或地区开拓“徐工-乌兹”等培训中心，实施“中文+挖掘机操作”、“中文+旋挖钻操作”等多个定制培训项目等工作，形成了“定点实训基地、定制双语课程、定向海外岗位、定标认证体系”的四定人才培养模式，预期完成国际生规模200人。',
     '9-3-23-1 就业学生名单60人以上及支撑材料（如就业协议/劳动合同/服务合同）
9-3-23-2 国际生150人培养支撑材料（如“徐工乌兹”现代学徒培养推进相关过程材料）
9-3-23-3 相关企业的“校企合作协议
9-3-23-4 就业人员名单、就业国别、就业岗位等资料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2029, 'SG09032429', '9-3-24 学生国际大赛获奖数1项', 3,
     5, 31, 31, '获奖数1项', '省级及以上', '通过构建“以赛促学、赛教融合”的创新人才培养机制与系统的强化训练，形成了学生团队在国际高水平赛事中斩获佳绩的突破性成果，覆盖了技能水平、创新思维与团队协作等核心能力维度，明确了将国际大赛标准融入日常教学以提升人才培养质量的有效路径，实现学生国际大赛获奖1项，预期在未来实现了从单一奖项到全面开花的可持续竞赛成绩，为持续提升专业群国际知名度、持续增强专业群品牌示范性效应。',
     '9-3-24-1 比赛证书', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 37, '0,9,37', 2029, 'SG09032529', '9-3-25 省级及以上媒体报道2篇', 3,
     5, 27, 27, '媒体报道2篇', '省级及以上', '通过获得省级及以上媒体报道2篇，形成了立体化、权威性的品牌宣传与成果展示格局，覆盖了主流官方媒体与行业核心受众，明确了项目在服务国家战略、推动职教改革方面的标杆形象与核心价值，预期在短期内显著提升项目公信力与社会认知度，并吸引更多优质生源与合作伙伴，为塑造卓越公众形象、获取持续政策与资源支持奠定扎实基础。',
     '9-3-25-1媒体报道2篇', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2025, 'SG10010125', '10-1-1 筹建工程机械产业发展研究院', 3,
     11, 33, 33, '成立筹备小组1个', NULL, '通过成立工程机械产业发展研究院筹备小组，系统调研产业趋势与需求、联动多方主体整合资源并制定分阶段计划，形成研究院筹建的基础协作框架与推进体系，明确研究院的核心定位、各方权责投入及关键筹建节点与应对预案，为后续研究院正式组建与运营提供清晰指引。',
     '10-1-1-1 关于成立研究院筹备小组的正式通知或批复文件
10-1-1-2 筹备小组成员名单及职责分工
10-1-1-3 研究院筹建工作方案或计划书
10-1-1-4 筹备工作会议纪要等过程性文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2025, 'SG10010225', '10-1-2 出台工程机械产业发展研究院章程与制度', 3,
     11, 33, 33, '出台章程与制度1套', NULL, '通过开展工程机械产业发展研究院章程与制度的调研起草、多方论证及修订完善工作，形成覆盖研究院组织架构、决策机制、运营管理、人才激励、成果转化等核心领域的系统化章程与制度体系，明确研究院各主体权责边界、日常运营规范及长期发展保障机制，为研究院规范化、可持续运营提供制度支撑。',
     '10-1-2-1 《工程机械产业发展研究院章程》
10-1-2-2 研究院各项管理制度汇编
10-1-2-3 章程与制度的审议通过会议纪要或签发文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2025, 'SG10010325', '10-1-3 召开产业研究院年会，发布《工程机械产业发展报告》', 3,
     11, 25, 25, '发布《产业发展报告》1个', NULL, '通过组织召开产业研究院年会，汇聚行业专家、企业代表及科研人员开展产业研讨，并同步编制发布《工程机械产业发展报告》，形成集行业交流共识与产业数据研判于一体的成果输出，明确工程机械产业当前发展现状、核心技术瓶颈及未来市场趋势，为研究院后续科研方向规划与行业企业发展决策提供参考依据。',
     '10-1-3-1 首届年会会议通知与完整议程
10-1-3-2 参会人员签到表或嘉宾名单
10-1-3-3 首版《工程机械产业发展报告》正式发布稿
10-1-3-4 年会现场照片、新闻稿、媒体报道等影像图文资料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2025, 'SG10010425', '10-1-4 【新增】编制“技能人才需求清单”“技术创新需求清单”1套', 3,
     11, 23, 23, '编制技能人才需求、技术创新需求清单1套', NULL, '通过编制 “技能人才需求清单”“技术创新需求清单” 形成了贴合工程机械产业发展实际的需求导向体系，覆盖了徐工集团各大主机、零配件企业及相关院校的技能人才培养方向与技术创新重点领域，明确了产业发展所需的各类技能人才规格标准与关键技术创新突破方向，预期在人才精准培养、技术协同攻关等方面实现了供需精准对接、资源高效整合，为产业链高质量发展与校企深度合作奠定扎实基础。',
     '10-1-4-1 技能人才需求清单
10-1-4-2 技术创新需求清单
10-1-4-3 支持清单编制的调研报告、数据分析过程文件
', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2026, 'SG10010526', '10-1-5 建成工程机械产业发展研究院', 3,
     11, 33, 33, '成立工程机械产业发展研究院1个', NULL, '通过成立工程机械产业发展研究院，构建了一个集产业研究、技术创新与政策咨询于一体的综合性平台，明确了研究院的组织架构、研究方向与运行机制，为工程机械产业的可持续发展提供智力支持和战略引导，预期通过研究院的运作推动产业关键技术突破与资源整合，提升产业核心竞争力。',
     '10-1-5-1 工程机械产业发展研究院成立批复文件
10-1-5-2 研究院组织架构与人员名单
10-1-5-3 研究院章程或制度建设文件
10-1-5-4 研究院成立相关会议纪要或新闻报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2026, 'SG10010626', '10-1-6 召开产业研究院年会，发布《工程机械产业发展报告》', 3,
     11, 25, 25, '召开年会1次，发布《产业发展报告》1个', NULL, '通过召开产业研究院年会，汇聚行业专家与企业代表，深入探讨工程机械产业现状与趋势，同时发布《工程机械产业发展报告》，系统梳理产业发展动态与挑战，为政策制定和企业决策提供参考，预期增强产业协同与信息共享，推动产业高质量发展。',
     '10-1-6-1 年会会议通知与议程
10-1-6-2 参会人员签到表或名单
10-1-6-3 《工程机械产业发展报告》
10-1-6-4 年会现场照片或新闻报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2026, 'SG10010726', '10-1-7 【新增】更新“技能人才需求清单”“技术创新需求清单”', 3,
     11, 23, 23, '编制技能人才需求、技术创新需求清单1套', NULL, '通过编制并更新技能人才需求清单和技术创新需求清单，精准识别工程机械产业在人才与技术方面的短板与需求，为教育培训机构和企业研发提供导向，预期优化人才供给与技术资源配置，助力产业转型升级与创新能力提升。',
     '10-1-7-1 技能人才需求清单
10-1-7-2 技术创新需求清单
10-1-7-3 需求调研报告或数据分析材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2027, 'SG10010827', '10-1-8 召开产业研究院年会，发布《工程机械产业发展报告》', 3,
     11, 25, 25, '召开年会1次，发布《产业发展报告》1个', NULL, '通过年会召开和报告发布，持续跟踪工程机械产业发展脉络，促进产学研用深度融合，预期形成年度产业分析机制，为行业提供前瞻性指导，推动产业生态优化与国际化发展。',
     '10-1-8-1 年会会议纪要与总结报告
10-1-8-2 《工程机械产业发展报告》
10-1-8-3 年会相关宣传材料或媒体报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2027, 'SG10010927', '10-1-9 【新增】更新“技能人才需求清单”“技术创新需求清单”', 3,
     11, 23, 23, '编制技能人才需求、技术创新需求清单1套', NULL, '通过需求清单，动态反映工程机械产业在技能人才和技术创新方面的最新需求，预期形成常态化需求监测体系，为政策调整和企业战略规划提供数据支撑，促进产业与人才、技术要素的精准对接。',
     '10-1-9-1 技能人才需求清单
10-1-9-2 技术创新需求清单
10-1-9-3 需求调研报告或数据分析材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2028, 'SG10011028', '10-1-10 修订工程机械产业发展研究院章程或制度', 3,
     11, 33, 33, '修订章程与制度1套', NULL, '通过修订研究院章程或制度，完善治理结构与管理规范，提升研究院运行效率与适应性，预期明确权责分工与决策机制，保障研究院长期稳定发展，并为产业合作与创新活动提供制度保障。',
     '10-1-10-1 修订后的研究院章程或制度文本
10-1-10-2 修订过程会议记录或征求意见稿', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2028, 'SG10011128', '10-1-11 召开产业研究院年会，发布《工程机械产业发展报告》', 3,
     11, 33, 33, '召开年会1次，发布《产业发展报告》1个', NULL, '通过年会平台深化产业交流，发布年度发展报告，系统分析市场趋势与技术进展，预期强化研究院的行业影响力，推动产学研合作项目落地，促进工程机械产业向智能化转型。',
     '10-1-11-1 年会会议通知与议程
10-1-11-2 《工程机械产业发展报告》
10-1-11-3 年会相关宣传材料或媒体报道', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2028, 'SG10011128', '10-1-12 【新增】更新“技能人才需求清单”“技术创新需求清单”', 3,
     11, 23, 23, '编制技能人才需求、技术创新需求清单1套', NULL, '通过需求清单，保持产业需求与外部资源的动态匹配，预期形成需求导向的产业服务机制，推动职业教育与技术创新体系优化，为工程机械产业高质量发展注入新动能。',
     '10-1-12-1 技能人才需求清单
10-1-12-2 技术创新需求清单
10-1-12-3 需求调研报告或数据分析材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2029, 'SG10011329', '10-1-13 调整工程机械产业研究机构', 3,
     11, 33, 33, '调整机构1次', NULL, '通过调整研究机构，优化资源配置与职能定位，适应产业发展新要求，预期提升研究院的响应速度与研究能力，强化其在产业政策制定和技术推广中的支撑作用，促进机构运行高效化与专业化。',
     '10-1-13-1 机构调整方案
10-1-13-2 调整后组织架构图与职责说明
10-1-13-3 调整效果初步评估报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2029, 'SG10011429', '10-1-14 召开产业研究院年会，发布《工程机械产业发展报告》', 3,
     11, 33, 33, '召开年会1次，发布《产业发展报告》1个', NULL, '通过年会与报告发布，总结年度成果并规划未来方向，预期构建产业发展的长效交流机制，增强研究院的品牌权威性，推动工程机械产业在国内外市场中的竞争力提升。',
     '10-1-14-1 年会活动方案与参会名单
10-1-14-2 《工程机械产业发展报告》
10-1-14-3 年会决议或倡议文件
10-1-14-4 媒体报道或社会反响材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 38, '0,10,38', 2029, 'SG10011529', '10-1-15 【新增】更新“技能人才需求清单”“技术创新需求清单”', 3,
     11, 23, 23, '编制技能人才需求、技术创新需求清单1套', NULL, '通过需求清单更新，固化工程机械产业在人才与技术领域的核心需求，预期为长期产业规划与政策设计提供依据，促进教育、科技与产业深度融合，支撑产业可持续创新与全球市场拓展。',
     '10-1-15-1 技能人才需求清单
10-1-15-2 技术创新需求清单
10-1-15-3 清单终审会议纪要', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2025, 'SG10020125', '10-2-1 开展工程机械高端装备制造业调研', 3,
     11, 33, 33, '开展调研1次，发布调研报告1个', NULL, '通过系统性的产业调研，全面掌握工程机械高端装备制造业的发展现状、产业链布局、技术水平和市场趋势，形成高质量的调研报告，为政府产业规划、企业战略决策和研究院后续工作提供精准的数据支撑和方向指引。',
     '10-2-1-1 调研工作方案及调研问卷
10-2-1-2 《工程机械高端装备制造业调研报告》
10-2-1-3 调研原始数据汇总及分析过程材料', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2025, 'SG10020225', '10-2-2 制定行业或企业标准3个', 3,
     11, 26, 26, '制定行业或企业标准3个', NULL, '通过牵头或参与制定多项行业或企业标准，填补相关领域技术规范空白，统一产品规格和质量要求，促进行业技术进步和产业协同，提升区域内工程机械产业的标准化水平和市场竞争力。',
     '10-2-2-1 标准正式发布文本或批准文件
10-2-2-2 标准编制说明
10-2-2-3 技术论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2026, 'SG10020326', '10-2-3 制定行业或企业标准3个', 3,
     11, 26, 26, '制定行业或企业标准3个', NULL, '通过牵头或参与制定多项行业或企业标准，填补相关领域技术规范空白，统一产品规格和质量要求，促进行业技术进步和产业协同，提升区域内工程机械产业的标准化水平和市场竞争力。',
     '10-2-3-1 标准正式发布文本或批准文件
10-2-3-2 标准编制说明
10-2-3-3 技术论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2026, 'SG10020426', '10-2-4 推动上升为国家/国际标准1个', 3,
     11, 26, 26, '参与制定国家/国际标准1个', '国家级', '通过将成熟的行业或企业标准推动上升为国家或国际标准，显著增强在工程机械领域的话语权和影响力，引领行业技术发展潮流，为企业开拓国内外市场扫除技术壁垒，实现从“跟跑”到“并跑”乃至“领跑”的跨越。',
     '10-2-4-1 正式发布的国家/国际标准文本
10-2-4-2 相关新闻报道或行业主管部门的认可文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2027, 'SG10020527', '10-2-5 制定行业或企业标准3个', 3,
     11, 26, 26, '制定行业或企业标准3个', NULL, '通过牵头或参与制定多项行业或企业标准，填补相关领域技术规范空白，统一产品规格和质量要求，促进行业技术进步和产业协同，提升区域内工程机械产业的标准化水平和市场竞争力。',
     '10-2-5-1 标准正式发布文本或批准文件
10-2-5-2 标准编制说明
10-2-5-3 技术论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2027, 'SG10020627', '10-2-6 推动上升为国家/国际标准1个', 3,
     11, 26, 26, '参与制定国家/国际标准1个', '国家级', '通过将成熟的行业或企业标准推动上升为国家或国际标准，显著增强在工程机械领域的话语权和影响力，引领行业技术发展潮流，为企业开拓国内外市场扫除技术壁垒，实现从“跟跑”到“并跑”乃至“领跑”的跨越。',
     '10-2-6-1 正式发布的国家/国际标准文本
10-2-6-2 相关新闻报道或行业主管部门的认可文件', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2028, 'SG10020728', '10-2-7 制定行业或企业标准3个', 3,
     11, 26, 26, '制定行业或企业标准3个', NULL, '通过牵头或参与制定多项行业或企业标准，填补相关领域技术规范空白，统一产品规格和质量要求，促进行业技术进步和产业协同，提升区域内工程机械产业的标准化水平和市场竞争力。',
     '10-2-7-1 标准正式发布文本或批准文件
10-2-7-2 标准编制说明
10-2-7-3 技术论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2029, 'SG10020829', '10-2-8 制定行业或企业标准3个', 3,
     11, 26, 26, '制定行业或企业标准3个', NULL, '通过牵头或参与制定多项行业或企业标准，填补相关领域技术规范空白，统一产品规格和质量要求，促进行业技术进步和产业协同，提升区域内工程机械产业的标准化水平和市场竞争力。',
     '10-2-8-1 标准正式发布文本或批准文件
10-2-8-2 标准编制说明
10-2-8-3 技术论证报告', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 39, '0,10,39', 2029, 'SG10020929', '10-2-9 推动企业标准在市域联合体中应用', 3,
     11, 26, 26, '推动企业标准在市域联合体中应用1次', NULL, '通过推动自主制定的企业标准在市域产教联合体内得到实际应用与推广，促进技术创新成果的快速转化和产业协同效率的提升，形成“标准引领、产业协同”的良好生态，增强联合体内企业的核心竞争力。',
     '10-2-9-1 企业标准在市域联合体中推广应用的工作方案
10-2-9-2 联合体成员单位采纳并应用该标准的证明文件
10-2-9-3 标准应用的效果评估报告或典型案例
10-2-9-4 相关推广活动、培训的会议记录或照片', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2025, 'SG10030125', '10-3-1 转化教学、职业技能类标准4个', 3,
     8, 9, 9, ' 转化教学、职业技能类标准4个', NULL, '通过转化教学标准与职业技能标准，持续优化核心课程内容，增强其与岗位能力要求的匹配度，进而提升学生的岗位适应能力。计划设立试点班级开展实践探索，预计试点班级学生在相关技能考核中的通过率提升5%。在此基础上，进一步推动专业人才培养方案的系统重构，将新标准模块融入课程体系，实现人才培养质量的整体提升',
     '10-3-1-1 标准文本4套
10-3-1-2《机械制造及自动化专业人才培养方案》1份
10-3-1-3 试点班级教学实施记录、学生技能考核成绩分析报告1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2025, 'SG10030225', '10-3-2 发布《工程机械产业人才需求分析报告》', 3,
     11, 22, 22, '发布《工程机械产业人才需求分析报告》1个', NULL, '通过系统调研与分析，明确未来区域工程机械产业紧缺岗位类型，为专业设置与调整提供科学依据。调研样本覆盖30家以上企业，确保报告的权威性与参考价值，助力学校精准对接产业需求，优化人才培养结构。',
     '10-3-2-1 《工程机械产业人才需求分析报告（2025）》1份
10-3-2-2 调研样本清单1份
10-3-2-3 问卷/访谈提纲1份
10-3-2-4 统计分析数据1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2026, 'SG10030326', '10-3-3 转化教学、职业技能类标准4个', 3,
     8, 9, 9, ' 转化教学、职业技能类标准4个', NULL, '通过持续转化教学与职业技能标准，推动实训课程与岗位标准的深度对接，提升实训项目达标率。预计新增1门实训课程实现标准对接，学生双证书获取率提升10%，促进课程体系优化与人才培养质量提升。',
     '10-3-3-1 标准文本4套
10-3-3-2 2个专业修订后的课程体系文件与教学资源包1套
10-3-3-3 学生职业技能等级证书获取情况统计表1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2026, 'SG10030426', '10-3-4 发布《工程机械产业人才需求分析报告》', 3,
     11, 22, 22, '发布《工程机械产业人才需求分析报告》1个', NULL, '通过《工程机械产业人才需求分析报告》动态追踪工程机械产业人才结构变化，识别新兴岗位类型，为课程体系的前瞻性调整提供依据。调研范围扩展至50家以上企业，增强报告的区域代表性，支撑专业布局与课程设置的持续优化。',
     '10-3-4-1 《工程机械产业人才需求分析报告（2026）》1份
10-3-4-2 调研样本清单1份
10-3-2-3 问卷/访谈提纲1份
10-3-4-4 统计分析数据1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2027, 'SG10030527', '10-3-5 转化教学、职业技能类标准4个', 3,
     8, 9, 9, ' 转化教学、职业技能类标准4个', NULL, '推动专业课程与最新技术标准同步更新，提升学生项目作品的优良率。建成覆盖“设计-制造-运维”全流程的跨专业模块化课程群，促进学生综合能力与创新能力的协同发展。',
     '10-3-5-1 标准文本及校企联合发布文件4份；
10-3-5-2 模块化课程群建设方案与教学实施记录1套
10-3-5-3 学生项目作品评价记录及企业评价反馈表1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2027, 'SG10030627', '10-3-6 发布《工程机械产业人才需求分析报告》', 3,
     11, 22, 22, '发布《工程机械产业人才需求分析报告》1个', NULL, '基于多年调研数据，并结合本年度《工程机械产业人才需求分析报告》，提出区域人才供需平衡建议，构建人才预测模型，提升预测准确率，为产业政策制定与人才培养规划提供科学支撑。',
     '10-3-6-1 《工程机械产业人才需求分析报告（2027）》1份
10-3-6-2 调研样本清单1份
10-3-2-3 问卷/访谈提纲1份
10-3-6-4 统计分析数据1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2028, 'SG10030728', '10-3-7 转化教学、职业技能类标准4个', 3,
     8, 9, 9, ' 转化教学、职业技能类标准4个', NULL, '推动多个专业全面实施“课标融通”，提升学生就业对口率。建成标准资源库，支持多门课程的在线教学与评估，促进教学资源的共建共享与教学质量的持续提升。',
     '10-3-7-1 标准文本4套
10-3-7-2 资源库建设方案1份
10-3-7-3 3个专业实施总结报告与就业对口率统计', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2028, 'SG10030828', '10-3-8 发布《工程机械产业人才需求分析报告》', 3,
     11, 22, 22, '发布《工程机械产业人才需求分析报告》1个', NULL, '通过近五年的人才需求分析报告，形成五年人才需求趋势图谱，为学校中长期专业规划提供数据支撑。调研覆盖全省主要产业园区，样本数量超过100家，增强报告的全面性与指导性。',
     '10-3-8-1 《工程机械产业人才需求分析报告（2028）》1份
10-3-8-2 趋势图谱及分析说明1份
10-3-6-3 调研样本清单1份
10-3-2-4 问卷/访谈提纲1份
10-3-6-5 统计分析数据1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2029, 'SG10030929', '10-3-9 转化教学、职业技能类标准4个', 3,
     8, 9, 9, ' 转化教学、职业技能类标准4个', NULL, '完成全部核心课程与标准的对接，提升学生综合能力满意度。形成可复制推广的标准转化模式，并向兄弟院校输出，扩大项目成果的辐射范围与影响力。',
     '10-3-9-1 标准文本4套
10-3-9-2 全校核心课程对标分析报告1套
10-3-9-3 校外推广实施反馈报告1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

INSERT INTO `biz_task` (
    `project_id`, `parent_id`, `ancestors`, `phase`, `task_code`, `task_name`, `level`,
    `dept_id`, `principal_id`, `leader_id`, `exp_target`, `exp_level`, `exp_effect`,
    `exp_material_desc`, `data_type`, `target_value`, `current_value`, `weight`,
    `progress`, `status`, `is_delete`, `create_time`, `update_time`
)
VALUES
    (1, 40, '0,10,40', 2029, 'SG10031029', '10-3-10 发布《工程机械产业人才需求分析报告》', 3,
     11, 22, 22, '发布《工程机械产业人才需求分析报告》1个', NULL, '通过过去及本年度的人才需求数据，建立产业人才数据平台，实现人才需求的实时监测与发布，为政府、企业与院校提供动态、精准的人才信息服务，推动区域人才生态的智能化与协同发展。',
     '10-3-10-1 《工程机械产业人才需求分析报告（2029）》1份
10-3-6-2 调研样本清单1份
10-3-2-3 问卷/访谈提纲1份
10-3-6-4 统计分析数据1套', '1', 0.0, 0.0, 1.0,
     0, '0', 0, NOW(), NOW());

