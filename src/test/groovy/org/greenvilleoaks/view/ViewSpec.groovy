package org.greenvilleoaks.view

import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.Config
import org.greenvilleoaks.view.AgeView
import org.greenvilleoaks.view.CityView
import org.greenvilleoaks.view.DistanceView
import org.greenvilleoaks.view.DurationView
import org.greenvilleoaks.view.GradeView
import org.greenvilleoaks.view.NumInHouseholdView
import org.greenvilleoaks.view.RoleView
import org.greenvilleoaks.view.View
import org.greenvilleoaks.view.ZipView
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate

class ViewSpec extends Specification {
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


    def "Commute Distance"() {
        setup:
        View view = new DistanceView(config.membersCsvColumnMappings.commuteDistance2CentralPointInMeters,
                memberList(config.membersCsvColumnMappings.commuteDistance2CentralPointInMeters,
                        ["1610", "4000", null, "2000"]))

        expect:
        view.name == config.membersCsvColumnMappings.commuteDistance2CentralPointInMeters
        view.data.get("0-1").size() == 2
        view.data.get("1-2").size() == 1
        view.data.get(View.NULL_BIN_NAME).size() == 1
    }


    def "Commute Time"() {
        setup:
        View view = new DurationView(config.membersCsvColumnMappings.commuteTime2CentralPointInSeconds,
                memberList(config.membersCsvColumnMappings.commuteTime2CentralPointInSeconds,
                        [null, Integer.toString(56*60).toString(),
                         Integer.toString(1*60).toString(),   Integer.toString(6*60).toString(),
                         Integer.toString(11*60).toString(),  Integer.toString(16*60).toString(),
                         Integer.toString(21*60).toString(),  Integer.toString(26*60).toString(),
                         Integer.toString(31*60).toString(),  Integer.toString(36*60).toString(),
                         Integer.toString(41*60).toString(),  Integer.toString(46*60).toString(),
                         Integer.toString(51*60).toString(),  Integer.toString(56*60).toString(),
                         Integer.toString(61*60).toString(),  Integer.toString(66*60).toString(),
                         null]))

        expect:
        view.name == config.membersCsvColumnMappings.commuteTime2CentralPointInSeconds
        view.data.get("0-5").size()   == 1
        view.data.get("5-10").size()  == 1
        view.data.get("10-15").size() == 1
        view.data.get("15-20").size() == 1
        view.data.get("20-25").size() == 1
        view.data.get("25-30").size() == 1
        view.data.get("30-35").size() == 1
        view.data.get("35-40").size() == 1
        view.data.get("40-45").size() == 1
        view.data.get("45-50").size() == 1
        view.data.get("50-55").size() == 1
        view.data.get("55-60").size() == 2
        view.data.get("over 60").size()  == 2
        view.data.get(View.NULL_BIN_NAME).size() == 2
    }


    def "Number of People in Household"() {
        setup:
        View view = new NumInHouseholdView(config.membersCsvColumnMappings.numInHousehold,
                memberList(config.membersCsvColumnMappings.address,
                        [
                                "a", "a", "a",
                                "b", "b", "b",
                                "c", "c",
                                "d", "d",
                                "e", "e",
                                "j", "j",
                                "f", "g", "h", "i",
                                null, null, null, null, null
                        ]))

        List<Map<String, String>> histogram = view.createStats()
        Map<String, String> histogramRow1 = histogram.find { it.get("Category") == "1" }
        Map<String, String> histogramRow2 = histogram.find { it.get("Category") == "2" }
        Map<String, String> histogramRow3 = histogram.find { it.get("Category") == "3" }
        Map<String, String> histogramRow5 = histogram.find { it.get("Category") == "5" }

        expect:
        view.name == config.membersCsvColumnMappings.numInHousehold
        view.data.get("1").size() == 4 // a & b
        view.data.get("2").size() == 8 // c, d, e, & j
        view.data.get("3").size() == 6 // f, g, h, & i
        view.data.get("5").size() == 5 // The null addresses

        histogramRow1.get("Number of Members") == "4"
        histogramRow1.get("Percentage of Members") == " 17"
        histogramRow2.get("Number of Members") == "8"
        histogramRow2.get("Percentage of Members") == " 35"
        histogramRow3.get("Number of Members") == "6"
        histogramRow3.get("Percentage of Members") == " 26"
        histogramRow5.get("Number of Members") == "5"
        histogramRow5.get("Percentage of Members") == " 22"
    }


