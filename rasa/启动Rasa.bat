@echo off
chcp 65001
echo ========================================
echo    启动 Rasa 智能问答系统
echo ========================================
echo.
echo [1/2] 正在启动 Rasa Action Server...
start "Rasa Action Server" cmd /k "cd /d %~dp0 && py -3.10 -m rasa run actions"
timeout /t 5 /nobreak >nul

echo [2/2] 正在启动 Rasa 主服务...
start "Rasa Main Server" cmd /k "cd /d %~dp0 && py -3.10 -m rasa run --enable-api --cors * --debug"

echo.
echo ✅ Rasa 服务正在启动...
echo.
echo 📌 Action Server: http://localhost:5055
echo 📌 Rasa 主服务:   http://localhost:5005
echo.
echo ⏳ 请等待 10-15 秒，让服务完全启动
echo 💡 如遇错误，请查看弹出的命令行窗口
echo.
pause

