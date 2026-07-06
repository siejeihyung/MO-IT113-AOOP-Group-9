/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package reports;

public class PayslipModel {
    private String payslipNo;
    private String periodStartDate;
    private String periodEndDate;
    private String employeeId;
    private String employeeName;
    private String positionDepartment;
    private Double monthlySalary;
    private Double dailyRate;
    private Integer daysWorked;
    private Double overtime;
    private Double grossIncome;
    private Double riceSubsidy;
    private Double phoneAllowance;
    private Double clothingAllowance;
    private Double totalBenefits;
    private Double sss;
    private Double philhealth;
    private Double pagIbig;
    private Double withholdingTax;
    private Double totalDeductions;
    private Double takeHomePay;

    // Constructor to easily create a new payslip record
    public PayslipModel(String payslipNo, String employeeName, Double takeHomePay /* add others here */) {
        this.payslipNo = payslipNo;
        this.employeeName = employeeName;
        this.takeHomePay = takeHomePay;
        // Assign other fields here...
    }

    // IMPORTANT: JasperReports uses these getter methods to find the data.
    // Ensure the method names match your field names (e.g., getPayslipNo matches field 'payslipNo').
    
    public String getPayslipNo() { return payslipNo; }
    public String getPeriodStartDate() { return periodStartDate; }
    public String getPeriodEndDate() { return periodEndDate; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getPositionDepartment() { return positionDepartment; }
    public Double getMonthlySalary() { return monthlySalary; }
    public Double getDailyRate() { return dailyRate; }
    public Integer getDaysWorked() { return daysWorked; }
    public Double getOvertime() { return overtime; }
    public Double getGrossIncome() { return grossIncome; }
    public Double getRiceSubsidy() { return riceSubsidy; }
    public Double getPhoneAllowance() { return phoneAllowance; }
    public Double getClothingAllowance() { return clothingAllowance; }
    public Double getTotalBenefits() { return totalBenefits; }
    public Double getSss() { return sss; }
    public Double getPhilhealth() { return philhealth; }
    public Double getPagIbig() { return pagIbig; }
    public Double getWithholdingTax() { return withholdingTax; }
    public Double getTotalDeductions() { return totalDeductions; }
    public Double getTakeHomePay() { return takeHomePay; }
}
