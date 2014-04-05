Wolf External Process wrapper
====

This is a simple process wrapper to allow other languages to compete in the [Wolf challenge on code-golf](http://codegolf.stackexchange.com/q/25347/17546).

Specification
====
The protocol will be via STDIN and STDOUT hooks, and is split into initialization, Move, and Attack. In each case communication with your process will be via STDIN, and a reply is necessary from STDOUT. If a reply is not received in 1 second, your process will be assumed to be dead and an exception will be thrown. All characters will be encoded in UTF-8, for consistency.

Note that only a single process will be created, all Wolves must be managed within that one process. Read on for how this spec will help.

###Initialization

**STDIN**: `S<id>$<ndigits>$<mapsize>`

**STDOUT**: `K<id>`

**`<id>`**: `00` or `01` or ... or `99`

####Explanation:

The character `S` will be sent followed by two numeric characters `00`, `01`, ..., `99` indicating which of the 100 wolves is being initialized. In all future communication with that specific wolf, the same `<id>` will be used. After the `$` character, a variable size sequence of numeric characters will be sent, indicating how many digits are in the map size. The end of `<ndigits>` is indicated by the `$` character. What follows is a sequence of `<ndigits>` numeric characterswhich is the `<mapsize>`, the total # of cells in the map (see the original post for details).

To ensure your process is alive, you must reply with the character `K` followed by the same `<id>` you received. Any other reply will result in an exception, killing your wolf.

###Movement

**STDIN**: `M<id><C0><C1>...<C7><C8>`

**STDOUT**: `<mv><id>`

**`<Cn>`**: `W` or ` ` or `B` or `S` or `L`

**`W`**: Wolf

**` `**: Empty Space

**`B`**: Bear

**`S`**: Stone

**`L`**: Lion

**`<mv>`**: `H` or `U` or `L` or `R` or `D`

**`H`**: Move.HOLD

**`U`**: Move.UP

**`L`**: Move.LEFT

**`R`**: Move.RIGHT

**`D`**: Move.DOWN

####Explanation:

The character `M` will be sent followed by the two character `<id>` to indicate which Wolf needs to choose a move. Following that, 9 characters will be sent representing that Wolf's surroundings, in row order (top row, middle row, bottom row from leftmost to rightmost).

Reply with one of the valid movement characters `<mv>`, followed by the Wolf's two digit `<id>` for confirmation.

###Attack

**STDIN**: `A<id><C>`

**STDOUT**: `<atk><id>`

**`<C>`**: `W` or `B` or `S` or `L`

**`<atk>`**: `R` or `P` or `S` or `D`

**`R`**: Attack.ROCK

**`P`**: Attack.PAPER

**`S`**: Attack.SCISSORS

**`D`**: Attack.SUICIDE

####Explanation:

The character `A` will be sent followed by the two character `<id>` to indicate which Wolf is participating in an attack. This is followed by a single character `<C>` indicating which type of thing is attacking, either a `W`olf, `B`ear, `S`tone, or `L`ion.

Reply with one of the `<atk>` characters listed above, indicating what your response to the attack is, following by the two digit `<id>` for confirmation.

And that's it. There's no more to it. If you lose an attack, that `<id>` will never be sent to your process again, that's how you will know your Wolf has died -- if a complete Movement round has passed without that `<id>` ever being sent.

Conclusion
====

In this repository you'll find a single .java file. Search and replace the following strings to set up your bot:

Replace `<invocation>` with the command line argument that will properly execute your process.

Replace `<custom-name>` with a unique name for your Wolf.

Rename the file to be `Wolf<custom-name>.java`, where `<custom-name>` is replaced with the name you chose above.

To test your Wolf, compile the Java program (`javac Wolf<custom-name.java`), and follow Rusher's instructions to include it in the simulation program. Be sure to provide _clear_, _concise_ instructions on how to compile/execute your actual Wolf, which follows the scheme I've outlined above.

Good luck, and may nature be ever in your favor.
