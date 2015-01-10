package org.greenvilleoaks.config

import org.greenvilleoaks.beans.MemberBean
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
//mapsEngineProjectId = 'abc'
memberRoleCommute = '0,1,2'
geodedicCsvHeaders = '0,1'
google {
   // apisProjectNumber = "123"
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
        
        config.google.mapsEngineProjectId     == 'greenvilleoaks'
        config.google.apisProjectNumber == "297047284747"
    }
    
    
    def "Member columns are in sync between cached and full member data (defaults)"() {
        setup:
        Config config = new Config().init()
        Set<String> memberKeys =
                new MemberBean([:], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList).
                        toMap(config.membersCsvColumnMappings).
                        keySet()

        expect:
        config.geodedicCsvHeaderList.each { assert memberKeys.contains(it) }
    }




    def "Member columns are in sync between cached and full member data (with overrides from config file)"() {
        setup:
        ConfigSlurper cs = new ConfigSlurper()
        Config config = cs.parse("""
membersCsvColumnMappings {
   firstName = 'Common Name'
   age = 'Edad'
   // commuteDistance2CentralPointHumanReadable = 'THIS CANNOT BE OVERRIDDEN'
   // commuteDistance2CentralPointInMeters      = 'THIS CANNOT BE OVERRIDDEN'
   // commuteTime2CentralPointInSeconds         = 'THIS CANNOT BE OVERRIDDEN'
   // commuteTime2CentralPointHumanReadable     = 'THIS CANNOT BE OVERRIDDEN'
}
""")
        config.init()
        Set<String> memberKeys =
                new MemberBean([:], config.membersCsvColumnMappings, config.dateFormatter, config.memberRoleCommuteList).
                        toMap(config.membersCsvColumnMappings).
                        keySet()

        expect:
        config.geodedicCsvHeaderList.each { assert memberKeys.contains(it) }
    }
}