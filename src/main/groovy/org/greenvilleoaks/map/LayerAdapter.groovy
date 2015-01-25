package org.greenvilleoaks.map

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.*
import groovy.transform.Immutable
import groovy.util.logging.Log4j

@Immutable
@Log4j
class LayerAdapter {
    private MapsEngine engine
    
    private final ZoomLevels ALL_ZOOM_LEVELS = zoomLevels(0, 24)
    private final List<Filter> NO_FILTERS = []
    private final PointStyle gx_measle_blue = createPointStyle(createIconStyle())

    public List<Layer> findAllLayers() {
        log.info("Finding all layers ...")
        List<Layer> layers = engine.layers().list().execute().getLayers()
        log.info("Found all layers ...")
        
        if (layers.isEmpty()) {
            log.info("No layers were found.")
        }
        else {
            log.info("Found ${layers.size()} layers:")
            layers.each { log.info("\tname = '${it.getName()}' id = ${it.getId()}") }
        }
        
        return layers
    }


    public void deleteLayerByName(
            final String layerName,
            final MapAdapter mapAdapter) {
        Set<String> layerIds = [] as Set
        findLayerByName(layerName).each { layerIds << it.getId() }
        deleteLayersByLayerId(layerIds, mapAdapter)
    }


    Collection<Layer> findLayerByName(String layerName) {
        return findAllLayers().findAll { it.getName().equals(layerName) }
    }
    
    
    /** Creates a layer using the table provided. */
    public Layer createLayer(
            final String layerName,
            final String projectId,
            final String tableId,
            final VectorStyle style
    ) throws IOException {
        log.info("Creating layer '$layerName'")
        
        // Build a new layer using the styles defined above and render using the supplied table.
        Layer newLayer = new Layer()
                .setLayerType("vector")
                .setName(layerName)
                .setProjectId(projectId)
                .setDatasources(Arrays.asList(new Datasource().setId(tableId)))
                .setStyle(style);
        
        Layer layer = engine.layers().create(newLayer)
                .setProcess(true) // flag that this layer should be processed immediately
                .execute();

        log.info("Created layer '$layerName'")
        
        if (layer.getStyle().getDisplayRules().size() != style.getDisplayRules().size()) {
            throw new RuntimeException("Layer '$layerName' was successfully created but something about the display rules was invalid")
        }
        else {
            for(int ndx=0; ndx<style.getDisplayRules().size(); ndx++) {
                if (style.getDisplayRules().get(ndx).filters.size() !=
                        layer.getStyle().getDisplayRules().get(ndx).filters.size())
                    throw new RuntimeException("Layer '$layerName' was successfully created but something about filter '$ndx' was invalid")
            }
        }
        
        return layer
    }
    
    
    public VectorStyle createStyle(
            final List<DisplayRule> displayRules,
            final String featureInfoContent=null) {
        VectorStyle vectorStyle = new VectorStyle()
                .setType("displayRule")
                .setDisplayRules(displayRules);
        
        if (featureInfoContent) vectorStyle.setFeatureInfo(new FeatureInfo().setContent(featureInfoContent))
        
        return vectorStyle
    }
    
    
    public ZoomLevels zoomLevels(final int minZoomLevel, final int maxZoomLevel) {
        return new ZoomLevels().setMin(minZoomLevel).setMax(maxZoomLevel)
    }



    public Filter createFilter(final String columnName, final StringOperatorEnum operator, final String value) {
        return new Filter()
                .setColumn(columnName)
                .setOperator(operator.toString())
                .setValue(value)
    }



    public Filter createFilter(final String columnName, final NumOperatorEnum operator, final long value) {
        return new Filter()
                .setColumn(columnName)
                .setOperator(operator.toString())
                .setValue(Long.toString(value))
    }
    

    
    
    public DisplayRule createDisplayRule(
            final PointStyle pointStyle=gx_measle_blue,
            final List<Filter> filters=NO_FILTERS,
            final ZoomLevels zoomLevels=ALL_ZOOM_LEVELS) {
        return new DisplayRule()
                .setZoomLevels(zoomLevels)
                .setPointOptions(pointStyle)
                .setFilters(filters);
    }


    public PointStyle createPointStyle(final StockIconNames stockIconName) {
        new PointStyle().setIcon(createIconStyle(stockIconName))
    }

    public PointStyle createPointStyle(final IconStyle iconStyle) {
        new PointStyle().setIcon(iconStyle)
    }


    public IconStyle createIconStyle(final StockIconNames stockIconName=StockIconNames.gx_measle_blue) {
        new IconStyle().setName(stockIconName.toString())
    }


