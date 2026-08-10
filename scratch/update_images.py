import re

with open('docs/SMARTPHONE.md', 'r', encoding='utf-8') as f:
    content = f.read()

new_content = re.sub(
    r'!\[([^\]]+)\]\(([^)]+)\)',
    r'<img src="\2" width="250" alt="\1" />',
    content
)

with open('docs/SMARTPHONE.md', 'w', encoding='utf-8') as f:
    f.write(new_content)
