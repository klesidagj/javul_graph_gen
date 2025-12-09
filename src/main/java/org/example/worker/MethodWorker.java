package org.example.worker;

import org.example.config.Config;
import org.example.model.MethodResult;
import org.example.model.ExtractionResult;
import org.example.parser.MethodLevelParser;
import org.example.service.GraphExtractorImpl;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

public class MethodWorker {

    private static final Logger log = Logger.getLogger(MethodWorker.class.getName());
    private final Config cfg;
    private final GraphExtractorImpl extractor;

    public MethodWorker(Config cfg) {
        this.cfg = cfg;
        this.extractor = new GraphExtractorImpl(cfg, new MethodLevelParser());
    }

    public void run() throws Exception {

        String selectSql = """
            SELECT id, raw_code FROM %s
            WHERE source = 'CVEFixes'
              AND ast_graph IS NULL
            LIMIT ?
            """.formatted(cfg.getDbTable());

        String updateSql = """
            UPDATE %s
            SET ast_graph = ?::jsonb,
                cfg_graph = ?::jsonb,
                dfg_graph = ?::jsonb
            WHERE id = ?
            """.formatted(cfg.getDbTable());

        try (Connection conn = DriverManager.getConnection(
                cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword());
             PreparedStatement sel = conn.prepareStatement(selectSql);
             PreparedStatement upd = conn.prepareStatement(updateSql)) {

            sel.setInt(1, cfg.getLimit());
            ResultSet rs = sel.executeQuery();

            conn.setAutoCommit(false);

            int processed = 0;
            int batch = 0;

            while (rs.next()) {
                UUID id = (UUID) rs.getObject("id");
                String raw = rs.getString("raw_code");

                log.info("Processing CVEfixes id=" + id);
                log.fine("Raw code:\n" + raw);

                ExtractionResult er = extractor.extractFromSource(raw);

                boolean hasGood = er.getMethods().stream().anyMatch(MethodResult::isSuccess);
                if (!hasGood) {
                    log.warning("Skipping id " + id + " — parsing failed");
                    continue;
                }

                for (MethodResult m : er.getMethods()) {

                    if (!m.isSuccess()) {
                        log.warning("Method skipped (failed parse): " + m.getName());
                        continue;
                    }

                    upd.setString(1, m.getAstJson());
                    upd.setString(2, m.getCfgJson());
                    upd.setString(3, m.getDfgJson());
                    upd.setObject(4, id);
                    upd.addBatch();
                    batch++;
                }

                if (++processed % cfg.getBatchSize() == 0) {
                    upd.executeBatch();
                    conn.commit();
                    log.info("Committed CVE batch at " + processed + " rows");
                    batch = 0;
                }
            }

            if (batch > 0) {
                upd.executeBatch();
                conn.commit();
            }

            log.info("MethodWorker (CVEfixes) completed. Total processed: " + processed);
        }
    }
}