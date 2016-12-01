/**
 * Created by Administrator on 2016/11/14.
 */


import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import info.monitorenter.cpdetector.io.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import net.paoding.analysis.analyzer.PaodingAnalyzer;

/*
 * Purpose: this class is used to do the following things
 *  1. section all the words in a passage
 *  2. wipe off the stopwords
 *  3. save the results in SPassage
 */
public class Section {

    private PaodingAnalyzer analyzer;
    private StringBuilder sb;
    private SPassage sp;

    public Section(String stopwordspath){
        sp = new SPassage(stopwordspath);
        analyzer = new PaodingAnalyzer();
        sb = new StringBuilder();
    }

    private String getCharset(String fileName) throws IOException{
        String code = "GBK";
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

        detector.add(new ParsingDetector(false));
        detector.add(JChardetFacade.getInstance());
        detector.add(ASCIIDetector.getInstance());
        detector.add(UnicodeDetector.getInstance());
        java.nio.charset.Charset charset = null;

        File f = new File(fileName);

        charset = detector.detectCodepage(f.toURI().toURL());
        code = "GBK";
        if(charset.name() == "UTF-8")
            code = "UTF-8";

//		System.out.println(code);
        return code;
    }

    /*

     * Puspose: the function is used to section the words in a passage whose name is
     *  obtained by the parameter "name", the results returned is a big String-type
     *  instance which distinguish the words by char '\t'
     */
    String dissect(String name){
        try{
            sb.delete(0, sb.length());
            //define the reader to avoid encoding errors
            File file = new File(name);
            String charset = getCharset(name);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            TokenStream ts = analyzer.tokenStream("", bufferedReader);
            Token token;
            while((token = ts.next()) != null){
                sb.append(token.termText()).append('\t');
            }
            if(sb.length() > 0){
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    /*

     * Purpose: the function is used to read each passage in a recursive way and section the words
     */
    public void indexDocs(File file)
            throws IOException{
        if(file.canRead()){
            if(file.isDirectory()){
                String []files = file.list();
                if(files != null){
                    for(int i=0;i<files.length;i++){
                        indexDocs(new File(file,files[i]));
                    }
                }
                return;
            }
            else{
                String token_result = dissect(file.getPath());
                sp.readPassage(file.getName(), token_result);
            }
        }
    }

    // calculate the TF-IDF values
    public void calPTF_IDF(int N){
        sp.calTF_IDF(N);
    }

    // get max frequency
    public Map getMaxFrequency(){
        return sp.getMaxFrequency();
    }

    // get max weight
    public Map getMaxWeight(){
        return sp.getMaxWeight();
    }

    // get frequency map
    public Map getFrequencyMap(){
        return sp.getWF_Map();
    }

    // get weight map
    public Map getWeightMap(){
        return sp.getWW_Map();
    }

    // free space
    public void freeSection(){
        sp.freeSPassage();
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
                bufferedWriter.write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // output the Frequency map using function "output"
    public void outputFrequency(Map map){
        try {
            FileWriter fileWriter = new FileWriter(Output.FeatureWordPath.getPath());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            output(map,bufferedWriter);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // output the Frequency map using function "output"
    // if the path is given, use this function instead of the other one
    public void outputFrequency(Map map,String path){
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
}

