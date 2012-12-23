/*
 * WordList.java - Uses LinkedList implementation to store nodes that 
 * contain entries with similar alphagrams
 * 
 * Starter code by: Ashwin Thangali  cs112, Boston University
 * 
 * Implemented by: Tim Duffy <timmahd@bu.edu>
 */

import java.util.*;

class WordListNode {
    Entry nodeEntry;
    WordListNode nextNode;
    
    WordListNode(Entry newEntry, WordListNode newNext){
        nodeEntry = newEntry;
        nextNode = newNext;
    }
    Entry getEntry(){ return nodeEntry; }
    WordListNode getNext(){ return nextNode; }
    void setNext(WordListNode x){ nextNode = x; }
}
 
class WordListIterator implements Iterator {
    WordListNode currentWordListNode;
    
    WordListIterator( WordListNode wordListHead ) {
        currentWordListNode = wordListHead;
    }
    public boolean hasNext() {
        return (currentWordListNode != null);
    }
    public WordListNode next() {
        WordListNode currentNode = currentWordListNode;
        currentWordListNode = currentWordListNode.getNext();
        return currentNode;
    }
    public void remove() {}
}
 
class WordList implements Iterable {
    WordListNode wordListHead;
    
    WordList() { wordListHead = null; }
    void addToHead( Entry newEntry ) {
        wordListHead = new WordListNode( newEntry, wordListHead ); 
    }
    public WordListNode getListHead(){ return wordListHead; }
    public Iterator iterator() {
        return new WordListIterator( wordListHead );
    }
}