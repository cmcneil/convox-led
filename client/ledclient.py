#/usr/bin/env python

"""
This file is where various routines are stored. LEDClient provides a thin 
interface. LEDWarlock contains the routines.
"""

from socket import socket
import struct
from time import sleep
import sys

NGLOBES = 9
IDEAL_FLUSH = 0.05 # Experimental result. Flushing the buffer at about
                   # 200Hz prevents the TCP buffer from filling up,
                   # but still allows for smooth transitions. 

class LEDClient:
    """
    Thin interface over the protocol. Mostly just an example for reference
    if you want to use other languages.
    """
    def __init__(self, host='192.168.1.124', port=666):
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
    
    def _smooth_transition(self, c1, c2, poffset=0.4, toffset=0.01,
                          flush_rate=IDEAL_FLUSH):
        """
        This is a helper function for building routines easily.
        Given two colors, it will cause a smooth transition between them in
        rgb space. Lights will oscillate back and forth between the two 
        colors, linearly interpolating in RGB space. The lights are out of
        phase, the offset of which is determined by 
        poffset: 1 implies completely out of phase, 0 implies in phase.
        toffset: Determines how quickly they change in time.
        """    
        zipWith = lambda f, lst1, lst2 : [f(a, b) for (a,b) in 
                                          zip(lst1, lst2)]
        interp = lambda l1, l2, r : zipWith(
                        lambda x,y: int(r*(float(x) - float(y)) + 
                                        float(y)), l2, l1)
        tval = 0.0
        self.client.set_color_basis('rgb')
        while(1):
            for i in range(9):
                rval = (i*poffset + tval) % 2.0
                if rval > 1.0:
                    rval = 1.0 - (rval - 1.0)
                color = interp(c1, c2, rval)
                self.client.set(i, *color)
            self.client.update()
            sleep(flush_rate)
            tval += toffset     
               
    def clear(self):
        """
        Clears the Chain.
        """
        self.client.fill(0, 0, 0)
        self.client.update()
    
    def strobe(self, freq=10.0):
        """
        White light strobe. freq is the frequency in Herz
        """
        self.client.set_color_basis("rgb")
        while(1):
            self.client.fill(0, 0, 0)
            self.client.update()
            sleep(1.0 / (freq * 2.0))
            self.client.fill(255, 255, 255)
            self.client.update()
            sleep(1.0 / (freq * 2.0))

    def sexy_time(self):
        """
        Get your seduction on ;)
        """
        sexy_pink = (249, 130, 155)
        hot_red = (146, 0, 10) # Sangria
        apricot = (251, 206, 177)
        orange_red = (255, 36, 0)
        violet_pink = (249, 120, 200)
        self._smooth_transition(orange_red, violet_pink, toffset=0.04)

    def disco(self):
        """
        Pushes a rainbow out onto the chain.
        """
        H = 0
        S = 255
        V = 255
        self.client.set_color_basis('hsv')
        while(1):
            self.client.push(H, S, V)
            self.client.update()
            H += 30
            H %= 256
            sleep(0.5)

    def rainbow(self):
        """
        Rawr!
        """
        H = 0
        S = 255
        V = 255
        step = 0
        while True:
            self.client.set_color_basis('hsv')
            for i in range(9):
                self.client.set(i, (H + step + 28.333 * i) % 255, S, V)
            self.client.update()
            sleep(0.05)
            step += 1
            step %= 255

    def slow_fade_rainbow(self):
        H = 0
        S = 255
        V = 255
        self.client.set_color_basis('hsv')
        while(1):
            self.client.fill(H, S, V)
            self.client.update()
            H += 1
            H %= 256
            sleep(0.05)
 
    def christmas_time(self):
        """
        Ho ho ho.
        """
        red = (213, 15, 37)
        green = (0, 153, 37)
        self.client.set_color_basis('rgb')
        while(1):
            for i in range (9): 
                self.client.push(*red)
                self.client.update()
                sleep(0.3)
                self.client.push(*green)
                self.client.update()
                sleep(0.3)
            for i in range(9): 
                self.client.push(*red)
                self.client.update()
                sleep(0.15)
            sleep(0.15)
            for i in range(5):
                self.client.fill(*green)
                self.client.update()
                sleep(0.3)
                self.client.fill(*red)
                self.client.update()
                sleep(0.3)
            for i in range(9):
                for i in range(3):
                    self.client.push(*green) 
                    self.client.update()
                    sleep(0.1)
                for i in range(3):
                    self.client.push(*red)
                    self.client.update()
                    sleep(0.1)

    def google(self):
        """
        It's a Google party!
        """
        # https://sites.google.com/site/studentchromebook/event-resources-and-calendar/event-planning-print-materials
        self.client.set_color_basis('rgb')
        gblue = (51, 105, 232)
        gred = (213, 15, 37)
        gyellow = (238, 178, 17)
        ggreen = (0, 153, 37)
        palette = [gblue, gred, gyellow, ggreen]
        
        i = 0
        while(1):
            self.client.push(*palette[i])
            self.client.update()
            sleep(0.5)
            i = (i + 1) % len(palette)

    def outside(self):
        """
        An attempt to emulate natural light using 
        http://planetpixelemporium.com/tutorialpages/light.html as a reference.
        """
        sun = (255, 255, 251)
        sky = (64, 156, 255)
        self._smooth_transition(sun, sky)
    
    def warm(self):
        """
        Trying to emulate warm daytime colors
        """        
        orangey = ()


if __name__=="__main__":
    routine = sys.argv[1][2:]
    w = LEDWarlock()
    func = getattr(w, routine)
    func()
    """
    import alsaaudio, time, audioop

    # Open the device in nonblocking capture mode. The last argument could
    # just as well have been zero for blocking mode. Then we could have
    # left out the sleep call in the bottom of the loop
    inp = alsaaudio.PCM(alsaaudio.PCM_CAPTURE,alsaaudio.PCM_NONBLOCK)

    # Set attributes: Mono, 8000 Hz, 16 bit little endian samples
    inp.setchannels(1)
    inp.setrate(8000)
    inp.setformat(alsaaudio.PCM_FORMAT_S16_LE)

    # The period size controls the internal number of frames per period.
    # The significance of this parameter is documented in the ALSA api.
    # For our purposes, it is suficcient to know that reads from the device
    # will return this many frames. Each frame being 2 bytes long.
    # This means that the reads below will return either 320 bytes of data
    # or 0 bytes of data. The latter is possible because we are in nonblocking
    # mode.
    inp.setperiodsize(160)
    MINSOUND = 100
    MAXSOUND = 2130

    client = LEDClient()
    client.set_color_basis('hsv')

    counter = 0;
    max_counter = 15

    H = 0
    S = 255
    volsum = 0
    step = 0
    while True:
        # Read data from device
        l,data = inp.read()
        if l:
            # Return the maximum of the absolute value of all samples in a fragment.
            m=audioop.max(data, 2)
            volsum +=m;
            counter += 1

            if counter < max_counter:
                print "vol:", m
            else:
                counter = 0
                v = ((volsum/max_counter - MINSOUND) * 255)/MAXSOUND;
                if v > 255:
                    v = 255
                if v < 0:
                    v = 0
                print m,v
                # H += 1
                # H %= 256
                # client.fill(H,S,v)
                for i in range(9):
                    client.set(i, (H + step + 28.333 * i) % 255, S, v)
                client.update()
                volsum = 0
                step += max_counter
                step %= 255
            time.sleep(.001)
    """
