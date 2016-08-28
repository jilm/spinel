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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jilm
 */
public class PhysicalPeerTest {

  SpinelMessage response = new SpinelMessage(55, 44);

  public PhysicalPeerTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    new Thread(new Runnable(){
      @Override
      public void run() {
        try (
          ServerSocket server = new ServerSocket(12345);
          Socket client = server.accept();
          SpinelInputStream is = new SpinelInputStream(client.getInputStream());
          SpinelOutputStream os = new SpinelOutputStream(client.getOutputStream());
        ) {
          SpinelMessage request = is.readMessage();
          os.write(response);
        } catch (IOException ex) {
        }
      }
    }).start();
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of close method, of class PhysicalPeer.
   */
  @Test
  @Ignore
  public void testClose() {
    System.out.println("close");
    PhysicalPeer instance = null;
    instance.close();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of putRequest method, of class PhysicalPeer.
   */
  @Test(timeout=300)
  public void testPutRequest() {
    System.out.println("putRequest");
    SpinelMessage request = new SpinelMessage(12, 13);
    PhysicalPeer instance = new PhysicalPeer("localhost", 12345);
    //Transaction expResult = null;
    Transaction result = instance.putRequest(request);
    assertEquals(request, result.getRequest());
    SpinelMessage resp;
    try {
      resp = result.get(200L);
      System.out.println(resp.toString());
      System.out.println(response.toString());
      System.out.println(resp.equals(response));
      assertEquals(response, resp);
    } catch (TimeoutException ex) {
      fail();
    }
  }

  /**
   * Test of toString method, of class PhysicalPeer.
   */
  @Test
  @Ignore
  public void testToString() {
    System.out.println("toString");
    PhysicalPeer instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
