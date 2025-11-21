package org.jdscope.device;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Component;
import java.util.ArrayList;

public class ParamTrigger extends Param<Integer> implements ListSelectionListener {

	JList<String> jlTrig;

	// note value is index of TrgType + 1
	public enum TrgType {
		Toggle, FallingEdge, RisingEdge, None
	};

	ArrayList<Integer> TRG_VALUES;

	public ParamTrigger() {
		super();
		initTRG();
		TRG_VALUES.get(0);
	}

	public ParamTrigger(int index) {
		initTRG();
		this.value = TRG_VALUES.get(index);
	}

	private void initTRG() {
		TRG_VALUES = new ArrayList<Integer>(4);
		TRG_VALUES.add(0);
		TRG_VALUES.add(2);
		TRG_VALUES.add(3);
		TRG_VALUES.add(4);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void setValue(Integer index) {
		this.value = TRG_VALUES.get(index);
		if (jlTrig != null)
			jlTrig.setSelectedIndex(index);
		bchanged = true;
	}

	@Override
	public String[] getList() {
		String ret[] = new String[TrgType.values().length];
		for (TrgType k : TrgType.values()) {
			ret[k.ordinal()] = k.name();
		}
		return ret;
	}

	@Override
	public JComponent getPanel() {

		jlTrig = new JList<>(getList());
		jlTrig.setBorder(BorderFactory.createTitledBorder("Triggers"));
		// mListTrig.setPreferredSize(new Dimension(200, 100));
		jlTrig.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlTrig.setLayoutOrientation(JList.VERTICAL);
		jlTrig.setAlignmentX(Component.LEFT_ALIGNMENT);
		int index = TRG_VALUES.indexOf(value);
		jlTrig.setSelectedIndex(index);
		jlTrig.addListSelectionListener(this);
		return jlTrig;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int index = ((JList<?>) e.getSource()).getSelectedIndex();
		setValue(index);
	}

	@Override
	public String getValueS() {
		int index = TRG_VALUES.indexOf(value);
		return TrgType.values()[index].name();
	}

	@Override
	public void setValueS(String s) {
		for (TrgType t : TrgType.values()) {
			if (t.name().equals(s)) {
				int i = t.ordinal();
				value = TRG_VALUES.get(i);
				break;
			}
		}
		bchanged = true;
	}

}