    public IconStyle createIconStyle(
            final ScaledShape scaledShape,
            final ScalingFunction scalingFunction) {
        new IconStyle()
                .setScaledShape(scaledShape)
                .setScalingFunction(scalingFunction)
    }
    
    
    public ScaledShape createScaledShape(
            final String shapeName="circle",
            final String fillColor="blue",
            final double fillOpacity=0.5,
            final String borderColor="blue",
            final double borderWidth=1.0
    ) {
        new ScaledShape()
                .setShape(shapeName)
                .setFill(new Color().setColor(fillColor).setOpacity(fillOpacity))
                .setBorder(new Border().setColor(borderColor).setWidth(borderWidth))
    }
    
    
    public ScalingFunction createScalingFunction(
            final String columnName,
            double sizeRangeMin,
            double sizeRangeMax,
            double valueRangeMin,
            double valueRangeMax
    ) {
        new ScalingFunction()
                .setColumn(columnName)
                .setSizeRange(new SizeRange().setMin(sizeRangeMin).setMax(sizeRangeMax))
                .setValueRange(new ValueRange().setMin(valueRangeMin).setMax(valueRangeMax))
    }
    
    


    /** Updates the style of the layer to include an icon for zero population growth. */
    public void updateLayerStyle(Layer layer, List<DisplayRule> displayRules) throws IOException {
        // Build a shell layer containing just the styles
        VectorStyle style = new VectorStyle()
                .setType("displayRule")
                .setDisplayRules(displayRules);
        Layer newLayer = new Layer().setStyle(style);

        // And patch!
        log.info("Patching ${layer.getName()} ...")
        engine.layers().patch(layer.getId(), newLayer).execute();
        log.info("Patched ${layer.getName()}")
    }

    
    /** Block until the provided layer has been marked as processed. Returns the new layer. */
    private Layer processLayerAndWaitUntilProcessingDone(Layer layer) throws IOException {
        processLayer(layer)
        
        return waitUntilProcessingDone(layer)
    }

    
    /** Process a layer */
    protected void processLayer(Layer layer) {
        try {
            log.info("Processing ${layer.getName()} ...")
            engine.layers().process(layer.getId()).execute()
            log.info("Processed ${layer.getName()}")
        } catch (GoogleJsonResponseException ex) {
            // TODO: This logic doesn't work because the details in the exception is null
            log.error("Process layer ${layer.getName()} is ", ex)
            /*
            // We only continue if there is exactly one error, as >1 error indicates an additional,
            // unknown problem that we are unable to handle. Zero errors is also unexpected.
            if (ex.getDetails() && ex.getDetails().getErrors().size() == 1) {
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
            */
        }
    }



    /** @return the Layer when its processing has reached a terminal state */
    protected Layer waitUntilProcessingDone(Layer layer) {
        String processingStatus = layer.getProcessingStatus()
        log.info("Layer ${layer.getName()} has processing status '$processingStatus'")

        while ("complete".equals(processingStatus) || "failed".equals(processingStatus)) {
            // This is safe to run in a while loop as it executes synchronously and we have used a
            // BackOffWhenRateLimitedRequestInitializer when creating the mapsEngine.
            try {
                sleep(1000) // Unnecessary (?) but just in case, slow things down ...
                layer = engine.layers().get(layer.getId()).execute();
                processingStatus = layer.getProcessingStatus()
                log.info("Layer ${layer.getName()} has processing status '$processingStatus'")
            } catch (IOException ex) {
                log.error(ex.message, ex)
            }
        }

        return layer;
    }



    /** Publishes the layer, making it visible. */
    public PublishResponse publishLayer(Layer layer) throws IOException {
        log.info("Publishing ${layer.getName()} ...")
        PublishResponse response = engine.layers().publish(layer.getId()).execute();
        log.info("Published ${layer.getName()}")
    }


    /** Deletes the provided layers, including any maps where they are used. */
    public void deleteLayersByLayerId(
            final Set<String> layerIds,
            final MapAdapter mapAdapter) throws IOException {
        log.info("Deleting layers " + layerIds)
        
        for (String layerId : layerIds) {
            assertLayerIsNotPublished(layerId);

            log.info("Layer ID: " + layerId + ", finding maps.");
            ParentsListResponse layerParents = engine.layers().parents().list(layerId).execute();
            log.info("Layer ID: " + layerId + ", found maps.");
            
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
            if (!"404 Not Found".equals(ex.message)) log.error(ex.message, ex)
            // The API failed to retrieve a published version.
            publishedVersionExists = false;
        }

        if (publishedVersionExists) {
            throw new AssertionError("Layer ID " + layerId + " is published, "
                    + "please un-publish before deleting.");
        }
    }


    public void createLayerEvenIfItAlreadyExist(
            final String layerName,
            final MapAdapter mapAdapter,
            final String projectId,
            final String tableId,
            final String featureInfoContent,
            final List<DisplayRule> displayRules
    ) {
        createLayerEvenIfItAlreadyExist(
                layerName, mapAdapter, projectId, tableId, createStyle(displayRules, featureInfoContent))
    }




    public void createLayerEvenIfItAlreadyExist(
            final String layerName,
            final MapAdapter mapAdapter,
            final String projectId,
            final String tableId,
            final VectorStyle vectorStyle
    ) {
        log.info("Creating layer '$layerName' even if it already exists ...")

        deleteLayerByName(layerName, mapAdapter)

        Layer layer = createLayer(layerName, projectId, tableId, vectorStyle)
        
        layer = waitUntilProcessingDone(layer)

        log.info("Layer $layerName was created with status ${layer.getProcessingStatus()}")
    }
}