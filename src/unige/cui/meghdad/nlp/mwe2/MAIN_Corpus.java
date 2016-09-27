/* 
 * Copyright (C) 2016 Meghdad Farahmand<meghdad.farahmand@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unige.cui.meghdad.nlp.mwe2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import unige.cui.meghdad.knnsearch.KNN;
import unige.cui.meghdad.knnsearch.ReadAndFilterWordRep;
import unige.cui.meghdad.knnsearch.Transform;
import unige.cui.meghdad.toolkit.Tools;

/**
 * Retrieves a list of MWEs from the corpus, ranked by their statistical idiosyncrasy 
 * that is measured via Substitution-driven measures of Association (SDMAs).
 *
 * @author Meghdad Farahmand<meghdad.farahmand@gmail.com>
 * 
 */
public class MAIN_Corpus {

    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {

        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        //\\//\\//\\//\\//\\//\\  COMMAND LINE ARGUMENTS //\\//\\//\\//\\//\\//
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\ 
        //use apache commons CLI to parse command line arguments
        // create Options object
        Options options = new Options();

        //required options:
        options.addOption("p2corpus", true, "Path 2 POS tagged corpus.");
        options.addOption("p2wr", true, "Path 2 word representations.");

        //optional options:
        options.addOption("rc", true, "Ranking criteria: delta_12, delta_21, or combined.");
        options.addOption("maxRank", true, "Return MWEs up to this rank.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        //initialize options to default values and check required options are set
        if (!cmd.hasOption("p2corpus")) {
            System.out.println("Path to the POS tagged corpus must be set.");
        }
        if (!cmd.hasOption("p2wr")) {
            System.out.println("A valid word representation must be specified.");
            return;
        }
        String rc = "21";
        if (cmd.hasOption("rc")) {
            rc = cmd.getOptionValue("rc");
        }
        int maxRank = 200;
        if (cmd.hasOption("maxRank")) {
            rc = cmd.getOptionValue("maxRank");
        }

        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\\//\\//\\//
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        
        
        String p2corpus = cmd.getOptionValue("p2corpus");
        String p2wr = cmd.getOptionValue("p2wr");

        Tools T = new Tools();  
        
        //\\//\\//\\//\\//\\ related to KNN //\\//\\//\\//\\//\\
        
        //create an instance of class ReadAndFilterWordRep
        System.out.println("Reading word representations...");
        ReadAndFilterWordRep rv = new ReadAndFilterWordRep();

        //word2vec output entries are unique, so the following lists are going 
        //to be lists of unique vectors (with no duplicate)
        List<List<String>> wordsVectors = rv.rfwr(p2wr,100);
        
        HashMap<String,Integer> words = new HashMap<>();

        //for words, use HashMap insteaf of list for faster look up (contains)
        //preserve the index of the words as Map values for future use
        for(int i=0 ; i < wordsVectors.get(0).size() ; i++){
          words.put(wordsVectors.get(0).get(i), i);
        }
        //for vectors, use a List
        List<String> vectors = wordsVectors.get(1);
        
        //\\//\\//\\//\\//\\ end of related to KNN //\\//\\//\\//\\//\\
        
        System.out.println("Extracting 1-grams...");
        HashMap<String, Integer> unigrams = T.ExtractUnigram(p2corpus, 1, true, true).get(0);

        System.out.println("Extracting 2-grams...");
        HashMap<String, Integer> bigrams = T.ExtractNgrams(p2corpus, 1, 2, true, false, true);

        System.out.println("Extracting a set of \"nn-nn\" candidates...");
        LinkedHashMap<String, Integer> candidates = new LinkedHashMap<>(T.extractNCs(p2corpus, "nn-nn", true, false, 50));
        
        //TODO break the list of candidates into their components
        
        /*
        The map of compounds must be broken into a list of words (I)
        because knnExhSearch works with a list of words and not compounds. 
        Then in II, this the compounds will be reconstructed, this time together 
        with their neighbors. 
        */
        
        String[] wis;
        List<String> avail_lw_Rep = new ArrayList<>();
        List<String> avail_lw_forms = new ArrayList<>();
        //(I)
        for(String c : candidates.keySet()){
            wis = c.split(" ");
            for (String w : wis) {

                if (words.containsKey(w)) {
                    int index_of_l = words.get(w);
                    avail_lw_Rep.add(vectors.get(index_of_l));
                    avail_lw_forms.add(w);
                } else {
                    System.out.println("Vector representation for\" " + c + "\" is not availble. Skipping this entry.");
                }
            }
        }
        
        //Find nearest neighbors:
        //create an instance of Transform class
        Transform m = new Transform();
        //transform the representations (avail_lw_Rep) of avail_lw_forms from string to double
        List<List<Double>> M = m.createFromList(vectors, 100);
        List<List<Double>> lw = m.createFromList(avail_lw_Rep, 100);
        
        //create an instance of KNN class
        KNN knn = new KNN();
        //invoke Knn exhustive search
        
        /*
        lwNeighbors contains a list of indices pointing to the neighbors
        for each word in avail_lw_forms. lwNeighbors and avail_lw_forms have the same size. 
        Each index of lwNeighbors corresponds to the same index in avail_lw_forms. 
        */
        
        List<List<Integer>>  lwNeighbors = knn.knnExhSearch(lw, M, 5);

        //(II)
        /*
        - Read the candidates again. 
        - Split each candidate into its components. 
        - Check if both those two components are found in avail_lw_forms, i.e., for both
          of the components a vector representation was found, then write it to results. 
        
        */
        
        //compounds and the neighbors for each component of the compound.
        List<String> compoundAndComponNeighbors = new ArrayList<>();
        /*
        entry contains the compound and the neighbors of each of its components.
        format:
        vehicle,wrap,vehicle,vehicles,truck,car,airbag,gear,semi-trailer,wrap,wrapping,wrapped,wraps,glued,stitched,sewn
        */
        String entry = "";
        for (String c : candidates.keySet()) {
            wis = c.split(" ");
            /*
             If both of the components of the compound had representation (and hence 
             a neighbors were found for them) update entry and add it to the results:
             compoundAndComponNeighbors
             */
            if (avail_lw_forms.contains(wis[0]) && avail_lw_forms.contains(wis[1])) {
                entry.concat(wis[0]).concat(",").concat(wis[1]).concat(",");
                List<Integer> w1Neighbors = lwNeighbors.get(avail_lw_forms.indexOf(wis[0]));
                for (int neighbInd : w1Neighbors) {
                    entry.concat(wordsVectors.get(0).get(neighbInd)).concat(",");
                }
                List<Integer> w2Neighbors = lwNeighbors.get(avail_lw_forms.indexOf(wis[1]));
                for (int neighbInd : w2Neighbors) {
                    entry.concat(wordsVectors.get(0).get(neighbInd)).concat(",");
                }
                compoundAndComponNeighbors.add(entry);
            } else {
                /*
                The neighbors could not be retrieved for at least one of the components 
                of this compound and therefore it will not be added to the return list. 
                */
            }
        }
    }

}
