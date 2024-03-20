package com.example.pillanalyser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {

    @Test
    void union() {
        int[] arr = new int[10]; // Example array for testing
        UnionFind.union(arr, 1, 2); // Union two elements
        assertEquals(UnionFind.find(arr, 1), UnionFind.find(arr, 2)); // Check if they belong to the same set
    }

    @Test
    void find() {
        int[] arr = new int[10]; // Example array for testing
        arr[2] = 2; // Set index 2 as its own root
        assertEquals(2, UnionFind.find(arr, 2)); // Check if find returns the correct root
    }

    @Test
    void getAllInSet() {
        UnionFind unionFind = new UnionFind(); // Create an instance of UnionFind
        int[] arr = new int[10]; // Example array for testing
        arr[1] = 1; // Set index 1 as its own root
        arr[2] = 1; // Set index 2 to belong to the same set as index 1
        arr[3] = 3; // Set index 3 as its own root (different set)
        List<Integer> set = unionFind.getAllInSet(arr, 1); // Get all elements in the set with root at index 1
        assertTrue(set.contains(1)); // Check if index 1 is in the set
        assertTrue(set.contains(2)); // Check if index 2 is in the set
        assertEquals(2, set.size()); // Check if the size of the set is correct
    }
}