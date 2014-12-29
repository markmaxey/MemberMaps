package org.greenvilleoaks

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import groovy.util.logging.Log4j
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.map.Workflow
import org.greenvilleoaks.storage.Spreadsheet
import org.greenvilleoaks.view.*

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
        Config config = loadConfig(argv)
        
        log.info("Generating a members map and spreadsheet ...")
        log.info(config.toString())

        List<MemberBean> members = new Members(config).createMembers()

        Map<String, View> views = createViews(config, members)

        createStatSpreadsheet(config, views.values(), members)

        new Workflow(
                members,
                views,
                new NetHttpTransport(),
                new GsonFactory(),
                config.membersCsvColumnMappings,
                config.google.applicationName,
                new File(config.google.jsonKeyFileName)).run(config.google.projectId)
    }


    private static Config loadConfig(final String[] argv) {
        Config config
        if (argv.length == 0) {
            config = new Config()
        }
        else {
            File configFile = new File(argv[0])
            if (configFile.exists()) {
                config = new ConfigSlurper().parse(new URL("file:///" + argv[0]))
            }
            else {
                config = new Config()
            }
        }
        
        return config.init()
    }


    /**
     * @param members
     * @return various perspectives of the create
     */
    private static Map<String, View> createViews(
            final Config config,
            final List<MemberBean> members) {
        Map<String, View> views = [:]

        views.put(config.membersCsvColumnMappings.city,           new CityView(config.membersCsvColumnMappings.city, members))
        views.put(config.membersCsvColumnMappings.zip,            new ZipView(config.membersCsvColumnMappings.zip, members))
        views.put(config.membersCsvColumnMappings.numInHousehold, new NumInHouseholdView(config.membersCsvColumnMappings.numInHousehold, members))
        views.put(config.membersCsvColumnMappings.age,            new AgeView(config.membersCsvColumnMappings.age, members))
        views.put(config.membersCsvColumnMappings.grade,          new GradeView(config.membersCsvColumnMappings.grade, members))
        views.put(config.membersCsvColumnMappings.role,           new RoleView(config.membersCsvColumnMappings.role, members))
        views.put("Commute Distance in Miles",                    new DistanceView("Commute Distance in Miles", members))
        views.put("Commute Time in Minutes",                      new DurationView("Commute Time in Minutes", members))

        return views
    }


    /**
     * Dump all the perspectives of the create to an Excel workbook
     * @param views
     */
    private static void createStatSpreadsheet(
            final Config config,
            final Collection<View> views, 
            final List<MemberBean> members) {
        Spreadsheet spreadsheet = new Spreadsheet()

        // Convert the list of members into a list of maps
        List<Map<String, String>> membersListMap = []
        members.each { MemberBean member -> membersListMap << member.toMap(config.membersCsvColumnMappings) }

        // Create the Members tab of the workbook
        spreadsheet.addContent("Members", membersListMap[0].keySet().toArray() as String[], membersListMap)

        // Create a tab per view in the workboook
        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        // Write the workbook to disk
        spreadsheet.writeToFile(config.memberStatsDirName)
    }
}
