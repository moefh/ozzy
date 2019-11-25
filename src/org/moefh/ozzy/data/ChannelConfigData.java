package org.moefh.ozzy.data;

public class ChannelConfigData {

  public enum Mode {
    NONE,
    DIGITAL,
    ANALOG,
  };
  
  private Mode mode = Mode.NONE;
  private int pin = 0;

  public Mode getMode() {
    return mode;
  }
  
  public void setMode(Mode mode) {
    this.mode = mode;
  }
  
  public int getPin() {
    return pin;
  }
  
  public void setPin(int pin) {
    this.pin = pin;
  }
  
}
