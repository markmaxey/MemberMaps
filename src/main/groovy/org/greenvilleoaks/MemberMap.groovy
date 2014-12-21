package org.greenvilleoaks

import org.greenvilleoaks.view.*

/**
 * This is the "main" class containing all the control logic.
 */
class MemberMap {
    public static final Config config = new Config()

    /**
     * The high level workflow logic
     * @param argv
     */
    public static void main(final String[] argv) {
        List<Member> members = new Members(config).createMembers()

        List<View> views = createViews(members)

        createStatSpreadsheet(views, members)
    }




    /**
     * @param members
     * @return various perspectives of the create
     */
    private static List<View> createViews(final List<Member> members) {
        List<View> views = []

        views << new CityView(config.propertyNames.city, members)
        views << new ZipView(config.propertyNames.zip, members)
        views << new NumInHouseholdView(config.propertyNames.numInHousehold, members)
        views << new AgeView(config.propertyNames.age, members)
        views << new GradeView(config.propertyNames.grade, members)
        views << new DistanceView("Commute Distance in Miles", members)
        views << new DurationView("Commute Time in Minutes", members)

        return views
    }


    /**
     * Dump all the perspectives of the create to an Excel workbook
     * @param views
     */
    private static void createStatSpreadsheet(List<View> views, final List<Member> members) {
        List<Map<String, String>> membersListMap = []
        members.each { Member member -> membersListMap << member.toMap(config.propertyNames) }

        Spreadsheet spreadsheet = new Spreadsheet()

        spreadsheet.addContent("Members", config.propertyNames.values().toArray() as String[], membersListMap)

        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        spreadsheet.writeToFile(config.memberStatsDirName)
    }
}
