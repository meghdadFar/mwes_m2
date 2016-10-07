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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * Ranks a list of candidate MWEs read from a file by their statistical idiosyncrasy 
 * that is measured via Substitution-driven measures of Association (SDMAs).
 *
 * @author Meghdad Farahmand<meghdad.farahmand@gmail.com>
 * 
 */
public class MAIN_File {

    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {

        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        //\\//\\//\\//\\//\\//\\  COMMAND LINE ARGUMENTS //\\//\\//\\//\\//\\//
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\ 
        //use apache commons CLI to parse command line arguments
        // create Options object
        Options options = new Options();

        //required options:
        options.addOption("p2candidates", true, "Path 2 Not POS Tagged Candidates.");
        options.addOption("p2corpus", true, "Path 2 POS tagged corpus.");
        options.addOption("p2wr", true, "Path 2 word representations.");
        options.addOption("size", true, "Size/length of word representations.");

        //optional options:
        options.addOption("rc", true, "Model: m1 or m2.");
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
        if (!cmd.hasOption("p2candidates")) {
            System.out.println("A valid candidate list must be specified.");
            return;
        }
        
        int maxRank = 200;
        if (cmd.hasOption("maxRank")) {
            maxRank = Integer.parseInt(cmd.getOptionValue("maxRank"));
        }
        String model = "m2";
        if (cmd.hasOption("rc")) {
            model = cmd.getOptionValue("rc");
        }
        
        int rl = -1;
        if (cmd.hasOption("size")) {
            rl = Integer.parseInt(cmd.getOptionValue("size"));
        }else{
            System.out.println("Size/length of word representations must be specified.");
            return;
        }

        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\\//\\//\\//
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        
        
        String p2corpus = cmd.getOptionValue("p2corpus");
        String p2candidates = cmd.getOptionValue("p2candidates");
        String p2wr = cmd.getOptionValue("p2wr");

        Tools T = new Tools();  
        
        //\\//\\//\\//\\//\\ related to KNN //\\//\\//\\//\\//\\
        
        //create an instance of class ReadAndFilterWordRep
        System.out.println("Reading word representations...");
        ReadAndFilterWordRep rv = new ReadAndFilterWordRep();

        //word2vec output entries are unique, so the following lists are going 
        //to be lists of unique vectors (with no duplicate)
        List<List<String>> wordsVectors = rv.rfwr(p2wr,rl);
        
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
        //ExtractUnigram(String p2corpus, int lexFreqThreshold, boolean isPosTagged, boolean ignoreCase)
        HashMap<String, Integer> unigrams = T.ExtractUnigram(p2corpus, 1, false, true).get(0);

        System.out.println("Extracting 2-grams...");
        //ExtractNgrams(String p2corpus, int freqThreshold, int order, boolean isCorpusPosTagged, boolean outputPosTagged, boolean ignoreCase)
        HashMap<String, Integer> bigrams = T.ExtractNgrams(p2corpus, 1, 2, false, false, true);
        
        
        System.out.println("Reading candidates...");
        BufferedReader candidateFile = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(p2candidates), "UTF8"));

        //farahmad
        ///Users/svm/Resources/DATA/WN-Syn-Based/LowerCased/FAR/eval_farahmand/eval_instances_pos_avail.csv
        //schneider
        ///Users/svm/Resources/DATA/WN-Syn-Based/LowerCased/SCH/instance_files/filt/instances_filt_pos.csv
        ///Users/svm/Resources/DATA/VGI/ac.nc.all.pos.txt
        
        
        //TODO add exceptions when candidate list could not be created or is empty
        //TODO add , to the pattern. so that candidates be split around comma or space not just space
        //TODO make sure k is always > SYNSETSIZE (arg of nonSubFeatExtractConstituentDetails)
        
        
        /*
        Since at this point no frequency information is needed, I put the candidates in 
        a HashSet instead of a HashMap. 
        */
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        String Entry = "";
        Pattern entryETfreq = Pattern.compile("(\\w+\\s\\w+)\\s?(\\d+)?$");
        Matcher entryETfreqM;
        
        
        while ((Entry = candidateFile.readLine()) != null) {
            entryETfreqM = entryETfreq.matcher(Entry);
            if (entryETfreqM.find()) {
                candidates.add(entryETfreqM.group(1));
            } 
        }
        
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
        for(String c : candidates){
            wis = c.split(" ");
            for (String w : wis) {

                if (words.containsKey(w)) {
                    int index_of_l = words.get(w);
                    
                    if(!avail_lw_forms.contains(w)){
                        avail_lw_Rep.add(vectors.get(index_of_l));
                        avail_lw_forms.add(w);
                    }
                } else {
                    System.out.println("Vector representation for\" " + c + "\" is not availble. Skipping this entry.");
                }
            }
        }
           
        
        //Find nearest neighbors:
        //create an instance of Transform class
        Transform Tr = new Transform();
        //transform the representations (avail_lw_Rep) of avail_lw_forms from string to double
        System.out.println("Transforming word representations from String to Double");
        List<List<Double>> M = Tr.createFromList(vectors, rl);
        List<List<Double>> lw = Tr.createFromList(avail_lw_Rep, rl);
        
        
        
        //create an instance of KNN class
        KNN knn = new KNN();
        
        /*
        lwNeighbors contains a list of indices pointing to the neighbors
        for each word in avail_lw_forms. lwNeighbors and avail_lw_forms have the same size. 
        Each index of lwNeighbors corresponds to the same index in avail_lw_forms. 
        */
        System.out.println("Executing knn exhustive search for the components of the candidates...");
        List<List<Integer>>  lwNeighbors = knn.knnExhSearch(lw, M, 7);
        


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
        System.out.println("Constructing candidate list with neighbors...");
        String entry = "";
        
        for (String c : candidates) {
        
            wis = c.split(" ");
            /*
             If both of the components of the compound had representation (and hence 
             a neighbors were found for them) update entry and add it to the results:
             compoundAndComponNeighbors
             */
            if (avail_lw_forms.contains(wis[0]) && avail_lw_forms.contains(wis[1])) {
                entry = wis[0].concat(",").concat(wis[1]).concat(",");
                
                List<Integer> w1Neighbors = lwNeighbors.get(avail_lw_forms.indexOf(wis[0]));
                for (int neighbInd : w1Neighbors) {
                    entry = entry.concat(wordsVectors.get(0).get(neighbInd)).concat(",");
                }
                List<Integer> w2Neighbors = lwNeighbors.get(avail_lw_forms.indexOf(wis[1]));
                //counter to identify the last neighbor (to avoid adding a trailing comma)
                int co = 0;
                for (int neighbInd : w2Neighbors) {
                    entry = entry.concat(wordsVectors.get(0).get(neighbInd));
                    if(co < w2Neighbors.size()-1){
                        entry = entry.concat(",");
                        co++;
                    }
                }
                compoundAndComponNeighbors.add(entry);
                entry = "";
            } else {
                /*
                The neighbors could not be retrieved for at least one of the components 
                of this compound and therefore it will not be added to the return list.
                */
            }
        }
        
        /*
        Run SDMA.nonSubFeatExtractConstituentDetails to calculate SDMAs for each one of the candidates. 
        */
        System.out.println("Calculating SDMAs...");
        SDMA sdma = new SDMA();
        HashMap<String, Double> sdmaScores = sdma.nonSubFeatExtractConstituentDetails(compoundAndComponNeighbors, bigrams, unigrams, 5, 7, model,true,true);
        
        //sort (descending) candidates by their score:
        List<Map.Entry<String,Double>> entryList = new ArrayList<Map.Entry<String,Double>>(sdmaScores.entrySet());
        
        Collections.sort(entryList, new Comparator<Map.Entry<String,Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> e1,
                    Map.Entry<String, Double> e2) {
                return -1*e1.getValue().compareTo(e2.getValue());
            }
        });
        
        //print the results:
        DecimalFormat df = new DecimalFormat("0.000");
        System.out.println("Ranking the candidates...\n");
        for(Map.Entry<String,Double> e : entryList){
            System.out.println(e.getKey() + " "+ df.format(e.getValue()));   
        }
        
        
        
    }

}
