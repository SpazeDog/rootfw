[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [getWriter](.)

# getWriter

`fun getWriter(): `[`Writer`](http://docs.oracle.com/javase/6/docs/api/java/io/Writer.html)

Get a [Writer](-writer.md) connected to this stream

Unlike [getReader](get-reader.md), this will not change the behavior of this class.
This just provides a more direct access to the internal writer,
allowing you to control write and flush

