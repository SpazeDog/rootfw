[com.spazedog.lib.rootfw.utils](../index.md) / [Debug](index.md) / [&lt;init&gt;](.)

# &lt;init&gt;

`Debug(tag: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`

Debug class that can be used to send logcat entries with pre-defined tags

Note that this class will only send logcat messages if and only if `DEBUG` returns `true`,
also note that the methods are inline, meaning that they will be copied to the location where
they are called. There is no need to manually do a debug check each time to save resources on
calling the methods, the conditions are handled automatically.

### Parameters

`tag` -

```

```
    A tag that will automatically be pre-pended to all entries sent by this instance
```

```

