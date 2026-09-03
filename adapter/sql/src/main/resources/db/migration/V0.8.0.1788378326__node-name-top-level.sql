ALTER TABLE "node"
    ADD COLUMN "name" TEXT;

UPDATE "node"
SET "name" = COALESCE("settings" ->> 'name', "descriptor_name");

ALTER TABLE "node"
    ALTER COLUMN "name" SET NOT NULL;
