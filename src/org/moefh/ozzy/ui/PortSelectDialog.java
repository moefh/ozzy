package org.moefh.ozzy.ui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

@SuppressWarnings("serial")
class PortSelectDialog extends JDialog {

  public class Result {
    public String port;
    public int speed;
  }
  
  protected JComboBox<String> combo_port;
  protected JComboBox<String> combo_speed;
  protected boolean ok_clicked;
  
  public PortSelectDialog(JFrame parent, String default_serial_port, int default_serial_speed) {
    super(parent, "Select Serial Port", true);
    
    JPanel main_panel = new JPanel();
    GridBagLayout gbl_panel_2 = new GridBagLayout();
    gbl_panel_2.columnWidths = new int[] {30, 0, 0, 0, 30};
    gbl_panel_2.rowHeights = new int[] {30, 0, 0, 30};
    gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
    main_panel.setLayout(gbl_panel_2);
    
    JLabel lblPort = new JLabel("Port:");
    GridBagConstraints gbc_lblPort = new GridBagConstraints();
    gbc_lblPort.insets = new Insets(0, 0, 5, 5);
    gbc_lblPort.anchor = GridBagConstraints.EAST;
    gbc_lblPort.gridx = 1;
    gbc_lblPort.gridy = 1;
    main_panel.add(lblPort, gbc_lblPort);
    
    combo_port = new JComboBox<>();
    reloadSerialPortNames();
    GridBagConstraints gbc_combo_port = new GridBagConstraints();
    gbc_combo_port.insets = new Insets(0, 0, 5, 5);
    gbc_combo_port.fill = GridBagConstraints.HORIZONTAL;
    gbc_combo_port.gridx = 2;
    gbc_combo_port.gridy = 1;
    main_panel.add(combo_port, gbc_combo_port);
    
    JButton button_refresh_ports = new JButton("Refresh");
    button_refresh_ports.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) { reloadSerialPortNames(); }
    });
    GridBagConstraints gbc_button_refresh_ports = new GridBagConstraints();
    gbc_button_refresh_ports.insets = new Insets(0, 0, 5, 0);
    gbc_button_refresh_ports.gridx = 3;
    gbc_button_refresh_ports.gridy = 1;
    main_panel.add(button_refresh_ports, gbc_button_refresh_ports);
    
    JLabel lblSpeed = new JLabel("Speed:");
    GridBagConstraints gbc_lblSpeed = new GridBagConstraints();
    gbc_lblSpeed.anchor = GridBagConstraints.EAST;
    gbc_lblSpeed.insets = new Insets(0, 0, 0, 5);
    gbc_lblSpeed.gridx = 1;
    gbc_lblSpeed.gridy = 2;
    main_panel.add(lblSpeed, gbc_lblSpeed);
    
    combo_speed = new JComboBox<>();
    combo_speed.setModel(new DefaultComboBoxModel<>(new String[] {"115200", "57600", "38400", "19200", "9600"}));
    GridBagConstraints gbc_combo_speed = new GridBagConstraints();
    gbc_combo_speed.insets = new Insets(0, 0, 0, 5);
    gbc_combo_speed.fill = GridBagConstraints.HORIZONTAL;
    gbc_combo_speed.gridx = 2;
    gbc_combo_speed.gridy = 2;
    main_panel.add(combo_speed, gbc_combo_speed);

    JButton button_cancel = new JButton("Cancel");
    button_cancel.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) { cancelClicked(); }
    });
    JButton button_ok = new JButton("OK");
    button_ok.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) { okClicked(); }
    });
    getRootPane().setDefaultButton(button_ok);

    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
    button_panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    button_panel.add(Box.createHorizontalGlue());
    button_panel.add(button_cancel);
    button_panel.add(Box.createRigidArea(new Dimension(10, 0)));
    button_panel.add(button_ok);
    
    getContentPane().add(main_panel, BorderLayout.CENTER);
    getContentPane().add(button_panel, BorderLayout.PAGE_END);
    
    selectComboItem(combo_port, default_serial_port);
    selectComboItem(combo_speed, Integer.toString(default_serial_speed));
    
    pack();
    setLocationRelativeTo(parent);
  }
  
  private boolean selectComboItem(JComboBox<String> combo, String item) {
    if (item == null) return false;
    
    ComboBoxModel<String> model = combo.getModel();
    for (int i = 0; i < model.getSize(); i++) {
      if (item.equals(model.getElementAt(i))) {
        combo.setSelectedIndex(i);
        return true;
      }
    }
    return false;
  }
  
  private void reloadSerialPortNames() {
    SerialPort[] ports = SerialPort.getCommPorts();
    String[] port_names = new String[ports.length];
    for (int i = 0; i < ports.length; i++) {
      port_names[i] = ports[i].getSystemPortName();
    }

    combo_port.setModel(new DefaultComboBoxModel<>(port_names));
  }

  protected void okClicked() {
    if (getSelectedPort() == null || getSelectedSpeed() < 0) {
      return;
    }
    ok_clicked = true;
    setVisible(false);
  }
  
  protected void cancelClicked() {
    ok_clicked = false;
    setVisible(false);
  }
  
  protected String getSelectedPort() {
    if (combo_port == null) return null;
    return (String) combo_port.getSelectedItem();
  }
  
  protected int getSelectedSpeed() {
    if (combo_speed == null) return -1;
    String str = (String) combo_speed.getSelectedItem();
    if (str == null) return -1;
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  public Result run() {
    setVisible(true);
    
    if (! ok_clicked) {
      return null;
    }
    
    String port = getSelectedPort();
    int speed = getSelectedSpeed();
    if (port == null || speed < 0) {
      return null;
    }

    Result res = new Result();
    res.port = port;
    res.speed = speed;
    return res;
  }

}
