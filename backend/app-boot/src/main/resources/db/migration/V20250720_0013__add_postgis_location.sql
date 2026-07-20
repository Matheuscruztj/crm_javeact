-- P2.1: Add PostGIS location support to customers table
-- Enable PostGIS if not already enabled
CREATE EXTENSION IF NOT EXISTS postgis;

-- Add location column to customers
ALTER TABLE customers
  ADD COLUMN IF NOT EXISTS location geography(Point, 4326);

-- GIST index for spatial queries
CREATE INDEX IF NOT EXISTS idx_customers_location
  ON customers USING GIST (location);
