# MotorPH Payroll System - Database Migration

## Overview
This milestone marks the successful migration of the MotorPH Payroll System from a legacy CSV-based flat-file storage system to a robust, relational database architecture using MySQL and JDBC. This transition ensures data integrity, enhances security through RBAC, and lays the foundation for enterprise-level payroll processing.

## Key Changes
- **Database Schema**: Transitioned from raw CSV data to a normalized MySQL schema (Tables: `Employee`, `Role`, `UserAccount`, `UserRole`, `EmployeeDetails`).
- **Persistence Layer**: Implemented the DAO (Data Access Object) pattern. All data operations are now handled via `DatabaseConnection` and JDBC, replacing `FileHandler` logic.
- **Authentication**: Refactored the login system to authenticate against a database-backed `UserAccount` table, supporting Role-Based Access Control (RBAC).
- **Driver Integration**: Configured MySQL Connector/J in the project classpath for reliable database communication.
- **Model Upgrades**: Enhanced `Employee` model and `RegularEmployee` subclass to support comprehensive payroll fields including SSS, PhilHealth, TIN, and Pag-IBIG.

## Technical Stack
- **Database**: MySQL 8.0
- **Connectivity**: Java Database Connectivity (JDBC)
- **IDE**: Apache NetBeans
- **Driver**: MySQL Connector/J

## Accomplishments
- [x] Established `motorph_payroll` database schema.
- [x] Implemented JDBC connection utility for centralized configuration.
- [x] Migrated authentication service to query live database records.
- [x] Updated GUI-to-DAO communication to populate dashboards using real-time SQL queries.

## Next Steps
- Implement full CRUD (Create, Read, Update, Delete) operations in `EmployeeDAO`.
- Migrate attendance and salary history data from legacy files.
- Optimize payroll computation logic using the new database-backed models.

---
*Developed by: Group 9 (A2101)*
