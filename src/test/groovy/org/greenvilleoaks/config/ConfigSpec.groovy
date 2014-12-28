package org.greenvilleoaks.config

import spock.lang.Specification

class ConfigSpec extends Specification {
    def "Defaults"() {
        setup:
        Config config = new Config().init()
        
        expect:
        config.google.context.apiKey == config.google.apiKey
        config.dateFormatter.parse("05/30/1969")
        config.geodedicCsvHeaderList.size() == 15
        config.memberRoleCommuteList[0] == "Small Group Leader"
        config.membersCsvColumnMappings.firstName == "Preferred Name"
    }
    

    def "Overrides"() {
        setup:
        ConfigSlurper cs = new ConfigSlurper()
        Config config = cs.parse("""
dateTimeFormat = 'yyyy/d/M'
google.apiKey = 123
membersCsvColumnMappings {
   firstName = 'First Name'
   age = 'Edad'
}
//projectId = 'abc'
memberRoleCommute = '0,1,2'
geodedicCsvHeaders = '0,1'
google {
   // projectNumber = "123"
}
""")
        config.init()

        expect:
        config.google.context.apiKey == "123"
        config.dateFormatter.parse("1969/30/05")
        config.membersCsvColumnMappings.firstName == "First Name"
        config.membersCsvColumnMappings.age == "Edad"

        config.geodedicCsvHeaderList.size() == 17
        config.geodedicCsvHeaderList[0] == "0"
        config.geodedicCsvHeaderList[1] == "1"

        config.memberRoleCommuteList.size() == 3
        config.memberRoleCommuteList[0] == "0"
        config.memberRoleCommuteList[1] == "1"
        config.memberRoleCommuteList[2] == "2"
        
        config.google.projectId     == 'greenvilleoaks'
        config.google.projectNumber == "297047284747"
    }
}