package org.jdscope;

import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import org.jdscope.device.Param;

public class JDScopeApp {

	
	Preferences pref = Preferences.userNodeForPackage(JDScopeApp.class);
	
	File mPrevDir;
	
	/**
	 * static Singleton instance.
	 */
	private static JDScopeApp m_instance;

	/**
	 * Private constructor for singleton.
	 */
	private JDScopeApp() {		
		mPrevDir = new File(System.getProperty("user.home"));
		loadPref();		
	}


	/**
	 * Return a singleton instance of JDScopeApp.
	 */
	public static JDScopeApp getInstance() {
		// Double lock for thread safety.
		if (m_instance == null) {
			synchronized (JDScopeApp.class) {
				if (m_instance == null) {
					m_instance = new JDScopeApp();
				}
			}
		}
		return m_instance;
	}
	
	public void run(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				MainFrame frame = new MainFrame();
				frame.setLocationRelativeTo(null);
				frame.pack();
				frame.setVisible(true);		
			}
		});
	}	
	
	public static void main(String[] args) {
		JDScopeApp app =  JDScopeApp.getInstance();
		app.run(args);
	}

	private void loadPref() {
		String sPrevDir = pref.get("prevdir", System.getProperty("user.home"));
		File prevdir = new File(sPrevDir);
		if(!prevdir.isDirectory()) prevdir = new File(System.getProperty("user.home"));
		mPrevDir = prevdir;		
	}
	
	public void loadParam(ArrayList<Param<?>> params) {
		for(Param<?> p: params) {
			String pname = "param." + p.getName();			
			String value = pref.get(pname,null);
			if(value != null)
				p.setValueS(value);			
		}		
	}

	public void saveParam(ArrayList<Param<?>> params) {
		for(Param<?> p: params) {
			String pname = "param." + p.getName();
			String value = p.getValueS();
			pref.put(pname, value);			
		}
	}
	
	public File getmPrevDir() {
		return mPrevDir;
	}

	public void setmPrevDir(File mPrevDir) {
		this.mPrevDir = mPrevDir;
		String sPrevDir = mPrevDir.getAbsolutePath();
		pref.put("prevdir", sPrevDir);
	}
	


}
