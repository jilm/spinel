/*
 * Copyright (C) 2016 jilm
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

import java.net.Socket;

/**
 * A simple command-line tool that allows you to send one command over the
 * spinel protocol.
 *
 * <p>The command line arguments are as follows
 * <pre>host port address instruction</pre>
 */
public class Spinel {

  public static void main(String[] args) throws Exception {

    // command line arguments
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    int address = Integer.getInteger(args[2]);
    int instr = Integer.getInteger(args[3]);

    // create request message
    SpinelMessage request = new SpinelMessage(address, instr);
    System.out.println(request.toString());

    // create socket
    try (
        Socket socket = new Socket(host, port);
        SpinelInputStream is
            = new SpinelInputStream(socket.getInputStream());
        SpinelOutputStream os
            = new SpinelOutputStream(socket.getOutputStream());
        ) {
      os.write(request);
      SpinelMessage response = is.readMessage();
      System.out.println(response.toString());
    }
  }

}
