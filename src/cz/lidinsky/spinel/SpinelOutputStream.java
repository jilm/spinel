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

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 *  Output stream which writes spinel messages.
 */
public class SpinelOutputStream 
extends BufferedOutputStream {

  /**
   *  Create and initialize the output stream.
   *
   *  @param outputStream
   *             underlying output stream
   */
  public SpinelOutputStream(OutputStream outputStream) {
    super(outputStream);
  }

  /**
   *  Writes given spinel message into the underlying output stream.
   *
   *  @param message
   *             message to send
   *
   *  @throws IOException 
   *             if somethig went wrong
   */
  public void write(SpinelMessage message) throws IOException {
    int length = message.length();
    for (int i=0; i<length; i++)
      write(message.get(i));
    flush();
  }
}
