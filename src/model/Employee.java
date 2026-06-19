package model;

/**
 * The SINGLE Parent Class for all types of Employees.
 * Implements IPayable to ensure every employee has a calculation for gross pay.
 */

/**
 *
 * @author SunnyEljohn
 */
// Inside Employee.java

public abstract class Employee implements IPayable {

    // 1. Identity Fields: Protected so subclasses can access them directly
    protected String employeeID;
    protected String lastName;
    protected String firstName;
    protected String sss;
    protected String philhealth;
    protected String tin;
    protected String pagibig;

    // 2. Salary Fields: Common to all employee types
    protected double basicSalary;
    protected double semiMonthlyRate;
    protected double hourlyRate;
    protected double totalBenefits;

    // 3. Merged Constructor: Initializes the common state of any employee
    public Employee(String employeeID, String lastName, String firstName,
                String sss, String philhealth, String tin, String pagibig,
                double basicSalary, double semiMonthlyRate, 
                double hourlyRate, double totalBenefits) {
        this.employeeID = employeeID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.sss = sss;
        this.philhealth = philhealth;
        this.tin = tin;
        this.pagibig = pagibig;
        this.basicSalary = basicSalary;
        this.semiMonthlyRate = semiMonthlyRate;
        this.hourlyRate = hourlyRate;
        this.totalBenefits = totalBenefits;
    }
   
    // 5. GETTERS: Controlled way to read data
    public String getEmployeeID() { return employeeID; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getSSS() { return sss; }
    public String getPhilhealth() { return philhealth; }
    public String getTin() { return tin; }
    public String getPagibig() { return pagibig; }
    
    public double getBasicSalary() { return basicSalary; } // Common Concrete Methods: Standard logic used by all subclasses
    public double getSemiMonthlyRate() { return semiMonthlyRate; }
    public double getHourlyRate() { return hourlyRate; }
    public double getTotalBenefits() { return totalBenefits; } // Common Concrete Methods: Standard logic used by all subclasses
    
    // 6. SETTERS: Controlled way to update data with validation logic
    public void setBasicSalary(double basicSalary) {
        if (basicSalary > 0) { // Example validation
            this.basicSalary = basicSalary;
        }
    }

    public void setLastName(String lastName) {
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName;
        }
    }

    public void setTotalBenefits(double totalBenefits) {
        this.totalBenefits = totalBenefits;
    }

    // 7. The Abstract Method: This is the OOP Abstraction
    public abstract String getEmployeeType();
    
    @Override
    public abstract double calculateGrossPay();
}