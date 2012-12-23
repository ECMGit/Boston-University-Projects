public class MovingVan extends Truck{
  
  private int cargoHeight;
  private boolean hasRamp;
  
  // make, model, etc. are inherited from Vehicle
  public MovingVan(String make, String model, int year, int numWheels, int distance, boolean ramp){
    
    //invoke the Truck constructor to initilize the feilds from the Truck constructor
    super(make, model, year, numWheels);
    
    //set the height of the cargo area
    cargoHeight= distance;
    
    //determine if the truck has a ramp
    hasRamp = ramp;
  }
  
  //accessors 
  
  // Get the height of the cargo area
  public int getCargoHeight(){
    return cargoHeight;
  }
  
  public boolean hasRamp(){
    return hasRamp;
  }
  
  // mutators
  
  public String toString() {
    // Call the truck version of this method
    String str = super.toString();
    
    // Add information about a MovingVan object.
    
    if(hasRamp == true){
      str = str + ", distance to cargo = " + cargoHeight + ", has a ramp.";
    }
    else{
      str = str + ", distance to cargo = " + cargoHeight + ", does not have a ramp.";
    }
    return str;
  }
}
