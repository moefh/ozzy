package org.moefh.ozzy.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.moefh.ozzy.data.ChannelConfigData;

class ChannelConfigPanel {
  
  private int index;
  private JComboBox<String> mode_combo;
  private JComboBox<String> pin_combo;
  private ChannelConfigData.Mode has_pins_for_mode;
  
  ChannelConfigPanel(int index, JComboBox<String> mode_combo, JComboBox<String> pin_combo) {
    this.index = index;
    this.mode_combo = mode_combo;
    this.pin_combo = pin_combo;
    
    this.mode_combo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent action) {
        ChannelConfigData.Mode mode = getSelectedMode();
        if (has_pins_for_mode != mode) {
          setPinComboOptions(mode);
          has_pins_for_mode = mode;
        }
      }
    });

    setPinComboOptions(ChannelConfigData.Mode.NONE);
    has_pins_for_mode = ChannelConfigData.Mode.NONE;
  }
  
  JComboBox<String> buildComboBox(int width) {
    JComboBox<String> combo = new JComboBox<String>();
    
    Dimension size = new Dimension(combo.getMaximumSize());
    size.width = width;
    size.height = combo.getPreferredSize().height;
    combo.setMaximumSize(size);
    combo.setPreferredSize(size);
    combo.setMinimumSize(size);
    return combo;
  }
  
  public int getIndex() { return index; }
  public JComboBox<String> getModeCombo() { return mode_combo; }
  public JComboBox<String> getPinCombo() { return pin_combo; }

  private ChannelConfigData.Mode getModeFromComboIndex(int index) {
    switch (index) {
    case 0:  return ChannelConfigData.Mode.NONE;
    case 1:  return ChannelConfigData.Mode.DIGITAL;
    case 2:  return ChannelConfigData.Mode.ANALOG;
    default: return ChannelConfigData.Mode.NONE;
    }
  }
  
  private int getPinFromComboIndex(ChannelConfigData.Mode mode, int index) {
    switch (mode) {
    case NONE:    return 0;
    case DIGITAL: return PinNames.DIGITAL_PINS[index].num;
    case ANALOG:  return PinNames.ANALOG_PINS[index].num;
    }
    return 0;
  }
  
  private int getComboIndexForMode(ChannelConfigData.Mode mode) {
    switch (mode) {
    case NONE:    return 0;
    case DIGITAL: return 1;
    case ANALOG:  return 2;
    }
    return 0;
  }
  
  private int getComboIndexForPin(ChannelConfigData.Mode mode, int pin) {
    switch (mode) {
    case NONE:    return -1;
    case DIGITAL: return PinNames.getPinIndexByNumber(PinNames.DIGITAL_PINS, pin);
    case ANALOG:  return PinNames.getPinIndexByNumber(PinNames.ANALOG_PINS, pin);
    }
    return 0;
  }
  
  public ChannelConfigData.Mode getSelectedMode() {
    return getModeFromComboIndex(mode_combo.getSelectedIndex());
  }
  
  public int getSelectedPin() {
    return getPinFromComboIndex(getSelectedMode(), pin_combo.getSelectedIndex());
  }
  
  public void setSelection(ChannelConfigData.Mode mode, int pin) {
    mode_combo.setSelectedIndex(getComboIndexForMode(mode));
    if (has_pins_for_mode != mode) {
      setPinComboOptions(mode);
      has_pins_for_mode = mode;
    }
    pin_combo.setSelectedIndex(getComboIndexForPin(mode, pin));
  }
  
  private void setPinComboOptions(ChannelConfigData.Mode mode) {
    pin_combo.removeAllItems();
    switch (mode) {
    case NONE:
      pin_combo.setEnabled(false);
      return;
    
    case DIGITAL:
      pin_combo.setEnabled(true);
      for (PinNames.Pin pin : PinNames.DIGITAL_PINS) {
        pin_combo.addItem(pin.name);
      }
      return;
    
    case ANALOG:
      pin_combo.setEnabled(true);
      for (PinNames.Pin pin : PinNames.ANALOG_PINS) {
        pin_combo.addItem(pin.name);
      }
      return;
    }
  }

  /**
   * Update chan_config with the options set in the UI. 
   */
  public void updateChannelConfigData(ChannelConfigData chan_config_data) {
    chan_config_data.setMode(getSelectedMode());
    chan_config_data.setPin(getSelectedPin());
  }

}

