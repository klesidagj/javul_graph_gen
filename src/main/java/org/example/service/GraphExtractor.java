package org.example.service;

import org.example.model.ExtractionResult;

public interface GraphExtractor {
        ExtractionResult extractFromSource(String rawCode);
}
