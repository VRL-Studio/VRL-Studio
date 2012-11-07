VRL-Studio
==========

Webpage: http://vrl-studio.mihosoft.eu

Innovative IDE based on VRL (Visual Reflection Library)

VRL-Studio is an innovative Integrated Development Environment (IDE) based on the Java Platform that combines both visual and text-based programming. In contrast to many other Development Environments VRL-Studio Projects are fully functional programs that are developed at run-time. This makes it an ideal environment for rapid prototyping, teaching and experimentation.

# Dependencies

- Java >= 1.6 
- NetBeans >= 7.01
- [VRL](https://github.com/miho/VRL)

# How To Build

- Open the *VRL* project with NetBeans and build it
- Open the *VRL-Studio* project with NetBeans and build it

# How To Run

You can either run VRL-Studio from NetBeans (see 1.) or run the application bundle manually (see 2.)

1. You can run VRL-Studio from NetBeans just like any other Java application project
2. After building, the `VRL-Studio/dist-final/` folder contains application bundles for Linux, Mac and Windows.

<br><hr></hr><br>

# How To Deploy

VRL-Studio bundles are created automatically when building it. The `build.properties` file contains additional configuration
options.

### Bundled JRE

The following options can be enabled to create bundles that contain a full JRE installation (supported on Linux and Windows):

    jre.location.linux.x86=/path/to/linux/jre/x86/
    jre.location.linux.x64=/path/to/jre/x64/
    jre.location.windows.x86=/path/to/jre/x86/
    jre.location.windows.x64=/path/to/jre/x64/

### VRL-Studio Help

The official VRL-Studio bundles conain offline help pages. To include the help in a bundle, use the following option:

    help.location=/path/to/help/

### Splashscreen

A custom splashscreen can be defined via:

    splashscreen.location=/path/to/splashscreen.png

> **NOTE** Please read the [LICENSE](https://github.com/miho/VRL-Studio/blob/master/VRL-Studio/LICENSE) file, especially the **Attribution Requirements**

### Zip Bundles

To automatically create zip bundles use the following option (only supported on Unix):

    zip-bundles.enabled=true