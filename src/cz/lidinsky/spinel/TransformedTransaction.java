/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
