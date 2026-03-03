CREATE TABLE IF NOT EXISTS "notification"
(
    "id"           uuid PRIMARY KEY,
    "user_id"      TEXT                                NOT NULL,
    "attributes"   JSONB                               NOT NULL,
    "open"         BOOLEAN   DEFAULT TRUE              NULL,
    "resolved_at"  TIMESTAMP DEFAULT NULL              NULL,
    "count"        INT       DEFAULT 1                 NOT NULL,
    "last_seen_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "title"        TEXT                                NOT NULL,
    "description"  TEXT                                NULL,
    CONSTRAINT "notification_open_resolved_at_consistent"
        CHECK
            (
            ("open" = TRUE AND "resolved_at" IS NULL)
                OR
            ("open" IS NULL AND "resolved_at" IS NOT NULL)
            ),
    CONSTRAINT "notification_user_id_attributes_open_unique" UNIQUE ("user_id", "attributes", "open")
);
