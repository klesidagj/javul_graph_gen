package org.example.builder;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Builds a simple AST graph:
 *  - nodes: {id, type, code}
 *  - edges: parent -> child
 */
public class AstBuilder implements GraphBuilder{

    private static final Logger log = Logger.getLogger(AstBuilder.class.getName());

    public Map<String, Object> build(MethodDeclaration md) {

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            if (md == null) {
                log.warning("AST build failed: MethodDeclaration is null");
                return error("null_method_declaration");
            }

            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Integer>> edges = new ArrayList<>();
            Map<Node, Integer> idMap = new IdentityHashMap<>();
            AtomicInteger idGen = new AtomicInteger(0);

            // Assign IDs to each node
            md.walk(node -> {
                int id = idGen.getAndIncrement();
                idMap.put(node, id);

                Map<String, Object> n = new LinkedHashMap<>();
                n.put("id", id);
                n.put("type", node.getClass().getSimpleName());

                String src = node.toString();
                if (src.length() > 300) src = src.substring(0, 300);
                n.put("code", src);

                nodes.add(n);
            });

            // Add parent-child edges
            for (Node parent : idMap.keySet()) {
                int pid = idMap.get(parent);
                for (Node child : parent.getChildNodes()) {
                    Integer cid = idMap.get(child);
                    if (cid != null) {
                        edges.add(Map.of("from", pid, "to", cid));
                    }
                }
            }

            result.put("nodes", nodes);
            result.put("edges", edges);
            return result;

        } catch (Exception ex) {
            log.severe("AST builder exception: " + ex.getMessage());
            return error("ast_builder_exception");
        }
    }

    private Map<String, Object> error(String msg) {
        return Map.of("error", msg);
    }
}