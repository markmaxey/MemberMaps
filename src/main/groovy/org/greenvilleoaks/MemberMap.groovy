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
        List<Member> members      = loadMembers()
        List<Member> geodedicInfo = loadGeodedicInfo()

        geocodeMembers(members, geodedicInfo)

        storeGeodedicInfo(geodedicInfo)

        List<View> views = createViews(members)

        List<Map<String, String>> membersListMap = []
        members.each { Member member -> membersListMap << member.toMap(config.propertyNames) }

        createStatSpreadsheet(views, membersListMap)
    }


    private static List<Member> loadMembers() {
        List<Member> members = []

        new Csv(config.membersCsvFileName).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter)
        }

        computeNumInHousehold(members)

        return members
    }



    public static void computeNumInHousehold(List<Member> members) {
        Map<String, Integer> fullAddress2NumInHousehold = [:]
        members.each { Member member ->
            Integer numInHousehold = fullAddress2NumInHousehold.get(member.fullAddress)
            if (numInHousehold == null) {
                numInHousehold = new Integer(1)
                fullAddress2NumInHousehold.put(member.fullAddress, numInHousehold)
            }
            else {
                fullAddress2NumInHousehold.put(member.fullAddress, new Integer(numInHousehold + 1))
            }
        }

        members.each { Member member ->
            member.numInHousehold = fullAddress2NumInHousehold.get(member.fullAddress)
        }
    }


    private static List<Member> loadGeodedicInfo() {
        List<Member> members = []

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).load().each {
            members << new Member(it, config.propertyNames, config.dateFormatter)
        }

        return members
    }


    private static void storeGeodedicInfo(List<Member> geodedicInfo) {
        List<Map<String, Object>> geodedicListOfMaps = []
        geodedicInfo.each { geodedicListOfMaps << it.toMap(config.propertyNames)}

        new Csv(config.geodedicCsvFileName, config.geodedicCsvHeaders).store(geodedicListOfMaps)
    }



    /**
     * @return The create with geodedic information
     */
    private static void geocodeMembers(final List<Member> members, final List<Member> geodedicInfo) {
        config.context.apiKey = config.apiKey

        Geodedic geocode = new Geodedic(centralAddress: config.centralAddress, context: config.context, geodedicMembers: geodedicInfo)

        geocode.create(members)
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
        views << new DistanceView("Commute Distance in Miles", members)
        views << new DurationView("Commute Time in Minutes", members)

        return views
    }


    /**
     * Dump all the perspectives of the create to an Excel workbook
     * @param views
     */
    private static void createStatSpreadsheet(List<View> views, List<Map<String, String>> memberListMap) {
        Spreadsheet spreadsheet = new Spreadsheet()

        spreadsheet.addContent("Members", config.propertyNames.values().toArray() as String[], memberListMap)

        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        spreadsheet.writeToFile(config.memberStatsDirName)
    }
}
