package org.greenvilleoaks.map

import com.google.api.client.util.DateTime
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.*
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


    /** Creates a map using the layer provided */
    public Map createMap(Layer layer) throws IOException {
        Map newMap = new Map()
                .setName("Population growth (map1)")
                .setProjectId(layer.getProjectId());

        MapLayer mapLayer = new MapLayer()
                .setId(layer.getId())
                .setKey("map1-layer1");

        List<MapItem> layers = new ArrayList<>();
        layers.add(mapLayer);

        newMap.setContents(layers);

        // Map processing is triggered automatically, so no need to set a flag during creation.
        return engine.maps().create(newMap).execute();
    }

    /** Marks the provided map as "published", making it visible. */
    public PublishResponse publishMap(Map map) throws IOException {
        String processingStatus = null;

        // Initially the map will be in a 'processing' state and will return '409 Conflict'
        // while processing is happening. Poll until it's ready.
        while (!"complete".equals(processingStatus)) {
            // Note that if you are using the Maps Engine API Wrapper there is no need to sleep between
            // requests, as it will automatically retry any 'rate limit exceeded' errors.
            processingStatus = engine.maps().get(map.getId()).execute().getProcessingStatus();
        }

        return engine.maps().publish(map.getId()).execute();
    }

    /** Makes the map publicly visible. */
    public PermissionsBatchUpdateResponse setPermissions(Map map) throws IOException {
        PermissionsBatchUpdateRequest request = new PermissionsBatchUpdateRequest()
                .setPermissions(Arrays.asList(new Permission()
                .setId("anyone")
                .setRole("viewer")));

        return engine.maps().permissions().batchUpdate(map.getId(), request).execute();
    }


    // TODO(macd): Update this to edit the map, once available in the API.
    /** Safely deletes a map, as long as all layers contained are scheduled for deletion. */
    public void deleteMap(Set<String> layerIdsPendingDeletion, String mapId) throws IOException {
        assertMapIsNotPublished(mapId);

        log.info("Checking for other layers on this map (ID: " + mapId + ")");
        Set<String> mapLayerIds = getLayerIdsFromMap(mapId);

        // Determine if this map will still have layers once we perform our delete.
        mapLayerIds.removeAll(layerIdsPendingDeletion);
        if (mapLayerIds.size() == 0) {
            // Map will not contain any more Layers when done, so delete it.
            log.info("Deleting map.");
            engine.maps().delete(mapId).execute();
            log.info("Map deleted.");
        } else {
            // Map will contain Layers not scheduled for deletion, so we can't continue.
            throw new IllegalStateException("Map " + mapId + " contains layers not scheduled for "
                    + "deletion. You will need to remove them before we can delete this map.");
        }
    }


    /** Safely deletes the layers from a map, as long as all layers contained are scheduled for deletion. */
    public void deleteMapLayers(Set<String> layerIdsPendingDeletion, String mapId) throws IOException {
        assertMapIsNotPublished(mapId);

        log.info("Checking for other layers on this map (ID: " + mapId + ")");
        Set<String> mapLayerIds = getLayerIdsFromMap(mapId);

        // Determine if this map will still have layers once we perform our delete.
        mapLayerIds.removeAll(layerIdsPendingDeletion);
        if (mapLayerIds.size() == 0) {
            Map cloneOfMapWithoutLayers = cloneMap(mapId).setContents(new ArrayList<MapItem>())

            // Map will not contain any more Layers when done, so delete it.
            log.info("Deleting map layers.");
            engine.maps().patch(mapId, cloneOfMapWithoutLayers).execute();
            log.info("Map layers deleted.");
        } else {
            // Map will contain Layers not scheduled for deletion, so we can't continue.
            throw new IllegalStateException("Map " + mapId + " contains layers not scheduled for "
                    + "deletion. You will need to remove them before we can delete this map.");
        }
    }


    public Map cloneMap(String mapId) {
        Map map = engine.maps().get(mapId).execute();
        return new Map()
                .setName(map.getName())
                .setBbox(map.getBbox())
                .setContents(map.getContents())
                .setContents(new ArrayList<MapItem>())
                .setCreationTime(map.getCreationTime())
                .setCreatorEmail(map.getCreatorEmail())
                .setDefaultViewport(map.getDefaultViewport())
                .setDescription(map.getDescription())
                .setDraftAccessList(map.getDraftAccessList())
                .setEtag(map.getEtag())
                .setId(map.getId())
                .setLastModifiedTime(new DateTime(System.currentTimeMillis()))
                .setProcessingStatus(map.getProcessingStatus())
                .setProjectId(map.getProjectId())
                .setPublishedAccessList(map.getPublishedAccessList())
                .setTags(map.getTags())
                .setWritersCanEditPermissions(map.getWritersCanEditPermissions())
                .setVersions(map.getVersions())
    }

    /** Ensures that a map is not published. Useful to test before deleting. */
    public void assertMapIsNotPublished(String mapId) throws IOException {
        Map map = engine.maps().get(mapId).execute();
        if (map.getVersions().contains("published")) {
            throw new AssertionError("Map ID " + mapId + " is published, "
                    + "please un-publish before deleting.");
        }
    }


    /** Finds all layers attached to a map. */
    public Set<String> getLayerIdsFromMap(String mapId) throws IOException {
        // Retrieve the map.
        Map map = engine.maps().get(mapId).execute();

        // Find the layers
        Set<String> layerIds = new HashSet<String>();
        List<MapItem> mapContents = map.getContents();
        while (mapContents != null && mapContents.size() > 0) {
            MapItem item = mapContents.remove(0);
            if (item instanceof MapLayer) {
                layerIds.add(((MapLayer) item).getId());
            } else if (item instanceof MapFolder) {
                mapContents.addAll(((MapFolder) item).getContents());
            }
            // MapKmlLinks do not have IDs
        }

        return layerIds;
    }
}
