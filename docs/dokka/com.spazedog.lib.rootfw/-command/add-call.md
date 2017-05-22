[com.spazedog.lib.rootfw](../index.md) / [Command](index.md) / [addCall](.)

# addCall

`fun addCall(command: String): <ERROR CLASS>`

Add a new [Call](-containers/-call/index.md) that is auto build from a shell command.
The acceptible result code will be `0`

### Parameters

`command` - Shell command

`fun addCall(command: String, resultCode: Int): <ERROR CLASS>`

Add a new [Call](-containers/-call/index.md) that is auto build from a shell command.

### Parameters

`command` - Shell command

`resultCode` - An acceptible result code for the shell command

`fun addCall(command: String, resultCode: Int, populate: Boolean): <ERROR CLASS>`

Add a new [Call](-containers/-call/index.md) that is auto build from a shell command,
and optionally auto populate with all registered all-in-one binaries

Auto populate means that multiple [Call](-containers/-call/index.md) instances will be auto generated, each
using one of the registered all-in-one binaries that can be located at [BINARIES](#).

**Example**

```
ins.addCall("ls /", 0, true)
    -> ls /
    -> busybox ls /
    -> toolbox ls /
    -> toybox ls /
```

### Parameters

`command` - Shell command

`resultCode` - An acceptible result code for the shell command

`populate` - Populate with all registered all-in-one binaries

`fun addCall(command: String, resultCodes: Array<Int>, populate: Boolean): Unit`

Add a new [Call](-containers/-call/index.md) that is auto build from a shell command with multiple acceptible result codes
and optionally auto populate with all registered all-in-one binaries

Auto populate means that multiple [Call](-containers/-call/index.md) instances will be auto generated, each
using one of the registered all-in-one binaries that can be located at [BINARIES](#).

**Example**

```
ins.addCall("ls /", 0, true)
    -> ls /
    -> busybox ls /
    -> toolbox ls /
    -> toybox ls /
```

### Parameters

`command` - Shell command

`resultCode` - An acceptible result code for the shell command

`populate` - Populate with all registered all-in-one binaries

`fun addCall(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`): Unit`

Add a new [Call](-containers/-call/index.md) instances that is build from a callback interface.

This method will run through all the all-in-one binaries in [BINARIES](#)
and call the callback each time, allowing you to create custom [Call](-containers/-call/index.md) instances
for each binary.

### Parameters

`callback` - The [CallCreator](-interfaces/-call-creator/index.md) callback to use

**Return**
This callback should return the [Call](-containers/-call/index.md) instances to use or `NULL` to skip the current one

`fun addCall(callback: (String?) -> `[`Call`](-containers/-call/index.md)`): Unit`

Add a new [Call](-containers/-call/index.md) instances that is build from a Kotlin lambda callback.

This method will run through all the all-in-one binaries in [BINARIES](#)
and call the callback each time, allowing you to create custom [Call](-containers/-call/index.md) instances
for each binary.

### Parameters

`callback` - The lambda callback to use

**Return**
This callback should return the [Call](-containers/-call/index.md) instances to use or `NULL` to skip the current one

