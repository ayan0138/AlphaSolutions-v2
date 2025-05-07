-- Roller
INSERT INTO Roles (role_name) VALUES
        ('Admin'), ('Projektleder'), ('Medarbejder');

-- Brugere
INSERT INTO Users (user_id, username, email, password, role_id) VALUES
        (1, 'admin', 'admin@test.com', 'admin123', 1),
        (2, 'line', 'line@test.com', 'line123', 2),
        (3, 'kasper', 'kasper@test.com', 'kasper123', 3);

-- Projekter
INSERT INTO Projects (name, description, start_date, end_date, created_by) VALUES
        ('Testprojekt A', 'Et testprojekt med underprojekter',
         '2025-05-01', '2025-06-01', 1),
        ('Testprojekt B', 'Et simpelt testprojekt',
         '2025-05-10', '2025-06-15', 2);

-- Projekt-Tildelinger
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
        (1, 'UI Review', 'Få feedback fra teamet',
         2, 'Afventer', '2025-05-05'),
        (2, 'Opsæt database', 'MySQL-struktur og testdata',
         3, 'Ikke startet', '2025-05-12'),
        (2, 'Byg login-system', 'Spring Boot + session',
         3, 'I gang', '2025-05-20');