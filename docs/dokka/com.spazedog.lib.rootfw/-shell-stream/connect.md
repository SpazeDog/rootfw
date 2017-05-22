[com.spazedog.lib.rootfw](../index.md) / [ShellStream](index.md) / [connect](.)

# connect

`fun connect(): Boolean`

Connect to the terminal process using default setup

Default setup means no root privileges and stderr will not be ignored

`fun connect(requestRoot: Boolean): Boolean`

Connect to the terminal process

### Parameters

`requestRoot` - Request root privileges. If not possible, the connection will use normal privileges

`fun connect(requestRoot: Boolean, wait: Boolean): Boolean`

Connect to the terminal process

### Parameters

`requestRoot` - Request root privileges. If not possible, the connection will use normal privileges

`wait` - Block until connection listeners are done

`fun connect(requestRoot: Boolean, wait: Boolean, ignoreErr: Boolean): Boolean`

Connect to the terminal process

### Parameters

`requestRoot` - Request root privileges. If not possible, the connection will use normal privileges

`wait` - Block until connection listeners are done

`ignoreErr` - Filter stderr out of the output