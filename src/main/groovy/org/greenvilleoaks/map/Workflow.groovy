package org.greenvilleoaks.map

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.mapsengine.MapsEngine
import groovy.util.logging.Log4j
import org.greenvilleoaks.LogSetup
import org.greenvilleoaks.Members
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.config.CsvColumnMappings
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

        List<MemberBean> members = new Members(config).loadMembers()

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
                System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\Output.test\\2015-01-02 18.04.07\\MembersSmall.csv"
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


    public void run(final String projectId, final String fileName) {
        tablesWrapper.publishTable("Members", projectId, [fileName], layerAdapter, mapAdapter)
    }
}
