ALTER TABLE "node"
    ADD COLUMN "type" TEXT;

UPDATE "node"
SET "type" = "descriptor_name";

ALTER TABLE "node"
    ALTER COLUMN "type" SET NOT NULL;

ALTER TABLE "node"
    ALTER COLUMN "descriptor_namespace" DROP NOT NULL;

ALTER TABLE "node"
    ALTER COLUMN "descriptor_name" DROP NOT NULL;
