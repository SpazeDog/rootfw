[com.spazedog.lib.rootfw.utils](../index.md) / [Data](.)

# Data

`abstract class Data<T : Data<T>> : Any`

Abstract data container that stores data as lines in an array.

The class provides basic tools to manipulate the data
being stored in different ways.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Data(lines: Array<String>)`<br>Abstract data container that stores data as lines in an array. |

### Properties

| Name | Summary |
|---|---|
| [mLines](m-lines.md) | `var mLines: Array<String>`<ul><li></li></ul> |

### Functions

| Name | Summary |
|---|---|
| [assort](assort.md) | `fun assort(regex: <ERROR CLASS>): T`<br>Sort lines by invalidating a RegExp`fun assort(contains: String): T`<br>`fun assort(contains: String, ignoreCase: Boolean): T`<br>Sort lines by checking the absence of a sequence within each line`fun assort(start: Int): T`<br>Sort lines using a range between `start` and the end of the data array and exclude the elements within the range`fun assort(start: Int, stop: Int): T`<br>Sort lines using a range between `start` and `stop` and exclude the elements within the range |
| [getArray](get-array.md) | `fun getArray(): Array<String>`<br>This will return the data array |
| [getLine](get-line.md) | `fun getLine(): String?`<br>This will return the last line in the data array`fun getLine(lineNum: Int): String?`<br>`fun getLine(lineNum: Int, skipEmpty: Boolean): String?`<br>This will return one specified line of the data array. |
| [getSize](get-size.md) | `fun getSize(): Int`<br>Count the lines in the data array |
| [getString](get-string.md) | `fun getString(): String`<br>This will return a string of the data array with line feed as separators`fun getString(separater: String): String`<br>This will return a string of the data array with custom characters used as line breakers |
| [replace](replace.md) | `fun replace(regex: <ERROR CLASS>, replace: String): T`<br>Replace sequence in each line using RegExp`fun replace(find: String, replace: String): T`<br>Replace sequence in each line by matching a sequence |
| [reverse](reverse.md) | `fun reverse(): T`<br>Reverses the lines in this collection |
| [sort](sort.md) | `fun sort(regex: <ERROR CLASS>): T`<br>Sort lines by validating a RegExp`fun sort(contains: String): T`<br>`fun sort(contains: String, ignoreCase: Boolean): T`<br>Sort lines by checking the existence of a sequence within each line`fun sort(start: Int): T`<br>Sort lines using a range between `start` and the end of the data array and keep the elements within the range`fun sort(start: Int, stop: Int): T`<br>Sort lines using a range between `start` and `stop` and keep the elements within the range |
| [trim](trim.md) | `fun trim(): T`<br>This method will remove all of the empty lines from the data array and trim each line |

### Inheritors

| Name | Summary |
|---|---|
| [Command](../../com.spazedog.lib.rootfw/-command/index.md) | `class Command : Data<`[`Command`](../../com.spazedog.lib.rootfw/-command/index.md)`>`<br>Used to configure a command before executing and to store the result |
