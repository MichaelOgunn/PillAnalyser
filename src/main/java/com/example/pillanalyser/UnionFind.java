package com.example.pillanalyser;

import java.util.HashMap;
import java.util.Map;

public class UnionFind {
    public Map<String, String> parent;

    public UnionFind() {
        parent = new HashMap<>();
    }

    public void makeSet(String position) {
        if (!parent.containsKey(position)) {
            parent.put(position, position);
        }
    }

  public static void union(int[] a, int p, int q)
    {
        a[find(a,q)]=find(a, p);


    }
    public static int find(int[] a, int id) {
        if(a[id]<0 ) return a[id];
        if(a[id] == id) return id;
        else return find(a, a[id]);

    }

}
