[com.spazedog.lib.rootfw](../index.md) / [Shell](index.md) / [execute](.)

# execute

`fun execute(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, autopopulate: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, timeout: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 0L): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` -

```

```
    The command to execute
```

```

`resultCode` -

```

```
    The result code that should be produced upon a successful call
```

```

`autopopulate` -

```

```
    Whether or not to auto populate with all registered all-in-one binaries
```

```

`timeout` -

```

```
    Timeout in milliseconds after which to force quit
```

```

`fun execute(command: `[`Command`](../-command/index.md)`, timeout: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 0L): `[`Command`](../-command/index.md)

Execute a command

### Parameters

`command` -

```

```
    [Command] to execute
```

```

`timeout` -

```

```
    Timeout in milliseconds after which to force quit
```

```

