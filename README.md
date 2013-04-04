rootfw
======

An Android Root Shell Framework

Usage
------

RootFW is a tool that helps Android Applications act as root. 
The only way for an application to perform tasks as root, is by executing shell commands
as Android has no native way of doing this. However, due to different types of shell support on
different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy
task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method
tries to support as many different environments as possible by implementing different approaches 
for each environment. This makes the work of app developers a lot easier.

Unlike other Root methods, RootFW does not reconnect/exit on each command. Once an instance has
been created, the connection to the shell will stay open until the close() method is called.
This means that you can execute as many commands as you like without the SuperUser Toast
going crazy on the users screen.

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

RootFW can also return named instances which allows
for clones to be returned containing the same shell connection as it's parent instance. 
This means that you can access the same shell connection across application classes/methods
without parsing the instance manually.

    public void method1() {
        // A new connection is opened here
        RootFW root = RootFW.instance("myinstance"); 

        root.shell.execute("...");

        // Call the second method
        method2(); 

        // The connection is being closed here
        root.close(); 
    }

    public void method2() {

        // Since "myinstance" is still active, a clone will be returned instead
        RootFW root = RootFW.instance("myinstance");

        root.shell.execute("...");

        // This will be ignored as clones cannot close connections
        root.close(); 
    }

    method1();

Since method1 is first to be called, this one will be provided with the main instance and method2 will be provided
with a copy/clone. Since clones is not allowed to close a connection, the connection will not be closed until close()
is called from method1. So even though it seams that two connections is established and closed, in fact only one
connection is being used. 

Using RootFW you can also add multiple command entries which will stop at the first command entry
that returns 0. This makes it easy to add better support for different environments.

    ShellResult result = root.shell.execute("mv file1 file2", "cat file1 > file2 && unlink file1");

If the device supports "mv", then the second command will not be executed. But if the first entry fails, 
it will move to the second command.
 
Using this feature, RootFW adds an easier way to use different all-in-one binary environments.

    ShellResult result = root.shell.execute( ShellProcess.generate("%binary mv file1 file2") );

The above will generate one command entry per index in ShellProcess.BINARIES[]
By default it will generate {"busybox mv file1 file2", "toolbox mv file1 file2", "mv file1 file2"}
 
If you want to use different binaries across RootFW, just modify BINARIES[]

    ShellProcess.BINARIES = new String[] {"/path/to/my/custom/busybox", "busybox", "toolbox"};

    ShellResult result = root.shell.execute( ShellProcess.generate("%binary mv file1 file2") );

Now /path/to/my/custom/busybox will be used as default and the others will be used to
fallback on if the custom one lacks support for a specific command.

By default RootFW will stop at the first command entry that returns 0 (Normally means success). 
But you can also add other result codes to be seen as successful. 

    ShellProcess process = new ShellProcess();
    process.addCommand("exit 255");
    process.addCommand("exit 130");
    process.addCommand("exit 0");
    process.addResultCode(130);

    ShellResult result = root.shell.execute(process);

Now RootFW will stop at the second command

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
