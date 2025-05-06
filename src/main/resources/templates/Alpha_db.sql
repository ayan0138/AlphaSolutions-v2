-- Opret database
CREATE DATABASE IF NOT EXISTS alpha_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alpha_db;

-- Roller
CREATE TABLE roles (
                       role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       role_name VARCHAR(255) NOT NULL
);

-- Brugere
CREATE TABLE users (
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role_id BIGINT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE SET NULL
);

-- Projekter
CREATE TABLE projects (
                        project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        start_date DATE,
                        end_date DATE,
                        created_by BIGINT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Tildelinger
CREATE TABLE project_assignments (
                        assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        project_id BIGINT NOT NULL,
                        project_role VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                        FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE
);

-- Underprojekter
CREATE TABLE sub_projects (
                        sub_project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        project_id BIGINT NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        start_date DATE,
                        end_date DATE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE
);

-- Opgaver (tasks)
CREATE TABLE tasks (
                       task_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       sub_project_id BIGINT NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       assigned_to BIGINT,
                       status VARCHAR(100),
                       due_date DATE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (sub_project_id) REFERENCES sub_projects(sub_project_id) ON DELETE CASCADE,
                       FOREIGN KEY (assigned_to) REFERENCES users(user_id) ON DELETE SET NULL
);

