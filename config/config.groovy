// This is an example configuration file that can be given as the first (and only) command line paramter to MemberMap

google {
   /** REQUIRED: The Google project name */
    applicationName = ""

   /** The Google project name */
   projectId = ""

   /** The Google project number */
   projectNumber = ""

   /** The public access key created for the organization required to authenticate to Google Map APIs */
   apiKey = ""

   /** The name of the generated OAuth JSON service account private key */
   // jsonKeyFileName = ""
}





/** The name of the input file containing membership information */
// membersCsvFileName = ''

/** The name of the (optional) input file containing additional "bonus" membership information */
// bonusMembersCsvFileName = ''

/** The name of the input/output cache file containing cached geodedic information
 * for the member's addresses (not full membership information) */
// geodedicCsvFileName = ''

/** The name of the input/output cache file containing cached geodedic information
 * for the member's addresses (not full membership information) */
// distanceDataCacheCsvFileName = ''

/** The name of the directory to store the spreadsheet output containing
 * full membership information (including geodedic) plus histograms of various views of the membership */
// memberStatsDirName = ''

/** A CSV separated list of Member fields/properties to cache in the geodedic address file */
// geodedicCsvHeaders = ''

/** The format of all dates (e.g. birthdays) */
// dateTimeFormat = "M/d/yyyy"

/** The name of a central location we want to find the distance to for each member */
// centralPointName    = "Greenville Oaks Church of Christ"

/** The address of a central location we want to find the distance to for each member */
// centralPointAddress = "703 South Greenville Avenue, Allen, TX 75002"

/** The shortest commute will be found for each member to another member in each of these CSV roles. */
// public String memberRoleCommute = "Small Group Leader"


membersCsvColumnMappings {
    // directoryName = "Directory Name"
    // lastName = "Last Name"
    // firstName = "Preferred Name"

    // address = "Address"
    // city = "City"
    // zip = "Zip Code"
    // fullAddress = "Full Address"
    // formattedAddress = "Formatted Address"
    // numInHousehold = "Number in Household"

    // birthday = "Birth Date"
    // age = "Age"
    // grade = "School Grade"

    // role = "Role"

    // latitude = "Latitude"
    // longitude = "Longitude"

    // commuteDistance2CentralPointInMeters = "Commute Distance In Meters"
    // commuteDistance2CentralPointHumanReadable = "Commute Distance"

    // commuteTime2CentralPointInSeconds = "Commute Time In Seconds"
    // commuteTime2CentralPointHumanReadable = "Commute Time"

    // primaryKey = "Unique Id"
}
