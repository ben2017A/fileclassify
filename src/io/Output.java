/**
 * Created by Administrator on 2016/11/14.
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/*
Purpose: this class is used to output the corresponding
 *  frequency and sememe to certain files
 */
public enum Output {

    FeatureWordPath("E:\\fileclassify_data\\zengxiaosen\\concept-file\\results\\featureword.txt"),
    ConceptPath("E:\\fileclassify_data\\zengxiaosen\\data_splite\\data_splite\\test_concept\\health.txt"),
    Arrfpath("E:\\fileclassify_data\\zengxiaosen\\concept-file\\results\\arff\\new.arff");
    String path;

    private Output(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

