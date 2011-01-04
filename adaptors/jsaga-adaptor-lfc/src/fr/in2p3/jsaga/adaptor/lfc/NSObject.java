package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.nio.ByteBuffer;

abstract class NSObject{
	protected final ByteBuffer byteBuffer;
	public NSObject(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}
	
	protected abstract void fillObject() throws IOException;
}