ALTER USER debezium WITH REPLICATION;

CREATE SCHEMA IF NOT EXISTS example_schema;
ALTER SCHEMA example_schema OWNER TO debezium;

CREATE TABLE IF NOT EXISTS example_schema.customer (
  customer_id VARCHAR(256) NOT NULL,
  first_name VARCHAR(256),
  last_name VARCHAR(256),
  PRIMARY KEY (customer_id)
);
ALTER TABLE example_schema.customer OWNER TO debezium;

GRANT USAGE, CREATE ON SCHEMA example_schema TO debezium;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA example_schema TO debezium;
ALTER DEFAULT PRIVILEGES IN SCHEMA example_schema GRANT ALL ON TABLES TO debezium;
