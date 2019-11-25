package org.moefh.ozzy.ui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.moefh.ozzy.data.ChannelConfigData;
import org.moefh.ozzy.data.ConfigData;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.Component;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
class ConfigPanel extends JPanel {

  private ChannelConfigPanel[] channels;
  private List<ConfigUpdateListener> config_update_listeners = new ArrayList<>();
  
  public ConfigPanel(int num_channels) {
    JPanel chan_panel = new JPanel();
    chan_panel.setAlignmentX(LEFT_ALIGNMENT);
    GridBagLayout gbl_chan_panel = new GridBagLayout();
    gbl_chan_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
    gbl_chan_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    gbl_chan_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    gbl_chan_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    chan_panel.setLayout(gbl_chan_panel);

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(chan_panel);
    
    Component top_strut = Box.createVerticalStrut(20);
    GridBagConstraints gbc_top_strut = new GridBagConstraints();
    gbc_top_strut.insets = new Insets(0, 0, 5, 5);
    gbc_top_strut.gridx = 2;
    gbc_top_strut.gridy = 0;
    chan_panel.add(top_strut, gbc_top_strut);
    
    Component left_strut = Box.createHorizontalStrut(20);
    GridBagConstraints gbc_left_strut = new GridBagConstraints();
    gbc_left_strut.insets = new Insets(0, 0, 5, 5);
    gbc_left_strut.gridx = 0;
    gbc_left_strut.gridy = 1;
    chan_panel.add(left_strut, gbc_left_strut);
    GridBagConstraints gbc_label_mode = new GridBagConstraints();
    gbc_label_mode.insets = new Insets(0, 0, 5, 5);
    gbc_label_mode.anchor = GridBagConstraints.WEST;
    gbc_label_mode.gridx = 2;
    gbc_label_mode.gridy = 1;
    JLabel label_mode = new JLabel("Mode");
    chan_panel.add(label_mode, gbc_label_mode);

    GridBagConstraints gbc_label_pin = new GridBagConstraints();
    gbc_label_pin.insets = new Insets(0, 0, 5, 0);
    gbc_label_pin.anchor = GridBagConstraints.WEST;
    gbc_label_pin.gridx = 3;
    gbc_label_pin.gridy = 1;
    JLabel label_poin = new JLabel("Pin");
    chan_panel.add(label_poin, gbc_label_pin);
    
    channels = new ChannelConfigPanel[num_channels];
    for (int i = 0; i < num_channels; i++) {
      channels[i] = buildChannel(chan_panel, i);
    }
    
    Component btn_sep_strut = Box.createVerticalStrut(20);
    GridBagConstraints gbc_btn_sep_strut = new GridBagConstraints();
    gbc_btn_sep_strut.insets = new Insets(0, 0, 5, 5);
    gbc_btn_sep_strut.gridx = 1;
    gbc_btn_sep_strut.gridy = 3 + num_channels;
    chan_panel.add(btn_sep_strut, gbc_btn_sep_strut);

    JButton button = new JButton("Update");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        notifyConfigUpdateListeners(ev);
      }
    });
    GridBagConstraints gbc_button = new GridBagConstraints();
    gbc_button.anchor = GridBagConstraints.WEST;
    gbc_button.gridwidth = 2;
    gbc_button.insets = new Insets(0, 0, 0, 5);
    gbc_button.fill = GridBagConstraints.VERTICAL;
    gbc_button.gridx = 1;
    gbc_button.gridy = 4 + num_channels;
    chan_panel.add(button, gbc_button);
    button.setAlignmentX(LEFT_ALIGNMENT);

    setDefaultSettings();
  }
  
  private ChannelConfigPanel buildChannel(JPanel chan_panel, int chan_index) {
    JLabel label_channel = new JLabel("Channel " + (chan_index+1) + ":");
    GridBagConstraints gbc_label_channel = new GridBagConstraints();
    gbc_label_channel.anchor = GridBagConstraints.EAST;
    gbc_label_channel.insets = new Insets(0, 0, 5, 5);
    gbc_label_channel.gridx = 1;
    gbc_label_channel.gridy = 2 + chan_index;
    chan_panel.add(label_channel, gbc_label_channel);
    
    JComboBox<String> combo_mode = new JComboBox<>();
    combo_mode.setModel(new DefaultComboBoxModel<>(new String[] {"None", "Digital", "Analog"}));
    GridBagConstraints gbc_combo_mode = new GridBagConstraints();
    gbc_combo_mode.fill = GridBagConstraints.HORIZONTAL;
    gbc_combo_mode.insets = new Insets(0, 0, 5, 5);
    gbc_combo_mode.gridx = 2;
    gbc_combo_mode.gridy = 2 + chan_index;
    chan_panel.add(combo_mode, gbc_combo_mode);
    
    JComboBox<String> combo_pin = new JComboBox<>();
    GridBagConstraints gbc_combo_pin = new GridBagConstraints();
    gbc_combo_pin.fill = GridBagConstraints.HORIZONTAL;
    gbc_combo_pin.insets = new Insets(0, 0, 5, 0);
    gbc_combo_pin.gridx = 3;
    gbc_combo_pin.gridy = 2 + chan_index;
    chan_panel.add(combo_pin, gbc_combo_pin);

    return new ChannelConfigPanel(chan_index, combo_mode, combo_pin);
  }
  
  public void addUpdateListener(ConfigUpdateListener listener) {
    config_update_listeners.add(listener);
  }
  
  protected void notifyConfigUpdateListeners(ActionEvent ev) {
    for (ConfigUpdateListener l : config_update_listeners) {
      l.update();
    }
  }

  protected void setDefaultSettings() {
    if (channels.length == 0) return;
    
    channels[0].setSelection(ChannelConfigData.Mode.ANALOG, 0);
  }
  
  public boolean isChannelEnabled(int chan) {
    if (chan < 0 || chan >= channels.length) {
      return false;
    }
    return channels[chan].getSelectedMode() != ChannelConfigData.Mode.NONE;
  }
  
  /**
   * Update config_data with the options set in the UI. 
   */
  public void updateConfigData(ConfigData config_data) {
    for (int chan_index = 0; chan_index < config_data.getNumChannels(); chan_index++) {
      ChannelConfigPanel chan_panel = channels[chan_index];
      ChannelConfigData chan_data = config_data.getChannel(chan_index);
      chan_panel.updateChannelConfigData(chan_data);
    }
  }

}
