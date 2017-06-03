[com.spazedog.lib.rootfw](../index.md) / [Command](index.md) / [addCall](.)

# addCall

`open fun addCall(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

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

`command` -

```

```
    Shell command
```

```

`resultCode` -

```

```
    An acceptible result code for the shell command
```

```

`populate` -

```

```
    Populate with all registered all-in-one binaries
```

```

`open fun addCall(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCodes: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>, populate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

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

`command` -

```

```
    Shell command
```

```

`resultCode` -

```

```
    An acceptible result code for the shell command
```

```

`populate` -

```

```
    Populate with all registered all-in-one binaries
```

```

`open fun addCall(callback: `[`CallCreator`](-interfaces/-call-creator/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Add a new [Call](-containers/-call/index.md) instances that is build from a callback interface.

This method will run through all the all-in-one binaries in [BINARIES](#)
and call the callback each time, allowing you to create custom [Call](-containers/-call/index.md) instances
for each binary.

### Parameters

`callback` -

```

```
    The [CallCreator] callback to use
```

```

**Return**

```

```
    This callback should return the [Call] instances to use or `NULL` to skip the current one
```

```

`open fun addCall(callback: (bin: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?) -> `[`Call`](-containers/-call/index.md)`?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Add a new [Call](-containers/-call/index.md) instances that is build from a Kotlin lambda callback.

This method will run through all the all-in-one binaries in [BINARIES](#)
and call the callback each time, allowing you to create custom [Call](-containers/-call/index.md) instances
for each binary.

### Parameters

`callback` -

```

```
    The lambda callback to use
```

```

**Return**

```

```
    This callback should return the [Call] instances to use or `NULL` to skip the current one
```

```

