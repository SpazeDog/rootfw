[com.spazedog.lib.rootfw](../../../index.md) / [ShellStream](../../index.md) / [Interfaces](../index.md) / [StreamListener](.)

# StreamListener

`interface StreamListener : Any`

Used to retrieve output from stdout of the terminal process

This can be added using [ShellStream.addStreamListener](../../add-stream-listener.md).

If you use `Kotlin` then you can use lambda functions instead of this interface.
You can read more in the docs for [ShellStream.addStreamListener](../../add-stream-listener.md).

### Functions

| Name | Summary |
|---|---|
| [onStdOut](on-std-out.md) | `abstract fun onStdOut(stream: `[`ShellStream`](../../index.md)`, line: String): Unit`<br>Called whenever the stream receives a new output line from the terminal process |

### Inheritors

| Name | Summary |
|---|---|
| [Shell](../../../-shell/index.md) | `class Shell : `[`ConnectionListener`](../-connection-listener/index.md)`, StreamListener`<br>Wrapper class for [ShellStreamer](#) for synchronous tasks |
