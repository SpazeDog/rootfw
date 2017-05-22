[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [&lt;init&gt;](.)

# &lt;init&gt;

`ShellStream(listener: (`[`ShellStream`](index.md)`, Boolean) -> Unit)`

Create a new [ShellStream](index.md)

### Parameters

`listener` - A kotlin lambda used a [ConnectionListener](-interfaces/-connection-listener/index.md)

`ShellStream(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`)`

Create a new [ShellStream](index.md)

### Parameters

`listener` - A [ConnectionListener](-interfaces/-connection-listener/index.md)

`ShellStream()`

I/O stream for a terminal process

This stream is used to write to a terminal process and uses
listeners to retrieve any output from stdout asynchronously.

