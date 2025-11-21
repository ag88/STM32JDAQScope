package org.jdscope;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jdscope.device.DeviceMgr;
import org.jdscope.device.Param;
import org.jdscope.device.ParamSamprate;
import org.jdscope.device.SerialCommMgr;

public class MainFrame extends JFrame implements ActionListener, WindowListener {

	GraphPanel graphpanel;
	ArrayList<JComponent> mParPanel;
	JToolBar mToolbar;
	JLabel mlStatus;
	JCheckBoxMenuItem miDevDebug;
	JCheckBoxMenuItem miCommDebug;

	DeviceMgr mDevMgr;

	public MainFrame() throws HeadlessException {
		setTitle("JDScope");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(1024, 768));
		// mDevice = new STM32F401GirinoDevice();
		mDevMgr = new DeviceMgr(this);
		ArrayList<Param<?>> params = mDevMgr.getDevice().getParams();
		JDScopeApp.getInstance().loadParam(params);
		creategui();
	}

	private void creategui() {

		JMenuBar mBar = new JMenuBar();
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic(KeyEvent.VK_F);
		mFile.add(addmenuitem("Close", "CLOSE", KeyEvent.VK_L));
		mFile.add(addmenuitem("Snapshot", "SNAP", KeyEvent.VK_S));
		mFile.add(addmenuitem("Export", "EXPORT", KeyEvent.VK_X));
		mBar.add(mFile);

		JMenu jmDevice = new JMenu("Device");
		jmDevice.setMnemonic(KeyEvent.VK_D);
		jmDevice.add(addmenuitem("Connect", "CONNECT", KeyEvent.VK_C));
		jmDevice.add(addmenuitem("Start Aquisition", "ASTART", KeyEvent.VK_S));
		jmDevice.add(addmenuitem("Stop Aquisition", "ASTOP", KeyEvent.VK_T));
		jmDevice.add(addmenuitem("Rec", "AREC", KeyEvent.VK_R));
		jmDevice.add(addmenuitem("Disconnect", "DISCON", KeyEvent.VK_D));
		jmDevice.addSeparator();
		miDevDebug = new JCheckBoxMenuItem("Debug (device)");
		miDevDebug.setActionCommand("DEVDEBUG");
		miDevDebug.addActionListener(this);
		jmDevice.add(miDevDebug);
		miCommDebug = new JCheckBoxMenuItem("Debug (com)");
		miCommDebug.setActionCommand("COMMDEBUG");
		miCommDebug.setSelected(false);
		miCommDebug.addActionListener(this);
		jmDevice.add(miCommDebug);
		mBar.add(jmDevice);
		setJMenuBar(mBar);

		getContentPane().setLayout(new BorderLayout());

		mToolbar = new JToolBar();
		mToolbar.add(makeNavigationButton("plug.png", "CONNECT", "Connect", "Connect"));
		mToolbar.add(makeNavigationButton("noplug.png", "DISCON", "Disconnect", "Disconnect"));
		mToolbar.add(makeNavigationButton("Play24.gif", "ASTART", "Start Aquisition", "Start Aquisition"));
		mToolbar.add(makeNavigationButton("Stop24.gif", "ASTOP", "Stop Aquisition", "Stop Aquisition"));
		mToolbar.add(makeNavigationButton("camera.png", "SNAP", "Snapshot", "Snapshot"));
		mToolbar.add(makeNavigationButton("Rec24.png", "AREC", "Rec", "Rec"));
		getContentPane().add(mToolbar, BorderLayout.NORTH);

		ArrayList<Double> data = new ArrayList<Double>();
		data.add(0.0);
		data.add(3.3);
		graphpanel = new GraphPanel(data);
		graphpanel.setPreferredSize(new Dimension(800, 600));
		getContentPane().add(graphpanel, BorderLayout.CENTER);
		mDevMgr.setGraph(graphpanel);

		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		pane.add(Box.createVerticalStrut(30));

		ArrayList<Param<?>> params = mDevMgr.getDevice().getParams();
		for (Param<?> p : params) {
			JComponent parpanel = p.getPanel();
			pane.add(parpanel);
			pane.add(Box.createVerticalStrut(20));
		}

		pane.add(Box.createVerticalGlue());
		Filler strut = (Filler) Box.createHorizontalStrut(100);
		strut.setAlignmentX(LEFT_ALIGNMENT);
		pane.add(strut);

		getContentPane().add(pane, BorderLayout.EAST);

		mlStatus = new JLabel("Messages:");
		getContentPane().add(mlStatus, BorderLayout.SOUTH);
		
		addWindowListener(this);

	}

	protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText) {

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);

		setIcon(button, imageName, altText);

		return button;
	}

	protected void setIcon(JButton button, String imageName, String altText) {
		// Look for the image.
		String imgLocation = "icons/" + imageName;
		URL imageURL = JDScopeApp.class.getResource(imgLocation);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}
	}

	public JMenuItem addmenuitem(String label, String cmd, int keyevent) {
		JMenuItem item = new JMenuItem(label);
		item.setMnemonic(keyevent);
		item.setActionCommand(cmd);
		item.addActionListener(this);
		return item;
	}

	public void setMsg(String msg) {
		mlStatus.setText(msg);
	}

	public File savedialog() {
		File prevdir = JDScopeApp.getInstance().getmPrevDir();
		JFileChooser chooser = new JFileChooser(prevdir);
		int ret = chooser.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			File dir = file.getParentFile();
			if (dir != null && dir.isDirectory())
				JDScopeApp.getInstance().setmPrevDir(dir);
			return file;
		} else
			return null;
	}

	public void dosnapshot() {
		File file = savedialog();
		if (file == null)
			return;
		BufferedImage image = graphpanel.snapshot();
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doexport(ArrayList<?> data) {
		assert (data != null);
		File file = savedialog();
		if (file == null)
			return;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Param p : mDevMgr.getDevice().getParams()) {
				if (p instanceof ParamSamprate) {
					Integer samplerate = ((ParamSamprate) p).getValue();
					writer.write("Sample rate:".concat(samplerate.toString()));
					writer.newLine();
				}
			}
			writer.write("Data:");
			writer.newLine();
			Iterator<?> iter = data.iterator();
			while(iter.hasNext()) {
				Object v = iter.next();
				if(v instanceof Double) {
					writer.write(((Double) v).toString());
				} else if (v instanceof Float) {
					writer.write(((Float) v).toString());
				}
				writer.newLine();
			}
			writer.flush();
			writer.close();
			String msg = "Text data writtern to ".concat(file.getName());
			setMsg(msg);
			System.out.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkrec() {
		if (mDevMgr.getRecbuf() == null) return;
		if (mDevMgr.getRecbuf().size() > 0) {
			int ret = JOptionPane.showConfirmDialog(this, "Record buffer has data save?", "Save record?",
					JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				ArrayList<Float> data = mDevMgr.getRecbuf();
				doexport(data);
				mDevMgr.getRecbuf().clear();
				mDevMgr.setRecbuf(new ArrayList<Float>());
			}
		}
	}

	private void doRec() {
		if (mDevMgr.ismRec()) {
			if (mDevMgr.stop())
				stop_notify();
			checkrec();
		} else {
			if(mDevMgr.isRunning()) {
				mDevMgr.stop();
				checkrec();
			}
			mDevMgr.setmRec(true);
			if (mDevMgr.startacq()) {
				setIcon(((JButton) mToolbar.getComponent(5)), "RecOn24.png", "Rec");
				setMsg("Running");
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "CONNECT") {
			if (mDevMgr.connect()) {
				setIcon(((JButton) mToolbar.getComponent(0)), "plugon.png", "Connect");
				setMsg("Connected");
			}
		} else if (e.getActionCommand() == "DISCON") {
			if (mDevMgr.stop())
				stop_notify();
			checkrec();
			if (mDevMgr.disconnect()) {
				disconn_notify();				
			}
		} else if (e.getActionCommand() == "ASTART") {
			if (mDevMgr.startacq()) {
				setIcon(((JButton) mToolbar.getComponent(2)), "Play24g.png", "Start Aquisition");
				setMsg("Running");
			}
		} else if (e.getActionCommand() == "AREC") {
			doRec();
		} else if (e.getActionCommand() == "ASTOP") {
			if (mDevMgr.stop())
				stop_notify();
			checkrec();
		} else if (e.getActionCommand() == "SNAP") {
			dosnapshot();

		} else if (e.getActionCommand() == "EXPORT") {
			ArrayList<Double> data = mDevMgr.getDevice().getData().get();
			doexport(data);

		} else if (e.getActionCommand() == "CLOSE") {
			dispose();
		} else if (e.getActionCommand() == "DEVDEBUG") {
			JCheckBoxMenuItem o = (JCheckBoxMenuItem) e.getSource();
			mDevMgr.getDevice().setdebug(o.isSelected());
		} else if (e.getActionCommand() == "COMMDEBUG") {
			JCheckBoxMenuItem o = (JCheckBoxMenuItem) e.getSource();
			SerialCommMgr.GetInstance().setDebug(o.isSelected());
		}
	}

	public void stop_notify() {
		setIcon(((JButton) mToolbar.getComponent(2)), "Play24.gif", "Start Aquisition");
		setIcon(((JButton) mToolbar.getComponent(5)), "Rec24.png", "Rec");
		setMsg("Stopped");
	}

	public void disconn_notify() {
		setIcon(((JButton) mToolbar.getComponent(0)), "plug.png", "Connect");
		setMsg("Disconnected");
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		try {
			ArrayList<Param<?>> params = mDevMgr.getDevice().getParams();
			JDScopeApp.getInstance().saveParam(params);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {		
	}

	@Override
	public void windowIconified(WindowEvent e) {		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
