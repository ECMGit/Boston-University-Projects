using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Linq.Expressions;
using PINQ;

namespace DPLab
{
    class Record
    {
        public double state { get; private set; }
        string name;
        public double enrollment { get; private set; }          //
        public double totalRev { get; private set; }            //
        public double totalFedRev { get; private set; }         //
        public double totalStateRev { get; private set; }       //
        public double totalLocalRev { get; private set; }       //
        public double revTaxes { get; private set; }            //
        public double revOther { get; private set; }            //
        public double revCharges { get; private set; }          //
        public double totalExp { get; private set; }            //
        public double expInstr { get; private set; }            //
        public double expSscv { get; private set; }             //
        public double expOther { get; private set; }            //
        public double costFood { get; private set; }            //
        public double totalCapOut { get; private set; }         //
        public double teachEquip { get; private set; }          //
        public double totalSal { get; private set; }            
        public double teachSal { get; private set; }

        public double CostPerStudent { get; private set; }      //
        public double debt { get; private set; }                //
        public double TeachPayPerStudent { get; private set; }      //
        public double percentForFood { get; private set; }      //

        public void setAll(string[] data)
        {
            this.state = Convert.ToDouble(data[0]);
            this.name = data[1];
            this.enrollment = Convert.ToDouble(data[2]);
            this.totalRev = Convert.ToDouble(data[3]); ;
            this.totalFedRev = Convert.ToDouble(data[4]);
            this.totalStateRev = Convert.ToDouble(data[5]);
            this.totalLocalRev = Convert.ToDouble(data[6]);
            this.revTaxes = Convert.ToDouble(data[7]);
            this.revOther = Convert.ToDouble(data[14]);
            this.revCharges = Convert.ToDouble(data[24]);
            this.totalExp = Convert.ToDouble(data[31]);
            this.expInstr = Convert.ToDouble(data[32]);
            this.expSscv = Convert.ToDouble(data[33]);
            this.expOther = Convert.ToDouble(data[34]);
            this.costFood = Convert.ToDouble(data[35]);
            this.totalCapOut = Convert.ToDouble(data[36]);
            this.teachEquip = Convert.ToDouble(data[37]);
            this.totalSal = Convert.ToDouble(data[38]);
            this.teachSal = Convert.ToDouble(data[39]);

            this.CostPerStudent = (enrollment == 0)? 0 : (totalExp/ enrollment);
            this.debt = (totalExp - totalRev) <= 0 ? 0 : (totalExp - totalRev);
            this.TeachPayPerStudent = (enrollment <= 0) ? 0 : (teachSal /enrollment);
        }
    }
    class DBquery
    {
        static void Main(string[] args)
        {

            // Read from data file, and insert into class
            string[] lines = System.IO.File.ReadAllLines(@"C:\Users\Administrator\Documents\Visual Studio 2010\Projects\DPLab\ConsoleApplication2\DPLabData.csv");
            IList<Record> recList = new List<Record>();

            foreach (string line in lines)
            {
                // Use "," as delimeter to break line into an array of ints
                string[] words = line.Split(',');

                // Create new Record
                Record rec = new Record();

                rec.setAll(words);         // set all properties

                // Add Record to List
                recList.Add(rec);
            }

            // Convert recList to iQueryable
            var source = recList.AsQueryable<Record>();
            var agent = new PINQAgentBudget(5.0);

            var db = new PINQueryable<Record>(source, agent);

            distTotalRev(db, source);                                                       // Chart 1 (4 figures)      (e = 1)
            Console.WriteLine("=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=\n");
            distLocalRev(db, source);                                                       // Chart 2 (4 figures)      (e = 1)
            Console.WriteLine("=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=\n");
            distExpenses(db, source);                                                       // Chart 3 (4 figures)      (e = 1)
            Console.WriteLine("=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=\n");
            debt(db, source);                                                               // Figure 1, 2, and 3       (e = 0.5)
            Console.WriteLine("=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=\n");
            costPerStudent(db, source);                                                     // Figure 4                 (e = 0.2)       
            highEnrollment(db, source);                                                     // Figure 5,6               (e = 0.4)
            lowEnrollment(db, source);                                                      // Figure 7,8               (e = 0.4)
            Console.WriteLine("=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=+~+=\n");
            teachSalary(db, source);                                                        // Figure 9, 10, 11         (e = 0.5)

            Console.ReadLine(); //Pause
        }

