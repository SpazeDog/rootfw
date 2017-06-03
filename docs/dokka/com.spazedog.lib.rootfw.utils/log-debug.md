[com.spazedog.lib.rootfw.utils](index.md) / [logDebug](.)

# logDebug

`inline fun logDebug(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, tag: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, e: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send mesage to logcat

Note that this function will only send logcat messages if and only if `DEBUG` returns `true`
also note that the function is inline, meaning that it will be copied to the location where
it is called. There is no need to manually do a debug check each time to save resources on
calling the function, the conditions are handled automatically.

### Parameters

`type` -

```

```
    Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
```

```

`tag` -

```

```
    Tag to be used on the entry
```

```

`msg` -

```

```
    Message to send
```

```

`e` -

```

```
    Throwable to parse to logcat
```

```

`inline fun logDebug(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, tag: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send mesage to logcat

Note that this function will only send logcat messages if and only if `DEBUG` returns `true`
also note that the function is inline, meaning that it will be copied to the location where
it is called. There is no need to manually do a debug check each time to save resources on
calling the function, the conditions are handled automatically.

### Parameters

`type` -

```

```
    Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
```

```

`tag` -

```

```
    Tag to be used on the entry
```

```

`msg` -

```

```
    Message to send
```

```

