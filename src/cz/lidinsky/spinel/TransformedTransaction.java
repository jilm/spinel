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
import java.util.function.Function;

/**
 * It is used for hand messages over between virtual and physical peer.
 * [virtual peer] --&gt; (req) --&gt; [daemon] --&gt; (transform(req))
 * --&gt; [physical peer] --&gt; (transaction) --&gt; [daemon]
 * --&gt; (inv-transform(transaction)) --&gt; [virtual peer]
 */
class TransformedTransaction extends Transaction {

  private final Function<SpinelMessage, SpinelMessage> transform;

  private final Transaction transaction;

  /**
   * @param transaction
   *            a transaction which should be wrapped up
   *
   * @param transformation
   *            a function which transform a message from physical peer to
   *            the virtual peer
   */
  TransformedTransaction(Transaction transaction,
      Function<SpinelMessage, SpinelMessage> transform) {
    super(transaction.getRequest());
    this.transform = transform;
    this.transaction = transaction;
  }

  @Override
  void put(SpinelMessage message) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SpinelMessage get(long timeout) throws TimeoutException {
    return transform.apply(transaction.get(timeout));
  }

  @Override
  public SpinelMessage getRequest() {
    return transform.apply(transaction.getRequest());
  }
}
