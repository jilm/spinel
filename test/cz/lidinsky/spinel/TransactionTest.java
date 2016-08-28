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
public class TransactionTest {

  public TransactionTest() {
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
   * Test of get method, of class Transaction. Test that the method simply
   * returns given object.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGet1() throws Exception {
    System.out.println("get");
    long timeout = 0L;
    Transaction instance = new Transaction(new SpinelMessage(12, 55));
    SpinelMessage expResult = new SpinelMessage(13, 66);
    instance.put(expResult);
    SpinelMessage result = instance.get(timeout);
    assertEquals(expResult, result);
  }

  /**
   * Test the timeout.
   *
   * @throws java.lang.Exception
   */
  @Test(expected=TimeoutException.class, timeout=250)
  public void testGet2() throws Exception {
    System.out.println("get");
    long timeout = 200L;
    Transaction instance = new Transaction(new SpinelMessage(12, 55));
    //SpinelMessage expResult = new SpinelMessage(13, 66);
    //instance.put(expResult);
    SpinelMessage result = instance.get(timeout);
    fail();
  }

  /**
   * Test the timeout.
   *
   * @throws java.lang.Exception
   */
  @Test(timeout=250)
  public void testGet3() throws Exception {
    System.out.println("get");
    long timeout = 200L;
    Transaction instance = new Transaction(new SpinelMessage(12, 55));
    SpinelMessage expResult = new SpinelMessage(13, 66);
    new Thread(new Runnable() {
      @Override
      public synchronized void run() {
        try {
          wait(100);
        } catch (InterruptedException ex) {  }
        instance.put(expResult);
      }
    }).start();
    SpinelMessage result = instance.get(timeout);
    assertEquals(expResult, result);
  }

  /**
   * Test of hasResponse method, of class Transaction.
   */
  @Test
  @Ignore
  public void testHasResponse() {
    System.out.println("hasResponse");
    Transaction instance = null;
    boolean expResult = false;
    boolean result = instance.hasResponse();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of put method, of class Transaction.
   */
  @Test
  @Ignore
  public void testPut() {
    System.out.println("put");
    SpinelMessage message = null;
    Transaction instance = null;
    instance.put(message);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getRequest method, of class Transaction.
   */
  @Test
  @Ignore
  public void testGetRequest() {
    System.out.println("getRequest");
    Transaction instance = null;
    SpinelMessage expResult = null;
    SpinelMessage result = instance.getRequest();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
