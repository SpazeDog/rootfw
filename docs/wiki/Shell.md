/ [Wiki](wiki.md) / [Shell]()

# Shell

The `Shell` class is a wrapper for `ShellStream` that aims at more regular shell tasks. This class works synchronously and makes general shell tasks more easy than `ShellStream`.

### Example

```java
Shell shell = new Shell();
Command command = shell.execute("some command", 0);

if (command.getResultSuccess()) {
    // Handle command output
}

shell.destroy();
```

The `Shell` class does not really deal with strings, but instead with a [Command](Command.md) class that acts as both input and output container. But it has convenient methods that automaticaly creates a `Command` with the parsed string and populates it with shell output.

### Example: Using Command class

```java
Shell shell = new Shell();
Command command = new Command("some command", 0);

shell.execute(command);

if (command.getResultSuccess()) {
    // Handle command output
}

shell.destroy();
```

As mentioned above, the `Shell` class is a wrapper for `ShellStream`, which means that you can reuse any existing connections with `Shell`.

### Example: Re-use ShellStream

```java
ShellStream stream = new ShellStream();
Shell shell = new Shell(stream);  // Add existing ShellStream
Command command = new Command("some command", 0);

shell.execute(command);

if (command.getResultSuccess()) {
    // Handle command output
}

shell.close();  // Do not destroy
```

> Note the last line compared to the other examples. We replaced `destroy()` with `close()`. The first method will not only close the `Shell` class, but also destroy the wrapped `ShellStream`. If we want to keep use our connection for other things, we can instead use `close()` which will only cleanup the `Shell` class, but not touch the `ShellStream` within.

