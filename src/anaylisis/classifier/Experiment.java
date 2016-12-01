package classifier;
/*
 * Purpose: this class is used to classify the passages using the model
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import classifier.Model;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.J48graft;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import wlsvm.WLSVM;


public class Experiment {
	
	// define a Model class, used to read all the instances 
	private Model model;
	
	// constructor
	public Experiment(String modelPath, String dirPath, String samplePath, String instanceName){
		model = new Model(modelPath, dirPath, samplePath, instanceName);
	}
	
	
	public Instances getSamples(int dimensionNum){
		return model.getSamples(dimensionNum);
	}
	
	public void writeInstances(Instances instances,String samplePath){
		model.writeArff(instances,samplePath);
	}
	
	public void freeModel(){
		this.model.freeModel();
	}
	
	/*

	 * Purpose: this function is used to output the training results: recall and precision
	 */
	public void output(ArrayList arrayList, Instances instances) throws Exception{
		int classNum = instances.numClasses();
		int [][]classType = new int[classNum][classNum];
		for(int i = 0; i< classNum; i++)
			for(int j = 0; j<classNum; j++)
				classType[i][j] = 0;
		Iterator iteratorArrayList = arrayList.iterator();
		while(iteratorArrayList.hasNext()){
			String resultString =  (String)iteratorArrayList.next();
			String []resultArray = resultString.split("\t");
			int initial = Integer.parseInt(resultArray[0]);
			int result = Integer.parseInt(resultArray[1]);
			classType[initial][result] ++;
		}
		int []recall = new int[classNum];
		int []precise = new int[classNum];
		for(int i = 0; i<classNum ; i++){
			for(int j = 0; j< classNum; j++){
				//------------------------------ouput the classType[][] respectively
				System.out.print(classType[i][j] + "\t");
				precise[i] += classType[i][j];
				recall[j] += classType[i][j];
			}
			//----------------------------------output class information
			System.out.print(instances.classAttribute().value(i) + "\t");
			System.out.println();
		}
		/*
		 * ------------------------------------- for simple debug, output the average value
		 */
		double aPrecise = 0.0;
		double eachPrecise = 0.0;
		double aRecall = 0.0;
		for(int i = 0;i<classNum;i++){

			aPrecise += (double)classType[i][i]/(double)precise[i];
			aRecall += (double)classType[i][i]/(double)recall[i];
			System.out.print("" + (double)classType[i][i]/(double)precise[i] + "\t");
			System.out.println("" + (double)classType[i][i]/(double)recall[i]);
		}



		aPrecise = aPrecise/(double)classNum;
		aRecall = aRecall/(double)classNum;
		//-------------------------------------------------------------------------------------
		System.out.println("the average precision is: " + aPrecise);
		System.out.println("the average recall is: " + aRecall);





	}
	
	/*

	 * Purpose: this function is used to get new instances through new instances map
	 */
	public Instances getNewInstances(String dirPath, Instances sampleInstances){
		Map instancesMap = readNewInstancesData(dirPath, sampleInstances);
		
		// construct new instances based on sampleInstances, but I`ve no idea if this code works or not??????????
		Instances instances = new Instances(sampleInstances,0);
		
		Iterator iteratorInstances = instancesMap.keySet().iterator();
		
		while(iteratorInstances.hasNext()){
			String classValue = (String)iteratorInstances.next();
			ArrayList tempArray = (ArrayList) instancesMap.get(classValue);
			Iterator iteratorArray = tempArray.iterator();
			while(iteratorArray.hasNext()){
				SortedMap sortedMap = (SortedMap)iteratorArray.next();
				Iterator iteratorMap = sortedMap.keySet().iterator();
				// construct a double array to store the weights and 
				// for the initializing of one certain instance
				double []weights = new double[sampleInstances.numAttributes()];
				for(int i = 0; i<weights.length; i++)
					weights[i] = 0.0;
				while(iteratorMap.hasNext()){
					int id = (Integer)iteratorMap.next();
					double weight = (Double)sortedMap.get(id);
					weights[id] = weight;
				}
				weights[weights.length - 1] = sampleInstances.classAttribute().indexOfValue(classValue);
				Instance instance = new Instance(sampleInstances.numAttributes(), weights);
				instances.add(instance);
			}
		}
		System.out.println("construct new instances successfully");
		return instances;
	}
	
	/*

	 * Purpose: this function is used to get the map to construct New Instances, which can be analyzed 
	 * using the classifier trained by samples
	 */
	private Map readNewInstancesData(String dirPath, Instances sampleInstances){
		// if sample instances does not exist, return directly
		if(sampleInstances == null || sampleInstances.numAttributes() == 0)
			return null;
		File dir = new File(dirPath);
		Map key = new HashMap<String, Integer>();
		// set s to be no more than numAttributes - 1, as the last attribute is the classAttribute
		for(int i = 0; i < sampleInstances.numAttributes() - 1; i ++){
			key.put(sampleInstances.attribute(i).name(), i);
		}
		// store all the instances in a file and corresponding values
		Map instancesMap = new HashMap<String, ArrayList>();
		for(File file : dir.listFiles()){
			String []fileValue = file.getName().split("\\.");
			String classValue = fileValue[0];	
			//-------------------------------------------------
			System.out.println("read file: " + file.getName());
			instancesMap.put(classValue, readFile(file,key));
		}
		return instancesMap;
	}
	
	/*

	 * Purpose: this function is used to read contents from a non-sample file,
	 *  store and update the values based on a Map-type "key", which stores
	 *  the words and corresponding Id
	 */
	private ArrayList readFile(File file, Map key){
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
					else
						continue;
				}
				tempArray.add(sortedMap);
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
	 * Purpose: this function is used to test the Model instances, to test whether the classifier can be used
	 * 	and whether the classifying job can be done correctly
	 */
	public void testModel() throws Exception{
		Instances instances = model.getSamples(-1);
		Classifier classifier = trainClassifier(instances);
		ArrayList arrayList = (ArrayList)classifyInstances(instances,classifier);
		output(arrayList, instances);
	}

	
	/*
	 * Purpose: this function is used to get classifier based on the instances
	 */
	public Classifier trainClassifier(Instances instances){
		try{
			/*
			 * ********************************************************************
			 */
			//Classifier classifier = new J48graft();
			//Classifier classifier = new NaiveBayes();
			Classifier classifier = new NaiveBayesMultinomial();
			//Classifier classifier = new WLSVM();
			///////////////////////////////////////
			// to be changed
			//WLSVM wlsvm = new WLSVM();
			//wlsvm.setSVMType(svm_type)
			/*
			 * ********************************************************************
			 */
			classifier.buildClassifier(instances);
			System.out.println("train classifier successfully");
			return classifier;
		}catch(Exception e){
			System.out.println(e.toString());
			return null;}
	}
	
	/*

	 * Purpose: this function is used to classify the instances and output them in a Map data structure
	 *  
	 *  The type of the ArrayList structure is as follows: String: "initial '\t' result '\t' i". 
	 *  the value initial corresponds to the class value the instance is initially set to, value result
	 *  corresponds to the class value the instances is finally classified to, i is the ID.
	 */
	public ArrayList classifyInstances(Instances instances, Classifier classifier) throws Exception{
		/* 
		 * String corresponds to the name of a class, Set corresponds to the contents
		 * of each instance that contains the class it initially belongs to and classified to afterwards
		 */
		ArrayList arrayList = new ArrayList<String>();
		if(classifier == null){
			System.out.println("failed to find the concrete classifier");
			return null;
		}
		for(int i = 0; i<instances.numInstances(); i ++){
			Instance instance = instances.instance(i);
			int result = (int)classfyInstance(instance, classifier);
			int initial = (int)instance.classValue();
			arrayList.add(new String("" + initial + "\t" + result + "\t" + i));
		}
		System.out.println("classify instances successfully");
		return arrayList;
		//return resultMap;
	}
	// classifier a certain Instance
	private double classfyInstance(Instance instance, Classifier classifier) throws Exception{
		double predicted = classifier.classifyInstance(instance);
		return predicted;
	}


}
