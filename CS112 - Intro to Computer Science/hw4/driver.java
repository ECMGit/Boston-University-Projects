/*
 * driver.java - contains the user interface for use with BST.java
 * 
 * Starter code by: Ashwin cs112, Boston University
 * 
 * implemented by: Tim Duffy <timmahd@bu.edu>
 */

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

// User interface
class driver{
  public static void main(String[] args){
    String choice = "y";
    
    while(choice.equalsIgnoreCase("y")){
      
      BST bstree = new BST();
      Scanner console = new Scanner(System.in);
           
      System.out.print("Enter a JSON string for insertion: ");
      String JSON_string = console.nextLine();
      bstree.bulkInsert(JSON_string); // insert string given by user
      
      System.out.println();
      bstree.PrintAsText(); // print as text
      System.out.println();
      System.out.println();
      
      System.out.print("Press enter to print tree on canvas. ");
      console.nextLine();
      
      bstree.PrintOnCanvas();
      
      System.out.println();
      System.out.println();
      System.out.println("Would you like to clear the display and create a new tree? (press 'y' to run again): ");
      choice = console.next();
      System.out.println("===================================================");
      System.out.println();
    }
  }
}

class BSTClient extends javax.swing.JFrame implements MouseListener, FocusListener
{
  BST bst;
  public BSTClient()
  {
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE  );
    setSize(1021, 521);
    setTitle("Binary Search Tree display");
    addMouseListener(this);
    addFocusListener(this);
  }
  public void paint(Graphics g)
  {
    super.paint(g);
    bst.displayDataArray(g, this.getWidth(), this.getHeight());
    
    bst.addGraphicsHWVersion(g, this.getWidth(), this.getHeight());
    setTitle("Binary Search Tree display -- HOME WORK version");
  }
  public void mouseClicked(MouseEvent arg0) {
    String JSON_str = JOptionPane.showInputDialog(null, 
                                                  "Enter JSON string : ",
                                                  "Enter JSON string", 1);
    bst.bulkInsert(JSON_str);
    repaint();
  }
  public static void main(String[] args)
  {
    java.awt.EventQueue.invokeLater(new Runnable()
                                      {
      public void run()
      {
        new BSTClient().setVisible(true);
      }
    });    
  }
  
  // Other mouse listener event handlers blank methods
  public void mouseEntered(MouseEvent arg0) { }
  public void mouseExited(MouseEvent arg0) { }
  public void mousePressed(MouseEvent arg0) { }
  public void mouseReleased(MouseEvent arg0) { }
  public void focusGained(FocusEvent arg0) {
    repaint();
  }
  public void focusLost(FocusEvent arg0) { }   
}