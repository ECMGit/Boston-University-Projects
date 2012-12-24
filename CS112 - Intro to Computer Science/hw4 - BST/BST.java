import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.*;

// Defines a Binary Tree Node
class BSTNode {
  int nodeValue;
  int nodeDepth;
  BSTNode parent, leftChild, rightChild;
  
  // Used to store the in-order sequence number of this node
  int inOrderSequence;
  
  BSTNode( int nodeValue, int nodeDepth ){
    this.nodeValue = nodeValue;
    this.nodeDepth = nodeDepth;
    parent = null;
    leftChild = null;
    rightChild = null;
  }
  
  void setLeftChild( BSTNode leftChild ){ 
    this.leftChild = leftChild;
    if( leftChild != null ) leftChild.parent = this;
  }
  void setRightChild( BSTNode rightChild ){
    this.rightChild = rightChild; 
    if( rightChild != null ) rightChild.parent = this;
  }
  void setInOrderSequence(int inOrderSequence){ 
    this.inOrderSequence = inOrderSequence; 
  }
  int getNodeValue(){ return nodeValue; }
  int getNodeDepth(){ return nodeDepth; }
  int getInOrderSequence(){ return inOrderSequence; }
  BSTNode getParent(){ return parent; }
  BSTNode getLeftChild(){ return leftChild; }
  BSTNode getRightChild(){ return rightChild; }
  boolean isExternal() { return (leftChild == null && rightChild==null);}
}

// Defines a Binary Search Tree
class BST {
  BSTNode rootNode;
  int i = 0;
  int inOrderCounter; // will be useful to assign inOrderSequence numbers
  int [] dataArray = new int[20]; // stores the bulk insert data array
  BST(){ rootNode = null; }
    
  // bulkInsert breaks apart a JSON string and stores the integer values in dataArray
  void bulkInsert(String JSON_str){
    String numsOnly = JSON_str.substring(1, JSON_str.length()-1);
    StringTokenizer st = new StringTokenizer(numsOnly, ", ");
    
    i=0;
    while(st.hasMoreTokens()){
      dataArray[i] = Integer.parseInt(st.nextToken());
      i++;
    }
    dataArray = removeDuplicates(dataArray);
    
    i = 0;
    while(dataArray[i] != 0){
      boolean added = false;
      BSTNode subRoot = rootNode;
      
      // store first element as root
      if(rootNode == null){
        rootNode = new BSTNode(dataArray[i], 0);
      }
      else{
        while(!added){ // loop until we break out
          
          BSTNode x = new BSTNode(dataArray[i], subRoot.getNodeDepth() + 1);
          
          if(dataArray[i] < subRoot.getNodeValue()){
            if(subRoot.leftChild == null){
              subRoot.setLeftChild(x);
              added = true;
            }
            else{
              subRoot = subRoot.getLeftChild();
            }
          }
          else if(dataArray[i] > subRoot.getNodeValue()){
            if(subRoot.rightChild == null){
              subRoot.setRightChild(x);
              added = true;
            }
            else{
              subRoot = subRoot.getRightChild();
            }
          }
          else{added = true;} // do nothing if the element is already in the tree
        }
      }
      i++;
    }
  }
  
