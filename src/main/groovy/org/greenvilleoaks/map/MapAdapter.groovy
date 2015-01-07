package org.greenvilleoaks.map

import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.Map
import groovy.transform.Immutable
import groovy.util.logging.Log4j

@Immutable
@Log4j
class MapAdapter {
    private MapsEngine engine

    public List<Map> findAllMaps() {
        log.info("Finding all maps ...")
        List<Map> maps = engine.maps().list().execute().getMaps()

        if (maps.isEmpty()) {
            log.info("No maps were found.")
        }
        else {
            log.info("Found ${maps.size()} maps:")
            maps.each { log.info("\tname = '${it.getName()}' id = ${it.getId()}") }
        }

        return maps
    }
}
