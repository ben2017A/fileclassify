/**
 * Created by Administrator on 2016/11/14.
 */
/*
Purpose: this enum is used to define all the paths for all the input-source paths
 */
public enum Input {

    input_path("E:\\fileclassify_data\\Reduced"),
    //fudan test1
    train_dirPath("E:\\fileclassify_data\\zengxiaosen\\data_splite\\data_splite\\train_cancept"),
    model_path("E:\\fileclassify_data\\zengxiaosen\\concept-file\\results\\model\\classifier.model"),
    sample_path("E:\\fileclassify_data\\zengxiaosen\\concept-file\\results\\model\\classifier.sample");
    String path;
    private Input(String path){
        this.path = path;
    }
    public String getPath(){
        return this.path;
    }
}
