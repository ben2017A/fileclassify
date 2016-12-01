package classifier;
/*
 * Purpose: this class is used to get samples from data sources and store them
 * 	in specific data structures
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import weka.attributeSelection.FilteredAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import wlsvm.WLSVM;

public class Model{

	/*
	 *  key : to find whether an attribute is stored quickly
	 *  
	 *  sampleMap : store all the samples and corresponding class values
	 *  
	 *  arrayList : store all the attributes in a certain sequence
	 *  
	 *  instances: the training instances
	 */
	private Map key;
	private Map sampleMap;
	private ArrayList attributeList;
	/*
	 * ID : to store the current number of the stored attributes
	 * 
	 * passageNum : the number of all the passages
	 * 
	 * dirPath: the path of the dir paths, whose files are texts corresponding to different classes
	 * 
	 * samplePath: the path of sample instances path, the default instances path
	 * 
	 * modelPath: default model path
	 * 
	 * instanceName: instance name
	 */
	private static int ID = 0;
	private static int passageNum = 1;
	private static String dirPath;
	private static String samplePath;
	private static String modelPath;
	private static String instanceName;
	
	
	public Model(String modelPath, String dirPath, String samplePath, String instanceName){
		key = new HashMap<String,Integer>();
		attributeList = new ArrayList<String>();
		sampleMap = new HashMap<String, ArrayList>();
		this.modelPath = modelPath;
		this.samplePath = samplePath;
		this.instanceName = instanceName;
		this.dirPath = dirPath;
	}
	
	/*

	 * Purpose : this function is used to free all the space that the class Model occupies
	 */
	public void freeModel(){
		ID = 0;
		passageNum = 1;
		dirPath = "";
		samplePath = "";
		modelPath = "";
		instanceName = "";
		key.clear();
		sampleMap.clear();
		attributeList.clear();
	}
	
	/*

	 * Purpose: this function is used to read the contents from a certain file,
	 *  store and update the values
	 */
	private ArrayList readSampleFile(File file) {
		try {
			InputStreamReader ir = new InputStreamReader(new FileInputStream(file),"UTF-8");
			BufferedReader br = new BufferedReader(ir);
			/* 
			 * line : read a line from the file, the format of the line is as follows:
			 * "(String)keyword,(Double)weight \t (String)keyword,(Double)weight \t ……"
			 * 
			 * tempArray : to store the IDs of feature words and the corresponding weights
			 */	
			String line;
			ArrayList tempArray = new ArrayList<SortedMap>();		
			
			while(true){
				if((line = br.readLine()) == null) break;
				// there are some cases that there is nothing in a line
				if(line.length() < 3)
					continue;
				
				String []features = line.split("\t");
				SortedMap sortedMap = new TreeMap<Integer, Double>();
				/*
				 * 1. if the feature(attribute) is already stored in the key map, put it 
				 * 	in the SortedMap with the corresponding weight
				 * 
				 * 2. else, before storing it, put the feature(attribute) in the key Map first,
				 * 	update the ID in the last
				 */
				for(int i = 0; i < features.length; i ++){
					String []feature = features[i].split(",");
					// for the purpose of outputing to the text
					feature[0]  = feature[0].replace(' ', '-');
					if(key.containsKey(feature[0])){
						int attributeID = (Integer)key.get(feature[0]);
						sortedMap.put(attributeID, Double.parseDouble(feature[1]));
					}
					else{
						key.put(feature[0], ID);
						sortedMap.put(ID,Double.parseDouble(feature[1]));
						attributeList.add(feature[0]);
						ID ++;
					}
				}
				tempArray.add(sortedMap);
				passageNum ++;
			}
			br.close();
			ir.close();
			return tempArray;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*

	 * Purpose: this function is used to read contents from a given dir
	 * 	using the function readFile(File file), 3 points to be notified:
	 * 	1. this function is used to get the training data
	 * 	2. the name of the file is the default class value
	 * 	3. each file in the dir correspond to a training instance set 
	 */
	public Map readInstances(String dirPath){
		File dir = new File(dirPath);
		// store all the instances in a file and corresponding values
		Map instancesMap = new HashMap<String, ArrayList>();
		for(File file : dir.listFiles()){
			String []fileValue = file.getName().split("\\.");
			String classValue = fileValue[0];
			//-------------------------------------------------
			System.out.println("read file: " + file.getName());
			instancesMap.put(classValue, readSampleFile(file));
		}
		return instancesMap;
	}

	/*

	 * Purpose: this function is used to read contents from a sample dir
	 */
	private void readSampleData(String dirPath){
		sampleMap = readInstances(dirPath);
	}
	
	public Map getKey(){ return key;}
	public Map getSampleMap(){ return sampleMap;}
	public ArrayList getAttributeList(){return attributeList;}
	
	/*

	 * Purpose: this class is used to get all the instances
	 */
	public Instances getSamples(int dimensionNum){
		readSampleData(dirPath);
		/*
		 * instances : store all the instances
		 * 
		 * attributesNum : the number of attributes, whose valuse is "the number of attributeList"
		 * 
		 * attributesFastVector : the attributes Vector
		 * 
		 * classValues : store the class names
		 */
		Instances instances = null;
		int attributesNum = attributeList.size();
		FastVector attributesFastVector = new FastVector(attributesNum + 1);
		FastVector classValues = new FastVector(sampleMap.size());
		
		//add attributes to attributesFastVector
		for(int i = 0; i<attributesNum; i++)
			attributesFastVector.addElement(new Attribute((String)attributeList.get(i)));
		
		// store the class values into attributesFastVector
		Iterator iteratorSample = sampleMap.keySet().iterator();
		while(iteratorSample.hasNext())
			classValues.addElement((String)iteratorSample.next().toString());
		attributesFastVector.addElement(new Attribute("class",classValues));
		
		// store the class values in the instances
		instances = new Instances(instanceName,attributesFastVector,passageNum);
		instances.setClassIndex(instances.numAttributes() - 1);
		
		// set the data stored in sampleMap into instances
		Iterator iteratorSample2 = sampleMap.keySet().iterator();
		while(iteratorSample2.hasNext()){
			// get the classValue of one instance
			String classValue = (String)iteratorSample2.next();
			// get the data of one instance
			ArrayList tempArray = (ArrayList) sampleMap.get(classValue);
			Iterator iteratorArray = tempArray.iterator();
			while(iteratorArray.hasNext()){
				SortedMap sortedMap = (SortedMap)iteratorArray.next();
				Iterator iteratorMap = sortedMap.keySet().iterator();
				// construct a double array to store the weights and 
				// for the initializing of one certain instance
				double []weights = new double[attributesNum + 1];
				for(int i = 0; i<weights.length; i++)
					weights[i] = 0.0;
				while(iteratorMap.hasNext()){
					int id = (Integer)iteratorMap.next();
					double weight = (Double)sortedMap.get(id);
					weights[id] = weight;
				}
				weights[weights.length - 1] = classValues.indexOf(classValue);
				Instance instance = new Instance(attributeList.size(),weights);
				instances.add(instance);
			}
		}
		System.out.println("construct sample instances successfully");
		if(dimensionNum < 0 || dimensionNum > instances.numAttributes() - 1)
			return instances;
		else
			return getFilterInstances(instances,dimensionNum);
	}
	
	/*

	 * Purpose: this function is used to abridge the number of the attributes of a certain intances
	 */
	private Instances getFilterInstances(Instances instances, int dimensionNum){
		// if the number of the attributes is already smaller than the given dimensionNum, return directly
		if(instances.numAttributes() - 1 <= dimensionNum)
			return instances;
		
		// define the attribute selection and evaluation variables
		Ranker ranker = new Ranker();
		FilteredAttributeEval filteredAttributeEval = new FilteredAttributeEval();
		// builder Evaluator
		try {
			filteredAttributeEval.buildEvaluator(instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// get the attributes and the classAttribute
		FastVector attributes = new FastVector(dimensionNum + 1);
		Attribute classAttribute = instances.classAttribute();
		// get the relationship of the attribute id and the new attribute id
		Map tempHashMap = new HashMap<Integer, Integer>();
		
		// the main filter part
		try {
			ranker.search(filteredAttributeEval, instances);
			/*
			 * tempDouble is a 2-dimension array, the format is as follows:
			 *  tempDouble[attributeNum][2], of which tempDouble[attributeNum][0] corresponds to the id of the attribute, 
			 *  tempDouble[attributeNum][1] corresponds to the weight of the attribute.
			 */
			double [][]tempDouble = ranker.rankedAttributes();
			// add numerical attributes
			for(int i = 0; i< dimensionNum; i++){
				int attributeID = (int) tempDouble[i][0];
				tempHashMap.put(i,attributeID);
				attributes.addElement(new Attribute((String)instances.attribute(attributeID).name()));
			}
			// add class Attribute
			attributes.addElement(classAttribute);
			Instances filterInstances = new Instances(instanceName, attributes, instances.numInstances());
			filterInstances.setClass(classAttribute);
			// acquire the new Instances
			for(int i = 0; i< instances.numInstances(); i++){
				double []weights = new double[dimensionNum + 1];
				// set new weights of instances
				for(int j = 0; j < weights.length - 1; j++){
					int oldAttributeID = (Integer)tempHashMap.get(j);
					double weight = instances.instance(i).value(oldAttributeID);
					/////////////////////////////////////////////// can be changed to weight
					weights[j] = weight;
					/////////////////////////////////////////////// can be changed to weight
				}
				// set class value of the new instance
				weights[weights.length - 1] = instances.instance(i).classValue();
				Instance instance = new Instance(1, weights);
				filterInstances.add(instance);
			}
			return filterInstances;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*

	 * Purpose: this function is used to output instances into "arff" type file
	 */
	public void writeArff(Instances instances, String arffPath){
		try {
			FileWriter fileWriter = new FileWriter(arffPath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			// write the RELATION name
			bufferedWriter.write("@RELATION " + "model\n");
			// write all the attributes except for the class value
			for(int i = 0; i< instances.numAttributes() - 1; i++)
				bufferedWriter.write( instances.attribute(i) + "\n");
			// write the class attribute
			bufferedWriter.write("@ATTRIBUTE\t");
			bufferedWriter.write(instances.classAttribute().name() + "\t{"+ instances.classAttribute().value(0));
			for(int i = 1; i< instances.classAttribute().numValues(); i++)
				bufferedWriter.write("," + instances.classAttribute().value(i));
			bufferedWriter.write("}\n");
			// write all the instances data
			bufferedWriter.write("@DATA\n");
			for(int i = 0; i< instances.numInstances(); i++){
				bufferedWriter.write("{");
				// write the instances data except for the class value
				for(int j = 0; j< instances.numAttributes() - 1; j++){
					if(instances.instance(i).value(j) != 0){
						bufferedWriter.write(j + " " + instances.instance(i).value(j) + ",");
					}
				}
				// write the class value
				bufferedWriter.write("" + (instances.numAttributes()-1) +" " + instances.classAttribute().value((int)instances.instance(i).classValue()) + "}\n");
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// write instances as default instances
	public boolean writeSamples(Instances samples){
		if(samples == null){
			System.out.println("failed to write the default samples as the samples are null");
			return false;
		}
		try{
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(samplePath));
			objectOutputStream.writeObject(samples);
			objectOutputStream.close();
			return true;
		}catch(Exception e){return false;}
	}
	
	// load default instances
	public Instances loadDefaultSamples(){
		try{
			if(new File(samplePath).exists()){
				ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(samplePath));
				Instances defaultSamples = (Instances) objectInputStream.readObject();
				objectInputStream.close();
				return defaultSamples;
			}
			return null;
		}catch(Exception e){return null;}
	}
	
}
