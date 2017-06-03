[com.spazedog.lib.rootfw](../../../index.md) / [Command](../../index.md) / [Interfaces](../index.md) / [CallCreator](index.md) / [onCreateCall](.)

# onCreateCall

`abstract fun onCreateCall(bin: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): `[`Call`](../../-containers/-call/index.md)`?`

Called by [Command.addCall](../../add-call.md) for each all-in-one binaries to create a call for

### Parameters

`bin` -

```

```
    The binary to create the call for
```

```

**Return**

```

```
    The [Call] that was created, or `NULL` to skip this binary
```

```

