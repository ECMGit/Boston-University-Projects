/*
 * Truck.java
 *
 * A class that represents a truck  It is a subclass of Vehicle and
 * a superclass of superclass of other classes that represent trucks
 * (for now, just TractorTrailer).
 *
 * It inherits many of its fields and methods from Vehicle.
 *
 * Fields and methods that all trucks have in common are defined
 * here so that they can be inherited by the subclasses of this class.
 *
 * Computer Science 111, Boston University
 */

public class Truck extends Vehicle {
    private int capacity;
 
    /** make, model, etc. are inherited from Vehicle **/
 
    public Truck(String make, String model, int year, int numWheels) {
        // invoke the Vehicle constructor to initialize
        // the fields inherited form Vehicle
        super(make, model, year, numWheels);
  
        // default value, which can be changed using setCapacity.
        capacity = 0;
    }

    /*** accessors ***/
    /** getMake(), getModel(), etc. are inherited from Vehicle. **/
 
    public int getCapacity() {
        return capacity;
    }
 
    public int getNumAxles() {
        // We need to invoke getNumWheels(),
        // because numWheels is a private instance
        // variable of the superclass.
        return getNumWheels() / 2;
    }
 
    /*** mutators ***/
    /** setMileage and setPlateNumber are inherited from Vehicle. **/
 
    public void setCapacity(int c) {
        capacity = c;
    }   
 
    /*
     * Creates a string that can be used when printing
     * a Truck object.  This method overrides the 
     * toString() method inherited from the Vehicle class.
     */
    public String toString() {
        // Call the Vehicle version of this method
        // to get a string containing the make and model.
        String str = super.toString();
  
        // Add information about the capacity.
        str = str + ", capacity = " + capacity;
        return str;
    }
}
