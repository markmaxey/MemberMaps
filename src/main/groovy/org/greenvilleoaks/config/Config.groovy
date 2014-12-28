package org.greenvilleoaks.config

import groovy.transform.ToString

import java.time.format.DateTimeFormatter

@ToString(includeNames = true, includeFields = true)
class Config {
    public Google google = new Google()
    public CsvColumnMappings membersCsvColumnMappings = new CsvColumnMappings()
    
    /** The name of the input file containing membership information */
    public String membersCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Members.csv"

    /** The name of the (optional) input file containing additional "bonus" membership information */
    public String bonusMembersCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\BonusMembers.csv"

    /** The name of the input/output cache file containing cached geodedic information
     * for the member's addresses (not full membership information) */
    public String geodedicCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Geodedic.csv"

    /** The name of the input/output cache file containing cached geodedic information
     * for the member's addresses (not full membership information) */
    public String distanceDataCacheCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\DistanceDataCache.csv"

    /** The name of the directory to store the spreadsheet output containing
     * full membership information (including geodedic) plus histograms of various views of the membership */
    public String memberStatsDirName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\MemberStats"

    /** The name of the subset of Member fields/properties to cache in the geodedic address file */
    public List<String> geodedicCsvHeaderList = []

    /** A CSV separated list of Member fields/properties to cache in the geodedic address file */
    public String geodedicCsvHeaders = membersCsvColumnMappings.address + "," +
            membersCsvColumnMappings.city + "," +
            membersCsvColumnMappings.zip + "," +
            membersCsvColumnMappings.latitude + "," +
            membersCsvColumnMappings.longitude + "," +
            membersCsvColumnMappings.commuteDistance2CentralPointInMeters + "," +
            membersCsvColumnMappings.commuteDistance2CentralPointHumanReadable + "," +
            membersCsvColumnMappings.commuteTime2CentralPointInSeconds + "," +
            membersCsvColumnMappings.commuteTime2CentralPointHumanReadable + "," +
            membersCsvColumnMappings.formattedAddress


    /** The format of all dates (e.g. birthdays) */
    public DateTimeFormatter dateFormatter
    
    public String dateTimeFormat = "M/d/yyyy"

    /** The name of a central location we want to find the distance to for each member */
    public String centralPointName    = "Greenville Oaks Church of Christ"

    /** The address of a central location we want to find the distance to for each member */
    public String centralPointAddress = "703 South Greenville Avenue, Allen, TX 75002"

    /** The shortest commute will be found for each member to another member in each of these roles. */
    public List<String> memberRoleCommuteList = []

    /** The shortest commute will be found for each member to another member in each of these CSV roles. */
    public String memberRoleCommute = "Small Group Leader"


    public Config init() {
        google.init()
        
        dateFormatter = DateTimeFormatter.ofPattern(dateTimeFormat)
        
        geodedicCsvHeaderList = geodedicCsvHeaders.split(",")

        memberRoleCommuteList = memberRoleCommute.split(",")
        
        memberRoleCommuteList.each { String role ->
            membersCsvColumnMappings.metaClass.("Minimum Commute Distance In Meters to " + role) = "Minimum Commute Distance In Meters to " + role
            membersCsvColumnMappings.metaClass.("Minimum Commute Distance to " + role) =           "Minimum Commute Distance to " + role
            membersCsvColumnMappings.metaClass.("Minimum Commute Time In Seconds to " + role) =    "Minimum Commute Time In Seconds to " + role
            membersCsvColumnMappings.metaClass.("Minimum Commute Time to " + role) =               "Minimum Commute Time to " + role
            membersCsvColumnMappings.metaClass.("Minimum Commute to " + role) =                    "Minimum Commute to " + role

            geodedicCsvHeaderList.add("Minimum Commute Distance In Meters to " + role)
            geodedicCsvHeaderList.add("Minimum Commute Distance to " + role)
            geodedicCsvHeaderList.add("Minimum Commute Time In Seconds to " + role)
            geodedicCsvHeaderList.add("Minimum Commute Time to " + role)
            geodedicCsvHeaderList.add("Minimum Commute to " + role)
        }

        return this
    }
}
