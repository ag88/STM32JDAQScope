package org.jdscope.device;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import java.awt.Component;

public class ParamThreshold extends Param<Double> implements PropertyChangeListener {

	JFormattedTextField tf;
	
	public ParamThreshold(double threshold) {
		super();
		this.value = threshold;
	}	
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}

	
	@Override
	public void setValue(Double v) {
		this.value = v;				
		if(tf != null) tf.setValue(v);
		bchanged = true;
	}

	@Override
	public JComponent getPanel() {
				
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		tf = new JFormattedTextField(format);
		tf.setBorder(BorderFactory.createTitledBorder("Threshold"));
		tf.setMaximumSize(new Dimension(100, 30));
		tf.setValue(value);
		tf.addPropertyChangeListener(this);
		tf.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		return  tf;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		value = ((Number) ((JFormattedTextField) e.getSource()).getValue()).doubleValue();
	}
	
	@Override
	public String getValueS() {
		return Double.toString(value);
	}

	@Override
	public void setValueS(String s) {
		try {
			double v = Double.parseDouble(s);
			this.value = v;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		bchanged = true;
	}

	
}
