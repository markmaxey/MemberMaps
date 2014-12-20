package org.greenvilleoaks

import com.google.maps.GeoApiContext
import org.greenvilleoaks.view.*

import java.time.format.DateTimeFormatter

/**
 * This is the "main" class containing all the control logic.
 */
class MemberMap {
    private static final GeoApiContext context = new GeoApiContext()

    private static final String apiKey = "AIzaSyBqAYLqYrV9ArcEsU3MNi3ffHbf-BQ3F1s"
    private static final String membersCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Members.csv"
    private static final String geodedicCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Geodedic.csv"
    private static final String memberStatsDirName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\MemberStats"
    private static final Map<String, String> propertyNames = [
            "fullName": "Directory Name",
            "lastName": "Last Name",
            "firstName": "Preferred Name",

            "address": "Address",
            "city": "City",
            "zip": "Zip Code",
            "fullAddress": "Formatted Address",
            "formattedAddress": "Formatted Address",
            "numInHousehold": "Number in Household",

            "birthday": "Birth Date",
            "age": "Age",
            "grade": "School Grade",

            "latitude": "Latitude",
            "longitude": "Longitude",

            "distanceInMeters": "Distance In Meters",
            "distanceHumanReadable": "Distance",

            "durationInSeconds": "Duration In Seconds",
            "durationHumanReadable": "Duration"
    ] as TreeMap<String, String>

    private static final String[] geodedicCsvHeaders = [
            propertyNames.address,  propertyNames.city, propertyNames.zip,
            propertyNames.latitude, propertyNames.longitude,
            propertyNames.distanceInMeters, propertyNames.distanceHumanReadable,
            propertyNames.durationInSeconds, propertyNames.durationHumanReadable,
            propertyNames.formattedAddress
    ] as String[]


    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

    private static final String centralAddress = "703 South Greenville Avenue, Allen, TX 75002"


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
        members.each { Member member -> membersListMap << member.toMap(propertyNames) }

        createStatSpreadsheet(views, membersListMap)
    }


    private static List<Member> loadMembers() {
        List<Member> members = []

        new Csv(membersCsvFileName).load().each { members << new Member(it, propertyNames, dateFormatter) }

        return members
    }


    private static List<Member> loadGeodedicInfo() {
        List<Member> members = []

        new Csv(geodedicCsvFileName, geodedicCsvHeaders).load().each { members << new Member(it, propertyNames, dateFormatter) }

        return members
    }


    private static void storeGeodedicInfo(List<Member> geodedicInfo) {
        List<Map<String, Object>> geodedicListOfMaps = []
        geodedicInfo.each { geodedicListOfMaps << it.toMap(propertyNames)}

        new Csv(geodedicCsvFileName, geodedicCsvHeaders).store(geodedicListOfMaps)
    }



    /**
     * @return The create with geodedic information
     */
    private static void geocodeMembers(final List<Member> members, final List<Member> geodedicInfo) {
        context.apiKey = apiKey

        Geodedic geocode = new Geodedic(centralAddress: centralAddress, context: context, geodedicMembers: geodedicInfo)

        geocode.create(members)
    }


    /**
     * @param members
     * @return various perspectives of the create
     */
    private static List<View> createViews(final List<Member> members) {
        List<View> views = []

        views << new CityView(propertyNames.city, members)
        views << new ZipView(propertyNames.zip, members)
        views << new NumInHouseholdView(propertyNames.numInHousehold, members)
        views << new AgeView(propertyNames.age, members)
        views << new DistanceView(propertyNames.distanceInMeters, members)
        views << new DurationView(propertyNames.durationInSeconds, members)

        return views
    }


    /**
     * Dump all the perspectives of the create to an Excel workbook
     * @param views
     */
    private static void createStatSpreadsheet(List<View> views, List<Map<String, String>> memberListMap) {
        Spreadsheet spreadsheet = new Spreadsheet()

        spreadsheet.addContent("Members", propertyNames.values().toArray() as String[], memberListMap)

        views.each { View view ->
            spreadsheet.addContent(view.name, view.headers, view.createStats())
        }

        spreadsheet.writeToFile(memberStatsDirName)
    }
}
