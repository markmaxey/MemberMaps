package org.greenvilleoaks.config

import groovy.transform.ToString

import java.time.format.DateTimeFormatter

@ToString(includeNames = true, includeFields = true)
class Config {
    public Google google = new Google()
    public CsvColumnMappings csvColumnMappings = new CsvColumnMappings()
    
    /** The name of the input file containing membership information */
    public final String membersCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Members.csv"

    /** The name of the (optional) input file containing additional "bonus" membership information */
    public final String bonusMembersCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\BonusMembers.csv"

    /** The name of the input/output cache file containing cached geodedic information
     * for the member's addresses (not full membership information) */
    public final String geodedicCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Geodedic.csv"

    /** The name of the input/output cache file containing cached geodedic information
     * for the member's addresses (not full membership information) */
    public final String distanceDataCacheCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\DistanceDataCache.csv"

    /** The name of the directory to store the spreadsheet output containing
     * full membership information (including geodedic) plus histograms of various views of the membership */
    public final String memberStatsDirName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\MemberStats"

    /** A map of the Member class field/property names to the name of the columns in the input/output files */
    /*
    public Map<String, String> csvColumnMappings = [
            "fullName": "Directory Name",
            "lastName": "Last Name",
            "firstName": "Preferred Name",

            "address": "Address",
            "city": "City",
            "zip": "Zip Code",
            "fullAddress": "Full Address",
            "formattedAddress": "Formatted Address",
            "numInHousehold": "Number in Household",

            "birthday": "Birth Date",
            "age": "Age",
            "grade": "School Grade",

            "role": "Role",

            "latitude": "Latitude",
            "longitude": "Longitude",

            "commuteDistance2CentralPointInMeters": "Commute Distance In Meters",
            "commuteDistance2CentralPointHumanReadable": "Commute Distance",

            "commuteTime2CentralPointInSeconds": "Commute Time In Seconds",
            "commuteTime2CentralPointHumanReadable": "Commute Time",
            
            "primaryKey": "Unique Id"
    ] as LinkedHashMap<String, String>
    */

    /** The name of the subset of Member fields/properties to cache in the geodedic address file */
    public List<String> geodedicCsvHeaders = [
            csvColumnMappings.address,  csvColumnMappings.city, csvColumnMappings.zip,
            csvColumnMappings.latitude, csvColumnMappings.longitude,
            csvColumnMappings.commuteDistance2CentralPointInMeters,
            csvColumnMappings.commuteDistance2CentralPointHumanReadable,
            csvColumnMappings.commuteTime2CentralPointInSeconds,
            csvColumnMappings.commuteTime2CentralPointHumanReadable,
            csvColumnMappings.formattedAddress
    ]


    /** The format of all dates (e.g. birthdays) */
    public final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

    /** The name of a central location we want to find the distance to for each member */
    public final String centralPointName    = "Greenville Oaks Church of Christ"

    /** The address of a central location we want to find the distance to for each member */
    public final String centralPointAddress = "703 South Greenville Avenue, Allen, TX 75002"

    /** The shortest commute will be found for each member to another member in each of these roles. */
    public final List<String> memberRoleCommute = ["Small Group Leader"]


    public Config() {
        memberRoleCommute.each { String role ->
            csvColumnMappings.metaClass.("Minimum Commute Distance In Meters to " + role) = "Minimum Commute Distance In Meters to " + role
            csvColumnMappings.metaClass.("Minimum Commute Distance to " + role) =           "Minimum Commute Distance to " + role
            csvColumnMappings.metaClass.("Minimum Commute Time In Seconds to " + role) =    "Minimum Commute Time In Seconds to " + role
            csvColumnMappings.metaClass.("Minimum Commute Time to " + role) =               "Minimum Commute Time to " + role
            csvColumnMappings.metaClass.("Minimum Commute to " + role) =                    "Minimum Commute to " + role

            geodedicCsvHeaders.add("Minimum Commute Distance In Meters to " + role)
            geodedicCsvHeaders.add("Minimum Commute Distance to " + role)
            geodedicCsvHeaders.add("Minimum Commute Time In Seconds to " + role)
            geodedicCsvHeaders.add("Minimum Commute Time to " + role)
            geodedicCsvHeaders.add("Minimum Commute to " + role)
        }
    }
}
