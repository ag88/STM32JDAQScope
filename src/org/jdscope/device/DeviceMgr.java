package org.jdscope.device;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.jdscope.GraphPanel;
import org.jdscope.MainFrame;

public class DeviceMgr implements Runnable, ParamChangeListener {

	public enum State {
		Init,
		Connected,
		Running,
		Done,
		Stopped,
		Aborted,
		Disconnected
	};
	
	public State mState = State.Init;
	
	public boolean mRec = false;
	public ArrayList<Float> mRecbuf;
	public final int INITSIZE = 2000; 
		
	final int TIMEOUT = 5000; //5s
	
	public Device mDevice;
	public Thread mRunthread;
	
	public GraphPanel mGraph;
	public MainFrame mainFrame;
	
	boolean mRunonce = false;
	
	public DeviceMgr(MainFrame main) {
		//mDevice = new Device();
		this.mainFrame = main;
		mDevice = new STM32F401DAQDevice();
		for(Param<?> p: mDevice.getParams()) {
			p.setChangeListener(this);
		}
		mRecbuf = new ArrayList<Float>();
	}
		
	public Device getDevice() {
		return mDevice;
	}
	
	public void setGraph(GraphPanel graph) {
		mGraph = graph;
	}
	
	public boolean connect() {
		
		if(!(mState == State.Disconnected ||
			mState == State.Init ||
			mState == State.Aborted)) return false; 
			
		String[] ports = SerialCommMgr.GetInstance().getNames();
		JPanel pane = new JPanel();
		pane.add(new JLabel("Select port:"));
		JList<String> jlports = new JList<String>(ports);
		jlports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlports.setSelectedIndex(0);
		pane.add(jlports);
		int ret = JOptionPane.showConfirmDialog(mainFrame, pane, "Select port", 
				JOptionPane.OK_CANCEL_OPTION);
		if(ret == JOptionPane.OK_OPTION) {
			String portName = ports[jlports.getSelectedIndex()];
			try {
				if(mDevice.connect(portName, 115200))
					mState = State.Connected;
			} catch (InterruptedException |IOException e) {
				System.err.println("Unable to connect:");
				e.printStackTrace();
				try {
					mDevice.disconnect();
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				}
				mState = State.Disconnected;
			}
		}
		if(mState == State.Connected) 
			return true;
		else
			return false;
	}
	
