#!/bin/bash

######### EDIT IF NECESSARY #########

VRL_VERSION=0.4.2
PROPERTY_SUFFIX=default


CONF="-enable3d yes -resolution 1024x768 -defaultProject default.vrlp -property-folder-suffix $PROPERTY_SUFFIX -plugin-checksum-test no"


######### DO NOT EDIT #########

#OSX_CONF="-Xdock:icon=../vrl-icon-osx.icns -Djava.library.path=custom-lib/osx -Xbootclasspath/p:lib/java3d/j3dcore.jar:lib/java3d/j3dutils.jar:lib/osx/jogl.jar:lib/java3d/vecmath.jar:lib/osx/gluegen-rt.jar -Dsun.boot.library.path=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Libraries:lib/osx -Dapple.laf.useScreenMenuBar=true"

#OSX_CONF="-Xdock:icon=../vrl-icon-osx.icns -Dapple.laf.useScreenMenuBar=true -Djava.ext.dirs=lib/ext -cp lib/java3d/jogl-all.jar:lib/java3d/gluegen.jar:lib/java3d/gluegen-rt.jar:lib/java3d/j3dcore.jar:lib/java3d/j3dutils.jar:lib/java3d/vecmath.jar:lib/osx/gluegen-rt-natives-macosx-universal.jar:lib/osx/jogl-all-natives-macosx-universal.jar"

OSX_CONF="-Xdock:name=VRL-Studio -Xdock:icon=../vrl-icon-osx.icns -Dapple.laf.useScreenMenuBar=true  -Djava.ext.dirs=."

APPDIR="$(dirname "$0")/../Resources/.application"
cd "$APPDIR"
APPDIR="$(pwd)"

if [[ "$(uname)" == "Darwin" ]]
then
	echo ">> detected os x: $(pwd)"
	# drag&drop to the dock icon
	export CFProcessPath="$0"
fi

# ugly hack to enable vtk on osx
export DYLD_LIBRARY_PATH="$HOME/.vrl/$VRL_VERSION/$PROPERTY_SUFFIX/plugins/VRL-VTK/natives/osx/:$DYLD_LIBRARY_PATH"

# IF APPLICATION STUB SHALL BE USED
#../../MacOS/JavaApplicationStub "$@"&
#exit 0


# IF BASH SCRIPT SHALL BE USED
LIBDIR32="lib/linux/x86:custom-lib/linux/x86"
LIBDIR64="lib/linux/x64:custom-lib/linux/x64"
LIBDIROSX=""


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

$JAVAEXE -Xms64m -Xmx4096m -splash:resources/studio-resources/splashscreen.png $OSX_CONF -jar VRL-Studio.jar $CONF "-file" "$@"

#PID=$!
#wait $PID
