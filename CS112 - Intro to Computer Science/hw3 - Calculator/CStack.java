/* CStack.java: Defines a stack that will evaluate postfix expressions
 * 
 * code by: Tim Duffy <timmahd@bu.edu>
 */

import java.util.Stack;

public class CStack{
  private int[] S = new int[1000]; // create a stack of fixed size
  private int actualCap = 1000;
  private int top = -1;
  
  //returns size of stack
  public int size(){
    return (top +1);
  }
  
  // is stack empty?
  public boolean isEmpty(){
    return (top < 0);
  }
  
  // add an elemnt to the top of the stack
  public void push(int element)throws FullStackException{
    if(size() == actualCap){
      throw new FullStackException("Stack is Full.");
    }
    S[++top] = element;
  }
  
  public int top()throws EmptyStackException{
    if(isEmpty()){
      throw new EmptyStackException("Stack is Empty.");
    }
    return S[top];
  }
  
  // Deletes top element and returns its value
  public int pop() throws EmptyStackException{
    int element;
    if(isEmpty()){
      throw new EmptyStackException("Stack is Empty.");
    }
    element = S[top];
    S[top--] = 0;
    return element;
  }
  
  // Returns the value of the element on top
  public int peek() throws EmptyStackException{
    if(isEmpty()){
      throw new EmptyStackException("Stack is Empty.");
    }
    return S[top];
  }
  
//inherits all methods and member variables
  public class EmptyStackException extends RuntimeException {  
    public EmptyStackException(String err){
      super(err); //Calls constructor from RuntimeException
    }
  }
  
//inherits all methods and member variables
  public class FullStackException extends RuntimeException {  
    public FullStackException(String err){
      super(err); //Calls constructor from RuntimeException
    }
  }
}