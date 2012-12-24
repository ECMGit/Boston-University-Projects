/*
 * Vehicle.java
 *
 * A class that represents a vehicle.  It is a superclass of all of the
 * other classes that represent vehicles (Automobile, Motorcycle, Truck, 
 * and their subclasses).
 *
 * Fields and methods that all vehicles have in common are defined
 * here so that they can be inherited by the subclasses of this class.
 *
 * Computer Science 111, Boston University
 */

public class Vehicle {
    private String make;
    private String model;
    private int year;
    private int numWheels;
    private int mileage;
    private String plateNumber;
     
    public Vehicle(String make, String model, int year, int numWheels) {
        this.make = make;
        this.model = model;
        if (year < 1900) {
            throw new IllegalArgumentException();
        }
        this.year = year;
        this.numWheels = numWheels;
        mileage = 0;
        plateNumber = "unknown";
    }

    /*** accessors ***/
    
    public String getMake() {
        return make;
    }
    
    public String getModel() {
        return model;
    }
    
    public int getYear() {
        return year;
    }
    
    public int getNumWheels() {
        return numWheels;
    }
    
    public int getMileage() {
        return mileage;
    }
    
    public String getPlateNumber() {
        return plateNumber;
    }

 
    /*** mutators ***/ 
       
    public void setMileage(int newMileage) {
        if (newMileage < mileage) {
            throw new IllegalArgumentException();
        }
        mileage = newMileage;
    }
 
    public void setPlateNumber(String plate) {
        plateNumber = plate;
    }
    
    /*
     * Creates a string that can be used when printing
     * Vehicles.  This method overrides the toString() 
     * method inherited from the Object class.
     */
    public String toString() {
        String str = make + " " + model;
        return str;
    }
}
