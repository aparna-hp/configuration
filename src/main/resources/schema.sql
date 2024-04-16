------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS collectionservice;
SET search_path="collectionservice";
------------------------------------------------------

CREATE TABLE IF NOT EXISTS NETWORK (
    ID SERIAL PRIMARY KEY,
    NAME TEXT NOT NULL,
    DRAFT BOOLEAN,
    DRAFT_CONFIG TEXT,
    UNIQUE (NAME)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS COLLECTOR (
  ID SERIAL PRIMARY KEY,
  NAME TEXT NOT NULL,
  PARAMS TEXT,
  SOURCE_COLLECTOR TEXT,
  CONSOLIDATION_TYPE TEXT,
  TYPE VARCHAR(255),
  TIMEOUT INTEGER,
  UNIQUE (NAME)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS AGENTS (
  ID SERIAL PRIMARY KEY,
  NAME TEXT NOT NULL,
  PARAMS TEXT,
  TYPE VARCHAR(255),
  UPDATE_DATE DATE NOT NULL DEFAULT CURRENT_DATE,
  UNIQUE (NAME)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS AUTH_GROUP (
    ID SERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    LOGIN_TYPE VARCHAR(255),
    USERNAME VARCHAR(255),
    PASSWORD bytea,
    CONFIRM_PASSWORD bytea,
    UPDATE_DATE DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (NAME)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS SNMP_GROUP (
    ID SERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    SNMP_TYPE VARCHAR(255),
    USERNAME VARCHAR(255),
    RO_COMMUNITY VARCHAR(255),
    SECURITY_LEVEL VARCHAR(255),
    AUTHENTICATION_PROTOCOL VARCHAR(255),
    AUTHENTICATION_PASSWORD bytea,
    ENCRYPTION_PROTOCOL VARCHAR(255),
    ENCRYPTION_PASSWORD bytea,
    UPDATE_DATE DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (NAME)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS NODE_PROFILE (
    ID SERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    DEFAULT_AUTH_GROUP VARCHAR(255),
    DEFAULT_SNMP_GROUP VARCHAR(255),
    USE_NODE_LIST_AS_INCLUDE_FILTER BOOLEAN,
    UPDATE_DATE DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (NAME)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS NODE_LIST (
    ID SERIAL PRIMARY KEY,
    NODE_IP VARCHAR(255) NOT NULL,
    NODE_MANAGEMENT_IP VARCHAR(255)
);
------------------------------------------------------

CREATE TABLE IF NOT EXISTS NODE_FILTER (
    ID SERIAL PRIMARY KEY,
    TYPE VARCHAR(255) NOT NULL,
    CONDITION VARCHAR(255),
    VALUE VARCHAR(255),
    ENABLED BOOLEAN
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS IMPORT_HISTORY (
  ID SERIAL PRIMARY KEY,
  TYPE VARCHAR(255),
  STATUS VARCHAR(255),
  FAILURE_REPORT TEXT,
  START_TIME BIGINT,
  END_TIME BIGINT
);

------------------------------------------------------

CREATE TABLE IF NOT EXISTS NODE_PROFILE_REF (
    ID SERIAL PRIMARY KEY,
    NODE_PROFILE_ID INTEGER REFERENCES NODE_PROFILE(ID),
    NETWORK INTEGER REFERENCES NETWORK(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS COLLECTOR_REF (
    ID SERIAL PRIMARY KEY,
    COLLECTOR_ID INTEGER REFERENCES COLLECTOR(ID),
    NETWORK INTEGER REFERENCES NETWORK(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS AGENT_REF (
    ID SERIAL PRIMARY KEY,
    AGENT_ID INTEGER REFERENCES AGENTS(ID),
    COLLECTOR INTEGER REFERENCES COLLECTOR(ID)
);;
------------------------------------------------------


CREATE TABLE IF NOT EXISTS AGENT_REF (
    ID SERIAL PRIMARY KEY,
    AGENT_ID INTEGER REFERENCES AGENTS(ID),
    COLLECTOR INTEGER REFERENCES COLLECTOR(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS CHILD_COLLECTOR_REF (
    ID SERIAL PRIMARY KEY,
    CHILD_COLLECTOR_ID INTEGER REFERENCES COLLECTOR(ID),
    COLLECTOR INTEGER REFERENCES COLLECTOR(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS NODE_LIST_REF (
    ID SERIAL PRIMARY KEY,
    NODE_LIST_ID INTEGER REFERENCES NODE_LIST(ID),
    NODE_PROFILE INTEGER REFERENCES NODE_PROFILE(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS NODE_FILTER_REF (
    ID SERIAL PRIMARY KEY,
    NODE_FILTER_ID INTEGER REFERENCES NODE_FILTER(ID),
    NODE_PROFILE INTEGER REFERENCES NODE_PROFILE(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS AUTH_GROUP_REF (
    ID SERIAL PRIMARY KEY,
    AUTH_GROUP_ID INTEGER REFERENCES AUTH_GROUP(ID),
    NODE_LIST INTEGER REFERENCES NODE_LIST(ID)
);;
------------------------------------------------------

CREATE TABLE IF NOT EXISTS SNMP_GROUP_REF (
    ID SERIAL PRIMARY KEY,
    SNMP_GROUP_ID INTEGER REFERENCES SNMP_GROUP(ID),
    NODE_LIST INTEGER REFERENCES NODE_LIST(ID)
);;
------------------------------------------------------
