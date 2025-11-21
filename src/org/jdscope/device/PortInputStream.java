package org.jdscope.device;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PortInputStream extends FilterInputStream {
	
	boolean debug = false;
	Charset charset;
	
	public PortInputStream(InputStream in) {
		super(in);
		charset = Charset.forName("ISO-8859-1");
	}
	
	public void setdebug(boolean debug) {
		this.debug = debug;
	}
	
	private void printbuf(byte buffer[], int len) {
		if (debug) {
			System.out.print("< ");
			System.out.println(new String(buffer, 0, len));							
			String hex = byteArrayToHex(buffer);
			System.out.println(" ".concat(hex));
		}
	}
	
	public String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}

	@Override
	public int read() throws IOException {
		int c = super.read();
		if (debug) {
			byte b[] = new byte[1];
			b[0] = (byte) c;
			System.out.print("< ");
			System.out.print(charset.decode(ByteBuffer.wrap(b)));
			System.out.println(" ".concat(String.format("%02x", c)));
		}
		return c;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		int len = super.read(buffer);		
		printbuf(buffer, len);
		return len;
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException {
		int lenr = super.read(buffer, off, len);
		printbuf(buffer, lenr);
		return lenr;
	}

	@Override
	public long skip(long n) throws IOException {
		return super.skip(n);
	}

	@Override
	public int available() throws IOException {
		return super.available();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		super.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		super.reset();
	}

	@Override
	public boolean markSupported() {
		return super.markSupported();
	}

}
