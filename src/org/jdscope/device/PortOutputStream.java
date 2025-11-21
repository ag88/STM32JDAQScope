package org.jdscope.device;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.fazecast.jSerialComm.SerialPortIOException;

public class PortOutputStream extends FilterOutputStream {

	boolean debug = false;
	Charset charset;
	
	public PortOutputStream(OutputStream out) {
		super(out);
		charset = Charset.forName("ISO-8859-1");
	}

	public void setdebug(boolean debug) {
		this.debug = debug;
	}
	
	private void printbuf(byte buffer[], int len) {
		if (debug) {
			System.out.print("> ");							
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
	public void write(int c) throws IOException {
		if (debug) {
			byte b[] = new byte[1];
			b[0] = (byte) c;
			System.out.print("> ");
			System.out.print(charset.decode(ByteBuffer.wrap(b)));
			System.out.println(" ".concat(String.format("%02x", c)));							
		}
		try {
			super.write(c);
		} catch (SerialPortIOException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		printbuf(b, b.length);
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		printbuf(b, b.length);
		super.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		super.flush();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

}
