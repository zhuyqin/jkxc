/**
 * 表格列宽度超界问题批量修复脚本
 * 
 * 功能：
 * 1. 扫描所有Vue文件，查找可能存在列宽度超界问题的表格
 * 2. 生成修复建议报告
 * 3. 可选：自动修复（需要人工确认）
 * 
 * 使用方法：
 * node scripts/fix-table-column-overflow.js [--auto-fix]
 */

const fs = require('fs');
const path = require('path');

// 配置
const CONFIG = {
  // 需要检查的目录
  scanDirs: [
    'src/views/order',
    'src/views/customer',
    'src/views/accounting',
    'src/views/address',
    'src/views/operation'
  ],
  // 需要检查的列名（可能会超长的列）
  longTextColumns: [
    'companyName',
    'corporateName',
    'address',
    'registeredAddress',
    'actualAddress',
    'businessScope',
    'remarks',
    'description'
  ],
  // 建议的最大宽度
  maxWidth: 200,
  // 建议的最大字符数
  maxLength: 20
};

// 扫描结果
const scanResults = {
  totalFiles: 0,
  checkedFiles: 0,
  issuesFound: [],
  suggestions: []
};

/**
 * 递归扫描目录
 */
function scanDirectory(dir) {
  const fullPath = path.join(__dirname, '..', dir);
  
  if (!fs.existsSync(fullPath)) {
    console.warn(`目录不存在: ${dir}`);
    return;
  }
  
  const files = fs.readdirSync(fullPath);
  
  files.forEach(file => {
    const filePath = path.join(fullPath, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      scanDirectory(path.join(dir, file));
    } else if (file.endsWith('.vue')) {
      scanResults.totalFiles++;
      checkVueFile(filePath, path.join(dir, file));
    }
  });
}

/**
 * 检查Vue文件
 */
function checkVueFile(filePath, relativePath) {
  const content = fs.readFileSync(filePath, 'utf-8');
  scanResults.checkedFiles++;
  
  // 检查是否包含表格组件
  if (!content.includes('<a-table') && !content.includes('columns:')) {
    return;
  }
  
  // 检查列定义
  CONFIG.longTextColumns.forEach(columnName => {
    // 匹配列定义模式
    const patterns = [
      // 模式1: { title: 'xxx', dataIndex: 'companyName', width: 200 }
      new RegExp(`dataIndex:\\s*['"]${columnName}['"].*?width:\\s*(\\d+)`, 'g'),
      // 模式2: { title: 'xxx', dataIndex: 'companyName' } (没有width)
      new RegExp(`dataIndex:\\s*['"]${columnName}['"](?!.*?width:)`, 'g')
    ];
    
    patterns.forEach((pattern, index) => {
      const matches = content.match(pattern);
      if (matches) {
        matches.forEach(match => {
          const issue = {
            file: relativePath,
            column: columnName,
            issue: '',
            suggestion: '',
            line: getLineNumber(content, match)
          };
          
          if (index === 0) {
            // 有width，检查是否有ellipsis
            const widthMatch = match.match(/width:\s*(\d+)/);
            const width = widthMatch ? parseInt(widthMatch[1]) : 0;
            
            if (!content.includes(`dataIndex: '${columnName}'`) || 
                !content.includes(`ellipsis: true`)) {
              issue.issue = `列 "${columnName}" 宽度为 ${width}px，但未设置 ellipsis`;
              issue.suggestion = `添加 ellipsis: true 或使用 customRender 截断文本`;
              scanResults.issuesFound.push(issue);
            }
          } else {
            // 没有width
            issue.issue = `列 "${columnName}" 未设置宽度，可能导致布局问题`;
            issue.suggestion = `添加 width: ${CONFIG.maxWidth} 和 ellipsis: true`;
            scanResults.issuesFound.push(issue);
          }
        });
      }
    });
  });
}

/**
 * 获取匹配内容的行号
 */
function getLineNumber(content, match) {
  const index = content.indexOf(match);
  if (index === -1) return 0;
  
  const lines = content.substring(0, index).split('\n');
  return lines.length;
}

/**
 * 生成修复建议
 */
function generateSuggestions() {
  // 按文件分组
  const groupedByFile = {};
  
  scanResults.issuesFound.forEach(issue => {
    if (!groupedByFile[issue.file]) {
      groupedByFile[issue.file] = [];
    }
    groupedByFile[issue.file].push(issue);
  });
  
  // 生成建议
  Object.keys(groupedByFile).forEach(file => {
    const issues = groupedByFile[file];
    const suggestion = {
      file,
      issueCount: issues.length,
      issues: issues,
      fixCode: generateFixCode(issues)
    };
    scanResults.suggestions.push(suggestion);
  });
}

/**
 * 生成修复代码
 */
