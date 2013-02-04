@echo off
set CONF=-updater

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

REM version of 2011
REM start /min /realtime %JAVAEXE% -Xms64m -Xmx512m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=256m -splash:resources\studio-resources\splashscreen.png -Djava.library.path="%LIBDIR%" -jar VRL-Studio.jar %CONF%

REM optimized for jre 7 (19.04.2012)
start /min /realtime %JAVAEXE% -Xms64m -Xmx%MAXHEAP%m -XX:MaxPermSize=256m -splash:resources\studio-resources\splashscreen-update.png -Djava.library.path="%LIBDIR%" -jar VRL-Studio.jar %CONF% "%*"

exit
