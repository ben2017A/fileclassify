/**
 * Created by Administrator on 2016/11/15.
 */

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;


import classifier.Experiment;
import weka.classifiers.Classifier;
import weka.core.Instances;




public class Main {
    static Input input;
    public Output output;
    static Experiment experiment;
    Section section;
    CConcept concept;

    public void read() throws ClassNotFoundException, UnsupportedEncodingException, SQLException {
        int N = 20;
        String filename = "health";
        try {
            section = new Section("E:\\fileclassify_data\\zengxiaosen\\concept-file\\source\\stopwords.txt");
            concept = new CConcept();

            //分词，去停用词，统计词频
            section.indexDocs(new File("E:\\fileclassify_data\\zengxiaosen\\data_splite\\data_splite\\test1\\" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //计算TF-IDF权值，N为所期望的特征词的个数
        System.out.println("IDF");
        section.calPTF_IDF(N);
        //根据词频和TF-IDF权值计算义原向量
        System.out.println("Feature");
        concept.calFeatureSpace(section.getFrequencyMap(), section.getWeightMap(), section.getMaxFrequency(), section.getMaxWeight());
        //输出义原向量和TF-IDF权值向量
        System.out.println("output");
        concept.outputConcept(concept.getConceptMap());
        section.outputFrequency(section.getWeightMap(), "E:\\fileclassify_data\\zengxiaosen\\data_splite\\data_splite\\test_frequency\\" + filename + ".txt");
        //释放空间
        concept.freeConceptMap();
        section.freeSection();
    }

    public void test(){
        int attributeNum[] = {200,300,500,1000,-1};
        int tempNum = 4;
        System.out.println(attributeNum[tempNum]);
        experiment = new Experiment(input.model_path.getPath(), input.train_dirPath.getPath(), input.sample_path.getPath(), "instances");
        Instances samples = experiment.getSamples(attributeNum[tempNum]);
        //experiment.test(input.train_dirPath.getPath());
        long time1 = System.currentTimeMillis();
        Classifier classifier = experiment.trainClassifier(samples);

        Instances instances = experiment.getNewInstances("E:\\fileclassify_data\\zengxiaosen\\data_splite\\data_splite\\test_cancept", samples);
        long time2 = System.currentTimeMillis();
        //experiment.writeInstances(samples, "E:/MyWork/Work-2013-2014/concept/results/arff/samples.arff");
        //experiment.writeInstances(instances, "E:/MyWork/Work-2013-2014/concept/results/arff/instances.arff");
        System.out.println(time2 - time1);
        try {
            experiment.output(experiment.classifyInstances(instances, classifier),instances);
            long time3 = System.currentTimeMillis();
            System.out.println(time3 - time2);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String []args) throws ClassNotFoundException, UnsupportedEncodingException, SQLException{
        Main main = new Main();
        //main.read();
        main.test();
    }

}
