[com.spazedog.lib.rootfw.utils](../index.md) / [Data](index.md) / [assort](.)

# assort

`fun assort(regex: <ERROR CLASS>): T`

Sort lines by invalidating a RegExp

### Parameters

`regex` -

```

```
    RegExp to invalidate
```

```

**Return**

```

```
    This instance
```

```

`fun assort(contains: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, ignoreCase: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): T`

Sort lines by checking the absence of a sequence within each line

### Parameters

`contains` -

```

```
    Sequence that each line must not contain
```

```

`ignoreCase` -

```

```
    Ignore case when comparing
```

```

**Return**

```

```
    This instance
```

```

`fun assort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`

Sort lines using a range between `start` and the end of the data array and exclude the elements within the range

### Parameters

`start` -

```

```
    Where to start
```

```

**Return**

```

```
    This instance
```

```

`fun assort(start: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, stop: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): T`

Sort lines using a range between `start` and `stop` and exclude the elements within the range

### Parameters

`start` -

```

```
    Where to start
```

```

`stop` -

```

```
    When to stop
```

```

**Return**

```

```
    This instance
```

```

