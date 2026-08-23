ALTER TABLE "flow"
    ADD COLUMN "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE "flow"
    ADD COLUMN "modified_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE "flow"
SET "created_at"  = "uuid_extract_timestamp"("id"),
    "modified_at" = "uuid_extract_timestamp"("id")
WHERE "uuid_extract_version"("id") = 7;

ALTER TABLE "node"
    ADD COLUMN "created_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE "node"
    ADD COLUMN "modified_at" TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE "node"
SET "created_at"  = "uuid_extract_timestamp"("id"),
    "modified_at" = "uuid_extract_timestamp"("id")
WHERE "uuid_extract_version"("id") = 7;
