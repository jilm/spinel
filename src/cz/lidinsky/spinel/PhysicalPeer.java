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
 * Encapsulates communication over the spinel protocol. Incoming request
 * messages are placed in the queue and send as soon as it's their go.
 *
 */
public class PhysicalPeer implements Handler {

  public enum Status {

    /**
     * After initialization (constructor), connection is not estabilished,
     * send receive loop is not working. It is just the start state.
     */
    INIT,

    /**
     * from INIT, after the message is put;
     * from ERROR, after the message is put;
     * the object is trying to estabilish the connection.
     */
    CONNECTING,

    /**
     * from CONNECTING after the connection was opened.
     */
    SEND_RECEIVE_LOOP,

    /**
     * from any state after the close method has been called. This is the
     * terminal state.
     */
    CLOSED,

    /**
     * from CONNECTING, after the making connection process fails;
     * from SEND_RECEIVE_LOOP after some exception. In this state, object
     * is waiting form some time.
     */
    FAILED,

    /**
     * from FAILED after some timeout.
     */
    ERROR;
  }

  /**
   * The requirement to stop the loop and to close the socket.
   */
  private volatile boolean stop;

  /**
   * Status of the object.
   */
  private volatile Status status;

  /**
   * Waiting requests.
   */
  private final BlockingQueue<Transaction> queue;

  /**
   * An address of the peer to communicate with.
   */
  private final InetSocketAddress inetSocketAddress;

  /** Logger. */
  private static final Logger logger;

  /** An identification of this object for log purposes. */
  private final String identification;

  /** If true, indicates that connecting process failed last time. It is used
      not to log another connecting failure. */
  private boolean connectingFailureLogged;

  /** The latest catched exception. */
  private Exception lastException;

  static {
    logger = Logger.getLogger(PhysicalPeer.class.getName());
    logger.setResourceBundle(
        ResourceBundle.getBundle("cz/lidinsky/spinel/messages"));
  }

  /**
   * Initialize internal structures.
   *
   * @param host
   *            host name of the remote server
   *
   * @param port
   *            port number
   */
  public PhysicalPeer(String host, int port) {
    this(new InetSocketAddress(host, port));
  }

  public PhysicalPeer(InetSocketAddress inetSocketAddress) {
    this.status = Status.INIT;
    this.inetSocketAddress = inetSocketAddress;
    this.queue = new LinkedBlockingQueue();
    this.stop = false;
    this.identification = String.format(
      "An instance of class: %s; host: %s; port %d",
      PhysicalPeer.class.getName(),
      inetSocketAddress.getHostName(),
      inetSocketAddress.getPort());
    this.connectingFailureLogged = false;
  }

  /**
   * Returns the latest catched exception.
   *
   * @return the latest catched exception or null
   */
  public Exception getLatestException() {
    return lastException;
  }

  /**
   * Change status to connecting.
   */
  private void connecting() {
    if (status != Status.CONNECTING) {
      status = Status.CONNECTING;
      if (!connectingFailureLogged) {
        logger.log(Level.INFO, "PP_CONNECTING",
            new Object[] {
              inetSocketAddress.getHostName(), inetSocketAddress.getPort()});
      }
    }
  }

  /**
   * Change status to fail.
   */
  private void fail(Exception ex) {
    if (status == Status.CONNECTING) {
      this.status = Status.FAILED;
      if (!connectingFailureLogged) {
        logger.log(Level.SEVERE, "PP_CONNECTING_FAILED",
            new Object[] {
              inetSocketAddress.getHostName(), inetSocketAddress.getPort()});
        connectingFailureLogged = true;
        lastException = ex;
      }
    } else if (status == Status.SEND_RECEIVE_LOOP) {
      this.status = Status.FAILED;
      logger.severe("An exception inside the send receive loop");
      lastException = ex;
    }
  }

  /**
   * Change status to closed.
   */
  private void closed() {
    if (status != Status.CLOSED) {
      this.status = Status.CLOSED;
      logger.info(identification + "has been closed.");
    }
  }

  /**
   * Change status to error.
   */
  private void error() {
    if (status != Status.ERROR) {
      this.status = Status.ERROR;
    }
  }

  /**
   * Changle status to send receive loop.
   */
  private void loop() {
    this.status = Status.SEND_RECEIVE_LOOP;
  }

  /**
   * Send, receive loop; Intended to be run in the separate thread.
   */
  private void run() {

    // create connection
    connecting();
    try (
        Socket socket = new Socket(
            inetSocketAddress.getAddress(),
            inetSocketAddress.getPort());
        SpinelInputStream is
            = new SpinelInputStream(socket.getInputStream());
        SpinelOutputStream os
            = new SpinelOutputStream(socket.getOutputStream());
    ) {

      // connection was created, enter the request, response loop
      loop();
      socket.setSoTimeout(987);
      while (!stop) {
        Transaction transaction = get();
        if (stop) break;
        SpinelMessage request = transaction.getRequest();
        os.write(request);
        SpinelMessage response = is.readMessage();
        transaction.put(response);
      }
      closed();
    } catch (Exception e) {
      fail(e);
    } finally {
      logger.fine("Leaving the request, response loop.");
    }
  }

  /**
   * Returns transaction which should be handled next.
   *
   * @return transaction to handle
   */
  private Transaction get() {
    Transaction transaction = null;
    while (transaction == null) {
      try {
        transaction = queue.take();
      } catch (InterruptedException ex) {
        // it is ok, we are just waitng for some transaction to handle
      }
      if (stop) return null;
    }
    return transaction;
  }


  /**
   * Starts requests handling in the separate thread.
   */
  private void start() {
    new Thread(this::run).start();
  }

  /**
   * Request to stop the handling thread and close the socket.
   */
  @Override
  public synchronized void close() {
    stop = true;
    if (status == Status.INIT || status == Status.ERROR) {
      closed();
    }
  }

  /**
   * Wrap up the given request by the transaction object and insert it into
   * the internal queue. The request is then send as soon as the preceded
   * requests are finished.
   *
   * @param request
   *            request to send
   *
   * @return transaction object through which the response could be obtained
   */
  @Override
  public synchronized Transaction putRequest(SpinelMessage request) {
    switch (status) {
      case INIT:
      case ERROR:
        Transaction transaction = new Transaction(request);
        queue.add(transaction);
        start();
        return transaction;
      case SEND_RECEIVE_LOOP:
      case CONNECTING:
        transaction = new Transaction(request);
        queue.add(transaction);
        return transaction;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public String toString() {
    switch (status) {
      case CLOSED:
        return "Closed object";
      case INIT:
        return "Initialized object, not connected, waits for a message to send.";
      case CONNECTING:
        return "Connecting";
      case SEND_RECEIVE_LOOP:
        return "Send receive loop";
      case ERROR:
        return "Last IO operation failed.";
      case FAILED:
        return "Last IO oepration failed.";
      default:
        return "The object is in undefined state.";
    }
  }

}
