import pandas as pd
import re

# 读取 Excel 文件
df = pd.read_excel('产业链党组织工作专项任务清单_完整版拆分_含量化达成情况.xlsx', sheet_name='Sheet1')

# 部门名称到 dept_id 的映射
dept_mapping = {
    '党委组织部': 109,
    '人事处': 106,
    '学工处': 108,
    '教务部': 124,
    '党委宣传部': 107,
    '网络思想政治工作中心': 137,
    '马克思主义学院': 126,
    '工会': 112,
    '团委': 130,
    '科技与产业部': 125,
    '国际教育学院': 114,
    '党委办公室、院长办公室（合署）': 105,
    '继续教育学院': 122,
    '国有资产管理处': 115,
    '现代教育技术中心': 132,
}

# 用户姓名到 user_id 的映射
user_mapping = {
    '阮浩': 110009,
    '赵圣洁': 112212,
    '王敏': 120221,
    '于本成': 110144,
    '高强': 110116,
    '杨宏楼': 110503,
    '侯亚合': 110379,
    '周寒': 121360,
    '高祥秀': 121111,
    '曹凯瑞': 121112,
    '张妍妍': 120115,
    '王博': 110434,
    '刘新良': 110023,
    '孙鹏': 110795,
    '张海波': 110029,
    '孙婷婷': 120311,
    '王建华': 110053,
    '庞世华': 120977,
    '范震波': 110944,
    '郑方': 121000,
    '石卓成': 110188,
    '刘继峰': 110101,
    '李新平': 111353,
    '涂辉': 110887,
    '苏晓丽': 120030,
    '宋广侠': 121517,
    '赵颖': 120877,
    '王锋': 110472,
    '李敢': 110645,
    '张岩': 121359,
    '刘耿龙': 110042,
    '马光辉': 110105,
    '李宗磊': 110314,
    '赵侠': 220044,
    '龙浩': 110164,
    '王艳秋': 120052,
    '赵镇': 111020,
    '孙梦': 121471,
    '李红蕾': 220859,
    '宋子豪': 111616,
    '朱涛': 110279,
    '余心明': 110240,
    '权宁': 110750,
    '褚超': 111634,
    '李赛': 112327,
    '刘娟': 120222,
    '燕硕': 111586,
    '孟宝星': 110261,
    '卓自明': 110264,
    '周天沛': 110254,
    '陈奥林': 112120,
    '周波': 110260,
    '詹国兵': 110234,
    '庄阳': 112044,
    '吴红': 120028,
    '陈华堂': 110092,
    '宁军胜': 110097,
    '罗军': 110031,
    '谭卫东': 110129,
    '周蕊': 120026,
    '刘海涛': 110057,
    '程建伟': 110932,
    '陈祥章': 110124,
    '朱慧芹': 120128,
    '顾广辉': 110220,
    '陈竹': 110133,
    '魏贤运': 110207,
    '徐忠杰': 110228,
    '肖亚杰': 110121,
    '杨勇': 110165,
    '燕传勇': 110312,
    '周树会': 120118,
    '乔向东': 110110,
    '徐云慧': 120380,
    '王方杰': 110125,
}

def get_parent_code_from_task_name(task_name):
    """从四级任务名称中提取父任务编码（如 1-1-1-1 -> 1-1-1）"""
    match = re.match(r'^(\d+-\d+-\d+)-\d+', str(task_name))
    if match:
        return match.group(1)
    return None

def extract_year_from_text(text):
    """从文本中提取年份"""
    if pd.isna(text):
        return 2025
    text = str(text)
    if '2029' in text:
        return 2029
    elif '2028' in text:
        return 2028
    elif '2027' in text:
        return 2027
    elif '2026' in text:
        return 2026
    return 2025

def parse_target_value(target_cell):
    """从目标值列解析数值（第K列，索引10）"""
    if pd.isna(target_cell):
        return 1.0
    
    # 如果是数字类型，直接返回
    if isinstance(target_cell, (int, float)):
        return float(target_cell)
    
    text = str(target_cell).strip()
    # 如果是空字符串，返回1
    if not text or text == 'nan':
        return 1.0
    
    # 尝试直接转换为数字
    try:
        return float(text)
    except ValueError:
        # 如果包含数字，提取第一个数字
        numbers = re.findall(r'(\d+(?:\.\d+)?)', text)
        if numbers:
            return float(numbers[0])
        return 1.0

