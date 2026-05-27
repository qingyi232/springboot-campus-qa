@echo off
chcp 65001 >nul
echo ========================================
echo   校园智能问答系统 - 一键启动脚本
echo ========================================
echo.

REM 获取当前脚本所在目录
set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

echo [提示] 当前目录: %SCRIPT_DIR%
echo.
echo [提示] 启动前请确保：
echo   1. MySQL数据库服务已启动
echo   2. 已在IDEA中手动启动后端（SpringBoot）
echo   3. 已完成数据库初始化（init.sql）
echo.
echo 按任意键开始启动前端和Rasa服务...
pause >nul

echo.
echo ========================================
echo [1/2] 正在启动前端服务...
echo ========================================
echo.
echo [提示] 将打开一个新窗口运行前端服务
echo [提示] 请不要关闭该窗口！
echo.

REM 启动前端
start "前端服务 - Vue" cmd /k "cd /d %SCRIPT_DIR%\frontend && npm run serve"

echo [提示] 前端服务正在启动，请等待5秒...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo [2/2] 正在启动Rasa AI服务...
echo ========================================
echo.
echo [提示] 将打开两个新窗口运行Rasa服务
echo [提示] 请不要关闭这些窗口！
echo.

REM 启动Rasa Action Server
start "Rasa Action Server" cmd /k "cd /d %SCRIPT_DIR%\rasa && py -3.10 -m rasa run actions"

echo [提示] Rasa Action Server正在启动，请等待3秒...
timeout /t 3 /nobreak >nul

REM 启动Rasa主服务
start "Rasa主服务" cmd /k "cd /d %SCRIPT_DIR%\rasa && py -3.10 -m rasa run --enable-api --cors *"

echo.
echo ========================================
echo ✅ 系统启动完成！
echo ========================================
echo.
echo 📌 服务地址：
echo   - 前端：http://localhost:8081
echo   - 后端：http://localhost:8080
echo   - Rasa Action Server：http://localhost:5055
echo   - Rasa主服务：http://localhost:5005
echo.
echo 📌 默认账号：
echo   【学生】student01 / admin123
echo   【管理员】admin / admin123
echo.
echo 💡 提示：
echo   1. 请等待10-15秒，让所有服务完全启动
echo   2. 如果前端启动失败，请先进入frontend目录运行 npm install
echo   3. 如果Rasa启动失败，请先进入rasa目录运行 py -3.10 -m rasa train
echo   4. 不要关闭弹出的三个黑色窗口！
echo.
echo 按任意键关闭本窗口...
pause >nul


