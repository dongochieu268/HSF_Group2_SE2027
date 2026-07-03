SET IDENTITY_INSERT dbo.roles ON;
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE id = 1) INSERT INTO dbo.roles (id, name) VALUES (1, N'ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE id = 2) INSERT INTO dbo.roles (id, name) VALUES (2, N'HR_MANAGER');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE id = 3) INSERT INTO dbo.roles (id, name) VALUES (3, N'CANDIDATE');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE id = 4) INSERT INTO dbo.roles (id, name) VALUES (4, N'INTERVIEWER');
SET IDENTITY_INSERT dbo.roles OFF;

UPDATE dbo.roles SET name = N'HR_MANAGER' WHERE name = N'RECRUITER';

SET IDENTITY_INSERT dbo.users ON;
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 1) INSERT INTO dbo.users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (1, N'admin', N'$2a$10$GMxGu.0KX.u1gG5H5RrTj.cTzwQfaqGiEuyHWreaRr0A.IHEenilS', N'admin@recruit.com', N'System Administrator', 1, '2026-07-01T09:00:00', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 2) INSERT INTO dbo.users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (2, N'hrmanager', N'$2a$10$r7MkMz1PXe3JEz.USSGYn.ApIs.rI6yVROPRKLZUxMJC9n/QHx4cG', N'hrmanager@recruit.com', N'HR Manager', 1, '2026-07-01T09:01:00', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 3) INSERT INTO dbo.users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (3, N'alice', N'$2a$10$lrhTGzM3Lvp31lKh.89E5eNQ1ex1gErHRQuRiznLnywfLSNlsIxxG', N'alice@example.com', N'Alice Nguyen', 1, '2026-07-01T09:02:00', 3);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 4) INSERT INTO dbo.users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (4, N'bob', N'$2a$10$XyrNA8NFN0Ko6X34a8jEIu4mVTI1GgO7KTwiqxTT17pmA3aLwHZ7G', N'bob@example.com', N'Bob Tran', 1, '2026-07-01T09:03:00', 3);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 5) INSERT INTO dbo.users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (5, N'carol', N'$2a$10$Iahb6Eh0x0XLTJmcNkam8uLlRmej76zl8GW711KaGKpJh9U.f6r3u', N'carol@example.com', N'Carol Le', 1, '2026-07-01T09:04:00', 3);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE id = 6) INSERT INTO dbo.users (id, username, password, email, full_name, enabled, created_at, role_id) VALUES (6, N'david', N'$2a$10$3y2GoAVWd9tJ24xcsevCSerbxfOLIwWCXPgSLLehu1x1FDgruasG6', N'david@example.com', N'David Pham', 1, '2026-07-01T09:05:00', 3);
SET IDENTITY_INSERT dbo.users OFF;

UPDATE dbo.users SET username = N'hrmanager', email = N'hrmanager@recruit.com', full_name = N'HR Manager', role_id = 2 WHERE username = N'recruiter';

SET IDENTITY_INSERT dbo.company_profiles ON;
IF NOT EXISTS (SELECT 1 FROM dbo.company_profiles WHERE id = 1) INSERT INTO dbo.company_profiles (id, description, headquarters, employee_count, founded_year) VALUES (1, N'Cloud solutions leader', N'HCM', 500, 2010);
IF NOT EXISTS (SELECT 1 FROM dbo.company_profiles WHERE id = 2) INSERT INTO dbo.company_profiles (id, description, headquarters, employee_count, founded_year) VALUES (2, N'Digital banking fintech', N'Ha Noi', 200, 2015);
IF NOT EXISTS (SELECT 1 FROM dbo.company_profiles WHERE id = 3) INSERT INTO dbo.company_profiles (id, description, headquarters, employee_count, founded_year) VALUES (3, N'UX/UI agency', N'Da Nang', 80, 2018);
SET IDENTITY_INSERT dbo.company_profiles OFF;

