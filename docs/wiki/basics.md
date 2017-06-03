/ [Wiki](wiki.md) / [Basics]()

# Basics

### Streamer

RootFW starts with the class called `ShellStream`. This class connects to a shell process as either a regular user or as SuperUser _(root)_ and starts a thread that monitors the shell output. The shell provides methods to write to the shell and listeners are used to collect any output created from executed commands. In other words, the streamer works asynchronous. You can also add listeners to monitor the connection state.

```java
// Create a new instance
ShellStream stream = new ShellStream();

// Add connection listener
stream.addConnectionListener(new ConnectionListener(){
    @Override
    public void onConnect(ShellStream stream) {
        // Handle connect
    }

    @Override
    public void onDisconnect(ShellStream stream) {
        // Handle disconnect
    }
});

// Add stream listener
stream.addStreamListener(new StreamListener(){
    @Override
    public void onStdOut(ShellStream stream, String line) {
        // Handle new output line
    }
});

// Connect to the shell
stream.connect();

// Write a command
stream.writeLine("someCommand");

// Close the connection
stream.disconnect();
```

If you are using Kotlin, you can also make use of Kotlin lambdas

```kotlin
// Create a new instance
val stream = ShellStream()

// Add connection listener
stream.addConnectionListener { stream: ShellStream, connected: Boolean ->
    if (connected) {
        // Handle connect

    } else {
        // Handle disconnect
    }
}

// Add stream listener
stream.addStreamListener { stream: ShellStream, line: String ->
    // Handle new output line
}

// Connect to the shell
stream.connect()

// Write a command
stream.writeLine("someCommand")

// Close the connection
stream.disconnect()
```

> The method `disconnect()` sends a exit signal to the shell process. If the shell process is busy with a daemon like execution, the signal will never be read by the shell process and the call to `disconnect()` will have no affect. In this case you can use the `destroy()` method instead. Just be aware that this method will kill the shell process at it's current state. This means that if you execute some regular command that simply processes and then prints some output, it may not get to finish before it is terminated. So always consider the use case before deciding which to use.


### Shell

Another useful tool in RootFW's collection, is the `Shell` class. This class acts as an synchronous wrapper for `ShellStream`. For normal shell tasks, this is the class that you would want to use.

```java
// Create a new instance
Shell shell = new Shell();

Command cmd = shell.execute("someCommand");

if (cmd.getResultSuccess()) {
    println(cmd.getLine());
}

shell.destroy();
```

The `Command` is actually what contains the command to execute, our `Shell` class just has a few helper methods that automatically creates it when parsing `String` as command. It is also what get's populated with the result from the execution. So it is used as both input and output.

```java
Command cmd = shell.execute(new Command("someCommand"));

// Or we can simply do
Command cmd = new Command("someCommand");
shell.execute(cmd);
```
> The `Command` extends from the abstract `Data` class that has a collection of tools to handle the shell output. Tools such as `trim()` that removes empty lines and trims everything else, `sort()` and `assort` that can exclude certain lines based on range, regexp, sequence comparison and more, `replace()` that can replace specific sequences in each line based on simple sequence comparison or regexp and more.

Android has no standard when it comes to the shell. Each device has a different type of all-in-one binary and each binary has different features and procudes different output. This makes it hard to create apps that uses the shell. The `Command` can help with some of this using it's `Call` feature. Each `Command` can contain multiple `Call`'s, which is a class that stores one shell command and one or more acceptible result codes for that shell command. When you execute the `Command`, the `Shell` class will execute every `Call` instance within, until it produces a result code that matches one of the acceptible ones from the executed `Call`.

```java
// Create a new Command
Command cmd = new Command();

// Add calls for different binaries
cmd.addCall("someCommand", 0);
cmd.addCall("busybox someCommand", 30);
cmd.addCall("toybox someCommand", 1);
cmd.addCall("toolbox someCommand", 255);

// Execute
shell.execute(cmd);

// Check to see if one of the calls produced an acceptible result code
if (cmd.getResultSuccess()) {
    // Get the id of the successful call
    int callId = cmd.getResultCall();

    // Get the call instance
    Call call = cmd.getCallAt(callId);

    // Check something
    if (call.command.startsWith("toybox")) {

    }
}
```

You don't have to create a call for each binary, the `Command` class can do this automatically using the third argument in `addCall()`.

```java
// Create a new Command
Command cmd = new Command();

// Add calls for different binaries
cmd.addCall("someCommand", 0, true);
```

> Note that all calls will use the same acceptible result code. Most shell command will produce the same result code no mater which binary, but whenever this is not the case, this shortcut might not be the best one

Or you can just do it from the `Shell` class.

```java
Command cmd = shell.execute("someCommand", 0, true);
```

By default RootFW has registered 3 binaries `toolbox`, `busybox` and `toybox`, but you can always add more to the `Command` class, this will also affect auto creation via `Shell`.

```java
Command.addBinary("customBinary");
```
