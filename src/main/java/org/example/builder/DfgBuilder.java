package org.example.builder;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.body.VariableDeclarator;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Basic Data Flow Graph (DFG) for a method.
 * Tracks last definition -> use relationships.
 */
public class DfgBuilder implements GraphBuilder{

    private static final Logger log = Logger.getLogger(DfgBuilder.class.getName());

    public Map<String, Object> build(MethodDeclaration md) {

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            if (md == null) {
                log.warning("DFG build failed: MethodDeclaration is null");
                return error("null_method_declaration");
            }

            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Integer>> edges = new ArrayList<>();

            Map<String, Integer> lastDef = new HashMap<>();
            AtomicInteger nextId = new AtomicInteger(0);

            new TreeVisitor() {
                @Override
                public void process(Node node) {

                    // Variable Definitions
                    if (node instanceof VariableDeclarator vd) {
                        String name = vd.getNameAsString();
                        int id = nextId.getAndIncrement();

                        nodes.add(Map.of(
                                "id", id,
                                "kind", "Def",
                                "var", name
                        ));

                        lastDef.put(name, id);
                    }

                    // Assignments
                    else if (node instanceof AssignExpr ae) {
                        if (ae.getTarget().isNameExpr()) {
                            String name = ae.getTarget().asNameExpr().getNameAsString();
                            int id = nextId.getAndIncrement();

                            nodes.add(Map.of(
                                    "id", id,
                                    "kind", "Def",
                                    "var", name
                            ));

                            lastDef.put(name, id);
                        }
                    }

                    // Uses
                    else if (node instanceof NameExpr ne) {
                        String name = ne.getNameAsString();
                        Integer defNode = lastDef.get(name);

                        int useId = nextId.getAndIncrement();
                        nodes.add(Map.of(
                                "id", useId,
                                "kind", "Use",
                                "var", name
                        ));

                        if (defNode != null) {
                            edges.add(Map.of("from", defNode, "to", useId));
                        }
                    }
                }
            }.visitPreOrder(md);

            result.put("nodes", nodes);
            result.put("edges", edges);
            return result;

        } catch (Exception ex) {
            log.severe("DFG builder exception: " + ex.getMessage());
            return error("dfg_builder_exception");
        }
    }

    private Map<String, Object> error(String msg) {
        return Map.of("error", msg);
    }
}