[com.spazedog.lib.rootfw](../index.md) / [Shell](.)

# Shell

`class Shell : `[`ConnectionListener`](../-shell-stream/-interfaces/-connection-listener/index.md)`, `[`StreamListener`](../-shell-stream/-interfaces/-stream-listener/index.md)

Wrapper class for [ShellStreamer](#) for synchronous tasks

The [ShellStreamer](#) class works asynchronous, constantly reading stdout
and uses listeners to capture the output. This class can be used for synchronous
tasks on a more per command manner. It uses identifiers to figure out when output
from a specific command is done printing, meanwhile blocking calls and collecting
the data. Ones all output for a specific command has been captured, it is returned
along with result code and other useful information. For the most parts, this is
the class that you would want to work with.

Note that this class was build to handle multiple commands.
Unlike other similar libraries, this does not need to exit/re-connect
between each command.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Shell()`<br>`Shell(requestRoot: Boolean)`<br>Create a new Shell instance using a new default [ShellStream](../-shell-stream/index.md)`Shell(stream: `[`ShellStream`](../-shell-stream/index.md)`)`<br>Create a new Shell instance using an existing [ShellStream](../-shell-stream/index.md) |

### Functions

| Name | Summary |
|---|---|
| [destroy](destroy.md) | `fun destroy(): Unit`<br>Destroy the [ShellStream](../-shell-stream/index.md) used by this class |
| [execute](execute.md) | `fun execute(command: String): `[`Command`](../-command/index.md)<br>`fun execute(command: String, resultCode: Int): `[`Command`](../-command/index.md)<br>`fun execute(command: String, resultCode: Int, timeout: Long): `[`Command`](../-command/index.md)<br>`fun execute(command: `[`Command`](../-command/index.md)`): `[`Command`](../-command/index.md)<br>`fun execute(command: `[`Command`](../-command/index.md)`, timeout: Long): `[`Command`](../-command/index.md)<br>Execute a command |
| [getEnv](get-env.md) | `fun getEnv(name: String): String?`<br>Get value of an environment variable on the current connection |
| [getStream](get-stream.md) | `fun getStream(): `[`ShellStream`](../-shell-stream/index.md)<br>Get the [ShellStream](../-shell-stream/index.md) used by this class |
| [isActive](is-active.md) | `fun isActive(): Boolean`<br>Check whether or not the [ShellStream](../-shell-stream/index.md) used by this class is active/connected |
| [isRootShell](is-root-shell.md) | `fun isRootShell(): Boolean`<br>Check whether or not the [ShellStream](../-shell-stream/index.md) used by this class has root privileges |
| [setEnv](set-env.md) | `fun setEnv(name: String, value: Any): Boolean`<br>Set/Change the value of an environment variable on the current connection |
