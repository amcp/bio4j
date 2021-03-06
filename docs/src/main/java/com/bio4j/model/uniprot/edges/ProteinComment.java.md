
```java
package com.bio4j.model.uniprot.edges;

import com.bio4j.model.uniprot.UniProtGraph;
import com.bio4j.model.uniprot.vertices.CommentType;
import com.bio4j.model.uniprot.vertices.Protein;
import com.bio4j.angulillos.UntypedGraph;

/**
 * Created by ppareja on 7/28/2014.
 */
public final class ProteinComment <I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
  extends
  UniProtGraph.UniProtEdge<
    Protein<I, RV, RVT, RE, RET>, UniProtGraph<I, RV, RVT, RE, RET>.ProteinType,
    ProteinComment<I, RV, RVT, RE, RET>, UniProtGraph<I, RV, RVT, RE, RET>.ProteinCommentType,
    CommentType<I, RV, RVT, RE, RET>, UniProtGraph<I, RV, RVT, RE, RET>.CommentTypeType,
    I, RV, RVT, RE, RET
    > {

  // properties
  public String position() {
  return get(type().position);
  }
  public String mass() {  return get(type().mass);  }
  public String evidence() {
  return get(type().evidence);
  }
  public String method() {
  return get(type().method);
  }
  public int begin() {
  return get(type().begin);
  }
  public int end() {
  return get(type().end);
  }
  public String status() {
  return get(type().status);
  }
  public String text() {
  return get(type().text);
  }
  public String temperatureDependence() {
  return get(type().temperatureDependence);
  }
  public String phDependence() {
  return get(type().phDependence);
  }
  public String kineticsXML() {
  return get(type().kineticsXML);
  }
  public String absorptionMax() {
  return get(type().absorptionMax);
  }
  public String absorptionText() {
  return get(type().absorptionText);
  }
  public String redoxPotential() {
  return get(type().redoxPotential);
  }
  public String redoxPotentialEvidence() {
  return get(type().redoxPotentialEvidence);
  }

  public ProteinComment(RE edge, UniProtGraph<I, RV, RVT, RE, RET>.ProteinCommentType type) {

  super(edge, type);
  }

  @Override
  public ProteinComment<I, RV, RVT, RE, RET> self() {
  return this;
  }
}
```




