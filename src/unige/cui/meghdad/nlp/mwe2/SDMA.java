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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import unige.cui.meghdad.toolkit.Tools;

/**
 *
 * @author Meghdad Farahmand<meghdad.farahmand@gmail.com>
 */
public class SDMA {

    /**
     * Create non-substitutability features for a list of n-grams (default n==2)
     * and outputs the frequency of the components of n-grams.
     *
     * @param NCsSynSets list of candidates and k synonyms for each word of the candidate
     * 
     * input format of each cell of the candidate list:
     * vehicle,wrap,syn1_of_vehicle,syn2_of_vehicle,..synk_of_vehicle,syn1_of_wrap,..synk_of_wrap
     * 
     * 
     * @param bigrams HashMap<String,Integer> of all bigrams and their counts
     * @param unigrams HashMap<String,Integer> of all unigrams and their counts
     * @param SYNSETSIZE
     * @param k
     * @param model two models available: m1 and m2.
     * @param smoothing
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public HashMap<String, Double> nonSubFeatExtractConstituentDetails(List<String> NCsSynSets, HashMap<String, Integer> bigrams, HashMap<String, Integer> unigrams, int SYNSETSIZE, int k, String model, boolean smoothing, boolean genOutput) throws FileNotFoundException, IOException {


        long V = bigrams.size(); //unique bigrams
        long N = 0; //all bigrams
        for (String s : bigrams.keySet()) {
            N += bigrams.get(s);
        }

        System.out.println("N: "+N);
        System.out.println("V: "+V);
        


        Writer outfilef2f3f4Counts = null;
        Writer outfilef2f3Forms = null;
        Writer outfilef4Forms = null;
        Writer outfileEvalBigramProb = null;

        if (genOutput) {
            
            //get the path of the parent directory of the class path, assign it to p2output 
            File f1 = new File(Tools.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParentFile();
            String p2output = f1.getPath();
            
            outfilef2f3f4Counts = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(p2output + "_f1f2f3w1w2w1Primew2Prime.csv"), "UTF-8"));

            //output: the word forms
            outfilef2f3Forms = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(p2output + "_f1f2_forms.csv"), "UTF-8"));

            outfilef4Forms = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(p2output + "_f3_forms.csv"), "UTF-8"));

            //output that contains the probability of bigrams of the evalSet
            outfileEvalBigramProb = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(p2output + "counts_jprobs.csv"), "UTF-8"));
        }

        //results:
        HashMap<String, Double> candidatesSDMAs = new HashMap();
        /*
        variables to be used inside the while loop
        */
        double denumSDMA1;
        double denumSDMA2;
        double SDMA1;
        double SDMA2;
        
        //reading the input file.
        //format:
        //vehicle,wrap,vehicle,vehicles,truck,car,airbag,gear,semi-trailer,wrap,wrapping,wrapped,wraps,glued,stitched,sewn
        //String ncEtSyn = "";
        //while ((ncEtSyn = ncsEtsynsets.readLine()) != null) {
            
