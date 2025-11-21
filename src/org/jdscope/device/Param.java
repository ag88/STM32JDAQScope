package org.jdscope.device;

import javax.swing.JComponent;

public class Param<V> {
	
	public V value;
	public boolean bchanged = false;
	
	protected ParamChangeListener changelistener = null;

	public Param() {
	}
	
	public String getName() {
		return "";
	}
		
	public V getValue() {
		return value;
	}

	public String getValueS() {
		return "";
	}

	public void setValue(V v) {
		this.value = v;
		this.bchanged = true;
	}
	
	public void setValueS(String s) {
	}
	
	public boolean ischanged() {
		return bchanged;
	}
	
	public void setChanged(boolean changed) {
		bchanged = changed;
	}	
	
	public String[] getList() {
		return null;
	}
	
	public JComponent getPanel() {
		return null;
	}
	
	public void setChangeListener(ParamChangeListener listener) {
		this.changelistener = listener;
	}
}
