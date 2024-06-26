@echo off
set CONF=-enable3d no -resolution 1024x768 -defaultProject default.vrlp -property-folder-suffix default -plugin-checksum-test no

set APPDIR="%CD%\.application"

cd "%APPDIR%"


set LIBDIR32=%pwd%lib\windows\x86;custom-lib\windows\x86
set LIBDIR64=%pwd%lib\windows\x64;custom-lib\windows\x64

if defined ProgramW6432 (
  @echo detected 64-bit OS
  set LIBDIR=%LIBDIR64%
  set JAVAEXE=jre\x64\bin\java
  set MAXHEAP=1024

) else (
  @echo detected 32-bit OS
  set LIBDIR=%LIBDIR32%
  set JAVAEXE=jre\x86\bin\java
  set MAXHEAP=512
)


REM version of 2011
REM start /realtime %JAVAEXE% -Xms64m -Xmx512m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=256m -splash:resources\studio-resources\splashscreen.png -Djava.library.path="%LIBDIR%" -jar VRL-Studio.jar %CONF%


REM version of 2012
REM start /realtime %JAVAEXE% -Xms64m -Xmx%MAXHEAP%m -XX:MaxPermSize=256m -splash:resources\studio-resources\splashscreen.png -Djava.library.path="%LIBDIR%" -jar VRL-Studio.jar %CONF% %*


REM optimized for jre 21 (26.06.2024)
REM start /min /realtime %JAVAEXE% -Xms128m -Xmx%MAXHEAP%m -Xss16m -splash:resources\studio-resources\splashscreen.png -Djava.library.path="%LIBDIR%" -jar VRL-Studio.jar %CONF% %*
start /min /realtime %JAVAEXE% -Xms128m -Xmx%MAXHEAP%m -Xss16m -splash:resources\studio-resources\splashscreen.png -Djava.library.path="%LIBDIR%" --add-modules javafx.controls,javafx.fxml,javafx.graphics --module-path "%LIBDIR%" --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.jar=ALL-UNNAMED -jar VRL-Studio.jar %CONF% %*

exit