package org.jdscope.device;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ParamSamprate extends Param<Integer> implements PropertyChangeListener, ActionListener{	

	public enum Rates {
		Range500, // 0 - 500 ksps range timer driven, has watch dog
		Fixed2400, // 2.4 msps, adc clock 36 mhz, sample time 3 adc clocks, has wd
		Fixed1333, // 1.333 msps, adc clock 36 mhz, sample time 15 adc clocks, has wd
		Fixed900, // 900 ksps, adc clock 36 mhz, sample time 28 adc clocks, has wd
		Fixed667 // 666.67 ksps, adc clock 18 mhz, sample time 15 adc clocks, has wd
	}
	
	final int fixedrates[] = {500000, 2400000, 1333000, 900000, 667000}; 
	
	JFormattedTextField mtf;
	ButtonGroup bg;
	
	public ParamSamprate(int samplerate) {
		this.value = samplerate;
	}
	
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}

	
	@Override
	public void setValue(Integer v) {
		this.value = v;
		mtf.setValue(v);
		bchanged = true;
	}


	@Override
	public JComponent getPanel() {
				
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		pane.setBorder(BorderFactory.createTitledBorder("Sample rate"));
		mtf = new JFormattedTextField(format);		
		mtf.setMaximumSize(new Dimension(100, 30));
		mtf.setValue(value);
		mtf.setAlignmentX(Component.LEFT_ALIGNMENT);
		mtf.setDisabledTextColor(Color.BLUE);
		mtf.addPropertyChangeListener(this);
		pane.add(mtf);
		bg = new ButtonGroup();
		for(Rates r : Rates.values()) {
			JRadioButton rb = new JRadioButton(r.name());
			rb.setActionCommand(r.name());
			if(r.ordinal() == 0) rb.setSelected(true);
			rb.addActionListener(this);
			bg.add(rb);
			pane.add(rb);
		}
		if (this.value > 500000) {
			for(int i=0; i<fixedrates.length; i++) {
				if(this.value == fixedrates[i]) {
					int j=0;
					Enumeration<AbstractButton> btns = bg.getElements();
					while(btns.hasMoreElements()) {
						AbstractButton b = btns.nextElement();
						if(i==j) {
							b.setSelected(true);
							mtf.setEnabled(false);
							break;
						}
						j++;
					}
					break;
				}				
			}
		}
			
		
		return  pane;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		JFormattedTextField fld = (JFormattedTextField) e.getSource();
		this.value = ((Number)fld.getValue()).intValue();
		if(bg.getSelection().getActionCommand().equals(Rates.Range500.name()) &&
		   this.value > 500000) {
			   this.value = 500000;
			   fld.setValue(this.value);
		   }
		if(changelistener != null) changelistener.paramChanged(this);
		bchanged = true;
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		for(Rates r : Rates.values()) {
			if(e.getActionCommand() == r.name()) {
				mtf.setValue(fixedrates[r.ordinal()]);
				value = fixedrates[r.ordinal()];
				if (r.ordinal() == 0)
					mtf.setEnabled(true);
				else
					mtf.setEnabled(false);
				if(changelistener != null) changelistener.paramChanged(this);
				bchanged = true;	
			}
		}

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
