RootFW
======

An Android Root Shell Framework

Usage
------

RootFW is a tool that helps Android Applications act as root. The only way for an application to perform tasks as root, is by executing shell commands as Android has no native way of doing this. However, due to different types of shell support on different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method tries to support as many different environments as possible by implementing different approaches for each environment. This makes the work of app developers a lot easier.

Unlike other Root methods, RootFW does not reconnect/exit on each command. Once an instance has been created, the connection to the shell will stay open until the `close()` method is called. This means that you can execute as many commands as you like without the SuperUser Toast going crazy on the users screen.

```java
RootFW root = new RootFW();

if (root.connected()) {
    ShellResult result = root.shell.execute("ls -1 /system");

    if (result != null && result.code() == 0) {
        String[] output = result.output().raw();

        for (int i=0; i < output.length; i++) {
            ...
        }
    }
}

root.close();
```

RootFW can also return named instances which allows for clones to be returned containing the same shell connection as it's parent instance.  This means that you can access the same shell connection across application classes/methods without parsing the instance manually.

```java
/* Sample 1 */

// This will create and return a new instance
RootFW root = RootFW.instance("MyInstance");
 
root.shell.execute("...");

// Call another method
helperMethod();

// helperMethod was not allowed to close the connection, so this will work fine
root.shell.execute("...");

// This will close the shell connection
root.close(); 
```
```java
/* Sample 2 */

public void helperMethod() {
    // This will create and return a clone using the same shell connection
    RootFW root = RootFW.instance("MyInstance");

    root.shell.execute("...");

    // This will do nothing as clones is not allowed to close connections
    root.close(); 
}
```
We call for the instance `MyInstance` in both `Sample 1` and `Sample 2`. However, since the instance has not yet been created in `Sample 1`, RootFW will create and return a new one. When we call `helperMethod` in `Sample 2`, the instance is still active as we have yet to call `close()` in `Sample 1`. So here it will instead return a clone containing the same shell connection as used in `Sample 1`. And because `helperMethod` get's a clone, it is not allowed to close the connection which means that we can keep executing shell commands in `Sample 1`, althought `Sample 2` made a call to `close()`.

Using RootFW you can also add multiple command entries which will stop at the first command entry that returns `0`. This makes it easy to add better support for different environments.

```java
ShellResult result = root.shell.execute("mv file1 file2", "cat file1 > file2 && unlink file1");
```

If the device supports "mv", then the second command will not be executed. But if the first entry fails, it will move to the second command. Using this feature, RootFW adds an easier way to use different `all-in-one binary` environments.

```java
ShellResult result = root.shell.execute( ShellProcess.generate("%binary mv file1 file2") );
```

The above will generate one command entry per index in `ShellProcess.BINARIES[]`. By default it will generate `{"busybox mv file1 file2", "toolbox mv file1 file2", "mv file1 file2"}`
 
If you want to use different binaries across RootFW, just modify `BINARIES[]`

```java
ShellProcess.BINARIES = new String[] {"/path/to/my/custom/busybox", "busybox", "toolbox"};

ShellResult result = root.shell.execute( ShellProcess.generate("%binary mv file1 file2") );
```

Now `/path/to/my/custom/busybox` will be used as default and the others will be used to fallback on if the custom one lacks support for a specific command.

By default RootFW will stop at the first command entry that returns `0` (Normally means success). But you can also add other result codes to be seen as successful. 

```java
ShellProcess process = new ShellProcess();
process.addCommand("exit 255");
process.addCommand("exit 130");
process.addCommand("exit 0");
process.addResultCode(130);

ShellResult result = root.shell.execute(process);
```

RootFW will now stop at the second command as you have added result code `130` to the `ShellProcess` instance.

You may also want to execute commands without root at some point. Using root will for an example have SuperUser creating Toast messages. To do this, simply add a boolean `false` when creating the instance.

```java
// Get a non-root instance
RootFW user = new RootFW(false);

// Get a non-root instance using the 'instance' method
RootFW user = RootFW.instance("MyInstance", false);
```

Note that when using `RootFW.instance()`, you can have two instances with the same name. One with root and one without. Calling `RootFW.instance()` with a `false` boolean will **not** return an already existing root instance with the same name. 

Examples
------

Like mentioned before, RootFW has a lot of pre-built tools called `extenders` to help make the job faster and easier. An `extender` is a group class containing a lot of tools matching each group type. RootFW has the fallowing extenders: `Shell`, `File`, `Filesystem`, `Binary`, `Busybox`, `Memory`, `Processes` and `Utils`.

* **Folder Content**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        ArrayList<FileStat> statlist = root.file.statList("/system");
        FileStat stat;

        if (statlist != null) {
            for (int i=0; i < statlist.size(); i++) {
                stat = statlist.get(i);

                System.out.print("[" + stat.type() + "] " + stat.name() + " (" + stat.size() + " bytes), " + stat.access() + ""));
            }
        }
    }
    ```
    ------

* **List Memory Information**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        MemStat stat = root.memory.usage();

        System.out.print("Used " + stat.memUsage() + " bytes of " + stat.memTotal() + " bytes, " + stat.memPercentage() + "%");
    }
    ```
    ------

* **Launch an Update.zip in Recovery**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        root.utils.recoveryInstall(context, R.raw.package);
    }
    ```
    ------

* **Remount /system as RW**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        root.filesystem.mount("/system", new String[]{"remount", "rw"});
    }
    ```
    ------

* **Mount an sd-ext partition**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        // Make sure that the directory exists
        if (!root.file.check("/sd-ext", "d")) {
            root.filesystem.mount("/", new String[]{"remount", "rw"});
            root.file.create("/sd-ext");
            root.filesystem.mount("/", new String[]{"remount", "ro"});
        }

        root.filesystem.mount("/dev/block/mmcblk0p2", "/sd-ext", "ext4", new String[]{"relatime", "noatime", "nodev"});
    }
    ```
    ------

* **Bind two folders**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        root.filesystem.mount("/sd-ext/app-asec", "/data/app-asec");
    }
    ```
    ------

* **Shutdown the device**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        root.processes.shutdown();
    }
    ```
    ------

* **Check if Busybox is available**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        if (root.busybox.exists()) {
            ...
        }
    }
    ```
    ------

* **Get device mount location**
    ```java
    RootFW root = new RootFW();

    if (root.connected()) {
        DiskStat stat = root.filesystem.statDisk("/dev/block/mmcblk0p2");

        System.out.print( stat.location() );
    }
    ```
    ------

* **Get registered property**
    ```java
    RootFW root = new RootFW();

    System.out.print( root.property.get("persist.sys.language") );
    ```
    ------

* **Change registered property globally**
    ```java
    RootFW root = new RootFW();
    root.property.set("persist.sys.language", "en");
    ```
    ------

* **Change property in prop files**
    ```java
    RootFW root = new RootFW();

    root.filesystem.mount("/system", new String[]{"remount", "rw"});
    root.property.setInFile("/system/build.prop", "keyguard.no_require_sim", "true");
    root.filesystem.mount("/system", new String[]{"remount", "ro"});
    ```
    ------

* **Remove property in prop files**
    ```java
    RootFW root = new RootFW();

    if (root.property.existInFile("/system/build.prop", "keyguard.no_require_sim")) {
        root.filesystem.mount("/system", new String[]{"remount", "rw"});
        root.property.removeFromFile("/system/build.prop", "keyguard.no_require_sim");
        root.filesystem.mount("/system", new String[]{"remount", "ro"});
    }
    ```
    ------

**Check the Documentation for further information about all of the available tools**

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
