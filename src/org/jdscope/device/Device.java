package org.jdscope.device;

import java.io.IOException;
import java.util.ArrayList;

public class Device {
	
	
	public DataBuf databuf;
	
	interface Parm {		
	}
	
	ArrayList<Param<?>> params = new ArrayList<Param<?>>(10);
	
	public Device() {
		databuf = new DataBuf(); 
		setup();
	}
	
	public String getName() {
		return "";
	}
	
	public int getBufsize() {
		return 0;
	}
	
	public DataBuf getData() {
		return databuf;
	}
		
	public void setup() {		
	}

	public ArrayList<Param<?>> getParams() {
		return null;
	}
	
	public double getVmax() {
		return 0.0;
	}
	
	public double getVmin() {
		return 0.0;
	}

	public boolean connect(String portName, int portRate) throws InterruptedException, IOException {
		return false;
	}
	
	public void disconnect() throws IOException, InterruptedException {
	}
	
	public void run() throws IOException, InterruptedException {		
	}
	
	public void stop() throws IOException, InterruptedException {
	}

	public void slurp() throws IOException, InterruptedException {		
	}

	public void reset() throws IOException, InterruptedException {	
	}

	public void setdebug(boolean debug) {		
	}

	
}
