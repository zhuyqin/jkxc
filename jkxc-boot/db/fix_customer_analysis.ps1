# ============================================
# 修复客户画像和价值分析超时问题
# 功能：自动执行SQL脚本创建数据库索引
# 创建时间：2026-02-03
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复客户画像和价值分析超时问题" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 数据库配置
$DB_HOST = "101.35.140.125"
$DB_PORT = "3306"
$DB_NAME = "jkxc-cg"
$DB_USER = "jkxc-cg"
$DB_PASS = "jkxc-cg"

# SQL文件路径
$SQL_FILE = "fix_customer_analysis_timeout.sql"

Write-Host "数据库信息：" -ForegroundColor Yellow
Write-Host "  主机: $DB_HOST" -ForegroundColor White
Write-Host "  端口: $DB_PORT" -ForegroundColor White
Write-Host "  数据库: $DB_NAME" -ForegroundColor White
Write-Host "  用户: $DB_USER" -ForegroundColor White
Write-Host ""

# 检查mysql命令是否可用
$mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlCmd) {
    Write-Host "错误: 未找到mysql命令，请确保MySQL客户端已安装并添加到PATH环境变量" -ForegroundColor Red
    Write-Host ""
    Write-Host "解决方案：" -ForegroundColor Yellow
    Write-Host "1. 下载并安装MySQL客户端" -ForegroundColor White
    Write-Host "2. 或者手动执行SQL文件：" -ForegroundColor White
    Write-Host "   mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < $SQL_FILE" -ForegroundColor Cyan
    Write-Host ""
    Read-Host "按任意键退出"
    exit 1
}

Write-Host "正在执行SQL脚本..." -ForegroundColor Yellow
Write-Host ""

# 执行SQL脚本
$mysqlArgs = @(
    "-h", $DB_HOST,
    "-P", $DB_PORT,
    "-u", $DB_USER,
    "-p$DB_PASS",
    $DB_NAME
)

try {
    Get-Content $SQL_FILE | & mysql $mysqlArgs
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✓ 索引创建成功！" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "后续步骤：" -ForegroundColor Yellow
        Write-Host "1. 重启后端服务以应用配置更改" -ForegroundColor White
        Write-Host "2. 清空浏览器缓存" -ForegroundColor White
        Write-Host "3. 重新访问客户画像分析和客户价值分析页面" -ForegroundColor White
        Write-Host ""
        Write-Host "预期效果：" -ForegroundColor Yellow
        Write-Host "- 页面加载时间从30秒以上降低到3秒以内" -ForegroundColor White
        Write-Host "- 不会再出现超时错误" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "执行失败，请检查错误信息" -ForegroundColor Red
    }
} catch {
    Write-Host ""
    Write-Host "执行出错: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "请手动执行以下命令：" -ForegroundColor Yellow
    Write-Host "mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < $SQL_FILE" -ForegroundColor Cyan
}

Write-Host ""
Read-Host "按任意键退出"
