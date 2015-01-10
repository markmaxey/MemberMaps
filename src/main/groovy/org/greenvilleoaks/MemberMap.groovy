package org.greenvilleoaks

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.mapsengine.MapsEngine
import groovy.util.logging.Log4j
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.map.MapEngineFactory
import org.greenvilleoaks.map.Workflow
import org.greenvilleoaks.view.View
import org.greenvilleoaks.view.Views

/**
 * This is the "main" class containing all the control logic.
 */
@Log4j
class MemberMap {
    /**
     * The high level workflow logic
     * @param argv
     */
    public static void main(final String[] argv) {
        Config config = Config.loadConfig(argv)
        
        LogSetup.addFileLogAppender(config.memberStatsDirName)
        LogSetup.enableHttpLogging()
        
        log.info("Generating a members map and spreadsheet ...")
        log.info(config.toString())

        List<MemberBean> members = new Members(config).createMembers()

        Map<String, View> views = new Views().createAndStoreViews(config, members)

        if (System.getProperty("Map", null)) {
            MapsEngine mapsEngine = new MapEngineFactory().createMapEngine(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    config.centralPointName,
                    new File(config.google.jsonKeyFileName))
                    
            Workflow workflow = new Workflow(members,views, config.membersCsvColumnMappings, mapsEngine)

            workflow.run(config.google.mapsEngineProjectId, config.memberStatsDirName + "\\" + "Members.csv")
        }
    }
}
