package org.example.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.example.model.MethodResult;

public abstract class AbstractParserStrategy implements ParserStrategy {

   /* ---------------------------------------------------
       Shared utilities for ALL parsing strategies
    ----------------------------------------------------- */

    protected String sanitize(String raw) {
        if (raw == null) return "";
        String s = raw.replace("\u0000", "");
        s = s.replaceAll("[\\p{C}&&[^\\n\\t]]", "");
        return s.trim();
    }

    protected String repairSignature(String code) {
        if (code == null) return null;
        String s = code.trim();
        if (s.isEmpty()) return s;

        // fix common "public" typos in Juliet/Owasp
        s = s.replaceFirst("(?i)^(for|fur|pubic|ptic)\\b", "public");

        // Bare identifier like "foo(...)" → prepend "public void "
        if (s.matches("^[A-Za-z_]\\w*\\s*\\(.*")) {
            s = "public void " + s;
        }

        // signature-only → append {}
        String firstLine = s.split("\\R", 2)[0];
        if (firstLine.contains("(") && !s.contains("{") && !firstLine.endsWith(";")) {
            s += " {}";
        }

        return s;
    }

//    /** Wrap invalid content into a synthetic method so JavaParser can parse it. */
//    protected MethodDeclaration wrapAsSyntheticMethod(String code, String methodName) {
//        try {
//            String wrapped = code.startsWith("{") ? code : ("{" + code + "}");
//            BlockStmt block = StaticJavaParser.parseBlock(wrapped);
//
//            MethodDeclaration md = new MethodDeclaration();
//            md.setName(methodName);
//            md.setType("void");
//            md.setBody(block);
//            return md;
//
//        } catch (Exception ex) {
//            return null;
//        }
//    }

    /** Attempt direct method parsing. */
    protected MethodDeclaration tryParseMethod(String code) {
        try {
            return StaticJavaParser.parseMethodDeclaration(code);
        } catch (Exception ex) {
            return null;
        }
    }

    /** Try parse compilation unit and extract first method. */
    protected MethodDeclaration tryParseClass(String code) {
        try {
            return StaticJavaParser.parse(code)
                    .findFirst(MethodDeclaration.class)
                    .orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    /* ---------------------------------------------------
       Helper to build a MethodResult object
    ----------------------------------------------------- */
    protected MethodResult buildResult(String name,
                                       String strategy,
                                       MethodDeclaration md) {

        MethodResult r = new MethodResult(name);
        r.setParseStrategy(strategy);
        r.setMethodDeclaration(md);
        return r;
    }
}