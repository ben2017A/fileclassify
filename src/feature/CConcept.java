/**
 * Created by Administrator on 2016/11/14.
 */

// Purpose: this class is used to map the feature words space to conception space

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import HowNet.Function;
public class CConcept {

    public CTree ctree;
    public CWord2Sememe cword2sememe;
    public Hownet hownet;
    // featureMap is used to store the sememes and corresponding weights of all passages
    public Map conceptMap;


    public CConcept() throws ClassNotFoundException, SQLException, UnsupportedEncodingException {
       /* function = new Function();
        if(!function.HowNet_Initial() || !function.HowNet_InitialSynAtnCon())
            return;*/
        // the sequence to initialize is important, the type CTree must be initialized after Function
        String database = "sys";
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database + "?useUnicode=true&characterEncoding=UTF8", "root", "qazwsxedcrf123");
        System.out.println("Connection Successful!");

        HownetAPI ha = new HownetAPI(connection);
        hownet = new Hownet(ha);
        ctree = new CTree(hownet);
        cword2sememe = new CWord2Sememe(hownet,ctree);
            /*int result = ha.Get_Sememe_Code("Attachment|归属",connection);
            result = ha.Get_Sememe_Hyp(95, connection);
            String sResult = ha.Get_Sememe_String(95, connection);
            sResult = ha.Get_Unit_Item10(9750, connection);
            sResult = ha.Get_Unit_Item2(9750, connection);
            result = ha.Get_TreeID(95, connection);
            result = ha.GetUnitNum(connection);
            System.out.println(sResult);
            System.out.println(result);*/
        conceptMap = new HashMap<String, Map>();
    }

    /*public CConcept(){
        function = new Function();
        if(!function.HowNet_Initial() || !function.HowNet_InitialSynAtnCon())
            return;
        // the sequence to initialize is important, the type CTree must be initialized after Function
        ctree = new CTree(function);
        hownet = new Hownet(function);
        cword2sememe = new CWord2Sememe();
        conceptMap = new HashMap<String, Map>();
    }*/

    // free space occupied by conceptMap
    public void freeConceptMap(){
        this.conceptMap.clear();
    }

    // free space occupied by the element of the map
    public boolean removeConceptMapByTitle(String title){
        if(this.conceptMap.containsKey(title)){
            this.conceptMap.remove(title);
            return true;
        }
        return false;
    }

    public Map getConceptMap(){
        return this.conceptMap;
    }

    /*
     * Purpose: this function is used to select the sememe that represent a feature word best
	 *  and calculate the corresponding weight of the sememe
     */

    public void calFeatureSpace(Map wfmap, Map wwmap, Map maxf, Map maxw) throws UnsupportedEncodingException {
        //
        // for each passage, select the sememe of a feature word
        Iterator twiterator = wwmap.keySet().iterator();
        Iterator tfiterator = wfmap.keySet().iterator();
        Iterator maxf_iterator = maxf.keySet().iterator();
        Iterator maxw_iterator = maxw.keySet().iterator();
        while(twiterator.hasNext()){
            // define a tempfeature to store the sememe and the corresponding weights
            Map tempconcept = new HashMap<String, Double>();
            // get the map of a certain passage
            String title = (String)twiterator.next();
            // get the feature map of a certain passage
            Map tempwwmap = (Map)wwmap.get(title);
            Map tempwfmap = (Map)wfmap.get(title);
            int tempmaxf = (Integer)maxf.get(title);
            double tempmaxw = (Double)maxw.get(title);
            Iterator witerator = tempwwmap.keySet().iterator();
            double maxWeightValue = 0.0;
            while(witerator.hasNext()){
                // store the max weight value of the map
                try{
                    String word = (String)witerator.next();
                    // get the sememe and the corresponding weight
                    String sememe_weight = cword2sememe.getSememe(word, tempwwmap, tempwfmap, ctree, hownet,tempmaxw, tempmaxf);
                    // section the sememe and the weight respectively
                    String []sw = sememe_weight.split("\t");
                    // update tempfeature
                    if(tempconcept.containsKey(sw[0])){
                        double currentvalue = (Double)tempconcept.get(sw[0]);
                        currentvalue = currentvalue + Double.parseDouble(sw[1]);
                        if(maxWeightValue < currentvalue)
                            maxWeightValue = currentvalue;
                        tempconcept.remove(sw[0]);
                        tempconcept.put(sw[0], currentvalue);
                        continue;
                    }
                    else{
                        double value = Double.parseDouble(sw[1]);
                        if(maxWeightValue < value)
                            maxWeightValue = value;
                        tempconcept.put(sw[0], value);
                    }

                }catch(java.lang.NullPointerException e){}
            }
            // normalization
            Iterator conceptIterator = tempconcept.keySet().iterator();
            // the map used to store sememe and normalized weight value
            Map nTempConcept = new HashMap<String, Double>();
            while(conceptIterator.hasNext()){
                String sememe = (String)conceptIterator.next();
                Double value = (Double)tempconcept.get(sememe);
                nTempConcept.put(sememe, value/maxWeightValue);
            }
            tempconcept.clear();
            // store tempfeature to conceptMap
            conceptMap.put(title, nTempConcept);
        }
    }
    /*
         * Output the contents of a certain map into certain files.
         * One can change this output function in order to satisfy all the format requirements
         */
    private void output(Map map, BufferedWriter bufferedWriter){
        // get the information of a certain passage
        Set set = map.keySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()){
            String title = (String)iterator.next();
            try {
                // output the contents(feature words or concepts) of a certain passage
                Map map2 = (Map)map.get(title);
                Set set2 = map2.keySet();
                Iterator iterator2 = set2.iterator();
                while( iterator2.hasNext()){
                    String word = (String)iterator2.next();
                    double value = (Double)map2.get(word);
                    bufferedWriter.write(word + "," + value + '\t');
                }
                bufferedWriter.write("\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // output the Concept map using function "output"
    public void outputConcept(Map map){
        try {
            FileWriter fileWriter = new FileWriter(Output.ConceptPath.getPath());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            output(map,bufferedWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // output the Concept map using function "output"
    // if the name is given, use this function instead of the other one
    public void outputConcept(Map map, String path){
        try {
            FileWriter fileWriter = new FileWriter(path);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            output(map,bufferedWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException, SQLException, ClassNotFoundException {
        // the number of feature words or concepts
        final int N = 30;
        CConcept concept;
        Section section;
        concept = new CConcept();
        section = new Section("E:\\fileclassify_data\\zengxiaosen\\concept-file\\source\\stopwords.txt");
        try {
            File base = new File("E:\\fileclassify_data\\zengxiaosen\\concept-file\\source\\Fudan_train");
            for(File dir : base.listFiles()){
                try {
                    section.indexDocs(new File(dir.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // calculate the tf-idf values
                section.calPTF_IDF(N);

                Map myWeightMap = section.getWeightMap();
                section.outputFrequency(myWeightMap);

                concept.calFeatureSpace(section.getFrequencyMap(), section.getWeightMap(), section.getMaxFrequency(), section.getMaxWeight());
                concept.outputConcept(concept.getConceptMap(),"E:\\fileclassify_data\\zengxiaosen\\concept-file\\results\\Fudan-concept-me\\" + dir.getName() + ".txt");
                section.outputFrequency(section.getWeightMap(), "E:\\fileclassify_data\\zengxiaosen\\concept-file\\results\\Fudan-frequency-me\\" + dir.getName() + ".txt");
                System.out.println("finished "+ "dir: " + dir.getName());
                concept.freeConceptMap();
                section.freeSection();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
