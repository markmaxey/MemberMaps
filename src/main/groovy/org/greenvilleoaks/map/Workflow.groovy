package org.greenvilleoaks.map

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import com.google.api.services.mapsengine.model.Table
import groovy.util.logging.Log4j
import org.greenvilleoaks.LogSetup
import org.greenvilleoaks.Members
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.config.MapInfo
import org.greenvilleoaks.view.View
import org.greenvilleoaks.view.Views

/**
 * https://developers.google.com/maps-mapsEngine/documentation/workflow-overview
 */
@Log4j
final class Workflow {
    private final MapsEngine mapsEngine
    private final List<MemberBean>  members
    private final Map<String, View> views
    private final CsvColumnMappings csvColumnMappings

    private final ProjectAdapter projectAdapter
    private final LayerAdapter layerAdapter
    private final MapAdapter mapAdapter
    private final TablesAdapter  tablesWrapper

    
    public static void main(String[] argv) {
        LogSetup.enableHttpLogging()

        Config config = Config.loadConfig(argv)

        String fileName = (argv.length == 0) ? System.properties.getProperty("user.home") +
                "\\Documents\\GO_Members_Map\\Output\\2015-01-16 06.22.30\\Members.csv" : argv[0]
        List<MemberBean> members = new Members(config).loadMembers(fileName)

        Workflow workflow = new Workflow(
                members,
                new Views().createViews(config, members),
                config.membersCsvColumnMappings,
                new MapEngineFactory().createMapEngine(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        config.centralPointName,
                        new File(config.google.jsonKeyFileName)))

        workflow.run(
                config.google.mapsEngineProjectId,
                fileName,
                config.publicMap, config.privateMap
        )
    }
    
    
    public Workflow(
            final List<MemberBean> members,
            final Map<String, View> views,
            final CsvColumnMappings csvColumnMappings,
            final MapsEngine mapsEngine) {
        this.members           = members
        this.views             = views
        this.csvColumnMappings = csvColumnMappings
        this.mapsEngine        = mapsEngine

        projectAdapter = new ProjectAdapter(engine: mapsEngine)
        layerAdapter   = new LayerAdapter  (engine: mapsEngine)
        mapAdapter     = new MapAdapter    (engine: mapsEngine)
        tablesWrapper  = new TablesAdapter (engine: mapsEngine)
    }


    public void run(
            final String projectId, 
            final String fileName, 
            final MapInfo publicMapInfo, 
            final MapInfo privateMapInfo) {
        // TODO: Upload the table dynamically instead of hardcoding the table ID
        //Table table = tablesWrapper.uploadTableEvenIfItAlreadyExists("Members", projectId, [fileName], layerAdapter, mapAdapter)
        Table table = tablesWrapper.findTableById("01824222381788524396-01312094446748519596")
        tablesWrapper.deleteLayersAssociatedWithTable(table.getId(), layerAdapter, mapAdapter)

        [publicMapInfo/*, privateMapInfo*/].each { MapInfo mapInfo -> // TODO
            PointStyle pointStyle = layerAdapter.createPointStyle(StockIconNames.valueOf(mapInfo.stockIconName))

            // Create a layer folder for each view
            createViewLayer(
                    views.get("Commute Distance in Miles"), pointStyle, mapInfo.privatePublicQualifier,
                    table.getProjectId(), table.getId(), mapInfo.featureInfoContent)

            // Create a layer for all members
            String layerName = "Everyone" + mapInfo.privatePublicQualifier
            layerAdapter.createLayerEvenIfItAlreadyExist(
                    layerName, mapAdapter, table.getProjectId(), table.getId(), 
                    mapInfo.featureInfoContent, [layerAdapter.createDisplayRule(pointStyle)])
            
/*
            views.each { String viewName, View view ->
                createViewLayer(
                        view, pointStyle, mapInfo.privatePublicQualifier, 
                        table.getProjectId(), table.getId(), mapInfo.featureInfoContent)
            }
*/
        }
    }



    protected void createViewLayer(
            final View view, 
            final PointStyle pointStyle, 
            final String privatePublicQualifier, 
            final String projectId, 
            final String tableId, 
            final String featureInfoContent) {
        Map<String, List<DisplayRule>> category2DisplayRules =
                view.createDisplayRules(csvColumnMappings, layerAdapter, pointStyle)
        category2DisplayRules.each { String category, List<DisplayRule> displayRules ->
            String layerName = view.name + " - " + category + privatePublicQualifier
            layerAdapter.createLayerEvenIfItAlreadyExist(
                    layerName, mapAdapter, projectId, tableId,
                    view.createVectorStyle(layerAdapter, displayRules, featureInfoContent))
        }
    }
}
