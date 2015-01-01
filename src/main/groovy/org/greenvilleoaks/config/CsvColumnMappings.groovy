package org.greenvilleoaks.config

import groovy.transform.ToString

/** A map of the Member class field/property names to the name of the columns in the input/output files */
@ToString(includeNames = true, includeFields = true)
class CsvColumnMappings {
    String directoryName = "Directory Name"
    String lastName = "Last Name"
    String firstName = "Preferred Name"

    String address = "Address"
    String city = "City"
    String zip = "Zip Code"
    String fullAddress = "Full Address"
    String formattedAddress = "Formatted Address"
    String numInHousehold = "Number in Household"

    String birthday = "Birth Date"
    String age = "Age"
    String grade = "School Grade"

    String role = "Role"

    String latitude = "Latitude"
    String longitude = "Longitude"

    // TODO: These four properties cannot be overridden by a user config file because they are enhanced below in init.
    String commuteDistance2CentralPointInMeters = "Commute Distance In Meters"
    String commuteDistance2CentralPointHumanReadable = "Commute Distance"

    String commuteTime2CentralPointInSeconds = "Commute Time In Seconds"
    String commuteTime2CentralPointHumanReadable = "Commute Time"

    String primaryKey = "Unique Id"
    
    private boolean inited = false
    
    protected CsvColumnMappings init(final String centralPointName) {
        if (!inited) {
            inited = true

            commuteDistance2CentralPointHumanReadable += " to " + centralPointName
            commuteDistance2CentralPointInMeters      += " to " + centralPointName
            commuteTime2CentralPointHumanReadable     += " to " + centralPointName
            commuteTime2CentralPointInSeconds         += " to " + centralPointName
        }

        return this
    }
}
