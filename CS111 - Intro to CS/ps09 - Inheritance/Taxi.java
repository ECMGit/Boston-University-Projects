/*
 * Taxi.java
 *
 * A class that represents a taxi.  It is a subclass of Automobile.
 *
 * It inherits fields and methods from Automobile and Vehicle.
 *
 * Computer Science 111, Boston University
 */

public class Taxi extends Automobile {
    private String taxiID;
    private double fareTotal;
    private int numFares;

    public Taxi(String make, String model, int year, String id) {
        // Invoke the Automobile constructor to initialize
        // the fields inherited from Automobile and Vehicle.
        // Note that we assume that the taxi is not an SUV.
        super(make, model, year);
        taxiID = id;
    }
 
    /*** accessors ***/
    // getMake(), getModel(), etc. are inherited from Vehicle.
    // getNumSeats() and isSUV(), etc. are inherited from Automobile.

    public String getID() {
        return taxiID;
    }

    public double getFareTotal() {
        return fareTotal;
    }
    
    public int getNumFares() {
        return numFares;
    }

    public double getAverageFare() {
        if (numFares == 0) {
            System.out.println("no fares collected");
            return -1.0;
        }
     
        return fareTotal/numFares;
    }
    
    /*** mutators ***/
    
    public void setID(String id) {
        taxiID = id;
    }
    
    public void addFare(double fare) {
        if (fare < 0) {
            throw new IllegalArgumentException();
        }
            
        fareTotal += fare;
        numFares++;
    }
    
    public void resetFareInfo() {
        fareTotal = 0;
        numFares = 0;
    }
    
    /*
     * Creates a string that can be used when printing
     * a Taxi object.  This method overrides the 
     * toString() method inherited from the Automobile class.
     */
    public String toString() {
        return "Taxi (id = " + taxiID + ")";
    }
}
