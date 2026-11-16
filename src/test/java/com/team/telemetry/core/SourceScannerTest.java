// Jibril contributed this code: Test cases for SourceScanner

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.parse.SourceScanner;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SourceScanner}.
 *
 * <p>Verifies that the source scanner correctly identifies Java method
 * and constructor declarations while ignoring comments, string literals,
 * and control flow statements.
 */
class SourceScannerTest {
    private final SourceScanner scanner = new SourceScanner();

    /**
     * Tests that methods and constructors are correctly identified
     * when opening braces are placed on the following line.
     *
     * <p>Uses a Java source example containing fields, constructors,
     * methods, and control flow statements to verify that only valid
     * method names are returned.
     */
    @Test
    void findsMethodsAndConstructorsInBlueJBraceOnNextLineStyle() {
        String src = """
                public class Foo
                {
                    private int x;

                    public Foo()
                    {
                        x = 0;
                    }

                    public int add(int a, int b)
                    {
                        if (a > b)
                        {
                            return a;
                        }
                        for (int i = 0; i < 3; i++) {
                            x++;
                        }
                        return a + b;
                    }

                    static void run() { }
                }
                """;
        assertEquals(List.of("Foo", "add", "run"), List.copyOf(scanner.findMethodNames(src)));
    }

    /**
     * Tests that method-like text inside comments and string literals
     * is ignored during source scanning.
     *
     * <p>Also verifies that control flow statements are not incorrectly
     * identified as method declarations.
     */
    @Test
    void ignoresCommentsStringsAndControlFlow() {
        String src = """
                class A {
                    // void fake() {
                    /* void alsoFake() { } */
                    String s = "void inString() {";
                    void real() {
                        while (true) { }
                    }
                }
                """;
        assertEquals(List.of("real"), List.copyOf(scanner.findMethodNames(src)));
    }

    /**
     * Tests that empty and null source code inputs return an empty
     * collection of method names without causing errors.
     */
    @Test
    void emptyAndNullSourceGiveNoMethods() {
        assertTrue(scanner.findMethodNames("").isEmpty());
        assertTrue(scanner.findMethodNames(null).isEmpty());
    }
}