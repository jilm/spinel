/*
 *  Copyright 2016 Jiri Lidinsky
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

import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Communicates with some server (data provider) via the spinel protocol. The
 * request message is placed into the input queue. Than it is sent to the spinel
 * server.
 */
public class PhysicalPeer {

  /**
   * Indicate that the connection is not estabilished and the communication loop
   * is not running.
   */
  private volatile boolean closed;

  private volatile boolean stop;

  private final BlockingQueue<Transaction> queue;

  private final String host;

  private final int port;

  private static final Logger logger;

  static {
    logger = Logger.getLogger(PhysicalPeer.class.getName());
    logger.setResourceBundle(
        ResourceBundle.getBundle("cz/lidinsky/spinel/messages"));
  }

  public PhysicalPeer(String host, int port) {
    this.queue = new LinkedBlockingQueue();
    this.host = host;
    this.port = port;
    this.closed = true;
    this.stop = false;
  }

  private void run() {
    logger.info("PHYSICAL_PEER_START");
    closed = false;
    Transaction transaction = null;
    do {
      try { transaction = queue.take(); } catch (InterruptedException ex) {}
    } while (transaction == null);
    try (
        Socket socket = new Socket(host, port);
        SpinelInputStream is
        = new SpinelInputStream(socket.getInputStream());
        SpinelOutputStream os
        = new SpinelOutputStream(socket.getOutputStream());
        ) {
      logger.log(Level.INFO, "PHYSICAL_PEER_CE", new Object[] {host, port});
      socket.setSoTimeout(987);
      while (!stop) {
        SpinelMessage request = transaction.getRequest();
        os.write(request);
        SpinelMessage response = is.readMessage();
        transaction.put(response);
        transaction = null;
        do {
          try { transaction = queue.take(); } catch (InterruptedException ex) {}
        } while (transaction == null);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "PHYSICAL_PEER_EXCEPTION", e);
      while (transaction != null) {
        transaction.put(
            transaction.getRequest().getAckMessage(
                SpinelMessage.ACK_AUTO_OTHER));
        transaction = queue.poll();
      }

      // TODO: log exception
      // TODO: write error response
    } finally {
      closed = true;
      SpinelD.getLogger().info("PHYSICAL_PEER_STOP");
    }
  }

  private void start() {
    if (closed) {
      closed = false;
      new Thread(this::run).start();
    }
  }

  void close() {
    stop = true;
  }

  /**
   * Wrap up the given request by the transaction object and insert it into
   * the internal queue. The request is then send as soon as the preceded
   * requests are finished.
   *
   * @param request
   *            request to send
   *
   * @return transaction object through which the response coudl be obtained
   */
  public Transaction putRequest(SpinelMessage request) {
    Transaction transaction = new Transaction(request);
    queue.add(transaction);
    start();
    return transaction;
  }

  @Override
  public String toString() {
    return String.format("A physical peer object, host: %s, port: %d", host, port);
  }

}
