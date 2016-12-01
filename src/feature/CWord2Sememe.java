/**
 * Created by Administrator on 2016/11/14.
 */

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import HowNet.Function;
//Purpose: this class is used to represent a word to a certain sememe

public class CWord2Sememe {
    private Hownet hownet = null;
    private CTree ctree = null;

    public CWord2Sememe(Hownet hownet,CTree ctree){
        this.hownet = hownet;
        this.ctree = ctree;
    }

    public Hownet getHownet(){
        return this.hownet;
    }

    public CTree getCtree(){
        return this.ctree;
    }



    //Purpose: this function is used to select the proper feature
    private String getFeature(Hownet hownet, String word, Map wfmap){
        if(hownet.getDEF_Content(word,0) == null)
            return null;
        // get the content of the DEFs
        String []tempcontent = hownet.getDEF_Content(word, 0).split("\t");
        // store the frequency of each DEF
        int []tfDEF = new int[tempcontent.length];
        int maxDEF = 0;  //store the ID of the DEF that has the greatest frequency
        int maxFrequency = 0;  //store the value of the current greatest frequency
        for(int i=0;i<tempcontent.length;i++){
            tfDEF[i] = 0;
            // there are cases that the content of the current DEF is the same with the former one
            if(i>0 && tempcontent[i].equals(tempcontent[i -1]))
                continue;
            // get all the sememes of the DEF
            String []tempsememe = hownet.getSememe(tempcontent[i], hownet.getHownetAPI()).split("\t");
			/*
			 * there are cases a sememe appears more than one time in a certain DEF
			 * thus the String-type Set is defined to avoid re-calculating the frequencies of a certain sememe
			 */
            Set sememeset = new HashSet<String>();
			/*
			 * the following loop do such things:
			 *  1. get the sememes that a certain DEF contains
			 *  2. calculate the frequencies that a certain DEF has, measured by
			 *     the sum of the frequencies of all the sememes of the DEF that
			 *     appear in the wfmap
			 */
            for(int j = 0;j<tempsememe.length; j++){
                // get the Chinese part of a certain sememe
                String []spilt_Chinese = tempsememe[j].split("\\|");
                if(sememeset.contains(spilt_Chinese[1]))
                    continue;
                // update the value of the tfDEF[i]
                if(wfmap.containsKey(spilt_Chinese[1])){
                    tfDEF[i] += (Integer)wfmap.get(spilt_Chinese[1]);
                }
                sememeset.add(spilt_Chinese[1]);
            }
            // select the content of the DEF that has the greatest frequency
            if(maxFrequency < tfDEF[i]){
                maxFrequency = tfDEF[i];
                maxDEF = i;
            }
        }
        return tempcontent[maxDEF];
    }

    /*
     * Purpose: this function is used to select the proper sememe, the output format is as follows:
	 *  maxsememeID	maxweight. For example: "1120	3.33253"
     */

    // first, define all the constant parameters used to calculate the weight of a certain sememe
    double WEIGHT_A = 1.5;
    double WEIGHT_B = 5.0;
    double WEIGHT_C = 0.15;

    public String getSememe(String word,Map wwmap, Map wfmap, CTree ctree,Hownet hownet, double maxw, int maxf) throws UnsupportedEncodingException {
        // get the content of DEF
        String contentDEF = getFeature(hownet,word,wfmap);
        String contentSememe = hownet.getSememe(contentDEF,hownet.getHownetAPI());
			/*
			 *  if the word does not exist in the hownet dictionary, check whether the frequency
			 *  or the weight is greater than half the value of the max frequency of weight
			 */
        if(contentDEF == null || contentSememe == null){
            int frequency = (Integer)wfmap.get(word);
            double weight = (Double)wwmap.get(word);
            if(frequency < (maxf/2) || weight < (maxw/2)){
                return null;
            }
				/*
				 * *************************************************************************************
				 * You can change the return value here
				 * *************************************************************************************
				 */
            return null;
            //return "" + word + "\t" + weight;
        }
        // get the sememes that a contentDEF contains
        String []sememes = contentSememe.split("\t");
        // store the max weight of a certain sememe
        double maxweight = 0.0;
        // store the ID of the sememe with the max weight
        int maxsememeID = 65535;
			/*
			 * there are cases a sememe appears more than one time in a certain DEF
			 * thus the String-type Set is defined to avoid re-calculating the frequencies of a certain sememe
			 */
        Set sememeset = new HashSet<String>();
			/*
			 *  select the sememe that represent the DEF best, which is measured by the following factors:
			 *   1. the tree that the sememe locates at
			 *   2. the depth that the sememe lies in the tree
			 *   3. the number of LYPs of the sememe
			 *  the function is as follows:
		     *  W(Sememe) = wtree * (log(rlevel + WEIGHT_A) + 1/(numLYP + WEIGHT_B) + WEIGHT_C);
		     *  where: wtree stands for the weight of the tree which the sememe locates at
		     *            rlevel stands for the level the sememe locates at the sememe  tree
		     *            numLYP stands for the number of the LYPs the sememe has
			 */

        for(int i=0;i<sememes.length;i++){
            if(sememeset.contains(sememes[i]))
                continue;
            sememeset.add(sememes[i]);
            //int sememeID = function.HowNet_Get_Sememe_Code(sememes[i]);
            int sememeID = 0;
            try {
                sememeID = hownet.getHownetAPI().Get_Sememe_Code(sememes[i],hownet.getHownetAPI().getConnnection());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            double wtree = ctree.getTreeTypeWeight(sememeID);
            int rlevel = ctree.getLevel(sememeID);
            int numLYP = ctree.getLYPNum(sememeID);
            double tempweight = wtree * (Math.log((double)rlevel + WEIGHT_A) + 1/((double)numLYP + WEIGHT_B) + WEIGHT_C);
            if(tempweight > maxweight){
                maxweight = tempweight;
                maxsememeID = sememeID;
            }
        }

        // multiply the weight of the feature word and the sememe
        double wordweight = (Double)wwmap.get(word);
			/*
			 * *************************************************************************************
			 * You can change the return value here
			 * *************************************************************************************
			 */
        //maxweight = wordweight;
        maxweight = maxweight * wordweight;
        //return "" + function.HowNet_Get_Sememe_String(maxsememeID) + "\t" + maxweight;
        if(maxweight <= 0){
            maxweight = -maxweight;
        }
        return "" + hownet.getHownetAPI().Get_Sememe_String(maxsememeID,hownet.getHownetAPI().getConnnection()) + "\t" + maxweight;


    }



}
