/* 
 * quicksort.java- sorts an array by recursively breaking it down into smaller subarrays
 * 
 * Original code for sorting algarithms provided by: Prof. John W. Byers, Boston University
 * 
 * Implemented by: Tim Duffy <timmahd>
 */


/* Question #1: The worst runni
 * ng time of this quicksort algarithm is O(n^2). 
 * This occurs when the selected pivot is either the largest or smallest element 
 * in the array, dividing the array into 2 arrays of size 1 and n-1.
 * 
 * Example array that causes the worst case:
 * {14,12,13,8,10,9,11,0,6,5,7,1,3,2,4}
 */

/* Question #2: The worst running time of this quicksort algarithm is O(n^2). 
 * This occurs when the selected pivot is either the second largest or second 
 * smallest element in the array, causing the array into split into 2 arrays 
 * of size 2 and n-2.
 * 
 * This does about half the work of the worst case situation in question #1, it is still
 * O(1/2n^2) = ?(n^2).
 * 
 * Example array that causes the worst case:
 * {15,12,14,13,16,11,12,1,10,9,6,4,3,2,0}
 * 
 */

/* Question #3: By using ThreeSorts.java, I was able to determine how long each algarithm took
 * for different sized arrays. I found the base by determining approxamently when insertion 
 * sort became faster than quicksort. Here are the results of my tests:
 * 
 * size    Runtime of          Runtime of 
 *of n     insertion sort      quicksort
 *             (ns)               (ns)
 * 10         12851               36038
 * 100        106718              176000
 * 200        483302              548114
 * 240        612368              1063822  <== // Afer this point, insertion sort
 * 245        602591              558451       // fails to work faster than
 * 250        908774              570464       // quicksort
 * 1,000      3596268             2366223
 */

public class quicksort{ 
  
  // Quicksort elements of the array A between a and b inclusive
  public static void quicksort (int [] A, int a, int b){
    
    int base = b-a;
    
// Insertion sort has proven to be faster with arrays less than 240
    if(base <= 240){
      int[] temp = new int[base];
      
      for(int i=0; i < base;i++){
        temp[i] = A[a+i];
      }
      insertionSort(temp, A, a);
    }
    else{
      
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
  }
  
  // This helper method swaps two elements in a give0n array
  public static void swap (int[] arr, int x, int y){
    int temp = arr[x];
    arr[x] = arr[y];
    arr[y] = temp;
  }
  
  /*
   * Sort an array of ints using simple insertion sort. 
   */
  public static void insertionSort(int [] a, int[] real, int start) 
  {
    int p = 1;
    while (p < a.length) {
      int tmp = a[p];
      
      int j = p;
      while (j > 0 && a[j-1] > tmp) {
        a[j] = a[j-1];
        j--;
      }
      a[j] = tmp;   // I put this statement inside the loop in class
      // Putting it here is also correct, and saves many unnecessary copies.
      
      p++;
    }
    
    // copy back to A
    for(int i = 0; i <= a.length-1; i++){
      real[start+i] = a[i];
    }
  }
}