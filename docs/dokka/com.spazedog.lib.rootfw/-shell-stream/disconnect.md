[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [disconnect](.)

# disconnect

`fun disconnect(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Send exit signal to the terminal process

Note that if a daemon like process is running in the
terminal process, this signal will never be intercepted by the terminal process.
To force kill the terminal process, you can use [ShellStream.destroy](destroy.md)

