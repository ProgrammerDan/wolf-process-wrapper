Wolf External Process wrapper
====

This is a simple process wrapper to allow other languages to compete in the Wolf challenge on code-golf.

Specification
====
The protocol will be via STDIN and STDOUT hooks, and is split into initialization, Move, and Attack. In each case communication with your process will be via STDIN, and a reply is necessary from STDOUT. If a reply is not received in 1 second, your process will be assumed to be dead and an exception will be thrown. All characters will be encoded in UTF-8, for consistency.

Note that only a single process will be created, all Wolves must be managed within that one process. Read on for how this spec will help.

###Initialization

**STDIN**: `S<id>`
**STDOUT**: `K<id>`
**<id>**: `00` or `01` or ... or `99`

####Explanation:

The character `S` will be sent followed by the numeric characters `0`, `1`, ..., `99` indicating which of the 100 wolves is being initialized. In all future communication with that specific wolf, the same `<id>` will be used.

To ensure your process is alive, you must reply with the character `K` followed by the same `<id>` you received. Any other reply will result in an exception, killing your wolf.

###Movement

**STDIN**: `M<id><C0><C1>...<C7><C8>`
**STDOUT**: `<mv><id>`
**<C_n_>**: `W` or ` ` or `B` or `R` or `L`
**<mv>**: `H` or `U` or `L` or `R` or `D`

####Explanation:
The character `M` will be sent followed by the two character <id> to indicate which Wolf needs to choose a move. Following that, 9 characters will be sent representing that Wolf's surroundings, in row order (top row, middle row, bottom row from leftmost to rightmost).

Reply with one of the valid movement characters, followed by the Wolf's two digit <id> for confirmation.

###Attack

**STDIN**: `A<id><C>`
**STDOUT**: `<atk><id>`

