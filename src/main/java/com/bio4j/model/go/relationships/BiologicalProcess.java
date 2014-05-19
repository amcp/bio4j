package com.bio4j.model.go.relationships;

import com.ohnosequences.typedGraphs.Relationship;


/**
 *
 * @author <a href="mailto:ppareja@era7.com">Pablo Pareja Tobes</a>
 * @author <a href="mailto:eparejatobes@ohnosequences.com">Eduardo Pareja-Tobes</a>
 */
public interface BiologicalProcess <
  S extends Term<S,ST>, ST extends Term.Type<S,ST>,
  R extends BiologicalProcess<S,ST,R,RT,T,TT>, RT extends BiologicalProcess.Type<S,ST,R,RT,T,TT>,
  T extends SubOntologies<T,TT>, TT extends SubOntologies.Type<T,TT>
>
  extends Relationship<S,ST,R,RT,T,TT>
{

  interface Type <
    S extends Term<S,ST>, ST extends Term.Type<S,ST>,
    R extends BiologicalProcess<S,ST,R,RT,T,TT>, RT extends BiologicalProcess.Type<S,ST,R,RT,T,TT>,
    T extends SubOntologies<T,TT>, TT extends SubOntologies.Type<T,TT>
  >
    extends Relationship.Type<S,ST,R,RT,T,TT>
  {}
}