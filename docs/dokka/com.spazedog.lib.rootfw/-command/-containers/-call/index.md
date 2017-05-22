[com.spazedog.lib.rootfw](../../../index.md) / [Command](../../index.md) / [Containers](../index.md) / [Call](.)

# Call

`class Call : Any`

Used to store a call that can be added to [Command](../../index.md)

### Parameters

`command` - The shell command to execute

`resultCodes` - One or more acceptable result codes

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Call(command: String, resultCodes: Array<Int>)`<br>Used to store a call that can be added to [Command](../../index.md) |

### Properties

| Name | Summary |
|---|---|
| [command](command.md) | `val command: String` |
| [resultCodes](result-codes.md) | `val resultCodes: Array<Int>` |

### Functions

| Name | Summary |
|---|---|
| [hasResult](has-result.md) | `fun hasResult(resultCode: Int): Boolean`<br>Check if a specific result code is acceptable to this call |
