RootFW
======

An Android Root Shell Framework

RootFW is a tool that helps Android Applications act as root. The only way for an application to perform tasks as root, is by executing shell commands as Android has no native way of doing this. However, due to different types of shell support on different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method tries to support as many different environments as possible by implementing different approaches for each environment. This makes the work of app developers a lot easier.

Table of content
----------------

* [Usage](#usage)
* Extender Information
    * [ProcessExtender](#processextender)
    * [MemoryExtender](#memoryextender)
    * [PropertyExtender](#propertyextender)
    * [FilesystemExtender](#filesystemextender)
    * [FileExtender](#fileextender)
    * [BinaryExtender](#binaryextender)

Usage
-----

The main RootFW does not contain much. It's job is only to handle the shell (Connect, disconnect etc), everything else is done by extender classes. Even communicating with the shell requires using an extender. However, each extender is accessed via the main RootFW instance. 

Here is a small example of how to connect and execute a command in the shell.

```java
RootFW root = new RootFW();

if ( root.connect() ) {
    ShellResult result = root.shell().run("df /dev/block/mmcblk0p1");

    if ( result.wasSuccessful() ) {
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

    if ( result.wasSuccessful() ) {
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

    if ( result.wasSuccessful() ) {
        ...
    }

    root.disconnect();
}
```

By using `buildCommands()` instead of `addCommands()`, you will automatically add support for more enviroments. In this case the `buildCommands()` will create 3 command attempts, `busybox df -h /dev/block/mmcblk0p1`, `toolbox df -h /dev/block/mmcblk0p1` and plain `df -h /dev/block/mmcblk0p1`. Each attempt is executed until one is successful.

You can also build more than one stack of attempts per command by using `buildAttempts()`.
```java
ShellResult result = root.shell().buildAttempts("%binary df -h /dev/block/mmcblk0p1", "%binary df /dev/block/mmcblk0p1");
```

This will produce the fallowing attempts, `busybox df -h /dev/block/mmcblk0p1`, `toolbox df -h /dev/block/mmcblk0p1`, `df -h /dev/block/mmcblk0p1`, `busybox df /dev/block/mmcblk0p1`, `toolbox df /dev/block/mmcblk0p1` and `df /dev/block/mmcblk0p1`. So if the device does not have any support for the `-h` argument, it will try without. Again, all 6 attempts are executed until one is successful. 

The `buildCommands()` and `buildAttempts()` uses the available binaries in RootFW.Config.BINARIES to create the attempts. You can add other binaries, like a full path to a custom busybox version.

You can also create your own attempts using `addAttempts()`.

```java
RootFW root = new RootFW();

if ( root.connect() ) {
    ShellResult result = root.shell().addAttempts("cp /path/to/file /path/to/destination", "cat /path/to/file > /path/to/destination/file").run();

    if ( result.wasSuccessful() ) {
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

The `rootInstance()` will return a shared instance of a root shell which can be used everywhere. The `lock()` method is used to add a lock to the connection, preventing the connection to close even though `disconnect()` might be called in another method which got called within this one. Other classes/methods can also add a lock. Each call to `lock()` adds a new lock, and the connection can only be closed once all of the locks has been removed. 

If you have a large application, different methods in different classes might be called randomly across the application depending on different circumstances. The shared connection is a perfect tool to use if you want to avoid a lot of connects/disconnects during certain executable threads, however you might need a way to interact whenever the connection state of the shared connection changes. 

The `InstanceExtender` allows you to add one or more callbacks to the shared root and user connections. These callbacks are stored in the `InstanceCallback` interface and allows for 3 methods which is called when the connection is established, disconnected or failed. This way you can add one global handler for these circumstances instead of adding checks to each method and class which might need it. 

```java
Instance instance = RootFW.rootInstance();

instance.addCallback(new InstanceCallback() {

    /* This interface is actually an abstract class. 
     * So you only have to add the methods which you need.
     */

    @Override
    public void onConnect(RootFW instance) {
        ...
    }

    @Override
    public void onDisconnect(RootFW instance) {
        ...
    }

    @Override
    public void onFailed(RootFW instance) {
        ...
    }

});

/* When calling get() to retrieve the shared root connection,
 * it will automatically connect if not already connected. 
 * If you, our InstanceCallback.onConnect() will be called. 
 * If the connection failed to establish, InstanceCallback.onFailed() is called.
 */
RootFW root = instance.lock().get();

if ( root.isConnected() ) {
    ...

    /* If no other method has added a lock to this connection, 
     * then the connection will be disconnected and our InstanceCallback.onDisconnect() is called.
     */
    instance.unlock().disconnect();
}
```

ProcessExtender
---------------

This class contains the fallowing extenders

* ProcessExtender.Processes
    * Accessed via `RootFW.processes()`
    * This is a global process extender which can be used to for and example list all current processes with information like `pid`, `name` and `path (if available)`
* ProcessExtender.Process
    * Accessed via `RootFW.processes(String process)` or `RootFW.processes(Integer pid)`
    * This is a per process extender which can be used to get information on a specific process or pid and kill a process
* ProcessExtender.Power
    * Accessed via `RootFW.power()`
    * This provides you with tools to reboot a device, shut it down, reboot into recovery or do a soft reboot

```java
RootFW root = new RootFW();

if (root.connect()) {
    if ( root.process("myprocess").getPid() == 0 ) {
        root.power().reboot();

    } else {
        ...
    }

    root.disconnect();
}
```

MemoryExtender
--------------

This class contains the fallowing extenders

* MemoryExtender.Memory
    * Accessed via `RootFW.memory()`
    * This is a global memory extender which can be used to get basic memory information
* MemoryExtender.Device
    * Accessed via `RootFW.memory(String device)`
    * This is a per device extender which can be used to get memory information about a specific device, for and example SWAP information if the device is a SWAP device

```java
RootFW root = new RootFW();

if (root.connect()) {
    SwapStat stat = root.memory("/dev/block/zram0").statSwap();

    if (stat != null) {
        Long usage = stat.usage();
        Long size = stat.size();
        Long remaning = size - usage;

        ...
    }

    root.disconnect();
}
```

PropertyExtender
----------------

This class contains the fallowing extenders

* PropertyExtender.Properties
    * Accessed via `RootFW.property()`
    * This is a global property extender which can be used to work with registered properties (Like using getprop and setprop in the shell)
* PropertyExtender.File
    * Accessed via `RootFW.property(String file)`
    * This is a per file extender which can be used to work with property files like /system/build.prop

```java
RootFW root = new RootFW();

if (root.connect()) {
    if ( ! root.property().exists("dalvik.vm.dexopt-data-only") ) {
        root.filesystem("/system").addMount( new String[]{"remount", "rw"} );

        root.property("/system/build.prop").set("dalvik.vm.dexopt-data-only", "1");

        root.filesystem("/system").addMount( new String[]{"remount", "ro"} );
    }

    root.disconnect();
}
```

FilesystemExtender
------------------

This class contains the fallowing extenders

* FilesystemExtender.Filesystem
    * Accessed via `RootFW.filesystem()`
    * This is a global file system extender which is used for general file system work like getting list of mounted devices, checking file system type support etc.
* FilesystemExtender.Device
    * Accessed via `RootFW.filesystem(String device)`
    * This is a per device extender which can be used to get device information like mount location, file system type partition size and so on, or operations like mount, unmount, remount and move mount location. 

```java
RootFW root = new RootFW();

if (root.connect()) {
    // If mmcblk0p2 is not mounted and the device kernel supports ext4 file system
    if (! root.filesystem("/dev/block/mmcblk0p2").isMounted() && root.filesystem().hasTypeSupport("ext4")) {
        // Mount mmcblk0p2 on /sd-ext
        if( root.filesystem("/dev/block/mmcblk0p2").addMount("/sd-ext", "ext4", new String[]{"nosuid", "nodev", "noatime", "nodiratime"}) ) {
            // Get disk statistisk like device path, disk size, usage etc.
            DiskStat diskInfo = root.filesystem("/sd-ext").statDisk();

            Long size = diskInfo.size();
            Long usage = diskInfo.usage();

            // Attach /sd-ext/app to /data/app (mount --bind)
            root.filesystem("/sd-ext/app").addMount("/data/app") );
        }
    }

    root.disconnect();
}
```

FileExtender
------------

This class contains the fallowing extenders

* FileExtender.File
    * Accessed via `RootFW.file(String file)`
    * This is a per file extender which can be used to get information about a file or folder, move, delete, rename, write, read and more

```java
RootFW root = new RootFW();

if (root.connect()) {
    File myfile = root.file("/path/to/myfile");

    if (myfile.exists()) {
        if ( myfile.isLink() ) {
            myfile = myfile.openCanonical();
        }

        if ( myfile.readOneMatch("myPattern") == null ) {
            myfile.write("something to write");

            ...

        } else {
            FileData data = myfile.readMatches("myPattern");

            if (data != null) {
                String[] lines = data.trim().assort("#").getArray();

                for (int i=0; i < lines.length; i++) {
                    ...
                }
            }
        }
    }

    root.disconnect();
}
```

BinaryExtender
--------------

This class contains the fallowing extenders

* BinaryExtender.Binary
    * Accessed via `RootFW.binary(String binary)`
    * This is a per binary extender which provides tools for checking the existence of a binary among others
* BinaryExtender.Busybox
    * Accessed via `RootFW.busybox()` or `RootFW.busybox(String busybox)`
    * This is a per binary extender which provides busybox specific binary tools

```java
RootFW root = new RootFW();

if (root.connect()) {
    // We could also use root.busybox().exists()
    if ( root.binary("busybox").exists() ) {
        // We could also use root.busybox("/path/to/busybox").getApplets() if we want a specific busybox binary
        String[] applets = root.busybox().getApplets();

        for (int i=0; i < applets.length; i++) {
            ...
        }
    }

    root.disconnect();
}
```


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
