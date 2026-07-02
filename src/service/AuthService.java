/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.EmployeeDAO;

public class AuthService {
    private final EmployeeDAO employeeDAO;

    public AuthService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public boolean userExists(String username) {
        return employeeDAO.exists(username);
    }

    public boolean addUser(String username, String password) {
        return employeeDAO.save(username, password);
    }

    public boolean updateUserPassword(String username, String password) {
        return employeeDAO.updatePassword(username, password);
    }
}