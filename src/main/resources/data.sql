-- Indsæt roller
INSERT INTO roles (role_name) VALUES
        ('Admin'),
        ('Projektleder'),
        ('Medarbejder');

-- Indsæt brugere (med BCrypt-hashede adgangskoder – udskift evt. med egne)
INSERT INTO users (username, email, password, role_id) VALUES
        ('admin', 'admin@example.com',
         '$2a$10$7YVxkPo4uTwqZ7U7v7gfEe28pBOZJupAfAaJ67d3AH2aygk07L3Ya', 1),
        ('marcus', 'marcus@firma.dk',
         '$2a$10$6bIUfncfPjIjMyLXiJWiqe4NO6BAaJ6q2qowcAf1IoTrdoqL3NgX2', 2),
        ('najib', 'najib@firma.dk',
         '$2a$10$8BF8mJPPZHg5fR11iKCyMeCJvq7WrNh5L3Vdj2.yopYbdwvppI8vC', 3);

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
INSERT INTO sub_projects (project_id, name, description, start_date, end_date) VALUES
         (1, 'Designfase', 'Design af nye hjemmesidelayouts',
          '2025-05-01', '2025-05-15'),
        (1, 'Udviklingsfase', 'Kodning og implementering',
         '2025-05-16', '2025-06-10'),
        (2, 'Kampagnemateriale', 'Udarbejdelse af grafik og tekster',
         '2025-05-10', '2025-06-01');

-- Indsæt opgaver (Tasks)
INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date) VALUES
        (1, 'Layout forslag', 'Lav 3 forskellige layout forslag',
         3, 'Ikke påbegyndt', '2025-05-05'),
        (2, 'Implementering', 'Kodning af frontend i HTML/CSS',
         3, 'I gang', '2025-06-01'),
        (3, 'Grafik', 'Design af bannere og ikoner',
         3, 'Afventer', '2025-05-20');
