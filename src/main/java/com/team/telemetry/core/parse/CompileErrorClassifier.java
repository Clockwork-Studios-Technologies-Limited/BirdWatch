package com.team.telemetry.core.parse;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Jibril contributed this code: added the javac-style error code rule table
/**
 * BlueJ's CompileEvent exposes a message and line number but no error code, so a stable javac-style
 * code is derived from the message text.
 */
public class CompileErrorClassifier {
    /** Fallback code for blank or unrecognized compiler messages. */
    public static final String UNKNOWN = "compiler.err.unknown";

    private final Map<String, String> rules = new LinkedHashMap<>();

    /** Creates a classifier with the built-in message-to-code rules. */
    public CompileErrorClassifier() {
        rules.put("cannot find symbol", "compiler.err.cant.resolve");
        rules.put("cannot be applied to", "compiler.err.cant.apply.symbol");
        rules.put("incompatible types", "compiler.err.prob.found.req");
        rules.put("missing return statement", "compiler.err.missing.ret.stmt");
        rules.put("might not have been initialized", "compiler.err.var.might.not.have.been.initialized");
        rules.put("is already defined", "compiler.err.already.defined");
        rules.put("unreachable statement", "compiler.err.unreachable.stmt");
        rules.put("reached end of file while parsing", "compiler.err.premature.eof");
        rules.put("class, interface, enum, or record expected", "compiler.err.expected4");
        rules.put("unclosed string literal", "compiler.err.unclosed.str.lit");
        rules.put("not a statement", "compiler.err.not.stmt");
        rules.put("illegal start of expression", "compiler.err.illegal.start.of.expr");
        rules.put("illegal start of type", "compiler.err.illegal.start.of.type");
        rules.put("unreported exception", "compiler.err.unreported.exception.need.to.catch.or.throw");
        rules.put("non-static", "compiler.err.non-static.cant.be.ref");
        rules.put("has private access", "compiler.err.report.access");
        rules.put("cannot be instantiated", "compiler.err.abstract.cant.be.instantiated");
        rules.put("does not exist", "compiler.err.doesnt.exist");
        rules.put("is not abstract and does not override", "compiler.err.does.not.override.abstract");
        rules.put("expected", "compiler.err.expected");
    }

    // Jibril contributed this code: added message-based compile error classification
    /**
     * Maps a compiler message to the first matching javac-style code.
     *
     * @param message compiler message to classify
     * @return a derived code, or {@link #UNKNOWN} for blank or unrecognized messages
     */
    public String classify(String message) {
        if (message == null || message.isBlank()) {
            return UNKNOWN;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            if (lower.contains(rule.getKey())) {
                return rule.getValue();
            }
        }
        return UNKNOWN;
    }
}
