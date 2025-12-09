package org.example.builder;

import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.Map;

public interface GraphBuilder {
    Map<String, Object> build(MethodDeclaration md);
}
