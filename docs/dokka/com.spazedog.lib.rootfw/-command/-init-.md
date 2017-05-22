[com.spazedog.lib.rootfw](../index.md) / [Command](index.md) / [&lt;init&gt;](.)

# &lt;init&gt;

`Command(command: String)`
`Command(command: String, resultCode: Int)`
`Command(command: String, resultCode: Int, populate: Boolean)`
`Command(command: String, resultCodes: Array<Int>, populate: Boolean)`
`Command(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`)`
`Command(callback: (String?) -> `[`Call`](-containers/-call/index.md)`)`

Create a new [Command](index.md)

**See Also**

[addCall](add-call.md)

`Command()`

Used to configure a command before executing and to store the result

One issue with Android is it's lack of standard shell environments.
Different ROM's has different support like different shells and
different all-in-one binaries such as `toybox`, `toolbox` and `busybox`.
Even each `toybox`, `toolbox` and `busybox` binaries are compiled with
different support and output layout. This makes it difficult to create something
that is ensured to work on all devices.

This class can help take some of the load of. Each command instance can contain
multiple `calls`. Each call contains a shell command and one or more acceptable result codes.
When the command instance are executed, each call will be executed until one produces an
acceptable result code.

