package org.jdscope.device;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdscope.device.ParamTrigger.TrgType;

import java.awt.Component;
import java.util.ArrayList;

public class ParamWordSize extends Param<Integer> implements ListSelectionListener {

	JList<String> jlWsize;

	// note value is index of TrgType + 1
	public enum WSizeType {
		Bits8, Bits12, Bits16
	};

	ArrayList<Integer> WSIZE_VALUES;

	public ParamWordSize() {
		super();
		initTRG();
		value = WSIZE_VALUES.get(0);
	}

	public ParamWordSize(int index) {
		initTRG();
		this.value = WSIZE_VALUES.get(index);
	}

	private void initTRG() {
		WSIZE_VALUES = new ArrayList<Integer>(4);
		WSIZE_VALUES.add(8);
		WSIZE_VALUES.add(12);
		WSIZE_VALUES.add(16);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void setValue(Integer index) {
		this.value = WSIZE_VALUES.get(index);
		if (jlWsize != null)
			jlWsize.setSelectedIndex(index);
		bchanged = true;
	}

	@Override
	public String[] getList() {
		String ret[] = new String[WSizeType.values().length];
		for (WSizeType k : WSizeType.values()) {
			ret[k.ordinal()] = k.name();
		}
		return ret;
	}

	@Override
	public JComponent getPanel() {

		jlWsize = new JList<>(getList());
		jlWsize.setBorder(BorderFactory.createTitledBorder("Bits"));
		// mListTrig.setPreferredSize(new Dimension(200, 100));
		jlWsize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlWsize.setLayoutOrientation(JList.VERTICAL);
		jlWsize.setAlignmentX(Component.LEFT_ALIGNMENT);
		int index = WSIZE_VALUES.indexOf(value);
		jlWsize.setSelectedIndex(index);
		jlWsize.addListSelectionListener(this);
		return jlWsize;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = ((JList<?>) e.getSource()).getSelectedIndex();
		setValue(index);
	}

	@Override
	public String getValueS() {
		int index = WSIZE_VALUES.indexOf(value);
		return WSizeType.values()[index].name();
	}

	@Override
	public void setValueS(String s) {
		for (WSizeType t : WSizeType.values()) {
			if (t.name().equals(s)) {
				int i = t.ordinal();
				value = WSIZE_VALUES.get(i);
				break;
			}
		}
		bchanged = true;
	}

}
