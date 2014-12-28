package org.greenvilleoaks

import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import spock.lang.Shared
import spock.lang.Specification

class BonusMemberSpec extends Specification {
    @Shared Config config = new Config().init()
    
    /** The Class Under Test */
    @Shared Members cut = new Members(config)
    

    def "No Members"() {
        setup:
        List<MemberBean> members = []
        List<MemberBean> bonusMembers = [
                new MemberBean(["Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)
        
        expect:
        members.size() == 0
        bonusMembers.size() == 1
        bonusMembers[0].firstName == "1"
    }



    def "No Bonus Members"() {
        List<MemberBean> bonusMembers = []
        List<MemberBean> members = [
                new MemberBean(["Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].firstName == "1"
        bonusMembers.size() == 0
    }



    def "Bonus Members without PKs / no matches"() {
        List<MemberBean> members = [
                new MemberBean(["Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["School Grade": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].firstName == "1"
        bonusMembers.size() == 1
    }



    def "Bonus Members with matching PK but no other data"() {
        List<MemberBean> members = [
                new MemberBean(["Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].firstName == "1"
        bonusMembers.size() == 1
    }



    def "Bonus Members with partial PKs"() {
        List<MemberBean> members = [
                new MemberBean(["Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["Preferred Name": "1", "Role": "Small Group Leader"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].firstName == "1"
        members[0].role == "Small Group Leader"
        bonusMembers.size() == 1
    }



    def "Bonus Members with replacement values"() {
        List<MemberBean> members = [
                new MemberBean(["Preferred Name": "1", "Role": "SHOULD BE REPLACED"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["Preferred Name": "1", "Role": "Small Group Leader"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].firstName == "1"
        members[0].role == "Small Group Leader"
        bonusMembers.size() == 1
    }



    def "Bonus Members with city only"() {
        List<MemberBean> members = [
                new MemberBean(["City": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["City": "1", "Role": "Small Group Leader"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].city == "1"
        members[0].role == "Small Group Leader"
        bonusMembers.size() == 1
    }



    def "Bonus Members with null values"() {
        List<MemberBean> members = [
                new MemberBean(["Preferred Name": "1", "Role": "Small Group Leader"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["Preferred Name": "1", "Role": null], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 1
        members[0].firstName == "1"
        members[0].role == "Small Group Leader"
        bonusMembers.size() == 1
    }


    def "Bonus Members with multiple matches"() {
        List<MemberBean> members = [
                new MemberBean(["Last Name": "1", "Preferred Name": "1"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList),
                new MemberBean(["Last Name": "1", "Preferred Name": "2"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        List<MemberBean> bonusMembers = [
                new MemberBean(["Last Name": "1", "Role": "Small Group Leader"], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList)
        ] as List<MemberBean>
        cut.mergeBonusMembers(bonusMembers, members)

        expect:
        members.size() == 2
        members[0].firstName == "1"
        members[0].lastName  == "1"
        members[0].role      == "Small Group Leader"
        members[1].firstName == "2"
        members[1].lastName  == "1"
        members[1].role      == "Small Group Leader"
        bonusMembers.size()  == 1
    }
}
