RootFW 4
========

An Android Root Shell Framework

RootFW is a tool that helps Android Applications act as root. The only way for an application to perform tasks as root, is by executing shell commands as Android has no native way of doing this. However, due to different types of shell support on different devices/ROM's (Shell type, busybox/toolbox versions etc.), this is not an easy task. RootFW comes with a lot of pre-built methods to handle the most common tasks. Each method tries to support as many different environments as possible by implementing different approaches for each environment. This makes the work of app developers a lot easier.

Table of content
----------------

* Basic Overview
    * [Usage](#usage)
	* [Attempts](#attempts)
	* [Stream](#stream)

* Utils/IO
	* [FileReader](#filereader)

Usage
-----

Executing shell commands using RootFW is easy. Create an instance of the `Shell` class and use the `Shell.execute(String)` method. 

```java
// Create a new Shell instance
Shell conn = new Shell(true); // The Boolean arguments defines whether or not to create a root shell, or just a regular shell.

// Execute a shell command
conn.execute("echo 'Some text' > /data/file.txt");
```

One issue when working with the Android shell, is that there is no standard for the tools available. Some systems has busybox available while others don't, and the busybox version differs on each platform. 

To handle this issue in the easiest way possible, the `Shell` class has the `Shell.execute(String[])` method, which takes a range of command and tries each one until one of them succeed. 

```java
// Create a new Shell instance
Shell conn = new Shell(true); // The Boolean arguments defines whether or not to create a root shell, or just a regular shell.

// Try 3 different commands 
conn.execute( new String[]{"cat /data/file.txt", "toolbox cat /data/file.txt", "busybox cat /data/file.txt"} );
```

Both `Shell.execute(String)` and `Shell.execute(String[])` returns an instance of `Shell.Result`, which contains the result code, result data along with a lot of useful tools to help make it easier to work with the result data. 

```java
// Create a new Shell instance
Shell conn = new Shell(true); // The Boolean arguments defines whether or not to create a root shell, or just a regular shell.

// Try 3 different commands 
Result result = conn.execute( new String[]{"cat /data/file.txt", "toolbox cat /data/file.txt", "busybox cat /data/file.txt"} );

/*
 * Check to see if the result was a success
 */
if (result.wasSuccessful()) {
    /*
     * Get the number for the command that was successful
     */
    Integer cmdNum = result.getCommandNumber();

    switch(cmdNum) {
        case 0: The first command was the success. 
        case 1: The second command was the success. Toolbox was used. 
        case 2: The third command was the success. Busybox was used. 
    }

    /*
     * Remove all empty lines from the result data
     */
    result.trim();

    /*
     * Get the last line from the result data
     */
    String line = result.getLine(-1);
}
```

You can also do an asynchronous execution and have the result passed to a callback class. 

```java
// Create a new Shell instance
Shell conn = new Shell(true); // The Boolean arguments defines whether or not to create a root shell, or just a regular shell.

// Execute a shell command asynchronous
conn.executeAsync("cat /data/file.txt", new OnShellResultListener(){
    @Override
    public void onShellResult(Result result) {
        if (result.wasSuccess()) {
            // Do something here...
        }
    }
});
```

Attempts
--------

Using attempts is a good way to support different kinds of invironments. But it can be a pain to manually create a range of commands for each command that you wish to execute, so to help with this we have an `Attempts` class that can be accessed using `Shell#createAttempts`. 

```java
// Create a new Shell instance
Shell conn = new Shell(true);

// Create a range of attempts
Attempts attempts = conn.createAttempts("ls"); // This will create `ls`, `busybox ls` and `toolbox ls` by default. 

// Execute normal
Result result = attempts.execute();

// Execute asynchronous
attempts.executeAsync(new OnShellResultListener(){
    @Override
    public void onShellResult(Result result) {
        
    }
});

Each attempt is created based on `Common#BINARIES` which by default contains `String[null, "busybox", "toolbox"]`.
```

Stream
------

Sometimes one need to start a consistent command and monitor the ongoing output. For this RootFW has the `ShellStream` class. 

```java
// Create a new ShellStream instance
ShellStream stream = new ShellStream(true, new OnStreamListener(){
    public void onStreamStart() {
        // A command has been executed
    }

    public void onStreamInput(String outputLine) {
        // A new output line has been recieved
    }

    public void onStreamStop(Integer resultCode) {
        // The command has ended
    }

    public void onStreamDied() {
        // The connection has been closed by 'exit' or died
    }
});

// Execute a command
stream.execute("cat /dev/input/event1");
```

FileReader
----------

RootFW comes with it's own `FileReader` class. Unlike the `java.io.FileReader`, this one is also able to access protected files via a SuperUser shell. However the shell is only used in cases where it is needed, otherwise a regular reader is used to access the file. 

```java
// Create an instance of com.spazedog.lib.rootfw4.utils.io.FileReader
FileReader reader = new FileReader("/data/data/com.some.package/shared_prefs/pref.xml");

// Add the reader to a regular java.io.BufferedReader
BufferedReader buffer = new BufferedReader(reader);

String line = null;
while ((line = buffer.readLine()) != null) {

}
```

Like mentioned above, the SuperUser shell will not be used unless it has to. If the app running, has access to the file, then a regular reader is used to access it. 
