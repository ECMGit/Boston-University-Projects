/*
 * GraphTools.java - 
 * 
 * Base code By: Ashwin Thangali
 * 
 * Implemented By: Tim Duffy <timmahd@bu.edu>
 * 
 */

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

class Graph{
  NodeList [] adjacencyList; // array of NodeLists
  int numNodes; // number of nodes in graph
  int getNumNodes(){ return numNodes; }
  NodeList[] getList() { return adjacencyList; }
  
  public Graph(String fileName) throws FileNotFoundException{
    Scanner fileScanner = new Scanner(new FileInputStream(fileName));
    int maxNodeId = -1;
    
    // find the largest node ID in graph
    while(fileScanner.hasNextInt()){
      int x = fileScanner.nextInt();
      if(x > maxNodeId){ maxNodeId = x; }
    }
    fileScanner.close();
    
    // Set numNodes: note that the nodeIds are in the range [1, maxNodeId].
    numNodes = maxNodeId + 1;
    System.out.println("Graph created with " + numNodes + " nodes.");
    
    // initialize adjacencyList to NodeList array of length numNodes.
    adjacencyList = new NodeList[numNodes];
    
    // initialize each adjacencyList element to a new NodeList.
    for(int i = 0; i < numNodes; i++){
      adjacencyList[i] = new NodeList();
      adjacencyList[i].addToHead(i);
    }
    
    // Reopen the file to construct the graph.
    fileScanner = new Scanner(new FileInputStream(fileName));
    while( fileScanner.hasNextInt() ){
      
      // Read a pair of nodeIds (node1, node2) from the input file.
      int a = fileScanner.nextInt();
      int b = fileScanner.nextInt();
      
      // Add node2 to the NodeList corresponding to node1 in adjacencyList
     adjacencyList[a].addToHead(b);
    }
    
    fileScanner.close();
  }
  
  public static void main(String[] args)throws Exception{
    
    Graph g = new Graph("small-graph.txt");
    NodeList[] list = g.getList();
    
    degreeRanking(list); // Rank Nodes by Degree
    clusterRanking(list); // Rank Nodes by Cluster Coefficent
    centralityRanking(list, g.getNumNodes()); // Rank Nodes by Closeness Centrality
  }
  
  // ranks nodes by degree
  static void degreeRanking(NodeList[] list)throws Exception{
    BufferedWriter degOut = new BufferedWriter(new FileWriter("degreeRanking.txt")); // create file
    NodeList[] degArray = list;
    degreeSort(degArray);
    
    // Write to file 
    degOut.write("Node ID\tDegree\r\n"); // heading in file
    for(int i = 0; i < degArray.length; i++){
      NodeListIterator listIterator = degArray[i].iterator();
      
      int nodeID = 0;
      while(listIterator.hasNext()){
        nodeID = listIterator.next();
      }
      degOut.write(nodeID + "\t" + degArray[i].getSize() + "\r\n");
    }
    degOut.close(); // flush and close BufferedWriter
  }
  
  // rank nodes by cluster coefficient
  static void clusterRanking(NodeList[] list) throws Exception{
    Scanner fileScanner = new Scanner(new FileInputStream("degreeRanking.txt")); // read from file
    fileScanner.nextLine(); // clear first line of file. It is the heading and does not need to be read
    
    NodeInfo[] clusterArray = new NodeInfo[100]; // array of NodeInfo entries that will be sorted later
    
    // initalize clusterArray to the 100 nodes with highest degree
    for(int i = 0; i < 100; i++){
      int id = fileScanner.nextInt();
      int deg = fileScanner.nextInt();
      clusterArray[i] = new NodeInfo(id, deg);
      
      // set cluster coeffiecent
      clusterArray[i].setCC( clusteringCoefficient(list, id) );
    }
    
    clusterSort(clusterArray); // sort array
    
    BufferedWriter clusterOut = new BufferedWriter(new FileWriter("clusteringRanking.txt")); // create file
    clusterOut.write("Node ID\tCluster Coefficient\r\n"); // heading in file
    for(int i = 0; i < clusterArray.length; i++){
      clusterOut.write(clusterArray[i].getID() + "\t" + clusterArray[i].getCC() + "\r\n");
    }
    clusterOut.close();
  }
  
