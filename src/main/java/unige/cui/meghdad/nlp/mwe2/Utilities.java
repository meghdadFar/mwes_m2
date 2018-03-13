package unige.cui.meghdad.nlp.mwe2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Meghdad Farahmand (meghdad.farahmand@gmail.com)
 * @date 13.03.18
 */
public class Utilities {


    public LinkedHashSet getCandidates(String pathToCandidates) throws IOException{
        /*
        Since at this point no frequency information is needed, I put the candidates in
        a HashSet instead of a HashMap.
        */
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        String Entry = "";
        Pattern entryETfreq = Pattern.compile("(\\w+\\s\\w+)\\s?(\\d+)?$");
        Matcher entryETfreqM;

        BufferedReader candidateFile = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(pathToCandidates), "UTF8"));

        while ((Entry = candidateFile.readLine()) != null) {
            entryETfreqM = entryETfreq.matcher(Entry);
            if (entryETfreqM.find()) {
                candidates.add(entryETfreqM.group(1));
            }
        }
        return candidates;
    }


    public List candidatesToVectors(LinkedHashSet<String> candidates, HashMap<String,Integer> words, List<String> vectors){

        List ret = new ArrayList();

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

        ret.add(avail_lw_forms);
        ret.add(avail_lw_Rep);
        return ret;
    }


}
