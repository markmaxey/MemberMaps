package org.greenvilleoaks.map

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.InputStreamContent
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.*
import com.google.maps.clients.mapsengine.geojson.Point
import groovy.transform.Immutable
import groovy.util.logging.Log4j

import java.io.File
import java.util.Map

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



    /** Creates an empty table in your maps mapsEngine account. */
    public Table createTable(
            final String projectId,
            final List<String> fileNames,
            final String tableName,
            final String tableDescription,
            final String schemaPrimaryKey,
            final List<String> tags) throws IOException {
        log.info("Creating an empty table in Maps Engine, under projectAdapter ID " + projectId);

        // Note that we need a com.google.api.services.mapsengine.model.File, not a java.io.File
        List<com.google.api.services.mapsengine.model.File> files = new ArrayList<>(fileNames.size());
        fileNames.each { files <<  new com.google.api.services.mapsengine.model.File().setFilename(it) }

        // Build the table, including the minimal schema
        Table newTable = new Table()
                .setName(tableName)
                .setDescription(tableDescription)
                .setSchema(new Schema().setPrimaryKey(schemaPrimaryKey))
                .setProjectId(projectId)
                .setFiles(files)
                .setTags(tags);

        Table table = engine.tables().upload(newTable).execute();

        log.info("Table created");
        
        return table
    }


    /** Uploads the file data to the empty table. */
    public void uploadFile(final Table table, final String fileName, final String contentType) throws IOException {
        log.info("Uploading table from '$fileName' ...");

        InputStreamContent contentStream = createContentStreamFromFile(fileName, contentType)

        engine.tables().files().insert(table.getId(), fileName, contentStream).execute()

        log.info("Uploaded table");
    }

    
    /** Load the file into a stream */
    protected InputStreamContent createContentStreamFromFile(String fileName, String contentType) {
        File file = new File(fileName);
        InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
        return new InputStreamContent(contentType, fileInputStream);
    }


    /** Adds a new feature to a table. */
    public void insertFeature(final Table table, final Map<String, Object> properties, final Point point) throws IOException {
        // Build the feature by attaching properties
        Feature newFeature = point.asFeature(properties);

        FeaturesBatchInsertRequest insertRequest = new FeaturesBatchInsertRequest()
                .setFeatures(Arrays.asList(newFeature));

        engine.tables().features().batchInsert(table.getId(), insertRequest).execute();
    }


    /** Updates a feature. */
    public void updateFeature(final Table table, final Map<String, Object> properties) throws IOException {
        // Build the feature. We're not changing geometry so we can omit it.
        Feature updateFeature = new Feature()
                .setProperties(properties);

        FeaturesBatchPatchRequest patchRequest = new FeaturesBatchPatchRequest()
                .setFeatures(Arrays.asList(updateFeature));

        engine.tables().features().batchPatch(table.getId(), patchRequest).execute();
    }


    /** Ensures the given ID belongs to a table and that the user can access it. */
    public boolean validateId(final String tableId) throws IOException {
        try {
            Asset asset = engine.assets().get(tableId).execute();
            return "table".equalsIgnoreCase(asset.getType());
        } catch (GoogleJsonResponseException ex) {
            // A "400 Bad Request" is thrown when the asset ID is missing or invalid
            log.error(ex.message, ex)
            return false;
        }
    }



    /** Deletes a table, including any layers displaying the table. */
    public void deleteTableByTableId(
            final String tableId, 
            final LayerAdapter layerAdapter,
            final MapAdapter mapAdapter) throws IOException {
        log.info("Finding layers belonging to table.");
        ParentsListResponse tableParents = engine.tables().parents().list(tableId).execute();
        log.info("Layers retrieved.");

        // Collect the layer IDs to ensure we can safely delete maps.
        Set<String> allLayerIds = new HashSet<String>();
        for (Parent tableParent : tableParents.getParents()) {
            allLayerIds.add(tableParent.getId());
        }

        // We need to delete layers before we can delete the table.
        layerAdapter.deleteLayers(allLayerIds, mapAdapter);

        log.info("Deleting '$tableId' ...")
        engine.tables().delete(tableId).execute();
        log.info("Deleted '$tableId' ...")
    }



    public void deleteTableByTableName(
            final List<Table> tables,
            final String tableName,
            final LayerAdapter layerAdapter,
            final MapAdapter mapAdapter) {
        Table table = findTable(tables, tableName)
        
        if (table) {
            log.info("Found '$tableName'")

            deleteTableByTableId(table.getId(), layerAdapter, mapAdapter)
        }
        else {
            log.info("Could not find a table named '$tableName'")
        }
    }


    public void deleteTableByTableName(
            final String tableName,
            final LayerAdapter layerAdapter,
            final MapAdapter mapAdapter) {
        Table table = findTable(tableName)
        if (table) {
            log.info("Found '$tableName'")

            deleteTableByTableId(table.getId(), layerAdapter, mapAdapter)
        }
        else {
            log.info("Could not find a table named '$tableName'")
        }
    }


    /** Performs a batch insert of data into the table. */
    public void insertData(Table table, List<Feature> features) throws IOException {
        FeaturesBatchInsertRequest payload = new FeaturesBatchInsertRequest()
                .setFeatures(features);
        engine.tables().features().batchInsert(table.getId(), payload).execute();
    }


    public Table findTable(final String tableName) {
        return findAllTables().find { it.getName().equals(tableName) }
    }


    public Table findTable(final List<Table> tables, final String tableName) {
        return tables.find { it.getName().equals(tableName) }
    }


    /** Create a new or replace an existing table with the given CSV file */
    public void publishTable(
            final String tableName,
            final String projectId,
            final List<String> fileNames,
            final LayerAdapter layerAdapter,
            final MapAdapter mapAdapter) {
        deleteTableByTableName(tableName, layerAdapter, mapAdapter)

        Table table = createTable(projectId, fileNames, tableName, "All members", "Unique Id", []);
        
        sleep(10000)

        fileNames.each { uploadFile(table, it, "text/csv") }
    }
}
