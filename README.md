RootFW
======

An Android Root Shell Framework

RootFW is a tool that helps Android Applications act as root. The only way for an application to perform tasks as root, is by executing shell commands as Android has no native way of doing this. However, due to different types of shell support on different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method tries to support as many different environments as possible by implementing different approaches for each environment. This makes the work of app developers a lot easier.


**Checkout the [Documentation Page](docs/dokka/index.md) or the [Wiki Page](docs/wiki/index.md) for further info**

### Include Library
-----------

**Maven**

Reflect Tools is available in Maven respository at [Bintray](https://bintray.com/dk-zero-cool/maven/rootfw/view) and can be accessed via jCenter.

```
dependencies {
    compile 'com.spazedog.lib:rootfw:$version'
}
```

**Android Studio**

First download the [rootfw-release.aar](https://github.com/SpazeDog/rootfw/raw/5.x/releases/rootfw-release.aar) file.

Place the file in something like the `libs` folder in your module.

Open your `build.gradle` file for your module _(not the main project version)_.

```
dependencies {
    compile(name:'rootfw-release', ext:'aar')
}

repositories {
    flatDir {
        dirs 'libs'
    }
}
```

