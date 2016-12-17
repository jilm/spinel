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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * This object provides some sort of spinel communication router. It listends
 * on the specified port for spinel messages. Than it finds appropriate
 * handler in the internal table to reply to the obtained message. The lookup
 * is based on the given spinel address.
 *
 * It allows to virtualize devices which communicates over the spinel protocol.
 * It accepts connections on some given port. New handler is created for each
 * connection. Such a handler reads spinel message, finds appropriate physical
 * peer which could respond received message, translate the message, hand it
 * over, waits for response, translate it back, and send it.
 *
 *
 */
public class SpinelD implements Runnable {

  /**
   * The table to find a physical peer, that can reply to a request with
   * specified virtual address. Index into the array is the virtual address.
   * It contains null for virtual addresses that are not used.
   */
  private final Handler[] clients;

  /**
   * The translate table between the physical and virtual address. The index
   * represents the virtual address, whereas the content is appropriate
   * physical address. Unused virtual addresses contains negative number.
   */
  private final int[] addressTable;

  /** The port the server listen to. */
  private final int port;

  /**
   * Application logger.
   */
  public static final Logger logger = Logger.getLogger("cz.lidinsky.spinel");

  /**
   * Messages.
   */
  public static final ResourceBundle messages
      = ResourceBundle.getBundle("cz/lidinsky/spinel/messages");

  /** requirement to stop. */
  private boolean stop;

  private final Map<InetSocketAddress, PhysicalPeer> peers;

  /**
   * Initialize internal state. This object is singleton, private constructor
   * prevents instance creation. Use getInstance instead.
   *
   * @param port
   *            port the server listen to
   */
  public SpinelD(final int port) {
    stop = false;
    //messages = ResourceBundle.getBundle("cz/lidinsky/spinel/messages");
    clients = new Handler[256];
    Arrays.fill(clients, null);
    addressTable = new int[256];
    Arrays.fill(addressTable, -1);
    this.port = port;
    this.peers = new HashMap<>();
  }

  /**
   * Runs the application.
   *
   * @param args the command line arguments
   *
   * @throws java.lang.Exception
   */
  public static void main(String[] args) throws Exception {

    // load the configuration
    Properties configuration = new Properties();
    configuration.load(new java.io.FileInputStream(args[0]));

    // create and configure deamon instance
    int port = Integer.parseInt(configuration.getProperty("port", "12340"));
    SpinelD instance = new SpinelD(port);
    //instance.logger
    //    = Logger.getLogger(
    //        configuration.getProperty("logger", "cz.lidinsky.spinel"));

    // create list of clients and translate table
    //Map<InetSocketAddress, PhysicalPeer> clientMap = new HashMap<>();
    for (String key : configuration.stringPropertyNames()) {
      if (key.startsWith("virtual[")) {
        String[] values = configuration.getProperty(key).split(",");
        int virtualAddress = Integer.parseInt(values[0].trim());
        String host = values[1].trim();
        int remotePort = Integer.parseInt(values[2].trim());
        int physicalAddress = Integer.parseInt(values[3].trim());
        instance.createRule(virtualAddress, physicalAddress, host, remotePort);
//        instance.addressTable[virtualAddress] = physicalAddress;
//        InetSocketAddress socketAddress = new InetSocketAddress(host, remotePort);
//        if (!clientMap.containsKey(socketAddress)) {
//          clientMap.put(socketAddress, new PhysicalPeer(host, remotePort));
//        }
//        instance.clients[virtualAddress] = clientMap.get(socketAddress);
      }
    }
//    System.out.println("physical peers: ");
//    clientMap.values().stream().map(peer -> peer.toString()).forEach(System.out::println);
    // start the daemon
    instance.start();
  }

  protected synchronized void createRule(
      int virtualAddress, int physicalAddress, Handler peer) {

    addressTable[virtualAddress] = physicalAddress;
    clients[virtualAddress] = peer;
  }

  public synchronized void createRule(
      int virtualAddress, int physicalAddress, String host, int port) throws UnknownHostException {

    // first of all, take a look if there is a peer with given host and port
    InetAddress inetAddress = InetAddress.getByName(host);
    InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
    PhysicalPeer peer = peers.get(inetSocketAddress);
    if (peer != null) {
      createRule(virtualAddress, physicalAddress, peer);
    } else {
      // if not create one
      peer = new PhysicalPeer(inetSocketAddress);
      createRule(virtualAddress, physicalAddress, peer);
      peers.put(inetSocketAddress, peer);
    }
  }

  /**
   * Starts the main thread of the application.
   */
  private void start() {
    logger.info(messages.getString("APPLICATION_START"));
    // start the server loop
    new Thread(this::run, "Server thread").start();
  }

  /**
   * Server loop, it listens on the given port and a new VirtualPeer instance is
   * created for each incoming connection. This method is intende to be run
   * as a separate thred. Once this thread is terminated, the whole application
   * is stopped.
   */
  @Override
  public void run() {
    try (
        ServerSocket server = new ServerSocket(port);) {
      logger.info(
          String.format(
              messages.getString("SERVER_START"), port));
      while (!stop) {
        Socket socket = server.accept();
        new VirtualPeer(socket, this).start();
      }
    } catch (IOException e) {
      logger.severe(
          String.format(
              messages.getString("SERVER_EXCEPTION"), e.toString()));
    } finally {
      stop = true;
      logger.info(messages.getString("SERVER_END"));
    }
  }

  /**
   *
   * @param request
   *            a request from the virtual peer
   *
   * @return
   */
  Transaction putRequest(SpinelMessage request) {

    int virtualAddress = request.getAdr();
    // translate it
    int physicalAddress = addressTable[virtualAddress];
    if (physicalAddress < 0 || physicalAddress > 255) {
      // unsupported virtual address
      SpinelD.logger.warning(
          String.format(
              SpinelD.messages.getString("UNSUPPORTED_VIRTUAL_ADDRESS"),
              virtualAddress));
      // TODO: send reply
      // return transaction which wont be replied
      return new Transaction(request);
    } else {
      // int sig = storeTransaction(transaction);
      // send it
      Handler client = clients[virtualAddress];
      return new TransformedTransaction(
          client.putRequest(request.modify(physicalAddress, 0)),
          message -> message.modify(virtualAddress, request.getSig()));
    }
  }

  /**
   * Transform a message from virtual peer into the message form physical peer.
   *
   * @param message
   * @return
   */
  protected SpinelMessage virtual2physical(SpinelMessage message) {
    int virtualAddress = message.getAdr();
    // translate it
    int physicalAddress = addressTable[virtualAddress];
    if (physicalAddress < 0 || physicalAddress > 255) {
      // unsupported virtual address
      throw new IndexOutOfBoundsException(
        String.format(
              SpinelD.messages.getString("UNSUPPORTED_VIRTUAL_ADDRESS"),
              virtualAddress));
    } else {
      return message.modify(physicalAddress, 0);
    }
  }

  public void close() {
    stop = true;
    try (
      Socket socket =  new Socket("localhost", port);
        )
    {} catch (IOException e) {}
  }

}
