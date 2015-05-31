package main

import (
	"fmt"
	"math"
	"time"
)

// This file contains LightMachine, which is the state machine
// that handles config processing. Given a config, we can generate
// a LightMachine, which tracks the state of the light progression, and can
// transform that state into an appropriate buffer.
type LightMachine struct {
	//offset float64 // Offset of the first light projected onto the color wheel.
	delta  float64   // Space between two lights projected on the color wheel.
	period uint32    // Time it takes for a light to change colors(ms)
	nsteps uint32    // Number of distinct frames it takes a light to change colors.
	colors [][3]byte // Colors in the wheel.
	t0     time.Time // Time of initialization. TODO(cmcneil): worry about floating point
	// precision later
}

func machineFromConfig(config *ConvoxLightConfig) *LightMachine {
	lm := new(LightMachine)
	colors := config.GetColors()

	lm.delta = float64(len(colors)) * float64(config.GetCircleCompression()) /
		float64(nglobes)
	//lm.offset = 0.0
	lm.period = config.GetPeriod()
	lm.nsteps = config.GetTransitionSteps()
	lm.t0 = time.Now()

	for _, color := range colors {
		lm.colors = append(lm.colors, colorToBytes(color))
	}
	// Add the first color on the end again, so the modular arithmetic works out.
	lm.colors = append(lm.colors, lm.colors[0])
	return lm
}

func testMachine() *LightMachine {
	lm := new(LightMachine)
	lm.delta = 0.5
	lm.nsteps = 200
	lm.t0 = time.Now()
	lm.period = 2000
	lm.colors = append(lm.colors, [3]byte{150, 150, 150})
	lm.colors = append(lm.colors, [3]byte{250, 50, 0})
	lm.colors = append(lm.colors, [3]byte{0, 0, 0})
	lm.colors = append(lm.colors, [3]byte{0, 200, 0})
	lm.colors = append(lm.colors, [3]byte{150, 150, 150})
	return lm
}

func (lm *LightMachine) GetBuffer() []byte {
	tdiff := time.Since(lm.t0).Nanoseconds() / 1000000
	o := math.Trunc(float64(tdiff) / float64(lm.period) * float64(lm.nsteps))
	offset := o / float64(lm.nsteps)
	buf := make([]byte, nleds*3)
	for i := 0; i < nglobes; i++ {
		osGlobe := math.Mod(offset+lm.delta*float64(i),
			float64(len(lm.colors)-1))
		// fmt.Println("The globe offset: ", osGlobe)
		// fmt.Println("Number of colors: ", len(lm.colors))
		colGlobe := colorInterp(lm.colors[int(math.Floor(osGlobe))],
			lm.colors[int(math.Ceil(osGlobe))],
			math.Mod(osGlobe, 1.0))
		for j := 0; j < nledsPerGlobe; j++ {
			for k := 0; k < 3; k++ {
				buf[i*nledsPerGlobe*3+j*3+k] = gamma[colGlobe[k]]
			}
		}
	}
	return buf
}

// Math util functions.
func colorToBytes(c *ConvoxLightConfig_Color) [3]byte {
	coords := c.GetCoordinates()
	space := c.GetColorSpace()
	if len(coords) != 3 {
		fmt.Print("Malformed Packet!")
		return [3]byte{0, 0, 0}
	}
	var r, g, b uint32
	switch space {
	case ConvoxLightConfig_Color_HSV:
		rgb := hsvToRgb([3]uint32{coords[0], coords[1], coords[2]})
		r, g, b = rgb[0], rgb[1], rgb[2]
	default:
		r, g, b = coords[0], coords[1], coords[2]

	}
	return [3]byte{byte(r), byte(g), byte(b)}
}

// Interpolate between two colors, given the first color c1,
// the second color, c2, and how far you are between them on a scale of 0-1.
func colorInterp(c1 [3]byte, c2 [3]byte, t float64) [3]byte {
	var newColor [3]byte
	for i := range c1 {
		newColor[i] = byte(float64(c1[i]) + (float64(c2[i])-float64(c1[i]))*t)
	}
	return newColor
}

func hsvToRgb(hsv [3]uint32) [3]uint32 {
	h, s, v := float64(hsv[0]), float64(hsv[1]), float64(hsv[2])
	c := s * v / 65536.0
	h0 := float64(6.0 * h / 256.0)
	x := c * float64(1.0-math.Abs(math.Mod(h0, 2.0)-1.0))
	var r, g, b float64
	if h0 < 1 {
		r, g, b = c, x, 0
	} else if h0 < 2 {
		r, g, b = x, c, 0
	} else if h0 < 3 {
		r, g, b = 0, c, x
	} else if h0 < 4 {
		r, g, b = 0, x, c
	} else if h0 < 5 {
		r, g, b = x, 0, c
	} else {
		r, g, b = c, 0, x
	}
	m := (v / 256.0) - c
	r += m
	g += m
	b += m
	return [3]uint32{uint32(255 * r), uint32(255 * g), uint32(255 * b)}
}
