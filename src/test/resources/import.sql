INSERT INTO roles (id, name) VALUES (1, 'ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'HR_MANAGER');
INSERT INTO roles (id, name) VALUES (3, 'CANDIDATE');
INSERT INTO roles (id, name) VALUES (4, 'INTERVIEWER');

INSERT INTO users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (1, 'admin', '$2a$10$GMxGu.0KX.u1gG5H5RrTj.cTzwQfaqGiEuyHWreaRr0A.IHEenilS', 'admin@recruit.com', 'System Administrator', TRUE, '2026-07-01T09:00:00', 1);
INSERT INTO users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (2, 'hrmanager', '$2a$10$r7MkMz1PXe3JEz.USSGYn.ApIs.rI6yVROPRKLZUxMJC9n/QHx4cG', 'hrmanager@recruit.com', 'HR Manager', TRUE, '2026-07-01T09:01:00', 2);
INSERT INTO users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (3, 'alice', '$2a$10$lrhTGzM3Lvp31lKh.89E5eNQ1ex1gErHRQuRiznLnywfLSNlsIxxG', 'alice@example.com', 'Alice Nguyen', TRUE, '2026-07-01T09:02:00', 3);
INSERT INTO users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (4, 'bob', '$2a$10$XyrNA8NFN0Ko6X34a8jEIu4mVTI1GgO7KTwiqxTT17pmA3aLwHZ7G', 'bob@example.com', 'Bob Tran', TRUE, '2026-07-01T09:03:00', 3);
INSERT INTO users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (5, 'carol', '$2a$10$Iahb6Eh0x0XLTJmcNkam8uLlRmej76zl8GW711KaGKpJh9U.f6r3u', 'carol@example.com', 'Carol Le', TRUE, '2026-07-01T09:04:00', 3);
INSERT INTO users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (6, 'david', '$2a$10$3y2GoAVWd9tJ24xcsevCSerbxfOLIwWCXPgSLLehu1x1FDgruasG6', 'david@example.com', 'David Pham', TRUE, '2026-07-01T09:05:00', 3);

INSERT INTO company_profiles (id, description, headquarters, employee_count, founded_year) VALUES (1, 'Cloud solutions leader', 'HCM', 500, 2010);
INSERT INTO company_profiles (id, description, headquarters, employee_count, founded_year) VALUES (2, 'Digital banking fintech', 'Ha Noi', 200, 2015);
INSERT INTO company_profiles (id, description, headquarters, employee_count, founded_year) VALUES (3, 'UX/UI agency', 'Da Nang', 80, 2018);

INSERT INTO companies (id, name, industry, website, profile_id) VALUES (1, 'TechCorp Inc.', 'Technology', 'techcorp.com', 1);
INSERT INTO companies (id, name, industry, website, profile_id) VALUES (2, 'FinanceHub Ltd.', 'Finance', 'financehub.com', 2);
INSERT INTO companies (id, name, industry, website, profile_id) VALUES (3, 'Creative Studio', 'Design', 'creativestudio.com', 3);

INSERT INTO skills (id, name, category) VALUES (1, 'Java', 'Backend');
INSERT INTO skills (id, name, category) VALUES (2, 'Spring Boot', 'Backend');
INSERT INTO skills (id, name, category) VALUES (3, 'PostgreSQL', 'Backend');
INSERT INTO skills (id, name, category) VALUES (4, 'Docker', 'Backend');
INSERT INTO skills (id, name, category) VALUES (5, 'React', 'Frontend');
INSERT INTO skills (id, name, category) VALUES (6, 'TypeScript', 'Frontend');
INSERT INTO skills (id, name, category) VALUES (7, 'CSS', 'Frontend');
INSERT INTO skills (id, name, category) VALUES (8, 'REST API', 'Backend');
INSERT INTO skills (id, name, category) VALUES (9, 'SQL', 'Database');
INSERT INTO skills (id, name, category) VALUES (10, 'Python', 'Database');
INSERT INTO skills (id, name, category) VALUES (11, 'Power BI', 'Database');
INSERT INTO skills (id, name, category) VALUES (12, 'Excel', 'Database');
INSERT INTO skills (id, name, category) VALUES (13, 'Kubernetes', 'Backend');
INSERT INTO skills (id, name, category) VALUES (14, 'AWS', 'Backend');
INSERT INTO skills (id, name, category) VALUES (15, 'CI/CD', 'Backend');
INSERT INTO skills (id, name, category) VALUES (16, 'Figma', 'Frontend');
INSERT INTO skills (id, name, category) VALUES (17, 'Adobe XD', 'Frontend');
INSERT INTO skills (id, name, category) VALUES (18, 'Prototyping', 'Frontend');

INSERT INTO job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (1, 'Senior Java Developer', 'Engineering', 'Develop backend services for the recruitment platform', 'Backend service development with Java, Spring Boot, SQL, and Docker.', 'HCM', 'FULL_TIME', 2000.00, 3500.00, '2000-3500 USD', '2026-07-01', '2026-07-31', 'ACTIVE', 1, 2);
INSERT INTO job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (2, 'Frontend Developer', 'Product Engineering', 'Build user interfaces for candidates and employers', 'React, TypeScript, CSS, and REST API experience.', 'Remote', 'REMOTE', 1500.00, 2500.00, '1500-2500 USD', '2026-07-01', '2026-07-21', 'ACTIVE', 1, 2);
INSERT INTO job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (3, 'Data Analyst', 'Analytics', 'Analyze finance and recruitment data', 'SQL, Python, Power BI, and Excel reporting experience.', 'Ha Noi', 'FULL_TIME', 1200.00, 2000.00, '1200-2000 USD', '2026-07-01', '2026-07-16', 'ACTIVE', 2, 2);
INSERT INTO job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (4, 'DevOps Engineer', 'Platform', 'Maintain cloud infrastructure and CI/CD', 'Docker, Kubernetes, AWS, and CI/CD experience.', 'Ha Noi', 'FULL_TIME', 1800.00, 3000.00, '1800-3000 USD', '2026-07-01', '2026-07-26', 'DRAFT', 2, 2);
INSERT INTO job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (5, 'UX/UI Designer', 'Design', 'Design candidate and recruiter experiences', 'Figma, Adobe XD, CSS, and prototyping experience.', 'Da Nang', 'FULL_TIME', 1000.00, 1800.00, '1000-1800 USD', '2026-07-01', '2026-07-11', 'CLOSED', 3, 2);

INSERT INTO candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (1, 5, 'Bachelor CS', 'Senior Java Developer', 'https://linkedin.com/in/alice-nguyen', 'Backend engineer with strong Java and cloud experience.');
INSERT INTO candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (2, 3, 'Bachelor IT', 'Frontend Developer', 'https://linkedin.com/in/bob-tran', 'Frontend developer focused on modern web applications.');
INSERT INTO candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (3, 4, 'Master Data Science', 'Data Analyst', 'https://linkedin.com/in/carol-le', 'Data analyst experienced in reporting and visualization.');
INSERT INTO candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (4, 6, 'Bachelor IT', 'DevOps Engineer', 'https://linkedin.com/in/david-pham', 'DevOps engineer with container, cloud, and CI/CD experience.');

INSERT INTO candidates (id, name, email, phone, profile_id, user_id) VALUES (1, 'Alice Nguyen', 'alice@example.com', '0901000001', 1, 3);
INSERT INTO candidates (id, name, email, phone, profile_id, user_id) VALUES (2, 'Bob Tran', 'bob@example.com', '0901000002', 2, 4);
INSERT INTO candidates (id, name, email, phone, profile_id, user_id) VALUES (3, 'Carol Le', 'carol@example.com', '0901000003', 3, 5);
INSERT INTO candidates (id, name, email, phone, profile_id, user_id) VALUES (4, 'David Pham', 'david@example.com', '0901000004', 4, 6);

INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (1, 1);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (1, 2);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (1, 3);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (1, 4);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (2, 5);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (2, 6);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (2, 7);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (2, 8);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (3, 9);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (3, 10);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (3, 11);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (3, 12);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (4, 1);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (4, 4);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (4, 13);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (4, 14);
INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (4, 15);

INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (1, 1);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (1, 2);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (1, 3);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (1, 4);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (2, 5);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (2, 6);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (2, 7);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (2, 8);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (3, 9);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (3, 10);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (3, 11);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (3, 12);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (4, 4);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (4, 13);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (4, 14);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (4, 15);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (5, 7);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (5, 16);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (5, 17);
INSERT INTO job_required_skills (job_posting_id, skill_id) VALUES (5, 18);

INSERT INTO applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (1, 1, 1, '2026-07-01T10:00:00', 'OFFERED', 'I have 5 years of Java experience and want to help TechCorp build reliable backend services.');
INSERT INTO applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (2, 2, 2, '2026-07-01T10:10:00', 'UNDER_REVIEW', 'I enjoy building clean user interfaces with React and TypeScript.');
INSERT INTO applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (3, 3, 3, '2026-07-01T10:20:00', 'SUBMITTED', 'My data science background fits the analytics needs of this role.');
INSERT INTO applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (4, 1, 2, '2026-07-01T10:30:00', 'SUBMITTED', 'I can also support full-stack work for candidate-facing features.');

INSERT INTO interviews (id, application_id, scheduled_at, interview_type, interviewer_name, notes, result) VALUES (1, 1, '2026-07-04T09:00:00', 'TECHNICAL', 'John Smith', 'Strong Java skills', 'PASSED');

ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE company_profiles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE companies ALTER COLUMN id RESTART WITH 100;
ALTER TABLE skills ALTER COLUMN id RESTART WITH 100;
ALTER TABLE job_postings ALTER COLUMN id RESTART WITH 100;
ALTER TABLE candidate_profiles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE candidates ALTER COLUMN id RESTART WITH 100;
ALTER TABLE applications ALTER COLUMN id RESTART WITH 100;
ALTER TABLE interviews ALTER COLUMN id RESTART WITH 100;
