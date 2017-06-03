[com.spazedog.lib.rootfw](../index.md) / [Command](index.md) / [getBinary](.)

# getBinary

`fun getBinary(shell: `[`Shell`](../-shell/index.md)`, bin: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`

Find which registered all-in-one binary packs a specific command

Transforms something like `cat` into `toybox cat` or `toolbox cat`,
depending on whichever supports `cat`. Be careful though, this method
does this by invoking the argument `-h` on the specified command.
Something like `toolbox reboot -h` will not produce any output,
it will simply ignore the argument and reboot the device, if the
installed version supports reboot. It will work on most commands though.
Just dont use it on commands that will affect the system, like reboot.

### Parameters

`shell` -

```

```
    A [Shell] instance to use for the search
```

```

`bin` -

```

```
    The command to search for like `cat`, `ps` etc
```

```

**Return**

```

```
    This method returns `NULL` if no support could be found
```

```

