[com.spazedog.lib.rootfw](../index.md) / [ShellStream](.)

# ShellStream

`class ShellStream : Any`

I/O stream for a terminal process

This stream is used to write to a terminal process and uses
listeners to retrieve any output from stdout asynchronously.

### Types

| Name | Summary |
|---|---|
| [Interfaces](-interfaces/index.md) | `object Interfaces : Any`<br>Object containing interfaces that can be used with ShellStream |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `ShellStream(listener: (ShellStream, Boolean) -> Unit)`<br>`ShellStream(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`)`<br>Create a new ShellStream`ShellStream()`<br>I/O stream for a terminal process |

### Functions

| Name | Summary |
|---|---|
| [addConnectionListener](add-connection-listener.md) | `fun addConnectionListener(listener: (ShellStream, Boolean) -> Unit): `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`?`<br>Add a new Kotlin lambda as [ConnectionListener](-interfaces/-connection-listener/index.md)`fun addConnectionListener(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`): Unit`<br>Add a new [ConnectionListener](-interfaces/-connection-listener/index.md) |
| [addStreamListener](add-stream-listener.md) | `fun addStreamListener(listener: (ShellStream, String) -> Unit): `[`StreamListener`](-interfaces/-stream-listener/index.md)`?`<br>Add a new Kotlin lambda as [StreamListener](-interfaces/-stream-listener/index.md)`fun addStreamListener(listener: `[`StreamListener`](-interfaces/-stream-listener/index.md)`): Unit`<br>Add a new [StreamListener](-interfaces/-stream-listener/index.md) |
| [connect](connect.md) | `fun connect(): Boolean`<br>Connect to the terminal process using default setup`fun connect(requestRoot: Boolean): Boolean`<br>`fun connect(requestRoot: Boolean, wait: Boolean): Boolean`<br>`fun connect(requestRoot: Boolean, wait: Boolean, ignoreErr: Boolean): Boolean`<br>Connect to the terminal process |
| [destroy](destroy.md) | `fun destroy(): Unit`<br>Kill the terminal process |
| [disconnect](disconnect.md) | `fun disconnect(): Unit`<br>Send exit signal to the terminal process |
| [ignoreErrorStream](ignore-error-stream.md) | `fun ignoreErrorStream(): Boolean`<br>Check whether or not this stream has been configured to ignore stderr |
| [isConnected](is-connected.md) | `fun isConnected(): Boolean`<br>Check whether or not the terminal process is active |
| [isRootStream](is-root-stream.md) | `fun isRootStream(): Boolean`<br>Check whether or not this stream is running with root privileges |
| [removeConnectionListener](remove-connection-listener.md) | `fun removeConnectionListener(listener: (ShellStream, Boolean) -> Unit): Unit`<br>`fun removeConnectionListener(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`): <ERROR CLASS>`<br>Remove a connection listener |
| [removeStreamListener](remove-stream-listener.md) | `fun removeStreamListener(listener: (ShellStream, String) -> Unit): Unit`<br>`fun removeStreamListener(listener: `[`StreamListener`](-interfaces/-stream-listener/index.md)`): <ERROR CLASS>`<br>Remove a stream listener |
| [streamId](stream-id.md) | `fun streamId(): Int`<br>Return this stream's id |
| [write](write.md) | `fun write(vararg out: String): Boolean`<br>Write one or more [String](#)'s to the terminal process`fun write(vararg out: Byte): Boolean`<br>Write one or more [Byte](#)'s to the terminal process |
| [writeLines](write-lines.md) | `fun writeLines(vararg lines: String): Boolean`<br>Write lines to the terminal process |

### Companion Object Properties

| Name | Summary |
|---|---|
| [STREAMS](-s-t-r-e-a-m-s.md) | `var STREAMS: Int` |
