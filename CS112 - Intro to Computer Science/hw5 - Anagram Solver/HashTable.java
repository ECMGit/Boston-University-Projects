/*
 * HashTable.java - defines a hashtable, which stores WordLists in a bucket array
 * 
 * Starter code by: Ashwin Thangali  cs112, Boston University
 * 
 * Implemented by: Tim Duffy <timmahd@bu.edu>
 */

import java.util.*;
import java.io.IOException;

class HashTable{
  WordList[] wordList = new WordList[ 150000 ];
  
  HashTable(){
       
    // initalize to blank lists
    for( int i = 0; i < wordList.length; i++ ){
      wordList[i] = new WordList();
    }
  }
  
  // Find the hashKey(alphagram) for each word in the list
  public static String getHashKey(String str) {
    char[] chars = new char[str.length()];
    str = str.toLowerCase();
    
    // put word into an array
    for(int i = 0; i < str.length(); i++){
      chars[i]= str.charAt(i);
    }
    
    //sort the array
    Arrays.sort(chars);
    
    //put sorted array into a String
    String alphagram  = "";
    for(int i = 0; i < str.length(); i++){
      alphagram += chars[i];
    }
    return alphagram;
  }
  
  // Insert Entry into correct bucket
  public void insert( Entry x ){
    int code = hashFunction(x.getKey());
    WordList list = this.wordList[ code ]; // get word list
    WordListNode lastNode = null;
    
    // add to wordList
    WordListIterator listIterator = new WordListIterator( list.getListHead()); 
    //find last node in the list
    while(listIterator.hasNext()){
      lastNode = listIterator.next();
    }
    
    WordListNode newNode = new WordListNode(x, null);
    if( lastNode != null ){
      lastNode.setNext(newNode);
    }
    else{
      list.addToHead(x);
    }
  }
  
  // generate the hashCode used to determine correct bucket for insertion
  public static int hashFunction(String key) {
    int code = key.hashCode();
    if(code < 0){ return (-1 * code) % 150000; }
    else{ return code % 150000; }
  }
}