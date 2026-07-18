package service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dao.EmployeeDAO;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmployeeServiceTest {

    private static final String[] KNOWN_EMPLOYEE = {
        "10001", "Garcia", "Ana", "1995-04-12", "Quezon City"
    };

    private FakeEmployeeDAO employeeDAO;
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeDAO = new FakeEmployeeDAO();
        employeeService = new EmployeeService(employeeDAO);
    }

    @Test
    @DisplayName("UT-08: Read an employee using a valid ID")
    void getEmployeeById_validId_returnsKnownEmployee() {
        employeeDAO.employeeById = KNOWN_EMPLOYEE.clone();

        String[] actual = employeeService.getEmployeeById("10001");

        assertNotNull(actual, "A known employee ID should return a row");
        assertArrayEquals(KNOWN_EMPLOYEE, actual,
                "The service should return the row supplied by EmployeeDAO");
    }

    @Test
    @DisplayName("UT-09: Read an employee using an invalid ID")
    void getEmployeeById_invalidId_returnsNull() {
        employeeDAO.employeeById = null;

        String[] actual = employeeService.getEmployeeById("UNKNOWN");

        assertNull(actual, "An unknown employee ID should return null");
    }

    @Test
    @DisplayName("UT-13: Retrieve all employee rows")
    void getAllEmployees_returnsControlledRowsInOrder() {
        String[] first = {"10001", "Garcia", "Ana"};
        String[] second = {"10002", "Reyes", "Ben"};
        employeeDAO.allEmployees = List.of(first, second);

        List<String[]> actual = employeeService.getAllEmployees();

        assertNotNull(actual, "The employee list should not be null");
        assertEquals(2, actual.size(), "The service should return both DAO rows");
        assertEquals("10001", actual.get(0)[0], "The first employee ID should retain DAO order");
        assertEquals("10002", actual.get(1)[0], "The second employee ID should retain DAO order");
    }

    private static final class FakeEmployeeDAO extends EmployeeDAO {

        private String[] employeeById;
        private List<String[]> allEmployees = new ArrayList<>();

        @Override
        public String[] findRawById(String id) {
            return employeeById;
        }

        @Override
        public List<String[]> findAllRaw() {
            return allEmployees;
        }
    }
}
