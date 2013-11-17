Welcome!
========

Overview
--------

We wanted to create a simple interface for controlling Adafruit LED strands.
This server is run on a Raspberry Pi connected to the LED strand, and recieves
messages under the protocol discribed below.

Protocol
--------

The input consists of a header byte, two optional index bytes, and three color
bytes, which are required for all headers except the update command. The bits in
the header are as follows:

|================
| bit | Use/meaning
|  0 | Update
|  1 | Push
|  2 | Set
|  3 | Fill
|  4 | Reverse
|  5 | Rotate
|  6 | Hypotnuse
|  7 | HSV
|================

That's it! Have fun.
