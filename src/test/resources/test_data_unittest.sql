-- TESTDATA UNITTIL TESTMILJØ (H2 + JDBC)
-- Må IKKE bruges i produktion eller udvikling

-- ========== SCHEMA ==========

CREATE TABLE Roles (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(255) NOT NULL
);

CREATE TABLE Users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

CREATE TABLE Projects (
    project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

CREATE TABLE Project_Assignments (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_role VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (project_id) REFERENCES Projects(project_id)
);

CREATE TABLE Sub_Projects (
    sub_project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES Projects(project_id)
);

CREATE TABLE Tasks (
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sub_project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_to BIGINT,
    status VARCHAR(100),
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sub_project_id) REFERENCES Sub_Projects(sub_project_id),
    FOREIGN KEY (assigned_to) REFERENCES Users(user_id)
);

-- ========== TESTDATA ==========

-- Roller
INSERT INTO Roles (role_name) VALUES
    ('Admin'), ('Projektleder'), ('Medarbejder');

-- Brugere (med hashed adgangskoder)
INSERT INTO Users (username, email, password, role_id) VALUES
    ('admin', 'admin@test.com',
     '$2a$10$7YVxkPo4uTwqZ7U7v7gfEe28pBOZJupAfAaJ67d3AH2aygk07L3Ya', 1),
    ('marcus', 'marcus@test.com',
     '$2a$10$6bIUfncfPjIjMyLXiJWiqe4NO6BAaJ6q2qowcAf1IoTrdoqL3NgX2', 2),
    ('najib', 'najib@test.com',
     '$2a$10$gChbqzqvU1YEC8V7edTFneQv5j1Mc03QO9.A0jOkFjIXZ.MUOr1vS', 3);

-- Projekter
INSERT INTO Projects (name, description, start_date, end_date, created_by) VALUES
    ('Testprojekt A', 'Et testprojekt med underprojekter',
     '2025-05-01', '2025-06-01', 1),
    ('Testprojekt B', 'Et simpelt testprojekt',
     '2025-05-10', '2025-06-15', 2);

-- Tildelinger
INSERT INTO Project_Assignments (user_id, project_id, project_role) VALUES
    (2, 1, 'Projektleder'),
    (3, 1, 'Udvikler');

-- Underprojekter
INSERT INTO Sub_Projects (project_id, name, description, start_date, end_date) VALUES
    (1, 'Designfase', 'Skab layout og UI',
     '2025-05-01', '2025-05-10'),
    (1, 'Udviklingsfase', 'Implementér backend og frontend',
     '2025-05-11', '2025-06-01');

-- Tasks
INSERT INTO Tasks (sub_project_id, name, description, assigned_to, status, due_date) VALUES
    (1, 'Sketch mockups', 'Lav wireframes til layout',
     3, 'I gang', '2025-05-03'),
    (1, 'UI Review', 'Få feedback på design',
     3, 'Afventer', '2025-05-05');
