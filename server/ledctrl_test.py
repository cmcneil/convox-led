#!/usr/bin/env python

import ledctrl as lc
from time import sleep

s = lc.Strand()
chain = lc.GlobeChain(s)

def test_fill():
    chain.fill(128, 0, 0)
    chain.update()
    sleep(2)
    chain.fill(0, 128, 0)
    chain.update()
    sleep(2)
    chain.fill(0, 128, 128)
    chain.update()
    sleep(2)
    chain.fill(0, 0, 0)
    chain.update()
    
def test_set():
    for i in range(chain.nglobes):
        chain.set(i, 0, 129, 129)
        chain.set((i - 1) % chain.nglobes, 0, 0, 0)
        chain.update()
        sleep(0.5)

def test_push():
    chain.push(128, 0, 0)
    chain.update()
    sleep(0.5)
    chain.push(0, 128, 0)
    chain.update()
    sleep(0.5)
    chain.push(0, 0, 128)
    chain.update()
    sleep(0.5)
    chain.push(128, 128, 0)
    chain.update()
    sleep(0.5)
    chain.push(0, 128, 128)
    chain.update()
    sleep(0.5)
    chain.push(128, 0, 128)
    chain.update()
    sleep(0.5)
    chain.push(128, 128, 128)
    chain.update()
    sleep(0.5)
    chain.push(0, 0, 0)
    chain.update()
    sleep(0.5)
    chain.push(128, 60, 60)
    chain.update()
    sleep(0.5)
    chain.push(60, 60, 128)
    chain.update()
    sleep(0.5)

def test_rotate():
    test_push()
    for i in range(chain.nglobes):
        chain.rotate()
        chain.update()
        sleep(0.5)

if __name__ == "__main__":
    test_fill()
    test_push()
    test_fill()
    test_set()
    test_rotate()
