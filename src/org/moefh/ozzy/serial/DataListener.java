package org.moefh.ozzy.serial;

public interface DataListener {
  void init(long id);
  void status(long id, ArduinoConnection.Status status);
  void received(long id, ArduinoConnection.Status status, String line);
}
