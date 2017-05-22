[com.spazedog.lib.rootfw](../../../index.md) / [ShellStream](../../index.md) / [Interfaces](../index.md) / [StreamListener](index.md) / [onStdOut](.)

# onStdOut

`abstract fun onStdOut(stream: `[`ShellStream`](../../index.md)`, line: String): Unit`

Called whenever the stream receives a new output line from the terminal process

### Parameters

`stream` - The [ShellStream](../../index.md) that invoked this method

`line` - The line received from stdout