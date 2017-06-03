[com.spazedog.lib.rootfw.utils](../index.md) / [Debug](index.md) / [log](.)

# log

`inline fun log(msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`msg` -

```

```
    Message to send
```

```

`inline fun log(msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, e: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

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

`inline fun log(identifier: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`identifier` -

```

```
    Additional tag that will be apended to one parsed in the constructor, separated by `:`
```

```

`msg` -

```

```
    Message to send
```

```

`inline fun log(identifier: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, e: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`identifier` -

```

```
    Additional tag that will be apended to one parsed in the constructor, separated by `:`
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

`inline fun log(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`type` -

```

```
    Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
```

```

`msg` -

```

```
    Message to send
```

```

`inline fun log(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, e: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`type` -

```

```
    Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
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

`inline fun log(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, identifier: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`type` -

```

```
    Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
```

```

`identifier` -

```

```
    Additional tag that will be apended to one parsed in the constructor, separated by `:`
```

```

`msg` -

```

```
    Message to send
```

```

`inline fun log(type: `[`Char`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-char/index.html)`, identifier: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, msg: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, e: `[`Throwable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-throwable/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send message to logcat

### Parameters

`type` -

```

```
    Type of log `d`,`i`,`w`, or `e` for `debug`, `info`, `warning` or `error`
```

```

`identifier` -

```

```
    Additional tag that will be apended to one parsed in the constructor, separated by `:`
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

