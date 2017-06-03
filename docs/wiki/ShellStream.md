/ [Wiki](wiki.md) / [ShellStream]()

# ShellStream

The `ShellStream` class is the actual shell connection used by RootFW. Any other class that uses a shell connection, does so by using this class or some form of wrapper like the `Shell` class, but this class is always in the center of a connection. It mainly handles things asynchronously using listeners to retrieve any output. But it does also pack a custom `Reader` that can be used synchronously and classes like `Shell` uses lock features to provide synchronous shell access. That's the idea with this class, to provide one single connection class that can be used in different ways.

### Java Example: Listeners

```java
ShellStream stream = new ShellStream();
stream.connect();
stream.addStreamListener(new StreamListener(){
    @Override
    public void onStdOut(stream: ShellStream, line: String) {
        // New line received asynchronously
    }
});
```

### Kotlin Example: Listeners

```kotlin
val stream = ShellStream();
stream.connect()
stream.addStreamListener { shell: ShellStream, line: String ->
    // New line received asynchronously
}
```

> Kotlin has it's own lambda method, so there is no need to invoke a 'StreamListener' interface at all. Not even as a functional interface.

Before receiving any output, you may need to execute some commands. Because `ShellStream` is meant for different things, it has a few write methods for writing `lines`, `strings` and `characters`. The `lines` and `strings` methods are mostly the same, one just automatically adds line feeds, which is the method that is mostly used for commands. The other two are great for dealing with other things, like writing content to a file where you may not want line feeds at certain places.

### Example: Write Command

```java
stream.writeLines("command1", "command2");
```

> For regular shell operations, you may want to checkout the [Shell](Shell.md) class

One problem with the listeners, is that they only receieve output after each new line. That means every time a line feed has been detected. This is not always the best option if you want to extract data from a file. A binary file for example may not have anything that could be interpreted as a line feed and you would end up buffering the whole file before receiving the data. So to handle something like this, we can use the custom `Reader` class.

### Example: Reader

```java
Reader reader = stream.getReader();
stream.writeLines("cat file.ext");
char[] buffer = new char[64];
int len = 0;

while ((len = reader.read(buffer)) > 0) {
    // Handle buffer content
}

reader.close();
```

> Important: The stream will not go into synchronous mode until we have at least one active reader. Get the reader before issuing any commands, or the asynchronous mode will simply read the content and parse it any active listeners and discard. Also remember to close the reader or the stream will stay in synchronous mode and no listeners will receieve any output.

> While in synchronous mode, the active listeners will still receieve output. But only when the reader has read it's way to a line feed or to the end of the stream. Listeners always receieves output line by line. If there is no registered listeners, any output will be discarted after a reader has received it. In case of large file output, it is worth noting that even with an active reader, if there is any registered listeners, the output is still buffered until a full line has been assembled.

> To read or write to files, you may want to consider using the `FileReader` and `FileWriter` classes from the `rootfw.stdlib` library.

