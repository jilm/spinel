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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Entry point of the application. This object is singleton.
 */
public class SpinelD {

  /**
   * The table to find a physical peer, that can reply to a request with
   * specified virtual address. Index into the array is the virtual address. It
   * contains null for virtual addresses that are not used.
   */
  private final PhysicalPeer[] clients;

  /**
   * The translate table between the physical and virtual address. The index
   * represents the virtual address, whereas the content is appropriate
   * physical address. Unused virtual addresses contains negative number.
   */
  private final int[] addressTable;

  /**
   * Initialize internal state. This object is singleton, private constructor
   * prevents instance creation. Use getInstance instead.
   */
  private SpinelD() {
    stop = false;
    //messages = ResourceBundle.getBundle("cz/lidinsky/spinel/messages");
    clients = new PhysicalPeer[256];
    Arrays.fill(clients, null);
    addressTable = new int[256];
    Arrays.fill(addressTable, -1);
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
    instance = SpinelD.getInstance();
    instance.port
        = Integer.parseInt(configuration.getProperty("port", "12340"));
    //instance.logger
    //    = Logger.getLogger(
    //        configuration.getProperty("logger", "cz.lidinsky.spinel"));

    // create list of clients and translate table
    Map<InetSocketAddress, PhysicalPeer> clientMap = new HashMap<>();
    for (String key : configuration.stringPropertyNames()) {
      if (key.startsWith("virtual[")) {
        String[] values = configuration.getProperty(key).split(",");
        int virtualAddress = Integer.parseInt(values[0].trim());
        String host = values[1].trim();
        int port = Integer.parseInt(values[2].trim());
        int physicalAddress = Integer.parseInt(values[3].trim());
        instance.addressTable[virtualAddress] = physicalAddress;
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        if (!clientMap.containsKey(socketAddress)) {
          clientMap.put(socketAddress, new PhysicalPeer(host, port));
        }
        instance.clients[virtualAddress] = clientMap.get(socketAddress);
      }
    }
    System.out.println("physical peers: ");
    clientMap.values().stream().map(peer -> peer.toString()).forEach(System.out::println);
    // start the daemon
    instance.start();
  }

  protected void createRule(
      int virtualAddress, int physicalAddress, PhysicalPeer peer) {

    addressTable[virtualAddress] = physicalAddress;
    clients[virtualAddress] = peer;
  }

  /**
   *
   */
  static ResourceBundle getMessages() {
    return getInstance().messages;
  }

  /**
   * Returns application logger.
   */
  static Logger getLogger() {
    return logger;
  }

  /**
   * Application logger.
   */
  public static final Logger logger = Logger.getLogger("cz.lidinsky.spinel");

  public static final ResourceBundle messages = ResourceBundle.getBundle("cz/lidinsky/spinel/messages");

  /**
   * The only instance of this object.
   */
  private static SpinelD instance;

  /**
   * Returns the only instance of this object.
   */
  static SpinelD getInstance() {
    return instance == null ? new SpinelD() : instance;
  }

  /**
   * Port on which the program listens.
   */
  private int port;

  private boolean stop;

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
   * created for each incoming connection. This method runs as a separate
   * thred. Once this thread is terminated, the whole application is stopped.
   */
  private void run() {
    try (
        ServerSocket server = new ServerSocket(port);) {
      logger.info(
          String.format(
              messages.getString("SERVER_START"), port));
      while (!stop) {
        Socket socket = server.accept();
        new VirtualPeer(socket).start();
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
      SpinelD.getLogger().warning(
          String.format(
              SpinelD.getMessages().getString("UNSUPPORTED_VIRTUAL_ADDRESS"),
              virtualAddress));
      // TODO: send reply
      // return transaction which wont be replied
      return new Transaction(request);
    } else {
      // int sig = storeTransaction(transaction);
      // send it
      PhysicalPeer client = clients[virtualAddress];
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
              SpinelD.getMessages().getString("UNSUPPORTED_VIRTUAL_ADDRESS"),
              virtualAddress));
    } else {
      return message.modify(physicalAddress, 0);
    }
  }

}