function generateFixCode(issues) {
  const fixes = [];
  
  issues.forEach(issue => {
    const columnName = issue.column;
    
    // 方案1：使用 ellipsis
    fixes.push({
      method: 'ellipsis',
      code: `{
  title: '${getColumnTitle(columnName)}',
  dataIndex: '${columnName}',
  width: ${CONFIG.maxWidth},
  ellipsis: true,
  customRender: (text) => {
    if (!text) return '-';
    return text.length > ${CONFIG.maxLength} ? text.substring(0, ${CONFIG.maxLength}) + '...' : text;
  }
}`
    });
    
    // 方案2：使用 scopedSlots + Tooltip
    fixes.push({
      method: 'tooltip',
      code: `// 在 columns 中：
{
  title: '${getColumnTitle(columnName)}',
  dataIndex: '${columnName}',
  width: ${CONFIG.maxWidth},
  scopedSlots: { customRender: '${columnName}' }
}

// 在 template 中：
<template slot="${columnName}" slot-scope="text, record">
  <a-tooltip :title="record.${columnName}" placement="topLeft">
    <span style="max-width: ${CONFIG.maxWidth - 20}px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block;">
      {{ record.${columnName} }}
    </span>
  </a-tooltip>
</template>`
    });
  });
  
  return fixes;
}

/**
 * 获取列标题（中文）
 */
function getColumnTitle(columnName) {
  const titleMap = {
    companyName: '公司名称',
    corporateName: '企业名称',
    address: '地址',
    registeredAddress: '注册地址',
    actualAddress: '实际地址',
    businessScope: '经营范围',
    remarks: '备注',
    description: '描述'
  };
  return titleMap[columnName] || columnName;
}

/**
 * 生成报告
 */
function generateReport() {
  console.log('\n========================================');
  console.log('表格列宽度超界问题扫描报告');
  console.log('========================================\n');
  
  console.log(`扫描统计：`);
  console.log(`  - 总文件数: ${scanResults.totalFiles}`);
  console.log(`  - 已检查: ${scanResults.checkedFiles}`);
  console.log(`  - 发现问题: ${scanResults.issuesFound.length}`);
  console.log(`  - 涉及文件: ${scanResults.suggestions.length}\n`);
  
  if (scanResults.suggestions.length === 0) {
    console.log('✅ 未发现问题！\n');
    return;
  }
  
  console.log('问题详情：\n');
  
  scanResults.suggestions.forEach((suggestion, index) => {
    console.log(`${index + 1}. ${suggestion.file}`);
    console.log(`   问题数量: ${suggestion.issueCount}`);
    
    suggestion.issues.forEach((issue, i) => {
      console.log(`   ${i + 1}) 第 ${issue.line} 行: ${issue.issue}`);
      console.log(`      建议: ${issue.suggestion}`);
    });
    
    console.log('\n   修复代码示例：');
    console.log('   ----------------------------------------');
    console.log('   方案一（推荐）：使用 ellipsis');
    console.log(suggestion.fixCode[0].code);
    console.log('\n   方案二：使用 Tooltip');
    console.log(suggestion.fixCode[1].code);
    console.log('   ----------------------------------------\n');
  });
  
  // 保存报告到文件
  const reportPath = path.join(__dirname, '..', 'column-overflow-report.txt');
  const reportContent = generateTextReport();
  fs.writeFileSync(reportPath, reportContent, 'utf-8');
  console.log(`\n📄 详细报告已保存到: ${reportPath}\n`);
}

/**
 * 生成文本报告
 */
function generateTextReport() {
  let report = '表格列宽度超界问题扫描报告\n';
  report += '生成时间: ' + new Date().toLocaleString() + '\n';
  report += '========================================\n\n';
  
  report += `扫描统计：\n`;
  report += `  - 总文件数: ${scanResults.totalFiles}\n`;
  report += `  - 已检查: ${scanResults.checkedFiles}\n`;
  report += `  - 发现问题: ${scanResults.issuesFound.length}\n`;
  report += `  - 涉及文件: ${scanResults.suggestions.length}\n\n`;
  
  if (scanResults.suggestions.length === 0) {
    report += '✅ 未发现问题！\n';
    return report;
  }
  
  report += '问题详情：\n\n';
  
  scanResults.suggestions.forEach((suggestion, index) => {
    report += `${index + 1}. ${suggestion.file}\n`;
    report += `   问题数量: ${suggestion.issueCount}\n`;
    
    suggestion.issues.forEach((issue, i) => {
      report += `   ${i + 1}) 第 ${issue.line} 行: ${issue.issue}\n`;
      report += `      建议: ${issue.suggestion}\n`;
    });
    
    report += '\n   修复代码示例：\n';
    report += '   ----------------------------------------\n';
    report += '   方案一（推荐）：使用 ellipsis\n';
    report += suggestion.fixCode[0].code + '\n\n';
    report += '   方案二：使用 Tooltip\n';
    report += suggestion.fixCode[1].code + '\n';
    report += '   ----------------------------------------\n\n';
  });
  
  return report;
}

/**
 * 主函数
 */
function main() {
  console.log('开始扫描...\n');
  
  // 扫描所有配置的目录
  CONFIG.scanDirs.forEach(dir => {
    console.log(`扫描目录: ${dir}`);
    scanDirectory(dir);
  });
  
  // 生成建议
  generateSuggestions();
  
  // 生成报告
  generateReport();
}

// 运行
main();