        public static void distTotalRev(PINQueryable<Record> db, IQueryable<Record> source)
        {

            // Get average total revenue
            Expression<Func<Record, double>> total = x => x.totalRev / 1000000;
            double totalRev = db.NoisyAverage(0.25, total) * 1000000;
            double cleanTotalRev = source.Average(total) * 1000000;
            Console.WriteLine("Noisy Average Total Revenue = " + totalRev + "\t\t\t\tClean: " + cleanTotalRev);

            // Get average revenue from federal sources
            Expression<Func<Record, double>> federal = x => x.totalFedRev / 1000000;
            double totalRevFromFed = db.NoisyAverage(0.25, federal) * 1000000;
            double cleanTotalRevFromFed = source.Average(federal) * 1000000;
            Console.WriteLine("Noisy Total Federal Revenue = " + totalRevFromFed + "\t\t\t\tClean: " + cleanTotalRevFromFed);

            // Get average revenue from state sources
            Expression<Func<Record, double>> state = x => x.totalStateRev / 1000000;
            double totalRevFromState = db.NoisyAverage(0.25, state) * 1000000;
            double cleanTotalRevFromState = source.Average(state) * 1000000;
            Console.WriteLine("Noisy Total State Revenue = " + totalRevFromState + "\t\t\t\tClean: " + cleanTotalRevFromState);

            // Get average revenue from local sources
            Expression<Func<Record, double>> local = x => x.totalLocalRev / 1000000;
            double totalRevFromLocal = db.NoisyAverage(0.25, local) * 1000000;
            double cleanTotalRevFromLocal = source.Average(local) * 1000000;
            Console.WriteLine("Noisy Total Local Revenue = " + totalRevFromLocal + "\t\t\t\tClean: " + cleanTotalRevFromLocal);

            Console.WriteLine("\nNoisy Percent of TotalRev from Federal Sources: " + (100 * totalRevFromFed / totalRev) + "% \tClean: " + (100 * cleanTotalRevFromFed / cleanTotalRev) + "%");
            Console.WriteLine("Noisy Percent of TotalRev from State Sources: " + (100 * totalRevFromState / totalRev) + "% \tClean: " + (100 * cleanTotalRevFromState / cleanTotalRev) + "%");
            Console.WriteLine("Noisy Percent of TotalRev from Local Sources: " + (100 * totalRevFromLocal / totalRev) + "% \tClean: " + (100 * cleanTotalRevFromLocal / cleanTotalRev) + "%");
        }
        public static void distLocalRev(PINQueryable<Record> db, IQueryable<Record> source)
        {
            // Get average revenue from local sources
            Expression<Func<Record, double>> local = x => x.totalLocalRev / 100000;
            double totalRevFromLocal = db.NoisyAverage(0.25, local) * 100000;
            double cleanTotalRevFromLocal = source.Average(local) * 100000;
            Console.WriteLine("Noisy Total Local Revenue = " + totalRevFromLocal + "\t\t\t\tClean: " + cleanTotalRevFromLocal);

            // Get average revenue from taxes
            Expression<Func<Record, double>> taxes = x => x.revTaxes / 100000;
            double revTax = db.NoisyAverage(0.25, taxes) * 100000;
            double cleanRevTax = source.Average(taxes) * 100000;
            Console.WriteLine("Noisy Total Revenue From Taxes = " + revTax + "\t\t\tClean: " + cleanRevTax);

            // Get average revenue from charges (tuition, fines, etc.)
            Expression<Func<Record, double>> charges = x => x.revCharges / 100000;
            double revCharges = db.NoisyAverage(0.25, charges) * 100000;
            double cleanRevCharges = source.Average(charges) * 100000;
            Console.WriteLine("Noisy Total Revenue From Charges = " + revCharges + "\t\t\tClean: " + cleanRevCharges);

            // Get average revenue from state sources
            Expression<Func<Record, double>> other = x => x.revOther / 100000;
            double revOther = db.NoisyAverage(0.25, other) * 100000;
            double cleanRevOther = source.Average(other) * 100000;
            Console.WriteLine("Noisy Total Revenue From Other = " + revOther + "\t\t\tClean: " + cleanRevOther);

            Console.WriteLine("\nNoisy Percent of Local Revenue from Taxes: " + (100 * revTax / totalRevFromLocal) + "%\t\tClean: " + (100 * cleanRevTax / cleanTotalRevFromLocal) + "%");
            Console.WriteLine("Noisy Percent of Local Revenue from Charges: " + (100 * revCharges / totalRevFromLocal) + "%\t\tClean: " + (100 * cleanRevCharges / cleanTotalRevFromLocal) + "%");
            Console.WriteLine("Noisy Percent of Local Revenue from Other: " + (100 * revOther / totalRevFromLocal) + "%\t\tClean: " + (100 * cleanRevOther / cleanTotalRevFromLocal) + "%");
        }
        public static void distExpenses(PINQueryable<Record> db, IQueryable<Record> source)
        {
            // Get average total expenses
            Expression<Func<Record, double>> totalExp = x => x.totalExp / 1000000;
            double totalExpenses = db.NoisyAverage(0.25, totalExp) * 1000000;
            double cleanTotalExpenses = source.Average(totalExp) * 1000000;
            Console.WriteLine("Noisy Total Expenses = " + totalExpenses + "\t\t\t\t\tClean: " + cleanTotalExpenses);

            // Get average expenses on instruction (Salaries + supplies)
            Expression<Func<Record, double>> inst = x => x.expInstr / 1000000;
            double expInstr = db.NoisyAverage(0.25, inst) * 1000000;
            double cleanExpInstr = source.Average(inst) * 1000000;
            Console.WriteLine("Noisy Total Expenses on Instruction = " + expInstr + "\t\t\tClean: " + cleanExpInstr);

            // Get average expenses on support services (Admin, transportation, maitenence)
            Expression<Func<Record, double>> ssvc = x => x.expSscv / 1000000;
            double expSsvc = db.NoisyAverage(0.25, ssvc) * 1000000;
            double cleanExpSsvc = source.Average(ssvc) * 1000000;
            Console.WriteLine("Noisy Total Expenses on Support Services = " + expSsvc + "\t\tClean: " + cleanExpSsvc);

            // Get average expenses on other
            Expression<Func<Record, double>> other = x => x.expOther / 1000000;
            double expOther = db.NoisyAverage(0.25, other) * 1000000;
            double cleanExpOther = source.Average(other) * 1000000;
            Console.WriteLine("Noisy Total Expenses on Other = " + expOther + "\t\t\tClean: " + cleanExpOther);

            Console.WriteLine("\nNoisy Percent of Expenses on Intruction: " + (100 * expInstr / totalExpenses) + "%\t\tClean: " + (100 * cleanExpInstr / cleanTotalExpenses) + "%");
            Console.WriteLine("Noisy Percent of Expenses on Support Services: " + (100 * expSsvc / totalExpenses) + "%\tClean: " + (100 * cleanExpSsvc / cleanTotalExpenses) + "%");
            Console.WriteLine("Noisy Percent of Expenses on Other: " + (100 * expOther / totalExpenses) + "%\t\t\tClean: " + (100 * cleanExpOther / cleanTotalExpenses) + "%");
        }
        public static void debt(PINQueryable<Record> db, IQueryable<Record> source)
        {
            // Get average debt. Omit records with no debt?
            var q1 = db.Where(x => x.debt != 0);
            var q2 = source.Where(x => x.debt != 0);

            Expression<Func<Record, double>> debt = x => x.debt / 10000;     // Divide by 10000 to ensure it is within the [-1,+1] range
            double avgDebt = q1.NoisyAverage(0.2, debt) * 10000;
            double cleanAvgDebt = q2.Average(debt) * 10000;

            Console.WriteLine("Noisy Schools with Debt: " + q1.NoisyCount(0.1) + "\t\t\t\tClean: " + q2.Count());
            Console.WriteLine("Noisy Average Debt: $" + (1000 * avgDebt) + "\t\t\t\t\tClean: $" + (1000 * cleanAvgDebt));
  
            double totalDebt = db.NoisySum(0.2, debt) * 10000;
            double cleanTotalDebt = source.Sum(debt) * 10000;

            Console.WriteLine("Total Debt: $" + (1000 * totalDebt) + "\t\t\t\t\t\tClean: $" + (1000 * cleanTotalDebt));
        }
        public static void costPerStudent(PINQueryable<Record> db, IQueryable<Record> source)
        {
            // Get average cost per Student
            Expression<Func<Record, double>> perStud = x => x.CostPerStudent / 1000;     // Divide by 1000 to ensure it is within the [-1,+1] range
            double costPerStud = db.NoisyAverage(0.2, perStud) * 1000;
            double cleanCostPerStud = source.Average(perStud) * 1000;

            Console.WriteLine("Noisy Average Amount Spent per Student: $" + (1000 * costPerStud) + "\t\tClean: $" + (1000 * cleanCostPerStud));       // Undo our hashing function above, then costPerStudent is in thousands of dollars.
        }
        public static void highEnrollment(PINQueryable<Record> db, IQueryable<Record> source)
        {

            // Districts with 25K+ students
            var q1 = db.Where(x => x.enrollment > 25000);
            var q2 = source.Where(x => x.enrollment > 25000);
           
            Expression<Func<Record, double>> highEnr= x => x.CostPerStudent / 10000;     // Divide by 10000 to ensure it is within the [-1,+1] range
            double avgCost = db.NoisyAverage(0.2, highEnr) * 10000;
            double cleanAvgCost = q2.Average(highEnr) * 10000;

            Console.WriteLine("\nNoisy # districts w/ 25k+ students: " + q1.NoisyCount(0.2) + "\t\t\tClean: " + q2.Count());
            Console.WriteLine("Noisy Amount Spent per Student: " + (1000 * avgCost) + "\t\t\tClean: " + (1000 * cleanAvgCost));
        }
        public static void lowEnrollment(PINQueryable<Record> db, IQueryable<Record> source)
        {

            // Districts with less than 1K students
            var q1 = db.Where(x => ((x.enrollment < 1000) && (x.enrollment > 0)));
            var q2 = source.Where(x => ((x.enrollment < 1000) && (x.enrollment > 0)));

            Expression<Func<Record, double>> lowEnr = x => x.CostPerStudent / 10000;     // Divide by 10000 to ensure it is within the [-1,+1] range
            double avgCost = db.NoisyAverage(0.2, lowEnr) * 10000;
            double cleanAvgCost = q2.Average(lowEnr) * 10000;

            Console.WriteLine("Noisy # districts w/ Less than 3K students: " + q1.NoisyCount(0.2) + "\t\tClean: " + q2.Count());
            Console.WriteLine("Noisy Amount Spent per Student: " + (1000 * avgCost) + "\t\t\tClean: " + (1000 * cleanAvgCost));
        }
        public static void teachSalary(PINQueryable<Record> db, IQueryable<Record> source)
        {

            // Districts with 20K+ students
            var highEnr = db.Where(x => x.enrollment > 20000);
            var cleanHighEnr = source.Where(x => x.enrollment > 20000);

            // Districts with less than 20K but more than 3K students
            var midEnr = db.Where(x => ((x.enrollment > 3000) && (x.enrollment < 20000)));
            var cleanMidEnr = source.Where(x => ((x.enrollment > 3000) && (x.enrollment < 20000)));

            // Districts with less than 3K students
            var lowEnr = db.Where(x => ((x.enrollment < 3000) && (x.enrollment > 0)));
            var cleanLowEnr = source.Where(x => ((x.enrollment < 3000) && (x.enrollment > 0)));

            Expression<Func<Record, double>> pps = x => x.TeachPayPerStudent;
            double highPPS = highEnr.NoisyAverage(0.17, pps);
            double cleanHighPPS = cleanHighEnr.Average(pps);

            double midPPS = midEnr.NoisyAverage(0.16, pps);
            double cleanMidPPS = cleanMidEnr.Average(pps);

            double lowPPS = lowEnr.NoisyAverage(0.17, pps);
            double cleanLowPPS = cleanLowEnr.Average(pps);

            Console.WriteLine("Average Teacher Pay Per Student (25K+ students): " + highPPS + "\t\tClean: " + cleanHighPPS);
            Console.WriteLine("Average Teahcer Pay Per Student (3K < x > 25K): " + midPPS + "\t\tClean: " + cleanMidPPS);
            Console.WriteLine("Average Teahcer Pay Per Student (> 3K students): " + lowPPS + "\t\tClean: " + cleanLowPPS);
        }
    } // End class DBquery
} // End namespace