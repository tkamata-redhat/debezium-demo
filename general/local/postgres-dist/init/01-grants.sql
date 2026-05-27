CREATE SCHEMA IF NOT EXISTS example_schema AUTHORIZATION example_user;

CREATE TABLE IF NOT EXISTS example_schema.customer (
  customer_id VARCHAR(256) NOT NULL,
  first_name VARCHAR(256),
  last_name VARCHAR(256),
  PRIMARY KEY (customer_id)
);
ALTER TABLE example_schema.customer OWNER TO example_user;

GRANT USAGE, CREATE ON SCHEMA example_schema TO example_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA example_schema TO example_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA example_schema GRANT ALL ON TABLES TO example_user;
