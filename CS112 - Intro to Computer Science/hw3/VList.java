/* VList.java: A linked list of Variable nodes
 * 
 * code by Tim Duffy <timmahd@bu.edu>
 */

import java.util.Iterator;

// Singly linked list class 
public class VList implements Iterable{
  private Node headNode;
  
  // Constructor
  public VList(){
    headNode = null;
  }
  
  // Get an iterator for this list
  public ListIterator iterator(){
    return new ListIterator( headNode );
  }
  
  // The ListIterator class below implements an iterator for the VList class 
  static class ListIterator implements Iterator {
    
    // Keeps track of the iterator's position in the list
    private Node currentNode;
    
    // The iterator starts off at head of the linked list
    public ListIterator( Node headNode ){
      currentNode = headNode;
    }
    // Are there more nodes ahead of us?
    public boolean hasNext(){
      return ( currentNode != null ); 
    }
    // Output current "node data" and walk to the next node
    public Variable next(){
      Variable currentData = currentNode.getData();
      currentNode = currentNode.getNextNode();
      return currentData;
    }
    // Output current "node" and walk to the next node
    public Node nextNode(){
      Node tempNode = currentNode;
      currentNode = currentNode.getNextNode();
      return tempNode;
    }
    // remove not implemented here but implemented in the 
    // LinkedList class above
    public void remove(){
    }
    
  }
  
  // Add a new node to tail of the list
  public void addToTail( Variable newNodeData ){
    // Locate the last node in the list so that we can 
    // add newNode after it.
    Node lastNode = null;
    
    // Use the listIterator to walk through the list to find last node
    ListIterator listIterator = this.iterator();
    while(listIterator.hasNext()){
      lastNode = listIterator.nextNode();
    }
    // Create a new node with newNodeData and set it as next node for lastNode
    // *** check whether lastNode is null and do appropriately 
    Node newNode = new Node(newNodeData, null);
    if( lastNode != null ){
      lastNode.setNextNode(newNode);
    }
    else{
      headNode = newNode;
    }
  }
  
  // Returns value of s apecified variable
  public int findValue(char ch){
    // Use the list-iterator to walk down the list
    ListIterator listIterator = this.iterator();
    while(listIterator.hasNext()){
      Variable temp = listIterator.next();
      if(temp.getName() == ch){
        return temp.getValue();
      }
    }
    throw new undefinedVariableException();
  }
  
  //inherits all methods and member variables
  public class undefinedVariableException extends RuntimeException {  
    public undefinedVariableException(){
      super("Variable is undefined"); //Calls constructor from RuntimeException
    }
  }
}

// The Node class 
class Node{
  
  private Variable nodeData;
  private Node nextNode;
  
  // Constructor
  public Node( Variable newData, Node newNextNode ){
    nodeData = newData; //
    nextNode = newNextNode;
  }
  
  // Accessor methods
  public Variable getData(){
    return nodeData;
  }
  public Node getNextNode(){
    return nextNode;
  }
  public void setData(Variable newData){
    nodeData = newData;
  }
  public void setNextNode( Node newNextNode ){
    nextNode = newNextNode;
  }
}