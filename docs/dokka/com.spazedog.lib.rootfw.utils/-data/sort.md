[com.spazedog.lib.rootfw.utils](../index.md) / [Data](index.md) / [sort](.)

# sort

`fun sort(regex: <ERROR CLASS>): T`

Sort lines by validating a RegExp

### Parameters

`regex` - RegExp to validate

**Return**
This instance

`fun sort(contains: String): T`

Sort lines by checking the existence of a sequence within each line

### Parameters

`contains` - Sequence that each line must contain

**Return**
This instance

`fun sort(contains: String, ignoreCase: Boolean): T`

Sort lines by checking the existence of a sequence within each line

### Parameters

`contains` - Sequence that each line must contain

`ignoreCase` - Ignore case when comparing

**Return**
This instance

`fun sort(start: Int): T`

Sort lines using a range between `start` and the end of the data array and keep the elements within the range

### Parameters

`start` - Where to start

**Return**
This instance

`fun sort(start: Int, stop: Int): T`

Sort lines using a range between `start` and `stop` and keep the elements within the range

### Parameters

`start` - Where to start

`stop` - When to stop

**Return**
This instance

