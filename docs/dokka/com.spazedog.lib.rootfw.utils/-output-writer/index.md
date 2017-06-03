[com.spazedog.lib.rootfw.utils](../index.md) / [OutputWriter](.)

# OutputWriter

`abstract class OutputWriter : `[`Writer`](http://docs.oracle.com/javase/6/docs/api/java/io/Writer.html)

Special writer that is build to work with [ShellStream](../../com.spazedog.lib.rootfw/-shell-stream/index.md)

This class cannot used by itself.
Use [ShellStream.getWriter](../../com.spazedog.lib.rootfw/-shell-stream/get-writer.md) to get the one that works with
a specific instance

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `OutputWriter(lock: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`? = null)`<br>Special writer that is build to work with [ShellStream](../../com.spazedog.lib.rootfw/-shell-stream/index.md) |

### Functions

| Name | Summary |
|---|---|
| [close](close.md) | `open fun close(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [flush](flush.md) | `open fun flush(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [write](write.md) | `open fun write(cbuf: `[`CharArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char-array/index.html)`?, off: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, len: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Inheritors

| Name | Summary |
|---|---|
| [Writer](../../com.spazedog.lib.rootfw/-shell-stream/-writer.md) | `abstract class Writer : OutputWriter`<br>Special stream reader class returned by [getWriter](../../com.spazedog.lib.rootfw/-shell-stream/get-writer.md) |
