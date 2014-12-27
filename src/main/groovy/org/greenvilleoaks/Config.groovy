package org.greenvilleoaks

import com.google.maps.GeoApiContext
import groovy.transform.ToString

import java.time.format.DateTimeFormatter

@ToString(includeNames = true, includeFields = true)
class Config {
    /** The Google API context used for all Google API calls */
    public final GeoApiContext context = new GeoApiContext()

    /** The Google project name */
    public final String applicationName = "Google/GreenvilleOaks-1.0"

    /** The Google project name */
    public final String projectId = "greenvilleoaks"

    /** The Google project number */
    public final String projectNumber = "297047284747"

    /** The public access key created for the organization required to authenticate to Google Map APIs */
    public final String apiKey = "AIzaSyBqAYLqYrV9ArcEsU3MNi3ffHbf-BQ3F1s"

    /** The name of the generated OAuth JSON service account private key */
    public final String jsonKeyFileName = 
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Greenville Oaks-0a1fa0b78eac.json"

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
    public Map<String, String> propertyNames = [
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


    /** The name of the subset of Member fields/properties to cache in the geodedic address file */
    public List<String> geodedicCsvHeaders = [
            propertyNames.address,  propertyNames.city, propertyNames.zip,
            propertyNames.latitude, propertyNames.longitude,
            propertyNames.commuteDistance2CentralPointInMeters,
            propertyNames.commuteDistance2CentralPointHumanReadable,
            propertyNames.commuteTime2CentralPointInSeconds,
            propertyNames.commuteTime2CentralPointHumanReadable,
            propertyNames.formattedAddress
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
            propertyNames.put("Minimum Commute Distance In Meters to " + role, "Minimum Commute Distance In Meters to " + role)
            propertyNames.put("Minimum Commute Distance to " + role,           "Minimum Commute Distance to " + role)
            propertyNames.put("Minimum Commute Time In Seconds to " + role,    "Minimum Commute Time In Seconds to " + role)
            propertyNames.put("Minimum Commute Time to " + role,               "Minimum Commute Time to " + role)
            propertyNames.put("Minimum Commute to " + role,                    "Minimum Commute to " + role)

            geodedicCsvHeaders.add("Minimum Commute Distance In Meters to " + role)
            geodedicCsvHeaders.add("Minimum Commute Distance to " + role)
            geodedicCsvHeaders.add("Minimum Commute Time In Seconds to " + role)
            geodedicCsvHeaders.add("Minimum Commute Time to " + role)
            geodedicCsvHeaders.add("Minimum Commute to " + role)
        }
    }
}