        for(String ncEtSyn : NCsSynSets){
            String elements[] = ncEtSyn.split("[,\\t]");
            String w1 = elements[0];
            String w2 = elements[1];
            String nc = w1 + " " + w2;
            
            List<String> synSetofW1 = new ArrayList();
            List<String> synSetofW2 = new ArrayList();

            if (genOutput) {
                outfilef2f3Forms.write(w1 + " " + w2 + ",Sim(w1)w2:,");
            }

            //smoothing factor for f1 (founSynw1withw2) and f2 (to be increased by one 
            //for each generated alternativecompound ):
            int smoothingForf1 = 0;
            int smoothingForf2 = 0;

            int allw1primecount = 0;
            int allw2primecount = 0;

            //create Syn(w1)w2 candidates
            int founSynw1withw2 = 0;
            int synsetSizew1 = 0;
            for (int i = 2; i <= 2 + k - 1; i++) {
                if (!elements[i].toLowerCase().equals(w1.toLowerCase())) {
                    if (!elements[i].toLowerCase().equals(w1.toLowerCase() + "s")) {

                       // System.out.println(elements[i] + " " + unigrams.get(elements[i]));
                        allw1primecount += unigrams.get(elements[i]);

                        synsetSizew1++;
                        if (synsetSizew1 <= SYNSETSIZE) {

                            synSetofW1.add(elements[i]);

                            String cand = elements[i] + " " + w2;
                            smoothingForf1++;                              
                            if (bigrams.containsKey(cand)) {
                                
                                
                                founSynw1withw2 = founSynw1withw2 + bigrams.get(cand);
                                
                            }
                            if (genOutput) {
                                /*
                                 Comment 1 and uncomment 2 for creating anti-collocations without checkthing them against
                                 the corpus. Do the reverse to return only the anti-collocations that were observed in the corpus.
                                 */
                                //(1)
//                              if (bigramsMap.containsKey(cand)) {
//                                    outfile2.write(elements[i] + " " + w2 + "\t");
//                                    System.out.println(elements[i] + " " + w2);
//                                }
                                //(2)
                                outfilef2f3Forms.write(elements[i] + " " + w2 + "\t");

                            }
                        }
                    }
                }
            }
            if (genOutput) {
                outfilef2f3Forms.write(",w1Sim(w2):,");
            }

            //create w1Syn(w2) candidates
            int synsetSizew2 = 0;
            int founw1withSynw2 = 0;
            for (int i = 2 + k; i <= 2 * k + 2 - 1; i++) {
                if (!elements[i].toLowerCase().equals(w2.toLowerCase())) {
                    if (!elements[i].toLowerCase().equals(w2.toLowerCase() + "s")) {

                        synsetSizew2++;
                        if (synsetSizew2 <= SYNSETSIZE) {
                            synSetofW2.add(elements[i]);
                            String cand = w1 + " " + elements[i];
                            smoothingForf2++;

                            if (bigrams.containsKey(cand)) {
                                founw1withSynw2 = founw1withSynw2 + bigrams.get(cand);
                            }

                            if (genOutput) {
                                /*
                                 Comment 1 and uncomment 2 for creating anti-collocations without checkthing them against
                                 the corpus. Do the reverse to return only the anti-collocations that were observed in the corpus.
                                 */
                                //(1)
//                                if (bigramsMap.containsKey(cand)) {
//                                    outfile2.write(w1 + " " + elements[i] + "\t");
//                                    System.out.println(w1 + " " + elements[i]);
//                                }
                                //(2)
                                outfilef2f3Forms.write(w1 + " " + elements[i] + "\t");
                            }
                        }
                    }
                }
            }

            if (genOutput) {
                outfilef2f3Forms.write("\n");
            }

            //smoothing factor for f4 (to be increased by one for each generated alternativecompound ):
            int smoothingForf3 = 0;

            //creating f4
            //create Syn(w1)w2 candidates
            //writing the anti collocation files from syn(w1) and syn(w2)
            int founSynw1withSynw2 = 0;
            if (genOutput) {
                outfilef4Forms.write(w1 + " " + w2 + ",Sim(w1)Sim(w2):,");
            }
            for (String s1 : synSetofW1) {
                for (String s2 : synSetofW2) {

                    String cand = s1 + " " + s2;
                    smoothingForf3++;

                    //check to make sure the created alternative bigram is not equal to the original bigram
                    if (!cand.equals(w1 + " " + w2)) {
                        if (bigrams.containsKey(cand)) {

                            founSynw1withSynw2 = founSynw1withSynw2 + bigrams.get(cand);

                        }
                    }

                    if (genOutput) {
                        /*
                         Comment 1 and uncomment 2 for creating anti-collocations without checkthing them against
                         the corpus. Do the reverse to return only the anti-collocations that were observed in the corpus.
                         */
                            //(1)
//                                if (bigramsMap.containsKey(cand)) {
//                                    outfile2.write(w1 + " " + elements[i] + "\t");
//                                    System.out.println(w1 + " " + elements[i]);
//                                }
                        //(2)

                        outfilef4Forms.write(s1 + " " + s2 + "\t");
                    }

                }
            }
            if (genOutput) outfilef4Forms.write("\n");

            if (smoothing) {

                founSynw1withw2 += smoothingForf1;
                founw1withSynw2 += smoothingForf2;
                founSynw1withSynw2 += smoothingForf3;
                
            }
            if (genOutput) 
                outfilef2f3f4Counts.write(founSynw1withw2 + " " + founw1withSynw2 + " " + founSynw1withSynw2 + " " + unigrams.get(w1) + " " + unigrams.get(w2) + " " + allw1primecount + " " + allw2primecount + "\n");

            /*
             The following part of the code, if uncommented, creates a file that contains the probability of each bigram
             This is a joint probability of w1w2. 
             The denuminator N must be given. 
             */
            

            double probOfBigram;
            if (bigrams.containsKey(w1 + " " + w2)) {

                int count = bigrams.get(w1 + " " + w2);

                if (smoothing) {
                    count++;
                    probOfBigram = (double) (count) / (double) (N + V);
                } else {
                    probOfBigram = (double) (count) / (double) N;
                }
                if (genOutput){ 
                    outfileEvalBigramProb.write(w1 + " " + w2 + " " + " " + count + " " + probOfBigram + "\n");
                }

            //if the bigram doesnt exist in the HashMap of all bigrams
            } else {

                int count = 0;

                if (smoothing) {
                    probOfBigram = (double) 1 / (double) (N + V);
                    count = 1;
                } else {
                    probOfBigram = 0;
                    count = 0;
                }
                if (genOutput) {
                    outfileEvalBigramProb.write(w1 + " " + w2 + " " + count + " " + probOfBigram + "\n");
                }
            }

            
            
            
            if (model.equals("m1")) {

                /*
                 If the alternatives were found in the corpus. If no alternative 
                 is found, SDMA cannot be computed for this nc.
                 */
                if (founSynw1withw2 > 0) {
                    /*
                     Smoothing condition already taken into account for numerator,
                     so here, it is only calculated for the denuminator.
                     */
                    if (smoothing) {
                        denumSDMA1 = (double) founSynw1withw2/(N + V);
                    } else {
                        denumSDMA1 = (double) founSynw1withw2/N;
                    }
                    SDMA1 = Math.log(probOfBigram / denumSDMA1);
                    candidatesSDMAs.put(nc, SDMA1);

                } else {
                    System.out.println("SDMA cannot be calculated for " + nc);
                }

            } else if (model.equals("m2")) {
                /*
                 If the alternatives were found in the corpus. If no alternative 
                 is found, SDMA cannot be computed for this nc.
                 */
                if (founw1withSynw2 > 0) {
                    /*
                     Smoothing condition already taken into account for numerator
                     so here, it is only calculated for the denuminator. 
                     */
                    if (smoothing) {
                        denumSDMA2 = (double) founw1withSynw2/(N + V);
                    } else {
                        denumSDMA2 = (double) founw1withSynw2/N;
                    }
                    SDMA2 = Math.log(probOfBigram / denumSDMA2);
                    candidatesSDMAs.put(nc, SDMA2);
                    //System.out.println(probOfBigram + " " + denumSDMA2 + " " + SDMA2);

                } else {
                    System.out.println("SDMA cannot be calculated for " + nc);
                }
            }
        }

//        System.out.println("Number of all bigrams(N): " + N);
//        System.out.println("Number of unique bigrams (V): " + V);

        if (genOutput) {
            outfilef2f3f4Counts.flush();
            outfilef2f3f4Counts.close();
            outfilef2f3Forms.flush();
            outfilef2f3Forms.close();
            outfilef4Forms.flush();
            outfilef4Forms.close();
            outfileEvalBigramProb.flush();
            outfileEvalBigramProb.close();
        }

        return candidatesSDMAs;
    }
}
