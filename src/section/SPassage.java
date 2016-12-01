/**
 * Created by Administrator on 2016/11/14.
 * Purpose: this class is used to store the parameters of a certain passage
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
public class SPassage {
    private static int passage_num = 0;  //the number of passages
    private Map wdf_map;  //the map used to store the "df" values of a certain word
    private Set pset;  //the set used to store all the passage classes
    private StopWords stopwords;

    public SPassage(String stopwordspath){
        wdf_map = new HashMap<String, Integer>();
        pset = new HashSet<Passage>();
        stopwords = new StopWords(stopwordspath);
    }

    // output the max frequency map
    public Map getMaxFrequency(){
        Map map = new HashMap<String, Integer>();
        Iterator iterator = pset.iterator();
        while(iterator.hasNext()){
            Passage text = (Passage)iterator.next();
            map.put(text.title, text.maxf);
        }
        return map;
    }
    // output the max weight map
    public Map getMaxWeight(){
        Map map = new HashMap<String, Double>();
        Iterator iterator = pset.iterator();
        while(iterator.hasNext()){
            Passage text = (Passage)iterator.next();
            map.put(text.title, text.maxw);
        }
        return map;
    }

    // output the frequency map
    public Map getWF_Map(){
        Map map = new HashMap<String, Map>();
        Iterator iterator = pset.iterator();
        while(iterator.hasNext()){
            Passage text = (Passage)iterator.next();
            map.put(text.title, text.wf_map);
        }
        return map;
    }
    // output the weight map
    public Map getWW_Map(){
        Map map = new HashMap<String, Map>();
        Iterator iterator = pset.iterator();
        while(iterator.hasNext()){
            Passage text = (Passage)iterator.next();
            map.put(text.title, text.ww_map);
        }
        return map;
    }

    // free all the space occupied
    public void freeSPassage(){
        wdf_map.clear();
        pset.clear();
        passage_num = 0;
    }

    /*

     * Purpose: this function is used to calculate all the tf-idf values of all words
     */
    public void calTF_IDF(int N){
        Iterator iterator = pset.iterator();
        while(iterator.hasNext()){
            Passage pass = (Passage)iterator.next();
            pass.p_calTF_IDF(N);
        }
    }

    /*

     * Purpose: this function is used to do the following things:
     * 	1. add the passage_num by 1
     * 	2. spilt the words into an word array
     *  3. construct a new passage class, and store this class into "pset"
     *  4. update the wdf_map
     */
    public void readPassage(String title, String words){
        this.passage_num ++;
        Map tmpMap = new HashMap<String, Integer>();
        String []section_words = words.split("\t");
        Passage pas = new Passage(title);
        for(int i = 0;i< section_words.length; i++){
            if(!stopwords.checkStopWord(section_words[i]))
                pas.putWord(section_words[i]);
        }
        //  update the wdf_map
        Iterator iterator = pas.wf_map.keySet().iterator();
        while(iterator.hasNext()){
            String tempword = (String)iterator.next();
            if(wdf_map.containsKey(tempword)){
                int df = (Integer)wdf_map.get(tempword);
                df ++;
                wdf_map.remove(tempword);
                wdf_map.put(tempword,df);
            }
            else{
                wdf_map.put(tempword, 1);
            }
        }
        // store the passage in pset
        pset.add(pas);
    }

    /*

     * Purpose: this class is used to store all the stopwords and check
     *  whether a certain word appears in a passage is a stopword
     */
    private class StopWords{
        private Set stopwordset;
        private String stopwordspath;
        StopWords(String path){
            stopwordset = new HashSet<String>();
            this.stopwordspath= path;
            readStopWords();
        }
        private void readStopWords(){
            try {
                FileReader fileReader = new FileReader(stopwordspath);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                try {
                    String readTemp;
                    readTemp = bufferedReader.readLine();
                    while(readTemp != null){
                        stopwordset.add(readTemp);
                        readTemp = bufferedReader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        private boolean checkStopWord(String word){
            if(stopwordset.contains(word))
                return true;
            return false;
        }
    }

    /*

     * Purpose:
     *	1. store the title, the sectioned words and the max frequency of all the words
     *  2. calculate the TF_IDF values and store them in the "ww_map"
     */
    private class Passage{
        private String title;  //the title of passage
        private int maxf;  //the max frequency of a certain passage
        private double maxw; // the max weight of a certain passage
        private Map wf_map;  //the map of feature words and corresponding frequencies
        private Map ww_map;  //the map of feature words and corresponding weights

        private Passage(String title){
            this.title = title;
            maxf = -1;
            maxw = -1.0;
            wf_map = new HashMap<String,Integer>();
            ww_map = new HashMap<String,Double>();
        }

        // output the map of feature words and corresponding frequencies
        public Map getFrequencies(){
            return wf_map;
        }
        // output the map of feature words and corresponding tf-idf weights
        public Map getTfidfWeights(){
            return ww_map;
        }

        // output the map of feature words and corresponding max frequencies
        public int getMaxFrequency(){
            return maxf;
        }

        // output the map of feature words and corresponding max weights
        public double getMaxWeight(){
            return maxw;
        }

        // output the title of the passage
        public String getTitle(){
            return title;
        }

        // free the space occupied by wf_map
        public void freeMaxFrequency(){
            wf_map.clear();
        }

        // free the space occupied by ww_map
        public void freeMaxWeight(){
            ww_map.clear();
        }

        // add a word and update the frequency map
        private void putWord(String word){
            if(wf_map.containsKey(word)){
                int frequency = (Integer)wf_map.get(word);
                frequency ++;
                if(maxf < frequency)
                    maxf = frequency;
                wf_map.remove(word);
                wf_map.put(word, frequency);
            }
            else {
                wf_map.put(word, 1);
            }
        }

        // calculate the tf-idf values of each word
        private void p_calTF_IDF(int N){
            Set wordset = wf_map.keySet();
            Iterator word_iterator = wordset.iterator();
            while(word_iterator.hasNext()){
                String word = (String)word_iterator.next();
                int frequency = (Integer)wf_map.get(word);
                int df = (Integer)wdf_map.get(word);
                double weight = (double)frequency/(double)maxf;
                weight = weight * Math.log((double)passage_num/(double)df);
                if(weight > maxw)
                    maxw = weight;
                ww_map.put(word, weight);
            }
            // get the top N feature words
            SHeap sHeap = new SHeap(N);
            ww_map = sHeap.getHeapMap(ww_map);
        }
    }
}
