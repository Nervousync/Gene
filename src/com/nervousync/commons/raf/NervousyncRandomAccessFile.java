/*
 * Copyright © 2003 Nervousync Studio, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * Nervous Studio, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with Nervous Studio.
 */
package com.nervousync.commons.raf;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.nervousync.utils.FileUtils;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

/**
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Dec 22, 2017 11:49:46 AM $
 */
public class NervousyncRandomAccessFile implements DataInput, DataOutput, Closeable {
	
	private String filePath = null;
	private Object originObject = null;
	
	public NervousyncRandomAccessFile(String filePath, String mode) throws FileNotFoundException {
		this.filePath = filePath;
		this.openFile(mode);
	}

	public long length() throws IOException {
		if (this.filePath.startsWith(FileUtils.SAMBA_URL_PREFIX)) {
			return FileUtils.getSMBFileSize(this.filePath);
		} else {
			return ((RandomAccessFile)this.originObject).length();
		}
	}

	public long getFilePointer() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).getFilePointer();
		} else {
			return ((RandomAccessFile)this.originObject).getFilePointer();
		}
	}

	public void seek(long pos) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).seek(pos);
		} else {
			((RandomAccessFile)this.originObject).seek(pos);
		}
	}

	@Override
	public void close() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).close();
		} else {
			((RandomAccessFile)this.originObject).close();
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).write(b);
		} else {
			((RandomAccessFile)this.originObject).write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).write(b);
		} else {
			((RandomAccessFile)this.originObject).write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).write(b, off, len);
		} else {
			((RandomAccessFile)this.originObject).write(b, off, len);
		}
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeBoolean(v);
		} else {
			((RandomAccessFile)this.originObject).writeBoolean(v);
		}
	}

	@Override
	public void writeByte(int v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeByte(v);
		} else {
			((RandomAccessFile)this.originObject).writeByte(v);
		}
	}

	@Override
	public void writeShort(int v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeShort(v);
		} else {
			((RandomAccessFile)this.originObject).writeShort(v);
		}
	}

	@Override
	public void writeChar(int v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeChar(v);
		} else {
			((RandomAccessFile)this.originObject).writeChar(v);
		}
	}

	@Override
	public void writeInt(int v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeInt(v);
		} else {
			((RandomAccessFile)this.originObject).writeInt(v);
		}
	}

	@Override
	public void writeLong(long v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeLong(v);
		} else {
			((RandomAccessFile)this.originObject).writeLong(v);
		}
	}

	@Override
	public void writeFloat(float v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeFloat(v);
		} else {
			((RandomAccessFile)this.originObject).writeFloat(v);
		}
	}

	@Override
	public void writeDouble(double v) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeDouble(v);
		} else {
			((RandomAccessFile)this.originObject).writeDouble(v);
		}
	}

	@Override
	public void writeBytes(String s) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeBytes(s);
		} else {
			((RandomAccessFile)this.originObject).writeBytes(s);
		}
	}

	@Override
	public void writeChars(String s) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeChars(s);
		} else {
			((RandomAccessFile)this.originObject).writeChars(s);
		}
	}

	@Override
	public void writeUTF(String s) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).writeUTF(s);
		} else {
			((RandomAccessFile)this.originObject).writeUTF(s);
		}
	}
	
	public int read(byte[] b) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).read(b, 0, b.length);
		} else {
			return ((RandomAccessFile)this.originObject).read(b, 0, b.length);
		}
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).read(b, off, len);
		} else {
			return ((RandomAccessFile)this.originObject).read(b, off, len);
		}
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).readFully(b);
		} else {
			((RandomAccessFile)this.originObject).readFully(b);
		}
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			((SmbRandomAccessFile)this.originObject).readFully(b, off, len);
		} else {
			((RandomAccessFile)this.originObject).readFully(b, off, len);
		}
	}

	@Override
	public int skipBytes(int n) throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).skipBytes(n);
		} else {
			return ((RandomAccessFile)this.originObject).skipBytes(n);
		}
	}

	@Override
	public boolean readBoolean() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readBoolean();
		} else {
			return ((RandomAccessFile)this.originObject).readBoolean();
		}
	}

	@Override
	public byte readByte() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readByte();
		} else {
			return ((RandomAccessFile)this.originObject).readByte();
		}
	}

	@Override
	public int readUnsignedByte() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readUnsignedByte();
		} else {
			return ((RandomAccessFile)this.originObject).readUnsignedByte();
		}
	}

	@Override
	public short readShort() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readShort();
		} else {
			return ((RandomAccessFile)this.originObject).readShort();
		}
	}

	@Override
	public int readUnsignedShort() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readUnsignedShort();
		} else {
			return ((RandomAccessFile)this.originObject).readUnsignedShort();
		}
	}

	@Override
	public char readChar() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readChar();
		} else {
			return ((RandomAccessFile)this.originObject).readChar();
		}
	}

	@Override
	public int readInt() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readInt();
		} else {
			return ((RandomAccessFile)this.originObject).readInt();
		}
	}

	@Override
	public long readLong() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readLong();
		} else {
			return ((RandomAccessFile)this.originObject).readLong();
		}
	}

	@Override
	public float readFloat() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readFloat();
		} else {
			return ((RandomAccessFile)this.originObject).readFloat();
		}
	}

	@Override
	public double readDouble() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readDouble();
		} else {
			return ((RandomAccessFile)this.originObject).readDouble();
		}
	}

	@Override
	public String readLine() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readLine();
		} else {
			return ((RandomAccessFile)this.originObject).readLine();
		}
	}

	@Override
	public String readUTF() throws IOException {
		if (this.originObject instanceof SmbRandomAccessFile) {
			return ((SmbRandomAccessFile)this.originObject).readUTF();
		} else {
			return ((RandomAccessFile)this.originObject).readUTF();
		}
	}
	
	private void openFile(String mode) throws FileNotFoundException {
		if (this.filePath.startsWith(FileUtils.SAMBA_URL_PREFIX)) {
			try {
				this.originObject = new SmbRandomAccessFile(new SmbFile(filePath), mode);
			} catch (Exception e) {
				throw new FileNotFoundException("Open file error! File location: " + filePath);
			}
		} else {
			this.originObject = new RandomAccessFile(filePath, mode);
		}
	}
}