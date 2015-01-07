package org.greenvilleoaks.map

import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.Table
import groovy.transform.Immutable
import groovy.util.logging.Log4j

@Immutable
@Log4j
class TablesAdapter {
    private MapsEngine engine

    public List<Table> findAllTables() {
        log.info("Finding all tables ...")
        List<Table> tables = engine.tables().list().execute().getTables()

        if (tables.isEmpty()) {
            log.info("No tables were found.")
        }
        else {
            log.info("Found ${tables.size()} tables:")
            tables.each { log.info("\tname = '${it.getName()}' id = ${it.getId()}") }
        }

        return tables
    }
}
