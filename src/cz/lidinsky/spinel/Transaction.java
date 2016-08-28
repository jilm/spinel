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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Exchange point between processes.
 */
public class Transaction {

  /**
   * Received response to the given request. It contains null until the
   * response is received.
   */
  private SpinelMessage response;

  /**
   * Request to be send.
   */
  private final SpinelMessage request;

  /**
   * @param request
   *            the request message
   */
  Transaction(SpinelMessage request) {
    this.response = null;
    this.request = request;
  }

  /**
   * To pick up the response to the given request. This method blocks until the
   * request is saved through the put method or until the given timeout.
   *
   * @param timeout
   *            required timeout is milliseconds
   *
   * @return responce to the given request
   *
   * @throws java.util.concurrent.TimeoutException
   *
   */
  public synchronized SpinelMessage get(long timeout) throws TimeoutException {

    Instant timeoutInstant = Instant.now().plusMillis(timeout);
    while (response == null && timeoutInstant.isAfter(Instant.now())) {
      try {
        wait(Duration.between(Instant.now(), timeoutInstant).toMillis());
      } catch (InterruptedException ex) { }
    }
    if (response == null) {
      throw new java.util.concurrent.TimeoutException();
    } else {
      return response;
    }
  }

  /**
   * Returns true iff the response has alredy been passed into this object.

   * @return true iff the get message returns response without bloking
   */
  public synchronized boolean hasResponse() {
    return response != null;
  }

  /**
   * Save response to the request.
   */
  synchronized void put(SpinelMessage message) {
    if (this.response == null) {
      this.response = message;
    }
    notifyAll();
  }

  /**
   * Returns given request.
   *
   * @return the request
   */
  public SpinelMessage getRequest() {
    return request;
  }

}
