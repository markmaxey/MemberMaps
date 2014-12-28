package org.greenvilleoaks.config

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class CsvColumnMappings {
    String fullName = "Directory Name"
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

    String commuteDistance2CentralPointInMeters = "Commute Distance In Meters"
    String commuteDistance2CentralPointHumanReadable = "Commute Distance"

    String commuteTime2CentralPointInSeconds = "Commute Time In Seconds"
    String commuteTime2CentralPointHumanReadable = "Commute Time"

    String primaryKey = "Unique Id"
}
