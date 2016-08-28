/*
 *  Copyright 2013, 2016 Jiri Lidinsky
 *
 *  This file is part of control4j.
 *
 *  control4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3.
 *
 *  control4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with control4j.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.lidinsky.spinel;

import java.util.Arrays;

/**
 *  One message of the Spinel communication protocol. This class
 *  supports only the binary format 97.
 */
public class SpinelMessage {

  public static final int ACK_OK = 0x00;
  public static final int ACK_ERR = 0x01;
  public static final int ACK_BAD_INST = 0x02;
  public static final int ACK_BAD_DATA = 0x03;
  public static final int ACK_REFUSED = 0x04;
  public static final int ACK_HW_MALFUNCTION = 0x05;
  public static final int ACK_AUTO_DATA = 0x0C;
  public static final int ACK_AUTO_INPUT_CHANGED = 0x0D;
  public static final int ACK_AUTO_MEASUREMENT = 0x0E;
  public static final int ACK_AUTO_OTHER = 0x0F;

  /** Prefix */
  public static final int PRE = 0x2A;

  /** Format identification */
  public static final int FRM = 0x61;

  /** Message end character */
  public static final int CR = 0x0D;

  /** Address of the device */
  private int adr;

  /** Signature of the message */
  private int sig;

  /** Instruction number, or acknowledge */
  private int inst;

