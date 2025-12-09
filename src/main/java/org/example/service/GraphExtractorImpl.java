package org.example.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import org.example.builder.AstBuilder;
import org.example.builder.CfgBuilder;
import org.example.builder.DfgBuilder;
import org.example.config.Config;
import org.example.parser.ParserStrategy;
import org.example.model.ExtractionResult;
import org.example.model.MethodResult;
import org.example.model.SourceType;
import org.example.parser.*;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GraphExtractorImpl implements GraphExtractor {

    private static final Logger log = Logger.getLogger(GraphExtractorImpl.class.getName());

    private final AstBuilder astBuilder = new AstBuilder();
    private final CfgBuilder cfgBuilder = new CfgBuilder();
    private final DfgBuilder dfgBuilder = new DfgBuilder();
    private final JsonGeneratorFactory jsonFactory = Json.createGeneratorFactory(Collections.emptyMap());

    private final ParserStrategy parser;
    private final Config cfg;

    // ---- NEW: proper constructor ----
    public GraphExtractorImpl(Config cfg, ParserStrategy forcedParser) {
        this.cfg = cfg;
        this.parser = forcedParser;
        log.info("Using parser (forced): " + parser.getClass().getSimpleName());
    }


    public ExtractionResult extractFromSource(String rawCode) {
        ExtractionResult result = new ExtractionResult();

        try {
            List<MethodResult> methods = parser.extractMethods(rawCode);
            result.getMethods().addAll(methods);

            for (MethodResult m : methods) {
                if (!m.isSuccess()) {
                    log.warning("Skipping graph: parse failed (" + m.getName() + ")");
                    continue;
                }

                MethodDeclaration md = m.getMethodDeclaration();

                if (cfg.isAstEnabled()) {
                    m.setAstJson(toJson(astBuilder.build(md)));
                }
                if (cfg.isCfgEnabled()) {
                    m.setCfgJson(toJson(cfgBuilder.build(md)));
                }
                if (cfg.isDfgEnabled()) {
                    m.setDfgJson(toJson(dfgBuilder.build(md)));
                }
            }

            return result;

        } catch (Exception ex) {
            result.setOverallError("Extractor exception: " + ex.getMessage());
            log.severe("Extractor failure: " + ex.getMessage());
            return result;
        }
    }


    private String toJson(Map<String, Object> root) {
        try {
            StringWriter out = new StringWriter();
            JsonGenerator gen = jsonFactory.createGenerator(out);

            writeValue(gen, root);  // one unified recursive method
            gen.close();

            return out.toString();

        } catch (Exception e) {
            log.severe("JSON serialization error: " + e.getMessage());
            return "{\"error\":\"json_serialization_exception\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private void writeValue(JsonGenerator gen, Object value) {

        if (value == null) {
            gen.writeNull();
            return;
        }

        // ---------------- primitive ----------------
        if (value instanceof String s) {
            gen.write(s);
            return;
        }
        if (value instanceof Boolean b) {
            gen.write(b);
            return;
        }
        if (value instanceof Number n) {
            gen.write(n.toString());
            return;
        }

        // ---------------- map ----------------
        if (value instanceof Map<?, ?> m) {
            gen.writeStartObject();
            for (var entry : m.entrySet()) {
                gen.writeKey(String.valueOf(entry.getKey()));
                writeValue(gen, entry.getValue());
            }
            gen.writeEnd();
            return;
        }

        // ---------------- list ----------------
        if (value instanceof List<?> list) {
            gen.writeStartArray();
            for (Object item : list) {
                writeValue(gen, item);
            }
            gen.writeEnd();
            return;
        }

        // ---------------- fallback ----------------
        gen.write(value.toString());
    }
}