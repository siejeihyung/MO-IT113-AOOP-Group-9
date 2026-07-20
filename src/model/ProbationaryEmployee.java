package model;

/**
 * MotorPH Probationary Employee Subclass.
 * Second concrete subclass of Employee - demonstrates runtime polymorphism.
 *
 * BUSINESS RULE (confirm with the team): probationary employees are not yet
 * entitled to the full allowance package.
 */
public class ProbationaryEmployee extends Employee {

    public static final double BENEFIT_ELIGIBILITY_RATE = 0.50;
    public static final int DEFAULT_PROBATION_MONTHS = 6;

    private int probationMonths;

    public ProbationaryEmployee(String employeeID, String lastName, String firstName,
                                String sss, String philhealth, String tin, String pagibig,
                                double basicSalary, double semiMonthlyRate,
                                double hourlyRate, double totalBenefits) {
        this(employeeID, lastName, firstName, sss, philhealth, tin, pagibig,
             basicSalary, semiMonthlyRate, hourlyRate, totalBenefits,
             DEFAULT_PROBATION_MONTHS);
    }

    public ProbationaryEmployee(String employeeID, String lastName, String firstName,
                                String sss, String philhealth, String tin, String pagibig,
                                double basicSalary, double semiMonthlyRate,
                                double hourlyRate, double totalBenefits,
                                int probationMonths) {
        super(employeeID, lastName, firstName, sss, philhealth, tin, pagibig,
              basicSalary, semiMonthlyRate, hourlyRate, totalBenefits);
        setProbationMonths(probationMonths);
    }

    public int getProbationMonths() {
        return probationMonths;
    }

    public final void setProbationMonths(int probationMonths) {
        this.probationMonths = (probationMonths > 0)
                ? probationMonths
                : DEFAULT_PROBATION_MONTHS;
    }

    public double getEligibleBenefits() {
        return getTotalBenefits() * BENEFIT_ELIGIBILITY_RATE;
    }

    @Override
    public double calculateGrossPay() {
        return getBasicSalary() + getEligibleBenefits();
    }

    @Override
    public String getEmployeeType() {
        return "Probationary";
    }
}