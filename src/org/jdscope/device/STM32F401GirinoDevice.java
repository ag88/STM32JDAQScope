package org.jdscope.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class STM32F401GirinoDevice extends Device {
	
	public enum ParamKey implements Parm {
		Count,
		Threshold,
		Trigger,
		Samplerate,
		RunOnce
	};
	
	public final double VMAX = 3.3;
	public final double VMIN = 0.0;
	public final int BUFSIZE = 1280;
		
	InputStream inputStream;
	OutputStream outputStream;
	
	final byte CMD_START = 's';
	final byte CMD_STOP = 'S';
	// (n - byte)
	// prescaler pnnn , int 
	final byte CMD_PRESCALER = 'p';
	// sample rate Pnnnn, int ksps 
	final byte CMD_SAMPRATE = 'A';
	// set trigger ennn, int
	//	TriggerEvent:
	//	0	Toggle
	//	2	Falling edge
	//	3	Rising edge (default
	//  4   None
	final byte CMD_SETTRIG = 'e';
	// set wait, wnnnn, int
	final byte CMD_SETWAIT = 'w';
	// set threshold, tnnn, int
	final byte CMD_SETTHRES = 't';
	// print status / parameters
	final byte CMD_PPARAM = 'd';	
	//returns identity string
	final byte CMD_IDENT = 'I';
	//reset
	final byte CMD_RESET = 'Z';
	
	
	boolean debug = false;
	
	public STM32F401GirinoDevice() {
		super();
	}
	

	@Override
	public String getName() {
		return "GirinoSTM32F401v1.0";
	}

	@Override
	public int getBufsize() {
		return BUFSIZE;
	}

	@Override
	public void setup() {
		super.setup();
		params.add(new ParamCount(1280));
		params.add(new ParamThreshold(0.1));
		params.add(new ParamTrigger(0));
		params.add(new ParamSamprate(500000));
		params.add(new ParamOnce());
		for(Param<?> p : params) {
			p.setChanged(true);
		}
	}
	
	@Override
	public ArrayList<Param<?>> getParams() {
		return params;
	}

	@Override
	public boolean connect(String portName, int portRate) throws IOException, InterruptedException {

		int timeout = 500;
		disconnect();

		if (!SerialCommMgr.GetInstance().connect(portName, portRate))
			return false;

		outputStream = SerialCommMgr.GetInstance().getOutputStream();
		inputStream = SerialCommMgr.GetInstance().getInputStream();

		bexpect("Girino ready", timeout);
		// check if device is ready
		sendCommand(CMD_IDENT);
		expect(getName(), timeout);

		sendCommand(CMD_RESET);
		Thread.sleep(10);
		return true;
	}
	
	@Override
    public void disconnect() throws IOException, InterruptedException {
    	if(SerialCommMgr.GetInstance().isConnected()) {            
			sendCommand(CMD_STOP);
			Thread.sleep(10);
			sendCommand(CMD_RESET);
			Thread.sleep(10);            
			outputStream = null;
			inputStream = null;
    	}
    	SerialCommMgr.GetInstance().disconnect();
    }
	
    private void sendCommand(int opcode, int data, int datlen) throws IOException {
    	if(datlen == 0) return;

    	//String s = String.format("%c%3d", (char) opcode, data);
    	StringBuffer sb = new StringBuffer(10);
    	sb.append((char) opcode);
    	sb.append(Integer.toString(data));
    	byte[] buf = sb.toString().getBytes("ISO-8859-1");
    	
		if(debug) {
			System.err.println("> " + sb);
		}
		
        outputStream.write(buf);
    }
	
    private void sendCommand(int opcode) throws IOException {
        byte buf = (byte)opcode;
		if(debug) {
			System.err.println("> " + (char) opcode);
		}
        outputStream.write(buf);
    }

	public ByteBuffer readSample(int len) throws IOException, InterruptedException {
		int v;
		if (len == 0)
			return null;
		ByteBuffer buf = ByteBuffer.allocate(len);
		buf.rewind();

		while (buf.position() < len) {
			if (inputStream.available() > 0) {
				v = inputStream.read();
				if (v < 0)
					break;
				buf.put((byte) v);
			} else
				Thread.sleep(1);
			if (Thread.interrupted())
				throw new InterruptedException("sleep interrupted");
		}
		if (debug) {
			System.err.println("< [" + Integer.toString(buf.position()) + "]");
		}

		return buf;
	}
	
	public int read(int timeout) throws IOException, InterruptedException {
		int c = 0;
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (inputStream.available() > 0) {
				c = inputStream.read();
				break;
			} else
				Thread.sleep(1);
			if (Thread.interrupted())
				throw new InterruptedException("sleep interrupted");
		}		
		if(debug) {
			System.err.println("< ".concat(Character.toString((char) c)));
		}
		return c;
	}

	public void expect(String regex, int timeout) throws IOException, InterruptedException {	
		if(!bexpect(regex, timeout))
			throw new InterruptedException("Nomatch :".concat(regex));
	}

	public boolean bexpect(String regex, int timeout) throws IOException, InterruptedException {

		int i = 0;
		ByteBuffer buf = ByteBuffer.allocate(1024);
		if (timeout == 0) timeout = 10; // 10ms

		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (inputStream.available() > 0) {
				int c = inputStream.read();
				if (c < 0)
					break;
				if (c == '\n')
					break; // end of line
				buf.put(i++, (byte) c);
			} else
				Thread.sleep(1);
			if (Thread.interrupted())
				throw new InterruptedException("sleep interrupted");
		}
		String s = new String(buf.array(),"ISO-8859-1");
		if(debug) {
			System.err.println("< ".concat(s));
		}
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(s);
		return m.find();
	}

	@Override
	public void run() throws IOException, InterruptedException {
		
		int timeout = 5000;

		//sendCommand(CMD_RESET);

		// set parameters
		for (Param<?> p : params) {
			// skip unchanged parameters
			if (!p.ischanged())
				continue;
			if (p instanceof ParamTrigger) {
				int t = (int) p.getValue();
				sendCommand(CMD_SETTRIG, t, 3);
				expect("Setting trigger event to:", timeout);
			} else if (p instanceof ParamCount) {
				int c = (int) p.getValue();
				sendCommand(CMD_SETWAIT, c, 3);
				expect("Setting waitDuration to:", timeout);
			} else if (p instanceof ParamThreshold) {
				double thres = (double) p.getValue();
				int t = (int) (thres * 255.0 / getVmax());
				sendCommand(CMD_SETTHRES,t,3);
				expect("Setting threshold to:", timeout);				
			} else if (p instanceof ParamSamprate) {
				int samprate = (int) p.getValue();
				sendCommand(CMD_SAMPRATE, samprate/1000, 4);
				expect("Setting samplerate to:", timeout);
			}
			p.setChanged(false);
		}

		ArrayList<Double> data = databuf.getAlt();
		data.clear();
		
		sendCommand(CMD_START);
		/* this use a different protocol which sent a code byte before data
		 * code 0 is for data
		 * code 1 is for error, error 10 is for ADC overrun 
		int c = read(timeout);
		if(c != 0) {
			c = read(timeout);
			switch(c) {
			case 10:
				throw new InterruptedException("Overrun");
			case -1:
				throw new InterruptedException("End of stream");
			default:
				throw new InterruptedException("Unknown error reading data");				
			}
		}
		*/
		
		ByteBuffer buf = readSample(BUFSIZE);
		buf.rewind();
		while(buf.hasRemaining()) {
			int b = (int) buf.get() & 0xFF;
			double v =	(double) b * VMAX / 255.0;
			data.add(v);
		}		 		
		databuf.swap();
		//stop();
	}

	@Override
    public void stop() throws IOException, InterruptedException {
			sendCommand(CMD_STOP);
    }
	
	@Override
	public void slurp() throws IOException, InterruptedException {
		int TIMEOUT = 500;
		long begin = System.currentTimeMillis();
		while(System.currentTimeMillis() - begin < TIMEOUT) {
			if(inputStream.available()>0) {
				if(debug) 
					System.err.println(
						"< [".concat(Integer.toString(inputStream.available()).concat("]")));
				inputStream.skip(inputStream.available());
			}
			Thread.sleep(1);			
		}
	}

	@Override
    public void reset() throws IOException, InterruptedException {
    	try {
			sendCommand(CMD_RESET);
			//flag parameters as changed so that they'd be set the next round
			for(Param<?> p : params) p.setChanged(true);				
		} catch (IOException e) {
			e.printStackTrace();
		}
    	/*
    	((ParamCount) params.get(0)).setValue(1280);
    	((ParamThreshold) params.get(1)).setValue(0.1);
    	((ParamTrigger) params.get(2)).setValue(0);
    	((ParamSamprate) params.get(3)).setValue(500000);
    	*/
    }

    @Override
	public double getVmax() {
    	return VMAX;
	}


	@Override
	public double getVmin() {
		return VMIN;
	}

	
	@Override
	public void setdebug(boolean debug) {
		this.debug = debug;
	}

	
}
