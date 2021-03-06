
```java
package com.bio4j.model.go.programs;

import com.bio4j.model.go.GoGraph;
import com.bio4j.model.go.vertices.GoTerm;
import com.bio4j.model.go.vertices.SubOntologies;
import com.ohnosequences.xml.api.model.XMLElement;
import org.jdom2.Element;

import com.bio4j.angulillos.*;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
```


## GO data import


```java
abstract class ImportGO<I extends UntypedGraph<RV,RVT,RE,RET>,RV,RVT,RE,RET> {
```


### XML tag names

These global constants correspond to the GO XML file which we use as input.


```java
  public static final String TERM_TAG_NAME          = "term";
  public static final String ID_TAG_NAME            = "id";
  public static final String NAME_TAG_NAME          = "name";
  public static final String DEF_TAG_NAME           = "def";
  public static final String DEFSTR_TAG_NAME        = "defstr";
  public static final String IS_ROOT_TAG_NAME       = "is_root";
  public static final String IS_OBSOLETE_TAG_NAME   = "is_obsolete";
  public static final String COMMENT_TAG_NAME       = "comment";
  public static final String NAMESPACE_TAG_NAME     = "namespace";
  public static final String RELATIONSHIP_TAG_NAME  = "relationship";

  public static final String REGULATES_OBOXML_RELATIONSHIP_NAME             = "regulates";
  public static final String POSITIVELY_REGULATES_OBOXML_RELATIONSHIP_NAME  = "positively_regulates";
  public static final String NEGATIVELY_REGULATES_OBOXML_RELATIONSHIP_NAME  = "negatively_regulates";
  public static final String PART_OF_OBOXML_RELATIONSHIP_NAME               = "part_of";
  public static final String HAS_PART_OF_OBOXML_RELATIONSHIP_NAME           = "has_part";
  public static final String IS_A_OBOXML_RELATIONSHIP_NAME                  = "is_a";

  private static final Logger logger = Logger.getLogger("ImportGO");
  private static FileHandler fh;

  protected abstract GoGraph<I,RV,RVT,RE,RET> config(String dbFolder, String propertiesFile);

  public final void importGO(String[] args) {

    if (args.length != 3) {
      System.out.println("This program expects the following parameters: \n"
        + "1. Gene ontology xml filename \n"
        + "2. Bio4j DB folder \n"
        + "3. DB Properties file (.properties)");
    }
    else {

      int termCounter         = 0;
      int limitForPrintingOut = 100;
      long initTime           = System.nanoTime();

      File inFile           = new File(args[0]);
      String dbFolder       = args[1];
      String propertiesFile = args[2];

      BufferedWriter statsBuff = null;

      GoGraph<I,RV,RVT,RE,RET> goGraph = config(dbFolder, propertiesFile);

      try {

        //This block configures the logger with handler and formatter
        fh = new FileHandler("ImportGO.log", true);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        logger.addHandler(fh);
        logger.setLevel(Level.ALL);

        //---creating writer for stats file-----
        statsBuff = new BufferedWriter(new FileWriter(new File("ImportGOStats.txt")));

        Map<String, ArrayList<String>> termParentsMap         = new HashMap<>();
        Map<String, ArrayList<String>> regulatesMap           = new HashMap<>();
        Map<String, ArrayList<String>> negativelyRegulatesMap = new HashMap<>();
        Map<String, ArrayList<String>> positivelyRegulatesMap = new HashMap<>();
        Map<String, ArrayList<String>> partOfMap              = new HashMap<>();
        Map<String, ArrayList<String>> hasPartMap             = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        String line;

        StringBuilder termStBuilder = new StringBuilder();

        logger.log(Level.INFO, "inserting subontologies nodes");

        SubOntologies<I,RV,RVT,RE,RET> subOntologiesBP = goGraph.addVertex(goGraph.SubOntologies());
        subOntologiesBP.set(goGraph.SubOntologies().name, "biological_process");

        SubOntologies<I,RV,RVT,RE,RET> subOntologiesCC = goGraph.addVertex(goGraph.SubOntologies());
        subOntologiesCC.set(goGraph.SubOntologies().name, "cellular_component");

        SubOntologies<I,RV,RVT,RE,RET> subOntologiesMM = goGraph.addVertex(goGraph.SubOntologies());
        subOntologiesMM.set(goGraph.SubOntologies().name, "molecular_function");

        logger.log(Level.INFO, "inserting term nodes");

        while((line = reader.readLine()) != null) {
```


