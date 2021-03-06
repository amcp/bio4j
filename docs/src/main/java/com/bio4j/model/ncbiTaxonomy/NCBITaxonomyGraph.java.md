
```java
package com.bio4j.model.ncbiTaxonomy;

import com.bio4j.model.ncbiTaxonomy.vertices.NCBITaxon;
import com.bio4j.model.ncbiTaxonomy.edges.NCBITaxonParent;
import com.bio4j.model.ncbiTaxonomy_geninfo.NCBITaxonomyGenInfoGraph;
import com.bio4j.model.uniprot_ncbiTaxonomy.UniProtNCBITaxonomyGraph;
import com.bio4j.angulillos.*;
```



# NCBI Taxonomy graph

This graph includes the whole NCBI taxonomy tree.
Files used in the importing process can be found [here](ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdump.tar.gz)

Once that information is extracted we are building the tree from the information included in the following files:

* **nodes.dmp**
* **names.dmp**

## data model

It simply consists of the vertices of NCBITaxon type plus the hierarchical relationships represented as NCBITaxonParent edges.

### NCBITaxons

We have a `NCBITaxon` vertex which contains property data present for each NCBI taxonomic unit.

##### NCBITaxon properties stored

- id
- name
- comment
- scientific name
- taxonomic rank



```java
public abstract class NCBITaxonomyGraph<
  // untyped graph
  I extends UntypedGraph<RV, RVT, RE, RET>,
  // vertices
  RV, RVT,
  // edges
  RE, RET
  >
  implements
  TypedGraph<
    NCBITaxonomyGraph<I, RV, RVT, RE, RET>,
    I, RV, RVT, RE, RET
    > {

  protected I raw = null;

  public NCBITaxonomyGraph(I graph){
    raw = graph;
  }

  public I raw(){
    return raw;
  }

  // indices
  public abstract TypedVertexIndex.Unique <
    // vertex
    NCBITaxon<I, RV, RVT, RE, RET>, NCBITaxonType,
    // property
    NCBITaxonType.id, String,
    // graph
    NCBITaxonomyGraph<I, RV, RVT, RE, RET>,
    I, RV, RVT, RE, RET
    >
  nCBITaxonIdIndex();

  public abstract UniProtNCBITaxonomyGraph<I, RV, RVT, RE, RET> uniProtNCBITaxonomyGraph();
  public abstract NCBITaxonomyGenInfoGraph<I, RV, RVT, RE, RET> ncbiTaxonomyGenInfoGraph();

  // types
  // vertices
  public abstract NCBITaxonType NCBITaxon();

  // edges
  public abstract NCBITaxonParentType NCBITaxonParent();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Vertex types

  public final class NCBITaxonType
    extends
    NCBITaxonomyVertexType<
      NCBITaxon<I, RV, RVT, RE, RET>,
      NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonType
      > {

  public final id id = new id();
  public final name name = new name();
  public final taxonomicRank taxonomicRank = new taxonomicRank();

  public NCBITaxonType(RVT raw) {
    super(raw);
  }

  @Override
  public NCBITaxonType value() {
    return graph().NCBITaxon();
  }

  @Override
  public NCBITaxon<I, RV, RVT, RE, RET> from(RV vertex) {
    return new NCBITaxon<I, RV, RVT, RE, RET>(vertex, this);
  }

  public final class id
    extends
    NCBITaxonomyVertexProperty<NCBITaxon<I, RV, RVT, RE, RET>, NCBITaxonType, id, String> {
    public id() {
    super(NCBITaxonType.this);
    }

    public Class<String> valueClass() {
    return String.class;
    }
  }

  public final class name
    extends
    NCBITaxonomyVertexProperty<NCBITaxon<I, RV, RVT, RE, RET>, NCBITaxonType, name, String> {
    public name() {
    super(NCBITaxonType.this);
    }

    public Class<String> valueClass() {
    return String.class;
    }
  }

  public final class taxonomicRank
    extends
    NCBITaxonomyVertexProperty<NCBITaxon<I, RV, RVT, RE, RET>, NCBITaxonType, taxonomicRank, String> {
    public taxonomicRank() {
    super(NCBITaxonType.this);
    }

    public Class<String> valueClass() {
    return String.class;
    }
  }


  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Edge types

  public final class NCBITaxonParentType
    extends
    NCBITaxonomyEdgeType<
      NCBITaxon<I, RV, RVT, RE, RET>, NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonType,
      NCBITaxonParent<I, RV, RVT, RE, RET>, NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonParentType,
      NCBITaxon<I, RV, RVT, RE, RET>, NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonType
      >
    implements
    TypedEdge.Type.OneToMany {

  public NCBITaxonParentType(RET raw) {
    super(NCBITaxonomyGraph.this.NCBITaxon(), raw, NCBITaxonomyGraph.this.NCBITaxon());
  }

  @Override
  public NCBITaxonParentType value() {
    return graph().NCBITaxonParent();
  }

  @Override
  public NCBITaxonParent<I, RV, RVT, RE, RET> from(RE edge) {
    return new NCBITaxonParent<I, RV, RVT, RE, RET>(edge, this);
  }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  // helper classes

  public abstract class NCBITaxonomyVertexProperty<
    V extends NCBITaxonomyVertex<V, VT, I, RV, RVT, RE, RET>,
    VT extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<V, VT>,
    P extends NCBITaxonomyVertexProperty<V, VT, P, PV>,
    PV
    >
    implements
    Property<V, VT, P, PV, NCBITaxonomyGraph<I, RV, RVT, RE, RET>, I, RV, RVT, RE, RET> {

  protected NCBITaxonomyVertexProperty(VT type) {

    this.type = type;
  }

  private VT type;

  @Override
  public final VT elementType() {
    return type;
  }
  }

  public abstract static class NCBITaxonomyVertex<
    V extends NCBITaxonomyVertex<V, VT, I, RV, RVT, RE, RET>,
    VT extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<V, VT>,
    I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET
    >
    implements
    TypedVertex<V, VT, NCBITaxonomyGraph<I, RV, RVT, RE, RET>, I, RV, RVT, RE, RET> {

  private RV vertex;
  private VT type;

  protected NCBITaxonomyVertex(RV vertex, VT type) {

    this.vertex = vertex;
    this.type = type;
  }

  @Override
  public NCBITaxonomyGraph<I, RV, RVT, RE, RET> graph() {
    return type().graph();
  }

  @Override
  public RV raw() {
    return this.vertex;
  }

  @Override
  public VT type() {
    return type;
  }
  }

  abstract class NCBITaxonomyVertexType<
    V extends NCBITaxonomyVertex<V, VT, I, RV, RVT, RE, RET>,
    VT extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<V, VT>
    >
    implements
    TypedVertex.Type<V, VT, NCBITaxonomyGraph<I, RV, RVT, RE, RET>, I, RV, RVT, RE, RET> {

  private RVT raw;

  protected NCBITaxonomyVertexType(RVT raw) {
    this.raw = raw;
  }

  @Override
  public final RVT raw() {
    return raw;
  }

  @Override
  public final NCBITaxonomyGraph<I, RV, RVT, RE, RET> graph() {
    return NCBITaxonomyGraph.this;
  }
  }

  public abstract static class NCBITaxonomyEdge<
    S extends NCBITaxonomyVertex<S, ST, I, RV, RVT, RE, RET>,
    ST extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<S, ST>,
    E extends NCBITaxonomyEdge<S, ST, E, ET, T, TT, I, RV, RVT, RE, RET>,
    ET extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyEdgeType<S, ST, E, ET, T, TT>,
    T extends NCBITaxonomyVertex<T, TT, I, RV, RVT, RE, RET>,
    TT extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<T, TT>,
    I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET
    >
    implements
    TypedEdge<
      S, ST, NCBITaxonomyGraph<I, RV, RVT, RE, RET>,
      E, ET, NCBITaxonomyGraph<I, RV, RVT, RE, RET>, I, RV, RVT, RE, RET,
      T, TT, NCBITaxonomyGraph<I, RV, RVT, RE, RET>
      > {

  private RE edge;
  private ET type;

  protected NCBITaxonomyEdge(RE edge, ET type) {

    this.edge = edge;
    this.type = type;
  }

  @Override
  public NCBITaxonomyGraph<I, RV, RVT, RE, RET> graph() {
    return type().graph();
  }

  @Override
  public RE raw() {
    return this.edge;
  }

  @Override
  public ET type() {
    return type;
  }
  }

  abstract class NCBITaxonomyEdgeType<
    S extends NCBITaxonomyVertex<S, ST, I, RV, RVT, RE, RET>,
    ST extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<S, ST>,
    E extends NCBITaxonomyEdge<S, ST, E, ET, T, TT, I, RV, RVT, RE, RET>,
    ET extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyEdgeType<S, ST, E, ET, T, TT>,
    T extends NCBITaxonomyVertex<T, TT, I, RV, RVT, RE, RET>,
    TT extends NCBITaxonomyGraph<I, RV, RVT, RE, RET>.NCBITaxonomyVertexType<T, TT>
    >
    implements
    TypedEdge.Type<
      S, ST, NCBITaxonomyGraph<I, RV, RVT, RE, RET>,
      E, ET, NCBITaxonomyGraph<I, RV, RVT, RE, RET>, I, RV, RVT, RE, RET,
      T, TT, NCBITaxonomyGraph<I, RV, RVT, RE, RET>
      > {

  private RET raw;
  private ST srcT;
  private TT tgtT;

  protected NCBITaxonomyEdgeType(ST srcT, RET raw, TT tgtT) {

    this.raw = raw;
    this.srcT = srcT;
    this.tgtT = tgtT;
  }

  @Override
  public final ST sourceType() {
    return srcT;
  }

  @Override
  public final TT targetType() {
    return tgtT;
  }

  @Override
  public final RET raw() {
    return raw;
  }

  @Override
  public final NCBITaxonomyGraph<I, RV, RVT, RE, RET> graph() {
    return NCBITaxonomyGraph.this;
  }
  }

}

```




