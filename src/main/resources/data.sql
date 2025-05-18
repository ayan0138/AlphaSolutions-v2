-- Indsæt roller
INSERT INTO roles (role_name) VALUES
        ('ADMIN'),
        ('PROJEKTLEDER'),
        ('MEDARBEJDER');

-- Indsæt brugere (med BCrypt-hashede adgangskoder – udskift evt. med egne)
INSERT INTO users (username, email, password, role_id) VALUES
        ('admin', 'admin@example.com',
         '$2a$10$SMHANoSd/3je9FeY7uEFkuXMdEEpegi745iiufQjo5piYi9ohEQ3W', 1),
        ('marcus', 'marcus@firma.dk',
         '$2a$10$DCpiidKn5AqJCcQG3RlokeZN1UegrHb1gbqYruVgisNj6OJdHNWWW', 2),
        ('najib', 'najib@firma.dk',
         '$2a$10$lwJ26Zp9fytdc/bXmvs6bugcxBThP6iEOW7CCSMcKeGUe72F7oSdq', 3);

-- Indæst projekter
INSERT INTO projects (name, description, start_date, end_date, created_by) VALUES
        ('Website Redesign', 'Opdatering af firmaets hjemmeside',
         '2025-05-01', '2025-06-15', 1),
        ('Kampagne 2025', 'Marketingkampagne for Q3',
         '2025-05-10', '2025-07-01', 2);

-- Indsæt projekt-tilknytninger
INSERT INTO project_assignments (user_id, project_id, project_role) VALUES
        (2, 1, 'Projektleder'),
        (3, 1, 'Udvikler'),
        (3, 2, 'Designer');

-- Indsæt subprojekter
INSERT INTO sub_projects (project_id, name, description, start_date, end_date, price) VALUES
         (1, 'Designfase', 'Design af nye hjemmesidelayouts',
          '2025-05-01', '2025-05-15', 0.00),
        (1, 'Udviklingsfase', 'Kodning og implementering',
         '2025-05-16', '2025-06-10', 0.00),
        (2, 'Kampagnemateriale', 'Udarbejdelse af grafik og tekster',
         '2025-05-10', '2025-06-01', 0.00);

-- Indsæt opgaver (Tasks)
INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date, price, estimated_hours, hourly_rate) VALUES
(1, 'Layout forslag', 'Lav 3 forskellige layout forslag',
 3, 'Ikke påbegyndt', '2025-05-05', 800.00, 10.0, 80.0),
(2, 'Implementering', 'Kodning af frontend i HTML/CSS',
 3, 'I gang', '2025-06-01', 1200.00, 12.0, 100.0),
(3, 'Grafik', 'Design af bannere og ikoner',
3, 'Afventer', '2025-05-20', 600.00, 6.0, 100.0);

