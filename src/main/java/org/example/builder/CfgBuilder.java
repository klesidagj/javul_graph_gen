package org.example.builder;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Builds a basic Control Flow Graph (CFG) for a method.
 */
public class CfgBuilder implements GraphBuilder{

    private static final Logger log = Logger.getLogger(CfgBuilder.class.getName());

    public Map<String, Object> build(MethodDeclaration md) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            if (md == null) {
                log.warning("CFG build failed: MethodDeclaration is null");
                return error("null_method_declaration");
            }

            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Integer>> edges = new ArrayList<>();

            AtomicInteger nextId = new AtomicInteger(0);

            class CNode {
                int id;
                String type;
                String code;
                CNode(int id, String t, String c) {
                    this.id = id;
                    this.type = t;
                    this.code = c;
                }
            }

            List<CNode> cNodes = new ArrayList<>();

            class Builder {

                int addNode(String type, String code) {
                    int id = nextId.getAndIncrement();
                    if (code.length() > 300) code = code.substring(0, 300);
                    cNodes.add(new CNode(id, type, code));
                    return id;
                }

                int[] buildStmt(Statement st) {
                    if (st instanceof IfStmt ifs) {
                        int id = addNode("If", ifs.getCondition().toString());

                        int[] thenFlow = buildStmt(ifs.getThenStmt());
                        if (thenFlow[0] != -1)
                            edges.add(Map.of("from", id, "to", thenFlow[0]));

                        if (ifs.getElseStmt().isPresent()) {
                            int[] elseFlow = buildStmt(ifs.getElseStmt().get());
                            if (elseFlow[0] != -1)
                                edges.add(Map.of("from", id, "to", elseFlow[0]));
                        }

                        return new int[]{id, id};
                    }

                    if (st instanceof ForStmt || st instanceof WhileStmt || st instanceof DoStmt) {
                        int id = addNode("Loop", st.toString());

                        Statement body = (st instanceof ForStmt)
                                ? ((ForStmt) st).getBody()
                                : (st instanceof WhileStmt)
                                ? ((WhileStmt) st).getBody()
                                : ((DoStmt) st).getBody();

                        int[] entry = buildStmt(body);
                        edges.add(Map.of("from", id, "to", entry[0]));
                        edges.add(Map.of("from", entry[1], "to", id));

                        return new int[]{id, id};
                    }

                    if (st instanceof ReturnStmt) {
                        int id = addNode("Return", st.toString());
                        return new int[]{id, -1};
                    }

                    if (st instanceof BlockStmt block) {
                        int start = -1;
                        int prevExit = -1;

                        for (Statement inner : block.getStatements()) {
                            int[] flow = buildStmt(inner);
                            if (start == -1) start = flow[0];
                            if (prevExit != -1 && flow[0] != -1)
                                edges.add(Map.of("from", prevExit, "to", flow[0]));

                            prevExit = flow[1];
                            if (prevExit == -1) break;
                        }

                        if (start == -1) {
                            int id = addNode("Empty", "");
                            return new int[]{id, id};
                        }

                        return new int[]{start, prevExit};
                    }

                    int id = addNode("Stmt", st.toString());
                    return new int[]{id, id};
                }
            }

            Builder b = new Builder();

            if (md.getBody().isPresent()) {
                BlockStmt block = md.getBody().get();
                b.buildStmt(block);
            }

            for (CNode cn : cNodes) {
                nodes.add(Map.of(
                        "id", cn.id,
                        "type", cn.type,
                        "code", cn.code
                ));
            }

            result.put("nodes", nodes);
            result.put("edges", edges);
            return result;

        } catch (Exception ex) {
            log.severe("CFG builder exception: " + ex.getMessage());
            return error("cfg_builder_exception");
        }
    }

    private Map<String, Object> error(String msg) {
        return Map.of("error", msg);
    }
}