[main/java/com/bio4j/model/enzymedb/vertices/Enzyme.java]: ../enzymedb/vertices/Enzyme.java.md
[main/java/com/bio4j/model/enzymedb/programs/ImportEnzymeDB.java]: ../enzymedb/programs/ImportEnzymeDB.java.md
[main/java/com/bio4j/model/enzymedb/EnzymeDBGraph.java]: ../enzymedb/EnzymeDBGraph.java.md
[main/java/com/bio4j/model/uniprot_uniref/programs/ImportUniProtUniRef.java]: ../uniprot_uniref/programs/ImportUniProtUniRef.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef50Member.java]: ../uniprot_uniref/edges/UniRef50Member.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef100Representant.java]: ../uniprot_uniref/edges/UniRef100Representant.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef100Member.java]: ../uniprot_uniref/edges/UniRef100Member.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef50Representant.java]: ../uniprot_uniref/edges/UniRef50Representant.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef90Representant.java]: ../uniprot_uniref/edges/UniRef90Representant.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef90Member.java]: ../uniprot_uniref/edges/UniRef90Member.java.md
[main/java/com/bio4j/model/uniprot_uniref/UniProtUniRefGraph.java]: ../uniprot_uniref/UniProtUniRefGraph.java.md
[main/java/com/bio4j/model/uniref/vertices/UniRef100Cluster.java]: ../uniref/vertices/UniRef100Cluster.java.md
[main/java/com/bio4j/model/uniref/vertices/UniRef50Cluster.java]: ../uniref/vertices/UniRef50Cluster.java.md
[main/java/com/bio4j/model/uniref/vertices/UniRef90Cluster.java]: ../uniref/vertices/UniRef90Cluster.java.md
[main/java/com/bio4j/model/uniref/UniRefGraph.java]: ../uniref/UniRefGraph.java.md
[main/java/com/bio4j/model/uniref/programs/ImportUniRef.java]: ../uniref/programs/ImportUniRef.java.md
[main/java/com/bio4j/model/go/vertices/SubOntologies.java]: ../go/vertices/SubOntologies.java.md
[main/java/com/bio4j/model/go/vertices/GoTerm.java]: ../go/vertices/GoTerm.java.md
[main/java/com/bio4j/model/go/vertices/GoSlims.java]: ../go/vertices/GoSlims.java.md
[main/java/com/bio4j/model/go/programs/ImportGO.java]: ../go/programs/ImportGO.java.md
[main/java/com/bio4j/model/go/edges/HasPartOf.java]: ../go/edges/HasPartOf.java.md
[main/java/com/bio4j/model/go/edges/PositivelyRegulates.java]: ../go/edges/PositivelyRegulates.java.md
[main/java/com/bio4j/model/go/edges/Regulates.java]: ../go/edges/Regulates.java.md
[main/java/com/bio4j/model/go/edges/SubOntology.java]: ../go/edges/SubOntology.java.md
[main/java/com/bio4j/model/go/edges/IsA.java]: ../go/edges/IsA.java.md
[main/java/com/bio4j/model/go/edges/NegativelyRegulates.java]: ../go/edges/NegativelyRegulates.java.md
[main/java/com/bio4j/model/go/edges/PartOf.java]: ../go/edges/PartOf.java.md
[main/java/com/bio4j/model/go/GoGraph.java]: ../go/GoGraph.java.md
[main/java/com/bio4j/model/ncbiTaxonomy_geninfo/programs/ImportGenInfoNCBITaxonIndex.java]: ../ncbiTaxonomy_geninfo/programs/ImportGenInfoNCBITaxonIndex.java.md
[main/java/com/bio4j/model/ncbiTaxonomy_geninfo/edges/GenInfoNCBITaxon.java]: ../ncbiTaxonomy_geninfo/edges/GenInfoNCBITaxon.java.md
[main/java/com/bio4j/model/ncbiTaxonomy_geninfo/NCBITaxonomyGenInfoGraph.java]: ../ncbiTaxonomy_geninfo/NCBITaxonomyGenInfoGraph.java.md
[main/java/com/bio4j/model/uniprot_ncbiTaxonomy/UniProtNCBITaxonomyGraph.java]: ../uniprot_ncbiTaxonomy/UniProtNCBITaxonomyGraph.java.md
[main/java/com/bio4j/model/uniprot_ncbiTaxonomy/programs/ImportUniProtNCBITaxonomy.java]: ../uniprot_ncbiTaxonomy/programs/ImportUniProtNCBITaxonomy.java.md
[main/java/com/bio4j/model/uniprot_ncbiTaxonomy/edges/ProteinNCBITaxon.java]: ../uniprot_ncbiTaxonomy/edges/ProteinNCBITaxon.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/vertices/NCBITaxon.java]: vertices/NCBITaxon.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/NCBITaxonomyGraph.java]: NCBITaxonomyGraph.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/programs/ImportNCBITaxonomy.java]: programs/ImportNCBITaxonomy.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/edges/NCBITaxonParent.java]: edges/NCBITaxonParent.java.md
[main/java/com/bio4j/model/geninfo/vertices/GenInfo.java]: ../geninfo/vertices/GenInfo.java.md
[main/java/com/bio4j/model/geninfo/GenInfoGraph.java]: ../geninfo/GenInfoGraph.java.md
[main/java/com/bio4j/model/uniprot_go/tests/ImportUniProtGoTest.java]: ../uniprot_go/tests/ImportUniProtGoTest.java.md
[main/java/com/bio4j/model/uniprot_go/UniProtGoGraph.java]: ../uniprot_go/UniProtGoGraph.java.md
[main/java/com/bio4j/model/uniprot_go/programs/ImportUniProtGo.java]: ../uniprot_go/programs/ImportUniProtGo.java.md
[main/java/com/bio4j/model/uniprot_go/edges/GoAnnotation.java]: ../uniprot_go/edges/GoAnnotation.java.md
[main/java/com/bio4j/model/uniprot_enzymedb/UniProtEnzymeDBGraph.java]: ../uniprot_enzymedb/UniProtEnzymeDBGraph.java.md
[main/java/com/bio4j/model/uniprot_enzymedb/programs/ImportUniProtEnzymeDB.java]: ../uniprot_enzymedb/programs/ImportUniProtEnzymeDB.java.md
[main/java/com/bio4j/model/uniprot_enzymedb/edges/EnzymaticActivity.java]: ../uniprot_enzymedb/edges/EnzymaticActivity.java.md
[main/java/com/bio4j/model/uniprot/UniProtGraph.java]: ../uniprot/UniProtGraph.java.md
[main/java/com/bio4j/model/uniprot/vertices/SequenceCaution.java]: ../uniprot/vertices/SequenceCaution.java.md
[main/java/com/bio4j/model/uniprot/vertices/Disease.java]: ../uniprot/vertices/Disease.java.md
[main/java/com/bio4j/model/uniprot/vertices/UniGene.java]: ../uniprot/vertices/UniGene.java.md
[main/java/com/bio4j/model/uniprot/vertices/InterPro.java]: ../uniprot/vertices/InterPro.java.md
[main/java/com/bio4j/model/uniprot/vertices/RefSeq.java]: ../uniprot/vertices/RefSeq.java.md
[main/java/com/bio4j/model/uniprot/vertices/Organism.java]: ../uniprot/vertices/Organism.java.md
[main/java/com/bio4j/model/uniprot/vertices/Country.java]: ../uniprot/vertices/Country.java.md
[main/java/com/bio4j/model/uniprot/vertices/OnlineJournal.java]: ../uniprot/vertices/OnlineJournal.java.md
[main/java/com/bio4j/model/uniprot/vertices/Thesis.java]: ../uniprot/vertices/Thesis.java.md
[main/java/com/bio4j/model/uniprot/vertices/Pubmed.java]: ../uniprot/vertices/Pubmed.java.md
[main/java/com/bio4j/model/uniprot/vertices/PIR.java]: ../uniprot/vertices/PIR.java.md
[main/java/com/bio4j/model/uniprot/vertices/EMBL.java]: ../uniprot/vertices/EMBL.java.md
[main/java/com/bio4j/model/uniprot/vertices/Institute.java]: ../uniprot/vertices/Institute.java.md
[main/java/com/bio4j/model/uniprot/vertices/City.java]: ../uniprot/vertices/City.java.md
[main/java/com/bio4j/model/uniprot/vertices/Reference.java]: ../uniprot/vertices/Reference.java.md
[main/java/com/bio4j/model/uniprot/vertices/Submission.java]: ../uniprot/vertices/Submission.java.md
[main/java/com/bio4j/model/uniprot/vertices/Protein.java]: ../uniprot/vertices/Protein.java.md
[main/java/com/bio4j/model/uniprot/vertices/Journal.java]: ../uniprot/vertices/Journal.java.md
[main/java/com/bio4j/model/uniprot/vertices/Dataset.java]: ../uniprot/vertices/Dataset.java.md
[main/java/com/bio4j/model/uniprot/vertices/Publisher.java]: ../uniprot/vertices/Publisher.java.md
[main/java/com/bio4j/model/uniprot/vertices/Patent.java]: ../uniprot/vertices/Patent.java.md
[main/java/com/bio4j/model/uniprot/vertices/Pfam.java]: ../uniprot/vertices/Pfam.java.md
[main/java/com/bio4j/model/uniprot/vertices/AlternativeProduct.java]: ../uniprot/vertices/AlternativeProduct.java.md
[main/java/com/bio4j/model/uniprot/vertices/Keyword.java]: ../uniprot/vertices/Keyword.java.md
[main/java/com/bio4j/model/uniprot/vertices/UnpublishedObservation.java]: ../uniprot/vertices/UnpublishedObservation.java.md
[main/java/com/bio4j/model/uniprot/vertices/Book.java]: ../uniprot/vertices/Book.java.md
[main/java/com/bio4j/model/uniprot/vertices/DB.java]: ../uniprot/vertices/DB.java.md
[main/java/com/bio4j/model/uniprot/vertices/Isoform.java]: ../uniprot/vertices/Isoform.java.md
[main/java/com/bio4j/model/uniprot/vertices/Consortium.java]: ../uniprot/vertices/Consortium.java.md
[main/java/com/bio4j/model/uniprot/vertices/ReactomeTerm.java]: ../uniprot/vertices/ReactomeTerm.java.md
[main/java/com/bio4j/model/uniprot/vertices/GeneName.java]: ../uniprot/vertices/GeneName.java.md
[main/java/com/bio4j/model/uniprot/vertices/Ensembl.java]: ../uniprot/vertices/Ensembl.java.md
[main/java/com/bio4j/model/uniprot/vertices/OnlineArticle.java]: ../uniprot/vertices/OnlineArticle.java.md
[main/java/com/bio4j/model/uniprot/vertices/CommentType.java]: ../uniprot/vertices/CommentType.java.md
[main/java/com/bio4j/model/uniprot/vertices/GeneLocation.java]: ../uniprot/vertices/GeneLocation.java.md
[main/java/com/bio4j/model/uniprot/vertices/FeatureType.java]: ../uniprot/vertices/FeatureType.java.md
[main/java/com/bio4j/model/uniprot/vertices/Taxon.java]: ../uniprot/vertices/Taxon.java.md
[main/java/com/bio4j/model/uniprot/vertices/Article.java]: ../uniprot/vertices/Article.java.md
[main/java/com/bio4j/model/uniprot/vertices/Kegg.java]: ../uniprot/vertices/Kegg.java.md
[main/java/com/bio4j/model/uniprot/vertices/SubcellularLocation.java]: ../uniprot/vertices/SubcellularLocation.java.md
[main/java/com/bio4j/model/uniprot/vertices/Person.java]: ../uniprot/vertices/Person.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportIsoformSequences.java]: ../uniprot/programs/ImportIsoformSequences.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProt.java]: ../uniprot/programs/ImportUniProt.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportProteinInteractions.java]: ../uniprot/programs/ImportProteinInteractions.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProtEdges.java]: ../uniprot/programs/ImportUniProtEdges.java.md
[main/java/com/bio4j/model/uniprot/programs/XMLConstants.java]: ../uniprot/programs/XMLConstants.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProtVertices.java]: ../uniprot/programs/ImportUniProtVertices.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinOrganism.java]: ../uniprot/edges/ProteinOrganism.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinRefSeq.java]: ../uniprot/edges/ProteinRefSeq.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinSequenceCaution.java]: ../uniprot/edges/ProteinSequenceCaution.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceArticle.java]: ../uniprot/edges/ReferenceArticle.java.md
[main/java/com/bio4j/model/uniprot/edges/BookPublisher.java]: ../uniprot/edges/BookPublisher.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinPIR.java]: ../uniprot/edges/ProteinPIR.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinEMBL.java]: ../uniprot/edges/ProteinEMBL.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinUniGene.java]: ../uniprot/edges/ProteinUniGene.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinProteinInteraction.java]: ../uniprot/edges/ProteinProteinInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinKegg.java]: ../uniprot/edges/ProteinKegg.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinDisease.java]: ../uniprot/edges/ProteinDisease.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinFeature.java]: ../uniprot/edges/ProteinFeature.java.md
[main/java/com/bio4j/model/uniprot/edges/BookEditor.java]: ../uniprot/edges/BookEditor.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinIsoform.java]: ../uniprot/edges/ProteinIsoform.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinSubcellularLocation.java]: ../uniprot/edges/ProteinSubcellularLocation.java.md
[main/java/com/bio4j/model/uniprot/edges/IsoformProteinInteraction.java]: ../uniprot/edges/IsoformProteinInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinDataset.java]: ../uniprot/edges/ProteinDataset.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceAuthorPerson.java]: ../uniprot/edges/ReferenceAuthorPerson.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferencePatent.java]: ../uniprot/edges/ReferencePatent.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinIsoformInteraction.java]: ../uniprot/edges/ProteinIsoformInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceBook.java]: ../uniprot/edges/ReferenceBook.java.md
[main/java/com/bio4j/model/uniprot/edges/OnlineArticleOnlineJournal.java]: ../uniprot/edges/OnlineArticleOnlineJournal.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceOnlineArticle.java]: ../uniprot/edges/ReferenceOnlineArticle.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceAuthorConsortium.java]: ../uniprot/edges/ReferenceAuthorConsortium.java.md
[main/java/com/bio4j/model/uniprot/edges/ArticleJournal.java]: ../uniprot/edges/ArticleJournal.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinEnsembl.java]: ../uniprot/edges/ProteinEnsembl.java.md
[main/java/com/bio4j/model/uniprot/edges/ThesisInstitute.java]: ../uniprot/edges/ThesisInstitute.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinReactomeTerm.java]: ../uniprot/edges/ProteinReactomeTerm.java.md
[main/java/com/bio4j/model/uniprot/edges/SubcellularLocationParent.java]: ../uniprot/edges/SubcellularLocationParent.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinComment.java]: ../uniprot/edges/ProteinComment.java.md
[main/java/com/bio4j/model/uniprot/edges/TaxonParent.java]: ../uniprot/edges/TaxonParent.java.md
[main/java/com/bio4j/model/uniprot/edges/SubmissionDB.java]: ../uniprot/edges/SubmissionDB.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinInterPro.java]: ../uniprot/edges/ProteinInterPro.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceThesis.java]: ../uniprot/edges/ReferenceThesis.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinGeneName.java]: ../uniprot/edges/ProteinGeneName.java.md
[main/java/com/bio4j/model/uniprot/edges/OrganismTaxon.java]: ../uniprot/edges/OrganismTaxon.java.md
[main/java/com/bio4j/model/uniprot/edges/IsoformEventGenerator.java]: ../uniprot/edges/IsoformEventGenerator.java.md
[main/java/com/bio4j/model/uniprot/edges/BookCity.java]: ../uniprot/edges/BookCity.java.md
[main/java/com/bio4j/model/uniprot/edges/ArticlePubmed.java]: ../uniprot/edges/ArticlePubmed.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceSubmission.java]: ../uniprot/edges/ReferenceSubmission.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinKeyword.java]: ../uniprot/edges/ProteinKeyword.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceUnpublishedObservation.java]: ../uniprot/edges/ReferenceUnpublishedObservation.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinPfam.java]: ../uniprot/edges/ProteinPfam.java.md
[main/java/com/bio4j/model/uniprot/edges/InstituteCountry.java]: ../uniprot/edges/InstituteCountry.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinReference.java]: ../uniprot/edges/ProteinReference.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinGeneLocation.java]: ../uniprot/edges/ProteinGeneLocation.java.md