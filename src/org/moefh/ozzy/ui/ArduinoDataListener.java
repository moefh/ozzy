package org.moefh.ozzy.ui;

import javax.swing.SwingUtilities;

import org.moefh.ozzy.serial.ArduinoConnection;
import org.moefh.ozzy.serial.DataListener;

class ArduinoDataListener implements DataListener {
  
  private MainWindow window;
  
  public ArduinoDataListener(MainWindow window) {
    this.window = window;
  }
  
  @Override
  public void init(final long id) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (window.getConnId() != id) return;
        window.setStatusMessage("Waiting for handshake...");
      }
    });
  }
  
  @Override
  public void status(final long id, final ArduinoConnection.Status status) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (window.getConnId() != id) return;
        window.setStatusMessage(status.toString());
      }
    });
  }

  @Override
  public void received(final long id, final ArduinoConnection.Status status, String line) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (window.getConnId() != id) return;
        window.addCommData(line);
      }
    });
  }
  
}