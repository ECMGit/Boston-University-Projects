/*
 * TractorTrailer.java
 *
 * A class that represents a tractor trailer.  It is a subclass of Truck.
 *
 * It inherits fields and methods from Truck and Vehicle.
 *
 * Computer Science 111, Boston University
 */

public class TractorTrailer extends Truck {
    private boolean isSleeper;

    public TractorTrailer(String make, String model, int year, int numWheels) {
        // Invoke the Truck constructor to initialize
        // the fields inherited from Truck and Vehicle.
        super(make, model, year, numWheels);
        
        // default value, which can be changed using setIsSleeper
        isSleeper = false;
    }
 
    /*** accessors ***/
    // getMake(), getModel(), etc. are inherited from Vehicle.
    // getCapacity() and getNumAxles() are inherited from Truck.

    public boolean isSleeper() {
        return isSleeper;
    }

    //
    // We override the getNumAxles() from the superclass.
    // It doesn't work for tractor trailers, because they
    // have four wheels on most of their axles.
    //
    public int getNumAxles() {
        // number of wheels on all but the front axle
        int numBackWheels = getNumWheels() - 2;  
     
        return 1 + numBackWheels/4;
    }
    
    /*** mutators ***/
    /** setMileage and setPlateNumber are inherited from Vehicle. **/
    /** setCapacity() is inherited from Truck. **/
    
    public void setIsSleeper(boolean isSleeper) {
        this.isSleeper = isSleeper;
    }
        
    /*
     * Creates a string that can be used when printing
     * a TractorTrailer object.  This method overrides the 
     * toString() method inherited from the Truck class.
     */
    public String toString() {
        // Call the Truck version of this method
        // to get a string containing make, model, and capacity.
        String str = super.toString();
  
        // Add tractor-trailer info.
        // We include the number of wheels, since 
        // different tractor trailers can have different
        // numbers of wheels.  Note that we have to call
        // getNumWheels(), because numWheels is a private
        // instance variable of a superclass.
        str = str + ", numWheels = " + getNumWheels();
        str = str + ", sleeper = " + isSleeper;
        return str;
    }
}
