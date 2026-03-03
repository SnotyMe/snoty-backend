CREATE TABLE "flow"
(
    "id"       uuid PRIMARY KEY,
    "user_id"  TEXT  NOT NULL,
    "name"     TEXT  NOT NULL,
    "settings" JSONB NULL
);

CREATE TABLE "node"
(
    "id"                   uuid PRIMARY KEY,
    "flow_id"              uuid        NOT NULL
        CONSTRAINT "fk_node_flow_id__id" REFERENCES "flow" ("id") ON DELETE CASCADE ON UPDATE RESTRICT,
    "user_id"              TEXT        NOT NULL,
    "descriptor_namespace" TEXT        NOT NULL,
    "descriptor_name"      TEXT        NOT NULL,
    "log_level"            VARCHAR(10) NULL,
    "settings"             JSONB       NOT NULL
);

CREATE TABLE IF NOT EXISTS "node_connection"
(
    "from" uuid NOT NULL
        CONSTRAINT "fk_node_connection_from__id" REFERENCES "node" ("id") ON DELETE CASCADE ON UPDATE RESTRICT,
    "to"   uuid NOT NULL
        CONSTRAINT "fk_node_connection_to__id" REFERENCES "node" ("id") ON DELETE CASCADE ON UPDATE RESTRICT,
    PRIMARY KEY ("from", "to")
);
