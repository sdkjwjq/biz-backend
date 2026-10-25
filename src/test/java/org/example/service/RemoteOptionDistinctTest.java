package org.example.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RemoteOptionDistinctTest {
 @Test void normalizesOnlyOuterValues(){assertEquals("SELECT value,label FROM (SELECT DISTINCT id FROM t) x",RemoteOptionPages.withoutOuterDistinct("SELECT DISTINCT value,label FROM (SELECT DISTINCT id FROM t) x"));}
 @Test void preservesCteMaterializationBoundary(){assertEquals("WITH ids AS (SELECT DISTINCT id FROM t) SELECT value,label FROM ids",RemoteOptionPages.withoutOuterDistinct("WITH ids AS (SELECT DISTINCT id FROM t) SELECT DISTINCT value,label FROM ids"));}
 @Test void quotedTextIsNeverRewritten(){assertEquals("SELECT 'SELECT DISTINCT',value FROM t",RemoteOptionPages.withoutOuterDistinct("SELECT 'SELECT DISTINCT',value FROM t"));}
}
