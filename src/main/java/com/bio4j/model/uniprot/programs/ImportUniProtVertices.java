package com.bio4j.model.uniprot.programs;

import com.bio4j.model.uniprot.UniProtGraph;
import com.bio4j.model.uniprot.UniProtGraph.Dataset;
import com.bio4j.model.uniprot.vertices.*;
import com.bio4j.model.uniprot.edges.*;

import static com.bio4j.model.uniprot.programs.XMLConstants.*;

import com.bio4j.angulillos.UntypedGraph;

import org.jdom2.*;
import com.bio4j.xml.XMLUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public abstract class ImportUniProtVertices<I extends UntypedGraph<RV,RVT,RE,RET>,RV,RVT,RE,RET> {

  protected abstract UniProtGraph<I,RV,RVT,RE,RET> config(File dbFolder);

  private static final Logger logger = Logger.getLogger("ImportUniProtVertices");
  private static FileHandler fh;

  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  final HashSet<String> alternativeProductTypeNameSet = new HashSet<String>();
  final HashSet<String> articleTitleNameSet           = new HashSet<String>();
  final HashSet<String> bookNameSet                   = new HashSet<String>();
  final HashSet<String> cityNameSet                   = new HashSet<String>();
  final HashSet<String> commentTypeNameSet            = new HashSet<String>();
  final HashSet<String> consortiumNameSet             = new HashSet<String>();
  final HashSet<String> countryNameSet                = new HashSet<String>();
  final HashSet<String> NameSet                = new HashSet<String>();
  final HashSet<String> dbNameSet                     = new HashSet<String>();
  final HashSet<String> diseaseIdSet                  = new HashSet<String>();
  final HashSet<String> eMBLIdSet                     = new HashSet<String>();
  final HashSet<String> ensemblIdSet                  = new HashSet<String>();
  final HashSet<String> featureTypeNameSet            = new HashSet<String>();
  final HashSet<String> geneLocationNameSet           = new HashSet<String>();
  final HashSet<String> geneNameSet                   = new HashSet<String>();
  final HashSet<String> instituteNameSet              = new HashSet<String>();
  final HashSet<String> interproIdSet                 = new HashSet<String>();
  final HashSet<String> isoformIdSet                  = new HashSet<String>();
  final HashSet<String> journalNameSet                = new HashSet<String>();
  final HashSet<String> keggIdSet                     = new HashSet<String>();
  final HashSet<String> keywordIdSet                  = new HashSet<String>();
  final HashSet<String> onlineArticleTitleSet         = new HashSet<String>();
  final HashSet<String> onlineJournalNameSet          = new HashSet<String>();
  final HashSet<String> organismScientificNameSet     = new HashSet<String>();
  final HashSet<String> patentNumberSet               = new HashSet<String>();
  final HashSet<String> personNameSet                 = new HashSet<String>();
  final HashSet<String> pfamIdSet                     = new HashSet<String>();
  final HashSet<String> pIRIdSet                      = new HashSet<String>();
  final HashSet<String> publisherNameSet              = new HashSet<String>();
  final HashSet<String> pubmedIdSet                   = new HashSet<String>();
  final HashSet<String> reactomeTermIdSet             = new HashSet<String>();
  final HashSet<String> refSeqIdSet                   = new HashSet<String>();
  final HashSet<String> sequenceCautionNameSet        = new HashSet<String>();
  final HashSet<String> subcellularLocationNameSet    = new HashSet<String>();
  final HashSet<String> submissionTitleSet            = new HashSet<String>();
  final HashSet<String> taxonNameSet                  = new HashSet<String>();
  final HashSet<String> thesisTitleSet                = new HashSet<String>();
  final HashSet<String> uniGeneIdSet                  = new HashSet<String>();

  protected void importUniProtVertices(File inFile, File dbFolder) {

    final long initTime = System.nanoTime();

    final UniProtGraph<I,RV,RVT,RE,RET> graph = config(dbFolder);

    // TODO there should be a better way of initializing these things
    Protein<I,RV,RVT,RE,RET> protein = null;
    BufferedWriter statsBuff = null;

    int proteinCounter      = 0;
    final int limitForPrintingOut = 10000;

    try {

      // This block configures the logger with handler and formatter
      fh = new FileHandler("ImportUniProtVertices.log", false);

      final SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
      logger.addHandler(fh);
      logger.setLevel(Level.ALL);

      statsBuff = new BufferedWriter(new FileWriter(new File("ImportUniProtVerticesStats.txt")));

      /* Iterate over the input file lines */
      final BufferedReader inFileReader = new BufferedReader(new FileReader(inFile));
      String line;
      while((line = inFileReader.readLine()) != null && line.trim().startsWith("<"+ENTRY_TAG_NAME)) {
        // this will advance the reader until the next extry
        final Element entryXMLElem = XMLUtils.uniProtEntryFrom(line, inFileReader);

          protein = importProteinFrom (entryXMLElem, graph);

          importProteinReferences   (entryXMLElem, graph);
          importProteinComments     (entryXMLElem, graph, protein, protein.get(protein.type().sequence));
          importProteinFeatures     (entryXMLElem, graph, protein);
          importProteinCitations    (entryXMLElem, graph, protein);
          importProteinGeneLocation (entryXMLElem, graph);

          proteinCounter++;

          if((proteinCounter % limitForPrintingOut) == 0) {

            final String countProteinsSt = proteinCounter +" proteins inserted!!";

            logger.log(Level.INFO, countProteinsSt);
            graph.raw().commit();
          }
      }
    }
    catch (Exception e) {

      logger.log(Level.SEVERE, "Exception retrieving protein " + protein.get(protein.type().accession));
      logger.log(Level.SEVERE, e.getMessage());

      StackTraceElement[] trace = e.getStackTrace();

      for (StackTraceElement stackTraceElement: trace) {

        logger.log(Level.SEVERE, stackTraceElement.toString());
      }
    }
    finally {

      try {

        /* This should write everything */
        graph.raw().shutdown();

        // closing logger file handler
        fh.close();

        long elapsedTime        = System.nanoTime() - initTime;
        long elapsedSeconds     = Math.round((elapsedTime / 1000000000.0));
        long hours              = elapsedSeconds / 3600;
        long minutes            = (elapsedSeconds % 3600) / 60;
        long seconds            = (elapsedSeconds % 3600) % 60;

        statsBuff.write("Statistics for program ImportUniProtVertices:\nInput file: " + inFile.getName()
        + "\nThere were " + proteinCounter + " proteins inserted.\n"
        + "The elapsed time was: " + hours + "h " + minutes + "m " + seconds + "s\n");

        statsBuff.close();
      }
      catch (IOException ex) {

        Logger.getLogger(ImportUniProtVertices.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /*
    This method gets the protein data from the corresponding XML element and writes it to the graph
  */
  private Protein<I,RV,RVT,RE,RET> importProteinFrom(
    Element entryXMLElem,
    UniProtGraph<I,RV,RVT,RE,RET> g
  )
    throws ParseException
  {

    // known to be there according to the schema
    final String accessionV = entryXMLElem.getChildText(ENTRY.ACCESSION.element);

    Protein<I,RV,RVT,RE,RET> p = g.addVertex(g.Protein());
    p.set( g.Protein().accession, accessionV );

    final String entryNameV = entryXMLElem.getChildText(ENTRY.NAME.element);
    p.set( p.type().entryName, entryNameV );

    final String datasetStr = entryXMLElem.getAttributeValue(ENTRY.DATASET.attribute);
    final Optional<Dataset> optDatasetV = Dataset.fromRepr(datasetStr);
    optDatasetV.ifPresent(
      datasetV -> p.set( p.type().dataset, datasetV )
    );

    final Optional<String> optGeneName =
      entryXMLElem
        .getChild(ENTRY.GENE.element)
        .getChildren(ENTRY.GENE.NAME.element)
        .stream()
        .filter( elem -> elem.getAttributeValue(ENTRY.GENE.NAME.TYPE.attribute).equals(ENTRY.GENE.NAME.TYPE.PRIMARY) )
        .map( Element::getText )
        .findFirst();

    optGeneName.ifPresent(
      geneNameV -> p.set( p.type().geneName, geneNameV )
    );

    final Element proteinElem = entryXMLElem.getChild(ENTRY.PROTEIN.element);
    final String fullNameV = proteinElem
      .getChild(ENTRY.PROTEIN.RECOMMENDEDNAME.element)
      .getChildText(ENTRY.PROTEIN.RECOMMENDEDNAME.FULLNAME.element);

    p.set( p.type().fullName, fullNameV );

    // TODO get all other names from the protein element

    final Element sequenceElem = entryXMLElem.getChild(ENTRY.SEQUENCE.element);

    final String sequenceV  = sequenceElem.getText();
    final Integer lengthV   = Integer.parseInt( sequenceElem.getAttributeValue(ENTRY.SEQUENCE.LENGTH.attribute) );
    final Integer massV     = Integer.parseInt( sequenceElem.getAttributeValue(ENTRY.SEQUENCE.MASS.attribute) );

    p.set( p.type().sequence, sequenceV );
    p.set( p.type().length, lengthV );
    p.set( p.type().mass, massV );

    return p;
  }

  private void importProteinReferences(
    Element entryXMLElem,
    UniProtGraph<I,RV,RVT,RE,RET> graph
  )
  {
    //-----db references-------------
    final List<Element> dbReferenceList = entryXMLElem.getChildren(DB_REFERENCE_TAG_NAME);

    for(Element dbReferenceElem: dbReferenceList) {

      String refId = dbReferenceElem.getAttributeValue("id");

      switch(dbReferenceElem.getAttributeValue(DB_REFERENCE_TYPE_ATTRIBUTE)) {

        case "Ensembl": {

          // looking for Ensembl vertex
          if(!ensemblIdSet.contains(refId)) {

            ensemblIdSet.add(refId);

            if(!graph.ensemblIdIndex().getVertex(refId).isPresent()) {

              String moleculeIdSt = "";
              String proteinSequenceIdSt = "";
              String geneIdSt = "";

              List<Element> children = dbReferenceElem.getChildren("property");

              for (Element propertyElem: children) {

                if (propertyElem.getAttributeValue("type").equals("protein sequence ID")) {
                  proteinSequenceIdSt = propertyElem.getAttributeValue("value");
                }
                if (propertyElem.getAttributeValue("type").equals("gene ID")) {
                  geneIdSt = propertyElem.getAttributeValue("value");
                }
              }

              Element moleculeTag = dbReferenceElem.getChild("molecule");

              if(moleculeTag != null) {

                moleculeIdSt = moleculeTag.getAttributeValue("id");

                if(moleculeIdSt == null) {

                  moleculeTag.getText();

                  if(moleculeIdSt == null) {

                    moleculeIdSt = "";
                  }
                }
              }

              Ensembl<I,RV,RVT,RE,RET> ensembl = graph.addVertex(graph.Ensembl());
              ensembl.set(graph.Ensembl().id, refId);
              ensembl.set(graph.Ensembl().proteinSequenceId, proteinSequenceIdSt);
              ensembl.set(graph.Ensembl().moleculeId, moleculeIdSt);
              ensembl.set(graph.Ensembl().geneId, geneIdSt);
            }
          }

          break;
        }

        case "PIR": {

          if(!pIRIdSet.contains(refId)) {

            pIRIdSet.add(refId);

            if(!graph.pIRIdIndex().getVertex(refId).isPresent()) {

              String entryNameSt = "";
              List<Element> children = dbReferenceElem.getChildren("property");

              for(Element propertyElem: children) {

                if(propertyElem.getAttributeValue("type").equals("entry name")) {
                  entryNameSt = propertyElem.getAttributeValue("value");
                }
              }

              PIR<I,RV,RVT,RE,RET> pIR = graph.addVertex(graph.PIR());
              pIR.set(graph.PIR().entryName, entryNameSt);
              pIR.set(graph.PIR().id, refId);
            }
          }

          break;
        }

        case "UniGene": {

          // looking for UniGene vertex
          if(!uniGeneIdSet.contains(refId)) {

            uniGeneIdSet.add(refId);

            if(!graph.uniGeneIdIndex().getVertex(refId).isPresent()) {

              UniGene<I,RV,RVT,RE,RET> uniGene = graph.addVertex(graph.UniGene());
              uniGene.set(graph.UniGene().id, refId);
            }
          }

          break;
        }

        case "KEGG": {

          //looking for Kegg vertex
          if(!keggIdSet.contains(refId)) {

            keggIdSet.add(refId);

            if(!graph.keggIdIndex().getVertex(refId).isPresent()) {

              Kegg<I,RV,RVT,RE,RET> kegg = graph.addVertex(graph.Kegg());
              kegg.set(graph.Kegg().id, refId);
            }
          }

          break;
        }

        case "EMBL": {

          //looking for EMBL vertex
          if(!eMBLIdSet.contains(refId)) {

            eMBLIdSet.add(refId);

            if(!graph.eMBLIdIndex().getVertex(refId).isPresent()) {

              String moleculeTypeSt       = "";
              String proteinSequenceIdSt  = "";

              List<Element> children = dbReferenceElem.getChildren("property");

              for(Element propertyElem: children) {

                if(propertyElem.getAttributeValue("type").equals("protein sequence ID")) {

                  proteinSequenceIdSt = propertyElem.getAttributeValue("value");
                }

                if(propertyElem.getAttributeValue("type").equals("molecule type")) {

                  moleculeTypeSt = propertyElem.getAttributeValue("value");
                }
              }

              EMBL<I,RV,RVT,RE,RET> embl = graph.addVertex(graph.EMBL());
              embl.set(graph.EMBL().id, refId);
              embl.set(graph.EMBL().proteinSequenceId, proteinSequenceIdSt);
              embl.set(graph.EMBL().moleculeType, moleculeTypeSt);
            }
          }

          break;
        }

        case "RefSeq": {

          //looking for RefSeq vertex
          if(!refSeqIdSet.contains(refId)) {

            refSeqIdSet.add(refId);

            if(!graph.refSeqIdIndex().getVertex(refId).isPresent()) {

              String nucleotideSequenceIdSt = "";
              List<Element> children = dbReferenceElem.getChildren("property");

              for(Element propertyElem: children) {

                if(propertyElem.getAttributeValue("type").equals("nucleotide sequence ID")) {

                  nucleotideSequenceIdSt = propertyElem.getAttributeValue("value");
                }
              }

              RefSeq<I,RV,RVT,RE,RET> refSeq = graph.addVertex(graph.RefSeq());
              refSeq.set(graph.RefSeq().id, refId);
              refSeq.set(graph.RefSeq().nucleotideSequenceId, nucleotideSequenceIdSt);
            }
          }

          break;
        }

        case "Reactome": {

          if(!reactomeTermIdSet.contains(refId)) {

            reactomeTermIdSet.add(refId);

            if(!graph.reactomeTermIdIndex().getVertex(refId).isPresent()) {

              Element propertyElem = dbReferenceElem.getChild("property");
              String pathwayName = "";

              if(propertyElem.getAttributeValue("type").equals("pathway name")) {

                pathwayName = propertyElem.getAttributeValue("value");
              }

              ReactomeTerm<I,RV,RVT,RE,RET> reactomeTerm = graph.addVertex(graph.ReactomeTerm());
              reactomeTerm.set(graph.ReactomeTerm().id, refId);
              reactomeTerm.set(graph.ReactomeTerm().pathwayName, pathwayName);
            }
          }

          break;
        }

        case INTERPRO_DB_REFERENCE_TYPE: {

          final String interproId = dbReferenceElem.getAttributeValue(DB_REFERENCE_ID_ATTRIBUTE);

          if(!interproIdSet.contains(interproId)) {

            interproIdSet.add(interproId);

            if(!graph.interproIdIndex().getVertex(interproId).isPresent()) {

              String interproEntryNameSt = "";

              List<Element> properties = dbReferenceElem.getChildren(DB_REFERENCE_PROPERTY_TAG_NAME);

              for (Element prop: properties) {

                if (prop.getAttributeValue(DB_REFERENCE_TYPE_ATTRIBUTE).equals(INTERPRO_ENTRY_NAME)) {

                  interproEntryNameSt = prop.getAttributeValue(DB_REFERENCE_VALUE_ATTRIBUTE);
                  break;
                }
              }

              final InterPro<I,RV,RVT,RE,RET> interpro = graph.addVertex(graph.InterPro());
              interpro.set(graph.InterPro().id, interproId);
              interpro.set(graph.InterPro().name, interproEntryNameSt);
            }
          }

          break;
        }

        case "Pfam": {

          final String pfamId = dbReferenceElem.getAttributeValue(DB_REFERENCE_ID_ATTRIBUTE);

          if(!pfamIdSet.contains(pfamId)) {

            pfamIdSet.add(pfamId);

            if(!graph.pfamIdIndex().getVertex(pfamId).isPresent()) {

              String pfamEntryNameSt = "";
              List<Element> properties = dbReferenceElem.getChildren(DB_REFERENCE_PROPERTY_TAG_NAME);

              for(Element prop: properties) {

                if(prop.getAttributeValue(DB_REFERENCE_TYPE_ATTRIBUTE).equals("entry name")) {

                  pfamEntryNameSt = prop.getAttributeValue(DB_REFERENCE_VALUE_ATTRIBUTE);
                  break;
                }
              }

              Pfam<I,RV,RVT,RE,RET> pfam = graph.addVertex(graph.Pfam());
              pfam.set(graph.Pfam().id, pfamId);
              pfam.set(graph.Pfam().name, pfamEntryNameSt);
            }
          }

          break;
        }
      }
    }
  }

  private void importProteinFeatures(
    Element entryXMLElem,
    UniProtGraph<I,RV,RVT,RE,RET> graph,
    Protein<I,RV,RVT,RE,RET> protein
  )
  {

    //--------------------------------features----------------------------------------------------
    final List<Element> featuresList = entryXMLElem.getChildren(FEATURE_TAG_NAME);

    for(Element featureElem: featuresList) {

      final String featureTypeSt = featureElem.getAttributeValue(FEATURE_TYPE_ATTRIBUTE);

      if(!featureTypeNameSet.contains(featureTypeSt)) {

        featureTypeNameSet.add(featureTypeSt);

        if(!graph.featureTypeNameIndex().getVertex(featureTypeSt).isPresent()) {

          final FeatureType<I,RV,RVT,RE,RET> feature = graph.addVertex(graph.FeatureType());
          feature.set(graph.FeatureType().name, featureTypeSt);
        }
      }
    }
  }

  private void importProteinGeneLocation(
    Element entryXMLElem,
    UniProtGraph<I,RV,RVT,RE,RET> graph
  )
  {
    final List<Element> geneLocationElements = entryXMLElem.getChildren(GENE_LOCATION_TAG_NAME);

    for(Element geneLocationElem: geneLocationElements) {

      final String geneLocationTypeSt = geneLocationElem.getAttributeValue("type");

      if(!geneLocationNameSet.contains(geneLocationTypeSt)) {

        geneLocationNameSet.add(geneLocationTypeSt);

        if(!graph.geneLocationNameIndex().getVertex(geneLocationTypeSt).isPresent()) {

          GeneLocation<I,RV,RVT,RE,RET> geneLocation = graph.addVertex(graph.GeneLocation());
          geneLocation.set(graph.GeneLocation().name, geneLocationTypeSt);
        }
      }
    }
  }

  private void importProteinComments(
    Element entryXMLElem,
    UniProtGraph<I,RV,RVT,RE,RET> graph,
    Protein<I,RV,RVT,RE,RET> protein,
    String proteinSequence
  )
  {

    final List<Element> comments = entryXMLElem.getChildren(COMMENT_TAG_NAME);

    for(Element commentElem: comments) {

      final String commentTypeSt = commentElem.getAttributeValue(COMMENT_TYPE_ATTRIBUTE);

      if(!commentTypeNameSet.contains(commentTypeSt)) {

        commentTypeNameSet.add(commentTypeSt);

        if(!graph.commentTypeNameIndex().getVertex(commentTypeSt).isPresent()) {

          CommentType<I,RV,RVT,RE,RET> comment = graph.addVertex(graph.CommentType());
          comment.set(graph.CommentType().name, commentTypeSt);
        }
      }

      switch(commentTypeSt) {

        case COMMENT_TYPE_DISEASE: {

          final Element diseaseElement = commentElem.getChild("disease");

          if(diseaseElement != null) {

            final String diseaseId          = diseaseElement.getAttributeValue("id");
            final String diseaseName        = diseaseElement.getChildText("name");
            final String diseaseDescription = diseaseElement.getChildText("description");
            final String diseaseAcronym     = diseaseElement.getChildText("acronym");

            if(diseaseId != null) {

              if(!diseaseIdSet.contains(diseaseId)) {

                diseaseIdSet.add(diseaseId);

                if(!graph.diseaseIdIndex().getVertex(diseaseId).isPresent()) {

                  final Disease<I,RV,RVT,RE,RET> disease = graph.addVertex(graph.Disease());
                  disease.set(graph.Disease().name, diseaseName);
                  disease.set(graph.Disease().id, diseaseId);
                  disease.set(graph.Disease().acronym, diseaseAcronym);
                  disease.set(graph.Disease().description, diseaseDescription);
                }
              }
            }
          }

          break;
        }

        case COMMENT_TYPE_SUBCELLULAR_LOCATION: {

          final List<Element> subcLocations = commentElem.getChildren(SUBCELLULAR_LOCATION_TAG_NAME);

          for(Element subcLocation: subcLocations) {

            final List<Element> locations = subcLocation.getChildren(LOCATION_TAG_NAME);

            for(int i = 0; i < locations.size(); i++) {

              final String tempLocationSt = locations.get(i).getTextTrim();

              Optional<SubcellularLocation<I,RV,RVT,RE,RET>> tempLocationOptional =  graph.subcellularLocationNameIndex().getVertex(tempLocationSt);

              if(!subcellularLocationNameSet.contains(tempLocationSt)) {

                subcellularLocationNameSet.add(tempLocationSt);

                if(!graph.subcellularLocationNameIndex().getVertex(tempLocationSt).isPresent()) {

                  final SubcellularLocation<I,RV,RVT,RE,RET> tempLocation = graph.addVertex(graph.SubcellularLocation());
                  tempLocation.set(graph.SubcellularLocation().name, tempLocationSt);
                }
              }
            }
          }

          break;
        }

        case COMMENT_ALTERNATIVE_PRODUCTS_TYPE: {

          final List<Element> eventList   = commentElem.getChildren("event");
          final List<Element> isoformList = commentElem.getChildren("isoform");

          for(Element isoformElem: isoformList) {

            final String isoformIdSt = isoformElem.getChildText("id");

            String isoformNoteSt  = isoformElem.getChildText("note");
            String isoformNameSt  = isoformElem.getChildText("name");
            String isoformSeqSt   = "";

            Element isoSeqElem = isoformElem.getChild("sequence");

            if(isoSeqElem != null) {

              final String isoSeqTypeSt = isoSeqElem.getAttributeValue("type");

              if(isoSeqTypeSt.equals("displayed")) {

                isoformSeqSt = proteinSequence;
              }
            }

            if(isoformNoteSt == null) {

              isoformNoteSt = "";
            }

            if(isoformNameSt == null) {

              isoformNameSt = "";
            }

            if(!isoformIdSet.contains(isoformIdSt)) {

              isoformIdSet.add(isoformIdSt);

              if(!graph.isoformIdIndex().getVertex(isoformIdSt).isPresent()) {

                final Isoform<I,RV,RVT,RE,RET> isoform = graph.addVertex(graph.Isoform());
                isoform.set(graph.Isoform().name, isoformNameSt);
                isoform.set(graph.Isoform().note, isoformNoteSt);
                isoform.set(graph.Isoform().sequence, isoformSeqSt);
                isoform.set(graph.Isoform().id, isoformIdSt);
              }
            }

            for(Element eventElem: eventList) {

              final String eventTypeSt = eventElem.getAttributeValue("type");

              if(!alternativeProductTypeNameSet.contains(eventTypeSt)) {

                alternativeProductTypeNameSet.add(eventTypeSt);

                if(!graph.alternativeProductNameIndex().getVertex(eventTypeSt).isPresent()) {

                  final AlternativeProduct<I,RV,RVT,RE,RET> alternativeProduct = graph.addVertex(graph.AlternativeProduct());
                  alternativeProduct.set(graph.AlternativeProduct().name, eventTypeSt);
                }
              }
            }
          }

          break;
        }

        case COMMENT_SEQUENCE_CAUTION_TYPE: {

          final Element conflictElem = commentElem.getChild("conflict");

          if(conflictElem != null) {

            final String conflictTypeSt = conflictElem.getAttributeValue("type");

            if(!sequenceCautionNameSet.contains(conflictTypeSt)) {

              sequenceCautionNameSet.add(conflictTypeSt);

              if(!graph.sequenceCautionNameIndex().getVertex(conflictTypeSt).isPresent()) {

                SequenceCaution<I,RV,RVT,RE,RET> sequenceCaution = graph.addVertex(graph.SequenceCaution());
                sequenceCaution.set(graph.SequenceCaution().name, conflictTypeSt);
              }
            }
          }

          break;
        }
      }
    }
  }

  private void importProteinCitations(
    Element entryXMLElem,
    UniProtGraph<I,RV,RVT,RE,RET> graph,
    Protein<I,RV,RVT,RE,RET> protein
  )
  {

    final List<Element> referenceList = entryXMLElem.getChildren(REFERENCE_TAG_NAME);

    for(Element referenceElement: referenceList) {

      final List<Element> citationsList = referenceElement.getChildren(CITATION_TAG_NAME);

      for(Element citation: citationsList) {

        String citationType = citation.getAttributeValue(DB_REFERENCE_TYPE_ATTRIBUTE);

        List<Person<I,RV,RVT,RE,RET>> authorsPerson         = new ArrayList<>();
        List<Consortium<I,RV,RVT,RE,RET>> authorsConsortium = new ArrayList<>();

        List<Element> authorPersonElems     = citation.getChild("authorList").getChildren("person");
        List<Element> authorConsortiumElems = citation.getChild("authorList").getChildren("consortium");

        for(Element personElement: authorPersonElems) {

          final String personName = personElement.getAttributeValue("name");

          if(!personNameSet.contains(personName)) {

            personNameSet.add(personName);

            if(!graph.personNameIndex().getVertex(personName).isPresent()) {

              final Person<I,RV,RVT,RE,RET> person = graph.addVertex(graph.Person());
              person.set(graph.Person().name, personName);
            }
          }
        }

        for(Element consortiumElement: authorConsortiumElems) {

          final String consortiumName = consortiumElement.getAttributeValue("name");

          if(!consortiumNameSet.contains(consortiumName)) {

            consortiumNameSet.add(consortiumName);

            if(!graph.consortiumNameIndex().getVertex(consortiumName).isPresent()) {

              final Consortium<I,RV,RVT,RE,RET> consortium = graph.addVertex(graph.Consortium());
              consortium.set(graph.Consortium().name, consortiumName);
            }
          }
        }

        // start the dance on citation type
        switch(citationType) {

          case THESIS_CITATION_TYPE: {

            String titleSt = citation.getChildText("title");

            if(titleSt == null) {

              titleSt = "";
            }
            else {

              if(!thesisTitleSet.contains(titleSt)) {

                thesisTitleSet.add(titleSt);

                if(!graph.thesisTitleIndex().getVertex(titleSt).isPresent()) {

                  final Thesis<I,RV,RVT,RE,RET> thesis = graph.addVertex(graph.Thesis());
                  thesis.set(graph.Thesis().title, titleSt);

                  String dateSt = citation.getAttributeValue("date");

                  if(dateSt == null) {

                    dateSt = "";
                  }

                  Reference<I,RV,RVT,RE,RET> reference = graph.addVertex(graph.Reference());
                  reference.set(graph.Reference().id, titleSt + graph.Thesis().name());
                  reference.set(graph.Reference().date, dateSt);
                  reference.addOutEdge(graph.ReferenceThesis(), thesis);
                }

                //-----------institute-----------------------------
                String instituteSt = citation.getAttributeValue("institute");
                String countrySt = citation.getAttributeValue("country");

                if (instituteSt != null) {

                  if(!instituteNameSet.contains(instituteSt)) {

                    instituteNameSet.add(instituteSt);

                    if(!graph.instituteNameIndex().getVertex(instituteSt).isPresent()) {
                      Institute<I,RV,RVT,RE,RET> institute = graph.addVertex(graph.Institute());
                      institute.set(graph.Institute().name, instituteSt);
                    }
                  }

                  if(countrySt != null) {

                    if(!countryNameSet.contains(countrySt)) {

                      countryNameSet.add(countrySt);

                      if(!graph.countryNameIndex().getVertex(countrySt).isPresent()) {

                        Country<I,RV,RVT,RE,RET> country = graph.addVertex(graph.Country());
                        country.set(graph.Country().name, countrySt);
                      }
                    }
                  }
                }
              }
            }
            break;
          }

          case PATENT_CITATION_TYPE: {

            String numberSt = citation.getAttributeValue("number");
            String titleSt  = citation.getChildText("title");

            if(titleSt == null) {
              titleSt = "";
            }
            if(numberSt == null) {
              numberSt = "";
            }

            if (!numberSt.equals("")) {

              if(!patentNumberSet.contains(numberSt)) {

                patentNumberSet.add(numberSt);

                if(!graph.patentNumberIndex().getVertex(numberSt).isPresent()) {

                  Patent<I,RV,RVT,RE,RET> patent = graph.addVertex(graph.Patent());
                  patent.set(graph.Patent().number, numberSt);
                  patent.set(graph.Patent().title, titleSt);

                  String dateSt = citation.getAttributeValue("date");
                  if (dateSt == null) {
                    dateSt = "";
                  }

                  Reference<I,RV,RVT,RE,RET> reference = graph.addVertex(graph.Reference());
                  reference.set(graph.Reference().id, numberSt + graph.Patent().name());
                  reference.set(graph.Reference().date, dateSt);
                  reference.addOutEdge(graph.ReferencePatent(), patent);
                }
              }
            }

            break;
          }

          case SUBMISSION_CITATION_TYPE: {

            String titleSt = citation.getChildText("title");
            String dbSt = citation.getAttributeValue("db");
            if (titleSt == null) {
              titleSt = "";
            }
            else {

              if(!submissionTitleSet.contains(titleSt)) {

                submissionTitleSet.add(titleSt);

                if(!graph.submissionTitleIndex().getVertex(titleSt).isPresent()) {

                  Submission<I,RV,RVT,RE,RET> submission = graph.addVertex(graph.Submission());
                  submission.set(graph.Submission().title, titleSt);

                  String dateSt = citation.getAttributeValue("date");
                  if(dateSt == null) {
                    dateSt = "";
                  }

                  Reference<I,RV,RVT,RE,RET> reference = graph.addVertex(graph.Reference());
                  reference.set(graph.Reference().id, titleSt + graph.Submission().name());
                  reference.set(graph.Reference().date, dateSt);
                  reference.addOutEdge(graph.ReferenceSubmission(), submission);
                }

                if(dbSt != null) {

                  if(!dbNameSet.contains(dbSt)) {

                    dbNameSet.add(dbSt);

                    if(!graph.dbNameIndex().getVertex(dbSt).isPresent()) {

                      DB<I,RV,RVT,RE,RET> db = graph.addVertex(graph.DB());
                      db.set(graph.DB().name, dbSt);
                    }
                  }
                }
              }
            }

            break;
          }

          case BOOK_CITATION_TYPE: {

            String nameSt = citation.getAttributeValue("name");
            String titleSt = citation.getChildText("title");
            String publisherSt = citation.getAttributeValue("publisher");
            String citySt = citation.getAttributeValue("city");

            if(nameSt == null) {
              nameSt = "";
            }

            if(titleSt == null) {
              titleSt = "";
            }

            if(publisherSt == null) {
              publisherSt = "";
            }

            if(citySt == null) {
              citySt = "";
            }

            if(!bookNameSet.contains(nameSt)) {

              bookNameSet.add(nameSt);

              if(!graph.bookNameIndex().getVertex(nameSt).isPresent()) {

                final Book<I,RV,RVT,RE,RET> book = graph.addVertex(graph.Book());
                book.set(graph.Book().name, nameSt);

                String dateSt = citation.getAttributeValue("date");

                if (dateSt == null) {
                  dateSt = "";
                }

                final Reference<I,RV,RVT,RE,RET> reference = graph.addVertex(graph.Reference());
                reference.set(graph.Reference().id, nameSt + graph.Book().name());
                reference.set(graph.Reference().date, dateSt);
                reference.addOutEdge(graph.ReferenceBook(), book);

                //---editor association-----
                final Element editorListElem = citation.getChild("editorList");

                if (editorListElem != null) {

                  List<Element> editorsElems = editorListElem.getChildren("person");

                  for (Element personElement : editorsElems) {

                    final String personName = personElement.getAttributeValue("name");

                    if(!personNameSet.contains(personName)) {

                      personNameSet.add(personName);

                      if(!graph.personNameIndex().getVertex(personName).isPresent()) {

                        final Person<I,RV,RVT,RE,RET> editor = graph.addVertex(graph.Person());
                        editor.set(graph.Person().name, personName);
                      }
                    }
                  }
                }

                //----publisher--
                if (!publisherSt.equals("")) {

                  if(!publisherNameSet.contains(publisherSt)) {

                    publisherNameSet.add(publisherSt);

                    if(!graph.publisherNameIndex().getVertex(publisherSt).isPresent()) {

                      final Publisher<I,RV,RVT,RE,RET> publisher = graph.addVertex(graph.Publisher());
                      publisher.set(graph.Publisher().name, publisherSt);
                    }
                  }
                }

                //-----city-----
                if (!citySt.equals("")) {

                  if(!cityNameSet.contains(citySt)) {

                    cityNameSet.add(citySt);

                    if(!graph.cityNameIndex().getVertex(citySt).isPresent()) {

                      City<I,RV,RVT,RE,RET> city = graph.addVertex(graph.City());
                      city.set(graph.City().name, citySt);
                    }
                  }
                }
              }

            }

            break;
          }

          case ONLINE_ARTICLE_CITATION_TYPE: {

            String nameSt   = citation.getAttributeValue("name");
            String titleSt  = citation.getChildText("title");

            if (titleSt == null) {
              titleSt = "";
            }

            if (nameSt == null) {
              nameSt = "";
            }

            if(!titleSt.equals("")) {

              if(!onlineArticleTitleSet.contains(titleSt)) {

                onlineArticleTitleSet.add(titleSt);

                if(!graph.onlineArticleTitleIndex().getVertex(titleSt).isPresent()) {

                  final OnlineArticle<I,RV,RVT,RE,RET> onlineArticle = graph.addVertex(graph.OnlineArticle());
                  onlineArticle.set(graph.OnlineArticle().title, titleSt);

                  String dateSt = citation.getAttributeValue("date");
                  if (dateSt == null) {
                    dateSt = "";
                  }

                  final Reference<I,RV,RVT,RE,RET> reference = graph.addVertex(graph.Reference());
                  reference.set(graph.Reference().id, titleSt + graph.OnlineArticle().name());
                  reference.set(graph.Reference().date, dateSt);
                  reference.addOutEdge(graph.ReferenceOnlineArticle(), onlineArticle);

                  //------online journal-----------
                  if (!nameSt.equals("")) {

                    if(!onlineJournalNameSet.contains(nameSt)){

                      onlineJournalNameSet.add(nameSt);

                      if(!graph.onlineJournalNameIndex().getVertex(nameSt).isPresent()) {

                        OnlineJournal<I,RV,RVT,RE,RET> onlineJournal = graph.addVertex(graph.OnlineJournal());
                        onlineJournal.set(graph.OnlineJournal().name, nameSt);
                      }
                    }
                  }
                }
              }
            }

            break;
          }

          case ARTICLE_CITATION_TYPE: {

            String journalNameSt = citation.getAttributeValue("name");
            String titleSt = citation.getChildText("title");
            String doiSt = "";
            String medlineSt = "";
            String pubmedId = "";

            if (journalNameSt == null) {
              journalNameSt = "";
            }

            if (titleSt == null) {
              titleSt = "";
            }

            final List<Element> dbReferences = citation.getChildren("dbReference");

            for(Element tempDbRef: dbReferences) {

              switch(tempDbRef.getAttributeValue("type")) {

                case "DOI":
                doiSt = tempDbRef.getAttributeValue("id");
                break;
                case "MEDLINE":
                medlineSt = tempDbRef.getAttributeValue("id");
                break;
                case "PubMed":
                pubmedId = tempDbRef.getAttributeValue("id");
                break;
              }
            }

            if (titleSt != "") {

              if(!articleTitleNameSet.contains(titleSt)) {

                articleTitleNameSet.add(titleSt);

                if(!graph.articleTitleIndex().getVertex(titleSt).isPresent()) {

                  Article<I,RV,RVT,RE,RET> article = graph.addVertex(graph.Article());
                  article.set(graph.Article().title, titleSt);
                  article.set(graph.Article().doId, doiSt);

                  String dateSt = citation.getAttributeValue("date");
                  if (dateSt == null) {
                    dateSt = "";
                  }

                  final Reference<I,RV,RVT,RE,RET> reference = graph.addVertex(graph.Reference());
                  reference.set(graph.Reference().id, titleSt + graph.Article().name());
                  reference.set(graph.Reference().date, dateSt);
                  reference.addOutEdge(graph.ReferenceArticle(), article);

                  if(pubmedId != "") {

                    if(!pubmedIdSet.contains(pubmedId)) {

                      pubmedIdSet.add(pubmedId);

                      if(!graph.pubmedIdIndex().getVertex(pubmedId).isPresent()) {

                        Pubmed<I,RV,RVT,RE,RET> pubmed = graph.addVertex(graph.Pubmed());
                        pubmed.set(graph.Pubmed().id, pubmedId);
                      }
                    }
                  }

                  //------journal-----------
                  if (!journalNameSt.equals("")) {

                    if(!journalNameSet.contains(journalNameSt)) {

                      journalNameSet.add(journalNameSt);

                      if(!graph.journalNameIndex().getVertex(journalNameSt).isPresent()) {

                        Journal<I,RV,RVT,RE,RET> journal = graph.addVertex(graph.Journal());
                        journal.set(graph.Journal().name, journalNameSt);
                      }
                    }
                  }
                }
              }
            }
            break;
          }
        }
      }
    }
  }

  /*
  ### Random helper methods
  */
  private Date parseDate(String date) throws ParseException {

    return dateFormat.parse(date);
  }

  private static String getProteinFullName(Element proteinElement) {

    if(proteinElement == null) {

      return "";
    }
    else {

      final Element recElem = proteinElement.getChild(PROTEIN_RECOMMENDED_NAME_TAG_NAME);

      if(recElem == null) {

        return "";
      }
      else {

        return recElem.getChildText(PROTEIN_FULL_NAME_TAG_NAME);
      }
    }
  }

  private static String getProteinShortName(Element proteinElement) {

    if(proteinElement == null) {

      return "";
    }
    else {

      final Element recElem = proteinElement.getChild(PROTEIN_RECOMMENDED_NAME_TAG_NAME);

      if(recElem == null) {

        return "";
      }
      else {

        return recElem.getChildText(PROTEIN_SHORT_NAME_TAG_NAME);
      }
    }
  }
}
