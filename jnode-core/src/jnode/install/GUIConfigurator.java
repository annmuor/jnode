/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.install;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jnode.dto.Link;
import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;

public class GUIConfigurator {
	private String configFile;

	private List<String> config = fillConfigList();
	private Map<String, Component> configMap;
	private Map<String, String> configNames = fillConfigNames();
	private JFrame frmJnodeConfigurator;

	private JPanel linksPanel;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			System.err.println("Args must have a config!");
			System.exit(0);
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIConfigurator window = new GUIConfigurator(args[0]);
					window.frmJnodeConfigurator.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private ArrayList<String> fillConfigList() {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("info.stationname");
		ret.add("info.location");
		ret.add("info.sysop");
		ret.add("info.ndl");
		ret.add("info.address");
		ret.add("log.level");
		ret.add("jdbc.url");
		ret.add("jdbc.user");
		ret.add("jdbc.pass");
		ret.add("binkp.server");
		ret.add("binkp.bind ");
		ret.add("binkp.port");
		ret.add("binkp.inbound");
		ret.add("binkp.client");
		ret.add("poll.period");
		ret.add("poll.delay");
		ret.add("fileecho.enable");
		ret.add("fileecho.path");
		ret.add("stat.enable");
		ret.add("stat.area");
		ret.add("jscript.enable");
		return ret;
	}

	private HashMap<String, String> fillConfigNames() {
		HashMap<String, String> props = new HashMap<>();
		props.put("info.stationname", "Имя узла");
		props.put("info.location", "Расположение узла");
		props.put("info.sysop", "Имя сисопа");
		props.put("info.ndl", "NDL");
		props.put("info.address", "Адрес узла");
		props.put("log.level", "Уровень логирования (1-5)");
		props.put("jdbc.url", "URL СУБД");
		props.put("jdbc.user", "Имя пользователя СУБД");
		props.put("jdbc.pass", "Пароль СУБД");
		props.put("binkp.server", "Принимать соединения");
		props.put("binkp.bind ", "Адрес для приема");
		props.put("binkp.port", "Порт для приема");
		props.put("binkp.inbound", "Путь к входящей почте");
		props.put("binkp.client", "Вызывать узлы по таймеру");
		props.put("poll.period", "Период вызова, с.");
		props.put("poll.delay", "Задержка первого вызова, с.");
		props.put("fileecho.enable", "Включить фэхи");
		props.put("fileecho.path", "Путь к папке с фэхами");
		props.put("stat.enable", "Включить статистику");
		props.put("stat.area", "Эха для статистики");
		props.put("jscript.enable", "Включить скрипты");
		return props;
	}

	private Properties fillDefaultConfig() {
		Properties props = new Properties();
		props.setProperty("info.stationname", "Sample Node");
		props.setProperty("info.location", "City, Country");
		props.setProperty("info.sysop", "Bill Joe");
		props.setProperty("info.ndl", "115200,TCP,BINKP");
		props.setProperty("info.address", "2:9999/9999");
		props.setProperty("log.level", "4");
		props.setProperty("jdbc.url", "jdbc:h2:tcp:jn");
		props.setProperty("jdbc.user", "jnode");
		props.setProperty("jdbc.pass", "jnode");
		props.setProperty("binkp.server", "true");
		props.setProperty("binkp.bind ", "0.0.0.0");
		props.setProperty("binkp.port", "24554");
		props.setProperty("binkp.inbound", "inbound");
		props.setProperty("binkp.client", "true");
		props.setProperty("poll.period", "600");
		props.setProperty("poll.delay", "600");
		props.setProperty("fileecho.enable", "true");
		props.setProperty("fileecho.path", "files");
		props.setProperty("stat.enable", "true");
		props.setProperty("stat.area", "9999.stat");
		props.setProperty("jscript.enable", "true");
		return props;
	}

