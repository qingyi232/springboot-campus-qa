@echo off
chcp 65001
echo ========================================
echo    Rasa模型重新训练脚本
echo ========================================
echo.

REM 获取当前脚本所在目录
cd /d %~dp0

echo [提示] 当前目录: %cd%
echo.
echo [提示] 即将重新训练Rasa模型...
echo [提示] 这个过程大约需要5-10分钟，请耐心等待
echo.
echo 按任意键开始训练，或关闭窗口取消...
pause >nul

echo.
echo ========================================
echo 开始训练Rasa模型...
echo ========================================
echo.

REM 训练模型
py -3.10 -m rasa train

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 模型训练成功！
    echo ========================================
    echo.
    echo 📌 新模型已生成在 models/ 目录
    echo 📌 请重启Rasa服务以使用新模型
    echo.
    echo 💡 重启方法：
    echo    1. 关闭所有Rasa相关窗口
    echo    2. 双击运行 "启动Rasa.bat"
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ 模型训练失败！
    echo ========================================
    echo.
    echo 💡 常见问题：
    echo    1. 检查训练数据格式是否正确
    echo    2. 确认Python和Rasa已正确安装
    echo    3. 查看上方红色错误信息
    echo.
)

echo 按任意键关闭本窗口...
pause >nul


