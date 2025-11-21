package org.jdscope.device;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class WordConv {

	public enum WSize {
		BITS8, BITS12, BITS16
	};

	public WSize wsize;

	public WordConv() {
		wsize = WSize.BITS8;
	}

	public WordConv(WSize size) {
		wsize = size;
	}

	public WordConv(int size) {
		switch(size) {
		case 16:
			wsize = WSize.BITS16;
			break;
		case 12:
			wsize = WSize.BITS12;
			break;
		case 8:
		default:
			wsize = WSize.BITS8;
		}		
	}

	public int fromWord(int data) {
		switch (wsize) {
		case BITS16:
			return data;
		case BITS12:
			return data & 0x0fff;
		case BITS8:
		default:
			int vret = data << 4; // use high order 8 bits
			return vret & 0xffff;
		}
	}

	public int toWord(int data) {
		switch (wsize) {
		case BITS16:
			return data;
		case BITS12:
			return data & 0x0fff;
		case BITS8:
		default:
			int vret = data >> 4; // take high order 8 bits
			return vret & 0xff;
		}
	}

	IntBuffer fromWordbuf(ByteBuffer buffer, int count, int blanksize) {
		IntBuffer ret = IntBuffer.allocate(count);
		buffer.rewind();
		ret.position(blanksize);
		switch (wsize) {
		case BITS16:
			buffer.position(blanksize * 2);
			break;
		case BITS12:
			buffer.position(blanksize * 3 / 2);
			break;
		case BITS8:
		default:
			buffer.position(blanksize);
			break;
		}

		while (buffer.hasRemaining()) {
			switch (wsize) {
			case BITS16:
				int v = buffer.get() & 0xff;
				v |= (buffer.get() & 0xff)<< 8;
				ret.put(v);
				break;
			case BITS12:
				int w = buffer.get() & 0xff;
				w |= (buffer.get() & 0xff) << 8;
				w |= (buffer.get() & 0xff) << 16;
				int w1 = w & 0xfff;
				ret.put(w1);
				int w2 = (w >> 12) & 0xfff;
				ret.put(w2);
				break;
			case BITS8:
			default:
				ret.put(buffer.get());
				break;
			}
		}
		return ret;
	}

	public WSize getWsize() {
		return wsize;
	}

	public int getwsize() {
		switch(wsize) {
		case BITS16:
			return 16;
		case BITS12:
			return 12;
		case BITS8:
		default:
			return 8;
		}
	}
	
	public void setWsize(WSize wsize) {
		this.wsize = wsize;
	}
		
}
