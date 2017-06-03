[com.spazedog.lib.rootfw.utils](../index.md) / [Data](.)

# Data

`abstract class Data<T : Data<T>>`

Abstract data container that stores data as lines in an array.

The class provides basic tools to manipulate the data
being stored in different ways.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Data(lines: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`<br>Abstract data container that stores data as lines in an array. |

### Properties

| Name | Summary |
|---|---|
| [mLines](m-lines.md) | `var mLines: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<ul><li></li></ul> |

### Functions

| Name | Summary |
|---|---|
| [assort](assort.md) | `fun assort(regex: <ERROR CLASS>): T`<br>Sort lines by invalidating a RegExp`fun assort(contains: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, ignoreCase: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): T`<br>Sort lines by checking the absence of a sequence within each line`fun assort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and the end of the data array and exclude the elements within the range`fun assort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, stop: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and `stop` and exclude the elements within the range |
| [getArray](get-array.md) | `fun getArray(): `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>This will return the data array |
| [getLine](get-line.md) | `fun getLine(lineNum: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = -1, skipEmpty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>This will return one specified line of the data array. |
| [getSize](get-size.md) | `fun getSize(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Count the lines in the data array |
| [getString](get-string.md) | `fun getString(separater: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "\n"): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This will return a string of the data array with custom characters used as line breakers |
| [replace](replace.md) | `fun replace(regex: <ERROR CLASS>, replace: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): T`<br>Replace sequence in each line using RegExp`fun replace(find: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, replace: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): T`<br>Replace sequence in each line by matching a sequence |
| [reverse](reverse.md) | `fun reverse(): T`<br>Reverses the lines in this collection |
| [sort](sort.md) | `fun sort(regex: <ERROR CLASS>): T`<br>Sort lines by validating a RegExp`fun sort(contains: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, ignoreCase: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): T`<br>Sort lines by checking the existence of a sequence within each line`fun sort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and the end of the data array and keep the elements within the range`fun sort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, stop: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and `stop` and keep the elements within the range |
| [trim](trim.md) | `fun trim(): T`<br>This method will remove all of the empty lines from the data array and trim each line |

### Inheritors

| Name | Summary |
|---|---|
| [Command](../../com.spazedog.lib.rootfw/-command/index.md) | `open class Command : Data<`[`Command`](../../com.spazedog.lib.rootfw/-command/index.md)`>`<br>Used to configure a command before executing and to store the result |
