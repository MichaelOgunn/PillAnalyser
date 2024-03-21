//package com.example.pillanalyser;
//
//import java.util.concurrent.TimeUnit;
//
//@Measurement(iterations=10)
//@Warmup(iterations=5)
//@Fork(value=1)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MICROSECONDS)
//@State(Scope.Thread)
//public class BenchMark {
//
//    private UnionFind uf;
//    private int[] arr;
//
//    public static void main(String[] args) {
//        BenchMark bm = new BenchMark();
//        bm.unionFindBenchMark();
//        bm.Find();
//    }
//    @Setup
//    public void setup() {
//        uf = new UnionFind();
//        int size = 100;
//        arr = new int[size];
//        for (int i = 0; i < size; i++) {
//            arr[i] = i;
//            uf.union(arr, i, i);
//        }
//
//
//    }
//    @Benchmark
//    public void unionFindBenchMark() {
//        for (int i = 0; i < arr.length /2 ; i++) {
//            uf.union(arr, 0, i);
//        }
//    }
//    @Benchmark
//    public void Find() {
//        for (int ar : arr){
//            uf.find(arr, ar);
//        }
//    }
//}