### Getting term properties from the XML

We stop first at those lines which mark the beginning of a GO term.

In this we have the beginning of a term

```java
          if(line.trim().startsWith("<" + TERM_TAG_NAME)) {
```

accumulate lines for the term, and advance to the next line

```java
            while(!line.trim().startsWith("</"+TERM_TAG_NAME+">")) {

              termStBuilder.append(line);
              line = reader.readLine();
            }
            // add organism line
            termStBuilder.append(line);
```

We now build an XML element representing this term

```java
            XMLElement termXMLElement = new XMLElement(termStBuilder.toString());
```

release the StringBuilder

```java
            termStBuilder.delete(0, termStBuilder.length());
```


### Term manipulation


term id

```java
            final String goId = termXMLElement.asJDomElement().getChildText(ID_TAG_NAME);
```

term name, which can be null

```java
            String goName = termXMLElement.asJDomElement().getChildText(NAME_TAG_NAME);
            if (goName == null) {
              goName = "";
            }
```

term namespace, which can be null

```java
            String goNamespace = termXMLElement.asJDomElement().getChildText(NAMESPACE_TAG_NAME);
            if (goNamespace == null) {
              goNamespace = "";
            }
```

term definition, which can be null in several ways

```java
            String goDefinition = "";
            Element defElem = termXMLElement.asJDomElement().getChild(DEF_TAG_NAME);
            if (defElem != null) {
              Element defstrElem = defElem.getChild(DEFSTR_TAG_NAME);
              if (defstrElem != null) {
                goDefinition = defstrElem.getText();
              }
            }
```

term comment, which again can be null

```java
            String goComment = termXMLElement.asJDomElement().getChildText(COMMENT_TAG_NAME);
            if (goComment == null) {
              goComment = "";
            }
```

term obsolescence, which again can be null

```java
            // TODO simply drop obsolete terms
            String goIsObsolete = termXMLElement.asJDomElement().getChildText(IS_OBSOLETE_TAG_NAME);
            if (goIsObsolete == null) {
              goIsObsolete = "";
            }
            else {
              if (goIsObsolete.equals("1")) {
                goIsObsolete = "true";
              }
              else {
                goIsObsolete = "false";
              }
            }

            //----term parents----
            List<Element> termParentTerms = termXMLElement.asJDomElement().getChildren(IS_A_OBOXML_RELATIONSHIP_NAME);
            ArrayList<String> array = new ArrayList<>();
            for (Element elem: termParentTerms) {
              array.add(elem.getText().trim());
            }
            termParentsMap.put(goId, array);
            //---------------------

            //-------relationship tags-----------
            List<Element> relationshipTags = termXMLElement.asJDomElement().getChildren(RELATIONSHIP_TAG_NAME);

            for (Element relationshipTag: relationshipTags) {

              String relType  = relationshipTag.getChildText("type");
              String toSt     = relationshipTag.getChildText("to");

              if (relType.equals(REGULATES_OBOXML_RELATIONSHIP_NAME)) {

                ArrayList<String> tempArray = regulatesMap.get(goId);
                if(tempArray == null) {
                  tempArray = new ArrayList<>();
                  regulatesMap.put(goId, tempArray);
                }
                tempArray.add(toSt);
              }
              else if(relType.equals(POSITIVELY_REGULATES_OBOXML_RELATIONSHIP_NAME)) {

                ArrayList<String> tempArray = positivelyRegulatesMap.get(goId);

                if(tempArray == null) {
                  tempArray = new ArrayList<>();
                  positivelyRegulatesMap.put(goId, tempArray);
                }

                tempArray.add(toSt);
              }
              else if(relType.equals(NEGATIVELY_REGULATES_OBOXML_RELATIONSHIP_NAME)) {

                ArrayList<String> tempArray = negativelyRegulatesMap.get(goId);

                if (tempArray == null) {
                  tempArray = new ArrayList<>();
                  negativelyRegulatesMap.put(goId, tempArray);
                }

                tempArray.add(toSt);
              }
              else if (relType.equals(PART_OF_OBOXML_RELATIONSHIP_NAME)) {

                ArrayList<String> tempArray = partOfMap.get(goId);

                if (tempArray == null) {
                  tempArray = new ArrayList<>();
                  partOfMap.put(goId, tempArray);
                }

                tempArray.add(toSt);
              }
              else if (relType.equals(HAS_PART_OF_OBOXML_RELATIONSHIP_NAME)) {

                ArrayList<String> tempArray = hasPartMap.get(goId);

                if (tempArray == null) {
                  tempArray = new ArrayList<>();
                  hasPartMap.put(goId, tempArray);
                }

                tempArray.add(toSt);
              }
            }
```


