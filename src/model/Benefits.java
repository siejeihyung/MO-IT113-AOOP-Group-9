package model;

// This class represents the non-monetary benefits or allowances provided to an employee
public class Benefits {

    // Rice subsidy allowance for the employee
    private double riceSubsidy;

    // Phone allowance provided to the employee
    private double phoneAllowance;

    // Clothing allowance given to the employee
    private double clothingAllowance;

    // Constructor to initialize all benefit values
    public Benefits(double riceSubsidy, double phoneAllowance, double clothingAllowance) {
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
    }

    // Getters
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }

    // Setters
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
}
