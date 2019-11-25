package org.moefh.ozzy.data;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class GraphData {

  private int[] times;
  private int[][] vals;
  private int start;
  private int size;
  private int read_buf[];
  
  public GraphData(int num_channels, int num_samples) {
    times  = new int[num_samples];
    vals = new int[num_channels][];
    for (int chan = 0; chan < num_channels; chan++) {
      vals[chan] = new int[num_samples];
    }
    read_buf = new int[num_channels];
    start = 0;
    size = 0;
    
    //fillDummyData();
  }
  
  protected void fillDummyData() {
    int[] v = new int[getNumChannels()];
    for (int i = 0; i < 2000; i++) {
      for (int chan = 0; chan < vals.length; chan++) {
        v[chan] = (int) (1023 * (0.5 + 0.5*Math.sin(chan * Math.PI / vals.length + i * Math.PI / 20)));
      }
      add(i, v);
    }
  }
  
  public int getNumChannels() {
    return vals.length;
  }
  
  private int getSampleIndex(int sample) {
    if (sample < 0) {
      // count backwards from end, -1=last sample
      sample = size + sample;
      if (sample < 0) {
        sample = 0;
      }
    }
    if (sample >= size) {
      sample = size - 1;
    }
    
    return (start + sample) % times.length;
  }
  
  public int start() {
    return start;
  }
  
  public int size() {
    return size;
  }
  
  public void clear() {
    size = 0;
  }
  
  public int getSampleTime(int sample) {
    synchronized (this) {
      return times[getSampleIndex(sample)];
    }
  }

  public int getSampleValue(int chan, int sample) {
    synchronized (this) {
      return vals[chan][getSampleIndex(sample)];
    }
  }
  
  public int getSamples(int sample, int num, int[] t, int[][] v) {
    synchronized (this) {
      if (num > size) num = size;
      if (num <= 0) return 0;
      int index = getSampleIndex(sample);
      int end = (index + num) % times.length;
      if (index < end) {
        System.arraycopy(times,  index, t, 0, num);
        for (int chan = 0; chan < v.length; chan++) {
          System.arraycopy(vals[chan], index, v[chan], 0, num);
        }
      } else {
        System.arraycopy(times,      index, t, 0,                  times.length-index);
        System.arraycopy(times,      0,     t, times.length-index, num - (times.length-index));
        for (int chan = 0; chan < v.length; chan++) {
          System.arraycopy(vals[chan], index, v[chan], 0,                  times.length-index);
          System.arraycopy(vals[chan], 0,     v[chan], times.length-index, num - (times.length-index));
        }
      }
      return num;
    }
  }

  public void add(int time, int... v) {
    synchronized (this) {
      int index = (start + size) % times.length;
      times[index]  = time;
      for (int chan = 0; chan < v.length; chan++) {
        vals[chan][index] = v[chan];
      }
      if (size < times.length) {
        size++;
      } else {
        start = (start + 1) % times.length;
      } 
    }
  }
  
  private int readHex(ByteArrayInputStream in) {
    int num = 0;
    while (true) {
      in.mark(1);
      int c = in.read();
      if (c < 0) {
        return num;
      }
      if (c >= (int)'0' && c <= (int)'9') {
        num = (num<<4) + (c-(int)'0');
      } else if (c >= (int)'a' && c <= (int)'f') {
        num = (num<<4) + (c-(int)'a'+10);
      } else {
        in.reset();
        break;
      }
    }
    return num;
  }
  
  private boolean addSingleNumberValue(String s) {
    s = s.trim();
    for (char c : s.toCharArray()) {
      if (c < '0' || c > '9') {
        return false;
      }
    }
    
    try {
      add((int) (System.currentTimeMillis() & 0x7fffffff), Integer.parseInt(s), 0, 0, 0);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
  
  public boolean add(String line) {
    if (addSingleNumberValue(line)) { // treat single number as value
      return true;
    }

    byte[] data = line.getBytes(StandardCharsets.UTF_8);
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    int type = in.read();
    if (type != 'V') return false;
    int time = readHex(in);
    for (int chan = 0; chan < read_buf.length; chan++) {
      type = in.read();
      if (type < 0) break;
      int val = readHex(in);
      read_buf[chan] = (type == 'D') ? val * 1023 : val;
    }
    
    add(time, read_buf);
    return true;
  }
  
}
