package org.moefh.ozzy.serial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoConnection {
  
  private static final int OPEN_DELAY_MS = 1000;
  private static final long KEEPALIVE_PERIOD = 4000;
  
  public enum Status {
    SETUP,
    READY,
    RECEIVING,
    DISCONNECTED,
  };

  private Object lock = new Object();
  private AtomicLong conn_id = new AtomicLong(0);
  private long last_keepalive = -1;
  private String config_cmd = null;
  
  private DataListener listener;
  private SerialPort port;
  private CommReader reader;
  private CommWriter writer;
  private Thread reader_thread;
  private Thread writer_thread;
  private Status status;

  public static List<String> getSerialPorts() {
    List<String> list = new ArrayList<>();
    for (SerialPort p : SerialPort.getCommPorts()) {
      list.add(p.getSystemPortName());
    }
    return list;
  }
  
  public ArduinoConnection(DataListener listener) {
    this.listener = listener;
  }
  
  private long getTime() {
    return System.currentTimeMillis();
  }
  
  public long getId() {
    return conn_id.get();
  }
  
  public Status getStatus() {
    synchronized (lock) {
      return status;
    }
  }
  
  public void setStatus(Status status) {
    synchronized (lock) {
      this.status = status;
    }
  }
  
  public String getConfigCommand() {
    synchronized (lock) {
      return config_cmd;
    }
  }
  
  public void setConfigCommand(String config_cmd) {
    synchronized (lock) {
      this.config_cmd = config_cmd;
    }
    
    Status cur_status = getStatus();
    if (cur_status == Status.READY || cur_status == Status.RECEIVING && config_cmd != null) {
      last_keepalive = getTime();
      writer.addOutput(config_cmd);
    } else if (cur_status == Status.RECEIVING && config_cmd == null) {
      last_keepalive = getTime();
      writer.addOutput("S\r\n");
    }
  }

  public boolean open(String port_name, int speed) {
    close();

    synchronized (lock) {
      conn_id.incrementAndGet();
    }

    listener.init(conn_id.get());

    synchronized (lock) {
      port = SerialPort.getCommPort(port_name);
      if (! port.openPort(OPEN_DELAY_MS)) {
        port = null;
        return false;
      }
      port.setComPortParameters(speed, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY, false);
      port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING|SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
      init();
      return true;
    }
  }
  
  private void init() {
    status = Status.SETUP;
    
    reader = new CommReader(port.getInputStream(), conn_id.get()) {
      @Override protected void lineReceived(String line) { processLine(saved_id, line); }
      @Override protected void disconnected() { handleDisconnect(saved_id); }
    };
    reader_thread = new Thread(this.reader);
    reader_thread.start();

    writer = new CommWriter(port.getOutputStream());
    writer_thread = new Thread(this.writer);
    writer_thread.start();
  }
  
  public void close() {
    synchronized (lock) {
      if (port != null) {
        reader.close();
        writer.close();
        port.closePort();
        reader_thread = null;
        reader = null;
        port = null;
      }
    }
  }
  
  public void writeString(String str) {
    writer.addOutput(str);
  }

  private void handleDisconnect(long id) {
    setStatus(Status.DISCONNECTED);
    listener.status(id, Status.DISCONNECTED);
  }

  private void handleKeepalive() {
    if (status != Status.RECEIVING) return;
    
    long cur_time = getTime();
    if (cur_time > last_keepalive + KEEPALIVE_PERIOD) {
      writer.addOutput("K\r\n");
      last_keepalive = cur_time;
    }
  }
  
  private void processLine(long id, String line) {
    listener.received(id, getStatus(), line);

    handleKeepalive();
    
    if (line.equals("Aready\r\n")) {
      String cur_config_cmd = getConfigCommand();
      if (cur_config_cmd != null) {
        writer.addOutput(cur_config_cmd);
        last_keepalive = getTime();
      }
      setStatus(Status.READY);
      listener.status(id, Status.READY);
      return;
    }

    if (line.equals("Srunning\r\n")) {
      setStatus(Status.RECEIVING);
      listener.status(id, Status.RECEIVING);
      return;
    }
  }

}
