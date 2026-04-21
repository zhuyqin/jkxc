# PowerShell脚本：执行角色表结构改造SQL
# 使用方法：.\execute_sql.ps1

$dbHost = "localhost"
$dbPort = "3306"
$dbName = "jeecg-boot"
$dbUser = "root"
$dbPassword = "root"

$sqlFile = Join-Path $PSScriptRoot "alter_role_table.sql"

Write-Host "开始执行数据库迁移脚本..."
Write-Host "数据库: $dbName"
Write-Host "SQL文件: $sqlFile"

# 检查SQL文件是否存在
if (-not (Test-Path $sqlFile)) {
    Write-Host "错误: SQL文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}

# 构建MySQL命令
$mysqlCmd = "mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPassword $dbName"

# 执行SQL文件
try {
    Get-Content $sqlFile | & mysql -h $dbHost -P $dbPort -u $dbUser -p$dbPassword $dbName
    if ($LASTEXITCODE -eq 0) {
        Write-Host "数据库迁移成功完成！" -ForegroundColor Green
    } else {
        Write-Host "数据库迁移失败，退出码: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "执行SQL时发生错误: $_" -ForegroundColor Red
    Write-Host "请手动执行SQL脚本: $sqlFile" -ForegroundColor Yellow
    exit 1
}

