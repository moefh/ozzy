# Ozzy

A *really* crummy oscilloscope built using an Arduino.

## Example Usage

Measuring the voltage across the capacitor in a circuit for a [555 timer IC in astable mode](https://en.wikipedia.org/wiki/555_timer_IC#Astable).

![Screenshot of graph tab](/screenshots/graph.png)

Photo of the circuit, showing the capacitor being measured (in the center, to the left of the 555). The blue wire is connecting the positive terminal of the capacitor to input A0 of the Arduino:

![Circuit photo](/screenshots/circuit.jpg)

Config tab showing the pin A0 selected as analog input:

![Screenshot of options tab](/screenshots/options.png)

## Code

This project contains two programs: the Java UI and the Arduino reader.

### Java Program

The Java program connects to the Arduino via serial port (USB) and sends the configuration of which pins to read. The measurements are displayed in the graph window.

The serial connection is done with the [jSerialComm library](https://github.com/Fazecast/jSerialComm).

### Arduino Code

The Arduino program receives the configuration options (i.e., which pins to read) and sends the measured values periodically via serial port.

## Limitations

Due to [limitations in the Arduino design](https://forum.arduino.cc/index.php?topic=54976.msg428738#msg428738), selecting two analog inputs doesn't work very well. It should be possible to work around this by lowering the sampling rate when two analog inputs are selected.

The graph doesn't work correctly if the selected channels are not consecutive (that is, if a channel selected as "None" is above a channel selected as "Analog" or "Digital"). I'm too lazy to fix this (maybe some day).
