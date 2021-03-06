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

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author jilm
 */
public class Simulator {

  public static void main(String[] args) throws Exception {
    ServerSocket server = new ServerSocket(12341);
    Socket socket = server.accept();
    VirtualPeer peer = new VirtualPeer(socket, null) {
      @Override
      protected SpinelMessage process(SpinelMessage request) {
        SpinelMessage response = request.getAckMessage(SpinelMessage.ACK_BAD_INST);
        return response;
      }
    };
    peer.start();
  }

}
