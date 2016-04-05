package com.bio4j.model.uniprot_ncbiTaxonomy.programs;

import com.bio4j.model.ncbiTaxonomy.vertices.NCBITaxon;
import com.bio4j.model.uniprot.vertices.Protein;
import com.bio4j.angulillos.UntypedGraph;
import com.bio4j.model.uniprot_ncbiTaxonomy.UniProtNCBITaxonomyGraph;

import org.jdom2.Element;
import com.bio4j.xml.XMLUtils;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class ImportUniProtNCBITaxonomy<I extends UntypedGraph<RV,RVT,RE,RET>,RV,RVT,RE,RET> {

  private static final Logger logger = Logger.getLogger("ImportUniProtNCBITaxonomy");
  private static FileHandler fh;

  public static final String ENTRY_TAG_NAME           = "entry";
  public static final String ENTRY_ACCESSION_TAG_NAME = "accession";

  public static final String ORGANISM_TAG_NAME            = "organism";
  public static final String DB_REFERENCE_TAG_NAME        = "dbReference";
  public static final String DB_REFERENCE_TYPE_ATTRIBUTE  = "type";
  public static final String DB_REFERENCE_ID_ATTRIBUTE    = "id";
  public static final String NCBI_TAXONOMY_TYPE           = "NCBI Taxonomy";

  protected abstract UniProtNCBITaxonomyGraph<I,RV,RVT,RE,RET> config(File dbFolder);

  final int limitForPrintingOut = 10000;

  public void importUniProtNCBITaxonomy(File inFile, File dbFolder) {

    final long initTime = System.nanoTime();

    final UniProtNCBITaxonomyGraph<I,RV,RVT,RE,RET> uniprotNCBITaxonomyGraph = config(dbFolder);

    BufferedWriter statsBuff = null;

    int proteinCounter = 0;

    try {

      // This block configures the logger with handler and formatter
      fh = new FileHandler("ImportUniProtNCBITaxonomy.log", false);

      SimpleFormatter formatter = new SimpleFormatter();
      fh.setFormatter(formatter);
      logger.addHandler(fh);
      logger.setLevel(Level.ALL);

      //---creating writer for stats file-----
      statsBuff = new BufferedWriter(new FileWriter(new File("ImportUniProtNCBITaxonomyStats.txt")));

      BufferedReader reader = new BufferedReader(new FileReader(inFile));
      StringBuilder entryStBuilder = new StringBuilder();
      String line;

      while((line = reader.readLine()) != null) {

        if(line.trim().startsWith("<" + ENTRY_TAG_NAME)) {

          while (!line.trim().startsWith("</" + ENTRY_TAG_NAME + ">")) {
            entryStBuilder.append(line);
            line = reader.readLine();
          }
          entryStBuilder.append(line);

          final Element entryXMLElem = XMLUtils.parseXMLFrom(entryStBuilder.toString());
          entryStBuilder.delete(0, entryStBuilder.length());

          String accessionSt = entryXMLElem.getChildText(ENTRY_ACCESSION_TAG_NAME);

          Optional<Protein<I,RV,RVT,RE,RET>> proteinOptional = uniprotNCBITaxonomyGraph.uniProtGraph().proteinAccessionIndex().getVertex(accessionSt);

          if(proteinOptional.isPresent()) {

            Protein<I,RV,RVT,RE,RET> protein = proteinOptional.get();

            Element organismElement = entryXMLElem.getChild(ORGANISM_TAG_NAME);

            if(organismElement != null) {

              //-----db references-------------
              List<Element> dbReferenceList = organismElement.getChildren(DB_REFERENCE_TAG_NAME);

              for (Element dbReferenceElem : dbReferenceList) {

                //-------------------NCBI TAXONOMY -----------------------------
                if (dbReferenceElem.getAttributeValue(DB_REFERENCE_TYPE_ATTRIBUTE).equals(NCBI_TAXONOMY_TYPE)) {

                  String taxonId = dbReferenceElem.getAttributeValue(DB_REFERENCE_ID_ATTRIBUTE);

                  Optional<NCBITaxon<I,RV,RVT,RE,RET>> ncbiTaxonOptional = uniprotNCBITaxonomyGraph.ncbiTaxonomyGraph().nCBITaxonIdIndex().getVertex(taxonId);
                  if(ncbiTaxonOptional.isPresent()){
                    protein.addOutEdge(uniprotNCBITaxonomyGraph.ProteinNCBITaxon(), ncbiTaxonOptional.get());
                  }
                }
              }
            }

            proteinCounter++;

            if ((proteinCounter % limitForPrintingOut) == 0) {

              String countProteinsSt = proteinCounter + " proteins updated!!";
              logger.log(Level.INFO, countProteinsSt);
            }
          }
        }
      }
    }
    catch (Exception e) {

      logger.log(Level.SEVERE, e.getMessage());
      StackTraceElement[] trace = e.getStackTrace();
      for (StackTraceElement stackTraceElement: trace) {

        logger.log(Level.SEVERE, stackTraceElement.toString());
      }
    }
    finally {

      try {
        //------closing writers-------

        // shutdown, makes sure all changes are written to disk
        uniprotNCBITaxonomyGraph.raw().shutdown();

        // closing logger file handler
        fh.close();

        //-----------------writing stats file---------------------
        long elapsedTime    = System.nanoTime() - initTime;
        long elapsedSeconds = Math.round((elapsedTime / 1000000000.0));
        long hours          = elapsedSeconds / 3600;
        long minutes        = (elapsedSeconds % 3600) / 60;
        long seconds        = (elapsedSeconds % 3600) % 60;

        statsBuff.write("Statistics for program ImportUniProtNCBITaxonomy:\nInput file: " + inFile.getName()
        + "\nThere were " + proteinCounter + " proteins inserted.\n"
        + "The elapsed time was: " + hours + "h " + minutes + "m " + seconds + "s\n");

        //---closing stats writer---
        statsBuff.close();

      }
      catch (IOException ex) {

        Logger.getLogger(ImportUniProtNCBITaxonomy.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
}
