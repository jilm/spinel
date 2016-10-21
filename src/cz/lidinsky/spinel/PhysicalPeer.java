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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It communicates with some device or devices over the spinel protocol. All
 * of the devices must share the same host and port.
 */
public class PhysicalPeer implements Handler {

  /**
   * Indicate that the connection is not estabilished and the communication
   * loop is not running.
   */
  private volatile boolean closed;

  /**
   * The requirement to stop the loop and to close the socket.
   */
  private volatile boolean stop;

  /**
   * Waiting requests.
   */
  private final BlockingQueue<Transaction> queue;

  private final InetSocketAddress inetSocketAddress;

  /** Logger. */
  private static final Logger logger;

  /**
   * Translate table between virtual and physical spinel addresses. The index
   * of the element represents virtual and the content of the element
   * represents appropriate physical address. Virtual addresses that are
   * not used contain negative number.
   */
  //private final int[] addressMap;

  static {
    logger = Logger.getLogger(PhysicalPeer.class.getName());
    logger.setResourceBundle(
        ResourceBundle.getBundle("cz/lidinsky/spinel/messages"));
  }

  /**
   * Initialize internal structures.
   *
   * @param host
   * @param port
   */
  public PhysicalPeer(String host, int port) {
    this(new InetSocketAddress(host, port));
  }

  public PhysicalPeer(InetSocketAddress inetSocketAddress) {
    this.inetSocketAddress = inetSocketAddress;
    this.queue = new LinkedBlockingQueue();
    this.closed = true;
    this.stop = false;
  }

  /**
   * Intended to be run in the separate thread.
   */
  private void run() {
    logger.info("PHYSICAL_PEER_START");
    closed = false;
    Transaction transaction = null;
    do {
      try { transaction = queue.take(); } catch (InterruptedException ex) {}
      if (stop) return;
    } while (transaction == null);
    try (
        Socket socket = new Socket(
            inetSocketAddress.getAddress(), inetSocketAddress.getPort());
        SpinelInputStream is
          = new SpinelInputStream(socket.getInputStream());
        SpinelOutputStream os
          = new SpinelOutputStream(socket.getOutputStream());
        ) {
      logger.log(Level.INFO, "PHYSICAL_PEER_CE",
          new Object[] {inetSocketAddress.getHostName(), inetSocketAddress.getPort()});
      socket.setSoTimeout(987);
      while (!stop) {
        SpinelMessage request = transaction.getRequest();
        os.write(request);
        SpinelMessage response = is.readMessage();
        transaction.put(response);
        transaction = null;
        do {
          try { transaction = queue.take(); } catch (InterruptedException ex) {}
          if (stop) return;
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

      // TODO: write error response
    } finally {
      closed = true;
      SpinelD.logger.info("PHYSICAL_PEER_STOP");
    }
  }

  /**
   * Starts requests handling in the separate thread.
   */
  private void start() {
    if (closed) {
      closed = false;
      new Thread(this::run).start();
    }
  }

  /**
   * Request to stop the handling thread and close the socket.
   */
  @Override
  public void close() {
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
  @Override
  public Transaction putRequest(SpinelMessage request) {
    Transaction transaction = new Transaction(request);
    queue.add(transaction);
    start();
    return transaction;
  }

  @Override
  public String toString() {
    if (closed) {
      return String.format(
          "A closed physical peer object, host: %s, port: %d",
          inetSocketAddress.getHostName(),
          inetSocketAddress.getPort());
    } else {
      return String.format(
          "An opened physical peer object, host: %s, port: %d, requests: %d",
          inetSocketAddress.getHostName(),
          inetSocketAddress.getPort(), queue.size());
    }
  }

}
