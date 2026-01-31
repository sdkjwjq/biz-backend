#!/usr/bin/env python3
"""
代码文件转Markdown脚本
将当前目录及其子目录中的所有代码文件转换为Markdown格式
"""

import os
import argparse
from pathlib import Path
from datetime import datetime

# 常见代码文件的扩展名映射
FILE_EXTENSIONS = {
    # 编程语言
    '.py': 'python',
    '.java': 'java',
    '.js': 'javascript',
    '.ts': 'typescript',
    '.jsx': 'jsx',
    '.tsx': 'tsx',
    '.c': 'c',
    '.cpp': 'cpp',
    '.cc': 'cpp',
    '.h': 'c',
    '.hpp': 'cpp',
    '.cs': 'csharp',
    '.go': 'go',
    '.rs': 'rust',
    '.rb': 'ruby',
    '.php': 'php',
    '.swift': 'swift',
    '.kt': 'kotlin',
    '.kts': 'kotlin',
    '.scala': 'scala',

    # 脚本和配置
    '.sh': 'bash',
    '.bash': 'bash',
    '.zsh': 'bash',
    '.ps1': 'powershell',
    '.yml': 'yaml',
    '.yaml': 'yaml',
    '.json': 'json',
    '.xml': 'xml',
    '.html': 'html',
    '.css': 'css',
    '.scss': 'scss',
    '.less': 'less',
    '.sql': 'sql',
    '.md': 'markdown',

    # 其他
    '.txt': 'text',
    '.cfg': 'ini',
    '.ini': 'ini',
    '.toml': 'toml',
}

# 要排除的目录
EXCLUDE_DIRS = {
    '.git', '__pycache__', 'node_modules', 'venv', '.env',
    'dist', 'build', 'target', 'out', 'bin', 'obj'
}

# 要排除的文件
EXCLUDE_FILES = {
    '.DS_Store', 'Thumbs.db', '.gitignore', '.gitattributes'
}

def get_language_from_extension(file_path):
    """根据文件扩展名获取编程语言"""
    ext = Path(file_path).suffix.lower()
    return FILE_EXTENSIONS.get(ext, 'text')

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
    if path.name.startswith('.'):
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
    """收集所有代码文件"""
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

def generate_markdown(root_dir, output_file, max_file_size=1024*1024):  # 默认1MB
    """生成Markdown文档"""

    print(f"正在扫描目录: {root_dir}")
    code_files = collect_code_files(root_dir)

    if not code_files:
        print("未找到任何代码文件")
        return

    print(f"找到 {len(code_files)} 个文件")

    with open(output_file, 'w', encoding='utf-8') as md_file:
        # 写入标题
        md_file.write(f"# 代码文件汇总\n\n")
        md_file.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        md_file.write(f"源目录: `{root_dir}`\n\n")
        md_file.write("---\n\n")

        # 写入目录
        md_file.write("## 目录\n\n")
        for rel_path, _ in code_files:
            filename = os.path.basename(rel_path)
            md_file.write(f"- [{rel_path}](#{filename.replace('.', '').lower()})\n")
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
                md_file.write(f"#### {rel_path}\n")
                md_file.write(f"\n文件过大 ({file_size} bytes)，已跳过\n\n")
                continue

            # 读取文件内容
            content = read_file_content(full_path)

            # 写入Markdown
            md_file.write(f"#### {rel_path}\n")
            md_file.write(f"\n```{language}\n")
            md_file.write(content)

            # 确保代码块以换行结束
            if content and not content.endswith('\n'):
                md_file.write('\n')

            md_file.write("```\n\n")

    print(f"\nMarkdown文件已生成: {output_file}")

def main():
    parser = argparse.ArgumentParser(description='将代码文件转换为Markdown格式')
    parser.add_argument('-o', '--output', default='code_summary.md',
                       help='输出Markdown文件名 (默认: code_summary.md)')
    parser.add_argument('-d', '--dir', default='.',
                       help='要扫描的目录 (默认: 当前目录)')
    parser.add_argument('-s', '--max-size', type=int, default=1024*1024,
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