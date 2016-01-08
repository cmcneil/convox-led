import socket
import convox_led_pb2 as ledbuf
from time import sleep
from random import randint

UDP_IP = "192.168.1.124"
UDP_PORT = 666

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

class LightFrame:
    def __init__(self, colors=((255, 255, 255)), period=200000, transition_steps=200000, circle_compression=1):
        self._lights = ledbuf.ConvoxLightConfig()
        self._lights.period = period
        self._lights.transition_steps = transition_steps
        self._lights.circle_compression = circle_compression
        self.set_colors(colors)

    def add_color(self, coords, rgb=True):
        self._lights.colors.add()
        self._lights.colors[-1].color_space = int(rgb)
        [self._lights.colors[-1].coordinates.append(i) for i in coords]

    def set_colors(self, colors, rgb=True):
        [self.add_color(coords, rgb=rgb) for coords in colors]

    def __str__(self):
        return self._lights.SerializeToString()


class Controller:
    default_colors = [(0, 255, 0) for _ in range(9)]
    
    def __init__(self, mode):
        self.lights = LightFrame(self.default_colors)
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

        if mode == 'disco':
            for _ in range(1000):
                colors = [(randint(0, 255), randint(0, 255), randint(0, 255)) for _ in range(9)]
                self.send_frame(colors)
                sleep(0.3)
        else:
            self.send_frame(self.default_colors)

    def send_frame(self, colors):
        self.lights = LightFrame(colors)
        self.sock.sendto(str(self.lights), (UDP_IP, UDP_PORT))


if __name__ == '__main__':
    c = Controller('disco')
