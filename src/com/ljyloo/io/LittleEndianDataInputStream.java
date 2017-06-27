package com.ljyloo.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public final class LittleEndianDataInputStream extends FilterInputStream implements DataInput {

  public LittleEndianDataInputStream(InputStream in) {
    super(Preconditions.checkNotNull(in));
  }

  @Override
  public String readLine() {
    throw new UnsupportedOperationException("readLine is not supported");
  }

  @Override
  public void readFully(byte[] b) throws IOException {
    ByteStreams.readFully(this, b);
  }

  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    ByteStreams.readFully(this, b, off, len);
  }

  @Override
  public int skipBytes(int n) throws IOException {
    return (int) in.skip(n);
  }

  @Override
  public int readUnsignedByte() throws IOException {
    int b1 = in.read();
    if (0 > b1) {
      throw new EOFException();
    }

    return b1;
  }

  @Override
  public int readUnsignedShort() throws IOException {
    byte b1 = readAndCheckByte();
    byte b2 = readAndCheckByte();

    return Ints.fromBytes((byte) 0, (byte) 0, b2, b1);
  }

  @Override
  public int readInt() throws IOException {
    byte b1 = readAndCheckByte();
    byte b2 = readAndCheckByte();
    byte b3 = readAndCheckByte();
    byte b4 = readAndCheckByte();

    return Ints.fromBytes(b4, b3, b2, b1);
  }

  @Override
  public long readLong() throws IOException {
    byte b1 = readAndCheckByte();
    byte b2 = readAndCheckByte();
    byte b3 = readAndCheckByte();
    byte b4 = readAndCheckByte();
    byte b5 = readAndCheckByte();
    byte b6 = readAndCheckByte();
    byte b7 = readAndCheckByte();
    byte b8 = readAndCheckByte();

    return Longs.fromBytes(b8, b7, b6, b5, b4, b3, b2, b1);
  }

  @Override
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  @Override
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public String readUTF() throws IOException {
      int utflen = this.readUnsignedShort();
      byte[] bytearr = new byte[utflen];
      char[] chararr = new char[utflen];

      int c, char2, char3;
      int count = 0;
      int chararr_count=0;

      this.readFully(bytearr, 0, utflen);

      while (count < utflen) {
          c = (int) bytearr[count] & 0xff;
          if (c > 127) break;
          count++;
          chararr[chararr_count++]=(char)c;
      }

      while (count < utflen) {
          c = (int) bytearr[count] & 0xff;
          switch (c >> 4) {
              case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                  /* 0xxxxxxx*/
                  count++;
                  chararr[chararr_count++]=(char)c;
                  break;
              case 12: case 13:
                  /* 110x xxxx   10xx xxxx*/
                  count += 2;
                  if (count > utflen)
                      throw new UTFDataFormatException(
                          "malformed input: partial character at end");
                  char2 = (int) bytearr[count-1];
                  if ((char2 & 0xC0) != 0x80)
                      throw new UTFDataFormatException(
                          "malformed input around byte " + count);
                  chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                                                  (char2 & 0x3F));
                  break;
              case 14:
                  /* 1110 xxxx  10xx xxxx  10xx xxxx */
                  count += 3;
                  if (count > utflen)
                      throw new UTFDataFormatException(
                          "malformed input: partial character at end");
                  char2 = (int) bytearr[count-2];
                  char3 = (int) bytearr[count-1];
                  if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                      throw new UTFDataFormatException(
                          "malformed input around byte " + (count-1));
                  chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                                  ((char2 & 0x3F) << 6)  |
                                                  ((char3 & 0x3F) << 0));
                  break;
              default:
                  /* 10xx xxxx,  1111 xxxx */
                  throw new UTFDataFormatException(
                      "malformed input around byte " + count);
          }
      }
      // The number of chars produced may be less than utflen
      return new String(chararr, 0, chararr_count);
  }

  @Override
  public short readShort() throws IOException {
    return (short) readUnsignedShort();
  }

  @Override
  public char readChar() throws IOException {
    return (char) readUnsignedShort();
  }

  @Override
  public byte readByte() throws IOException {
    return (byte) readUnsignedByte();
  }

  @Override
  public boolean readBoolean() throws IOException {
    return readUnsignedByte() != 0;
  }

  private byte readAndCheckByte() throws IOException, EOFException {
    int b1 = in.read();

    if (-1 == b1) {
      throw new EOFException();
    }

    return (byte) b1;
  }
}
