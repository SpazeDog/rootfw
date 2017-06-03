[com.spazedog.lib.rootfw](../index.md) / [ShellStream](.)

# ShellStream

`class ShellStream`

I/O stream for a terminal process

This stream is used to write to a terminal process and uses
listeners or a [Reader](-reader.md) to retrieve any output from stdout asynchronously or synchronously.

### Types

| Name | Summary |
|---|---|
| [Interfaces](-interfaces/index.md) | `object Interfaces`<br>Object containing interfaces that can be used with ShellStream |
| [Reader](-reader.md) | `abstract class Reader : `[`InputReader`](../../com.spazedog.lib.rootfw.utils/-input-reader/index.md)<br>Special stream reader class returned by [getReader](get-reader.md) |
| [Writer](-writer.md) | `abstract class Writer : `[`OutputWriter`](../../com.spazedog.lib.rootfw.utils/-output-writer/index.md)<br>Special stream reader class returned by [getWriter](get-writer.md) |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `ShellStream(listener: (ShellStream, `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)`<br>`ShellStream(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`)`<br>Create a new ShellStream`ShellStream()`<br>I/O stream for a terminal process |

### Functions

| Name | Summary |
|---|---|
| [addConnectionListener](add-connection-listener.md) | `fun addConnectionListener(listener: (ShellStream, `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`?`<br>Add a new Kotlin lambda as [ConnectionListener](-interfaces/-connection-listener/index.md)`fun addConnectionListener(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a new [ConnectionListener](-interfaces/-connection-listener/index.md) |
| [addStreamListener](add-stream-listener.md) | `fun addStreamListener(listener: (ShellStream, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`StreamListener`](-interfaces/-stream-listener/index.md)`?`<br>Add a new Kotlin lambda as [StreamListener](-interfaces/-stream-listener/index.md)`fun addStreamListener(listener: `[`StreamListener`](-interfaces/-stream-listener/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a new [StreamListener](-interfaces/-stream-listener/index.md) |
| [connect](connect.md) | `fun connect(requestRoot: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, wait: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, ignoreErr: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Connect to the terminal process |
| [destroy](destroy.md) | `fun destroy(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Kill the terminal process |
| [disconnect](disconnect.md) | `fun disconnect(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Send exit signal to the terminal process |
| [getReader](get-reader.md) | `fun getReader(): `[`Reader`](http://docs.oracle.com/javase/6/docs/api/java/io/Reader.html)<br>Get a [Reader](-reader.md) connected to this stream |
| [getWriter](get-writer.md) | `fun getWriter(): `[`Writer`](http://docs.oracle.com/javase/6/docs/api/java/io/Writer.html)<br>Get a [Writer](-writer.md) connected to this stream |
| [ignoreErrorStream](ignore-error-stream.md) | `fun ignoreErrorStream(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Check whether or not this stream has been configured to ignore stderr |
| [isConnected](is-connected.md) | `fun isConnected(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Check whether or not the terminal process is active |
| [isRootStream](is-root-stream.md) | `fun isRootStream(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Check whether or not this stream is running with root privileges |
| [removeConnectionListener](remove-connection-listener.md) | `fun removeConnectionListener(listener: (ShellStream, `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>`fun removeConnectionListener(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`): <ERROR CLASS>`<br>Remove a connection listener |
| [removeStreamListener](remove-stream-listener.md) | `fun removeStreamListener(listener: (ShellStream, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>`fun removeStreamListener(listener: `[`StreamListener`](-interfaces/-stream-listener/index.md)`): <ERROR CLASS>`<br>Remove a stream listener |
| [streamId](stream-id.md) | `fun streamId(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Return this stream's id |
| [write](write.md) | `fun write(vararg out: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Write one or more [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)'s to the terminal process`fun write(vararg out: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Write one or more [Char](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)'s to the terminal process |
| [writeLines](write-lines.md) | `fun writeLines(vararg lines: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Write lines to the terminal process |
