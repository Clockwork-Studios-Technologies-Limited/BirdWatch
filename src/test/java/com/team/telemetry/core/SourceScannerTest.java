// Jibril contributed this code: Test cases for SourceScanner

package com.team.telemetry.core;

import static org.junit.jupiter.api.Assertions.*;

import com.team.telemetry.core.parse.SourceScanner;
import java.util.List;
import org.junit.jupiter.api.Test;

class SourceScannerTest {
    private final SourceScanner scanner = new SourceScanner();

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

    @Test
    void emptyAndNullSourceGiveNoMethods() {
        assertTrue(scanner.findMethodNames("").isEmpty());
        assertTrue(scanner.findMethodNames(null).isEmpty());
    }
}