  // remove duplicate numbers and keep tree balanced
    int [] uniquesArray = null;
    int uniqueCount = 0;
    for( int pass = 1; pass <= 2; pass++ ){
      if( pass == 2 )
        uniquesArray = new int[uniqueCount];
      int i = 0, j = 0;
      while( i < sortedArray.length ){
        if( pass == 1 )
          uniqueCount++;
        else
          uniquesArray[j++] = sortedArray[i];
        i++;
        while( i < sortedArray.length && sortedArray[i] == sortedArray[i-1] )
          i++;
      }
    }
    return uniquesArray;
  }
  
  void inOrderTraversal( BSTNode treeNode ){
    int i=0;
    InOrderIterator inOrder = new InOrderIterator(treeNode);
    while( inOrder.hasNext() ){
      BSTNode x = inOrder.next();
      x.setInOrderSequence(i);
      i++;
    }
  }
  
  public void addGraphicsHWVersion(Graphics g, int window_w, int window_h){
    int i=0;
    
    // Use inOrderTraversal to assign inOrderSequence numbers for all nodes in the BST.
    LinkedList<BSTNode> inOrderQueue = new LinkedList<BSTNode>();
    
    int inOrderPosition = 0;
    inOrderQueue.add(rootNode);
    while( !inOrderQueue.isEmpty() ){
      // Get current node from the queue
      BSTNode curNode = inOrderQueue.remove();
      curNode.setInOrderSequence(inOrderPosition);
      
      // Use getNodeX with getLevelOrderPosition for curNode
      int curNode_x = getNodeX(curNode.getInOrderSequence()); 
      // Use getNodeY with getNodeDepth for curNode
      int curNode_y = getNodeY(curNode.getNodeDepth()); 
      
      // Display current node at coordinates (curNode_x, curNode_y) 
      drawNumberCircle(g, curNode.getNodeValue(), curNode_x, curNode_y);
      
      if( curNode.getNodeDepth() != 0 ){
        // Following code is to draw line connecting currentNode and its
        // parent node
        BSTNode parentNode = curNode.getParent();
        // TASK: fill in the ??? for the following lines
        int parentNode_x = getNodeX(parentNode.getNodeDepth());
        // Use getNodeX with getLevelOrderPosition
        // for parentNode  
        int parentNode_y = getNodeY(parentNode.getNodeDepth()); 
        // Use getNodeY with getNodeDepth 
        // for parentNode
        // Draw the connecting line.
        drawLine(g, curNode_x, curNode_y, parentNode_x, parentNode_y);
      }
      
      inOrderPosition++;
      if( curNode.getLeftChild() != null )
        inOrderQueue.add(curNode.getLeftChild());
      if( curNode.getRightChild() != null )
        inOrderQueue.add(curNode.getRightChild());
    }
  }
  
  void PrintAsText(){
    // set inOrderSequence for all nodes in the tree
    inOrderTraversal(rootNode);
    
    LinkedList<BSTNode> q = new LinkedList<BSTNode>();
    q.add( rootNode );
    int last_Y = 0;
    int last_X = 0;
    
    while( q.peek() != null ) {
      BSTNode m = q.remove();
      
      // if node is on the next level, move to the next line
      if(last_Y < m.getNodeDepth()){
        System.out.println();
        last_X = 0;
      }
      
      last_Y = m.getNodeDepth(); // update last_Y
      
      // print spaces
      for(int i = last_X; i < m.getInOrderSequence(); i++){
        System.out.print("  ");
      }
      
      last_X = m.getInOrderSequence(); // update last_X
      
      System.out.print(m.getNodeValue()); // print value
      
      if(m.getLeftChild() != null){ q.add(m.leftChild); }
      if(m.getRightChild() != null){ q.add(m.rightChild); }
    }
  }
  
  void PrintOnCanvas(){
    java.awt.EventQueue.invokeLater(new Runnable(){
      public void run(){
        new BSTClient().setVisible(true);
      }
    });
  }
  
  // The following are graphics helper methods, no change is needed
  static int node_diameter = 40;
  public void displayDataArray(Graphics g, int window_w, int window_h){
    
    int y_offset = node_diameter;
    int x_offset = (window_w - node_diameter * dataArray.length)/2;
    for( int i = 0; i < dataArray.length; i++ )
    {
      g.setColor(Color.ORANGE);
      g.drawRect(x_offset, y_offset , node_diameter , node_diameter );
      g.setColor(Color.RED);
      g.setFont(new Font("Verdana",Font.BOLD,15));
      int x = x_offset + node_diameter/2 - 12;
      int y = y_offset + node_diameter/2 + 5;
      g.drawString(""+dataArray[i], x , y);
      g.setColor(Color.BLUE);
      g.drawString(""+i, x, y + node_diameter);
      x_offset += node_diameter;
    }
  }
  int getNodeX(int xPosition){
    return (int)(1.2 * node_diameter * xPosition) + node_diameter;
  }
  int getNodeY(int nodeDepth){
    return 2 * node_diameter * nodeDepth + (int)(3.5 * node_diameter);
  }
  // Draws a circle at coordinates (node_x, node_y) with the node_value inside 
  void drawNumberCircle(Graphics g, int node_value, int node_x, int node_y)
  {
    g.setColor(Color.ORANGE);
    g.fillOval( node_x, node_y, node_diameter, node_diameter );
    g.setColor(Color.RED);
    g.setFont(new Font("Verdana",Font.BOLD,15));
    g.drawString( ""+node_value, node_x + node_diameter/2 - 12, node_y + node_diameter/2 +5 );
  }
  
  // Draws a line between node1 coordinates (node1_x, node1_y) 
  // and node2 coordinates (node2_x, node2_y)
  void drawLine( Graphics g, int node1_x, int node1_y, int node2_x, int node2_y)
  {
    int radius = node_diameter / 2;
    int x1 = node1_x + radius;
    int y1 = node1_y + radius;
    int x2 = node2_x + radius;
    int y2 = node2_y + radius;
    double dist = Math.sqrt(Math.pow((x1 - x2), 2)+Math.pow((y1 - y2), 2));
    double alpha1 = radius / dist;
    double alpha2 = (dist - radius) / dist;
    int xn1 = (int)(alpha1 * x1 + (1 - alpha1) * x2);
    int yn1 = (int)(alpha1 * y1 + (1 - alpha1) * y2);
    int xn2 = (int)(alpha2 * x1 + (1 - alpha2) * x2);
    int yn2 = (int)(alpha2 * y1 + (1 - alpha2) * y2);
    
    g.setColor(Color.GRAY);
    g.drawLine(xn1, yn1, xn2, yn2);
  }
}

// StackElement has two fields: BSTreeNode and a boolean visited,
// and will be used in the Inorder iterator.
class StackElement {
  BSTNode btreeNode;
  boolean visited;
  StackElement( BSTNode btreeNode, boolean visited ){
    this.btreeNode = btreeNode;
    this.visited = visited;
  }
  BSTNode getNode(){ return btreeNode; }
  boolean isVisited(){ return visited; }
}

class InOrderIterator implements Iterator<BSTNode>{
  Stack<StackElement> iteratorStack;
  
  InOrderIterator(BSTNode root){
    iteratorStack = new Stack<StackElement>();
    iteratorStack.push(new StackElement(root, false));
  }
  public boolean hasNext() {        
    return !iteratorStack.isEmpty();
  }
  public BSTNode next(){
    
    /// Repeat until we have a visited node on top of the stack.
    while( !iteratorStack.peek().isVisited() ){
      
      // Get current node from top of the stack.
      BSTNode curNode = iteratorStack.pop().getNode(); 
      
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