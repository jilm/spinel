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

import java.util.ResourceBundle;
import java.util.logging.Logger;
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
public class SpinelDTest {

  public SpinelDTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  @Ignore
  public void testCreateRule() {
    System.out.println("createRule");
    int virtualAddress = 0;
    int physicalAddress = 0;
    PhysicalPeer peer = null;
    SpinelD instance = null;
    instance.createRule(virtualAddress, physicalAddress, peer);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getMessages method, of class SpinelD.
   */
  @Test
  @Ignore
  public void testGetMessages() {
    System.out.println("getMessages");
    ResourceBundle expResult = null;
    ResourceBundle result = SpinelD.getMessages();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getLogger method, of class SpinelD.
   */
  @Test
  @Ignore
  public void testGetLogger() {
    System.out.println("getLogger");
    Logger expResult = null;
    Logger result = SpinelD.getLogger();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getInstance method, of class SpinelD.
   */
  @Test
  @Ignore
  public void testGetInstance() {
    System.out.println("getInstance");
    SpinelD expResult = null;
    SpinelD result = SpinelD.getInstance();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of putRequest method, of class SpinelD.
   */
  @Test
  @Ignore
  public void testPutRequest() {
    System.out.println("putRequest");
    SpinelMessage request = null;
    SpinelD instance = null;
    Transaction expResult = null;
    Transaction result = instance.putRequest(request);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of virtual2physical method, of class SpinelD.
   */
  @Test
  public void testVirtual2physical() {
    System.out.println("virtual2physical");
    SpinelD instance = SpinelD.getInstance();
    instance.createRule(10, 20, new PhysicalPeer("localhost", 12345));
    SpinelMessage message = new SpinelMessage(10, 3);
    SpinelMessage expResult = new SpinelMessage(20, 3);
    SpinelMessage result = instance.virtual2physical(message);
    assertEquals(expResult, result);
  }

}
