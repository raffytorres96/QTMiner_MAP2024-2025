package data;

import java.io.Serializable;
import java.util.Set;

public class Tuple implements Serializable {
    private Item[] tuple;

    public Tuple(int size){
        tuple = new Item[size];
    }

    public int getLength(){
        return tuple.length;
    }

    public Item get(int i){
        return tuple[i];
    }

    public void add(Item c, int i){
        tuple[i] = c;
    }

    public double getDistance(Tuple obj){
        double sum= 0.0;
            for (int i = 0; i < this.getLength(); i++) {
                sum += this.get(i).distance(obj.get(i).getValue());
            }
        return sum;
    }

    public double avgDistance(Data data, Set<Tuple> clusteredData) {
    double sumD = 0.0;
        for (Tuple t : clusteredData) {
            sumD += this.getDistance(t);
        }
    return sumD / clusteredData.size();
    }

    // @Override
    // public String toString(){
    //     String str = "";
    //         for (int i = 0; i < tuple.length; i++){
    //             str += tuple[i] + " ";
    //         }
    //     return str;
    // }
}

