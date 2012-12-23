/*
 * DateSubtracter.java
 * 
 * A program for computing the number of days between two dates in a given year.
 * 
 * Computer Science 111, Boston University
 */

public class DateSubtracter {
    /* array containing the number of days in each month (ignoring leap years) */
    public static final int[] DAYS_IN_MONTH =
      {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    
    public static void main(String[] args) {
        int numDays;

        numDays = daysBetween(7, 4, 12, 25);
        System.out.println("There are " + numDays + " days from July 4 to Christmas.");
        if (numDays != 174) {
            System.out.println("  *** ERROR: we should get 174 days");
        }
            
        numDays = daysBetween(2, 14, 3, 17);
        System.out.println("There are " + numDays + " days from Valentine's Day " +
          "to St. Patrick's Day.");
        if (numDays != 31) {
            System.out.println("  *** ERROR: we should get 31 days");
        }
    }
    
    /*
     * daysBefore- computes the number of days in the year
     * that come before the specified month.
     */
    private static int daysBefore(int month) {
        int numDays = 0;

        for (int m = 1; m < month; m++) {
            numDays = numDays + DAYS_IN_MONTH[m-1];
        }

        return numDays;
    }

    /*
     * daysBetween - computes the number of days from the date
     * represented by month1 and day1 to the date represented by
     * month2 and day2.
     */
    public static int daysBetween(int month1, int day1, int month2, int day2) {
        int dayInYear = daysBefore(month1) + day1;
        int otherDayInYear = daysBefore(month2) + day2;

        return (otherDayInYear - dayInYear);
    }
}