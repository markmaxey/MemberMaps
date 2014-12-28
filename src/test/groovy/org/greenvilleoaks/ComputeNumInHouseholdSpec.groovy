package org.greenvilleoaks

import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import spock.lang.Shared
import spock.lang.Specification

class ComputeNumInHouseholdSpec extends Specification {
    @Shared Config config = new Config().init()

    private List<MemberBean> memberList(final String key, final List<String> values) {
        List<MemberBean> memberList = []
        values.each { String value ->
            Map<String, String> memberMap = [:]
            memberMap.put(key, value)
            memberList << new MemberBean(memberMap, config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        }
        return memberList
    }


    def "Compute Number of People in Household"() {
        setup:
        List<MemberBean> members = memberList(config.membersCsvColumnMappings.address,
                [
                        "a", "a", "a",
                        "b", "b", "b",
                        "c", "c",
                        "d", "d",
                        "e", "e",
                        "j", "j",
                        "f", "g", "h", "i",
                        null, null, null, null, null
                ])
        new Members(config).computeNumInHousehold(members)

        expect:
        members.find { it.address == "a" }.numInHousehold == 3
        members.find { it.address == "b" }.numInHousehold == 3
        members.find { it.address == "c" }.numInHousehold == 2
        members.find { it.address == "d" }.numInHousehold == 2
        members.find { it.address == "e" }.numInHousehold == 2
        members.find { it.address == "f" }.numInHousehold == 1
        members.find { it.address == "g" }.numInHousehold == 1
        members.find { it.address == "h" }.numInHousehold == 1
        members.find { it.address == "i" }.numInHousehold == 1
        members.find { it.address == "j" }.numInHousehold == 2
    }
}
