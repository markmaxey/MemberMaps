package org.greenvilleoaks.map

import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.Layer
import groovy.transform.Immutable
import groovy.util.logging.Log4j

@Immutable
@Log4j
class LayerAdapter {
    private MapsEngine engine

    public List<Layer> findAllLayers() {
        log.info("Finding all layers ...")
        List<Layer> layers = engine.layers().list().execute().getLayers()
        
        if (layers.isEmpty()) {
            log.info("No layers were found.")
        }
        else {
            log.info("Found ${layers.size()} layers:")
            layers.each { log.info("\tname = '${it.getName()}' id = ${it.getId()}") }
        }
        
        return layers
    }
}
