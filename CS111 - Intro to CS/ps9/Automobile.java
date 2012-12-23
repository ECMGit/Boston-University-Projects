/*
 * Automobile.java
 *
 * A class that represents an automobile.  It is a subclass of Vehicle and
 * a superclass of superclass of other classes that represent automobiles
 * (for now, just Taxi).
 *
 * It inherits many of its fields and methods from Vehicle.
 *
 * Fields and methods that all automobiles have in common are defined
 * here so that they can be inherited by the subclasses of this class.
 *
 * Computer Science 111, Boston University
 */

public class Automobile extends Vehicle {
    private int numSeats;
    private boolean isSUV;
 
    /** make, model, etc. are inherited from Vehicle **/
 
    public Automobile(String make, String model, int year, int numSeats,
      boolean isSUV) {
        // invoke the Vehicle constructor to initialize
        // the fields inherited from Vehicle
        super(make, model, year, 4);   
        this.numSeats = numSeats;
        this.isSUV = isSUV;
    }

    public Automobile(String make, String model, int year) {
        this(make, model, year, 5, false);
    }                                          
    
    /*** accessors ***/
    // getMake(), getModel(), etc. are inherited from Vehicle.
 
    public int getNumSeats() {
        return numSeats;
    }
 
    public boolean isSUV() {
        return isSUV;
    }
 
    /*** mutators are all inherited from Vehicle ***/
    
    /*
     * Creates a string that can be used when printing
     * an Automobile object.  This method overrides the 
     * toString() method inherited from the Vehicle class.
     */
    public String toString() {
        // Call the Vehicle version of this method
        // to get a string containing the make and model.
        // Alternatively, we do: String str = getMake() + " " + getModel();
        String str = super.toString();
  
        // Add information about the number of seats.
        str = str + " (" + numSeats + " seats)";
        return str;
    }
}
