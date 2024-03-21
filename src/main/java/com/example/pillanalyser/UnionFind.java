package com.example.pillanalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnionFind {
    public Map<Integer, Integer> parent;

    public UnionFind() {
        parent = new HashMap<>();
    }

//    public void makeSet() {
//       // group all the root with same color
//    }

  public static void union(int[] a, int p, int q)
    {
        a[find(a,q)]=find(a, p);
    }
    public static int find(int[] a, int id) {
//        if (a[id] != id){
//            a[id] = find(a, a[id]);  // Path compression by halving
//        return a[id];
//        }
        if(a[id]<0 ) return a[id];
        if(a[id] == id) return id;

        else return find(a, a[id]);

    }

    public List<Integer> getAllInSet(int[] arr, int root) {
        List<Integer> set = new ArrayList<>();
        // This code snippet is iterating through an array `arr` and checking if the root of the element at index `i` is
        // equal to the given `root`. If the root matches, it adds the index `i` to the `set` list. This process
        // effectively finds and collects all elements in the same set as the given `root` using the Union-Find algorithm.
        for (int i = 0; i < arr.length; i++) {
            if (find(arr, i) == root) {
                set.add(i);
            }
        }
        return set;
    }

}
