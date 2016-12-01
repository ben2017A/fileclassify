

import HowNet.Function;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/*
 * Purpose: this class is used to store all the ids and terms corresponded with the keyword
 */
/*
 * Purpose: this class is used to store all the ids and terms corresponded with the keyword
 */
class Concept{
    int id;//id
    String term;//义项
    Concept(int id,String term){
        this.id = id;
        this.term = term;
    }
}
/*

 * Purpose: this class is the main class of this part, the main purpose of this class is used to
 * calculate the similarity of two semantic words
 */
public class Hownet {
    private Map wordmap;
    private long WORD_NUM;
    private HownetAPI ha = null;
    public HownetAPI getHownetAPI(){
        return this.ha;
    }
    public Hownet(HownetAPI ha){
        this.ha = ha;
        try {
            WORD_NUM = ha.GetUnitNum(ha.getConnnection());
            System.out.println("……………………………… hownet initialized, the number of units is: " + WORD_NUM);
            wordmap = new HashMap<String,HashSet>();
            readWord(ha);
            System.out.println("……………………………… "+ WORD_NUM + " units stored");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /*
     * this function is used to initialize all the dictionary, and store all the units in the Map
     */
    private void readWord(HownetAPI ha){
        for(int i=0;i<WORD_NUM;i++){
            //String keyword = function.HowNet_Get_Unit_Item(i, (byte)2, "****************");
            try {
                String keyword = ha.Get_Unit_Item2(i,ha.getConnnection());
                String term = ha.Get_Unit_Item10(i,ha.getConnnection());
                Concept concept = new Concept(i,term);
                if(wordmap.containsKey(keyword)){
                    Set set = (HashSet)wordmap.get(keyword);
                    set.add(concept);
                }
                else{
                    Set set = new HashSet<Concept>();
                    set.add(concept);
                    wordmap.put(keyword, set);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            /*String term = function.HowNet_Get_Unit_Item(i,(byte)10,"*************************************" +
                    "***********************************************************************************************" +
                    "******************************************");*/

        }
    }
    /*
     * this function is used to get the content of a certain DEF, which is obtained by a certain word
     * the parameter "type" illustrate whether to output the ID or the term of the word
     * if the type == 0, output term, == 1, output ID
     */
    public String getDEF_Content(String word,int type){
        if(wordmap.containsKey(word)){
            Set set = (HashSet)wordmap.get(word);
            Iterator iterator = set.iterator();
            Concept concept;
            String string = "";
            while(iterator.hasNext()){
                concept = (Concept)iterator.next();
                if(type == 0)
                    string += concept.term + '\t';
                if(type == 1)
                    string +="" + concept.id + '\t';
                if(type == 2)
                    string +="" + concept.id + '\t' + concept.term + '\n';
            }
            return string;
        }
        return null;
    }

    public String getSememe(String DEF_content, HownetAPI ha){
        try{
            String []DEF = DEF_content.split(":|\\{|\\}|\"");
            String dEF = "";
            for(int i = 0;i<DEF.length;i++){
                if(DEF[i].contains("|"))
                    dEF += DEF[i] + "\t";
            }
            return dEF;
        }catch (java.lang.NullPointerException e){return null;}
    }

    //this function is used to get the Sememe of a certain DEF by DEF_ID

    public String getSememe(int DEF_ID,HownetAPI ha){
        if(DEF_ID< -1 || DEF_ID > WORD_NUM){
            return null;
        }
       /* String term = function.HowNet_Get_Unit_Item(DEF_ID,(byte)10,"*************************************" +
                "***********************************************************************************************" +
                "***********************************************************************************************");*/
        String term = null;
        try {
            term = ha.Get_Unit_Item10(DEF_ID,ha.getConnnection());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return getSememe(term,ha);
    }

    private HashSet getWords(String word){
        if(wordmap.containsKey(word)){
            return (HashSet)wordmap.get(word);
        }
        return null;
    }
    /*
     * this function is used to calculate the similarities of all the "DEFs" of a given pair of words, and select the biggest value
     * as the similarity between the two words
     */
    public double calSimilarity(String word1,String word2,Function function){
        double similarity = 0.0;
        Set set1 = getWords(word1);
        Set set2 = getWords(word2);
        if(set1 == null || set2 == null){
            return 0.0;
        }
        Iterator iterator1 = set1.iterator();
        Concept con1;
        Concept con2;
        while(iterator1.hasNext()){
            con1 = (Concept)iterator1.next();
            Iterator iterator2 = set2.iterator();
            while(iterator2.hasNext()){
                con2 = (Concept)iterator2.next();
                int def1 = con1.id;
                int def2 = con2.id;
                double stemp = function.HowNet_Get_Concept_Similarity(def1, def2, (float)1.6, (float)0.50, (float)0.20, (float)0.17, (float)0.13);
                if(similarity <stemp)
                    similarity = stemp;
            }
        }
        return similarity;
    }
}
