package com.bio4j.model.go.programs;

import com.bio4j.model.go.GoGraph;
import com.bio4j.model.go.vertices.GoTerm;
import com.bio4j.model.go.vertices.SubOntologies;

import org.jdom2.*;

import com.bio4j.xml.XMLUtils;

import com.bio4j.angulillos.*;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
  ## GO data import
*/
abstract class ImportGO<I extends UntypedGraph<RV,RVT,RE,RET>,RV,RVT,RE,RET> {

  /*
    ### XML tag names

    These global constants correspond to the GO XML file which we use as input.
  */
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

  protected abstract GoGraph<I,RV,RVT,RE,RET> config(File dbFolder);

  public final void importGO(File inFile, File dbFolder) {

    int termCounter         = 0;
    int limitForPrintingOut = 100;
    long initTime           = System.nanoTime();

    BufferedWriter statsBuff = null;

    GoGraph<I,RV,RVT,RE,RET> goGraph = config(dbFolder);

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

        /*
          ### Getting term properties from the XML

          We stop first at those lines which mark the beginning of a GO term.
        */
        /* In this we have the beginning of a term */
        if(line.trim().startsWith("<" + TERM_TAG_NAME)) {

          /* accumulate lines for the term, and advance to the next line */
          while(!line.trim().startsWith("</"+TERM_TAG_NAME+">")) {

            termStBuilder.append(line);
            line = reader.readLine();
          }
          // add organism line
          termStBuilder.append(line);

          /* We now build an XML element representing this term */
          Element termXMLElement = XMLUtils.parseXMLFrom(termStBuilder.toString());
          /* release the StringBuilder */
          termStBuilder.delete(0, termStBuilder.length());

          /*
            ### Term manipulation

          */
          /* term id */
          final String goId = termXMLElement.getChildText(ID_TAG_NAME);
          /* term name, which can be null */
          String goName = termXMLElement.getChildText(NAME_TAG_NAME);
          if (goName == null) {
            goName = "";
          }
          /* term namespace, which can be null */
          String goNamespace = termXMLElement.getChildText(NAMESPACE_TAG_NAME);
          if (goNamespace == null) {
            goNamespace = "";
          }
          /* term definition, which can be null in several ways */
          String goDefinition = "";
          Element defElem = termXMLElement.getChild(DEF_TAG_NAME);
          if (defElem != null) {
            Element defstrElem = defElem.getChild(DEFSTR_TAG_NAME);
            if (defstrElem != null) {
              goDefinition = defstrElem.getText();
            }
          }
          /* term comment, which again can be null */
          String goComment = termXMLElement.getChildText(COMMENT_TAG_NAME);
          if (goComment == null) {
            goComment = "";
          }
          /* term obsolescence, which again can be null */
          // TODO simply drop obsolete terms
          String goIsObsolete = termXMLElement.getChildText(IS_OBSOLETE_TAG_NAME);
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
          List<Element> termParentTerms = termXMLElement.getChildren(IS_A_OBOXML_RELATIONSHIP_NAME);
          ArrayList<String> array = new ArrayList<>();
          for (Element elem: termParentTerms) {
            array.add(elem.getText().trim());
          }
          termParentsMap.put(goId, array);
          //---------------------

          //-------relationship tags-----------
          List<Element> relationshipTags = termXMLElement.getChildren(RELATIONSHIP_TAG_NAME);

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
          /*
            ### Write term to the database
          */
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
