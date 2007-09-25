@echo off
setlocal enabledelayedexpansion

REM # set arguments
set ARGS=
set SEP= 
:beginArgs
if "%1_"=="_" goto endArgs
set ARGS=%ARGS%%SEP%%1
set OPTION=%1
if "%OPTION:~0,2%"=="-D" (set SEP==) else (set SEP= )
shift
goto beginArgs
:endArgs

REM # set JSAGA_HOME
if "%JSAGA_HOME%_"=="_" (
  set JSAGA_HOME=.
)

REM # set system properties
set PROPERTIES=
set PROPERTIES=%PROPERTIES% -DJSAGA_HOME=%JSAGA_HOME%
set PROPERTIES=%PROPERTIES% -Dinteractive
rem set PROPERTIES=%PROPERTIES% -Ddebug
rem set PROPERTIES=%PROPERTIES% -Dconfig=%JSAGA_HOME%\etc\example\jsaga-config.xml

REM # set classpath
set CLASSPATH=.
for %%i in (%JSAGA_HOME%\lib\*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%i
)
for %%i in (%JSAGA_HOME%\lib-adaptors\*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%i
)

REM # set java
if "%JAVA_HOME%_"=="_" (
  set JAVA=java
) else (
  set JAVA=%JAVA_HOME%\bin\java
)

REM # run command
set CMD="%JAVA%" %PROPERTIES% -cp "%CLASSPATH%" ${class.name} %ARGS%
if "%DEBUG%_"=="_" (
  %CMD%
) else (
  echo %CMD%
  %CMD%
)
