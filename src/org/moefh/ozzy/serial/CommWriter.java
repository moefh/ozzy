package org.moefh.ozzy.serial;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

class CommWriter implements Runnable {

  protected Object lock = new Object();
  protected List<String> output = new LinkedList<>();
  protected Exception error = null;
  protected boolean close_requested = false;
  
  protected OutputStream stream;
  
  public CommWriter(OutputStream stream) {
    this.stream = stream;
  }

  public Exception getError() {
    synchronized (lock) {
      return error;
    }
  }
  
  public void close() {
    try {
      synchronized (lock) {
        stream.close();
        close_requested = true;
        lock.notify();
      }
    } catch (Exception e) {
      // ignore
    }
  }
  
  public void addOutput(String out) {
    synchronized (lock) {
      output.add(out);
      lock.notify();
    }
  }
  
  private String readOutput() throws Exception {
    synchronized (lock) {
      while (output.size() == 0 && ! close_requested) {
        lock.wait();
      }
      if (close_requested) {
        return null;
      }
      return output.remove(0);
    }
  }
  
  @Override
  public void run() {
    try {
      while (true) {
        String data = readOutput();
        if (data == null) {
          break;
        }
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        stream.write(bytes);
      }
    } catch (Exception e) {
      synchronized (lock) {
        error = e;
      }
    }
  }

}
