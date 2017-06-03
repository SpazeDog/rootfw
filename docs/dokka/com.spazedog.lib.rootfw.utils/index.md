[com.spazedog.lib.rootfw.utils](.)

## Package com.spazedog.lib.rootfw.utils

### Types

| Name | Summary |
|---|---|
| [Data](-data/index.md) | `abstract class Data<T : `[`Data`](-data/index.md)`<T>>`<br>Abstract data container that stores data as lines in an array. |
| [Debug](-debug/index.md) | `class Debug`<br>Debug class that can be used to send logcat entries with pre-defined tags |
| [InputReader](-input-reader/index.md) | `abstract class InputReader : `[`Reader`](http://docs.oracle.com/javase/6/docs/api/java/io/Reader.html)<br>Special reader that is build to work with the asynchronous [ShellStream](../com.spazedog.lib.rootfw/-shell-stream/index.md) |
| [OutputWriter](-output-writer/index.md) | `abstract class OutputWriter : `[`Writer`](http://docs.oracle.com/javase/6/docs/api/java/io/Writer.html)<br>Special writer that is build to work with [ShellStream](../com.spazedog.lib.rootfw/-shell-stream/index.md) |

### Properties

| Name | Summary |
|---|---|
| [DEBUG](-d-e-b-u-g.md) | `val DEBUG: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Check if we have a debug build or debug has been forced |

### Functions

| Name | Summary |
|---|---|
| [logDebug](log-debug.md) | `fun logDebug(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, tag: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, e: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>`fun logDebug(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, tag: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Send mesage to logcat |
| [setForcedDebug](set-forced-debug.md) | `fun setForcedDebug(flag: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Debug is set based on build type, but you can force it on releases |