[main/java/com/bio4j/model/enzymedb/vertices/Enzyme.java]: ../../enzymedb/vertices/Enzyme.java.md
[main/java/com/bio4j/model/enzymedb/programs/ImportEnzymeDB.java]: ../../enzymedb/programs/ImportEnzymeDB.java.md
[main/java/com/bio4j/model/enzymedb/EnzymeDBGraph.java]: ../../enzymedb/EnzymeDBGraph.java.md
[main/java/com/bio4j/model/uniprot_uniref/programs/ImportUniProtUniRef.java]: ../../uniprot_uniref/programs/ImportUniProtUniRef.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef50Member.java]: ../../uniprot_uniref/edges/UniRef50Member.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef100Representant.java]: ../../uniprot_uniref/edges/UniRef100Representant.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef100Member.java]: ../../uniprot_uniref/edges/UniRef100Member.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef50Representant.java]: ../../uniprot_uniref/edges/UniRef50Representant.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef90Representant.java]: ../../uniprot_uniref/edges/UniRef90Representant.java.md
[main/java/com/bio4j/model/uniprot_uniref/edges/UniRef90Member.java]: ../../uniprot_uniref/edges/UniRef90Member.java.md
[main/java/com/bio4j/model/uniprot_uniref/UniProtUniRefGraph.java]: ../../uniprot_uniref/UniProtUniRefGraph.java.md
[main/java/com/bio4j/model/uniref/vertices/UniRef100Cluster.java]: ../../uniref/vertices/UniRef100Cluster.java.md
[main/java/com/bio4j/model/uniref/vertices/UniRef50Cluster.java]: ../../uniref/vertices/UniRef50Cluster.java.md
[main/java/com/bio4j/model/uniref/vertices/UniRef90Cluster.java]: ../../uniref/vertices/UniRef90Cluster.java.md
[main/java/com/bio4j/model/uniref/UniRefGraph.java]: ../../uniref/UniRefGraph.java.md
[main/java/com/bio4j/model/uniref/programs/ImportUniRef.java]: ../../uniref/programs/ImportUniRef.java.md
[main/java/com/bio4j/model/go/vertices/SubOntologies.java]: ../../go/vertices/SubOntologies.java.md
[main/java/com/bio4j/model/go/vertices/GoTerm.java]: ../../go/vertices/GoTerm.java.md
[main/java/com/bio4j/model/go/vertices/GoSlims.java]: ../../go/vertices/GoSlims.java.md
[main/java/com/bio4j/model/go/programs/ImportGO.java]: ../../go/programs/ImportGO.java.md
[main/java/com/bio4j/model/go/edges/HasPartOf.java]: ../../go/edges/HasPartOf.java.md
[main/java/com/bio4j/model/go/edges/PositivelyRegulates.java]: ../../go/edges/PositivelyRegulates.java.md
[main/java/com/bio4j/model/go/edges/Regulates.java]: ../../go/edges/Regulates.java.md
[main/java/com/bio4j/model/go/edges/SubOntology.java]: ../../go/edges/SubOntology.java.md
[main/java/com/bio4j/model/go/edges/IsA.java]: ../../go/edges/IsA.java.md
[main/java/com/bio4j/model/go/edges/NegativelyRegulates.java]: ../../go/edges/NegativelyRegulates.java.md
[main/java/com/bio4j/model/go/edges/PartOf.java]: ../../go/edges/PartOf.java.md
[main/java/com/bio4j/model/go/GoGraph.java]: ../../go/GoGraph.java.md
[main/java/com/bio4j/model/ncbiTaxonomy_geninfo/programs/ImportGenInfoNCBITaxonIndex.java]: ../../ncbiTaxonomy_geninfo/programs/ImportGenInfoNCBITaxonIndex.java.md
[main/java/com/bio4j/model/ncbiTaxonomy_geninfo/edges/GenInfoNCBITaxon.java]: ../../ncbiTaxonomy_geninfo/edges/GenInfoNCBITaxon.java.md
[main/java/com/bio4j/model/ncbiTaxonomy_geninfo/NCBITaxonomyGenInfoGraph.java]: ../../ncbiTaxonomy_geninfo/NCBITaxonomyGenInfoGraph.java.md
[main/java/com/bio4j/model/uniprot_ncbiTaxonomy/UniProtNCBITaxonomyGraph.java]: ../../uniprot_ncbiTaxonomy/UniProtNCBITaxonomyGraph.java.md
[main/java/com/bio4j/model/uniprot_ncbiTaxonomy/programs/ImportUniProtNCBITaxonomy.java]: ../../uniprot_ncbiTaxonomy/programs/ImportUniProtNCBITaxonomy.java.md
[main/java/com/bio4j/model/uniprot_ncbiTaxonomy/edges/ProteinNCBITaxon.java]: ../../uniprot_ncbiTaxonomy/edges/ProteinNCBITaxon.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/vertices/NCBITaxon.java]: ../../ncbiTaxonomy/vertices/NCBITaxon.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/NCBITaxonomyGraph.java]: ../../ncbiTaxonomy/NCBITaxonomyGraph.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/programs/ImportNCBITaxonomy.java]: ../../ncbiTaxonomy/programs/ImportNCBITaxonomy.java.md
[main/java/com/bio4j/model/ncbiTaxonomy/edges/NCBITaxonParent.java]: ../../ncbiTaxonomy/edges/NCBITaxonParent.java.md
[main/java/com/bio4j/model/geninfo/vertices/GenInfo.java]: ../../geninfo/vertices/GenInfo.java.md
[main/java/com/bio4j/model/geninfo/GenInfoGraph.java]: ../../geninfo/GenInfoGraph.java.md
[main/java/com/bio4j/model/uniprot_go/tests/ImportUniProtGoTest.java]: ../../uniprot_go/tests/ImportUniProtGoTest.java.md
[main/java/com/bio4j/model/uniprot_go/UniProtGoGraph.java]: ../../uniprot_go/UniProtGoGraph.java.md
[main/java/com/bio4j/model/uniprot_go/programs/ImportUniProtGo.java]: ../../uniprot_go/programs/ImportUniProtGo.java.md
[main/java/com/bio4j/model/uniprot_go/edges/GoAnnotation.java]: ../../uniprot_go/edges/GoAnnotation.java.md
[main/java/com/bio4j/model/uniprot_enzymedb/UniProtEnzymeDBGraph.java]: ../../uniprot_enzymedb/UniProtEnzymeDBGraph.java.md
[main/java/com/bio4j/model/uniprot_enzymedb/programs/ImportUniProtEnzymeDB.java]: ../../uniprot_enzymedb/programs/ImportUniProtEnzymeDB.java.md
[main/java/com/bio4j/model/uniprot_enzymedb/edges/EnzymaticActivity.java]: ../../uniprot_enzymedb/edges/EnzymaticActivity.java.md
[main/java/com/bio4j/model/uniprot/UniProtGraph.java]: ../UniProtGraph.java.md
[main/java/com/bio4j/model/uniprot/vertices/SequenceCaution.java]: ../vertices/SequenceCaution.java.md
[main/java/com/bio4j/model/uniprot/vertices/Disease.java]: ../vertices/Disease.java.md
[main/java/com/bio4j/model/uniprot/vertices/UniGene.java]: ../vertices/UniGene.java.md
[main/java/com/bio4j/model/uniprot/vertices/InterPro.java]: ../vertices/InterPro.java.md
[main/java/com/bio4j/model/uniprot/vertices/RefSeq.java]: ../vertices/RefSeq.java.md
[main/java/com/bio4j/model/uniprot/vertices/Organism.java]: ../vertices/Organism.java.md
[main/java/com/bio4j/model/uniprot/vertices/Country.java]: ../vertices/Country.java.md
[main/java/com/bio4j/model/uniprot/vertices/OnlineJournal.java]: ../vertices/OnlineJournal.java.md
[main/java/com/bio4j/model/uniprot/vertices/Thesis.java]: ../vertices/Thesis.java.md
[main/java/com/bio4j/model/uniprot/vertices/Pubmed.java]: ../vertices/Pubmed.java.md
[main/java/com/bio4j/model/uniprot/vertices/PIR.java]: ../vertices/PIR.java.md
[main/java/com/bio4j/model/uniprot/vertices/EMBL.java]: ../vertices/EMBL.java.md
[main/java/com/bio4j/model/uniprot/vertices/Institute.java]: ../vertices/Institute.java.md
[main/java/com/bio4j/model/uniprot/vertices/City.java]: ../vertices/City.java.md
[main/java/com/bio4j/model/uniprot/vertices/Reference.java]: ../vertices/Reference.java.md
[main/java/com/bio4j/model/uniprot/vertices/Submission.java]: ../vertices/Submission.java.md
[main/java/com/bio4j/model/uniprot/vertices/Protein.java]: ../vertices/Protein.java.md
[main/java/com/bio4j/model/uniprot/vertices/Journal.java]: ../vertices/Journal.java.md
[main/java/com/bio4j/model/uniprot/vertices/Dataset.java]: ../vertices/Dataset.java.md
[main/java/com/bio4j/model/uniprot/vertices/Publisher.java]: ../vertices/Publisher.java.md
[main/java/com/bio4j/model/uniprot/vertices/Patent.java]: ../vertices/Patent.java.md
[main/java/com/bio4j/model/uniprot/vertices/Pfam.java]: ../vertices/Pfam.java.md
[main/java/com/bio4j/model/uniprot/vertices/AlternativeProduct.java]: ../vertices/AlternativeProduct.java.md
[main/java/com/bio4j/model/uniprot/vertices/Keyword.java]: ../vertices/Keyword.java.md
[main/java/com/bio4j/model/uniprot/vertices/UnpublishedObservation.java]: ../vertices/UnpublishedObservation.java.md
[main/java/com/bio4j/model/uniprot/vertices/Book.java]: ../vertices/Book.java.md
[main/java/com/bio4j/model/uniprot/vertices/DB.java]: ../vertices/DB.java.md
[main/java/com/bio4j/model/uniprot/vertices/Isoform.java]: ../vertices/Isoform.java.md
[main/java/com/bio4j/model/uniprot/vertices/Consortium.java]: ../vertices/Consortium.java.md
[main/java/com/bio4j/model/uniprot/vertices/ReactomeTerm.java]: ../vertices/ReactomeTerm.java.md
[main/java/com/bio4j/model/uniprot/vertices/GeneName.java]: ../vertices/GeneName.java.md
[main/java/com/bio4j/model/uniprot/vertices/Ensembl.java]: ../vertices/Ensembl.java.md
[main/java/com/bio4j/model/uniprot/vertices/OnlineArticle.java]: ../vertices/OnlineArticle.java.md
[main/java/com/bio4j/model/uniprot/vertices/CommentType.java]: ../vertices/CommentType.java.md
[main/java/com/bio4j/model/uniprot/vertices/GeneLocation.java]: ../vertices/GeneLocation.java.md
[main/java/com/bio4j/model/uniprot/vertices/FeatureType.java]: ../vertices/FeatureType.java.md
[main/java/com/bio4j/model/uniprot/vertices/Taxon.java]: ../vertices/Taxon.java.md
[main/java/com/bio4j/model/uniprot/vertices/Article.java]: ../vertices/Article.java.md
[main/java/com/bio4j/model/uniprot/vertices/Kegg.java]: ../vertices/Kegg.java.md
[main/java/com/bio4j/model/uniprot/vertices/SubcellularLocation.java]: ../vertices/SubcellularLocation.java.md
[main/java/com/bio4j/model/uniprot/vertices/Person.java]: ../vertices/Person.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportIsoformSequences.java]: ../programs/ImportIsoformSequences.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProt.java]: ../programs/ImportUniProt.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportProteinInteractions.java]: ../programs/ImportProteinInteractions.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProtEdges.java]: ../programs/ImportUniProtEdges.java.md
[main/java/com/bio4j/model/uniprot/programs/XMLConstants.java]: ../programs/XMLConstants.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProtVertices.java]: ../programs/ImportUniProtVertices.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinOrganism.java]: ProteinOrganism.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinRefSeq.java]: ProteinRefSeq.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinSequenceCaution.java]: ProteinSequenceCaution.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceArticle.java]: ReferenceArticle.java.md
[main/java/com/bio4j/model/uniprot/edges/BookPublisher.java]: BookPublisher.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinPIR.java]: ProteinPIR.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinEMBL.java]: ProteinEMBL.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinUniGene.java]: ProteinUniGene.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinProteinInteraction.java]: ProteinProteinInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinKegg.java]: ProteinKegg.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinDisease.java]: ProteinDisease.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinFeature.java]: ProteinFeature.java.md
[main/java/com/bio4j/model/uniprot/edges/BookEditor.java]: BookEditor.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinIsoform.java]: ProteinIsoform.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinSubcellularLocation.java]: ProteinSubcellularLocation.java.md
[main/java/com/bio4j/model/uniprot/edges/IsoformProteinInteraction.java]: IsoformProteinInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinDataset.java]: ProteinDataset.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceAuthorPerson.java]: ReferenceAuthorPerson.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferencePatent.java]: ReferencePatent.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinIsoformInteraction.java]: ProteinIsoformInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceBook.java]: ReferenceBook.java.md
[main/java/com/bio4j/model/uniprot/edges/OnlineArticleOnlineJournal.java]: OnlineArticleOnlineJournal.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceOnlineArticle.java]: ReferenceOnlineArticle.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceAuthorConsortium.java]: ReferenceAuthorConsortium.java.md
[main/java/com/bio4j/model/uniprot/edges/ArticleJournal.java]: ArticleJournal.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinEnsembl.java]: ProteinEnsembl.java.md
[main/java/com/bio4j/model/uniprot/edges/ThesisInstitute.java]: ThesisInstitute.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinReactomeTerm.java]: ProteinReactomeTerm.java.md
[main/java/com/bio4j/model/uniprot/edges/SubcellularLocationParent.java]: SubcellularLocationParent.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinComment.java]: ProteinComment.java.md
[main/java/com/bio4j/model/uniprot/edges/TaxonParent.java]: TaxonParent.java.md
[main/java/com/bio4j/model/uniprot/edges/SubmissionDB.java]: SubmissionDB.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinInterPro.java]: ProteinInterPro.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceThesis.java]: ReferenceThesis.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinGeneName.java]: ProteinGeneName.java.md
[main/java/com/bio4j/model/uniprot/edges/OrganismTaxon.java]: OrganismTaxon.java.md
[main/java/com/bio4j/model/uniprot/edges/IsoformEventGenerator.java]: IsoformEventGenerator.java.md
[main/java/com/bio4j/model/uniprot/edges/BookCity.java]: BookCity.java.md
[main/java/com/bio4j/model/uniprot/edges/ArticlePubmed.java]: ArticlePubmed.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceSubmission.java]: ReferenceSubmission.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinKeyword.java]: ProteinKeyword.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceUnpublishedObservation.java]: ReferenceUnpublishedObservation.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinPfam.java]: ProteinPfam.java.md
[main/java/com/bio4j/model/uniprot/edges/InstituteCountry.java]: InstituteCountry.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinReference.java]: ProteinReference.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinGeneLocation.java]: ProteinGeneLocation.java.md