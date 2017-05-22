[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [writeLines](.)

# writeLines

`fun writeLines(vararg lines: String): Boolean`

Write lines to the terminal process

This method will append a line feed character to each line,
which is equal to hitting ENTER in a terminal. If you don't want any
line feeds, you can use [ShellStream.write](write.md) instead.

### Parameters

`lines` - Lines to write