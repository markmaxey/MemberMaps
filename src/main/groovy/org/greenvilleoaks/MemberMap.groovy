package org.greenvilleoaks

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import groovy.util.logging.Log4j
import org.greenvilleoaks.beans.MemberBean
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
                config.propertyNames,
                config.applicationName,
                new File(config.jsonKeyFileName)).run(config.projectId)
    }




    /**
     * @param members
     * @return various perspectives of the create
     */
    private static Map<String, View> createViews(final List<MemberBean> members) {
        Map<String, View> views = [:]

        views.put(config.propertyNames.city,           new CityView(config.propertyNames.city, members))
        views.put(config.propertyNames.zip,            new ZipView(config.propertyNames.zip, members))
        views.put(config.propertyNames.numInHousehold, new NumInHouseholdView(config.propertyNames.numInHousehold, members))
        views.put(config.propertyNames.age,            new AgeView(config.propertyNames.age, members))
        views.put(config.propertyNames.grade,          new GradeView(config.propertyNames.grade, members))
        views.put(config.propertyNames.role,           new RoleView(config.propertyNames.role, members))
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
        members.each { MemberBean member -> membersListMap << member.toMap(config.propertyNames) }

        Spreadsheet spreadsheet = new Spreadsheet()

        spreadsheet.addContent("Members", config.propertyNames.values().toArray() as String[], membersListMap)

        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        spreadsheet.writeToFile(config.memberStatsDirName)
    }
}
