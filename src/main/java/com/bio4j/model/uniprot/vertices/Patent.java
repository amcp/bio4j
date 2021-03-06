package com.bio4j.model.uniprot.vertices;

import com.bio4j.model.uniprot.UniProtGraph;
import com.bio4j.angulillos.UntypedGraph;

public final class Patent<I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
extends UniProtGraph.UniProtVertex<
Patent<I, RV, RVT, RE, RET>,
UniProtGraph<I, RV, RVT, RE, RET>.PatentType,
I, RV, RVT, RE, RET
>  {

  public Patent(RV vertex, UniProtGraph<I, RV, RVT, RE, RET>.PatentType type) {
    super(vertex, type);
  }

  @Override
  public Patent<I, RV, RVT, RE, RET> self() {
    return this;
  }
}
