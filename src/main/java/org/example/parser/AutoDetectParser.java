package org.example.parser;

import org.example.model.MethodResult;
import java.util.List;

public class AutoDetectParser extends AbstractParserStrategy implements ParserStrategy {

    private final MethodLevelParser methodParser = new MethodLevelParser();
    private final ClassLevelParser classParser = new ClassLevelParser();

    @Override
    public List<MethodResult> extractMethods(String code) {

        String clean = sanitize(code);

        // Heuristic: if it contains "class", assume class-level
        if (clean.contains("class ") || clean.contains("interface ")) {
            return classParser.extractMethods(code);
        }

        // Otherwise treat as method-level snippet
        return methodParser.extractMethods(code);
    }
}