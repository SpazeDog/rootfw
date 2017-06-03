[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [getReader](.)

# getReader

`fun getReader(): `[`Reader`](http://docs.oracle.com/javase/6/docs/api/java/io/Reader.html)

Get a [Reader](-reader.md) connected to this stream

After returning the [Reader](-reader.md), this class will enter a synchronous state
that will follow the read calls made to the [Reader](-reader.md). This will affect
any [StreamListener](-interfaces/-stream-listener/index.md)'s attached to this instance as they will have to wait
for read calls to the [Reader](-reader.md) before receiving any output.

Remember to close this reader when you are done, this will release the
synchronous state and enter it back into asynchronous

