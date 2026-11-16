package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.team.telemetry.core.parse.CompileErrorClassifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class CompileErrorClassifierTest {
    private final CompileErrorClassifier classifier = new CompileErrorClassifier();

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

    @Test
    void unknownNullAndBlankMessagesFallBack() {
        assertEquals(CompileErrorClassifier.UNKNOWN, classifier.classify("something exotic"));
        assertEquals(CompileErrorClassifier.UNKNOWN, classifier.classify(null));
        assertEquals(CompileErrorClassifier.UNKNOWN, classifier.classify("   "));
    }
}
