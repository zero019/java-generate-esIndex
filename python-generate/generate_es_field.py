import re
import json
import tkinter as tk
from tkinter import scrolledtext, messagebox
import pyperclip
import ctypes

# 定义 Java 类型到 ES 类型的映射字典
import win32con

java_to_es_mapping = {
    "Boolean": "boolean",
    "boolean": "boolean",
    "int": "integer",
    "Integer": "integer",
    "long": "long",
    "Long": "long",
    "float": "float",
    "Float": "float",
    "double": "double",
    "Double": "double",
    "BigDecimal": "text",
    "String": "keyword",  # 使用 keyword 类型表示字符串，适用于精确匹配
}

def generate_es_field(input_text):
    es_fields = {}
    lines = input_text.split('\n')

    for line in lines:
        if line.strip().startswith("private"):
            # Extract field name and data type using regular expression
            match = re.match(r'private\s+(\w+)\s+(\w+)', line.strip())
            if match:
                data_type, field_name = match.groups()

                # Map Java type to ES type
                es_type = java_to_es_mapping.get(data_type, "text")
                es_field = {field_name: {"type": es_type}}
                if es_type == "keyword":
                    es_field = {
                        field_name: {"type": es_type,
                                    "fields": {
                                        "ngram": {
                                            "type": "text",
                                            "analyzer": "ngram_analyzer"
                                        },
                                        "pinyin": {
                                            "type": "text",
                                            "analyzer": "pinyin_analyzer"
                                        }
                                    }
                                }
                    }
                elif es_type == "Date":
                    es_field = {
                        field_name: {"type": es_type},
                        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||strict_date_optional_time||epoch_millis"
                    }

                es_fields.update(es_field)

    return es_fields

#自动创建头部数据和尾部数据组成json
def generate_beginAndEnd(className):
    json_begin = {
        "aliases": {
            className: {}
        }
    }
    json_end = {
        "settings": {
            "index": {
                "number_of_shards": "5",
                "analysis": {
                    "filter": {
                        "pinyin_full_filter": {
                            "lowercase": "true",
                            "keep_original": "false",
                            "keep_first_letter": "false",
                            "keep_separate_first_letter": "false",
                            "type": "pinyin",
                            "limit_first_letter_length": "50",
                            "keep_full_pinyin": "true"
                        },
                        "my_pinyin": {
                            "lowercase": "true",
                            "keep_original": "false",
                            "keep_first_letter": "true",
                            "keep_separate_first_letter": "true",
                            "type": "pinyin",
                            "limit_first_letter_length": "16",
                            "keep_full_pinyin": "true"
                        }
                    },
                    "analyzer": {
                        "pinyin_full_analyzer": {
                            "filter": [
                                "pinyin_full_filter"
                            ],
                            "tokenizer": "ngram_tokenizer"
                        },
                        "ngram_analyzer": {
                            "filter": [
                                "lowercase"
                            ],
                            "tokenizer": "ngram_tokenizer"
                        },
                        "pinyin_analyzer": {
                            "filter": [
                                "my_pinyin"
                            ],
                            "tokenizer": "ngram_tokenizer"
                        }
                    },
                    "tokenizer": {
                        "ngram_tokenizer": {
                            "token_chars": [],
                            "min_gram": "1",
                            "type": "ngram",
                            "max_gram": "1"
                        }
                    }
                }
            },
            "number_of_replicas": "0"
        }
    }
    return json_begin,json_end

def show_result(json_data):
    result_window = tk.Toplevel(root)
    result_window.title("结果展示")

    result_text = scrolledtext.ScrolledText(result_window, wrap=tk.WORD, width=60, height=60)
    result_text.pack()

    # 显示 JSON 数据
    result_text.insert(tk.END, json.dumps(json_data, indent=2))
    result_text.configure(state="disabled")  # 禁用文本框编辑

    pyperclip.copy(json.dumps(json_data, indent=2))
    ctypes.windll.user32.MessageBoxTimeoutW(0, '已剪切(1秒后自动关闭)\n', '自动获取token', win32con.MB_OK, 0, 1000)

def execute_java_code():
    java_code = java_code_text.get("1.0", tk.END)
    # 在这里执行 Java 代码，并处理结果
    # 获取名称创建基础的es索引
    # 添加内容
    # 添加settings
    print(name_entry.get())
    print(java_code)
    es_json = generate_es_field(java_code)
    json_begin, json_end = generate_beginAndEnd(name_entry.get())

    json_all = {}
    json_all.update(json_begin)
    json_all.update(es_json)
    json_all.update(json_end)

    show_result(json_all)

if __name__ == '__main__':
    # 创建主窗口
    root = tk.Tk()
    root.title("Java类字段转换es索引表字段")

    # 添加标签和文本框
    name_label = tk.Label(root, text="输入你的es类别名:")
    name_label.pack()
    name_entry = tk.Entry(root)
    name_entry.pack()

    java_code_label = tk.Label(root, text="输入你要转换的java代码:")
    java_code_label.pack()
    java_code_text = scrolledtext.ScrolledText(root, wrap=tk.WORD, width=50, height=10)
    java_code_text.pack()

    execute_button = tk.Button(root, text="Execute", command=execute_java_code)
    execute_button.pack()

    # 启动主循环
    root.mainloop()










