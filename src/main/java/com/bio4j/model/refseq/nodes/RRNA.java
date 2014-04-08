package com.bio4j.model.refseq.nodes;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public interface RRNA extends RNA<RRNA, RRNA.type> { 

  public static type TYPE = type.rRNA;
  public static enum type implements RNAType<RRNA, RRNA.type> {

    rRNA;
    public type value() { return rRNA; }
  }   
}
