#!/usr/bin/env python3
"""
代码文件转Markdown脚本
专用于收集SpringBoot项目的代码文件内容并转换为Markdown格式
"""

import os
import argparse
from pathlib import Path
from datetime import datetime

# SpringBoot项目常用文件扩展名映射
SPRINGBOOT_EXTENSIONS = {
    # Java核心文件
    '.java': 'java',
    '.kt': 'kotlin',  # Kotlin文件
    '.kts': 'kotlin',  # Kotlin脚本

    # 配置文件
    '.yml': 'yaml',  # YAML配置 (application.yml)
    '.yaml': 'yaml',  # YAML配置
    '.properties': 'properties',  # 属性配置文件
    '.xml': 'xml',  # XML配置(如mybatis、spring配置)
    '.json': 'json',  # JSON配置

    # 构建工具
    '.gradle': 'groovy',  # Gradle构建文件
    '.gradle.kts': 'kotlin',  # Gradle Kotlin DSL
    'pom.xml': 'xml',  # Maven POM文件

    # 前端相关(可选，SpringBoot前后端分离/内嵌前端)
    '.js': 'javascript',  # JavaScript
    '.html': 'html',  # HTML
    '.css': 'css',  # CSS
    '.vue': 'vue',  # Vue组件

    # 脚本文件
    '.sh': 'bash',  # Shell脚本
    '.bat': 'batch',  # Windows批处理
    '.cmd': 'batch',  # Windows命令脚本

    # 文档和其他配置
    '.md': 'markdown',  # Markdown文档
    '.sql': 'sql',  # SQL脚本
    '.txt': 'text'  # 文本文件
}

# SpringBoot项目要排除的目录（增强版）
EXCLUDE_DIRS = {
    '.git', '__pycache__', 'node_modules', 'venv', '.env',
    'dist', 'build', 'target', 'out', 'bin', 'obj',
    # SpringBoot特有的排除目录
    '.gradle', 'build', 'gradle', 'gradlew.bat', 'gradlew',
    'logs', 'temp', 'tmp', '.idea', '.vscode', '*.iml',
    'coverage', 'reports', 'docs/build'
}

# SpringBoot项目要排除的文件
EXCLUDE_FILES = {
    '.DS_Store', 'Thumbs.db', '.gitignore', '.gitattributes',
    # 构建产物和临时文件
    '*.log', '*.tmp', '*.bak', '*.swp', '*.class',
    # IDE相关文件
    '.idea', '.vscode', '*.iml', '*.iws', '*.ipr'
}


def get_language_from_extension(file_path):
    """根据文件扩展名获取编程语言"""
    path = Path(file_path)

    # 特殊处理pom.xml
    if path.name == 'pom.xml':
        return 'xml'

    ext = path.suffix.lower()
    return SPRINGBOOT_EXTENSIONS.get(ext, None)


def should_exclude_file(file_path):
    """检查文件是否应该被排除"""
    path = Path(file_path)

    # 检查是否在排除的目录中
    for part in path.parts:
        if part in EXCLUDE_DIRS:
            return True

    # 检查是否是排除的文件
    if path.name in EXCLUDE_FILES:
        return True

    # 检查是否是隐藏文件（以点开头）
    if path.name.startswith('.') and path.name not in ['.env', '.gitignore']:
        return True

    # 检查文件扩展名是否在SpringBoot支持列表中
    if get_language_from_extension(file_path) is None:
        return True

    return False


def read_file_content(file_path):
    """读取文件内容，处理编码问题"""
    try:
        # 尝试多种编码
        encodings = ['utf-8', 'gbk', 'latin-1', 'iso-8859-1']

        for encoding in encodings:
            try:
                with open(file_path, 'r', encoding=encoding) as f:
                    return f.read()
            except UnicodeDecodeError:
                continue

        # 如果所有编码都失败，尝试二进制读取
        with open(file_path, 'rb') as f:
            content = f.read()
            # 尝试解码为utf-8，忽略错误
            return content.decode('utf-8', errors='ignore')

    except Exception as e:
        return f"无法读取文件: {e}"


