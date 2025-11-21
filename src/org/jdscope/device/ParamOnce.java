package org.jdscope.device;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class ParamOnce extends Param<Boolean> implements ActionListener {

	JCheckBox cb;
	
	public ParamOnce() {
		super();
		this.value = false;
	}
	
	public ParamOnce(boolean once) {
		super();
		this.value = once;
	}
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}
	
	
	@Override
	public JComponent getPanel() {
				
		cb = new JCheckBox("Run Once");
		cb.setMaximumSize(new Dimension(100, 30));
		cb.setSelected(value);
		cb.addActionListener(this);
		cb.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		return  cb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		value = ((JCheckBox) e.getSource()).isSelected();
	}

	@Override
	public String getValueS() {
		return Boolean.toString(value);
	}

	@Override
	public void setValueS(String s) {
		try {
			boolean v = Boolean.parseBoolean(s);
			this.value = v;
		} catch (Exception e) {
			e.printStackTrace();
		}
		bchanged = true;
	}


}
