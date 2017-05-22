[com.spazedog.lib.rootfw](../index.md) / [Shell](index.md) / [execute](.)

# execute

`fun execute(command: String): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` - The command to execute

`fun execute(command: String, resultCode: Int): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` - The command to execute

`resultCode` - The result code that should be produced upon a successful call

`fun execute(command: String, resultCode: Int, timeout: Long): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` - The command to execute

`resultCode` - The result code that should be produced upon a successful call

`timeout` - Timeout in milliseconds after which to force quit

`fun execute(command: `[`Command`](../-command/index.md)`): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` - [Command](../-command/index.md) to execute

`fun execute(command: `[`Command`](../-command/index.md)`, timeout: Long): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` - [Command](../-command/index.md) to execute

`timeout` - Timeout in milliseconds after which to force quit