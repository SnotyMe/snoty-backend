CREATE TABLE "flowexecution"
(
    "id"           TEXT PRIMARY KEY,
    "flow_id"      uuid        NOT NULL
        CONSTRAINT "fk_flowexecution_flow_id__id" REFERENCES "flow" ("id") ON DELETE CASCADE ON UPDATE RESTRICT,
    "triggered_by" VARCHAR(10) NOT NULL,
    "triggered_at" TIMESTAMP   NOT NULL,
    "status"       VARCHAR(15) NOT NULL
);

CREATE TABLE IF NOT EXISTS "flowexecutionlog"
(
    "id"           uuid PRIMARY KEY,
    "execution_id" TEXT        NOT NULL
        CONSTRAINT "fk_flowexecution_flow_id__id" REFERENCES "flowexecution" ("id") ON DELETE CASCADE ON UPDATE RESTRICT,
    "timestamp"    TIMESTAMP   NOT NULL,
    "level"        VARCHAR(10) NOT NULL,
    "message"      TEXT        NOT NULL,
    "node"         uuid        NULL
);
