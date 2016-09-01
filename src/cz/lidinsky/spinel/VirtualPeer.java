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

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

/**
 *
 */
class VirtualPeer {

  /**
   * Socket on which the peer comunnicates with the client.
   */
  private final Socket socket;

  /**
   * A flag which indicates that the socket was closed and this peer is no
   * longer active.
   */
  private boolean closed;

  private final int timeout;

  /**
   * Simply count number of virtual peers, it is used for just reporting
   * purposis.
   */
  private static int counter = 0;

  VirtualPeer(Socket socket) {
    this.socket = socket;
    this.closed = false;
    this.timeout = 2157;
    SpinelD.getLogger().info(
        String.format("New virtual peer was created; number of peers: %d",
            ++counter));
  }

  void start() {
    Thread thread = new Thread(this::run);
    thread.setDaemon(false);
    thread.setName("Virtual peer thread");
    thread.start();
  }

  void run() {
    try (
        SpinelInputStream is
          = new SpinelInputStream(socket.getInputStream());
        SpinelOutputStream os
          = new SpinelOutputStream(socket.getOutputStream());) {

      SpinelD.getLogger().info("Going to start wirtual peer loop");

      while (!closed) {
        try {
          // wait for request
          SpinelMessage request = is.readMessage();
          SpinelD.getLogger().fine("Request received");
          // hand it over
          SpinelMessage response = process(request);
          SpinelD.getLogger().fine("response received");
          // send it
          os.write(response);
        } catch (TimeoutException ex) {
          SpinelD.getLogger().fine("timeout");
          //
        }
      }
    } catch (java.io.EOFException ex) {
    } catch (IOException ex) {
      SpinelD.getLogger().severe(
          String.format("An exception was catched in the virtual peer! %s",
              ex.getMessage()));
    } finally {
      closed = true;
      SpinelD.getLogger().info(
          String.format("Virtual peer going to stop; number of peers: %d",
              --counter));
    }
  }

  protected SpinelMessage process(SpinelMessage request) throws TimeoutException {
          // hand it over
          Transaction transaction = SpinelD.getInstance().putRequest(request);
          SpinelD.getLogger().fine("Request sent to the virtual peer");
          // wait for response
          SpinelMessage response = transaction.get(timeout);
          return response;
  }

}
