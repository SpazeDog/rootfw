[com.spazedog.lib.rootfw](../index.md) / [Command](index.md) / [&lt;init&gt;](.)

# &lt;init&gt;

`Command(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false)`
`Command(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCodes: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`)`
`Command(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`)`
`Command(callback: (it: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?) -> `[`Call`](-containers/-call/index.md)`?)`

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

