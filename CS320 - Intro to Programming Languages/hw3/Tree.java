/*
 * Tree.java - defines a binary search tree and binary search tree nodes.
 * code by: Tim Duffy <timmahd@bu.edu
 */

import java.util.*;

// Defines a Binary Tree Node
class BTNode {
    int nodeValue;
    BTNode parent, leftChild, rightChild;
    
    // Constructs a BTNode
    BTNode( int nodeValue){
	this.nodeValue = nodeValue;
       	parent = null;
	leftChild = null;
	rightChild = null;
    }
    
    void setLeftChild( BTNode leftChild ){ 
	this.leftChild = leftChild;
	if( leftChild != null ) leftChild.parent = this;
    }
    
    void setRightChild( BTNode rightChild ){
	this.rightChild = rightChild; 
	if( rightChild != null ) rightChild.parent = this;
    }
        
    int getNodeValue(){ return nodeValue; }
    BTNode getParent(){ return parent; }
    BTNode getLeftChild(){ return leftChild; }
    BTNode getRightChild(){ return rightChild; }
}

class BT {
    BTNode rootNode;
    int i = 0;
    BT(){ rootNode = null; }
    
    // Checks if values of two nodes are equal
    Boolean isEqual(BTNode a, BTNode b){
	if (a.getNodeValue() == b.getNodeValue()) { return true;}
	return false;
    }

    // leafCount returns the number of leaves on a tree
    int leafCount(){ return leafCount(this.rootNode); }
    int leafCount (BTNode x){
	if (x == null) { return 1; } // If node is a leaf, return 1
	else{ return (leafCount(x.leftChild) + leafCount(x.rightChild)); }
    }
    
    // nodeCount returns the number of nodes in a tree
    int nodeCount(){ return nodeCount(this.rootNode); }
    int nodeCount (BTNode x){
	if (x == null) { return 0; }
	else{
	    return (1+ nodeCount(x.leftChild) + nodeCount(x.rightChild));
	}
    }
	
    int height(){
	return height(this.rootNode);
    }
    
    int height(BTNode x){
	
	if (x == null) { return 0; } // Height of a leaf is zero
	else{ 
	    return (1 + max(height(x.leftChild), height(x.rightChild)));
	}
    }

    int max(int a, int b){
	if (a >= b){ return a;}
	else { return b;}
    }
    
    // Checks if all nodes in the tree have the same depth
    Boolean isPerfect(){
	if ( this.nodeCount() == (Math.pow(2, this.height()) -1)){
	    return true;
	}
	
	return false;
    }
    
    // Checks if all nodes are arranged in a line
    Boolean isDegenerate(){
	if (this.nodeCount() == (this.height())){ return true;}
	    
	return false;
    }

    // Returns the tree in list form, using in order traversal
    String list(){
	String x = "[";
	
	if (!(this.isDegenerate())){ return "";}
	else {
	    
	    InOrderIterator inOrder = new InOrderIterator(rootNode);
	    while( inOrder.hasNext() ){
		
		x += inOrder.next().getNodeValue();
		// Add comma only if there is another element in the list
		if (inOrder.hasNext()){ x += ", "; }
	    }
	}
	x += "]";
	return x;
    }

}
// StackElement has two fields: BTreeNode and a boolean visited,
// and will be used in the Inorder iterator.
class StackElement {
    BTNode btreeNode;
    boolean visited;
    StackElement( BTNode btreeNode, boolean visited ){
	this.btreeNode = btreeNode;
	this.visited = visited;
    }
    BTNode getNode(){ return btreeNode; }
    boolean isVisited(){ return visited; }
}

class InOrderIterator implements Iterator<BTNode>{
    Stack<StackElement> iteratorStack;
    
    InOrderIterator(BTNode root){
	iteratorStack = new Stack<StackElement>();
	iteratorStack.push(new StackElement(root, false));
    }
    public boolean hasNext() {        
	return !iteratorStack.isEmpty();
    }
    public BTNode next(){
	
	/// Repeat until we have a visited node on top of the stack.
	while( !iteratorStack.peek().isVisited() ){
	    
	    // Get current node from top of the stack.
	    BTNode curNode = iteratorStack.pop().getNode(); 
	    
	    // If right child is present, push it on the stack as not-visited.
	    if( curNode.getRightChild() != null ){
		iteratorStack.push( new StackElement(curNode.getRightChild(), false) );
	    }
	    
	    // Push current node on the stack as visited.
	    iteratorStack.push(new StackElement(curNode, true));
	    
	    // If left child is present, push it on the stack as not-visited.
	    if( curNode.getLeftChild() != null ){
		iteratorStack.push(new StackElement(curNode.getLeftChild(), false));
	    }
	}
	
	// Pop the node on top of the stack (this node is a visited node) 
	// and return its data.
	StackElement x = iteratorStack.pop();
	return x.getNode();
    }
    public void remove(){}
}