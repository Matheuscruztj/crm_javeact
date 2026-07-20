package com.atlasops.integrations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tag;

/**
 * Property-based tests for SSRFValidator.
 *
 * <p><b>Validates: P0.J.1 — SSRF Protection Utility</b>
 *
 * <p>Properties tested:
 * <ul>
 *   <li>Property 1: ANY private IP range is always blocked
 *   <li>Property 2: ANY loopback address is always blocked
 *   <li>Property 3: ANY non-HTTP/HTTPS scheme is always rejected
 *   <li>Property 4: Well-formed public HTTPS URLs always pass validation
 * </ul>
 */
@Tag("Feature: monorepo-sdd-harness, Property: SSRF Validator Invariants")
class SSRFValidatorPropertyTest {

    /**
     * Property 1: Any URL with a private IP range (10.x.x.x) is ALWAYS blocked.
     */
    @Property(tries = 100)
    void should_alwaysReject_privateIpRange10(
            @ForAll("privateIpRange10Urls") String url) {

        assertThatThrownBy(() -> SSRFValidator.validate(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private");
    }

    /**
     * Property 2: Any URL targeting the loopback address (127.x.x.x) is ALWAYS blocked.
     */
    @Property(tries = 50)
    void should_alwaysReject_loopbackUrls(
            @ForAll("loopbackUrls") String url) {

        assertThatThrownBy(() -> SSRFValidator.validate(url))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property 3: Any URL with a non-HTTP/HTTPS scheme is ALWAYS rejected.
     */
    @Property(tries = 100)
    void should_alwaysReject_nonHttpSchemes(
            @ForAll("nonHttpSchemeUrls") String url) {

        assertThatThrownBy(() -> SSRFValidator.validate(url))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Property 4: Well-formed public HTTPS URLs with registered domain names
     * always pass validation (no exception thrown).
     */
    @Property(tries = 100)
    void should_alwaysAllow_publicHttpsUrls(
            @ForAll("publicHttpsUrls") String url) {

        // Should not throw — public HTTPS URLs are permitted
        org.assertj.core.api.Assertions.assertThatCode(() -> SSRFValidator.validate(url))
                .doesNotThrowAnyException();
    }

    // ---- Arbitraries ----

    @Provide
    Arbitrary<String> privateIpRange10Urls() {
        return Arbitraries.integers().between(0, 255).flatMap(b ->
            Arbitraries.integers().between(0, 255).flatMap(c ->
                Arbitraries.integers().between(0, 255).map(d ->
                    "http://10." + b + "." + c + "." + d + "/webhook"
                )
            )
        );
    }

    @Provide
    Arbitrary<String> loopbackUrls() {
        return Arbitraries.integers().between(0, 255).flatMap(b ->
            Arbitraries.integers().between(0, 255).map(c ->
                "http://127." + b + ".0." + c + "/api/callback"
            )
        );
    }

    @Provide
    Arbitrary<String> nonHttpSchemeUrls() {
        return Arbitraries.of("file", "ftp", "gopher", "ldap", "dict", "sftp")
                .map(scheme -> scheme + "://example.com/malicious");
    }

    @Provide
    Arbitrary<String> publicHttpsUrls() {
        // Well-known public domains that are safe
        return Arbitraries.of(
                "https://webhook.site/test-endpoint",
                "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXX",
                "https://api.github.com/repos/owner/repo/hooks",
                "https://app.pagerduty.com/generic/2010-04-15/create_event.json",
                "https://discord.com/api/webhooks/1234567890/token"
        );
    }
}
