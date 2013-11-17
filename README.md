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

  0. Whether to apply any pending changes to the strand.
  1. Signals pushing a color to the strand. Reverse bit (4) controls direction.
  2. Signals setting a specific led. If this bit is set, the two index bytes are
    required, else there should be no index bytes.
  3. Signals a fill command, where all led's are set to the given color.
  4. If set, a push command will push from the far end of the strand instead of
    the near end (relative to the raspberry pi), while a rotate command will
    rotate the opposite direction.
  5. Rotate. Reverse bit (4) will control direction.
  6. Hypotnuse bit; see
    http://en.wikipedia.org/wiki/Linear_feedback_shift_register#Galois_LFSRs
  7. If set, use HSV instead of RGB for color

That's it! Have fun.
