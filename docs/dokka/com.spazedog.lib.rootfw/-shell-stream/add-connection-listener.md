[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [addConnectionListener](.)

# addConnectionListener

`fun addConnectionListener(listener: (`[`ShellStream`](index.md)`, Boolean) -> Unit): `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`?`

Add a new Kotlin lambda as [ConnectionListener](-interfaces/-connection-listener/index.md)

The second argument of the lambda represents the new state of the terminal process.
`TRUE` means connected and `FALSE` means disconnected.

### Parameters

`listener` - The listener to add

**Return**
An [ConnectionListener](-interfaces/-connection-listener/index.md) wrapper that can be used with [ShellStream.removeConnectionListener](remove-connection-listener.md)

`fun addConnectionListener(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`): Unit`

Add a new [ConnectionListener](-interfaces/-connection-listener/index.md)

### Parameters

`listener` - The listener to add