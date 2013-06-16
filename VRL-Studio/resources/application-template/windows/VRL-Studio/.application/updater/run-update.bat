@echo off
set CONF=-updater

REM check number of arguments
set argC=0
for %%x in (%*) do (
  Set /A argC+=1
)

if not argC==10 (
  echo "Wrong number of arguments. (expected 10, got %argC%)"
)

REM set APPDIR="%CD%\.application"
set APPDIR="%CD%" REM .application folder

REM the folder must be set by the calling process,
REM e.g. by calling runtime.exec(this-cmd,appfolders)
cd "%APPDIR%"


set LIBDIR32=%pwd%lib\windows\x86;custom-lib\windows\x86
set LIBDIR64=%pwd%lib\windows\x64;custom-lib\windows\x64

if defined ProgramW6432 (
  @echo detected 64-bit OS
  set LIBDIR=%LIBDIR64%
  set JAVAEXE=jre/x64/bin/java
  set MAXHEAP=128

) else (
  @echo detected 32-bit OS
  set LIBDIR=%LIBDIR32%
  set JAVAEXE=jre/x86/bin/java
  set MAXHEAP=64
)


REM *** BATCH IS SO STUPID ***
REM Argument handling in Batch is totally broken.
REM There is no equivalent to bashs "$@".
REM Don't read this code if you don't have to!

REM Here I got some help:
REM
REM http://munishbansal.wordpress.com/2008/12/21/passing-more-than-9-parameters-to-a-batch-file/
REM http://www.zomeon.com/3416457/spaces-in-batch-script-arguments

setlocal enableextensions

set ARGONE="%~1"
set ARGTWO="%~2"
set ARGTHREE="%~3"
set ARGFOUR="%~4"
set ARGFIVE="%~5"
set ARGSIX="%~6"
set ARGSEVEN="%~7"
set ARGEIGHT="%~8"
set ARGNINE="%~9"

REM *** THIS IS SO STUPID ***
REM This SHIFT is needed to get argument 10
REM as %10 is interpreted as %1. After the
REM 9 SHIFT calls argument 1 contains argument 10

SHIFT
SHIFT
SHIFT
SHIFT
SHIFT
SHIFT
SHIFT
SHIFT
SHIFT

set ARGTEN="%~1"

REM echo "%~1"
REM echo "%~2"
REM echo "%~3"
REM echo "%~4"
REM echo "%~5"
REM echo "%~6"
REM echo "%~7"
REM echo "%~8"
REM echo "%~9"
REM echo "%ARGTEN%"

REM pause

REM optimized for jre 7 (19.04.2012)
start /min /realtime %JAVAEXE% -Xms64m -Xmx%MAXHEAP%m -XX:MaxPermSize=256m -splash:resources\studio-resources\splashscreen-update.png -Djava.library.path="%LIBDIR%" -jar VRL-Studio.jar %CONF% %ARGONE% %ARGTWO% %ARGTHREE% %ARGFOUR% %ARGFIVE% %ARGSIX% %ARGSEVEN% %ARGEIGHT% %ARGNINE% %ARGTEN%

exit
