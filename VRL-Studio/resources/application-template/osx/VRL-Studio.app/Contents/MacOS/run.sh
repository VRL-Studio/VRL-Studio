#!/bin/bash

######### EDIT IF NECESSARY #########

VRL_VERSION=0.4.4
PROPERTY_SUFFIX=default


CONF="-enable3d yes -resolution 1024x768 -defaultProject default.vrlp -property-folder-suffix $PROPERTY_SUFFIX -plugin-checksum-test no"


######### DO NOT EDIT #########

OS=$(uname -a)

APPDIR="$(dirname "$0")/../Resources/.application"
cd "$APPDIR"
APPDIR="$(pwd)"

if [[ $OS == *Darwin* ]]
then
	echo ">> detected Mac OS X:"
	# drag&drop to the dock icon
	if [[ "$1" == "-no-app" ]]
	then
		echo " --> disabled d&d and dock events"
	else
		export CFProcessPath="$0"
	fi

	# find JAVA_HOME on OS X
	JAVA_HOME=$(/usr/libexec/java_home)
fi

# ugly hack to enable vtk on osx
export DYLD_LIBRARY_PATH="$HOME/.vrl/$VRL_VERSION/$PROPERTY_SUFFIX/plugins/VRL-VTK/natives/osx/:$DYLD_LIBRARY_PATH"

# IF BASH SCRIPT SHALL BE USED
LIBDIR32="lib/linux/x86:custom-lib/linux/x86"
LIBDIR64="lib/linux/x64:custom-lib/linux/x64"
LIBDIROSX=""

if [[ $OS == *Darwin* ]]
then
	echo ">> detected x86 (64 bit) os"
	LIBDIR="$LIBDIR64:$LIBDIROSX"
	JAVAEXE="jre/x64/bin/java"
	JRE_EXT_DIR="jre/x64/lib/ext"
  if [ -e $JAVAEXE ]
  then
    chmod +x $JAVAEXE
  fi  
  if [ -e $JAVAEXE ]
  then
    chmod +x "jre/x64/lib/jspawnhelper"	
  fi  
elif [[ $OS == *x86_64* ]]
then
  echo ">> detected x86 (64 bit) os"
  LIBDIR="$LIBDIR64:$LIBDIROSX"
  JAVAEXE="jre/x64/bin/java"
  JRE_EXT_DIR="jre/x64/lib/ext"
  if [ -e $JAVAEXE ]
  then
    chmod +x $JAVAEXE
  fi   
elif [[ $OS == *86* ]]
then
  echo ">> detected x86 (32 bit) os"
  LIBDIR="$LIBDIR32:$LIBDIROSX"
  JAVAEXE="jre/x86/bin/java"
  JRE_EXT_DIR="jre/x86/lib/ext"
  if [ -e $JAVAEXE ]
  then
    chmod +x $JAVAEXE
  fi    
else
  echo ">> unsupported architecture!"
  echo " --> executing installed java version"
  JAVAEXE="java"
  JRE_EXT_DIR="$JAVA_HOME/jre/lib/ext"
fi

if [ ! -e $JAVAEXE ]
then
  echo ">> integrated jre not found!"
  echo " --> executing installed java version"
  JAVAEXE="java"
  JRE_EXT_DIR="$JAVA_HOME/jre/lib/ext"
fi

echo ">> specified parameters: $@"
echo ">> ext-dir: $JRE_EXT_DIR"
echo ">> using java executable from: $JAVAEXE"

## pre JDK 11
#OSX_CONF="-Xdock:name=VRL-Studio -Xdock:icon=../vrl-icon-osx.icns -Dapple.laf.useScreenMenuBar=true -Djava.ext.dirs=$JRE_EXT_DIR"
#$JAVAEXE -Xms64m -Xmx4096m -Xss16m -XX:+UseConcMarkSweepGC -splash:resources/studio-resources/splashscreen.png $OSX_CONF -jar VRL-Studio.jar $CONF "-file" "$@"

## >= JDK 11
OSX_CONF="-Xdock:name=VRL-Studio -Xdock:icon=../vrl-icon-osx.icns -Dapple.laf.useScreenMenuBar=true"
$JAVAEXE -Xms64m -Xmx4096m -Xss16m -splash:resources/studio-resources/splashscreen.png $OSX_CONF -jar VRL-Studio.jar $CONF "-file" "$@"

#PID=$!
#wait $PID
