/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
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
   * The table to find a physical address to the given virtual address.
   */
  private final int[] addressTable;

  /**
   * Initialize internal state. This object is singleton, private constructor
   * prevents instance creation.
   */
  private SpinelD() {
    stop = false;
    messages = ResourceBundle.getBundle("cz/lidinsky/spinel/messages");
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
    instance = new SpinelD();
    instance.port
        = Integer.parseInt(configuration.getProperty("port", "12340"));
    instance.logger
        = Logger.getLogger(
            configuration.getProperty("logger", "cz.lidinsky.spinel"));

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
    // start the daemon
    instance.start();
  }

  /**
   *
   */
  static ResourceBundle getMessages() {
    return instance.messages;
  }

  /**
   * Returns application logger.
   */
  static Logger getLogger() {
    return instance.logger;
  }

  /**
   * Application logger.
   */
  private Logger logger;

  private final ResourceBundle messages;

  /**
   * The only instance of this object.
   */
  private static SpinelD instance;

  /**
   * Returns the only instance of this object.
   */
  static SpinelD getInstance() {
    return instance;
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
   * Server loop, it listens on the given port and a new ViwrualPeer instance is
   * created for each incoming connection. This method is run as a separate
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

  private class TransformedTransaction extends Transaction {

    private Function<SpinelMessage, SpinelMessage> transform;

    private Transaction transaction;

    TransformedTransaction(Transaction transaction, Function<SpinelMessage, SpinelMessage> transform) {
      super(transaction.getRequest());
      this.transform = transform;
      this.transaction = transaction;
    }

    @Override
    void put(SpinelMessage message) {
      transaction.put(transform.apply(message));
    }

    @Override
    public SpinelMessage get(long timeout) throws TimeoutException {
      return transaction.get(timeout);
    }
  }

}