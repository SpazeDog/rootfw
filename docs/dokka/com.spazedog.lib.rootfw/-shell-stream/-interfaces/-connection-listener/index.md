[com.spazedog.lib.rootfw](../../../index.md) / [ShellStream](../../index.md) / [Interfaces](../index.md) / [ConnectionListener](.)

# ConnectionListener

`interface ConnectionListener`

Used to keep track of the terminal process connection

This can be added to a constructor or using [ShellStream.addConnectionListener](../../add-connection-listener.md).

If you use `Kotlin` then you can use lambda functions instead of this interface.
You can read more in the docs for [ShellStream.addConnectionListener](../../add-connection-listener.md).

### Functions

| Name | Summary |
|---|---|
| [onConnect](on-connect.md) | `abstract fun onConnect(stream: `[`ShellStream`](../../index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called when a connection has been established |
| [onDisconnect](on-disconnect.md) | `abstract fun onDisconnect(stream: `[`ShellStream`](../../index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called when a connection has been closed |

### Inheritors

| Name | Summary |
|---|---|
| [Shell](../../../-shell/index.md) | `class Shell : ConnectionListener, `[`StreamListener`](../-stream-listener/index.md)<br>Wrapper class for [ShellStreamer](#) for synchronous tasks |
