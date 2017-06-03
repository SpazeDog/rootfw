[com.spazedog.lib.rootfw](../index.md) / [Command](.)

# Command

`open class Command : `[`Data`](../../com.spazedog.lib.rootfw.utils/-data/index.md)`<Command>`

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
| [Containers](-containers/index.md) | `object Containers`<br>Contains class containers that can be used with this class |
| [Interfaces](-interfaces/index.md) | `object Interfaces`<br>Contains interfaces that can be used with this class |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Command(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false)`<br>`Command(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCodes: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`)`<br>`Command(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`)`<br>`Command(callback: (it: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?) -> `[`Call`](-containers/-call/index.md)`?)`<br>Create a new Command`Command()`<br>Used to configure a command before executing and to store the result |

### Properties

| Name | Summary |
|---|---|
| [mBinaries](m-binaries.md) | `val mBinaries: <ERROR CLASS>`<ul><li></li></ul> |
| [mCalls](m-calls.md) | `val mCalls: <ERROR CLASS>`<ul><li></li></ul> |
| [mExecuted](m-executed.md) | `var mExecuted: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<ul><li></li></ul> |
| [mResultCall](m-result-call.md) | `var mResultCall: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<ul><li></li></ul> |
| [mResultCode](m-result-code.md) | `var mResultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<ul><li></li></ul> |

### Inherited Properties

| Name | Summary |
|---|---|
| [mLines](../../com.spazedog.lib.rootfw.utils/-data/m-lines.md) | `var mLines: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<ul><li></li></ul> |

### Functions

| Name | Summary |
|---|---|
| [addCall](add-call.md) | `open fun addCall(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a new [Call](-containers/-call/index.md) that is auto build from a shell command, and optionally auto populate with all registered all-in-one binaries`open fun addCall(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCodes: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a new [Call](-containers/-call/index.md) that is auto build from a shell command with multiple acceptible result codes and optionally auto populate with all registered all-in-one binaries`open fun addCall(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a new [Call](-containers/-call/index.md) instances that is build from a callback interface.`open fun addCall(callback: (bin: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?) -> `[`Call`](-containers/-call/index.md)`?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a new [Call](-containers/-call/index.md) instances that is build from a Kotlin lambda callback. |
| [getCallAt](get-call-at.md) | `open fun getCallAt(pos: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Call`](-containers/-call/index.md)`?`<br>Get the [Call](-containers/-call/index.md) instance at the specified array position |
| [getCallSize](get-call-size.md) | `open fun getCallSize(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Get the number of [Call](-containers/-call/index.md) instances added to this class |
| [getCalls](get-calls.md) | `open fun getCalls(): `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Call`](-containers/-call/index.md)`>`<br>Get all [Call](-containers/-call/index.md) instances currently added to this class |
| [getResultCall](get-result-call.md) | `open fun getResultCall(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Array position of the last executed call |
| [getResultCode](get-result-code.md) | `open fun getResultCode(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The result code of the last executed call |
| [getResultSuccess](get-result-success.md) | `open fun getResultSuccess(): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns `TRUE` if one of the calls returned with an acceptible result code |
| [onReset](on-reset.md) | `open fun onReset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Allow interaction with resetInternal() without exposing it |
| [onUpdateResult](on-update-result.md) | `open fun onUpdateResult(lines: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, resultCall: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Allow interaction with updateResultInternal() without exposing it |
| [reset](reset.md) | `open fun reset(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Reset this instance and clear all [Call](-containers/-call/index.md)'s |

### Inherited Functions

| Name | Summary |
|---|---|
| [assort](../../com.spazedog.lib.rootfw.utils/-data/assort.md) | `fun assort(regex: <ERROR CLASS>): T`<br>Sort lines by invalidating a RegExp`fun assort(contains: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, ignoreCase: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): T`<br>Sort lines by checking the absence of a sequence within each line`fun assort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and the end of the data array and exclude the elements within the range`fun assort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, stop: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and `stop` and exclude the elements within the range |
| [getArray](../../com.spazedog.lib.rootfw.utils/-data/get-array.md) | `fun getArray(): `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>This will return the data array |
| [getLine](../../com.spazedog.lib.rootfw.utils/-data/get-line.md) | `fun getLine(lineNum: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = -1, skipEmpty: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>This will return one specified line of the data array. |
| [getSize](../../com.spazedog.lib.rootfw.utils/-data/get-size.md) | `fun getSize(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>Count the lines in the data array |
| [getString](../../com.spazedog.lib.rootfw.utils/-data/get-string.md) | `fun getString(separater: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "\n"): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>This will return a string of the data array with custom characters used as line breakers |
| [replace](../../com.spazedog.lib.rootfw.utils/-data/replace.md) | `fun replace(regex: <ERROR CLASS>, replace: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): T`<br>Replace sequence in each line using RegExp`fun replace(find: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, replace: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): T`<br>Replace sequence in each line by matching a sequence |
| [reverse](../../com.spazedog.lib.rootfw.utils/-data/reverse.md) | `fun reverse(): T`<br>Reverses the lines in this collection |
| [sort](../../com.spazedog.lib.rootfw.utils/-data/sort.md) | `fun sort(contains: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, ignoreCase: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): T`<br>Sort lines by checking the existence of a sequence within each line`fun sort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, stop: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and `stop` and keep the elements within the range`fun sort(regex: <ERROR CLASS>): T`<br>Sort lines by validating a RegExp`fun sort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`<br>Sort lines using a range between `start` and the end of the data array and keep the elements within the range |
| [trim](../../com.spazedog.lib.rootfw.utils/-data/trim.md) | `fun trim(): T`<br>This method will remove all of the empty lines from the data array and trim each line |

### Companion Object Properties

| Name | Summary |
|---|---|
| [mBinariySupport](m-binariy-support.md) | `val mBinariySupport: <ERROR CLASS>`<ul><li></li></ul> |
| [mStaticBinaries](m-static-binaries.md) | `val mStaticBinaries: <ERROR CLASS>`<ul><li></li></ul> |

### Companion Object Functions

| Name | Summary |
|---|---|
| [addBinary](add-binary.md) | `fun addBinary(bin: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register new all-in-one binary like `busybox` or `toybox` |
| [getBinary](get-binary.md) | `fun getBinary(shell: `[`Shell`](../-shell/index.md)`, bin: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>Find which registered all-in-one binary packs a specific command |
