[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [destroy](.)

# destroy

`fun destroy(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Kill the terminal process

Note that this will not wait until some ongoing command finishes.
If you need to make sure that something get's processed fully before terminating,
consider using [ShellStream.disconnect](disconnect.md)