### Write term to the database


```java
            GoTerm<I,RV,RVT,RE,RET> term = goGraph.addVertex(goGraph.GoTerm());

            term.set(goGraph.GoTerm().id, goId);
            term.set(goGraph.GoTerm().name, goName);
            term.set(goGraph.GoTerm().definition, goDefinition);
            term.set(goGraph.GoTerm().obsolete, goIsObsolete);
            term.set(goGraph.GoTerm().comment, goComment);

            goGraph.raw().commit();
            //----namespace---
            SubOntologies<I,RV,RVT,RE,RET> tmpSubontologies = goGraph.subontologiesNameIndex().getVertex(goNamespace).get();
            goGraph.addEdge(term,goGraph.SubOntology(), tmpSubontologies);
          }

          termCounter++;

          if((termCounter % limitForPrintingOut) == 0) {
            logger.log(Level.INFO, (termCounter+" GO terms inserted"));
          }
        }

        reader.close();

        //----committing transaction---
        // TODO we commit here, and again inside the loop with every insertion. Why?
        goGraph.raw().commit();

        //-----------------------------------------------------------------------

        logger.log(Level.INFO, "Inserting relationships....");
        logger.log(Level.INFO, "'is_a' relationships....");

        //-------------------'is_a' relationships-----------------
        Set<String> keys = termParentsMap.keySet();
        for(String key: keys) {

          // TODO get on option
          GoTerm<I,RV,RVT,RE,RET> tempGoTerm = goGraph.goTermIdIndex().getVertex(key).get();
          ArrayList<String> tempArray = termParentsMap.get(key);

          for (String string : tempArray) {
            // TODO get on option
            GoTerm<I,RV,RVT,RE,RET> tempGoTerm2 = goGraph.goTermIdIndex().getVertex(string).get();
            goGraph.addEdge(tempGoTerm,goGraph.IsA(), tempGoTerm2);
          }
        }

        logger.log(Level.INFO, "'regulates' relationships....");
        //-------------------'regulates' relationships----------------------
        keys = regulatesMap.keySet();
        for (String key: keys) {
          GoTerm<I,RV,RVT,RE,RET> tempGoTerm = goGraph.goTermIdIndex().getVertex(key).get();
          ArrayList<String> tempArray = regulatesMap.get(key);
          for (String string : tempArray) {
            GoTerm<I,RV,RVT,RE,RET> tempGoTerm2 = goGraph.goTermIdIndex().getVertex(string).get();
            goGraph.addEdge(tempGoTerm, goGraph.Regulates(), tempGoTerm2);
          }
        }

        logger.log(Level.INFO, "'negatively_regulates' relationships....");
        //-------------------'negatively regulates' relationships----------------------
        keys = negativelyRegulatesMap.keySet();

        for (String key: keys) {
          GoTerm<I,RV,RVT,RE,RET> tempGoTerm = goGraph.goTermIdIndex().getVertex(key).get();
          ArrayList<String> tempArray = negativelyRegulatesMap.get(key);
          for (String string: tempArray) {
            GoTerm<I,RV,RVT,RE,RET> tempGoTerm2 = goGraph.goTermIdIndex().getVertex(string).get();
            goGraph.addEdge(tempGoTerm, goGraph.NegativelyRegulates(), tempGoTerm2);
          }
        }

        logger.log(Level.INFO, "'positively_regulates' relationships....");
        //-------------------'positively regulates' relationships----------------------
        keys = positivelyRegulatesMap.keySet();
        for (String key: keys) {
          GoTerm<I,RV,RVT,RE,RET> tempGoTerm =  goGraph.goTermIdIndex().getVertex(key).get();
          ArrayList<String> tempArray = positivelyRegulatesMap.get(key);
          for (String string : tempArray) {
            GoTerm<I,RV,RVT,RE,RET> tempGoTerm2 =  goGraph.goTermIdIndex().getVertex(string).get();
            goGraph.addEdge(tempGoTerm, goGraph.PositivelyRegulates(), tempGoTerm2);
          }
        }

        logger.log(Level.INFO, "'part_of' relationships....");
        //-------------------'parf of' relationships----------------------
        keys = partOfMap.keySet();
        for (String key: keys) {
          GoTerm<I,RV,RVT,RE,RET> tempGoTerm = goGraph.goTermIdIndex().getVertex(key).get();
          ArrayList<String> tempArray = partOfMap.get(key);
          for (String string : tempArray) {
            GoTerm<I,RV,RVT,RE,RET> tempGoTerm2 = goGraph.goTermIdIndex().getVertex(string).get();
            goGraph.addEdge(tempGoTerm, goGraph.PartOf(), tempGoTerm2);
          }
        }

        logger.log(Level.INFO, "'has_part_of' relationships....");
        //-------------------'has part of' relationships----------------------
        keys = hasPartMap.keySet();
        for (String key : keys) {
          GoTerm<I,RV,RVT,RE,RET> tempGoTerm = goGraph.goTermIdIndex().getVertex(key).get();
          ArrayList<String> tempArray = hasPartMap.get(key);
          for (String string : tempArray) {
            GoTerm<I,RV,RVT,RE,RET> tempGoTerm2 = goGraph.goTermIdIndex().getVertex(string).get();
            goGraph.addEdge(tempGoTerm, goGraph.HasPartOf(), tempGoTerm2);
          }
        }

        logger.log(Level.INFO, "Done! :)");


      }
      catch (Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : trace) {
          logger.log(Level.SEVERE, stackTraceElement.toString());
        }
      }
      finally {

        try {
          //closing logger file handler
          fh.close();
          logger.log(Level.INFO, "Closing up manager....");
          //shutdown, makes sure all changes are written to disk
          goGraph.raw().shutdown();

          //-----------------writing stats file---------------------
          long elapsedTime      = System.nanoTime() - initTime;
          long elapsedSeconds   = Math.round((elapsedTime / 1000000000.0));
          long hours            = elapsedSeconds / 3600;
          long minutes          = (elapsedSeconds % 3600) / 60;
          long seconds          = (elapsedSeconds % 3600) % 60;

          statsBuff.write("Statistics for program ImportGeneOntology:\nInput file: " + inFile.getName()
            + "\nThere were " + termCounter + " terms inserted.\n"
            + "The elapsed time was: " + hours + "h " + minutes + "m " + seconds + "s\n");

          //---closing stats writer---
          statsBuff.close();
        }
        catch (Exception e) {
          logger.log(Level.SEVERE, e.getMessage());
          StackTraceElement[] trace = e.getStackTrace();
          for (StackTraceElement stackTraceElement : trace) {
            logger.log(Level.SEVERE, stackTraceElement.toString());
          }
        }
      }
    }
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
[main/java/com/bio4j/model/go/vertices/SubOntologies.java]: ../vertices/SubOntologies.java.md
[main/java/com/bio4j/model/go/vertices/GoTerm.java]: ../vertices/GoTerm.java.md
[main/java/com/bio4j/model/go/vertices/GoSlims.java]: ../vertices/GoSlims.java.md
[main/java/com/bio4j/model/go/programs/ImportGO.java]: ImportGO.java.md
[main/java/com/bio4j/model/go/edges/HasPartOf.java]: ../edges/HasPartOf.java.md
[main/java/com/bio4j/model/go/edges/PositivelyRegulates.java]: ../edges/PositivelyRegulates.java.md
[main/java/com/bio4j/model/go/edges/Regulates.java]: ../edges/Regulates.java.md
[main/java/com/bio4j/model/go/edges/SubOntology.java]: ../edges/SubOntology.java.md
[main/java/com/bio4j/model/go/edges/IsA.java]: ../edges/IsA.java.md
[main/java/com/bio4j/model/go/edges/NegativelyRegulates.java]: ../edges/NegativelyRegulates.java.md
[main/java/com/bio4j/model/go/edges/PartOf.java]: ../edges/PartOf.java.md
[main/java/com/bio4j/model/go/GoGraph.java]: ../GoGraph.java.md
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
[main/java/com/bio4j/model/uniprot/UniProtGraph.java]: ../../uniprot/UniProtGraph.java.md
[main/java/com/bio4j/model/uniprot/vertices/SequenceCaution.java]: ../../uniprot/vertices/SequenceCaution.java.md
[main/java/com/bio4j/model/uniprot/vertices/Disease.java]: ../../uniprot/vertices/Disease.java.md
[main/java/com/bio4j/model/uniprot/vertices/UniGene.java]: ../../uniprot/vertices/UniGene.java.md
[main/java/com/bio4j/model/uniprot/vertices/InterPro.java]: ../../uniprot/vertices/InterPro.java.md
[main/java/com/bio4j/model/uniprot/vertices/RefSeq.java]: ../../uniprot/vertices/RefSeq.java.md
[main/java/com/bio4j/model/uniprot/vertices/Organism.java]: ../../uniprot/vertices/Organism.java.md
[main/java/com/bio4j/model/uniprot/vertices/Country.java]: ../../uniprot/vertices/Country.java.md
[main/java/com/bio4j/model/uniprot/vertices/OnlineJournal.java]: ../../uniprot/vertices/OnlineJournal.java.md
[main/java/com/bio4j/model/uniprot/vertices/Thesis.java]: ../../uniprot/vertices/Thesis.java.md
[main/java/com/bio4j/model/uniprot/vertices/Pubmed.java]: ../../uniprot/vertices/Pubmed.java.md
[main/java/com/bio4j/model/uniprot/vertices/PIR.java]: ../../uniprot/vertices/PIR.java.md
[main/java/com/bio4j/model/uniprot/vertices/EMBL.java]: ../../uniprot/vertices/EMBL.java.md
[main/java/com/bio4j/model/uniprot/vertices/Institute.java]: ../../uniprot/vertices/Institute.java.md
[main/java/com/bio4j/model/uniprot/vertices/City.java]: ../../uniprot/vertices/City.java.md
[main/java/com/bio4j/model/uniprot/vertices/Reference.java]: ../../uniprot/vertices/Reference.java.md
[main/java/com/bio4j/model/uniprot/vertices/Submission.java]: ../../uniprot/vertices/Submission.java.md
[main/java/com/bio4j/model/uniprot/vertices/Protein.java]: ../../uniprot/vertices/Protein.java.md
[main/java/com/bio4j/model/uniprot/vertices/Journal.java]: ../../uniprot/vertices/Journal.java.md
[main/java/com/bio4j/model/uniprot/vertices/Dataset.java]: ../../uniprot/vertices/Dataset.java.md
[main/java/com/bio4j/model/uniprot/vertices/Publisher.java]: ../../uniprot/vertices/Publisher.java.md
[main/java/com/bio4j/model/uniprot/vertices/Patent.java]: ../../uniprot/vertices/Patent.java.md
[main/java/com/bio4j/model/uniprot/vertices/Pfam.java]: ../../uniprot/vertices/Pfam.java.md
[main/java/com/bio4j/model/uniprot/vertices/AlternativeProduct.java]: ../../uniprot/vertices/AlternativeProduct.java.md
[main/java/com/bio4j/model/uniprot/vertices/Keyword.java]: ../../uniprot/vertices/Keyword.java.md
[main/java/com/bio4j/model/uniprot/vertices/UnpublishedObservation.java]: ../../uniprot/vertices/UnpublishedObservation.java.md
[main/java/com/bio4j/model/uniprot/vertices/Book.java]: ../../uniprot/vertices/Book.java.md
[main/java/com/bio4j/model/uniprot/vertices/DB.java]: ../../uniprot/vertices/DB.java.md
[main/java/com/bio4j/model/uniprot/vertices/Isoform.java]: ../../uniprot/vertices/Isoform.java.md
[main/java/com/bio4j/model/uniprot/vertices/Consortium.java]: ../../uniprot/vertices/Consortium.java.md
[main/java/com/bio4j/model/uniprot/vertices/ReactomeTerm.java]: ../../uniprot/vertices/ReactomeTerm.java.md
[main/java/com/bio4j/model/uniprot/vertices/GeneName.java]: ../../uniprot/vertices/GeneName.java.md
[main/java/com/bio4j/model/uniprot/vertices/Ensembl.java]: ../../uniprot/vertices/Ensembl.java.md
[main/java/com/bio4j/model/uniprot/vertices/OnlineArticle.java]: ../../uniprot/vertices/OnlineArticle.java.md
[main/java/com/bio4j/model/uniprot/vertices/CommentType.java]: ../../uniprot/vertices/CommentType.java.md
[main/java/com/bio4j/model/uniprot/vertices/GeneLocation.java]: ../../uniprot/vertices/GeneLocation.java.md
[main/java/com/bio4j/model/uniprot/vertices/FeatureType.java]: ../../uniprot/vertices/FeatureType.java.md
[main/java/com/bio4j/model/uniprot/vertices/Taxon.java]: ../../uniprot/vertices/Taxon.java.md
[main/java/com/bio4j/model/uniprot/vertices/Article.java]: ../../uniprot/vertices/Article.java.md
[main/java/com/bio4j/model/uniprot/vertices/Kegg.java]: ../../uniprot/vertices/Kegg.java.md
[main/java/com/bio4j/model/uniprot/vertices/SubcellularLocation.java]: ../../uniprot/vertices/SubcellularLocation.java.md
[main/java/com/bio4j/model/uniprot/vertices/Person.java]: ../../uniprot/vertices/Person.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportIsoformSequences.java]: ../../uniprot/programs/ImportIsoformSequences.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProt.java]: ../../uniprot/programs/ImportUniProt.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportProteinInteractions.java]: ../../uniprot/programs/ImportProteinInteractions.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProtEdges.java]: ../../uniprot/programs/ImportUniProtEdges.java.md
[main/java/com/bio4j/model/uniprot/programs/XMLConstants.java]: ../../uniprot/programs/XMLConstants.java.md
[main/java/com/bio4j/model/uniprot/programs/ImportUniProtVertices.java]: ../../uniprot/programs/ImportUniProtVertices.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinOrganism.java]: ../../uniprot/edges/ProteinOrganism.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinRefSeq.java]: ../../uniprot/edges/ProteinRefSeq.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinSequenceCaution.java]: ../../uniprot/edges/ProteinSequenceCaution.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceArticle.java]: ../../uniprot/edges/ReferenceArticle.java.md
[main/java/com/bio4j/model/uniprot/edges/BookPublisher.java]: ../../uniprot/edges/BookPublisher.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinPIR.java]: ../../uniprot/edges/ProteinPIR.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinEMBL.java]: ../../uniprot/edges/ProteinEMBL.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinUniGene.java]: ../../uniprot/edges/ProteinUniGene.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinProteinInteraction.java]: ../../uniprot/edges/ProteinProteinInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinKegg.java]: ../../uniprot/edges/ProteinKegg.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinDisease.java]: ../../uniprot/edges/ProteinDisease.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinFeature.java]: ../../uniprot/edges/ProteinFeature.java.md
[main/java/com/bio4j/model/uniprot/edges/BookEditor.java]: ../../uniprot/edges/BookEditor.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinIsoform.java]: ../../uniprot/edges/ProteinIsoform.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinSubcellularLocation.java]: ../../uniprot/edges/ProteinSubcellularLocation.java.md
[main/java/com/bio4j/model/uniprot/edges/IsoformProteinInteraction.java]: ../../uniprot/edges/IsoformProteinInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinDataset.java]: ../../uniprot/edges/ProteinDataset.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceAuthorPerson.java]: ../../uniprot/edges/ReferenceAuthorPerson.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferencePatent.java]: ../../uniprot/edges/ReferencePatent.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinIsoformInteraction.java]: ../../uniprot/edges/ProteinIsoformInteraction.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceBook.java]: ../../uniprot/edges/ReferenceBook.java.md
[main/java/com/bio4j/model/uniprot/edges/OnlineArticleOnlineJournal.java]: ../../uniprot/edges/OnlineArticleOnlineJournal.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceOnlineArticle.java]: ../../uniprot/edges/ReferenceOnlineArticle.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceAuthorConsortium.java]: ../../uniprot/edges/ReferenceAuthorConsortium.java.md
[main/java/com/bio4j/model/uniprot/edges/ArticleJournal.java]: ../../uniprot/edges/ArticleJournal.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinEnsembl.java]: ../../uniprot/edges/ProteinEnsembl.java.md
[main/java/com/bio4j/model/uniprot/edges/ThesisInstitute.java]: ../../uniprot/edges/ThesisInstitute.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinReactomeTerm.java]: ../../uniprot/edges/ProteinReactomeTerm.java.md
[main/java/com/bio4j/model/uniprot/edges/SubcellularLocationParent.java]: ../../uniprot/edges/SubcellularLocationParent.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinComment.java]: ../../uniprot/edges/ProteinComment.java.md
[main/java/com/bio4j/model/uniprot/edges/TaxonParent.java]: ../../uniprot/edges/TaxonParent.java.md
[main/java/com/bio4j/model/uniprot/edges/SubmissionDB.java]: ../../uniprot/edges/SubmissionDB.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinInterPro.java]: ../../uniprot/edges/ProteinInterPro.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceThesis.java]: ../../uniprot/edges/ReferenceThesis.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinGeneName.java]: ../../uniprot/edges/ProteinGeneName.java.md
[main/java/com/bio4j/model/uniprot/edges/OrganismTaxon.java]: ../../uniprot/edges/OrganismTaxon.java.md
[main/java/com/bio4j/model/uniprot/edges/IsoformEventGenerator.java]: ../../uniprot/edges/IsoformEventGenerator.java.md
[main/java/com/bio4j/model/uniprot/edges/BookCity.java]: ../../uniprot/edges/BookCity.java.md
[main/java/com/bio4j/model/uniprot/edges/ArticlePubmed.java]: ../../uniprot/edges/ArticlePubmed.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceSubmission.java]: ../../uniprot/edges/ReferenceSubmission.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinKeyword.java]: ../../uniprot/edges/ProteinKeyword.java.md
[main/java/com/bio4j/model/uniprot/edges/ReferenceUnpublishedObservation.java]: ../../uniprot/edges/ReferenceUnpublishedObservation.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinPfam.java]: ../../uniprot/edges/ProteinPfam.java.md
[main/java/com/bio4j/model/uniprot/edges/InstituteCountry.java]: ../../uniprot/edges/InstituteCountry.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinReference.java]: ../../uniprot/edges/ProteinReference.java.md
[main/java/com/bio4j/model/uniprot/edges/ProteinGeneLocation.java]: ../../uniprot/edges/ProteinGeneLocation.java.md