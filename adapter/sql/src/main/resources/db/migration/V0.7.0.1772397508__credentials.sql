CREATE TABLE IF NOT EXISTS "credential"
(
    "id"            uuid PRIMARY KEY,
    "scope"         VARCHAR(50) NOT NULL,
    "owner_id"      TEXT        NULL,
    "role_required" TEXT        NULL,
    "type"          TEXT        NOT NULL,
    "name"          TEXT        NOT NULL,
    "data"          JSONB       NOT NULL,
    CONSTRAINT "credential_scope_owner_id_role_required_consistent"
        CHECK (
            ("scope" = 'USER' AND "owner_id" IS NOT NULL AND "role_required" IS NULL)
                OR
            ("scope" = 'ROLE' AND "role_required" IS NOT NULL) OR ("scope" = 'GLOBAL' AND "role_required" IS NULL)
            )
);
