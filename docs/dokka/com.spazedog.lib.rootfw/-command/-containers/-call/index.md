[com.spazedog.lib.rootfw](../../../index.md) / [Command](../../index.md) / [Containers](../index.md) / [Call](.)

# Call

`class Call`

Used to store a call that can be added to [Command](../../index.md)

### Parameters

`command` -

```

```
    The shell command to execute
```

```

`resultCodes` -

```

```
    One or more acceptable result codes
```

```

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Call(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`)`<br>`Call(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)``Call(command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, resultCodes: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>)`<br>Used to store a call that can be added to [Command](../../index.md) |

### Properties

| Name | Summary |
|---|---|
| [command](command.md) | `val command: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [resultCodes](result-codes.md) | `val resultCodes: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`>` |

### Functions

| Name | Summary |
|---|---|
| [hasResult](has-result.md) | `fun hasResult(resultCode: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Check if a specific result code is acceptable to this call |
