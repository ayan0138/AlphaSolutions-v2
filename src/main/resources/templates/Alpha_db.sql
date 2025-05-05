-- Opret database
CREATE DATABASE IF NOT EXISTS alpha_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alpha_db;

-- Roller
CREATE TABLE Role (
                      role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      role_name VARCHAR(50) NOT NULL
);

-- Brugere
CREATE TABLE User (
                      user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(50) NOT NULL,
                      email VARCHAR(100) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      role_id BIGINT,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      FOREIGN KEY (role_id) REFERENCES Role(role_id)
);

-- Projekter
CREATE TABLE Project (
                         project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(100) NOT NULL,
                         description TEXT,
                         start_date DATE,
                         end_date DATE,
                         created_by BIGINT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (created_by) REFERENCES User(user_id)
);

-- Tildelinger
CREATE TABLE Project_Assignment (
                                    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT NOT NULL,
                                    project_id BIGINT NOT NULL,
                                    project_role VARCHAR(50),
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (user_id) REFERENCES User(user_id),
                                    FOREIGN KEY (project_id) REFERENCES Project(project_id)
);
