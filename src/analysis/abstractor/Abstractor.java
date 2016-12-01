package abstractor;/*

 * Purpose: this class is used to implement the single passage abstractor job
 */


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import abstractor.PreProcess;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;


public class Abstractor {
	//--------------------------------------------------------------------------------------------------------------
	private static String testPath = "E:\\fileclassify_data\\zengxiaosen\\concept-file\\source\\policy_stocks.txt";
	private static PreProcess preProcess = new PreProcess();
	//--------------------------------------------------------------------------------------------------------------
	
	/*

	 * Purpose: this function is used to check whether a word is a "significant word"
	 */
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// this function needs to be modified
	private boolean significantCheck(String word){
		return true;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/*

	 * Purpose: this function is used to get the frequency, length,
	 *  and the number of paragraphs the word appears
	 */
	
	
	public Abstractor(){
	}
	
	public static void main(String []args) throws Exception{
	}
	
	/*
	 * Purpose: this class is used to get the frequency of a word in the passage,
	 *  the length of the word and also the times the word appears in different paragraphs
	 */
	private class WordFeature{
		private int frequency;
		private int wordLength;
		private int numParagraphs;
		public WordFeature(int frequency, int wordLength, int numParagraphs){
			this.frequency = frequency;
			this.wordLength = wordLength;
			this.numParagraphs = numParagraphs;
		}
		public int getFrequency(){
			return frequency;
		}
		public int getWordLength(){
			return wordLength;
		}
		public int getNumParagraphs(){
			return numParagraphs;
		}
	}
	
}
