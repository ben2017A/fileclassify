/**
 * Created by Administrator on 2016/11/14.
 */

import java.util.EnumMap;
import java.util.Map;
import java.util.HashMap;

/*
* Purpose: this class is used to store all the sememe tree types and the
 *  corresponding weights
 */
public class CTreeType {

    // store the weights of the different types
    private Map sememe_type_value;

    private Map sememe_type;

    public CTreeType(){
        setSememeSet();
        setSememeWeights();
    }

    // the type of the sememe tree
    private enum SememeType{
        SIGN(911),
        SECONDARY_FEATURE(876),
        PART_OF_SPEECH(705),
        ATTRIBUTE_VALUE(105),
        ATTRIBUTE(106),
        EVENT_ROLE_AND_FEATURES(354),
        EVENT(1596),
        PROPERNOUN(779),
        ENTITY(1586);
        int sememe_type;
        private SememeType(int value){
            sememe_type = value;
        }
        public int getSememeValue(){
            return sememe_type;
        }
    }

    // set set to store the sememe ID of the tree root
    private void setSememeSet(){
        sememe_type = new HashMap<Integer,SememeType>();
        sememe_type.put(911, SememeType.SIGN);
        sememe_type.put(876,SememeType.SECONDARY_FEATURE);
        sememe_type.put(705, SememeType.PART_OF_SPEECH);
        sememe_type.put(105, SememeType.ATTRIBUTE_VALUE);
        sememe_type.put(106, SememeType.ATTRIBUTE);
        sememe_type.put(354, SememeType.EVENT_ROLE_AND_FEATURES);
        sememe_type.put(1596,SememeType.EVENT);
        sememe_type.put(779, SememeType.PROPERNOUN);
        sememe_type.put(1586, SememeType.ENTITY);
    }


    // set weights to different types of sememe trees
    private void setSememeWeights(){
        sememe_type_value = new EnumMap<SememeType,Double>(SememeType.class);
        sememe_type_value.put(SememeType.ENTITY, 0.7);
        sememe_type_value.put(SememeType.EVENT, 0.5);
        sememe_type_value.put(SememeType.ATTRIBUTE, 0.5);
        sememe_type_value.put(SememeType.ATTRIBUTE_VALUE, 0.5);
        sememe_type_value.put(SememeType.PART_OF_SPEECH, 0.2);
        sememe_type_value.put(SememeType.PROPERNOUN, 1.0);
        sememe_type_value.put(SememeType.SIGN ,0.2);
        sememe_type_value.put(SememeType.SECONDARY_FEATURE, 1.0);
        sememe_type_value.put(SememeType.EVENT_ROLE_AND_FEATURES, 0.2);
    }

    // find the corresponding weights based on the ID of the root sememe
    public double getSememeWeight(int treeID){
        SememeType sememetype = (SememeType)sememe_type.get(treeID);
        if(sememetype == null)
            return 0.0;
        return (Double)sememe_type_value.get(sememetype);
    }
}
