/*
 *  Copyright 2013 Jiri Lidinsky
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

import java.io.IOException;
import java.io.InputStream;

/**
 *  Input stream which reads data in spinel format.
 */
public class SpinelInputStream extends InputStream {
//extends BufferedInputStream {

  private final int[] buffer = new int[255];

  private final InputStream is;

  /**
   *  Create and initialize new input stream.
   *
   *  @param inputStream
   *             underlying input stream
   */
  public SpinelInputStream(InputStream inputStream) {
    //super(inputStream, 255);
    //super(inputStream);
    is = inputStream;
  }

  /**
   *  Reades and returns one spinel message from underlying
   *  input stream. This method blocks.
   *
   *  @return received spinel message
   *
   *  @throws IOException
   *              if something went wrong
   */
  public SpinelMessage readMessage() throws IOException {
    //mark(200);
    // receive a header of the message
    for (int i=0; i<4; i++) {
      buffer[i] = read();
    }
    int num = ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
    // receive the rest of the message
    for (int i=4; i<num+4 && i<255; i++) {
      buffer[i] = read();
    }
    return new SpinelMessage(buffer, 0, num+4);
  }

  @Override
  public int read() throws IOException {
    int result = is.read();
    if (result < 0) {
      throw new java.io.EOFException();
    } else {
      return result;
    }
  }

}
