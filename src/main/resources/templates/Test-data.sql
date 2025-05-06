-- Roller
INSERT INTO roles (role_name) VALUES
                    ('Admin'),
                    ('Projektleder'),
                    ('Medarbejder');

-- Brugere
INSERT INTO users (username, email, password, role_id) VALUES
                    ('admin', 'admin@example.com', 'admin123', 1),
                    ('marcus', 'marcus@firma.dk', 'marcus123', 2),
                    ('najib', 'najib@firma.dk', 'najib123', 3);

-- Projekter
INSERT INTO projects (name, description, start_date, end_date, created_by) VALUES
                    ('Website Redesign', 'Opdatering af firmaets hjemmeside', '2025-05-01', '2025-06-15', 1),
                    ('Kampagne 2025', 'Marketingkampagne for Q3', '2025-05-10', '2025-07-01', 2);

-- Tildelinger
INSERT INTO project_assignments (user_id, project_id, project_role) VALUES
                    (2, 1, 'Projektleder'),
                    (3, 1, 'Udvikler'),
                    (3, 2, 'Designer');

-- Subprojekter
INSERT INTO sub_projects (project_id, name, description, start_date, end_date) VALUES
                    (1, 'Designfase', 'Design af nye hjemmesidelayouts', '2025-05-01', '2025-05-15'),
                    (1, 'Udviklingsfase', 'Kodning og implementering', '2025-05-16', '2025-06-10'),
                    (2, 'Kampagnemateriale', 'Udarbejdelse af grafik og tekster', '2025-05-10', '2025-06-01');

-- Opgaver (Tasks)
INSERT INTO tasks (sub_project_id, name, description, assigned_to, status, due_date) VALUES
                    (1, 'Lav wireframes', 'Skab grundlÃ¦ggende layoutskitser', 3, 'I gang', '2025-05-05'),
                    (1, 'Godkend design', 'FÃ¥ accept fra ledelsen', 2, 'Afventer', '2025-05-10'),
                    (2, 'Implementer forside', 'Kod HTML/CSS til forsiden', 3, 'Ikke startet', '2025-05-20'),
                    (3, 'Skriv kampagnetekst', 'Klar tekst til annoncer', 3, 'FÃ¦rdig', '2025-05-18');
