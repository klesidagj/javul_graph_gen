package org.example.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.example.model.MethodResult;

import java.util.ArrayList;
import java.util.List;

public class MethodLevelParser extends AbstractParserStrategy implements ParserStrategy {

    @Override
    public List<MethodResult> extractMethods(String code) {
        List<MethodResult> list = new ArrayList<>();
        String clean = sanitize(code);
        String repaired = repairSignature(clean);

        MethodDeclaration md =
                tryParseMethod(repaired);

        if (md != null) {
            list.add(buildResult(md.getNameAsString(),
                    "method_direct",
                    md));
            return list;
        }

        // fallback: try wrapped class
        md = tryParseClass("class X { " + repaired + " }");
        if (md != null) {
            list.add(buildResult(md.getNameAsString(),
                    "wrapped_class_method",
                    md));
            return list;
        }

        MethodResult r = new MethodResult("__unparsed__");
        r.setSuccess(false);
        r.setParseStrategy("failed_all_strategies");
        r.setError("Unable to parse method-level snippet");
        r.setMethodDeclaration(null);
        list.add(r);

        return list;
    }
}