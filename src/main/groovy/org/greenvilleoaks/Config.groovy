package org.greenvilleoaks

import com.google.maps.GeoApiContext
import groovy.transform.ToString

import java.time.format.DateTimeFormatter

@ToString(includeNames = true, includeFields = true)
class Config {
    public final GeoApiContext context = new GeoApiContext()

    public final String apiKey = "AIzaSyBqAYLqYrV9ArcEsU3MNi3ffHbf-BQ3F1s"
    public final String membersCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Members.csv"
    public final String geodedicCsvFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Geodedic.csv"
    public final String memberStatsDirName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\MemberStats"
    public final Map<String, String> propertyNames = [
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
            "commuteTime2CentralPointHumanReadable": "Commute Time"
    ] as TreeMap<String, String>

    public final String[] geodedicCsvHeaders = [
            propertyNames.address,  propertyNames.city, propertyNames.zip,
            propertyNames.latitude, propertyNames.longitude,
            propertyNames.commuteDistance2CentralPointInMeters,
            propertyNames.commuteDistance2CentralPointHumanReadable,
            propertyNames.commuteTime2CentralPointInSeconds,
            propertyNames.commuteTime2CentralPointHumanReadable,
            propertyNames.formattedAddress
    ] as String[]


    public final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

    public final String centralAddress = "703 South Greenville Avenue, Allen, TX 75002"
}
