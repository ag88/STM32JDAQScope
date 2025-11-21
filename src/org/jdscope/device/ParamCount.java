package org.jdscope.device;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import java.awt.Component;

public class ParamCount extends Param<Integer> implements PropertyChangeListener {

	JFormattedTextField tf;
	
	public ParamCount() {
		super();
		this.value = 1280;
	}
	
	public ParamCount(int count) {
		super();
		this.value = count;
	}
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}

	
	@Override
	public void setValue(Integer v) {
		this.value = v;
		tf.setValue(v);
		bchanged = true;
	}
	

	@Override
	public JComponent getPanel() {
				
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(0);
		format.setGroupingUsed(false);
		tf = new JFormattedTextField(format);
		tf.setBorder(BorderFactory.createTitledBorder("Count"));
		tf.setMaximumSize(new Dimension(100, 30));
		tf.setValue(value);
		tf.addPropertyChangeListener(this);
		tf.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		return  tf;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		value = ((Number) ((JFormattedTextField) e.getSource()).getValue()).intValue();
	}

	@Override
	public String getValueS() {
		return Integer.toString(value);
	}

	@Override
	public void setValueS(String s) {
		try {
			int v = Integer.parseInt(s);
			this.value = v;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		bchanged = true;
	}

	
}
