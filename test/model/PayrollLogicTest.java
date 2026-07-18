package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PayrollLogicTest {

    private static final double DELTA = 0.001;
    private final PayrollLogic payroll = new PayrollLogic();

    @Test
    @DisplayName("UT-18: Gross weekly salary includes work and allowances")
    void calculateGrossWeeklySalary_includesWorkAndAllowances() {
        double actual = payroll.calculateGrossWeeklySalary(100, 40, 500, 250, 125);

        assertEquals(4875.00, actual, DELTA,
                "Gross pay should include hourly pay and all three allowances");
    }

    @Test
    @DisplayName("UT-19: Net weekly salary subtracts one fourth of monthly deductions")
    void calculateNetWeeklySalary_subtractsQuarterOfMonthlyDeductions() {
        double actual = payroll.calculateNetWeeklySalary(30000, 4875);

        assertEquals(4081.65, actual, DELTA,
                "Net weekly pay should subtract one fourth of current monthly deductions");
    }

    @Test
    @DisplayName("UT-20: Gross weekly salary works without allowances")
    void calculateGrossWeeklySalary_withoutAllowances() {
        double actual = payroll.calculateGrossWeeklySalary(100, 40, 0, 0, 0);

        assertEquals(4000.00, actual, DELTA,
                "Gross pay should equal hourly pay when all allowances are zero");
    }

    @Test
    @DisplayName("UT-21: Late deduction uses the per-minute hourly rate")
    void calculateLateDeduction_usesPerMinuteRate() {
        double actual = payroll.calculateLateDeduction(120, 30);

        assertEquals(60.00, actual, DELTA,
                "Thirty late minutes at PHP 120 per hour should deduct PHP 60");
    }
}
