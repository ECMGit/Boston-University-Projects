/* Variable.java: class that defines a variable object
 * 
 * code by Tim Duffy <timmahd@bu.edu>
 */

public class Variable{
  protected char name;
  protected  int value;
  
  //constructor
  public Variable(char name, int value){
    this.name = name;
    this.value = value;
  }
  
  //accessor methods
    public char getName(){
    return name;
  }
  
  public int getValue(){
    return value;
  }
}