package abstractor;/*

 * Purpose: this function is used to do the pre-process jobs, such functions included:
 * 
 *  1. read a certain passage from a give file path: String readPassage(String path)
 *  
 *  2. section a certain passage to several paragraphs: String[] readParagraphs(String passage)
 *  
 *  3. section a set of paragraphs to sets of sentences: String[][] readSentences(String[] paragraphs)
 *  
 *  4. section a certain passage or a paragraph to a set of sentences: String[] readSentences(String content)
 *  
 *  5. section a certain passage, paragraph, or a sentence to a set of words: String[] readWords(String content)
 */


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class PreProcess {
	/*

	 * Purpose: this function is used to section words from a certain sentence, paragraph, or passage
	 */
	public static String[] readWords(String content) throws Exception{
		// using lucene and analyzer to section words
		PaodingAnalyzer paodingAnalyzer  = new PaodingAnalyzer();
		StringBuilder  stringBuilder = new StringBuilder();
		stringBuilder.delete(0, stringBuilder.length());
		StringReader stringReader = new StringReader(content);
		TokenStream tokenStream = paodingAnalyzer.tokenStream("", stringReader);
		Token token;
		// get the set of words which are stored in a String-type variable, appended each other using "\t"
		while((token = tokenStream.next()) != null){
			stringBuilder.append(token.termText()).append('\t');
		}
		if(stringBuilder.length() > 0){
			stringBuilder.setLength(stringBuilder.length() - 1);
		}
		// spilt the String-type variable and return
		String []tempWords = (stringBuilder.toString()).split("\t");
		return tempWords;
	}
	
	/*

	 * Purpose: this function is used to read sentences from a certain paragraph or passage
	 *  the parameter content can be a paragraph or a whole passage
	 */
	public static String[] readSentences(String content){
		content = content.replaceAll("　","");
		content = content.replaceAll(" ", "");
		content = content.replaceAll("" +(char)13 +(char)10, "");
		String []sentences = content.split("。|！");
		for(int i = 0; i<sentences.length; i++){
			sentences[i] = sentences[i] + "。";
		}
		return sentences;
	}
	
	/*

	 * Purpose: this function is used to read sentences using spilt signs "! 。"， Chinese only
	 */
	public static String[][] readSentences(String[] paragraphs) throws Exception{
		String [][]pSentences = new  String[paragraphs.length][];
		for(int i = 0; i< paragraphs.length; i++){
			pSentences[i] = paragraphs[i].split("。|！");
			for(int j = 0; j<pSentences[i].length; j++)
				pSentences[i][j] = pSentences[i][j] + "。";
		}
		return pSentences;
	}
	
	/*

	 * Purpose: this function is used to read paragraphs from a given passage
	 */
	public static String[] readParagraphs(String passage) throws Exception{
		// first, replace all the spaces(both Chinese and English ones) in the front of each paragraph
		passage = passage.replaceAll("　","");
		passage = passage.replaceAll(" ","");
		
		// second, spilt the passage using sign("0D 0A") 
		String []paragraphs = passage.split("" + (char)13 + (char)10);
		return paragraphs;
	}
	
	/*

	 * Purpose: this function is used to read a given file, 
	 *  return a String type value which contains the content of the passage
	 *  the variable "path" is the given path of the passage 
	 */
	public static String readPassage(String path) throws Exception{
		FileInputStream fileInputStream = new FileInputStream(path);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		
		// to store the content of the passage, '\n'('0D 0A') included
		StringBuffer passageBuffer = new StringBuffer();
		
		// read an char buffer(size of 65536 bits each time)
		int ch;
		while((ch = bufferedReader.read()) > -1){
			passageBuffer.append((char)ch);
		}
		
		bufferedReader.close();
		inputStreamReader.close();
		fileInputStream.close();
		return passageBuffer.toString();
	}
}
