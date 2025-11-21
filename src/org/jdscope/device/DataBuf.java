package org.jdscope.device;

import java.util.ArrayList;

public class DataBuf {
	
	//double buffer
	@SuppressWarnings("unchecked")
	ArrayList<Double> data[] = new ArrayList[2];
	
	int active = 0;
		
	public DataBuf() {
		data[0] = new ArrayList<Double>();
		data[1] = new ArrayList<Double>();
	}

	public DataBuf(int size) {
		data[0] = new ArrayList<Double>(size);
		data[1] = new ArrayList<Double>(size);
	}

	public ArrayList<Double> get() {
		return data[active];
	}
	
	public ArrayList<Double> getAlt() {
		int alt = active==1?0:1;
		return data[alt];
	}
	
	public void set(ArrayList<Double> data) {
		this.data[active] = data;
	}
	
	public void swap() {
		active = active==1?0:1;
	}
}
