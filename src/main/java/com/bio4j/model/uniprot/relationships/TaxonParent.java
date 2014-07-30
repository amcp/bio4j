package com.bio4j.model.uniprot.relationships;

import com.bio4j.model.uniprot.UniprotGraph;
import com.bio4j.model.uniprot.nodes.Taxon;
import com.ohnosequences.typedGraphs.UntypedGraph;

/**
 * Created by ppareja on 7/28/2014.
 */
public final class TaxonParent <I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
		extends
		UniprotGraph.UniprotEdge<
				Taxon<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.TaxonType,
				TaxonParent<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.TaxonParentType,
				Taxon<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.TaxonType,
				I, RV, RVT, RE, RET
				> {

	public TaxonParent(RE edge, UniprotGraph<I, RV, RVT, RE, RET>.TaxonParentType type) {

		super(edge, type);
	}

	@Override
	public TaxonParent<I, RV, RVT, RE, RET> self() {
		return this;
	}
}