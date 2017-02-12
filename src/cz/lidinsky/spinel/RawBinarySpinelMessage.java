/*
 * Copyright (C) 2017 jilm
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.lidinsky.spinel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The raw binary spinel message implementation.
 *
 * The message is of the form:
 *
 *      PRE FRM NUM NUM SDATA CR
 *
 * PRE - Prefix, it is there to simplify message beginning detection.
 * FRM - Frame number (97-255].
 * NUM - Bytes number.
 * SDATA - Data.
 * CR - Frame end character.
 *
 */
public class RawBinarySpinelMessage {

  public static final int PRE = 0x2a;
  public static final int CR = 0x0d;

  private final int frm;

  private final byte[] sdata;

  public RawBinarySpinelMessage(int frm, byte[] data) {
    this.frm = frm;
    this.sdata = data;
  }


  public void send(OutputStream stream) throws IOException {
    stream.write(PRE);
    stream.write(frm);
    int num = sdata.length + 1;
    stream.write((num & 0xff00) >>> 8);
    stream.write(num & 0xff);
    stream.write(sdata);
    stream.write(CR);
    stream.flush();
  }

  public static RawBinarySpinelMessage read(InputStream stream) throws IOException {
    int b = readChar(stream);
    if (b != PRE) {
      throw new SpinelException(
          String.format("PRE is expected, but %d was received!", b));
    }
    int frm = readChar(stream);
    int num = readChar(stream) * 0x100;
    num += readChar(stream);
    byte[] data = new byte[num];
    for (int i = 0; i < num - 1; i++) {
      data[i] = (byte)readChar(stream);
    }
    b = readChar(stream);
    if (b != CR) {
      throw new SpinelException("CR was expected!");
    }
    return new RawBinarySpinelMessage(frm, data);
  }

  private static int readChar(InputStream stream) throws IOException {
    int result = stream.read();
    if (result < 0) {
      throw new java.io.EOFException();
    } else {
      return result;
    }
  }


  public byte[] getData() {
    return sdata;
  }

}
