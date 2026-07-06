/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package reports;

public class PayrollSummaryModel {
    private String employeeNo;
    private String employeeFullName;
    private String position;
    private String department;
    private Double grossIncome;
    private String sssNo;
    private Double sssContribution;
    private String philhealthNo;
    private Double philhealthContribution;
    private String pagIbigNo;
    private Double pagIbigContribution;
    private String tin;
    private Double withholdingTax;
    private Double netPay;

    public PayrollSummaryModel(String employeeNo, String employeeFullName, String position, String department, 
                               Double grossIncome, String sssNo, Double sssContribution, String philhealthNo, 
                               Double philhealthContribution, String pagIbigNo, Double pagIbigContribution, 
                               String tin, Double withholdingTax, Double netPay) {
        this.employeeNo = employeeNo;
        this.employeeFullName = employeeFullName;
        this.position = position;
        this.department = department;
        this.grossIncome = grossIncome;
        this.sssNo = sssNo;
        this.sssContribution = sssContribution;
        this.philhealthNo = philhealthNo;
        this.philhealthContribution = philhealthContribution;
        this.pagIbigNo = pagIbigNo;
        this.pagIbigContribution = pagIbigContribution;
        this.tin = tin;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
    }

    // Getters for JasperReports
    public String getEmployeeNo() { return employeeNo; }
    public String getEmployeeFullName() { return employeeFullName; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public Double getGrossIncome() { return grossIncome; }
    public String getSssNo() { return sssNo; }
    public Double getSssContribution() { return sssContribution; }
    public String getPhilhealthNo() { return philhealthNo; }
    public Double getPhilhealthContribution() { return philhealthContribution; }
    public String getPagIbigNo() { return pagIbigNo; }
    public Double getPagIbigContribution() { return pagIbigContribution; }
    public String getTin() { return tin; }
    public Double getWithholdingTax() { return withholdingTax; }
    public Double getNetPay() { return netPay; }
}
