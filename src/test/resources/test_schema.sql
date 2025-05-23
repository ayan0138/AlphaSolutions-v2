-- TESTDATA UNITTIL TESTMILJØ (H2 + JDBC)
-- Må IKKE bruges i produktion eller udvikling

-- ========== SCHEMA ==========

CREATE TABLE roles (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);


CREATE TABLE projects (
    project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE project_assignments (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_role VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

CREATE TABLE sub_projects (
    sub_project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    price DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

CREATE TABLE tasks (
task_id BIGINT PRIMARY KEY AUTO_INCREMENT,
sub_project_id BIGINT NOT NULL,
name VARCHAR(255) NOT NULL,
description TEXT,
assigned_to BIGINT,
status VARCHAR(100),
due_date DATE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
price DECIMAL(10,2) DEFAULT 0.00,
estimated_hours DOUBLE,
hourly_rate DOUBLE,
FOREIGN KEY (sub_project_id) REFERENCES sub_projects(sub_project_id) ON DELETE CASCADE,
FOREIGN KEY (assigned_to) REFERENCES users(user_id) ON DELETE SET NULL
);
