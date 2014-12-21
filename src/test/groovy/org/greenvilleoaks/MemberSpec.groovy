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

                "Commute Distance In Meters": "99",
                "Commute Distance": "99 miles",

                "Commute Time In Seconds": "99",
                "Commute Time": "99 minutes"
        ]

        Member member = new Member(memberMap, config.propertyNames, config.dateFormatter)

        expect:
        member.address == memberMap."Address"
        member.age == Period.between(LocalDate.of(1969, 5, 30), LocalDate.now()).years
        member.birthday == LocalDate.of(1969, 5, 30)
        member.city == memberMap."City"
        member.commuteDistance2CentralPointHumanReadable == memberMap."Commute Distance"
        member.commuteDistance2CentralPointInMeters == Long.valueOf(memberMap."Commute Distance In Meters")
        member.commuteTime2CentralPointHumanReadable == memberMap."Commute Time"
        member.commuteTime2CentralPointInSeconds == Long.valueOf(memberMap."Commute Time In Seconds")
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
        map."Commute Distance" == memberMap."Commute Distance"
        map."Commute Distance In Meters" == memberMap."Commute Distance In Meters"
        map."Commute Time" == memberMap."Commute Time"
        map."Commute Time In Seconds" == memberMap."Commute Time In Seconds"
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
        member.commuteDistance2CentralPointHumanReadable == null
        member.commuteDistance2CentralPointInMeters == null
        member.commuteTime2CentralPointInSeconds == null
        member.commuteTime2CentralPointHumanReadable == null
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
        map."Commute Distance" == null
        map."Commute Distance In Meters" == null
        map."Commute Time" == null
        map."Commute Time In Seconds" == null
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


    def "Full Address"(String address, String city, String zip, String fullAddress) {
        expect:
        Member member = new Member([
                "Address" : address,
                "City"    : city,
                "Zip Code": zip
        ], config.propertyNames, config.dateFormatter)

        member.fullAddress == fullAddress


        where:
        address | city | zip  | fullAddress
        "a"     | "b"  | "9"  | "a, b 9"
        null    | "b"  | "9"  | "b 9"
        "a"     | null | "9"  | "a 9"
        "a"     | "b"  | null | "a, b"
        null    | null | "9"  | "9"
        null    | "b"  | null | "b"
        "a"     | null | null | "a"
        null    | null | null | null
    }
}