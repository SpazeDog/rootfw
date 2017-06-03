[com.spazedog.lib.rootfw.utils](../index.md) / [InputReader](.)

# InputReader

`abstract class InputReader : `[`Reader`](http://docs.oracle.com/javase/6/docs/api/java/io/Reader.html)

Special reader that is build to work with the asynchronous [ShellStream](../../com.spazedog.lib.rootfw/-shell-stream/index.md)

This class cannot used by itself.
Use [ShellStream.getReader](../../com.spazedog.lib.rootfw/-shell-stream/get-reader.md) to get the one that works with
a specific instance

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `InputReader(lock: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`? = null)`<br>Special reader that is build to work with the asynchronous [ShellStream](../../com.spazedog.lib.rootfw/-shell-stream/index.md) |

### Functions

| Name | Summary |
|---|---|
| [close](close.md) | `open fun close(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<ul><li></li></ul> |
| [read](read.md) | `open fun read(buf: `[`CharArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char-array/index.html)`?, off: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, len: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [ready](ready.md) | `open fun ready(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |

### Inheritors

| Name | Summary |
|---|---|
| [Reader](../../com.spazedog.lib.rootfw/-shell-stream/-reader.md) | `abstract class Reader : InputReader`<br>Special stream reader class returned by [getReader](../../com.spazedog.lib.rootfw/-shell-stream/get-reader.md) |
