// Jibril contributed this code: Test cases for CompileErrorClassifier

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.team.telemetry.core.parse.CompileErrorClassifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CompileErrorClassifier}.
 *
 * <p>Verifies that compiler error messages are correctly mapped to their
 * corresponding Java compiler diagnostic codes and that unrecognized or
 * invalid messages return the default unknown error code.
 */
class CompileErrorClassifierTest {
    private final CompileErrorClassifier classifier = new CompileErrorClassifier();

    /**
     * Tests that recognized compiler error messages are mapped to their
     * expected diagnostic codes.
     *
     * <p>Uses parameterized test cases to verify the classification of
     * common Java compilation errors, including missing symbols,
     * incompatible types, missing return statements, syntax errors,
     * and uninitialized variables.
     *
     * @param message the compiler error message to classify
     * @param expectedCode the expected compiler diagnostic code
     */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "cannot find symbol|compiler.err.cant.resolve",
            "incompatible types: String cannot be converted to int|compiler.err.prob.found.req",
            "missing return statement|compiler.err.missing.ret.stmt",
            "';' expected|compiler.err.expected",
            "reached end of file while parsing|compiler.err.premature.eof",
            "variable x might not have been initialized|compiler.err.var.might.not.have.been.initialized",
            "CANNOT FIND SYMBOL|compiler.err.cant.resolve"
    })
    void mapsKnownMessages(String message, String expectedCode) {
        assertEquals(expectedCode, classifier.classify(message));
    }

    /**
     * Tests that unknown, null, and blank compiler error messages
     * return the default unknown diagnostic code.
     */
    @Test
    void unknownNullAndBlankMessagesFallBack() {
        assertEquals(CompileErrorClassifier.UNKNOWN, classifier.classify("something exotic"));
        assertEquals(CompileErrorClassifier.UNKNOWN, classifier.classify(null));
        assertEquals(CompileErrorClassifier.UNKNOWN, classifier.classify("   "));
    }
}