package model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeductionsTest {

    private static final double DELTA = 0.001;
    private final Deductions deductions = new Deductions();

    @Test
    @DisplayName("UT-22: SSS contribution follows the salary bracket")
    void calculateSSS_returnsContributionForSalaryBracket() {
        double actual = deductions.calculateSSS(10000);

        assertEquals(450.00, actual, DELTA,
                "A PHP 10,000 basic salary should use the PHP 450 SSS bracket");
    }

    @Test
    @DisplayName("UT-23: PhilHealth contribution observes minimum and maximum limits")
    void calculatePhilHealth_observesMinimumAndMaximum() {
        assertAll(
                () -> assertEquals(150.00, deductions.calculatePhilHealth(9000), DELTA,
                        "The employee share should use the minimum premium at low salary"),
                () -> assertEquals(900.00, deductions.calculatePhilHealth(70000), DELTA,
                        "The employee share should not exceed half of the maximum premium")
        );
    }

    @Test
    @DisplayName("UT-24: Pag-IBIG contribution is capped at PHP 100")
    void calculatePagIbig_capsContributionAtOneHundred() {
        double actual = deductions.calculatePagIbig(10000);

        assertEquals(100.00, actual, DELTA,
                "Pag-IBIG employee contribution should be capped at PHP 100");
    }
}