  static void centralityRanking(NodeList[] list, int numNodes)throws Exception{
    Scanner fileScanner = new Scanner(new FileInputStream("degreeRanking.txt")); // read from file
    fileScanner.nextLine(); // clear first line of file. It is the heading and does not need to be read
    
    NodeInfo[] centralityArray = new NodeInfo[100]; // array of NodeInfo entries that will be sorted later
    
    // initalize centralityArray to the 100 nodes with highest degree
    for(int i = 0; i < 100; i++){
      int id = fileScanner.nextInt();
      int deg = fileScanner.nextInt();
      centralityArray[i] = new NodeInfo(id, deg);
      
      // set closeness centrality
      centralityArray[i].setCLC(closenessCentrality(id, numNodes, list));
    }
    
    closenessSort(centralityArray); // sort array
    
    // sort centralityArray
    
    BufferedWriter clcOut = new BufferedWriter(new FileWriter("closenessRanking.txt")); // create file
    clcOut.write("Node ID\tCloseness Centrality\r\n"); // heading in file
    for(int i = 0; i < centralityArray.length; i++){
      clcOut.write(centralityArray[i].getID() + "\t" + centralityArray[i].getCLC() + "\r\n");
    }
    clcOut.close();
  }
  
  
// Merge Sort implemented to sort by degree
  public static void degreeSort(NodeList[] A){
    
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
  
  public static void merge(NodeList [] A,  int leftPos, int rightPos, int rightEnd){
    int leftEnd = rightPos - 1;   // assumes rightPos > leftPos
    int numElements = rightEnd - leftPos + 1;
    NodeList [] B = new NodeList [numElements];
    int bPos = 0;
    
    // While *both* left and right have elements remaining, compare largest of each.
    while(leftPos <= leftEnd && rightPos <= rightEnd){
      
      // If left element smaller, copy it, and adjust ptrs
      if(A[leftPos].getSize() >= A[rightPos].getSize()){
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
  
  public static double clusteringCoefficient(NodeList[] list, int id){
    NodeList temp = list[id];
    int edgesInNbd = 0; // initialize edges-in-neighborhood to 0
    int [] nghbrs = new int [temp.getSize()+1];
        
    NodeListIterator listIterator = temp.iterator();
    // put every neighbor of list[id] in nghbrs array
    int i = 0;
    while(listIterator.hasNext()){
      nghbrs[i] = listIterator.next();
      i++;
    }
    
    // Scan pairs of neighbors and increment counter whenever there is an edge
    for(int j = 0; j < nghbrs.length; j++){ // for each node in nghbrs[]
      NodeList x = list[nghbrs[j]];
      for(int k = 0; k < nghbrs.length; k++){ // check for edge between all other nodes in nghbrs[]
        if(isEdge(x, nghbrs[k])){ edgesInNbd++; }
      }
    }
    
    return (double)(2* edgesInNbd)/ ( (double) ((temp.getSize()) *(temp.getSize()-1)) );
  }
  
  // Boolean method that tells us if {v1, v2} is an edge in the graph
  public static boolean isEdge(NodeList v1, int v2){
    NodeListIterator listIterator = v1.iterator();
    
    // if v2 exists in the adjacency list of v1, return true
    while(listIterator.hasNext()){
      Integer target = listIterator.next();
      if(target.equals(v2)){ return true; }
    }
    
    return false;
  }
  
  // Insertion sort implemented to sort by cluster coeffieient
  public static void clusterSort(NodeInfo[] a){
    int p = 1;
    while (p < a.length) {
      NodeInfo tmp = a[p];
      
      int j = p;
      while (j > 0 && a[j-1].getCC() < tmp.getCC()){
        a[j] = a[j-1];
        j--;
      }
      
      a[j] = tmp;
      p++;
    }
  }
  
  public static double closenessCentrality(int startNode, int numNodes, NodeList[] list){
    double clc=0;
 
    int[] bfs_distances = BFS_distance(startNode, numNodes, list);
    
    for(int i = 0; i < bfs_distances.length; i++){ 
      clc += bfs_distances[i];
    }
    
    return clc / (numNodes - 1);
  }
  
  // This method returns the shortest path distance from startNode to all nodes 
  // in the graph using Breadth First Search.
  public static int[] BFS_distance( int startNode, int numNodes, NodeList[] list){
    
    // BFS_queue will be used for BFS traversal.
    LinkedList<Integer> BFS_queue = new LinkedList<Integer>();
    
    // Visited array keeps track of nodes that have previously been added to the queue.
    boolean [] visited = new boolean[numNodes];
    
    // bfs_distances array stores the distance from startNode to all nodes in the graph. 
    int [] bfs_distances = new int[numNodes];
    
    // Initialize the distances to -1, this implies their distance is not yet known. 
    for( int i = 0; i < numNodes; i++ ){ bfs_distances[i] = -1; }
    
    // Perform BFS initialization here.
    BFS_queue.add(startNode);
    visited[startNode] = true;
    bfs_distances[startNode] = 0;
    
    // Loop until the BFS_queue is empty.
    while( !BFS_queue.isEmpty() ) {
      int currentNode = BFS_queue.remove();
      
      for( Integer adjacentNode : list[currentNode] ){ // For each 'unvisited' node V adjacent to currentNode
        if( !visited[adjacentNode] ){
          BFS_queue.add(adjacentNode); // add V to the BFS_queue
          visited[adjacentNode] = true; // set the visited flag for V in visited array
          bfs_distances[adjacentNode] = bfs_distances[currentNode] + 1; // set distance value for V in bfs_distances array 
        }
      }
    }
    
    return bfs_distances;
  }
  
    // Insertion sort implemented to sort by closeness coeffieient
  public static void closenessSort(NodeInfo[] a){
    int p = 1;
    while (p < a.length) {
      NodeInfo tmp = a[p];
      
      int j = p;
      while (j > 0 && a[j-1].getCLC() < tmp.getCLC()){
        a[j] = a[j-1];
        j--;
      }
      
      a[j] = tmp;
      p++;
    }
  }
}

class Node{
  int nodeId;
  Node nextNode;
  public Node(int newNodeId, Node newNext){
    nodeId = newNodeId;
    nextNode = newNext; 
  }
  public int getNodeId(){ return nodeId; }
  public Node getNext() { return nextNode; }
  public void setNext(Node x){ nextNode = x; }
}

class NodeListIterator implements Iterator<Integer>{
  Node currentNode;
  public NodeListIterator(Node headNode){
    currentNode = headNode;
  }
  public boolean hasNext(){
    return (currentNode != null);
  }
  public Integer next(){ 
    Integer currentNodeId = currentNode.getNodeId();
    currentNode = currentNode.getNext();
    return currentNodeId; 
  }
  public void remove(){}
}

class NodeList implements Iterable<Integer>{
  int size;
  Node headNode;
  
  public NodeList(){
    size = 0;
    headNode = null;
  }
  public int getSize(){ return size - 1; }
  public void addToHead(int addNodeId){
    headNode = new Node(addNodeId, headNode);
    size++;
  }
  public NodeListIterator iterator(){
    return new NodeListIterator(headNode);
  }
}

class NodeInfo{
  Integer ID;
  int degree;
  double cc; // closeness coefficent
  double clc; // closeness centrality
  
  public NodeInfo(int a, int b){
    ID = a;
    degree = b;
  }
  public int getID(){ return ID; }
  public int getDegree(){ return degree; }
  public double getCC(){ return cc; }
  public double getCLC(){ return clc; }
  public void setCC(double x){ cc = x; }
  public void setCLC(double x){ clc = x; }
}