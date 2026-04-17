package com.manuelpuchner.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    // Must be at least 32 characters (256 bits) for HMAC-SHA256
    private static final String TEST_SECRET     = "test-secret-key-for-unit-tests-minimum-32chars!!";
    private static final long   TEST_EXPIRATION = 3_600_000L; // 1 hour in ms

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",     TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    // -------------------------------------------------------------------------
    // generateToken
    // -------------------------------------------------------------------------

    @Test
    void generateToken_givenUsername_thenReturnsNonBlankToken() {
        String token = jwtUtil.generateToken("testuser");

        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_givenUsername_thenTokenContainsThreeParts() {
        String token = jwtUtil.generateToken("testuser");

        // JWT tokens are three Base64-encoded parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    // -------------------------------------------------------------------------
    // extractUsername
    // -------------------------------------------------------------------------

    @Test
    void extractUsername_givenValidToken_thenReturnsOriginalUsername() {
        String token = jwtUtil.generateToken("alice");

        String extracted = jwtUtil.extractUsername(token);

        assertThat(extracted).isEqualTo("alice");
    }

    // -------------------------------------------------------------------------
    // isValid
    // -------------------------------------------------------------------------

    @Test
    void isValid_givenValidToken_thenReturnsTrue() {
        String token = jwtUtil.generateToken("alice");

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_givenTamperedToken_thenReturnsFalse() {
        String token   = jwtUtil.generateToken("alice");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_givenTokenSignedWithDifferentSecret_thenReturnsFalse() {
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "secret",     "completely-different-secret-key-32chars!!");
        ReflectionTestUtils.setField(otherJwtUtil, "expiration", TEST_EXPIRATION);

        String foreignToken = otherJwtUtil.generateToken("alice");

        assertThat(jwtUtil.isValid(foreignToken)).isFalse();
    }

    @Test
    void isValid_givenExpiredToken_thenReturnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L); // expires immediately
        String expiredToken = jwtUtil.generateToken("alice");

        assertThat(jwtUtil.isValid(expiredToken)).isFalse();
    }

    @Test
    void isValid_givenEmptyString_thenReturnsFalse() {
        assertThat(jwtUtil.isValid("")).isFalse();
    }

    @Test
    void isValid_givenGarbageString_thenReturnsFalse() {
        assertThat(jwtUtil.isValid("not.a.token")).isFalse();
    }
}