# 存储生成的SQL
output_sql = []
output_sql.append("-- ==========================================================================")
output_sql.append("-- 插入四级任务（从Excel拆分）")
output_sql.append("-- ==========================================================================")
output_sql.append("")
output_sql.append("USE `biz`;")
output_sql.append("SET NAMES utf8mb4;")
output_sql.append("")

# 遍历Excel每一行
count = 0
for idx, row in df.iterrows():
    task_name = str(row.iloc[0]) if pd.notna(row.iloc[0]) else ''
    if not task_name or '任务' in task_name[:2]:
        continue
    
    # 提取父任务编码
    parent_code = get_parent_code_from_task_name(task_name)
    if not parent_code:
        continue
    
    # 获取Excel中的其他字段
    # 注意：列索引需要根据实际Excel调整
    # 第1列：任务名称
    # 第2列：预期达成情况
    # 第3列：预期成果级别
    # 第4列：预期效果
    # 第5列：佐证材料清单
    # 第9列：归口部门（索引8）
    # 第10列：归口部门负责人（索引9）
    # 第11列：目标值（索引10）
    
    exp_target = str(row.iloc[1]) if pd.notna(row.iloc[1]) else ''  # 预期达成情况
    exp_level = str(row.iloc[2]) if pd.notna(row.iloc[2]) else ''   # 预期成果级别
    exp_effect = str(row.iloc[3]) if pd.notna(row.iloc[3]) else ''   # 预期效果
    exp_material_desc = str(row.iloc[4]) if pd.notna(row.iloc[4]) else ''  # 佐证材料清单
    
    # 获取部门ID（第9列，索引8）
    dept_name = str(row.iloc[8]) if pd.notna(row.iloc[8]) else ''
    dept_id = dept_mapping.get(dept_name, 124)
    
    # 获取负责人ID（第10列，索引9）
    principal_name = str(row.iloc[9]) if pd.notna(row.iloc[9]) else ''
    principal_id = user_mapping.get(principal_name, 110379)
    
    # 专业群责任人（第6列，索引5）
    leader_name = str(row.iloc[5]) if pd.notna(row.iloc[5]) else ''
    leader_id = user_mapping.get(leader_name, 112212)
    
    # 专业群负责人（第7列，索引6）
    auditor_name = str(row.iloc[6]) if pd.notna(row.iloc[6]) else ''
    auditor_id = user_mapping.get(auditor_name, 120221)
    
    # 【重要修改】从目标值列（第11列，索引10）获取目标值
    target_value = parse_target_value(row.iloc[10])
    
    # 年份
    phase = extract_year_from_text(exp_material_desc)
    
    # 清理字符串中的换行符
    exp_target_clean = exp_target.replace('\n', ' ').replace('\r', ' ').strip()
    exp_effect_clean = exp_effect.replace('\n', ' ').replace('\r', ' ').strip()
    exp_material_clean = exp_material_desc.replace('\n', ' ').replace('\r', ' ').strip()
    
    # 生成SQL
    sql = f"""-- {task_name}
INSERT INTO `biz_task` (
    `project_id`, `task_code`, `task_name`, `level`, `parent_id`, `ancestors`, `phase`,
    `dept_id`, `principal_id`, `leader_id`, `auditor_id`,
    `exp_target`, `exp_level`, `exp_effect`, `exp_material_desc`,
    `data_type`, `target_value`, `current_value`, `status`, `is_delete`, `create_time`
)
SELECT
    1, NULL, '{task_name.replace("'", "''")}', 4,
    (SELECT task_id FROM biz_task WHERE task_name LIKE '{parent_code}%' AND level = 3 LIMIT 1),
    CONCAT((SELECT ancestors FROM biz_task WHERE task_name LIKE '{parent_code}%' AND level = 3 LIMIT 1), ',', (SELECT task_id FROM biz_task WHERE task_name LIKE '{parent_code}%' AND level = 3 LIMIT 1)),
    {phase}, {dept_id}, {principal_id}, {leader_id}, {auditor_id},
    '{exp_target_clean.replace("'", "''")}', {f"'{exp_level}'" if exp_level and exp_level != 'nan' else 'NULL'}, '{exp_effect_clean.replace("'", "''")}', '{exp_material_clean.replace("'", "''")}',
    '1', {target_value}, 0.00, '0', 0, NOW();
"""
    output_sql.append(sql)
    count += 1

# 写入文件
with open('insert_fourth_level_tasks.sql', 'w', encoding='utf-8') as f:
    f.write('\n'.join(output_sql))

print(f"已生成 {count} 条四级任务插入语句")
print(f"输出文件: insert_fourth_level_tasks.sql")