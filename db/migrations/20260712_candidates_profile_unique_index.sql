-- Run once for databases created before this correction.
-- A SQL Server UNIQUE constraint permits only one NULL, which prevented a second
-- newly registered candidate without a completed profile from being created.
IF EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE parent_object_id = OBJECT_ID(N'dbo.candidates')
      AND name = N'uq_candidates_profile'
)
BEGIN
    ALTER TABLE dbo.candidates DROP CONSTRAINT uq_candidates_profile;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.candidates')
      AND name = N'ux_candidates_profile_not_null'
)
BEGIN
    CREATE UNIQUE INDEX ux_candidates_profile_not_null
        ON dbo.candidates(profile_id)
        WHERE profile_id IS NOT NULL;
END;
