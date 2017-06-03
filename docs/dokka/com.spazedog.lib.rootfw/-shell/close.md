[com.spazedog.lib.rootfw](../index.md) / [Shell](index.md) / [close](.)

# close

`fun close(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

This will detach from [ShellStream](../-shell-stream/index.md) without destroying it

This class adds listeners to [ShellStream](../-shell-stream/index.md) to handle output and monitor connection state.
This method will detach itself from the stream instance but will not destroy it, meaning
that this instance will no longer work, but the [ShellStream](../-shell-stream/index.md) instance will.

