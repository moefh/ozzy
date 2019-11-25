package org.moefh.ozzy.ui;

class PinNames {

  public static class Pin {
    public int num;
    public String name;
    
    private Pin(int num, String name) {
      this.num = num;
      this.name = name;
    }
  }
  
  public static final Pin[] DIGITAL_PINS = {
      new Pin( 2, "2"),
      new Pin( 3, "3"),
      new Pin( 4, "4"),
      new Pin( 5, "5"),
      new Pin( 6, "6"),
      new Pin( 7, "7"),
      new Pin( 8, "8"),
      new Pin( 9, "9"),
      new Pin(10, "10"),
      new Pin(11, "11"),
      new Pin(12, "12"),
      new Pin(13, "13"),
      new Pin(14, "14"),
      new Pin(15, "15"),
      new Pin(16, "16"),
      new Pin(17, "17"),
      new Pin(18, "18"),
      new Pin(19, "19"),
  };

  public static final Pin[] ANALOG_PINS = {
      new Pin(0, "A0"),
      new Pin(1, "A1"),
      new Pin(2, "A2"),
      new Pin(3, "A3"),
      new Pin(4, "A4"),
      new Pin(5, "A5"),
  };

  public static int getPinIndexByNumber(Pin[] pins, int num) {
    for (int index = 0; index < pins.length; index++) {
      if (pins[index].num == num) {
        return index;
      }
    }
    return -1;
  }
  
}