    def "City"() {
        setup:
        View view = new CityView(config.membersCsvColumnMappings.city,
                memberList(config.membersCsvColumnMappings.city, ["a", "b", null, "a"]))

        expect:
        view.name == config.membersCsvColumnMappings.city
        view.data.get("a").size() == 2
        view.data.get("b").size() == 1
        view.data.get(View.NULL_BIN_NAME).size() == 1
    }



    def "Zip"() {
        setup:
        View view = new ZipView(config.membersCsvColumnMappings.zip,
                memberList(config.membersCsvColumnMappings.zip, ["2", "1", null, "2"]))

        expect:
        view.name == config.membersCsvColumnMappings.zip
        view.data.get("2").size() == 2
        view.data.get("1").size() == 1
        view.data.get(View.NULL_BIN_NAME).size() == 1
    }



    def "Grade"() {
        setup:
        View view = new GradeView(config.membersCsvColumnMappings.grade,
                memberList(config.membersCsvColumnMappings.grade, ["a", "b", null, "a"]))

        expect:
        view.name == config.membersCsvColumnMappings.grade
        view.data.get("a").size() == 2
        view.data.get("b").size() == 1
        view.data.get(View.NULL_BIN_NAME).size() == 1
    }



    def "Role"() {
        setup:
        View view = new RoleView(config.membersCsvColumnMappings.role,
                memberList(config.membersCsvColumnMappings.role, ["a", "b", null, "a"]))

        expect:
        view.name == config.membersCsvColumnMappings.role
        view.data.get("a").size() == 2
        view.data.get("b").size() == 1
        view.data.get(View.NULL_BIN_NAME).size() == 1
    }




    def "Age"() {
        setup:
        List<Integer> ages =
        [null,
         5,  10,
         15, 20,
         25, 30,
         35, 40,
         45, 50,
         55, 60,
         65, 70,
         75, 80,
         85, 90,
         95, 100,
         105, 110,
         null]
        List<String> birthdays = []
        ages.each { birthdays << (it ?  LocalDate.now().minusYears(it).format(config.dateFormatter) : null) }
        View view = new AgeView(config.membersCsvColumnMappings.age,
                memberList(config.membersCsvColumnMappings.birthday, birthdays))

        expect:
        view.name == config.membersCsvColumnMappings.age
        view.data.get(" 5").size()  == 1
        view.data.get("10").size() == 1
        view.data.get("15").size() == 1
        view.data.get("20s").size() == 2
        view.data.get("30s").size() == 2
        view.data.get("40s").size() == 2
        view.data.get("50s").size() == 2
        view.data.get("60s").size() == 2
        view.data.get("70s").size() == 2
        view.data.get("80s").size() == 2
        view.data.get("90s").size() == 2
        view.data.get("100s").size() == 3
        view.data.get(View.NULL_BIN_NAME).size() == 2
    }
    
    
    def "Grade Sort Order"() {
        setup:
        View view = new GradeView(config.membersCsvColumnMappings.grade,
                memberList(config.membersCsvColumnMappings.grade, ["12th", "Pre-School 1", "2nd", "Kindergarten", "Graduated", "Pre-School 2", "1st"]))
        List<String> keys = view.sortedDataKeys()
        
        expect:
        keys.size().equals(7)
        keys.get(0) == "Pre-School 1"
        keys.get(1) == "Pre-School 2"
        keys.get(2) == "Kindergarten"
        keys.get(3) == "1st"
        keys.get(4) == "2nd"
        keys.get(5) == "12th"
        keys.get(6) == "Graduated"
    }
}
