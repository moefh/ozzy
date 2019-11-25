package org.moefh.ozzy.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.moefh.ozzy.data.ConfigData;
import org.moefh.ozzy.data.GraphData;
import org.moefh.ozzy.serial.ArduinoConnection;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
  
  private static int MAX_CHANNELS = 4;
  private static int MAX_SAMPLES = 16 * 1024;
  
  private ArduinoConnection conn;
  private ConfigData config_data;
  private GraphData  graph_data;
  private String     selected_serial_port = null;
  private int        selected_serial_speed = -1;
  
  private ConfigPanel config;
  private GraphPanel  graph;
  private JTextArea   comm_log;
  private JLabel      status_label;
  
  public void alert(String message) {
    JOptionPane.showMessageDialog(MainWindow.this, message);
  }
  
  public MainWindow() {
    super("Ozzy");
    
    config_data = new ConfigData(MAX_CHANNELS);
    graph_data  = new GraphData(MAX_CHANNELS, MAX_SAMPLES);
    
    setIconImage(new ImageIcon(getClass().getResource("/resources/icons/ozzy.png")).getImage());
    setJMenuBar(buildMenu());
    
    Container pane = getContentPane();
    pane.add(BorderLayout.NORTH,  buildHeader());
    pane.add(BorderLayout.CENTER, buildBody());
    pane.add(BorderLayout.SOUTH,  buildFooter());
    
    setMinimumSize(new Dimension(300, 300));
    setPreferredSize(new Dimension(800, 600));
    pack();
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    conn = new ArduinoConnection(new ArduinoDataListener(this));
  }
  
  public long getConnId() {
    return conn.getId();
  }
  
  private JMenuBar buildMenu() {
    // File
    JMenu menu_file = new JMenu("File");
    
    JMenuItem menu_file_save_as = new JMenuItem("Save Data");
    menu_file.add(menu_file_save_as);

    menu_file.addSeparator();

    JMenuItem menu_file_exit = new JMenuItem("Exit");
    menu_file_exit.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));
    menu_file_exit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) { closeWindow(); }
    });
    menu_file.add(menu_file_exit);
    
    // Help
    JMenu menu_help = new JMenu("Help");
    
    JMenuItem menu_help_about = new JMenuItem("About");
    menu_help_about.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) { showAboutDialog(); }
    });
    menu_help.add(menu_help_about);
    
    // Menu Bar
    JMenuBar menu = new JMenuBar();
    menu.add(menu_file);
    menu.add(menu_help);
    return menu;
  }
  
  private Component buildHeader() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));

    JButton button_connect = new JButton(new ImageIcon(getClass().getResource("/resources/icons/settings.png")));
    button_connect.setRequestFocusEnabled(false);
    button_connect.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) { connectSerial(); }
    });
    button_connect.setFocusPainted(false);
    
    panel.add(button_connect);
    toolbar.add(panel);
    return toolbar;
  }
  
  private Component buildFooter() {
    JPanel footer = new JPanel();
    footer.setBackground(new Color(224, 224, 224));
    footer.setLayout(new FlowLayout(FlowLayout.LEFT));
    status_label = new JLabel("Disconnected");
    footer.add(status_label);
    return footer;
  }
  
  private Component buildBody() {
    JTabbedPane body = new JTabbedPane(); 

    graph = new GraphPanel(graph_data);
    body.addTab("Graph", graph);
    
    config = new ConfigPanel(config_data.getNumChannels());
    config.addUpdateListener(new ConfigUpdateListener() {
      @Override
      public void update() {
        updateConfig();
        conn.setConfigCommand(config_data.getArduinoConfigCommand());
      }
    });
    body.addTab("Config", config);
    
    comm_log = new JTextArea();
    comm_log.setEditable(false);
    JScrollPane scroll_text = new JScrollPane(comm_log,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    body.addTab("Serial Log", scroll_text);
    
    return body;
  }

  public void showAboutDialog() {
    alert("Ozzy: The World's Worst Oscilloscope");
  }
  
  public void closeWindow() {
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
  }
  
  public void setStatusMessage(String message) {
    status_label.setText(message);
  }
  
  public void addCommData(String line) {
    synchronized (graph_data) {
      if (graph_data.add(line)) {
        graph.repaint();
      }
    }
    comm_log.append(line);
  }
  
  private void updateConfig() {
    config.updateConfigData(config_data);
    
    boolean[] chan_enabled = new boolean[config_data.getNumChannels()];
    for (int chan = 0; chan < config_data.getNumChannels(); chan++) {
      chan_enabled[chan] = config.isChannelEnabled(chan);
    }
    graph.setEnabledChannels(chan_enabled);
  }
  
  private boolean selectSerialPort() {
    PortSelectDialog dlg = new PortSelectDialog(this, selected_serial_port, selected_serial_speed);
    PortSelectDialog.Result res = dlg.run();
    if (res != null) {
      selected_serial_port = res.port;
      selected_serial_speed = res.speed;
      return true;
    }
    return false;
  }
  
  private void connectSerial() {
    if (! selectSerialPort()) {
      return;
    }
    updateConfig();
    final String serial_port = selected_serial_port;
    final int serial_speed = selected_serial_speed;
    final String config_cmd = config_data.getArduinoConfigCommand();
    Thread conn_thread = new Thread(new Runnable() {
      @Override
      public void run() {
        conn.setConfigCommand(config_cmd);
        if (! conn.open(serial_port, serial_speed)) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              setStatusMessage("Disconnected");
              alert("Can't open serial port");
            }
          });
        }
      }
    });
    conn_thread.start();
  }

}
