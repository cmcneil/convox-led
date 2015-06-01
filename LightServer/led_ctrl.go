package main

import (
	"fmt"
	//"math"
	"os"
	"time"
)

const (
	spidev        string = "/dev/spidev0.0"
	nledsPerGlobe int    = 10
	nglobes       int    = 9
	nleds         int    = nglobes * nledsPerGlobe
)

// This thread is responsible for writing and propogating
// the buffer. It takes in configs, and stores two buffers,
// one which it's writing to, and one that is being flushed.
func LightManager() func(*ConvoxLightConfig) {
	confchan := make(chan *ConvoxLightConfig, 1)
	go processConfigs(confchan)

	return (func(indata *ConvoxLightConfig) {
		// If the channel is full, drop the packet.
		select {
		case confchan <- indata:
			fmt.Print("Sent data")
			return
		default:
			fmt.Print("dropped packet")
			return
		}
	})
}

// This thread is responsible for taking in a config. It owns a lightmachine,
// which tracks state. It gets buffers from the LightMachine struct, and
// dispatches them to the writer.
func processConfigs(confchan chan *ConvoxLightConfig) {
	//gamma := make([]byte, 256)
	//for i := range gamma {
	//  gamma[i] = 0x80 | byte(int(math.Pow(float64(i) / 255.0, 2.5) * 127.0 + 0.5))
	//}
	// Start the goroutine that writes the buffer:
	bufchan := make(chan []byte)
	go writer(bufchan)

	//buf := make([]byte, nleds)
	fmt.Print("processConfigs main loop")
	// Initialize light machine to do nothing.
	var lm *LightMachine = nil

	for {
		select {
		case config := <-confchan:
			// Reset and start using new config.
			// Make sure we write at least once:
			fmt.Print("New Config")
			lm = machineFromConfig(config)
			fmt.Println(config)
			fmt.Println(lm)
			buf := lm.GetBuffer()
			bufchan <- buf
		default:
			// Continue to propogate old config.
			if (lm != nil) {
				buf := lm.GetBuffer()
				bufchan <- buf
			}
		}

		time.Sleep(500 * time.Millisecond)
	}
}

// This thread is responsible for flushing the buffer to the lights.
func writer(buf chan []byte) {
	f, err := os.Create(spidev)
	if err != nil {
		fmt.Print(err)
	}
	defer f.Close()
	for {
		f.Write([]byte{0x00})
		writedata := <-buf
		/* Weird bugfix: Had to write 3 non-zero bytes to the end of the chain to
		   prevent the last led from getting messed up. Determined experimentally.
		*/
		writedata = append(writedata, 129, 129, 129, 0x00)
		f.Write(writedata)
		fmt.Println("Wrote data! Length: ", len(writedata))
	}
}
