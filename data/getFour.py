import pandas as pd
import os

def generate_level4_task_sql():
    # 文件配置
    excel_file = "四级任务_映射后.xlsx"
    output_sql = "level4_task_insert.sql"
    table_name = "biz_level4_task"

    # 固定默认值
    default_data_type = "1"
    default_current_value = 0.0000
    default_progress = 0
    default_status = "0"
    default_is_delete = 0

    try:
        # 读取Excel
        df = pd.read_excel(excel_file)
        df = df.dropna(subset=["四级任务"])

        with open(output_sql, "w", encoding="utf-8") as f:
            f.write(f"-- 四级任务表（最终正确版）\n")
            f.write(f"-- task_id 自增，parent_id = Excel中的 task_id（三级任务ID）\n")
            f.write(f"-- 总计 {len(df)} 条数据\n\n")

            # 自增主键 task_id 不写，让数据库自动生成
            f.write(f"INSERT INTO `{table_name}` (\n")
            f.write(f"    `parent_id`,\n")
            f.write(f"    `phase`,\n")
            f.write(f"    `task_name`,\n")
            f.write(f"    `leader_id`,\n")
            f.write(f"    `dept_id`,\n")
            f.write(f"    `data_type`,\n")
            f.write(f"    `target_value`,\n")
            f.write(f"    `current_value`,\n")
            f.write(f"    `progress`,\n")
            f.write(f"    `status`,\n")
            f.write(f"    `is_delete`\n")
            f.write(f") VALUES \n")

            values_list = []
            for index, row in df.iterrows():
                # ===================== 核心正确写法 =====================
                parent_id = int(row.get("task_id", 0))  # 取Excel里的task_id作为parent_id

                phase = int(row.get("年份", 2025)) if pd.notna(row.get("年份")) else 2025
                task_name = str(row.get("四级任务", "")).replace("'", "''")
                leader_id = int(row.get("user_id", 0)) if pd.notna(row.get("user_id")) else 0
                dept_id = int(row.get("dept_id", 0)) if pd.notna(row.get("dept_id")) else 0
                target_value = round(float(row.get("目标值", 1.0)), 4)

                value = (
                    f"({parent_id}, {phase}, '{task_name}', {leader_id}, {dept_id}, "
                    f"'{default_data_type}', {target_value}, {default_current_value}, "
                    f"{default_progress}, '{default_status}', {default_is_delete})"
                )
                values_list.append(value)

            # 写入SQL文件
            for i, val in enumerate(values_list):
                end = ";\n" if i == len(values_list)-1 else ",\n"
                f.write(val + end)

        print("✅ SQL 生成成功！")
        print(f"📄 输出文件：{os.path.abspath(output_sql)}")
        print(f"📊 总条数：{len(df)}")
        print(f"✅ parent_id 已正确读取 Excel 中的 task_id")

    except Exception as e:
        print(f"❌ 错误：{str(e)}")

if __name__ == "__main__":
    generate_level4_task_sql()