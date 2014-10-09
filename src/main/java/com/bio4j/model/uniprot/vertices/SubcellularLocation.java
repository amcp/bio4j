package com.bio4j.model.uniprot.vertices;

import com.bio4j.model.uniprot.UniprotGraph;
import com.bio4j.model.uniprot.edges.ProteinSubcellularLocation;
import com.bio4j.model.uniprot.edges.SubcellularLocationParent;
import com.bio4j.angulillos.UntypedGraph;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ppareja on 7/23/2014.
 */
public final class SubcellularLocation <I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
		extends UniprotGraph.UniprotVertex<
		SubcellularLocation<I, RV, RVT, RE, RET>,
		UniprotGraph<I, RV, RVT, RE, RET>.SubcellularLocationType,
		I, RV, RVT, RE, RET
		> {

	public SubcellularLocation(RV vertex, UniprotGraph<I, RV, RVT, RE, RET>.SubcellularLocationType type) {
		super(vertex, type);
	}

	@Override
	public SubcellularLocation<I, RV, RVT, RE, RET> self() {
		return this;
	}

	// properties
	public String name() {
		return get(type().name);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////

	// relationships

	// proteinSubcellularLocation
	// ingoing
	public Stream<ProteinSubcellularLocation<I, RV, RVT, RE, RET>> proteinSubcellularLocation_in(){
		return inMany(graph().ProteinSubcellularLocation());
	}
	public Stream<Protein<I, RV, RVT, RE, RET>> proteinSubcellularLocation_inV(){
		return inManyV(graph().ProteinSubcellularLocation());
	}

	// subcellularLocationParent
	// ingoing
	public Stream<SubcellularLocationParent<I, RV, RVT, RE, RET>> subcellularLocationParent_in(){
		return inMany(graph().SubcellularLocationParent());
	}
	public Stream<SubcellularLocation<I, RV, RVT, RE, RET>> subcellularLocationParent_inV(){
		return inManyV(graph().SubcellularLocationParent());
	}

	// subcellularLocationParent
	// outgoing
	public SubcellularLocationParent<I, RV, RVT, RE, RET> subcellularLocationParent_out(){
		return outOne(graph().SubcellularLocationParent());
	}
	public SubcellularLocation<I, RV, RVT, RE, RET> subcellularLocationParent_outV(){
		return outOneV(graph().SubcellularLocationParent());
	}


}