SET IDENTITY_INSERT dbo.companies ON;
IF NOT EXISTS (SELECT 1 FROM dbo.companies WHERE id = 1) INSERT INTO dbo.companies (id, name, industry, website, profile_id) VALUES (1, N'TechCorp Inc.', N'Technology', N'techcorp.com', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.companies WHERE id = 2) INSERT INTO dbo.companies (id, name, industry, website, profile_id) VALUES (2, N'FinanceHub Ltd.', N'Finance', N'financehub.com', 2);
IF NOT EXISTS (SELECT 1 FROM dbo.companies WHERE id = 3) INSERT INTO dbo.companies (id, name, industry, website, profile_id) VALUES (3, N'Creative Studio', N'Design', N'creativestudio.com', 3);
SET IDENTITY_INSERT dbo.companies OFF;

SET IDENTITY_INSERT dbo.skills ON;
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 1) INSERT INTO dbo.skills (id, name, category) VALUES (1, N'Java', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 2) INSERT INTO dbo.skills (id, name, category) VALUES (2, N'Spring Boot', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 3) INSERT INTO dbo.skills (id, name, category) VALUES (3, N'PostgreSQL', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 4) INSERT INTO dbo.skills (id, name, category) VALUES (4, N'Docker', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 5) INSERT INTO dbo.skills (id, name, category) VALUES (5, N'React', N'Frontend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 6) INSERT INTO dbo.skills (id, name, category) VALUES (6, N'TypeScript', N'Frontend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 7) INSERT INTO dbo.skills (id, name, category) VALUES (7, N'CSS', N'Frontend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 8) INSERT INTO dbo.skills (id, name, category) VALUES (8, N'REST API', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 9) INSERT INTO dbo.skills (id, name, category) VALUES (9, N'SQL', N'Database');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 10) INSERT INTO dbo.skills (id, name, category) VALUES (10, N'Python', N'Database');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 11) INSERT INTO dbo.skills (id, name, category) VALUES (11, N'Power BI', N'Database');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 12) INSERT INTO dbo.skills (id, name, category) VALUES (12, N'Excel', N'Database');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 13) INSERT INTO dbo.skills (id, name, category) VALUES (13, N'Kubernetes', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 14) INSERT INTO dbo.skills (id, name, category) VALUES (14, N'AWS', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 15) INSERT INTO dbo.skills (id, name, category) VALUES (15, N'CI/CD', N'Backend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 16) INSERT INTO dbo.skills (id, name, category) VALUES (16, N'Figma', N'Frontend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 17) INSERT INTO dbo.skills (id, name, category) VALUES (17, N'Adobe XD', N'Frontend');
IF NOT EXISTS (SELECT 1 FROM dbo.skills WHERE id = 18) INSERT INTO dbo.skills (id, name, category) VALUES (18, N'Prototyping', N'Frontend');
SET IDENTITY_INSERT dbo.skills OFF;

SET IDENTITY_INSERT dbo.job_postings ON;
IF NOT EXISTS (SELECT 1 FROM dbo.job_postings WHERE id = 1) INSERT INTO dbo.job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (1, N'Senior Java Developer', N'Engineering', N'Develop backend services for the recruitment platform', N'Backend service development with Java, Spring Boot, SQL, and Docker.', N'HCM', N'FULL_TIME', 2000.00, 3500.00, N'2000-3500 USD', '2026-07-01', '2026-07-31', N'ACTIVE', 1, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.job_postings WHERE id = 2) INSERT INTO dbo.job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (2, N'Frontend Developer', N'Product Engineering', N'Build user interfaces for candidates and employers', N'React, TypeScript, CSS, and REST API experience.', N'Remote', N'REMOTE', 1500.00, 2500.00, N'1500-2500 USD', '2026-07-01', '2026-07-21', N'ACTIVE', 1, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.job_postings WHERE id = 3) INSERT INTO dbo.job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (3, N'Data Analyst', N'Analytics', N'Analyze finance and recruitment data', N'SQL, Python, Power BI, and Excel reporting experience.', N'Ha Noi', N'FULL_TIME', 1200.00, 2000.00, N'1200-2000 USD', '2026-07-01', '2026-07-16', N'ACTIVE', 2, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.job_postings WHERE id = 4) INSERT INTO dbo.job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (4, N'DevOps Engineer', N'Platform', N'Maintain cloud infrastructure and CI/CD', N'Docker, Kubernetes, AWS, and CI/CD experience.', N'Ha Noi', N'FULL_TIME', 1800.00, 3000.00, N'1800-3000 USD', '2026-07-01', '2026-07-26', N'DRAFT', 2, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.job_postings WHERE id = 5) INSERT INTO dbo.job_postings (id, title, department, description, requirements, location, job_type, salary_min, salary_max, salary_range, posted_date, deadline, status, company_id, created_by_id) VALUES (5, N'UX/UI Designer', N'Design', N'Design candidate and recruiter experiences', N'Figma, Adobe XD, CSS, and prototyping experience.', N'Da Nang', N'FULL_TIME', 1000.00, 1800.00, N'1000-1800 USD', '2026-07-01', '2026-07-11', N'CLOSED', 3, 2);
SET IDENTITY_INSERT dbo.job_postings OFF;

UPDATE dbo.job_postings
SET department = COALESCE(department, N'General'),
    requirements = COALESCE(requirements, description),
    salary_range = COALESCE(salary_range, CONCAT(COALESCE(CAST(salary_min AS NVARCHAR(30)), N'-'), N' - ', COALESCE(CAST(salary_max AS NVARCHAR(30)), N'-'))),
    created_by_id = COALESCE(created_by_id, 2),
    status = CASE WHEN status = N'OPEN' THEN N'ACTIVE' ELSE status END;

SET IDENTITY_INSERT dbo.candidate_profiles ON;
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_profiles WHERE id = 1) INSERT INTO dbo.candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (1, 5, N'Bachelor CS', N'Senior Java Developer', N'https://linkedin.com/in/alice-nguyen', N'Backend engineer with strong Java and cloud experience.');
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_profiles WHERE id = 2) INSERT INTO dbo.candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (2, 3, N'Bachelor IT', N'Frontend Developer', N'https://linkedin.com/in/bob-tran', N'Frontend developer focused on modern web applications.');
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_profiles WHERE id = 3) INSERT INTO dbo.candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (3, 4, N'Master Data Science', N'Data Analyst', N'https://linkedin.com/in/carol-le', N'Data analyst experienced in reporting and visualization.');
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_profiles WHERE id = 4) INSERT INTO dbo.candidate_profiles (id, years_of_experience, education_level, current_title, linkedin_url, resume_summary) VALUES (4, 6, N'Bachelor IT', N'DevOps Engineer', N'https://linkedin.com/in/david-pham', N'DevOps engineer with container, cloud, and CI/CD experience.');
SET IDENTITY_INSERT dbo.candidate_profiles OFF;

