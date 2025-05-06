-- Roller
INSERT INTO Roles (role_name) VALUES
('Admin'),
('Projektleder'),
('Medarbejder');

-- Brugere
INSERT INTO Users (username, email, password, role_id) VALUES
('admin', 'admin@example.com', 'admin123', 1),
('marcus', 'marcus@firma.dk', 'marcus123', 2),
('najib', 'najib@firma.dk', 'najib123', 3);

-- Projekter
INSERT INTO Projects (name, description, start_date, end_date, created_by) VALUES
('Website Redesign', 'Opdatering af firmaets hjemmeside', '2025-05-01', '2025-06-15', 1),
('Kampagne 2025', 'Marketingkampagne for Q3', '2025-05-10', '2025-07-01', 2);

-- Projekt-Tildelinger
INSERT INTO Project_Assignments (user_id, project_id, project_role) VALUES
(2, 1, 'Projektleder'),
(3, 1, 'Udvikler'),
(3, 2, 'Designer');