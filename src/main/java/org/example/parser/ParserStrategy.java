package org.example.parser;

import org.example.model.MethodResult;
import java.util.List;

public interface ParserStrategy {
    /**
     * Extracts method-level segments from code. Each MethodResult contains:
     * - method name (or synthetic name)
     * - parse strategy description
     * - raw snippet to feed to AST/CFG/DFG builders
     */
    List<MethodResult> extractMethods(String code);
}
