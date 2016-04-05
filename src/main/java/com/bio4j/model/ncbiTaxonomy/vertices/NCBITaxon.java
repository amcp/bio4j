package com.bio4j.model.ncbiTaxonomy.vertices;

import com.bio4j.model.ncbiTaxonomy.NCBITaxonomyGraph;
import com.bio4j.angulillos.UntypedGraph;

public final class NCBITaxon<I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
  extends
    NCBITaxonomyGraph.NCBITaxonomyVertex <
      NCBITaxon<I, RV, RVT, RE, RET>,
      NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonType,
      I, RV, RVT, RE, RET
    >
{

  public NCBITaxon(RV vertex, NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonType type) { super(vertex, type); }

  @Override
  public final NCBITaxon<I, RV, RVT, RE, RET> self() { return this; }
}
