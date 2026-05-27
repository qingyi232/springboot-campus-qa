@echo off
chcp 65001 >nul
echo ========================================
echo   校园智能问答系统 - 停止所有服务
echo ========================================
echo.

echo [警告] 这将关闭以下服务：
echo   - 前端Vue服务（端口8081）
echo   - Rasa Action Server（端口5055）
echo   - Rasa主服务（端口5005）
echo.
echo [提示] 后端SpringBoot服务需要在IDEA中手动停止！
echo.
echo 按任意键继续，或关闭本窗口取消...
pause >nul

echo.
echo [1/3] 正在停止前端服务（端口8081）...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    echo 发现进程ID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if errorlevel 1 (
        echo [失败] 无法停止进程 %%a
    ) else (
        echo [成功] 已停止进程 %%a
    )
)

echo.
echo [2/3] 正在停止Rasa Action Server（端口5055）...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5055 ^| findstr LISTENING') do (
    echo 发现进程ID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if errorlevel 1 (
        echo [失败] 无法停止进程 %%a
    ) else (
        echo [成功] 已停止进程 %%a
    )
)

echo.
echo [3/3] 正在停止Rasa主服务（端口5005）...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5005 ^| findstr LISTENING') do (
    echo 发现进程ID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if errorlevel 1 (
        echo [失败] 无法停止进程 %%a
    ) else (
        echo [成功] 已停止进程 %%a
    )
)

echo.
echo ========================================
echo ✅ 服务停止完成！
echo ========================================
echo.
echo [提示] 如需停止后端SpringBoot服务：
echo   1. 打开IDEA
echo   2. 找到底部的"Run"窗口
echo   3. 点击红色的停止按钮 ⬛
echo.
echo 按任意键关闭本窗口...
pause >nul


