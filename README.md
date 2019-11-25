# Ozzy

Voltage meter built with Arduino

## Demo

Measuring the voltage across the capacitor in a circuit for a [555 timer IC in astable mode](https://en.wikipedia.org/wiki/555_timer_IC#Astable).

![Screenshot](/screenshots/graph.png)

Options tab:

![Screenshot](/screenshots/options.png)

## Code

This project contains two programs: the Java UI and the Arduino reader.

## Java Program

The Java program connects to the Arduino via serial port (USB) and sends the configuration of which pins to read. The measurements are displayed in the graph window.

The serial connection is done with the [jSerialComm library](https://github.com/Fazecast/jSerialComm).

## Arduino Code

The Arduino program receives the configuration option (i.e., which pins to read) and sends the measured values via serial port to the Java program.
