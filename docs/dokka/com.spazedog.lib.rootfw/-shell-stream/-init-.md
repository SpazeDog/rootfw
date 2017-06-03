[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [&lt;init&gt;](.)

# &lt;init&gt;

`ShellStream(listener: (`[`ShellStream`](index.md)`, `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)`

Create a new [ShellStream](index.md)

### Parameters

`listener` -

```

```
    A kotlin lambda used a [ConnectionListener]
```

```

`ShellStream(listener: `[`ConnectionListener`](-interfaces/-connection-listener/index.md)`)`

Create a new [ShellStream](index.md)

### Parameters

`listener` -

```

```
    A [ConnectionListener]
```

```

`ShellStream()`

I/O stream for a terminal process

This stream is used to write to a terminal process and uses
listeners or a [Reader](-reader.md) to retrieve any output from stdout asynchronously or synchronously.

