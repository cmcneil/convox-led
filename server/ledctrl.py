#!/usr/bin/env python

from select import select
import socket as sck
import struct

NUM_LEDS = 90
NGLOBES = 9

class Strand:
    """
    Represents a full chain of LEDs, with LEDs controllable at the 
    individual level.
    """

    def __init__(self, leds=NUM_LEDS, dev="/dev/spidev0.0"):
        """
        Initializes a strand.
        leds: full number of leds in the chain.
        dev: device to write to. raspi spi port by defaul.
        """
        self.dev = dev
        self.spi = file(self.dev, "wb")
        self.leds = leds
        self.gamma = bytearray(256)
        self.n = 0 # The pixel currently being written
        
        self.buffer = [0 for x in range(self.leds)]
        for led in range(self.leds):
            self.buffer[led] = bytearray(3)

        self.start = 0
        
        # Hardware specific color correction. For explanation, see:
        # http://learn.adafruit.com/light-painting-with-raspberry-pi/software
        for i in range(256):
            self.gamma[i] = 0x80 | int(pow(float(i) / 255.0, 2.5)*127.0 + 0.5)
    
    def push(self, r, g, b):
        """
        Pushes the color specified by r, g, b out to the chain, which acts
        as a queue.(beginning with the end closest to device) 
        All colors move down, last color falls off.
        r, g, b: color values in the range 0 - 255
        """
        self.start = (self.start - 1) % len(self.buffer)
        self.buffer[self.start][0] = self.gamma[g]
        self.buffer[self.start][1] = self.gamma[r]
        self.buffer[self.start][2] = self.gamma[b]
    
    def ipush(self, r, g, b):
        """
        inverse push.
        Pushes the color specified by r, g, b out to the chain, which acts
        as a queue.(beginning with the end *FURTHEST FROM* device) 
        All colors move down, last color falls off.
        r, g, b: color values in the range 0 - 255
        """
        self.buffer[self.start][0] = self.gamma[g]
        self.buffer[self.start][1] = self.gamma[r]
        self.buffer[self.start][2] = self.gamma[b]
        self.start = (self.start + 1) % len(self.buffer)

    def set(self, i, r, g, b):
        """
        Sets led i (0-indexed beginning with the closest to the port) to 
        the color specified by r, g, b.
        i : integer index, 0 - self.leds
        r, g, b: integer color values in the range 0 - 255
        """
        self.buffer[i][0] = self.gamma[g]
        self.buffer[i][1] = self.gamma[r]
        self.buffer[i][2] = self.gamma[b]
        
    def rotate(self, opp=False):
        """
        Rotates all of the colors around the strand, looping the last to
        the first.
        """
        if opp:
            self.start = (self.start -1) % len(self.buffer)
        else:
            self.start = (self.start + 1) % len(self.buffer)
        
    def fill(self, r, g, b):
        """
        Fill the strand with a single color.
        r, g, b: integer color values in range 0-255
        """
        start = 0
        for led in range(self.leds):
            self.buffer[led][0] = self.gamma[g]
            self.buffer[led][1] = self.gamma[r]
            self.buffer[led][2] = self.gamma[b]
            
    def update(self):
        """
        Flush the buffer to the strand.
        This must be called in order to observe any change.
        """
        self.spi.write(bytearray(b'\x00'))
        self.spi.flush()
        for x in range(self.start, self.leds):
            self.spi.write(self.buffer[x])
            self.spi.flush()
        # Weird Bugfix:
        # For some as of yet undetermined reason, the last LED in the strip
        # does not behave correctly, unless an extra three bytes are 
        # pushed at the end of the chain. It does not matter what these
        # bytes are, so we push 0, 0, 0
        for x in range(0, self.start):
            self.spi.write(self.buffer[x])
            self.spi.flush()
        self.spi.write(bytearray([129, 129, 129]))
        self.spi.write(bytearray(b'\x00')) # 0 byte resets shift register
        self.spi.flush()


class GlobeChain:
    """
    Represents a chain of globes, each containing a constant number
    of leds, allowing for easier control of the chain at the globe
    level. (i.e. control lights in chunks of 10.)
    """
    def __init__(self, strand, nglobes=NGLOBES):
        """
        Takes a Strand() object, as well an integer number of globes.
        Assumes that the leds are distributed evenly across the globes.
        (for example, 10 LEDs per globe)
        """
        self.globe_size = int(strand.leds / nglobes)
        self.nglobes = nglobes
        self.strand = strand
    
    def fill(self, r, g, b):
        """
        Fills the entire chain of globes with 1 color.
        r, g, b: integer colors from 0-255
        """
        self.strand.fill(r, g, b)

    def set(self, i, r, g, b):
        """
        Sets a globe indexed by i to color specified by r, g, b
        i: integer 0 to (self.leds - 1)
        r, g, b: color specified by integer 0 - 255
        """
        for j in range(i * self.globe_size, (i+1) * self.globe_size):
            self.strand.set(j, r, g, b)

    def rotate(self, opp=False):
        """
        Rotates globe colors around the chain.
        """
        for i in range(self.globe_size):
            self.strand.rotate(opp)
            
    def rotate_strand(self, i=None):
        """
        Rotates individual led colors around the strand. If an index i is
        provided, then the colors are instead rotated in an individual 
        globe.
        """
        pass # TODO

    def push(self, r, g, b):
        """
        Pushes a color out at the globe level, treating it like a 
        queue.
        r, g, b: integer color values in range 0-255
        """
        old_start = self.strand.start
        self.strand.start = (self.strand.start - 10) % len(self.strand.buffer)
        if self.strand.start < old_start:
            for i in range(self.strand.start, old_start):
                self.strand.set(i, r, g, b)
        else:
            for i in range(self.strand.start, len(self.strand.buffer)):
                self.strand.set(i, r, g, b)
            for i in range(0, old_start):
                self.strand.set(i, r, g, b)
            

    def ipush(self, r, g, b):
        """
        inverse push.
        Pushes the color specified by r, g, b out to the chain, which acts
        as a queue.(beginning with the end *FURTHEST FROM* device) 
        All colors move down, last color falls off.
        r, g, b: color values in the range 0 - 255
        """
        old_start = self.strand.start
        self.strand.start = (self.strand.start + 10) % len(self.strand.buffer)
        
        if old_start < self.strand.start:
            for i in range(old_start, self.strand.start):
                self.strand.set(i, r, g, b)
        else:
            for i in range(old_start, len(self.strand.buffer)):
                self.strand.set(i, r, g, b)
            for i in range(0, self.strand.start):
                self.strand.set(i, r, g, b)
        
    def update(self):
        """
        Flush buffer.
        """
        self.strand.update()
