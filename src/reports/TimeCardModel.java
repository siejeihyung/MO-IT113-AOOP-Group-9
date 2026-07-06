/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package reports;

import java.util.Date;

public class TimeCardModel {
    private Date date;
    private String day;
    private String timeIn;
    private String breakOut;
    private String breakIn;
    private String timeOut;
    private String totalHoursWorked;
    private String remarks;

    public TimeCardModel(Date date, String day, String timeIn, String breakOut, 
                         String breakIn, String timeOut, String totalHoursWorked, String remarks) {
        this.date = date;
        this.day = day;
        this.timeIn = timeIn;
        this.breakOut = breakOut;
        this.breakIn = breakIn;
        this.timeOut = timeOut;
        this.totalHoursWorked = totalHoursWorked;
        this.remarks = remarks;
    }

    // Getters for JasperReports
    public Date getDate() { return date; }
    public String getDay() { return day; }
    public String getTimeIn() { return timeIn; }
    public String getBreakOut() { return breakOut; }
    public String getBreakIn() { return breakIn; }
    public String getTimeOut() { return timeOut; }
    public String getTotalHoursWorked() { return totalHoursWorked; }
    public String getRemarks() { return remarks; }
}
