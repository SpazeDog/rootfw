RootFW 4
========

An Android Root Shell Framework

RootFW is a tool that helps Android Applications act as root. The only way for an application to perform tasks as root, is by executing shell commands as Android has no native way of doing this. However, due to different types of shell support on different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method tries to support as many different environments as possible by implementing different approaches for each environment. This makes the work of app developers a lot easier.

**Checkout the [Wiki page](https://github.com/SpazeDog/rootfw/wiki) for further info**

### Include Library
-----------

**Maven**

Reflect Tools is available in Maven respository at [Bintray](https://bintray.com/dk-zero-cool/maven/rootfw_gen4/view) and can be accessed via jCenter. 

```
dependencies {
    compile 'com.spazedog.lib:rootfw_gen4'
}
```

**Android Studio**

First download the [rootfw-release.aar](https://github.com/SpazeDog/rootfw/raw/4_gen/projects/rootfw-release.aar) file. 

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

**Eclipse/ADT**

Download the source and import it into eclipse. Then simply include the new library project to your main project.
