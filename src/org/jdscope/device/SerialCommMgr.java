package org.jdscope.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.fazecast.jSerialComm.SerialPort;

public class SerialCommMgr {

	private SerialPort port = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private InputStream upinputStream = null;
	private OutputStream upoutputStream = null;
	
	private boolean debug = false;
	
	private static SerialCommMgr mInstance = null;
	SerialPort ports[];
		
	private SerialCommMgr() {
		ports = SerialPort.getCommPorts();
	}
	
	public static SerialCommMgr GetInstance() {
		if(mInstance == null)
			mInstance = new SerialCommMgr();
		
		return mInstance;
	}
			
	public String[] getNames() {
		ArrayList<String> names = new ArrayList<String>(10);
		for(SerialPort p : ports) {
			names.add(p.getDescriptivePortName());
		}
		return names.toArray(new String[0]);
	}

	
	public SerialPort getPort(String portName) {            
        for(SerialPort port : ports) {
        	if(port.getDescriptivePortName() == portName) {
        		return port;
        	}
        }
        return null;
	}


	public boolean connect(String portName, int portRate) throws IOException {
		port = getPort(portName);
		if (port == null) return false;
				
		// port = (SerialPort) portId.open("Logic Analyzer Client", 1000);
		if(!port.openPort()) {
			port = null;
			throw new IOException("Unable to open port");
		}
		//port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
		port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
		port.setComPortParameters(portRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
		// port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
		// port.disableReceiveFraming();
		// port.enableReceiveTimeout(100);

		inputStream = port.getInputStream();
		outputStream = port.getOutputStream();

		return true;
	}
	
	public void disconnect() {
		
        if (port != null) {
            
            try {
            	upoutputStream.close();
            	upinputStream.close();
				outputStream.close();
				inputStream.close();
			} catch (IOException e ) {
				e.printStackTrace();
			} catch (NullPointerException e1) {				
			}            
            port.closePort();
            port = null;

            outputStream = null;
            inputStream = null;
            upinputStream = null;
            upoutputStream = null;
        }
	}
		
	boolean isConnected() {
		if(port != null) {
			if (port.isOpen()) 
				return true;
			else {
				disconnect();
				return false;
			}				
		} else
			return false;
	}
	
	
	public OutputStream getOutputStream() {
		if (upoutputStream == null && outputStream != null)
			upoutputStream = new PortOutputStream(outputStream);
		((PortOutputStream) upoutputStream).setdebug(debug);		
		return upoutputStream;
	}
	
	public OutputStreamWriter getWriter() {
		try {
			return new OutputStreamWriter(getOutputStream(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {			
			e.printStackTrace();
			return null;
		}
	}
	
	public InputStream getInputStream() {
		if (upinputStream == null && inputStream != null)
			upinputStream = new PortInputStream(inputStream);
		((PortInputStream) upinputStream).setdebug(debug);
		return upinputStream;
	}
	
	public InputStreamReader getReader() {
		try {
			return new InputStreamReader(getInputStream(),"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
		if(isConnected()) {
			((PortOutputStream) upoutputStream).setdebug(debug);
			((PortInputStream) upinputStream).setdebug(debug);
		}
	}

}
