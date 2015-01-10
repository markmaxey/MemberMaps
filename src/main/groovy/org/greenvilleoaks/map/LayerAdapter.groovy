package org.greenvilleoaks.map

import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.*
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


    /** Creates a layer using the table provided. */
    public Layer createLayer(final Table table, final List<DisplayRule> displayRules) throws IOException {
        VectorStyle style = new VectorStyle()
                .setType("displayRule")
                .setDisplayRules(displayRules);


        // Build a new layer using the styles defined above and render using the supplied table.
        Layer newLayer = new Layer()
                .setLayerType("vector")
                .setName("Population Growth 2010")
                .setProjectId(table.getProjectId())
                .setDatasources(Arrays.asList(new Datasource().setId(table.getId())))
                .setStyle(style);

        return engine.layers().create(newLayer)
                .setProcess(true) // flag that this layer should be processed immediately
                .execute();
    }
    
    
    public ZoomLevels zoomLevels(final int minZoomLevel, final int maxZoomLevel) {
        return new ZoomLevels().setMin(minZoomLevel).setMax(maxZoomLevel)
    }



    public Filter filter(final String columnName, final StringOperatorEnum operator, final String value) {
        return new Filter()
                .setColumn(columnName)
                .setOperator(operator.toString())
                .setValue(value)
    }



    public Filter filter(final String columnName, final NumOperatorEnum operator, final long value) {
        return new Filter()
                .setColumn(columnName)
                .setOperator(operator.toString())
                .setValue(value)
    }
    
    
    public DisplayRule displayRule(final ZoomLevels zoomLevels, final List<Filter> filters) {
        // Define a rule to capture growth >0 and style it as a scaled blue circle relative to the
        // magnitude of the population growth
        return new DisplayRule()
                .setZoomLevels(zoomLevels)
                .setPointOptions(new PointStyle()
                .setIcon(new IconStyle()
                .setScaledShape(new ScaledShape()
                .setShape("circle")
                .setFill(new Color().setColor("blue").setOpacity(0.5))
                .setBorder(new Border().setColor("blue").setWidth(1.0)))
                .setScalingFunction(new ScalingFunction()
                .setColumn("POP_GROWTH")
                .setSizeRange(new SizeRange().setMin(1.0).setMax(100.0))
                .setValueRange(new ValueRange().setMin(0.0).setMax(9.6022000624)))))
                .setFilters(filters);
    }
    
    


    /** Updates the style of the layer to include an icon for zero population growth. */
    public void updateLayerStyle(Layer layer, List<DisplayRule> displayRules) throws IOException {
        // Build a shell layer containing just the styles
        VectorStyle style = new VectorStyle()
                .setType("displayRule")
                .setDisplayRules(displayRules);
        Layer newLayer = new Layer().setStyle(style);

        // And patch!
        engine.layers().patch(layer.getId(), newLayer).execute();
    }

    
    /** Block until the provided layer has been marked as processed. Returns the new layer. */
    private Layer processLayer(Layer layer) throws IOException {
        // Initiate layer processing.
        try {
            engine.layers().process(layer.getId()).execute();
        } catch (GoogleJsonResponseException ex) {
            // We only continue if there is exactly one error, as >1 error indicates an additional,
            // unknown problem that we are unable to handle. Zero errors is also unexpected.
            if (ex.getDetails().getErrors().size() == 1) {
                GoogleJsonError.ErrorInfo error = ex.getDetails().getErrors().get(0);
                // If we "fail" because the layer is already processed, then it's safe to continue. In
                // any other case we want to re-throw the error.
                if (!"processingUpToDate".equals(error.getReason())) {
                    log.error(ex.message, ex)
                    throw ex;
                }
            } else {
                log.error(ex.message, ex)
                throw ex;
            }
        }

        while (!"complete".equals(layer.getProcessingStatus())) {
            // This is safe to run in a while loop as it executes synchronously and we have used a
            // BackOffWhenRateLimitedRequestInitializer when creating the mapsEngine.
            try {
                layer = engine.layers().get(layer.getId()).execute();
                log.info(".")
            } catch (IOException ex) {
                log.error(ex.message, ex)
            }
        }
        return layer;
    }


    /** Publishes the layer, making it visible. */
    public PublishResponse publishLayer(Layer layer) throws IOException {
        return engine.layers().publish(layer.getId()).execute();
    }


    /** Deletes the provided layers, including any maps where they are used. */
    public void deleteLayers(final Set<String> layerIds,
                             final MapAdapter mapAdapter) throws IOException {
        for (String layerId : layerIds) {
            assertLayerIsNotPublished(layerId);

            log.info("Layer ID: " + layerId + ", finding maps.");
            ParentsListResponse layerParents = engine.layers().parents().list(layerId).execute();
            // Delete each layer. Note that these operations are not transactional,
            // so if a later operation fails, the earlier assets will still be deleted.
            for (Parent layerParent : layerParents.getParents()) {
                String mapId = layerParent.getId();
                mapAdapter.deleteMapLayers(layerIds, mapId);
            }

            log.info("Deleting layer.");
            engine.layers().delete(layerId).execute();
            log.info("Layer deleted.");
        }
    }


    /** Ensures that a layer is not published. Useful to test before deleting. */
    public void assertLayerIsNotPublished(String layerId) throws IOException {
        boolean publishedVersionExists;
        try {
            engine.layers().get(layerId).setVersion("published").execute();
            publishedVersionExists = true;
        } catch (GoogleJsonResponseException ex) {
            log.error(ex.message, ex)
            // The API failed to retrieve a published version.
            publishedVersionExists = false;
        }

        if (publishedVersionExists) {
            throw new AssertionError("Layer ID " + layerId + " is published, "
                    + "please un-publish before deleting.");
        }
    }
}