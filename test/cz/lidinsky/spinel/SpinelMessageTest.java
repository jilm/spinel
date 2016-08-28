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
public class SpinelMessageTest {

  public SpinelMessageTest() {
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

  /**
   * Test of hashCode method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testHashCode() {
    System.out.println("hashCode");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.hashCode();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of equals method, of class SpinelMessage.
   */
  @Test
  public void testEquals() {
    System.out.println("equals");
    Object obj = new SpinelMessage(55, 44);
    SpinelMessage instance = new SpinelMessage(55, 44, new int[0]);
    boolean expResult = true;
    boolean result = instance.equals(obj);
    assertEquals(expResult, result);
  }

  /**
   * Test of get method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGet() {
    System.out.println("get");
    int index = 0;
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.get(index);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getSig method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetSig() {
    System.out.println("getSig");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getSig();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getAdr method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetAdr() {
    System.out.println("getAdr");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getAdr();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getNum method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetNum() {
    System.out.println("getNum");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getNum();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getSuma method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetSuma() {
    System.out.println("getSuma");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getSuma();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getInst method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetInst() {
    System.out.println("getInst");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getInst();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of length method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testLength() {
    System.out.println("length");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.length();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testToString_0args() {
    System.out.println("toString");
    SpinelMessage instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testToString_4args() {
    System.out.println("toString");
    int emphasize = 0;
    int[] buffer = null;
    int offset = 0;
    int length = 0;
    String expResult = "";
    String result = SpinelMessage.toString(emphasize, buffer, offset, length);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getData method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetData() {
    System.out.println("getData");
    int index = 0;
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getData(index);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getDataLength method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetDataLength() {
    System.out.println("getDataLength");
    SpinelMessage instance = null;
    int expResult = 0;
    int result = instance.getDataLength();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of modify method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testModify() {
    System.out.println("modify");
    int address = 0;
    int sig = 0;
    SpinelMessage instance = null;
    SpinelMessage expResult = null;
    SpinelMessage result = instance.modify(address, sig);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getAckMessage method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetAckMessage_int() {
    System.out.println("getAckMessage");
    int ackCode = 0;
    SpinelMessage instance = null;
    SpinelMessage expResult = null;
    SpinelMessage result = instance.getAckMessage(ackCode);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getAckMessage method, of class SpinelMessage.
   */
  @Test
  @Ignore
  public void testGetAckMessage_int_intArr() {
    System.out.println("getAckMessage");
    int ackCode = 0;
    int[] data = null;
    SpinelMessage instance = null;
    SpinelMessage expResult = null;
    SpinelMessage result = instance.getAckMessage(ackCode, data);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
