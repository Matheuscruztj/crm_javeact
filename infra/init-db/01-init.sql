-- =============================================================================
-- AtlasOps AI — PostgreSQL Initialization Script
-- =============================================================================
-- This script is mounted at /docker-entrypoint-initdb.d/ in the PostgreSQL
-- container and executes automatically on first database creation.
-- =============================================================================

-- Enable pgvector extension for AI/RAG similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Enable uuid-ossp for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Note: The default database "atlasops" is created by the POSTGRES_DB env var
-- in docker-compose.yml. This script runs against that database.
-- Additional setup (schemas, initial tables) can be added below.

-- Create application schema
CREATE SCHEMA IF NOT EXISTS app;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON SCHEMA app TO CURRENT_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app TO CURRENT_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA app GRANT ALL PRIVILEGES ON TABLES TO CURRENT_USER;
