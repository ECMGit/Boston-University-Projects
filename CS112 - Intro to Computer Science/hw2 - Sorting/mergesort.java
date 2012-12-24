/* 
 * Mergesort.java- Sorts an array of ints using an iterative version of mergesort.
 * 
 * Code for merge() by: Prof. John W. Byers, Boston University
 * 
 * iterativeMergeSort() and impementation by: Tim Duffy <timmahd>
 */

class Mergesort{
  public static void iterativeMergeSort(int[] A){
    
    int mergesize=0;
    int lastPart;
    
    for(int x=2; x <= A.length; x=x*2){
      mergesize = x;
      
      for(int i = 0; i < A.length - (A.length % mergesize); i+=mergesize){
        merge(A, i,i+(mergesize/2),i+(mergesize-1));
      }
      lastPart = A.length - (A.length % mergesize) - mergesize;
      
      merge(A, lastPart, lastPart + mergesize, A.length-1);
      
    }
  }
  
  //  Merge the sorted subarray A[leftPos, ..., rightPos - 1] and
  //  the sorted subarray A[rightPos, ..., rightEnd] into a temp
  //  array B.  Then copy B[0, ...] into A[leftPos ... rightEnd]
  public static void merge(int [] A,  int leftPos, int rightPos, int rightEnd){
    int leftEnd = rightPos - 1;   // assumes rightPos > leftPos
    int numElements = rightEnd - leftPos + 1;
    int [] B = new int [numElements];
    int bPos = 0;
    
    // While *both* left and right have elements remaining, compare smallest of each.
    while(leftPos <= leftEnd && rightPos <= rightEnd){
      
      // If left element smaller, copy it, and adjust ptrs
      if(A[leftPos] <= A[rightPos]){
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
    while(rightPos <= rightEnd){
      B[bPos++] = A[rightPos++];
    }
    
    // B contains the merged results.  Copy them back to A.
    leftPos = rightEnd - numElements + 1;
    for(bPos = 0; bPos < numElements; bPos ++){
      A[leftPos++] = B[bPos];
    }
  }
}