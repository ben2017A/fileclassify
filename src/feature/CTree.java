import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import HowNet.Function;
import java.util.Map;
import java.util.EnumMap;

/*
 * Purpose: this class is to build the tree of sememes
 */
public class CTree {
    private int SEMEME_NUM = 2600;
    private Set temp_set = new HashSet<Integer>();
    private CTreeNode []cTreeNode = new CTreeNode[2600];
    private CTreeType cTreeType = new CTreeType();
    private Hownet ha = null;
    public Hownet getHownet(){
        return this.ha;
    }
    CTree(Hownet ha) throws UnsupportedEncodingException {
        this.ha = ha;
        //read the sememe and HYP by sequence
        System.out.println("……………………………… "+"sememe tree initialized, " + 2600 + " sememes stores");
        for(int i=0;i<SEMEME_NUM;i++){
            //String name = function.HowNet_Get_Sememe_String(i);
            String name = ha.getHownetAPI().Get_Sememe_String(i,ha.getHownetAPI().getConnnection());
            //int parent = function.HowNet_Get_Sememe_Hyp(i);
            int parent = ha.getHownetAPI().Get_Sememe_Hyp(i,ha.getHownetAPI().getConnnection());
            cTreeNode[i] = new CTreeNode(name, i,parent,-1,-1,-1);
        }
        //store the parent of each sememe
        for(int i=0;i<SEMEME_NUM;i++){
            int parent = cTreeNode[i].parent;
            if(parent < 0){
                continue;
            }

            cTreeNode[parent].addToTree_set(i);
        }
        //store the number of LYPs, the Level, and the TreeID of each sememe
        for(int i=0;i<SEMEME_NUM;i++){
            cTreeNode[i].number_of_LYP = calLYPNum(i);
            Iterator iterator = cTreeNode[i].tree_set.iterator();
            int level = 0;
            int tempID = i;
            while(true){
                if(cTreeNode[tempID].parent < 0){
                    break;
                }
                tempID = cTreeNode[tempID].parent;
                level++;
            }
            cTreeNode[i].level = level;
            cTreeNode[i].treeID = tempID;
        }
    }

    //to calculate the number of LYP in a recursive way
    private int calLYPNum(int sememeID){
        int num = 0;
        Iterator iterator= cTreeNode[sememeID].tree_set.iterator();
        while(iterator.hasNext()){
            num ++;
            int nextID = (Integer)iterator.next();
            num += calLYPNum(nextID);
        }
        return num;
    }

    int getID(int sememeID){
        return cTreeNode[sememeID].ID;
    }

    int getParent(int sememeID){
        return cTreeNode[sememeID].parent;
    }

    int getLYPNum(int sememeID){
        return cTreeNode[sememeID].number_of_LYP;
    }

    int getTreeID(int sememeID){
        return cTreeNode[sememeID].treeID;
    }

    double getTreeTypeWeight(int sememeID){
        int treeID = cTreeNode[sememeID].treeID;
        double treeweight = cTreeType.getSememeWeight(treeID);
        return treeweight;
    }

    int getLevel(int sememeID){
        return cTreeNode[sememeID].level;
    }
    /*

     * Purpose: this class is used to store the attributes of a certain sememe
     */
    private class CTreeNode {
        String name;
        int treeID;
        int ID;
        int parent;
        int number_of_LYP;
        int level;
        Set tree_set;//the tree_set is used to store the direct childs that the current node contains
        CTreeNode(String name, int ID, int parent, int number_of_LYP, int level, int treeID){
            this.name = name;
            this.ID = ID;
            this.parent = parent;
            this.number_of_LYP = number_of_LYP;
            this.level = level;
            this.treeID = treeID;
            this.tree_set = new HashSet<Integer>();
        }

        int addToTree_set(int sememeID){
            if(tree_set.contains(sememeID)){
                return tree_set.size();
            }
            tree_set.add(sememeID);
            return tree_set.size();
        }
    }


}

