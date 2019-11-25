package org.moefh.ozzy.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.moefh.ozzy.data.GraphData;

@SuppressWarnings("serial")
class GraphPanel extends JPanel {
  
  public static Color BG_ACTIVE   = new Color(0x00, 0x00, 0x20);
  public static Color BG_INACTIVE = new Color(0x00, 0x20, 0x20);
  public static Color[] LINES = {
      new Color(0xff, 0xff, 0x00),
      new Color(0x00, 0xff, 0xff),
      new Color(0x80, 0xff, 0x00),
      new Color(0xff, 0xff, 0xff),
  };

  private GraphData data;
  private int[] times;
  private int[][] values;
  private boolean enabled_channels[];

  public GraphPanel(GraphData data) {
    this.data = data;
    this.times = new int[4096];
    this.values = new int[data.getNumChannels()][];
    for (int chan = 0; chan < this.values.length; chan++) {
      this.values[chan] = new int[4096];
    }
    this.enabled_channels = new boolean[data.getNumChannels()];
  }

  public void setEnabledChannels(boolean... enabled) {
    for (int i = 0; i < enabled.length; i++) {
      enabled_channels[i] = enabled[i];
    }
    for (int i = enabled.length; i < data.getNumChannels(); i++) {
      enabled_channels[i] = false;
    }
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D gr = (Graphics2D) g;
      
    int width  = getWidth();
    int height = getHeight();

    gr.setColor(BG_ACTIVE);
    gr.fillRect(0, 0, width, height);
    
    if (data != null) {
      drawData(gr, width, height);
    }
  }
  
  private void drawData(Graphics2D gr, int width, int height) {
    int x_scale = 5;
    int v_border = height / 20;
    int n = width/x_scale+1;
    synchronized (this) {
      n = data.getSamples(-n, n, times, values);
    }
    
    gr.setColor(BG_INACTIVE);
    gr.fillRect(0, 0, width-1, v_border-1);
    gr.fillRect(0, height-v_border+1, width-1, height-1);
    
    //System.out.printf("drawing %d/%d samples\n", n, data.size());
    for (int chan = values.length-1; chan >= 0; chan--) {
      if (! enabled_channels[chan]) continue;
      gr.setColor(LINES[chan % LINES.length]);
      int x = width - n*x_scale;
      for (int i = 0; i < n-1; i++) {
        int x1 = x + x_scale * i;
        int x2 = x + x_scale * (i+1);
        int y1 = height - v_border - values[chan][i  ] * (height - 2*v_border) / 1023;
        int y2 = height - v_border - values[chan][i+1] * (height - 2*v_border) / 1023;
        //System.out.printf("[%d]=%d (%d,%d)-(%d,%d)\n", i, values[i], x1, y1, x2, y2);
        gr.drawLine(x1, y1, x2, y2);
      }
    }
  }

  
}
