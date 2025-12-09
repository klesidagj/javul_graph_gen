package org.example.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.example.model.MethodResult;

import java.util.ArrayList;
import java.util.List;

public class ClassLevelParser extends AbstractParserStrategy implements ParserStrategy {

    @Override
    public List<MethodResult> extractMethods(String code) {
        List<MethodResult> out = new ArrayList<>();
        String clean = sanitize(code);

        try {
            var cu = com.github.javaparser.StaticJavaParser.parse(clean);
            var classNode = cu.findFirst(ClassOrInterfaceDeclaration.class);

            if (classNode.isPresent()) {
                for (MethodDeclaration md : classNode.get().getMethods()) {
                    MethodResult r = new MethodResult(md.getNameAsString());
                    r.setParseStrategy("class_level");
                    r.setMethodDeclaration(md);
                    out.add(r);
                }
                return out;
            }
        } catch (Exception ignored) {}

        MethodResult r = new MethodResult("__unparsed_class__");
        r.setSuccess(false);
        r.setParseStrategy("failed_all_strategies");
        r.setError("Unable to parse class-level snippet");
        r.setMethodDeclaration(null);
        out.add(r);

        return out;
    }
}