def collect_code_files(root_dir):
    """收集所有SpringBoot相关代码文件"""
    code_files = []

    for root, dirs, files in os.walk(root_dir):
        # 排除不需要的目录
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

        for file in files:
            file_path = os.path.join(root, file)

            if should_exclude_file(file_path):
                continue

            # 获取相对路径
            rel_path = os.path.relpath(file_path, root_dir)
            code_files.append((rel_path, file_path))

    # 按文件路径排序
    code_files.sort(key=lambda x: x[0].lower())

    return code_files


def generate_markdown(root_dir, output_file, max_file_size=1024 * 1024):  # 默认1MB
    """生成SpringBoot代码的Markdown文档"""

    print(f"正在扫描SpringBoot项目目录: {root_dir}")
    code_files = collect_code_files(root_dir)

    if not code_files:
        print("未找到任何SpringBoot相关的代码文件")
        return

    print(f"找到 {len(code_files)} 个SpringBoot相关文件")

    with open(output_file, 'w', encoding='utf-8') as md_file:
        # 写入标题
        md_file.write(f"# SpringBoot项目代码汇总\n\n")
        md_file.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        md_file.write(f"项目目录: `{root_dir}`\n\n")
        md_file.write("---\n\n")

        # 写入目录
        md_file.write("## 文件目录\n\n")
        for rel_path, _ in code_files:
            # 生成锚点（移除特殊字符）
            anchor = rel_path.replace('/', '-').replace('.', '-').replace(' ', '-').lower()
            md_file.write(f"- [{rel_path}](#{anchor})\n")
        md_file.write("\n---\n\n")

        # 写入每个文件的内容
        for rel_path, full_path in code_files:
            filename = os.path.basename(rel_path)
            language = get_language_from_extension(filename)

            print(f"处理文件: {rel_path}")

            # 检查文件大小
            file_size = os.path.getsize(full_path)
            if file_size > max_file_size:
                print(f"  警告: 文件过大 ({file_size} bytes)，跳过")
                md_file.write(f"### {rel_path}\n")
                md_file.write(f"\n> 文件过大 ({file_size} bytes)，已跳过\n\n")
                continue

            # 读取文件内容
            content = read_file_content(full_path)

            # 写入Markdown（修复锚点问题）
            anchor = rel_path.replace('/', '-').replace('.', '-').replace(' ', '-').lower()
            md_file.write(f"### <a id='{anchor}'></a>{rel_path}\n")
            md_file.write(f"\n```{language}\n")
            md_file.write(content)

            # 确保代码块以换行结束
            if content and not content.endswith('\n'):
                md_file.write('\n')

            md_file.write("```\n\n")
            md_file.write("---\n\n")

    print(f"\nSpringBoot代码汇总Markdown文件已生成: {output_file}")


def main():
    parser = argparse.ArgumentParser(description='收集SpringBoot项目代码文件并转换为Markdown格式')
    parser.add_argument('-o', '--output', default='springboot_code_summary.md',
                        help='输出Markdown文件名 (默认: springboot_code_summary.md)')
    parser.add_argument('-d', '--dir', default='.',
                        help='要扫描的SpringBoot项目目录 (默认: 当前目录)')
    parser.add_argument('-s', '--max-size', type=int, default=1024 * 1024,
                        help='最大文件大小(bytes)，超过此大小的文件将被跳过 (默认: 1MB)')

    args = parser.parse_args()

    root_dir = os.path.abspath(args.dir)
    output_file = args.output

    if not os.path.exists(root_dir):
        print(f"错误: 目录不存在: {root_dir}")
        return

    generate_markdown(root_dir, output_file, args.max_size)


if __name__ == "__main__":
    main()