#!/bin/bash

######### EDIT IF NECESSARY #########

VRL_VERSION=0.4.2
PROPERTY_SUFFIX=numerics-studio


CONF="-enable3d yes -resolution 1024x768 -defaultProject default.vrlp -property-folder-suffix $PROPERTY_SUFFIX -plugin-checksum-test no"


######### DO NOT EDIT #########

OSX_CONF="-Xdock:icon=../vrl-icon-osx.icns -Djava.library.path=custom-lib/osx -Xbootclasspath/p:lib/java3d/j3dcore.jar:lib/java3d/j3dutils.jar:lib/osx/jogl.jar:lib/java3d/vecmath.jar:lib/osx/gluegen-rt.jar -Dsun.boot.library.path=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Libraries:lib/osx -Dapple.laf.useScreenMenuBar=true"



APPDIR="$(dirname "$0")/../Resources/.application"
cd "$APPDIR"
APPDIR="$(pwd)"

# ugly hack to enable vtk on osx
export DYLD_LIBRARY_PATH="$HOME/.vrl/$VRL_VERSION/$PROPERTY_SUFFIX/plugins/VRL-VTK/natives/osx/:$DYLD_LIBRARY_PATH"

# IF APPLICATION STUB SHALL BE USED
../../MacOS/JavaApplicationStub "$@"&
exit 0


# IF BASH SCRIPT SHALL BE USED
LIBDIR32="lib/linux/x86:custom-lib/linux/x86"
LIBDIR64="lib/linux/x64:custom-lib/linux/x64"
LIBDIROSX="lib/osx:custom-lib/osx"


if [[ $OS == *x86_64* ]]
then
  echo ">> detected x86 (64 bit) os"
  LIBDIR="$LIBDIR64:$LIBDIROSX"
  JAVAEXE="jre/x64/bin/java"
elif [[ $OS == *86* ]]
then
  echo ">> detected x86 (32 bit) os"
  LIBDIR="$LIBDIR32:$LIBDIROSX"
  JAVAEXE="jre/x86/bin/java"
else
  echo ">> unsupported architecture!"
  echo " --> executing installed java version"
  JAVAEXE="java"
fi

if [ ! -e $JAVAEXE ]
then
  echo ">> integrated jre not found!"
  echo " --> executing installed java version"
  JAVAEXE="java"
fi

echo "PARAMS: $@"

$JAVAEXE -Xms64m -Xmx1024m -XX:MaxPermSize=256m -splash:resources/studio-resources/splashscreen.png $OSX_CONF -jar VRL-Studio.jar $CONF "-file" "$@"

#PID=$!
#wait $PID
