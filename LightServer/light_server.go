package main

import (
	"fmt"
	"github.com/golang/protobuf/proto"
  "math"
	"net"
	"time"
)

var gamma []byte;

func startServer() {
	// Bind the port.
	ServerAddr, err := net.ResolveUDPAddr("udp", "localhost:666")
	if err != nil {
		fmt.Println("Error binding port!")
	}
	fmt.Println("Beginning Convox LightServer!")
	fmt.Println(ServerAddr)

	ServerConn, _ := net.ListenUDP("udp", ServerAddr)
	defer ServerConn.Close()

	pushConf := LightManager()

	buf := make([]byte, 1024)
	for {
		// Recieve a UDP packet and unmarshal it into a protobuf.
		n, _, _ := ServerConn.ReadFromUDP(buf)
		indata := new(ConvoxLightConfig)
		marshalerr := proto.Unmarshal(buf[0:n], indata)
		if marshalerr != nil {
			fmt.Print("Corrupt packet!")
			continue
		}

		//fmt.Print(indata)
		pushConf(indata)
		/* Do stuff with indata. */
	}
}

func main() {
	// startServer()
  gamma = make([]byte, 256)
  for i := range gamma {
    gamma[i] = 0x80 | byte(int(math.Pow(float64(i) / 255.0, 2.5) * 127.0 + 0.5))
  }

	lm := testMachine()
	datachan := make(chan []byte)
	go writer(datachan)
  fmt.Print(gamma)
	for {
		time.Sleep(1000 * time.Millisecond)
		buf := lm.GetBuffer()
		fmt.Print(buf)
		datachan <- lm.GetBuffer()
	}
}
