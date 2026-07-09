# MotorPH Payroll System - Database Migration

## Overview
This milestone marks the successful migration of the MotorPH Payroll System from a legacy CSV-based flat-file storage system to a robust, relational database architecture using MySQL and JDBC. This transition ensures data integrity, enhances security through RBAC, and lays the foundation for enterprise-level payroll processing.

## Functional Features
*   **Login Functionality:** Secure user authentication system.
*   **Role-Based Access Control (RBAC):** Permissions managed based on user roles.
*   **Employee Management:** Full CRUD (Create, Read, Update, and Delete) capabilities for employee records, restricted by user roles.
*   **Reporting Functionality (JasperReports):**
    *   Payslip generation for each employee.
    *   Comprehensive Payroll Reports.
    *   Time card generation.
    
## Key Changes
*   **Database Schema:** Transitioned from raw CSV data to a normalized MySQL schema (Tables: `Employee`, `Role`, `UserAccount`, `UserRole`, `EmployeeDetails`).
*   **Persistence Layer:** Implemented the DAO (Data Access Object) pattern. All data operations are now handled via `DatabaseConnection` and JDBC, replacing `FileHandler` logic.
*   **Authentication:** Refactored the login system to authenticate against a database-backed `UserAccount` table.
*   **Driver Integration:** Configured MySQL Connector/J in the project classpath for reliable database communication.
*   **Model Upgrades:** Enhanced `Employee` model and `RegularEmployee` subclass to support comprehensive payroll fields including SSS, PhilHealth, TIN, and Pag-IBIG.

## Technical Stack
*   **Database:** MySQL 8.0
*   **Connectivity:** Java Database Connectivity (JDBC)
*   **Reporting:** JasperReports
*   **IDE:** Apache NetBeans
*   **Driver:** MySQL Connector/J

## Accomplishments
*   Established `motorph_payroll` database schema.
*   Implemented JDBC connection utility for centralized configuration.
*   Migrated authentication service to query live database records.
*   Updated GUI-to-DAO communication to populate dashboards using real-time SQL queries.

  ## 🗺️ System Navigation & Testing Credentials

To review the JasperReports integration, use the following test credentials to log into their respective dashboards:

#### 💰 Payroll Summary & Payslip Reports
* **Role:** Finance
* **Username:** `finance`
* **Password:** `finance123`

#### ⏱️ TimeCard Reports
* **Role:** Human Resources (HR)
* **Username:** `hr`
* **Password:** `hr123`

### Other Credentials for CRUD Purposes
* **Role:** Employee
* **Username:** `[Employee ID]`
* **Password:** `[Employee's Surname]`

* **Role:** IT Support
* **Username:** `it_support`
* **Password:** `support123`

* * **Role:** Admin
* **Username:** `admin`
* **Password:** `1234`

## 👥 Project Team & Contributions

Our team is dedicated to transitioning our data infrastructure from legacy CSV files into a fully functional, optimized SQL database, complete with robust backend logic, polished GUI components, and dynamic JasperReports reporting.

## 🛠️ Core Team & Roles

| Member | Role | Primary Responsibilities |
| :--- | :--- | :--- |
| **Carl John Pontanilla** | Project Lead | • Spearheads the gradual transition and cleaning of legacy CSV files into a structured SQL database.<br>• Manages the implementation and generation of print-ready `.jrxml` files. |
| **Sunny Eljohn Lico** | Assistant Lead | • Directs backend optimization, refactoring code from OOP to Advanced OOP (AOOP).<br>• Integrates and fixes GUI components for JasperReports, ensuring seamless SQL-to-report compilation and clean `.jrxml` files. |
| **Isidro Romano** | Database Architecture Lead | • Architects and manages the overall SQL database schema.<br>• Ensures end-to-end database functionality, data integrity, and cross-platform performance. |
| **Anton Roger Galfo** | Architecture Design (GUI) Lead | • Designs and polishes the CRUD interfaces for all core modules: *HR, Finance, IT Support, Admin,* and *Employee*.<br>• Coordinates and executes internal system testing. |

## 📝 Documentation
* **All Team Members** contribute equally to fulfilling system requirements, writing technical documentation, and maintaining project records.

## 🚀 Next Steps

1. **JasperReports Optimization:** The development team will refine and enhance the formatting, data mapping, and visual layout of the generated report outputs.
2. **UI/UX Polishing:** The front-end team will clean up the user interface, specifically addressing layout sizing, responsiveness, and component alignment on the Employee Dashboard.
---
*Developed by: Group 9 (A2101)*
