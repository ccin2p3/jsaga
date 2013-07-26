@echo off
setlocal enabledelayedexpansion

REM # set arguments
set ARGS=
set SEP=
:beginArgs
set CURRENT=%~1
if "%CURRENT%_"=="_" goto endArgs
set ARGS=%ARGS%%SEP%%1
set OPTION=%CURRENT%
if "%OPTION:~0,2%"=="-D" (set SEP==) else (set SEP= )
shift
goto beginArgs
:endArgs

REM # set JSAGA_HOME
if "%JSAGA_HOME%_"=="_" (
  if exist "$INSTALL_PATH" (
    set JSAGA_HOME=$INSTALL_PATH
  ) else (
    set JSAGA_HOME=.
  )
)

REM # set system properties
REM set PROPERTIES=
set PROPERTIES=%PROPERTIES% -DJSAGA_HOME="%JSAGA_HOME%"
rem set PROPERTIES=%PROPERTIES% -Ddebug

REM # set classpath
set CLASSPATH=.
set CLASSPATH=!CLASSPATH!;"%JSAGA_HOME%\lib\*"
set CLASSPATH=!CLASSPATH!;"%JSAGA_HOME%\lib-adaptors\*"
if "${class.name}"=="junit.textui.TestRunner" (
  set CLASSPATH=!CLASSPATH!;"%JSAGA_HOME%\lib-test\*"
)

REM # set java
if "%JAVA_HOME%_"=="_" (
  set JAVA=java
) else (
  set JAVA="%JAVA_HOME%\examples\java"
)

REM # run command
set CMD=%JAVA% %PROPERTIES% -cp "%CLASSPATH%" ${class.name} %ARGS%
if "%DEBUG%_"=="_" (
  %CMD%
) else (
  echo %CMD%
  %CMD%
)
