[com.spazedog.lib.rootfw.utils](../index.md) / [Data](index.md) / [getLine](.)

# getLine

`fun getLine(): String?`

This will return the last line in the data array

**Return**
The last non-empty line of the data array

`fun getLine(lineNum: Int): String?`

This will return one specified line of the data array.

Note that this also takes negative number to get a line from the end and up

### Parameters

`lineNum` - The line number to return

**Return**
The specified line

`fun getLine(lineNum: Int, skipEmpty: Boolean): String?`

This will return one specified line of the data array.

Note that this also takes negative number to get a line from the end and up

### Parameters

`lineNum` - The line number to return

`skipEmpty` - Whether or not to include empty lines

**Return**
The specified line

