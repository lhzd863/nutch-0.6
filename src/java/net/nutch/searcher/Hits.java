/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.nutch.io.Writable;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/** A set of hits matching a query. */
public final class Hits implements Writable {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.searcher.Hits");

  private long total;
  private boolean totalIsExact = true;
  private Hit[] top;

  public Hits() {}

  public Hits(long total, Hit[] top) {
    this.total = total;
    this.top = top;
  }

  /** Returns the total number of hits for this query.  This may be an estimate
   * when (@link totalIsExact()} is false. */
  public long getTotal() { return total; }

  /** True if {@link getTotal()} gives the exact number of hits, or false if
   * it is only an estimate of the total number of hits. */
  public boolean totalIsExact() { return totalIsExact; }

  /** Set {@link totalIsExact()}. */
  public void setTotalIsExact(boolean isExact) { totalIsExact = isExact; }

  /** Returns the number of hits included in this current listing. */
  public int getLength() { return top.length; }

  /** Returns the <code>i</code><sup>th</sup> hit in this list. */
  public Hit getHit(int i) { return top[i]; }

  /** Returns a subset of the hit objects. */
  public Hit[] getHits(int start, int length) {
    Hit[] results = new Hit[length];
    for (int i = 0; i < length; i++) {
      results[i] = top[start+i];
    }
    return results;
  }


  public void write(DataOutput out) throws IOException {
    out.writeLong(total);
    out.writeInt(top.length);
    for (int i = 0; i < top.length; i++) {
      top[i].write(out);
    }
  }

  public void readFields(DataInput in) throws IOException {
    total = in.readLong();
    top = new Hit[in.readInt()];
    for (int i = 0; i < top.length; i++) {
      top[i] = new Hit();
      top[i].readFields(in);
    }
  }

}
