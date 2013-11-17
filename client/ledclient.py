#!/usr/bin/env python

from socket import socket
import struct
from time import sleep

class LEDClient:
    """
    Thin interface over the protocol. Mostly just an example for reference
    if you want to use other languages.
    """
    def __init__(self, host='lucifer', port=666):
        self.server = socket()
        self.server.connect((host, port))
        self.hsv = 0x00

    def push(self, r, g, b):
        """
        Pushes a globe out.
        """
        self.server.send(struct.pack('!BBBB', 0x40 | self.hsv, r, g, b))
    
    def set(self, i, r, g, b):
        """
        Sets the color of a globe of index i
        """
        self.server.send(struct.pack('!BHBBB', 0x20 | self.hsv, i, r, g, b))

    def fill(self, r, g, b):
        """
        Sets all the lights to a single color
        """
        self.server.send(struct.pack('!BBBB', 0x10 | self.hsv, r, g, b))

    def rotate(self):
        """
        Rotates the globes around the chain.
        """
        self.server.send(struct.pack('!BBBB', 0x04 | self.hsv, r, g, b))

    def set_color_basis(self, basis):
        """
        Sets the color basis that arguments to other methods will be 
        interpreted in. Options are 'hsv' or 'rgb'
        """
        if basis.lower() == 'hsv':
            self.hsv = 0x01
        elif basis.lower() == 'rgb':
            self.hsv = 0x00
        else:
            raise ValueError, "We only support RGB or HSV color basis!"

    def update(self):
        """
        Flushes the buffer. SPI port is too slow to update constantly with
        no artifacts. Please update when you want a change to become
        visible.
        """
        self.server.send(struct.pack('!B', 0x80))


class LEDWarlock:
    """
    This is a higher level object which creates some cool packaged effects.
    """
    def __init__(self, client=None):
        if client == None:
            self.client = LEDClient()
        else:
            self.client = client
    
    def strobe(self, freq=10.0):
        """
        White light strobe. freq is the frequency in Herz
        """
        while(1):
            self.client.fill(0, 0, 0)
            self.client.update()
            sleep(1.0 / (freq * 2.0))
            self.client.fill(254, 254, 254)
            self.client.update()
            sleep(1.0 / (freq * 2.0))

    def sexy_time(self):
        """
        """

    def google(self):
        """
        It's a Google party!
        """
