/* Calculator.java: a programable calculator which: 
 * - assigns a one character variable to a value expressed as a valid postfix expression.
 * - evaluates a function as a postfix expression and returns the equivalent integer value.
 * 
 * code by: Tim Duffy <timmahd@bu.edu>
 */

public class Calculator{
  
  private static VList vlst;
  
  //constructor
  public void Calculator(){
    //make a linked list
    vlst = new VList();
  }
  
  // Assign a variable to its corresponding value
  public void assign(String statement){
    
    //create new variable
    char ch = statement.charAt(0);
    int value = evaluate(statement.substring(2));// value expression begins after equal sign
    Variable x = new Variable(ch, value);
  }
  
  // Convert an expression given as a string into an integer value
  public static int evaluate(String expr){
    int answer = 0;

    //make a new empty stack
    CStack cstk = new CStack();
        
    // Check each element in the array and assign it appropriately
    for(int i = 0; i <= expr.length()-1; i++){
      char ch = expr.charAt(i);
      
      // if ch is a digit, push onto stack
      if(ch >= '0' && ch <='9'){
        cstk.push(ch - 48);
        answer = cstk.peek();
      }
      else if(ch >= 'a' && ch <='Z'){
        cstk.push(vlst.findValue(ch));
      }
      // otherwise it is an operator. Therefore we preform the operation.
      else{
        int op1 = cstk.pop();
        int op2 = cstk.pop();
        
        // determine which operand is used, evaluate, and push
        // the result back on the stack
               
        if(ch == '*'){
          answer = (op1 * op2);
        }
        else if(ch == '+'){
          answer = (op1 + op2);
        }
        else if(ch == '-'){
          answer = (op1 - op2);
        }
        else if(ch == '/'){
          answer = (op1 / op2);
        }
      }
    }
    return answer;
  }
}