/**
 * Created by Administrator on 2016/11/14.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
public class SHeap {
    // N is the size of the heap
    private int N;
    /*
     * define the structure priority queue(heap), in another word to store the
     * N words and corresponding frequencies
     */
    private PriorityQueue pQueue;
    // define the class to store the feature word and corresponding frequencies
    private class word_weight{
        private String word;
        private double weight;
        public word_weight(String word, double weight){
            this.word = word;
            this.weight = weight;
        }
    }
    // implement the interface Comparator to define the PriorityQueue
    private class DComparator implements Comparator<word_weight>{
        public int compare(word_weight wf1, word_weight wf2){
            if(wf1.weight > wf2.weight)
                return 1;
            if(wf2.weight > wf1.weight)
                return -1;
            return 0;
        }
    }
    // initialize the heap
    public SHeap(int number){
        N = number;
        pQueue = new PriorityQueue(N,new DComparator());
    }
    // to get the top N items based on the queue
    public Map getHeapMap(Map map){
        // if the N is greater than the size of map, return it directly
        if(map.size() <= N)
            return map;
        // choose the top N items and return
        Iterator iterator = map.keySet().iterator();
        while(iterator.hasNext()){
            String word = (String)iterator.next();
            pQueue.add(new word_weight(word,(Double)map.get(word)));
        }
        while(pQueue.size() > N)
            pQueue.remove();
        Iterator qIterator = pQueue.iterator();
        Map mapHeap = new HashMap<String, Double>();
        while(qIterator.hasNext()){
            word_weight wf = (word_weight)qIterator.next();
            mapHeap.put(wf.word, wf.weight);
        }
        return mapHeap;
    }
}
