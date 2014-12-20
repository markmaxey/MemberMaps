package org.greenvilleoaks

import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.Period

class MemberSpec extends Specification {
    @Shared Config config = new Config()

    def "Construct a member given all header/column/key names - even those that are computed"() {
        setup:
        Map<String, String> memberMap = [
                "Directory Name": "Maxey, Mark",
                "Last Name": "Maxey",
                "Preferred Name": "Mark",

                "Address": "132 Collin Ct",
                "City": "Murphy",
                "Zip Code": "75094",
                "Full Address": "COMPUTED",
                "Formatted Address": "132 Collin Ct, Murphy, TX 75094, USA",
                "Number in Household": "99",

                "Birth Date": "5/30/1969",
                "Age": "COMPUTED",
                "School Grade": "Graduated",

                "Latitude": "1.234",
                "Longitude": "5.678",

                "Distance In Meters": "99",
                "Distance": "99 miles",

                "Duration In Seconds": "99",
                "Duration": "99 minutes"
        ]

        Member member = new Member(memberMap, config.propertyNames, config.dateFormatter)

        expect:
        member.address == memberMap."Address"
        member.age == Period.between(LocalDate.of(1969, 5, 30), LocalDate.now()).years
        member.birthday == LocalDate.of(1969, 5, 30)
        member.city == memberMap."City"
        member.distanceHumanReadable == memberMap."Distance"
        member.distanceInMeters == Long.valueOf(memberMap."Distance In Meters")
        member.durationHumanReadable == memberMap."Duration"
        member.durationInSeconds == Long.valueOf(memberMap."Duration In Seconds")
        member.firstName == memberMap."Preferred Name"
        member.formattedAddress == memberMap."Formatted Address"
        member.fullAddress == "132 Collin Ct, Murphy 75094"
        member.fullName == memberMap."Directory Name"
        member.grade == memberMap."School Grade"
        member.lastName == memberMap."Last Name"
        member.latitude ==  Double.valueOf(memberMap."Latitude")
        member.longitude == Double.valueOf(memberMap."Longitude")
        member.numInHousehold == Integer.valueOf(memberMap."Number in Household")
        member.zip == Integer.valueOf(memberMap."Zip Code")

        Map<String, String> map = member.toMap(config.propertyNames)
        map."Address" == memberMap."Address"
        map."Age" == Integer.toString(Period.between(LocalDate.of(1969, 5, 30), LocalDate.now()).years)
        map."Birth Date" == memberMap."Birth Date"
        map."City" == memberMap."City"
        map."Distance" == memberMap."Distance"
        map."Distance In Meters" == memberMap."Distance In Meters"
        map."Duration" == memberMap."Duration"
        map."Duration In Seconds" == memberMap."Duration In Seconds"
        map."Preferred Name" == memberMap."Preferred Name"
        map."Formatted Address" == memberMap."Formatted Address"
        map."Full Address" == "132 Collin Ct, Murphy 75094"
        map."Directory Name" == memberMap."Directory Name"
        map."School Grade" == memberMap."School Grade"
        map."Last Name" == memberMap."Last Name"
        map."Latitude" == memberMap."Latitude"
        map."Longitude" == memberMap."Longitude"
        map."Number in Household" == memberMap."Number in Household"
        map."Zip Code" == memberMap."Zip Code"
    }


    def "Empty Map"() {
        setup:
        Map<String, String> memberMap = [:]
        Member member = new Member(memberMap, config.propertyNames, config.dateFormatter)

        expect:
        member.address == null
        member.age == null
        member.birthday == null
        member.city == null
        member.distanceHumanReadable == null
        member.distanceInMeters == null
        member.durationHumanReadable == null
        member.durationInSeconds == null
        member.firstName == null
        member.formattedAddress == null
        member.fullName == null
        member.grade == null
        member.lastName == null
        member.latitude == null
        member.longitude == null
        member.numInHousehold == null
        member.zip == null

        Map<String, String> map = member.toMap(config.propertyNames)
        map."Address" == null
        map."Age" == null
        map."Birth Date" == null
        map."City" == null
        map."Distance" == null
        map."Distance In Meters" == null
        map."Duration" == null
        map."Duration In Seconds" == null
        map."Preferred Name" == null
        map."Formatted Address" == null
        map."Directory Name" == null
        map."School Grade" == null
        map."Last Name" == null
        map."Latitude" == null
        map."Longitude" == null
        map."Number in Household" == null
        map."Zip Code" == null
    }
}
