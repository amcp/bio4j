package com.bio4j.model.uniprot_uniref.programs;

import com.bio4j.model.uniprot.vertices.Protein;
import com.bio4j.model.uniprot_uniref.UniProtUniRefGraph;
import com.bio4j.model.uniref.vertices.UniRef100Cluster;
import com.bio4j.model.uniref.vertices.UniRef50Cluster;
import com.bio4j.model.uniref.vertices.UniRef90Cluster;
import com.bio4j.angulillos.UntypedGraph;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.jdom2.*;
import com.bio4j.xml.XMLUtils;

public abstract class ImportUniProtUniRef<I extends UntypedGraph<RV,RVT,RE,RET>,RV,RVT,RE,RET>  {

  public static final String ENTRY_TAG_NAME = "entry";

  private static final Logger logger = Logger.getLogger("ImportUniProtUniRef");
  private static FileHandler fh;

  protected abstract UniProtUniRefGraph<I,RV,RVT,RE,RET> config(File dbFolder);

  public void importUniProtUniRef(File inFile, File dbFolder) {

    final long initTime = System.nanoTime();

    final UniProtUniRefGraph<I,RV,RVT,RE,RET> uniprotUniRefGraph = config(dbFolder);

    BufferedWriter statsBuff = null;

    int unirefEntryCounter = 0;

    try {

      final File uniref50File  = new File(inFile, "something50");
      final File uniref90File  = new File(inFile, "something90");
      final File uniref100File = new File(inFile, "something100");

      // This block configure the logger with handler and formatter
      fh = new FileHandler("ImportUniProtUniRef.log", false);
      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
      logger.addHandler(fh);
      logger.setLevel(Level.ALL);

      //---creating writer for stats file-----
      statsBuff = new BufferedWriter(new FileWriter(new File("ImportUniProtUniRefStats.txt")));

      importProteinUniRef50   (uniref50File, uniprotUniRefGraph);
      importProteinUniRef90   (uniref90File, uniprotUniRefGraph);
      importProteinUniRef100  (uniref100File, uniprotUniRefGraph);

    }
    catch(Exception ex) {

      logger.log(Level.SEVERE, ex.getMessage());
      StackTraceElement[] trace = ex.getStackTrace();
      for (StackTraceElement stackTraceElement : trace) {
        logger.log(Level.SEVERE, stackTraceElement.toString());
      }
    }
    finally {

      try {
        //committing last transaction
        uniprotUniRefGraph.raw().commit();
        //closing logger file handler
        fh.close();
        //closing neo4j managers
        uniprotUniRefGraph.raw().shutdown();

        //-----------------writing stats file---------------------
        long elapsedTime    = System.nanoTime() - initTime;
        long elapsedSeconds = Math.round((elapsedTime / 1000000000.0));
        long hours          = elapsedSeconds / 3600;
        long minutes        = (elapsedSeconds % 3600) / 60;
        long seconds        = (elapsedSeconds % 3600) % 60;

        // statsBuff.write("Statistics for program ImportUniProtUniRefTitan:\nInput files: " +
        // "\nUniRef file: " + uniRefFile.getName()
        // + "\nThe following number of entries was parsed:\n"
        // + unirefEntryCounter + " entries\n"
        // + "The elapsed time was: " + hours + "h " + minutes + "m " + seconds + "s\n");

        //---closing stats writer---
        statsBuff.close();

      }
      catch(Exception e) {

        logger.log(Level.SEVERE, e.getMessage());
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : trace) {
          logger.log(Level.SEVERE, stackTraceElement.toString());
        }
      }
    }

