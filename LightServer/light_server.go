package main

import (
  "fmt"
  "github.com/golang/protobuf/proto"
  "net"
)

func startServer() {
  // Bind the port.
  ServerAddr, err := net.ResolveUDPAddr("udp", "localhost:666")
  if (err != nil) {
    fmt.Println("Error binding port!")
  }
  fmt.Println("Beginning Convox LightServer!")
  fmt.Println(ServerAddr)

  ServerConn, _ := net.ListenUDP("udp", ServerAddr)
  defer ServerConn.Close()

  buf := make([]byte, 1024)
  for {
    // Recieve a UDP packet and unmarshal it into a protobuf.
    n, _, _ := ServerConn.ReadFromUDP(buf)
    indata := new(ConvoxLightConfig)
    marshalerr := proto.Unmarshal(buf[0:n], indata)
    if (marshalerr != nil) {
      fmt.Print("Corrupt packet!")
      continue
    }

    /* Do stuff with indata. */
  }
}

func main() {
  startServer()
}
