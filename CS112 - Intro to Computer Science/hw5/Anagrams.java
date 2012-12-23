/*
 * Anagrams.java - Computes all legal anagrams (words in the scrabble dicionary)
 * of a word given by user
 * 
 * -uses hashing to store all words and their alphagrams 
 * 
 * Code by: Tim Duffy <timmahd@bu.edu>
 */

import java.io.FileInputStream;
import java.util.*;

class Anagrams{
  public static void main(String[] args) throws Exception {
    String choice = "y";
    
    // Create WordList array
    WordList[] hashTable = new WordList[200000];
    
    // Initalize hashTable to empty WordLists
    for( int j = 0; j < hashTable.length; j++ ){
      hashTable[j] = new WordList();
    }
    
    // load dictionary into the array 'WordList'
    Scanner scan = new Scanner(new FileInputStream("TWL.txt"));
    
    // Insert entry into hash table
    HashTable bucketList = new HashTable();
      
    while(scan.hasNext()){
            
      String curString = scan.next();
      String curKey = HashTable.getHashKey(curString); //get the alphagram
      
      // Create a new entry for this word
      Entry newEntry = new Entry(curKey, curString);
      
      bucketList.insert(newEntry);
    }
    
    while(choice.equalsIgnoreCase("y")){
      // Ask user for input
      Scanner console = new Scanner(System.in);
      System.out.print("Enter a word to be anagramized: ");
      String querey = console.next();
      
      String targetAlphagram = HashTable.getHashKey(querey); // get alphagram of target word
      int targetCode = HashTable.hashFunction(targetAlphagram); // Get hash code for correct bucket
      
      // print out words in that bucket
      WordList list = bucketList.wordList[targetCode];
      WordListIterator listIterator = new WordListIterator( list.getListHead());
      
      while(listIterator.hasNext()){
        Entry nodeEntry = listIterator.next().getEntry();
        String nodeKey = nodeEntry.getKey(); // key of this entry
        
        // error checking: all words in the list have the same hashCode
        // but might not have the same alphagram
        if(nodeKey.equalsIgnoreCase(targetAlphagram)){ // if they have the same alphagram
          System.out.println(nodeEntry.getValue());
        }
      }
      System.out.println();
      System.out.println("Run again? (y/n)");
      choice = console.next();
    }
  }
}
