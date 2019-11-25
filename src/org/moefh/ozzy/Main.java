package org.moefh.ozzy;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.moefh.ozzy.ui.MainWindow;

public class Main {

  public static void main(String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          MainWindow win = new MainWindow();
          win.setVisible(true);
        } catch (Exception e) {
          System.out.printf("UNHANDLED EXCEPTION: %s\n", e.getMessage());
          e.printStackTrace(System.out);
        }
      }
    });
  }
  
}