    System.out.println("Program finished!! :D");
  }

  private static String getRepresentantAccession(Element elem) {

    String result = null;
    Element dbReference = elem.getChild("dbReference");
    List<Element> properties = dbReference.getChildren("property");
    for (Element prop : properties) {
      if (prop.getAttributeValue("type").equals("UniProtKB accession")) {
        result = prop.getAttributeValue("value");
      }
    }

    return result;
  }

  private void importProteinUniRef50(
    File unirefFile,
    UniProtUniRefGraph<I,RV,RVT,RE,RET> uniprotUniRefGraph
  )
  throws Exception {

    StringBuilder entryStBuilder = new StringBuilder();

    BufferedReader reader = new BufferedReader(new FileReader(unirefFile));
    String line;

    int entryCounter = 0;
    final int limitForPrintingOut = 1000;

    while((line = reader.readLine()) != null) {
      //----we reached a entry line-----
      if(line.trim().startsWith("<" + ENTRY_TAG_NAME)) {

        while (!line.trim().startsWith("</" + ENTRY_TAG_NAME + ">")) {
          entryStBuilder.append(line);
          line = reader.readLine();
        }
        entryStBuilder.append(line);

        final Element entryXMLElem = XMLUtils.parseXMLFrom(entryStBuilder.toString());
        //freeing up memory
        entryStBuilder.delete(0, entryStBuilder.length());

        ArrayList<String> membersAccessionList = new ArrayList<String>();
        String entryId = entryXMLElem.getAttributeValue("id");
        Element representativeMember = entryXMLElem.getChild("representativeMember");
        String representantAccession = getRepresentantAccession(representativeMember);
        if(representantAccession != null) {

          membersAccessionList.add(representantAccession);
        }

        if(entryId != null) {
          //----obtaining cluster members---
          List<Element> members = entryXMLElem.getChildren("member");
          for (Element member : members) {
            Element memberDbReference = member.getChild("dbReference");
            List<Element> memberProperties = memberDbReference.getChildren("property");
            for (Element prop : memberProperties) {
              if (prop.getAttributeValue("type").equals("UniProtKB accession")) {
                String memberAccession = prop.getAttributeValue("value");
                membersAccessionList.add(memberAccession);
              }
            }
          }

          for(String proteinAccession : membersAccessionList) {

            Optional<Protein<I,RV,RVT,RE,RET>> optionalProtein = uniprotUniRefGraph.uniProtGraph().proteinAccessionIndex().getVertex(proteinAccession);

            if(optionalProtein.isPresent()) {

              Protein<I,RV,RVT,RE,RET> protein = optionalProtein.get();
              protein.set(uniprotUniRefGraph.uniProtGraph().Protein().uniRef50ClusterId, entryId);
            }
          }
          //-----------------------------------------------------

            Optional<UniRef50Cluster<I,RV,RVT,RE,RET>> optionalCluster = uniprotUniRefGraph.uniRefGraph().uniRef50ClusterIdIndex().getVertex(entryId);
            if(optionalCluster.isPresent()){
              UniRef50Cluster<I,RV,RVT,RE,RET> cluster = optionalCluster.get();
              cluster.set(uniprotUniRefGraph.uniRefGraph().UniRef50Cluster().members, membersAccessionList.toArray(new String[membersAccessionList.size()]));
            }else{
              logger.log(Level.INFO, (entryId + " cluster not found... :|"));
            }
        }
      }

      entryCounter++;

      if ((entryCounter % limitForPrintingOut) == 0) {
        logger.log(Level.INFO, (entryCounter + " entries parsed!!"));
        uniprotUniRefGraph.raw().commit();
      }

    }
    reader.close();
  }

  private void importProteinUniRef90(
    File unirefFile,
    UniProtUniRefGraph<I,RV,RVT,RE,RET> uniprotUniRefGraph
  )
  throws Exception {

    StringBuilder entryStBuilder = new StringBuilder();

    BufferedReader reader = new BufferedReader(new FileReader(unirefFile));
    String line;

    int entryCounter = 0;
    final int limitForPrintingOut = 1000;

    while((line = reader.readLine()) != null) {
      //----we reached a entry line-----
      if(line.trim().startsWith("<" + ENTRY_TAG_NAME)) {

        while (!line.trim().startsWith("</" + ENTRY_TAG_NAME + ">")) {
          entryStBuilder.append(line);
          line = reader.readLine();
        }
        entryStBuilder.append(line);

        final Element entryXMLElem = XMLUtils.parseXMLFrom(entryStBuilder.toString());
        //freeing up memory
        entryStBuilder.delete(0, entryStBuilder.length());

        ArrayList<String> membersAccessionList = new ArrayList<String>();
        String entryId = entryXMLElem.getAttributeValue("id");
        Element representativeMember = entryXMLElem.getChild("representativeMember");
        String representantAccession = getRepresentantAccession(representativeMember);
        if(representantAccession != null) {

          membersAccessionList.add(representantAccession);
        }

        if(entryId != null) {
          //----obtaining cluster members---
          List<Element> members = entryXMLElem.getChildren("member");
          for (Element member : members) {
            Element memberDbReference = member.getChild("dbReference");
            List<Element> memberProperties = memberDbReference.getChildren("property");
            for (Element prop : memberProperties) {
              if (prop.getAttributeValue("type").equals("UniProtKB accession")) {
                String memberAccession = prop.getAttributeValue("value");
                membersAccessionList.add(memberAccession);
              }
            }
          }

          for(String proteinAccession : membersAccessionList) {

            Optional<Protein<I,RV,RVT,RE,RET>> optionalProtein = uniprotUniRefGraph.uniProtGraph().proteinAccessionIndex().getVertex(proteinAccession);

            if(optionalProtein.isPresent()) {

              Protein<I,RV,RVT,RE,RET> protein = optionalProtein.get();
              protein.set(uniprotUniRefGraph.uniProtGraph().Protein().uniRef90ClusterId, entryId);
            }
          }
          //-----------------------------------------------------

            Optional<UniRef90Cluster<I,RV,RVT,RE,RET>> optionalCluster = uniprotUniRefGraph.uniRefGraph().uniRef90ClusterIdIndex().getVertex(entryId);
            if(optionalCluster.isPresent()){
              UniRef90Cluster<I,RV,RVT,RE,RET> cluster = optionalCluster.get();
              cluster.set(uniprotUniRefGraph.uniRefGraph().UniRef90Cluster().members, membersAccessionList.toArray(new String[membersAccessionList.size()]));
            }else{
              logger.log(Level.INFO, (entryId + " cluster not found... :|"));
            }
        }
      }

      entryCounter++;
      if ((entryCounter % limitForPrintingOut) == 0) {
        logger.log(Level.INFO, (entryCounter + " entries parsed!!"));
        uniprotUniRefGraph.raw().commit();
      }

    }
    reader.close();
  }

  private void importProteinUniRef100(
    File unirefFile,
    UniProtUniRefGraph<I,RV,RVT,RE,RET> uniprotUniRefGraph
  )
  throws Exception {

    StringBuilder entryStBuilder = new StringBuilder();

    BufferedReader reader = new BufferedReader(new FileReader(unirefFile));
    String line;

    int entryCounter = 0;
    final int limitForPrintingOut = 1000;

    while((line = reader.readLine()) != null) {
      //----we reached a entry line-----
      if(line.trim().startsWith("<" + ENTRY_TAG_NAME)) {

        while (!line.trim().startsWith("</" + ENTRY_TAG_NAME + ">")) {
          entryStBuilder.append(line);
          line = reader.readLine();
        }
        entryStBuilder.append(line);

        final Element entryXMLElem = XMLUtils.parseXMLFrom(entryStBuilder.toString());
        //freeing up memory
        entryStBuilder.delete(0, entryStBuilder.length());

        ArrayList<String> membersAccessionList = new ArrayList<String>();
        String entryId = entryXMLElem.getAttributeValue("id");
        Element representativeMember = entryXMLElem.getChild("representativeMember");
        String representantAccession = getRepresentantAccession(representativeMember);
        if(representantAccession != null) {

          membersAccessionList.add(representantAccession);
        }

        if(entryId != null) {
          //----obtaining cluster members---
          List<Element> members = entryXMLElem.getChildren("member");
          for (Element member : members) {
            Element memberDbReference = member.getChild("dbReference");
            List<Element> memberProperties = memberDbReference.getChildren("property");
            for (Element prop : memberProperties) {
              if (prop.getAttributeValue("type").equals("UniProtKB accession")) {
                String memberAccession = prop.getAttributeValue("value");
                membersAccessionList.add(memberAccession);
              }
            }
          }

          for(String proteinAccession : membersAccessionList) {

            Optional<Protein<I,RV,RVT,RE,RET>> optionalProtein = uniprotUniRefGraph.uniProtGraph().proteinAccessionIndex().getVertex(proteinAccession);

            if(optionalProtein.isPresent()) {

              Protein<I,RV,RVT,RE,RET> protein = optionalProtein.get();
              protein.set(uniprotUniRefGraph.uniProtGraph().Protein().uniRef100ClusterId, entryId);
            }
          }
          //-----------------------------------------------------

            Optional<UniRef100Cluster<I,RV,RVT,RE,RET>> optionalCluster = uniprotUniRefGraph.uniRefGraph().uniRef100ClusterIdIndex().getVertex(entryId);
            if(optionalCluster.isPresent()){
              UniRef100Cluster<I,RV,RVT,RE,RET> cluster = optionalCluster.get();
              cluster.set(uniprotUniRefGraph.uniRefGraph().UniRef100Cluster().members, membersAccessionList.toArray(new String[membersAccessionList.size()]));
            }else{
              logger.log(Level.INFO, (entryId + " cluster not found... :|"));
            }
        }
      }

      entryCounter++;
      if ((entryCounter % limitForPrintingOut) == 0) {
        logger.log(Level.INFO, (entryCounter + " entries parsed!!"));
        uniprotUniRefGraph.raw().commit();
      }

    }
    reader.close();
  }
}
