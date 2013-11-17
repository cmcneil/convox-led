#!/usr/bin/env python

import math
from select import select
import socket as sck
import struct
import ledctrl

NUM_LEDS = 90
NGLOBES = 9

def hsv_to_rgb(H, S, V):
    C = S * V / 65536.0
    H0 = 6.0 * H / 256.0
    X = C * (1 - abs((H0 % 2) - 1))
    if H0 < 1:
        r, g, b = C, X, 0
    elif H0 < 2:
        r, g, b = X, C, 0
    elif H0 < 3:
        r, g, b = 0, C, X
    elif H0 < 4:
        r, g, b = 0, X, C
    elif H0 < 5:
        r, g, b = X, 0, C
    else:
        r, g, b = C, 0, X
    m = V - C
    r += m
    g += m
    b += m
    return int(255 * r), int(255 * g), int(255 * b)

# Start the server.
strand = ledctrl.Strand(NUM_LEDS)
leds = ledctrl.GlobeChain(strand, NGLOBES)

server = sck.socket()
server.bind(('', 666))
server.listen(5)

inputs = [server]

while(1):
    r, s, e = select(inputs, [], [])

    for ss in r:
        # Accept another client
        if ss == server:
            inputs.append(client)
        else:
            try:
                header = ss.recv(1)
                if not header:
                    raise sck.error
                header = struct.unpack('!B', header)[0]
                # Update
                if header & 0x80:
                    leds.update()
                    break
                # Rotate (reverse bit controls direction)
                if header & 0x04:
                    leds.rotate(header & 0x08)
                    break
                # Set, requires index bytes
                if header & 0x20:
                    index = ss.recv(2)
                    if not index:
                        raise sck.error
                    index = struct.unpack('!H', index)[0]
                data = ss.recv(3)
                if not data:
                    raise sck.error
                color = struct.unpack('!BBB', data)
                # HSV color
                if header & 0x01:
                    r, g, b = hsv_to_rgb(color[0], color[1], color[2])
                # RGB color
                else:
                    r = color[0]
                    g = color[1]
                    b = color[2]
                # Set
                if header & 0x20:
                    leds.set(index, r, g, b)
                # Fill
                elif header & 0x10:
                    leds.fill(r, g, b)
                # Push
                elif header & 0x40:
                    # Push from opposite end
                    if header & 0x08:
                        leds.ipush(r, g, b)
                    # Normal push
                    else:
                        leds.push(r, g, b)
            except sck.error, e:
                inputs.remove(s)

server.close()

