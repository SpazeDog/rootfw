RootFW
======

An Android Root Shell Framework

RootFW is a tool that helps Android Applications act as root. The only way for an application to perform tasks as root, is by executing shell commands as Android has no native way of doing this. However, due to different types of shell support on different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method tries to support as many different environments as possible by implementing different approaches for each environment. This makes the work of app developers a lot easier.

Usage
-----

The main RootFW does not contain much. It's job is only to handle the shell (Connect, disconnect etc), everything else is done by extender classes. Even communicating with the shell requires using an extender. However, each extender is accessed via the main RootFW instance. 

Here is a small example of how to connect and execute a command in the shell.

```java
RootFW root = new RootFW();

if ( root.connect() ) {
    ShellResult result = root.shell().run("df /dev/block/mmcblk0p1");

    if ( result.wasSuccessfull() ) {
        String line = result.getLine();
    }

    root.disconnect();
}
```

You can also add multiple commands at once. The output from each command will be merged together in the result. 

```java
RootFW root = new RootFW();

if ( root.connect() ) {
    ShellResult result = root.shell().addCommands("command1", "command2").run();

    if ( result.wasSuccessfull() ) {
        ...
    }

    root.disconnect();
}
```

One of the biggest issues with Android, is the fact that you never know what is available to you. Some devices only have toolbox, these even exists with different supports, others have different version of busybox and some commands are available as single binaries. RootFW has an easy way of adding support for different enviroments.

```java
RootFW root = new RootFW();

if ( root.connect() ) {
    ShellResult result = root.shell().buildCommands("%binary df -h /dev/block/mmcblk0p1").run();

    if ( result.wasSuccessfull() ) {
        ...
    }

    root.disconnect();
}
```

By using `buildCommands()` instead of `addCommands()`, you will automatically add support for more enviroments. In this case the `buildCommands()` will create 3 command attempts, `busybox df -h /dev/block/mmcblk0p1`, `toolbox df -h /dev/block/mmcblk0p1` and plain `df -h /dev/block/mmcblk0p1`. Each attempt is executed until one is successfull. Also, by parsing an array instead of a string, you can add even more attempts.

```java
ShellResult result = root.shell().buildCommands( new String[]{"%binary df -h /dev/block/mmcblk0p1", "%binary df /dev/block/mmcblk0p1"} );
```

This will produce the fallowing attempts, `busybox df -h /dev/block/mmcblk0p1`, `toolbox df -h /dev/block/mmcblk0p1`, `df -h /dev/block/mmcblk0p1`, `busybox df /dev/block/mmcblk0p1`, `toolbox df /dev/block/mmcblk0p1` and `df /dev/block/mmcblk0p1`. So if the device does not have any support for the `-h` argument, it will try without. Again, all 6 attempts are executed until one is successfull. 

The `buildCommands()` uses the available binaries in RootFW.Config.BINARIES to create the attempts. You can add other binaries, like a full path to a custom busybox version.

You can also create your own attempts using `addCommands()`. This is also done by parsing an array. 

```java
RootFW root = new RootFW();

if ( root.connect() ) {
    ShellResult result = root.shell().addCommands( new String[]{"cp /path/to/file /path/to/destination", "cat /path/to/file > /path/to/destination/file"} ).run();

    if ( result.wasSuccessfull() ) {
        ...
    }

    root.disconnect();
}
```

Now if the device does not support the `cp` command, RootFW will instead go to the second attempt and use `cat` instead. 

RootFW also makes it possible to share the same connection across multiple classes and methods, without always knowing when and where it might be used. This is done by the `InstanceExtender` class which allows locks to be added to the connection to ensure that it is not disconnected.

```java
RootFW root = RootFW.rootInstance().lock().get();

if ( root.isConnected() ) {
    ...

    RootFW.rootInstance().unlock().disconnect();
}
```

The `rootInstance()` will return a shared instance of a root shell which can be used everywhere. The `lock()` method is used to add a lock to the connection, proventing the connection to close even though `disconnect()` might be called in another method which got called within this one. Other classes/methods can also add a lock. Each call to `lock()` adds a new lock, and the connection can only be closed once all of the locks has been removed. 

License
------

Copyright (c) 2013 Daniel Bergløv

RootFW is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RootFW is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with RootFW. If not, see <http://www.gnu.org/licenses/>
