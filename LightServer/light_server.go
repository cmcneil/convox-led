package main

import (
  "fmt"
  "github.com/golang/protobuf/proto"
  "net"
  "time"
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

  pushConf := LightManager()

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

    //fmt.Print(indata)
    pushConf(indata)
    /* Do stuff with indata. */
  }
}

func main() {
  // startServer()
  lm := testMachine()
  datachan := make(chan []byte)
  go writer(datachan)
  for {
    time.Sleep(100 * time.Millisecond)
    datachan<-lm.GetBuffer()
  }
}
