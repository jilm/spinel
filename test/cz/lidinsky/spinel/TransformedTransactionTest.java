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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jilm
 */
public class TransformedTransactionTest {

  public TransformedTransactionTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  SpinelMessage request;
  SpinelMessage response;
  Transaction transaction;
  Transaction transformedTransaction;
  SpinelMessage expRequest;
  SpinelMessage expResponse;

  @Before
  public void setUp() {
    request = new SpinelMessage(20, 10);
    response = new SpinelMessage(20, 15);
    transaction = new Transaction(request);
    transaction.put(response);
    transformedTransaction = new TransformedTransaction(transaction,
      message -> message.modify(30, 0));
    expRequest = new SpinelMessage(30, 10);
    expResponse = new SpinelMessage(30, 15);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of put method, of class TransformedTransaction.
   */
  @Test
  @Ignore
  public void testPut() {
    System.out.println("put");
    SpinelMessage message = null;
    TransformedTransaction instance = null;
    instance.put(message);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of get method, of class TransformedTransaction.
   */
  @Test
  public void testGet() throws Exception {
    System.out.println("get");
    long timeout = 100L;
    SpinelMessage result = transformedTransaction.get(timeout);
    assertEquals(expResponse, result);
  }

  /**
   * Test of getRequest method, of class TransformedTransaction.
   */
  @Test
  public void testGetRequest() {
    System.out.println("getRequest");
    SpinelMessage result = transformedTransaction.getRequest();
    assertEquals(expRequest, result);
  }

  private void fail(String the_test_case_is_a_prototype) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
