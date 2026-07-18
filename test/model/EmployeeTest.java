package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    private static final double DELTA = 0.001;

    @Test
    @DisplayName("UT-31: Employee rejects a negative basic salary")
    void setBasicSalary_rejectsNegativeValue() {
        RegularEmployee employee = new RegularEmployee(
                "10001", "Garcia", "Ana",
                "SSS-1", "PH-1", "TIN-1", "PI-1",
                30000, 15000, 180, 2500);

        employee.setBasicSalary(-5000);

        assertEquals(30000.00, employee.getBasicSalary(), DELTA,
                "A rejected negative salary must not replace the employee's current salary");
    }
}