  /**
   * Message data
   */
  private int[] data;

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 11 * hash + this.adr;
    hash = 11 * hash + this.sig;
    hash = 11 * hash + this.inst;
    hash = 11 * hash + Arrays.hashCode(this.data);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SpinelMessage other = (SpinelMessage) obj;
    if (this.adr != other.adr) {
      return false;
    }
    if (this.sig != other.sig) {
      return false;
    }
    if (this.inst != other.inst) {
      return false;
    }
    return ((this.data == null || this.data.length == 0) && (other.data == null || other.data.length == 0))
        || Arrays.equals(this.data, other.data);
  }

  /** Broadcast address */
  public static final int ADR_BROADCAST = 0xFF;

  /** Universal address */
  public static final int ADR_UNIVERSAL = 0xFE;

  /**
   *  Create message with given parameters. SIG is set to zero.
   *
   *  @param address
   *             address of the device
   *
   *  @param instruction
   *             instruction code
   *
   *  @param data
   *             data of the message
   */
  public SpinelMessage(int address, int instruction, int[] data) {
    this(address, instruction, data, 0, data.length);
  }

  public SpinelMessage(
      int address, int instruction, int[] data, int offset, int length) {
    this.adr = address;
    this.sig = 0;
    this.inst = instruction;
    this.data = new int[length];
    System.arraycopy(data, offset, this.data, 0, length);
  }

  /**
   *  Create message with given parameters, without the data.
   *  SIG is set to zero.
   *
   *  @param address
   *             address of the device
   *
   *  @param instruction
   *             instruction or acknowledge code
   */
  public SpinelMessage(int address, int instruction) {
    this.adr = address;
    this.inst = instruction;
    this.data = null;
    this.sig = 0;
  }

  /**
   *  Create spinel message from received data. First of all, it
   *  verify that the message is valid and consistent with the
   *  specification. If isn't it throws exception.
   *
   *  @param buffer
   *             must contain complete spinel message
   *
   *  @param offset
   *             index of the first char of the message
   *
   *  @param length
   *             length of the message
   *
   *  @throws SpinelException
   *             if given message is not valid spinel message
   */
  public SpinelMessage(int[] buffer, int offset, int length)
  throws SpinelException {

    check(buffer, offset, length);
    this.adr = buffer[offset+4];
    this.sig = buffer[offset+5];
    this.inst = buffer[offset+6];
    int dataLength = length - 9;
    this.data = new int[dataLength];
    for (int i=0; i<dataLength; i++)
      data[i] = buffer[offset+7+i];
  }

  /**
   *  Returns one char of the message with index index. This method is
   *  useful when you want to send the message.
   *
   *  @param index
   *             index of the desired message character
   *
   *  @return a message character with the index index
   *
   *  @throws IndexOutOfBoundsException
   *             if index is less than zero or greater than the message
   *             size
   */
  public int get(int index)
  {
    if (index < 0)
      throw new IndexOutOfBoundsException();
    switch (index)
    {
      case 0:
        return PRE;
      case 1:
        return FRM;
      case 2:
        return (getNum() & 0xff00) >>> 8;
      case 3:
        return getNum() & 0xff;
      case 4:
        return adr;
      case 5:
        return sig;
      case 6:
        return inst;
      default:
        int dataLength = data == null ? 0 : data.length;
        if (index-7 < dataLength)
	  return data[index-7];
        else if (index-dataLength == 7)
	  return getSuma();
        else if (index-dataLength == 8)
	  return CR;
        else
	  throw new IndexOutOfBoundsException();
    }
  }

  /**
   *  Returns SIG character.
   *
   *  @return SIG character
   */
  public int getSig() {
    return this.sig;
  }

  /**
   *  Returns the address ADR.
   *
   *  @return the address
   */
  public int getAdr() {
    return this.adr;
  }

  /**
   *  Returns number of characters of the message NUM. It is the length
   *  of the SDATA part of the message plus one. The total number of
   *  the message is than NUM + 4.
   *
   *  @return number of characters NUM
   */
  public int getNum()
  {
    return 5 + (data == null ? 0 : data.length);
  }

  /**
   *  Returns check sum of the message.
   *
   *  @return check sum
   */
  public int getSuma()
  {
    int sum = 0xff - PRE - FRM;
    int num = getNum();
    sum = sum - ((num & 0xff00) >>> 8) - (num & 0xff);
    sum = sum - adr - sig - inst;
    if (data != null) {
      for (int i=0; i < data.length; i++) {
        sum -= data[i];
      }
    }
    return sum & 0xFF;
  }

  /**
   *  Returns instruction code (INST) or acknowledge (ACK).
   *
   *  @return INST or ACK code
   */
  public int getInst()
  {
    return this.inst;
  }

  /**
   *  Returns length of the whole message.
   *
   *  @return number of bytes of the whole message
   */
  public int length()
  {
    return 9 + (data == null ? 0 : data.length);
  }

  /**
   *  Returns hexadecimal representation of the message.
   *
   *  @return message in the hexadecimal format
   */
  @Override
  public String toString()
  {
    int size = length();
    StringBuilder buffer = new StringBuilder(size * 3);
    for (int i=0; i<size; i++)
    {
      int character = get(i);
      if (character < 0x10) buffer.append('0');
      buffer.append(Integer.toHexString(character));
      if (i < size-1) buffer.append(' ');
    }
    return buffer.toString();
  }

  /**
   *  Returns hexadecimal representation of the buffer where
   *  one character is enclosed in []. This method serves
   *  mainly for debug purposes.
   *
   *  @param emphasize
   *             index of the character which will be enclosed
   *             in the brackets. Index is normalized, it means
   *             that if emphasize is zero, character in the
   *             buffer with index offset will be emphasized.
   *             If the emphasize is less than zero or greater
   *             than length, nothing will be emphasized.
   *
   *  @param buffer
   *             array with message that will be converted to
   *             the string representation.
   *
   *  @param offset
   *             index of the first character in the buffer which
   *             will be converted into the string
   *
   *  @param length
   *             number of characters (elements) which will be
   *             converted into the string representation
   * @return
   *
   *  @throws IndexOutOfBoundsException
   *             if buffer is not big enough.
   */
  public static String toString(int emphasize, int[] buffer, int offset, int length)
  {
    offset = offset >= buffer.length ? buffer.length - 1 : offset;
    length = offset + length > buffer.length ? buffer.length - offset : length;
    StringBuilder sb = new StringBuilder(length * 3 + 2);
    for (int i=0; i<length; i++)
    {
      if (i == emphasize) sb.append('[');
      if (buffer[i+offset] < 0x10) sb.append('0');
      sb.append(Integer.toHexString(buffer[i+offset]));
      if (i == emphasize) sb.append(']');
      if (i < length-1) sb.append(' ');
    }
    return sb.toString();
  }

  /**
   *  Returns one element of the data with index index.
   *
   *  @param index
   *             zero based index
   *
   *  @return one data character with index index
   *
   *  @throws IndexOutOfBoundsException
   *             if index is less than zero or greater than the
   *             size of data array
   */
  public int getData(int index)
  {
    if (index < 0 || data == null || index >= data.length)
      throw new IndexOutOfBoundsException();
    else
      return data[index];
  }

  /**
   *  Returns number of data elements.
   *
   *  @return size of the data array
   */
  public int getDataLength()
  {
    return data == null ? 0 : data.length;
  }
  /**
   *  Check wheather the message is consistent with the spinel
   *  specification.
   *
   *  @param buffer
   *             contains the message
   *
   *  @param offset
   *             index of the first character of the message
   *
   *  @param length
   *             size of the message
   *
   *  @return code of the error
   */
  private static void check(int[] buffer, int offset, int length)
    throws SpinelException {

    // the shortest message has at least nine characters
    if (length < 9) {
      throw new SpinelException(
          String.format(
            SpinelD.getMessages().getString("SHORT"),
            toString(0, buffer, offset, length)));
    }

    // The first character of the message is PRE
    if (buffer[offset+0] != PRE) {
      throw new SpinelException(
          String.format(
            SpinelD.getMessages().getString("PRE"),
            toString(0, buffer, offset, length)));
    }

    // The second character is FRM
    if (buffer[offset+1] != FRM) {
      throw new SpinelException(
          String.format(
            SpinelD.getMessages().getString("FRM"),
            toString(1, buffer, offset, length)));
    }

    // The length of the message and number of characters must match
    int num = buffer[offset+2] * 0x100 + buffer[offset+3];
    if (num+4 > length) {
      throw new SpinelException(
          String.format(
            SpinelD.getMessages().getString("NUM"),
            toString(3, buffer, offset, length)));
    }

    // The check sum must match
    int sum = 0xFF;
    for (int i=0; i<num+2; i++)
      sum -= buffer[i+offset] & 0xff;
    sum = sum & 0xFF;
    if (buffer[offset+num+2] != sum) {
      throw new SpinelException(
          String.format(
            SpinelD.getMessages().getString("SUM"),
            toString(length-2, buffer, offset, length)));
    }

    // The last character of the message is CR
    if (buffer[offset+num+3] != CR) {
      throw new SpinelException(
          String.format(
            SpinelD.getMessages().getString("CR"),
            toString(length-1, buffer, offset, length)));
    }
  }

  public SpinelMessage modify(int address, int sig) {
    return new SpinelMessage(this, address, sig);
  }

  private SpinelMessage(SpinelMessage message, int address, int sig) {
    this.adr = address;
    this.sig = sig;
    this.inst = message.inst;
    this.data = message.data;
  }

  public SpinelMessage getAckMessage(int ackCode) {
    SpinelMessage ackMessage = new SpinelMessage(adr, ackCode);
    ackMessage.sig = sig;
    return ackMessage;
  }

  public SpinelMessage getAckMessage(int ackCode, int[] data) {
    SpinelMessage message = new SpinelMessage(this.getAdr(), ackCode, data);
    message.sig = this.sig;
    return message;
  }

}
