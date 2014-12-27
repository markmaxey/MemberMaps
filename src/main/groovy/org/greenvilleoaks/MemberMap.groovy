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
    public static final Config config = new Config()

    /**
     * The high level workflow logic
     * @param argv
     */
    public static void main(final String[] argv) {
        log.info("Generating a members map and spreadsheet ...")
        log.info(config.toString())

        List<MemberBean> members = new Members(config).createMembers()

        Map<String, View> views = createViews(members)

        createStatSpreadsheet(views.values(), members)

        new Workflow(
                members,
                views,
                new NetHttpTransport(),
                new GsonFactory(),
                config.csvColumnMappings,
                config.google.applicationName,
                new File(config.google.jsonKeyFileName)).run(config.google.projectId)
    }




    /**
     * @param members
     * @return various perspectives of the create
     */
    private static Map<String, View> createViews(final List<MemberBean> members) {
        Map<String, View> views = [:]

        views.put(config.csvColumnMappings.city,           new CityView(config.csvColumnMappings.city, members))
        views.put(config.csvColumnMappings.zip,            new ZipView(config.csvColumnMappings.zip, members))
        views.put(config.csvColumnMappings.numInHousehold, new NumInHouseholdView(config.csvColumnMappings.numInHousehold, members))
        views.put(config.csvColumnMappings.age,            new AgeView(config.csvColumnMappings.age, members))
        views.put(config.csvColumnMappings.grade,          new GradeView(config.csvColumnMappings.grade, members))
        views.put(config.csvColumnMappings.role,           new RoleView(config.csvColumnMappings.role, members))
        views.put("Commute Distance in Miles",         new DistanceView("Commute Distance in Miles", members))
        views.put("Commute Time in Minutes",           new DurationView("Commute Time in Minutes", members))

        return views
    }


    /**
     * Dump all the perspectives of the create to an Excel workbook
     * @param views
     */
    private static void createStatSpreadsheet(Collection<View> views, final List<MemberBean> members) {
        List<Map<String, String>> membersListMap = []
        members.each { MemberBean member -> membersListMap << member.toMap(config.csvColumnMappings) }

        Spreadsheet spreadsheet = new Spreadsheet()

        spreadsheet.addContent("Members", config.csvColumnMappings.values().toArray() as String[], membersListMap)

        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        spreadsheet.writeToFile(config.memberStatsDirName)
    }
}
