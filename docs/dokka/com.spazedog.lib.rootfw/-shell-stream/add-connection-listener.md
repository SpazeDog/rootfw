[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [addConnectionListener](.)

# addConnectionListener

`fun addConnectionListener(listener: (`[`ShellStream`](index.md)`, `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`?`

Add a new Kotlin lambda as [ConnectionListener](-interfaces/-connection-listener/index.md)

The second argument of the lambda represents the new state of the terminal process.
`TRUE` means connected and `FALSE` means disconnected.

### Parameters

`listener` -

```

```
    The listener to add
```

```

**Return**

```

```
    An [ConnectionListener] wrapper that can be used with [ShellStream.removeConnectionListener]
```

```

`fun addConnectionListener(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Add a new [ConnectionListener](-interfaces/-connection-listener/index.md)

### Parameters

`listener` -

```

```
    The listener to add
```

```