	/**
	 * Create the application.
	 */
	public GUIConfigurator(String configFile) {
		this.configFile = configFile;
		if (MainHandler.getCurrentInstance() == null) {
			try {
				new MainHandler(configFile);
			} catch (Exception e) {
				e.printStackTrace();
				new MainHandler(fillDefaultConfig());
			}
		}
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		frmJnodeConfigurator = new JFrame();
		frmJnodeConfigurator.setTitle("jNode configurator");
		frmJnodeConfigurator.setBounds(0, 0, tk.getScreenSize().width,
				tk.getScreenSize().height);
		frmJnodeConfigurator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJnodeConfigurator.getContentPane().setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, frmJnodeConfigurator.getWidth(),
				frmJnodeConfigurator.getHeight());
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
						.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				switch (index) {
				case 0:
					updateConfig();
					break;
				case 1:
					updateLinks();
				default:
					break;
				}
			}
		});

		frmJnodeConfigurator.getContentPane().add(tabbedPane);

		JPanel configPanel = new JPanel();
		configPanel.setLayout(new GridLayout(30, 3, 10, 2));
		configMap = new HashMap<>();
		for (String key : config) {
			configPanel.add(new JLabel(configNames.get(key)));
			Component comp;
			Component comment;
			switch (key) {
			case "binkp.server":
			case "binkp.client":
			case "fileecho.enable":
			case "stat.enable":
			case "jscript.enable":
				comp = new Checkbox();
				break;
			default:
				comp = new TextField();
				break;
			}
			if (key.equals("fileecho.path") || key.equals("binkp.inbound")) {
				comment = new FileChooserButton((TextField) comp);
			} else {
				comment = new JLabel("Комментарий");
			}
			configPanel.add(comp);
			configPanel.add(comment);
			configMap.put(key.toString(), comp);
		}
		Button saveButton = new Button("Сохранить");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Properties props = new Properties();
				for (String key : config) {

					Component comp = configMap.get(key);
					String value = null;
					if (comp instanceof TextField) {
						value = ((TextField) comp).getText();
					} else if (comp instanceof Checkbox) {
						value = (((Checkbox) comp).getState()) ? "true"
								: "false";
					}
					if (value != null) {
						props.setProperty(key, value);
					}
				}
				try {
					OutputStream os = new FileOutputStream(configFile);
					props.store(os, "GUIConfigurator process");
					os.close();
					new MainHandler(configFile);
				} catch (IOException ig) {

				}
			}
		});
		Button defButton = new Button("По-умолчанию");
		defButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new MainHandler(fillDefaultConfig());
			}
		});

		configPanel.add(saveButton);
		configPanel.add(defButton);

		tabbedPane.addTab("Конфигурация", configPanel);

		linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		tabbedPane.addTab("Линки", linksPanel);
	}

	private void updateLinks() {
		try {
			ORMManager.INSTANSE.start();
			List<Link> links = ORMManager.get(Link.class).getAll();
			linksPanel.removeAll();
			for (final Link l : links) {
				JLabel label = new JLabel(l.getLinkName() + " "
						+ l.getLinkAddress() + " @ " + l.getProtocolHost()
						+ ":" + l.getProtocolPort());
				label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				label.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void mouseClicked(MouseEvent e) {
						new LinkDialog(frmJnodeConfigurator, l);

					}
				});
				linksPanel.add(label);
			}
			Button button = new Button("Новый линк");
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new LinkDialog(frmJnodeConfigurator, new Link());
					
				}
			});
			linksPanel.add(button);
		} catch (Exception e) {

		}
	}

	protected void updateConfig() {
		for (String key : config) {
			Component comp = configMap.get(key);
			if (comp instanceof TextField) {
				String value = MainHandler.getCurrentInstance().getProperty(
						key, "");
				((TextField) comp).setText(value);
			} else if (comp instanceof Checkbox) {
				boolean value = MainHandler.getCurrentInstance()
						.getBooleanProperty(key, true);
				((Checkbox) comp).setState(value);
			}
		}

	}

	class FileChooserButton extends Button {
		private static final long serialVersionUID = 1L;

		public FileChooserButton(final TextField path) throws HeadlessException {
			super();
			setLabel("Обзор");
			addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int rval = chooser.showOpenDialog(frmJnodeConfigurator);
					if (rval == JFileChooser.APPROVE_OPTION) {
						String txt = chooser.getSelectedFile()
								.getAbsolutePath();
						path.setText(txt);
					}
				}
			});

		}
	}

	class LinkDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		private Link link;
		private TextField linkName;
		private JTextField linkAddress;
		private TextField linkHost;
		private TextField linkPort;
		private TextField linkPassword;
		private TextField linkPktPassword;

		public LinkDialog(Frame owner, Link link) {
			super(owner);
			this.link = link;
			initialize();
			publish();
			setVisible(true);
		}

		public LinkDialog(Window owner, Link link) {
			super(owner);
			this.link = link;
			initialize();
			publish();
			setVisible(true);
		}

		private void publish() {
			if (link != null) {
				if (link.getLinkName() != null) {
					linkName.setText(link.getLinkName());
				}
				if (link.getLinkAddress() != null) {
					linkAddress.setText(link.getLinkAddress());
				}
				if (link.getProtocolHost() != null) {
					linkHost.setText(link.getProtocolHost());
				}
				if (link.getProtocolPort() != null) {
					linkPort.setText(link.getProtocolPort().toString());
				}
				if (link.getProtocolPassword() != null) {
					linkPassword.setText(link.getProtocolPassword());
				}
				if (link.getPaketPassword() != null) {
					linkPktPassword.setText(link.getPaketPassword());
				}
			}

		}

		private void initialize() {
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setModal(true);
			setBounds(30, 40, 400, 300);
			setTitle("Управление линком");
			setLayout(null);
			Insets insets = getInsets();
			JLabel l = new JLabel("Название линка");
			l.setBounds(insets.left+10, insets.top+10, 180, 20);
			add(l);
			linkName = new TextField(10);
			linkName.setBounds(insets.left+190, insets.top+10, 190, 20);
			add(linkName);
			l = new JLabel("Адрес линка");
			l.setBounds(insets.left+10, insets.top+40, 180, 20);
			add(l);
			linkAddress = new JTextField(10);
			linkAddress.setBounds(insets.left+190, insets.top+40, 190, 20);
			linkAddress.setInputVerifier(new InputVerifier() {

				@Override
				public boolean verify(JComponent input) {
					if (input instanceof JTextField) {
						String text = ((JTextField) input).getText();
						try {
							new FtnAddress(text);
							return true;
						} catch (NumberFormatException e) {
						}
					}
					return false;
				}
			});
			add(linkAddress);
			l = new JLabel("Хост линка");
			l.setBounds(insets.left+10, insets.top+80, 180, 20);
			add(l);
			linkHost = new TextField(10);
			linkHost.setBounds(insets.left+190, insets.top+80, 190, 20);
			add(linkHost);
			
			l = new JLabel("Порт линка");
			l.setBounds(insets.left+10, insets.top+120, 180, 20);
			add(l);
			linkPort = new TextField(10);
			linkPort.setBounds(insets.left+190, insets.top+120, 190, 20);
			add(linkPort);
			
			l = new JLabel("Пароль на соединение");
			l.setBounds(insets.left+10, insets.top+160, 180, 20);
			add(l);
			linkPassword = new TextField(10);
			linkPassword.setBounds(insets.left+190, insets.top+160, 190, 20);
			add(linkPassword);
			
			l = new JLabel("Пароль на пакеты");
			l.setBounds(insets.left+10, insets.top+200, 180, 20);
			add(l);
			
			linkPktPassword = new TextField(10);
			linkPktPassword.setBounds(insets.left+190, insets.top+200, 190, 20);
			add(linkPktPassword);

			Button save = new Button("Сохранить");
			save.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					link.setLinkAddress(linkAddress.getText());
					link.setLinkName(linkName.getText());
					link.setProtocolHost(linkHost.getText());
					link.setPaketPassword(linkPktPassword.getText());
					link.setProtocolPort(Integer.valueOf(linkPort.getText()));
					link.setProtocolPassword(linkPassword.getText());
					ORMManager.get(Link.class).saveOrUpdate(link);
					updateLinks();
					LinkDialog.this.dispose();
				}
			});
			
			Button close = new Button("Отмена");
			close.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					LinkDialog.this.dispose();

				}
			});
			save.setBounds(insets.left+100, insets.top+240, 90, 30);
			close.setBounds(insets.left+300, insets.top+240, 90, 30);
			add(save);
			add(close);
		}

	}
}
