/* 
 * ThreeSorts.Java - sorts an array of integers using three different methods:
 * insertion sort, merge sort, and quicksort.
 * 
 * Original code for sorting algorithms provided by: Prof. John W. Byers, Boston University
 * 
 * Implemented by: Tim Duffy <timmahd@bu.edu>
 */

import java.util.*;

public class ThreeSorts{
  
  // Sort an array of ints using simple insertion sort.
  public static void insertionSort(int [] a){
    int p = 1;
    while (p < a.length) {
      int tmp = a[p];
      
      int j = p;
      while (j > 0 && a[j-1] < tmp){
        a[j] = a[j-1];
        j--;
      }
      
      a[j] = tmp;
      p++;
    }
  }
  
  // Mergesort elements of the array A between left and right inclusive
  public static void mergesort(int[] A, int left, int right){
    
    if (left < right) { // recurse only if 2 or more elements
      int center = (left + right) / 2;
      mergesort(A, left, center);
      mergesort(A, center + 1, right);
      merge(A, left, center + 1, right);
    }
  }
  
  /* Merge the sorted subarray A[leftPos, ..., rightPos - 1] and
   * the sorted subarray A[rightPos, ..., rightEnd] into a temp
   * array B. Then copy B[0, ...] into A[leftPos ... rightEnd]
   */
  public static void merge (int [] A, int leftPos, int rightPos, int rightEnd){
    int leftEnd = rightPos - 1; // assumes rightPos > leftPos
    int numElements = rightEnd - leftPos + 1;
    int [] B = new int [numElements];
    int bPos = 0;
    
    // While *both* left and right have elements remaining, compare largest of each.
    while (leftPos <= leftEnd && rightPos <= rightEnd) {
      
      // If left element larger, copy it, and adjust ptrs
      if (A[leftPos] > A[rightPos]){
        B[bPos++] = A[leftPos++];
      }
      
      // Else copy right element and adjust
      else{
        B[bPos++] = A[rightPos++];
      }
    }
    
    // Exactly one of left and right now have elements remaining.
    // Copy from whichever one is non-empty.
    // Only one of the two loops below will execute.
    while (leftPos <= leftEnd){
      B[bPos++] = A[leftPos++];
    }
    
    while (rightPos <= rightEnd){ 
      B[bPos++] = A[rightPos++];
    }
    
    // B contains the merged results. Copy them back to A.
    leftPos = rightEnd - numElements + 1;
    for (bPos = 0; bPos < numElements; bPos ++){
      A[leftPos++] = B[bPos];
    }
  }
  
  // Quicksort elements of the array A between a and b inclusive
  public static void quicksort (int[] A, int a, int b){
     if (a < b){ 
      
      int pivot = A[a]; // select first element as the pivot so it is out of the way
      int l = a + 1;
      int r = b;
      
      // Keep pivoting until the l and r indices cross over.
      while (l <= r) {
        
        while (l <= r && A[r] <= pivot){ // slide r left until it points to an
          r--; // element larger than pivot
        }
        
        while (l <= r && A[l] >= pivot){ // slide l right until it points to an
          l++; // element smaller than pivot
        }
        
        if (l < r){ // swap out-of-order pairs 
          swap(A, l, r);
        }
      }
      // Re-position the pivot into its correct slot
      swap (A, a, r);
      
      quicksort (A, a, r-1);
      quicksort (A, r+1, b);
    }
  }
  
  // This helper method swaps two elements in a give0n array
  public static void swap (int[] arr, int x, int y){
    int temp = arr[x];
    arr[x] = arr[y];
    arr[y] = temp;
  }
  
  public static void main(String[] args){
    Scanner console = new Scanner(System.in);
    System.out.print("How many integers would you like to sort?: ");
    
    int n = console.nextInt();
    //Make 3 copies of an array to experiment with
    int[] A = new int[n];
    int[] A2 = new int[n];
    int[] A3 = new int[n];
    
    System.out.print("Would you like to generate a strictly increasing set of integers? (y/n): ");
    String notRand = console.next();
    if(notRand.equalsIgnoreCase("y")){
      A[0] = n;
      A2[0] = n;
      A3[0] = n;
      for(int i = 1; i < n ; i++){
        A[i] = A[i-1] - 1;
        A2[i] = A2[i-1] - 1;
        A3[i] = A3[i-1] - 1;
      }
    }
    else{
      // Create a random number generator
      Random rand = new Random(353124);
      //Create n random integers
      for(int i = 0; i < n; i++){
        A[i] = rand.nextInt(n);
        A2[i] = A[i];
        A3[i] = A[i];
      }
    }
    
    long startInsSort = System.nanoTime();
    insertionSort(A);
    long stopInsSort = System.nanoTime();
    System.out.println("Execution time of insertion sort with " + 
                       n + " integers: " + (stopInsSort - startInsSort) + " nanoseconds.");
    
    long startMrgSort = System.nanoTime();
    mergesort(A2, 0 , A2.length-1 );
    long stopMrgSort = System.nanoTime();
    System.out.println("Execution time of merge sort with " + 
                       n + " integers: " + (stopMrgSort - startMrgSort) + " nanoseconds.");
       
    long startQuikSort = System.nanoTime();
    quicksort(A, 0, A.length-1 );
    long stopQuikSort = System.nanoTime();
    System.out.println("Execution time of quicksort with " + 
                       n + " integers: " + (stopQuikSort - startQuikSort) + " nanoseconds.");
    
  }
}