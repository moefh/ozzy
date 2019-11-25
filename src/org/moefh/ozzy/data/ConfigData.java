package org.moefh.ozzy.data;

public class ConfigData {

  private ChannelConfigData[] channels;

  public ConfigData(int num_channels) {
    channels = new ChannelConfigData[num_channels];
    for (int i = 0; i < num_channels; i++) {
      channels[i] = new ChannelConfigData();
    }
  }
  
  public int getNumChannels() {
    return channels.length;
  }
  
  public ChannelConfigData getChannel(int n) {
    return channels[n];
  }
  
  public String getArduinoConfigCommand() {
    StringBuilder sb = new StringBuilder();
    sb.append('G');
    int num_active_channels = 0;
    for (ChannelConfigData chan : channels) {
      switch (chan.getMode()) {
      case NONE:    break;
      case DIGITAL: sb.append(String.format("D%x", chan.getPin())); num_active_channels++; break;
      case ANALOG:  sb.append(String.format("A%x", chan.getPin())); num_active_channels++; break;
      }
    }
    if (num_active_channels == 0) {
      return null;
    }
    sb.append("\r\n");
    return sb.toString();
  }
  
  public void dump() {
    for (int i = 0; i < channels.length; i++) {
      ChannelConfigData chan = channels[i];
      System.out.printf("chan[%d]: %s %d\n", i, chan.getMode(), chan.getPin());
    }
  }

}
