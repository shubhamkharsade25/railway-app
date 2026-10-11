package com.railway;

import com.railway.config.QueryLabCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryLabCatalogTest {

    @Test
    void allQueriesAreReadOnlyAndStartWithSelectWithOrExplain() {
        List<QueryLabCatalog.LabQuery> queries = QueryLabCatalog.getAll();
        assertFalse(queries.isEmpty(), "Catalog should contain queries");

        for (QueryLabCatalog.LabQuery q : queries) {
            String sql = q.sql().trim().toUpperCase();
            boolean valid = sql.startsWith("SELECT") || sql.startsWith("WITH") || sql.startsWith("EXPLAIN");
            assertTrue(valid, "Query " + q.id() + " must start with SELECT, WITH, or EXPLAIN. Was: " + sql);
            assertFalse(sql.contains("DROP"), "Query must not contain DROP");
            assertFalse(sql.contains("DELETE"), "Query must not contain DELETE");
            assertFalse(sql.contains("UPDATE"), "Query must not contain UPDATE");
            assertFalse(sql.contains("INSERT"), "Query must not contain INSERT");
        }
    }
}
