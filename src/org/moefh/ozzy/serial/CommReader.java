package org.moefh.ozzy.serial;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

abstract class CommReader implements Runnable {

  protected Object lock = new Object();
  protected Exception error;

  protected byte[] buf = new byte[1024];
  protected int buf_len = 0;
  protected InputStream stream;
  protected long saved_id;
  
  public CommReader(InputStream stream, long conn_id) {
    this.stream = stream;
    this.saved_id = conn_id;
  }
  
  public Exception getError() {
    synchronized(lock) {
      return error;
    }
  }
  
  @Override
  public void run() {
    try {
      while (true) {
        if (buf.length == buf_len) {
          System.out.printf("WARNING: line too long, breaking\n");
          lineReceived(new String(buf, StandardCharsets.UTF_8));
          buf_len = 0;
        }
        int n = stream.read(buf, buf_len, buf.length - buf_len);
        if (n < 0) {
          break;
        }
        if (n > 0) {
          buf_len += n;
          splitLines();
        }
      }
    } catch (Exception e) {
      synchronized (lock) {
        error = e;
      }
    }
    
    disconnected();
  }

  protected void splitLines() {
    int start = 0;
    for (int i = start; i < buf_len; i++) {
      if (buf[i] == '\n') {
        String line = new String(buf, start, i + 1 - start, StandardCharsets.UTF_8);
        start = i + 1;
        lineReceived(line);
      }
    }
    if (start > 0) {
      if (start < buf_len) {
        System.arraycopy(buf, start, buf, 0, buf_len - start);
        buf_len -= start;
      } else {
        buf_len = 0;
      }
    }
  }

  public void close() {
    try {
      stream.close();
    } catch (Exception e) {
      // ignore
    }
  }

  protected abstract void lineReceived(String line);
  protected abstract void disconnected();

}