	public boolean disconnect() {
		if(mState == State.Running ||
		   mState == State.Done) {
			mRunthread.interrupt();
			mState = State.Stopped;
			mRec = false;
			try {
				mRunthread.join();
			} catch (InterruptedException e) {
			}
		}
		try {
			mDevice.disconnect();
			mState = State.Disconnected;
			if(null != mRecbuf) mRecbuf.clear();
			mRecbuf = null;
			mRec = false;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		if(mState == State.Disconnected) 
			return true;
		else
			return false;
	}

	public boolean startacq() {
		if(mState == State.Init ||
		   mState == State.Disconnected) {
			if(!connect())
				return false;
		} else if (mState == State.Running ||
				   mState == State.Done) {
			stop();
			mainFrame.stop_notify();
			return false;
		}
		
		for(Param<?> p : mDevice.getParams()) {
			if(p instanceof ParamOnce) {
				mRunonce = ((ParamOnce) p).getValue();		
			}			
		}
		
		if(mState != State.Disconnected) {
			if(ismRec() && null == mRecbuf) {
				mRecbuf = new ArrayList<Float>(INITSIZE);
			}
			mRunthread = new Thread(this);
			mRunthread.start();
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		if(mState == State.Running)
			return true;
		else 
			return false;
	}

	public boolean stop() {
		if(mState == State.Running ||
		   mState == State.Done) {
			mRunthread.interrupt();
			try {
				mRunthread.join();	
				mState = State.Stopped;
				mRec = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (mState != State.Disconnected ){
			try {
				mDevice.stop();
				mDevice.reset();
				mState = State.Stopped;
				mRec = false;
			} catch (IOException|InterruptedException e) {
				e.printStackTrace();
				try {
					mDevice.disconnect();
					mState = State.Disconnected;
					mainFrame.stop_notify();
					mainFrame.disconn_notify();					
				} catch (IOException | InterruptedException e1 ) {					
					e1.printStackTrace();
				}
			}
		}
		if (mState == State.Stopped)
			return true;
		else
			return false;
	}
	
	
	public void settimescales(ParamSamprate param) {
		int samplerate = param.getValue();
		int bufsize = mDevice.getBufsize();
		
		double sweeptm = (double) bufsize / (double) samplerate;
		System.out.println(sweeptm);
		double tickw = 10.0;
		double tickfact = 1.0;
		int nticks = (int)(sweeptm / tickw);
		int tries = 0;
		while(!(nticks > 5 && nticks < 100 && tries < 100)) {
			if(tries%2 == 0) {
				tickw /= 2.0;
				tickfact = 0.5;
			} else {
				tickw /= 5.0;
				tickfact = 1.0;
			}
			nticks = (int)(sweeptm / tickw);
			tries++;
		}
		System.out.println("tickw:" + Double.toString(tickw));
		System.out.println("nticks:" + Integer.toString(nticks));
		double numticks = sweeptm / tickw;
		System.out.println("fract tics:" + Double.toString(numticks));
		mGraph.setXtickdivfrac(numticks);
				
		mGraph.setXDivisions(nticks);
		mGraph.setXtickfactor(tickfact);
		mGraph.setToplabel("xdiv:".concat(Double.toString(tickw/tickfact)));
	}

	
	private void copyToRecbuf() {
		Thread thread = new Thread(new Runnable() {			
			@Override
			public void run() {
				for(double v :mDevice.getData().get()) {
					mRecbuf.add((float) v);
				}
			}
		});
		thread.start();
	}
	
	/* 
	 * main processing thread during acquisition
	 */
	@Override
	public void run() {
		mState = State.Running;

		while (!(mState == State.Aborted || 
				mState == State.Stopped)) {
			try {
				mState = State.Running;
				mDevice.run();
				mGraph.setData(mDevice.getData().get());
				if(ismRec()) 
					copyToRecbuf();
				mState = State.Done;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					mDevice.stop();
					mDevice.reset();
					mDevice.disconnect();
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				}				
				for(Param<?> p : mDevice.getParams()) p.setChanged(true);
				mainFrame.stop_notify();
				mainFrame.disconn_notify();
				mState = State.Aborted;
				break;
			} catch (InterruptedException e) {
				String msg = e.getMessage();
				if(msg != null && msg.equals("sleep interrupted")) {
					mState = State.Stopped;
				} else {
					e.printStackTrace();
					mState = State.Aborted;					
				}
				try {
					mDevice.stop();
					mDevice.slurp();
					mDevice.reset();
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				} 
				break;
			}
			if(Thread.interrupted() || mRunonce) {
				mState = State.Stopped;
				break;
			}
		}
		if(mState == State.Stopped ||
		   mState == State.Aborted) {
			try {
				mDevice.stop();
				mainFrame.stop_notify();
				if(mState == State.Aborted) {					
					mainFrame.setMsg("Aborted");
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void paramChanged(Param param) {
		if(param instanceof ParamSamprate) 
			settimescales((ParamSamprate) param); 
		
	}

	public boolean isRunning() {
		return mState == State.Running;
	}
	
	public boolean ismRec() {
		return mRec;
	}

	public void setmRec(boolean mRec) {
		this.mRec = mRec;
	}

	public ArrayList<Float> getRecbuf() {
		return mRecbuf;
	}

	public void setRecbuf(ArrayList<Float> recbuf) {
		this.mRecbuf = recbuf;
	}
	
}
