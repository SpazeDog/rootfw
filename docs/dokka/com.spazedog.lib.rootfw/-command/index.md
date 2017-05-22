[com.spazedog.lib.rootfw](../index.md) / [Command](.)

# Command

`class Command : `[`Data`](../../com.spazedog.lib.rootfw.utils/-data/index.md)`<Command>`

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

### Types

| Name | Summary |
|---|---|
| [Containers](-containers/index.md) | `object Containers : Any`<br>Contains class containers that can be used with this class |
| [Interfaces](-interfaces/index.md) | `object Interfaces : Any`<br>Contains interfaces that can be used with this class |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Command(command: String)`<br>`Command(command: String, resultCode: Int)`<br>`Command(command: String, resultCode: Int, populate: Boolean)`<br>`Command(command: String, resultCodes: Array<Int>, populate: Boolean)`<br>`Command(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`)`<br>`Command(callback: (String?) -> `[`Call`](-containers/-call/index.md)`)`<br>Create a new Command`Command()`<br>Used to configure a command before executing and to store the result |

### Inherited Properties

| Name | Summary |
|---|---|
| [mLines](../../com.spazedog.lib.rootfw.utils/-data/m-lines.md) | `var mLines: Array<String>`<ul><li></li></ul> |

### Functions

| Name | Summary |
|---|---|
| [addCall](add-call.md) | `fun addCall(command: String): <ERROR CLASS>`<br>Add a new [Call](-containers/-call/index.md) that is auto build from a shell command.
The acceptible result code will be `0``fun addCall(command: String, resultCode: Int): <ERROR CLASS>`<br>Add a new [Call](-containers/-call/index.md) that is auto build from a shell command.`fun addCall(command: String, resultCode: Int, populate: Boolean): <ERROR CLASS>`<br>Add a new [Call](-containers/-call/index.md) that is auto build from a shell command,
and optionally auto populate with all registered all-in-one binaries`fun addCall(command: String, resultCodes: Array<Int>, populate: Boolean): Unit`<br>Add a new [Call](-containers/-call/index.md) that is auto build from a shell command with multiple acceptible result codes
and optionally auto populate with all registered all-in-one binaries`fun addCall(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`): Unit`<br>Add a new [Call](-containers/-call/index.md) instances that is build from a callback interface.`fun addCall(callback: (String?) -> `[`Call`](-containers/-call/index.md)`): Unit`<br>Add a new [Call](-containers/-call/index.md) instances that is build from a Kotlin lambda callback. |
| [getCallAt](get-call-at.md) | `fun getCallAt(pos: Int): `[`Call`](-containers/-call/index.md)`?`<br>Get the [Call](-containers/-call/index.md) instance at the specified array position |
| [getCallSize](get-call-size.md) | `fun getCallSize(): Int`<br>Get the number of [Call](-containers/-call/index.md) instances added to this class |
| [getCalls](get-calls.md) | `fun getCalls(): Array<`[`Call`](-containers/-call/index.md)`>`<br>Get all [Call](-containers/-call/index.md) instances currently added to this class |
| [getResultCall](get-result-call.md) | `fun getResultCall(): Int`<br>Array position of the last executed call |
| [getResultCode](get-result-code.md) | `fun getResultCode(): Int`<br>The result code of the last executed call |
| [getResultSuccess](get-result-success.md) | `fun getResultSuccess(): Boolean`<br>Returns `TRUE` if one of the calls returned with an acceptible result code |

### Inherited Functions

| Name | Summary |
|---|---|
| [assort](../../com.spazedog.lib.rootfw.utils/-data/assort.md) | `fun assort(regex: <ERROR CLASS>): T`<br>Sort lines by invalidating a RegExp`fun assort(start: Int, stop: Int): T`<br>Sort lines using a range between `start` and `stop` and exclude the elements within the range`fun assort(start: Int): T`<br>Sort lines using a range between `start` and the end of the data array and exclude the elements within the range`fun assort(contains: String): T`<br>`fun assort(contains: String, ignoreCase: Boolean): T`<br>Sort lines by checking the absence of a sequence within each line |
| [getArray](../../com.spazedog.lib.rootfw.utils/-data/get-array.md) | `fun getArray(): Array<String>`<br>This will return the data array |
| [getLine](../../com.spazedog.lib.rootfw.utils/-data/get-line.md) | `fun getLine(): String?`<br>This will return the last line in the data array`fun getLine(lineNum: Int, skipEmpty: Boolean): String?`<br>`fun getLine(lineNum: Int): String?`<br>This will return one specified line of the data array. |
| [getSize](../../com.spazedog.lib.rootfw.utils/-data/get-size.md) | `fun getSize(): Int`<br>Count the lines in the data array |
| [getString](../../com.spazedog.lib.rootfw.utils/-data/get-string.md) | `fun getString(): String`<br>This will return a string of the data array with line feed as separators`fun getString(separater: String): String`<br>This will return a string of the data array with custom characters used as line breakers |
| [replace](../../com.spazedog.lib.rootfw.utils/-data/replace.md) | `fun replace(regex: <ERROR CLASS>, replace: String): T`<br>Replace sequence in each line using RegExp`fun replace(find: String, replace: String): T`<br>Replace sequence in each line by matching a sequence |
| [reverse](../../com.spazedog.lib.rootfw.utils/-data/reverse.md) | `fun reverse(): T`<br>Reverses the lines in this collection |
| [sort](../../com.spazedog.lib.rootfw.utils/-data/sort.md) | `fun sort(contains: String): T`<br>`fun sort(contains: String, ignoreCase: Boolean): T`<br>Sort lines by checking the existence of a sequence within each line`fun sort(regex: <ERROR CLASS>): T`<br>Sort lines by validating a RegExp`fun sort(start: Int): T`<br>Sort lines using a range between `start` and the end of the data array and keep the elements within the range`fun sort(start: Int, stop: Int): T`<br>Sort lines using a range between `start` and `stop` and keep the elements within the range |
| [trim](../../com.spazedog.lib.rootfw.utils/-data/trim.md) | `fun trim(): T`<br>This method will remove all of the empty lines from the data array and trim each line |

### Companion Object Properties

| Name | Summary |
|---|---|
| [mStaticBinaries](m-static-binaries.md) | `val mStaticBinaries: <ERROR CLASS>`<ul><li></li></ul> |

### Companion Object Functions

| Name | Summary |
|---|---|
| [addBinary](add-binary.md) | `fun addBinary(bin: String?): Unit`<br>Register new all-in-one binary like `busybox` or `toybox` |
