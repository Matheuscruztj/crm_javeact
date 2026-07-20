package com.atlasops.customers.domain.ports;

import com.atlasops.customers.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PostGIS-backed port for geospatial customer queries.
 * Falls back gracefully when PostGIS is unavailable.
 *
 * <p>Implementations should use the {@code location} geography column on the
 * customers table (added by {@code V20250720_0013__add_postgis_location.sql})
 * and the {@code ST_DWithin} PostGIS function for radius queries.
 *
 * <p>Validates: P2.1 — PostGIS radius query support
 */
public interface GeospatialCustomerPort {

    /**
     * Finds customers within a given radius from the specified coordinates.
     *
     * @param lat       latitude of the search center (WGS84, decimal degrees)
     * @param lon       longitude of the search center (WGS84, decimal degrees)
     * @param radiusKm  search radius in kilometers (must be positive)
     * @param tenantId  the tenant to scope the search to
     * @param pageable  pagination parameters
     * @return a page of customers whose location is within {@code radiusKm} of
     *         the given coordinates, ordered by proximity ascending
     */
    Page<Customer> findWithinRadius(
            double lat, double lon, double radiusKm, String tenantId, Pageable pageable);
}
