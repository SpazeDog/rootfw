/ [Wiki](wiki.md) / [Command]()

# Command

This class is used together with [Shell](Shell.md) to execute commands. Each `Command` may contain multiple `Calls`. The `Shell` class will execute each `Call`, until one returns successful. This is useful on Android shells because Android does not really have much standards when it comes to what to support and or how much. F.eks. one device may include `pidof` while another does not, or support `ls -lna` while another only supports `ls -l` and so on. Some may support `ls` via `busybox` while others only use `toolbox`. The `Command` class can help deal with this issue. 

### Example

```java
Shell shell = new Shell();
Command command = new Command();

// Add multiple calls
command.addCall("ls -lna");
command.addCall("ls -l");

// Execute one by one until one is successful
shell.execute(command);

// Check if we have any successful calls
if (command.getResultSuccess()) {
    // See which one and deal with expected output
    if (command.getResultCall() > 0) {
        // We successfully executed 'ls -l'

    } else {
        // We successfully executed 'ls -lna'
    }
}
```

Maybe instead we want to deal with the different all-in-one binaries like `toolbox`, `busybox` and `toybox`.

```java
 ...

// Add multiple calls
command.addCall("toolbox ls -l");
command.addCall("busybox ls -l");
command.addCall("toybox ls -l");

// Execute one by one until one is successful
shell.execute(command);

// Check if we have any successful calls
if (command.getResultSuccess()) {
    ...
}
```

The `Command` class has a fast way of dealing with this type of thing. 

```java
 ...

// Add multiple calls
command.addCall("toolbox ls -l", 0, true);

// Execute one by one until one is successful
shell.execute(command);

// Check if we have any successful calls
if (command.getResultSuccess()) {
    ...
}
```

The second argument tells `Command` what result code is considered a successful code. It defaults to `0`. The third argument tells `Command` to auto-populate with calls for each registered all-in-one binary, these can be added via `Command.addBinary()`, and `Command` has `busybox`, `toybox` and `toolbox` by default. It also includes a non-binary `Call` for devices with symlinks. So the above will automatically create 4 calls: 

1. ls -l
2. toybox ls -l
3. busybox ls -l
4. toolbox ls -l

