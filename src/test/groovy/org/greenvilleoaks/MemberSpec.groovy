package org.greenvilleoaks

import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.Period

class MemberSpec extends Specification {
    @Shared Config config = new Config().init()

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

                "Role": "Small Group Leader",

                "Latitude": "1.234",
                "Longitude": "5.678",
        ]

        String commuteDistance2CentralPointHumanReadableKey = "Commute Distance to "  + config.centralPointName
        String commuteDistance2CentralPointInMetersKey      = "Commute Distance In Meters to " + config.centralPointName
        String commuteTime2CentralPointHumanReadableKey     = "Commute Time to " + config.centralPointName
        String commuteTime2CentralPointInSecondsKey         = "Commute Time In Seconds to " + config.centralPointName

        memberMap.put(commuteDistance2CentralPointHumanReadableKey, "99 miles")
        memberMap.put(commuteDistance2CentralPointInMetersKey,      "99")
        memberMap.put(commuteTime2CentralPointHumanReadableKey,     "99 minutes")
        memberMap.put(commuteTime2CentralPointInSecondsKey,         "99")

        MemberBean member = new MemberBean(memberMap, config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)


        expect:
        member.address == memberMap."Address"
        member.age == Period.between(LocalDate.of(1969, 5, 30), LocalDate.now()).years
        member.birthday == LocalDate.of(1969, 5, 30)
        member.city == memberMap."City"

        member.commuteDistance2CentralPointHumanReadable == memberMap.get(commuteDistance2CentralPointHumanReadableKey)
        member.commuteDistance2CentralPointInMeters      == Long.valueOf(memberMap.get(commuteDistance2CentralPointInMetersKey))
        member.commuteTime2CentralPointHumanReadable     == memberMap.get(commuteTime2CentralPointHumanReadableKey)
        member.commuteTime2CentralPointInSeconds         == Long.valueOf(memberMap.get(commuteTime2CentralPointInSecondsKey))

        member.firstName == memberMap."Preferred Name"
        member.formattedAddress == memberMap."Formatted Address"
        member.fullAddress == "132 Collin Ct, Murphy 75094"
        member.directoryName == memberMap."Directory Name"
        member.grade == memberMap."School Grade"
        member.role == memberMap."Role"
        member.lastName == memberMap."Last Name"
        member.latitude ==  Double.valueOf(memberMap."Latitude")
        member.longitude == Double.valueOf(memberMap."Longitude")
        member.zip == Integer.valueOf(memberMap."Zip Code")

        Map<String, String> map = member.toMap(config.membersCsvColumnMappings)
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
        map."Role" == memberMap."Role"
        map."Last Name" == memberMap."Last Name"
        map."Latitude" == memberMap."Latitude"
        map."Longitude" == memberMap."Longitude"
        map."Zip Code" == memberMap."Zip Code"
    }


    def "Empty Map"() {
        setup:
        Map<String, String> memberMap = [:]
        MemberBean member = new MemberBean(memberMap, config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)

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
        member.directoryName == null
        member.grade == null
        member.role == null
        member.lastName == null
        member.latitude == null
        member.longitude == null
        member.zip == null

        Map<String, String> map = member.toMap(config.membersCsvColumnMappings)
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
        map."Role" == null
        map."Last Name" == null
        map."Latitude" == null
        map."Longitude" == null
        map."Zip Code" == null
    }


    def "Full Address"(String address, String city, String zip, String fullAddress) {
        expect:
        MemberBean member = new MemberBean([
                "Address" : address,
                "City"    : city,
                "Zip Code": zip
        ], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)

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




    def "Find all members matching the bonus member"(
            String firstName, String lastName, String address, int numMatches) {
        setup:
        Members members = new Members(config)

        List<MemberBean> memberBeanList = []
        ["fn1", "fn2", "fn3"].each { String fn ->
            ["ln1", "ln2", "ln3"].each { String ln ->
                ["ad1", "ad2", "ad3"].each { String ad ->
                    memberBeanList << new MemberBean([
                            "Preferred Name" : fn,
                            "Last Name"      : ln,
                            "Address"        : ad
                    ], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
                }
            }
        }

        MemberBean bonusMember = new MemberBean([
                "Preferred Name" : firstName,
                "Last Name"      : lastName,
                "Address"        : address
        ], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)

        List<MemberBean> matchingMembers = members.findAllMembersMatchingTheBonusMember(bonusMember, memberBeanList)
        

        expect:
        matchingMembers.size() == numMatches

        where:
        firstName | lastName | address | numMatches
        "fn1"     | "ln1"    | "ad1"       | 1
        "fn1"     | "ln2"    | "ad1"       | 1
        "fn1"     | "ln1"    | "ad2"       | 1
        "fn1"     | "ln2"    | "ad2"       | 1
        "fn1"     | "ln3"    | "ad1"       | 1
        "fn1"     | "ln1"    | "ad3"       | 1
        "fn1"     | "ln3"    | "ad3"       | 1
        "fn1"     | "ln3"    | "ad2"       | 1
        "fn1"     | "ln2"    | "ad3"       | 1

        "fn2"     | "ln1"    | "ad1"       | 1
        "fn2"     | "ln2"    | "ad1"       | 1
        "fn2"     | "ln1"    | "ad2"       | 1
        "fn2"     | "ln2"    | "ad2"       | 1
        "fn2"     | "ln3"    | "ad1"       | 1
        "fn2"     | "ln1"    | "ad3"       | 1
        "fn2"     | "ln3"    | "ad3"       | 1
        "fn2"     | "ln3"    | "ad2"       | 1
        "fn2"     | "ln2"    | "ad3"       | 1

        "fn3"     | "ln1"    | "ad1"       | 1
        "fn3"     | "ln2"    | "ad1"       | 1
        "fn3"     | "ln1"    | "ad2"       | 1
        "fn3"     | "ln2"    | "ad2"       | 1
        "fn3"     | "ln3"    | "ad1"       | 1
        "fn3"     | "ln1"    | "ad3"       | 1
        "fn3"     | "ln3"    | "ad3"       | 1
        "fn3"     | "ln3"    | "ad2"       | 1
        "fn3"     | "ln2"    | "ad3"       | 1

        null      | "ln1"    | "ad1"       | 3
        null      | "ln2"    | "ad1"       | 3
        null      | "ln1"    | "ad2"       | 3
        null      | "ln2"    | "ad2"       | 3
        null      | "ln3"    | "ad1"       | 3
        null      | "ln1"    | "ad3"       | 3
        null      | "ln3"    | "ad3"       | 3
        null      | "ln3"    | "ad2"       | 3
        null      | "ln2"    | "ad3"       | 3

        "fn1"     | null     | "ad1"       | 3
        "fn1"     | null     | "ad2"       | 3
        "fn1"     | null     | "ad3"       | 3
        "fn2"     | null     | "ad1"       | 3
        "fn2"     | null     | "ad2"       | 3
        "fn2"     | null     | "ad3"       | 3
        "fn3"     | null     | "ad1"       | 3
        "fn3"     | null     | "ad2"       | 3
        "fn3"     | null     | "ad3"       | 3

        "fn1"     | null     | "ad1"       | 3
        "fn1"     | null     | "ad2"       | 3
        "fn1"     | null     | "ad3"       | 3
        "fn2"     | null     | "ad1"       | 3
        "fn2"     | null     | "ad2"       | 3
        "fn2"     | null     | "ad3"       | 3
        "fn3"     | null     | "ad1"       | 3
        "fn3"     | null     | "ad2"       | 3
        "fn3"     | null     | "ad3"       | 3

        "fn1"     | "ln1"    | null        | 3
        "fn1"     | "ln2"    | null        | 3
        "fn1"     | "ln3"    | null        | 3
        "fn2"     | "ln1"    | null        | 3
        "fn2"     | "ln2"    | null        | 3
        "fn2"     | "ln3"    | null        | 3
        "fn3"     | "ln1"    | null        | 3
        "fn3"     | "ln2"    | null        | 3
        "fn3"     | "ln3"    | null        | 3

        null      | null     | "ad1"       | 9
        null      | null     | "ad2"       | 9
        null      | null     | "ad3"       | 9
        null      | "ln1"    | null        | 9
        null      | "ln2"    | null        | 9
        null      | "ln3"    | null        | 9
        "fn1"     | null     | null        | 9
        "fn2"     | null     | null        | 9
        "fn3"     | null     | null        | 9
        null      | null     | null        | 27
    }
}