SET IDENTITY_INSERT dbo.candidates ON;
IF NOT EXISTS (SELECT 1 FROM dbo.candidates WHERE id = 1) INSERT INTO dbo.candidates (id, name, email, phone, profile_id, user_id) VALUES (1, N'Alice Nguyen', N'alice@example.com', N'0901000001', 1, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.candidates WHERE id = 2) INSERT INTO dbo.candidates (id, name, email, phone, profile_id, user_id) VALUES (2, N'Bob Tran', N'bob@example.com', N'0901000002', 2, 4);
IF NOT EXISTS (SELECT 1 FROM dbo.candidates WHERE id = 3) INSERT INTO dbo.candidates (id, name, email, phone, profile_id, user_id) VALUES (3, N'Carol Le', N'carol@example.com', N'0901000003', 3, 5);
IF NOT EXISTS (SELECT 1 FROM dbo.candidates WHERE id = 4) INSERT INTO dbo.candidates (id, name, email, phone, profile_id, user_id) VALUES (4, N'David Pham', N'david@example.com', N'0901000004', 4, 6);
SET IDENTITY_INSERT dbo.candidates OFF;

IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 1 AND skill_id = 1) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 1 AND skill_id = 2) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (1, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 1 AND skill_id = 3) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (1, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 1 AND skill_id = 4) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (1, 4);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 2 AND skill_id = 5) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (2, 5);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 2 AND skill_id = 6) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (2, 6);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 2 AND skill_id = 7) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (2, 7);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 2 AND skill_id = 8) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (2, 8);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 3 AND skill_id = 9) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (3, 9);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 3 AND skill_id = 10) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (3, 10);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 3 AND skill_id = 11) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (3, 11);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 3 AND skill_id = 12) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (3, 12);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 4 AND skill_id = 4) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (4, 4);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 4 AND skill_id = 13) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (4, 13);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 4 AND skill_id = 14) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (4, 14);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 4 AND skill_id = 15) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (4, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.candidate_skills WHERE candidate_id = 4 AND skill_id = 1) INSERT INTO dbo.candidate_skills (candidate_id, skill_id) VALUES (4, 1);

IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 1 AND skill_id = 1) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (1, 1);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 1 AND skill_id = 2) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (1, 2);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 1 AND skill_id = 3) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (1, 3);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 1 AND skill_id = 4) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (1, 4);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 2 AND skill_id = 5) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (2, 5);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 2 AND skill_id = 6) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (2, 6);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 2 AND skill_id = 7) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (2, 7);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 2 AND skill_id = 8) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (2, 8);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 3 AND skill_id = 9) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (3, 9);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 3 AND skill_id = 10) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (3, 10);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 3 AND skill_id = 11) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (3, 11);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 3 AND skill_id = 12) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (3, 12);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 4 AND skill_id = 4) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (4, 4);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 4 AND skill_id = 13) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (4, 13);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 4 AND skill_id = 14) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (4, 14);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 4 AND skill_id = 15) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (4, 15);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 5 AND skill_id = 16) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (5, 16);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 5 AND skill_id = 17) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (5, 17);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 5 AND skill_id = 7) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (5, 7);
IF NOT EXISTS (SELECT 1 FROM dbo.job_required_skills WHERE job_posting_id = 5 AND skill_id = 18) INSERT INTO dbo.job_required_skills (job_posting_id, skill_id) VALUES (5, 18);

SET IDENTITY_INSERT dbo.applications ON;
IF NOT EXISTS (SELECT 1 FROM dbo.applications WHERE id = 1) INSERT INTO dbo.applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (1, 1, 1, '2026-07-01T10:00:00', N'OFFERED', N'I have 5 years of Java experience and want to help TechCorp build reliable backend services.');
IF NOT EXISTS (SELECT 1 FROM dbo.applications WHERE id = 2) INSERT INTO dbo.applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (2, 2, 2, '2026-07-01T10:10:00', N'UNDER_REVIEW', N'I enjoy building clean user interfaces with React and TypeScript.');
IF NOT EXISTS (SELECT 1 FROM dbo.applications WHERE id = 3) INSERT INTO dbo.applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (3, 3, 3, '2026-07-01T10:20:00', N'SUBMITTED', N'My data science background fits the analytics needs of this role.');
IF NOT EXISTS (SELECT 1 FROM dbo.applications WHERE id = 4) INSERT INTO dbo.applications (id, candidate_id, job_posting_id, applied_at, status, cover_letter) VALUES (4, 1, 2, '2026-07-01T10:30:00', N'SUBMITTED', N'I can also support full-stack work for candidate-facing features.');
SET IDENTITY_INSERT dbo.applications OFF;

SET IDENTITY_INSERT dbo.interviews ON;
IF NOT EXISTS (SELECT 1 FROM dbo.interviews WHERE id = 1) INSERT INTO dbo.interviews (id, application_id, scheduled_at, interview_type, interviewer_name, notes, result) VALUES (1, 1, '2026-07-04T09:00:00', N'TECHNICAL', N'John Smith', N'Strong Java skills', N'PASSED');
SET IDENTITY_INSERT dbo.interviews OFF;
