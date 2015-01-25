package org.greenvilleoaks.view

import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.map.LayerAdapter
import org.greenvilleoaks.map.StringOperatorEnum

/** Members grouped by their age */
@ToString
@EqualsAndHashCode
final class AgeView extends View {
    public AgeView(final String name, final List<MemberBean> members) { super(name, members) }

    @Override
    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member ->
            if (member.age == null) return NULL_BIN_NAME

            String ageBin
            int age = Integer.valueOf(member.age)

            if (age < 20) {
                ageBin = String.format("%2d", age)
            } else if (age < 30) {
                ageBin = "20s"
            } else if (age < 40) {
                ageBin = "30s"
            } else if (age < 50) {
                ageBin = "40s"
            } else if (age < 60) {
                ageBin = "50s"
            } else if (age < 70) {
                ageBin = "60s"
            } else if (age < 80) {
                ageBin = "70s"
            } else if (age < 90) {
                ageBin = "80s"
            } else if (age < 100) {
                ageBin = "90s"
            }
            else {
                ageBin = "100s"
            }

            return ageBin
        })
    }
    
    
    private List<String> filterValues(final String category) {
        if (!category.contains("s")) {
            return [category.trim()]
        } 
        else if ("20s".equals(category)) {
            return ["20", "21", "22", "23", "24", "25", "26", "27", "28", "29"]
        }
        else if ("30s".equals(category)) {
            return ["30", "31", "32", "33", "34", "35", "36", "37", "38", "39"]
        }
        else if ("40s".equals(category)) {
            return ["40", "41", "42", "43", "44", "45", "46", "47", "48", "49"]
        }
        else if ("50s".equals(category)) {
            return ["50", "51", "52", "53", "54", "55", "56", "57", "58", "59"]
        }
        else if ("60s".equals(category)) {
            return ["60", "61", "62", "63", "64", "65", "66", "67", "68", "69"]
        }
        else if ("70s".equals(category)) {
            return ["70", "71", "72", "73", "74", "75", "76", "77", "78", "79"]
        }
        else if ("80s".equals(category)) {
            return ["80", "81", "82", "83", "84", "85", "86", "87", "88", "89"]
        }
        else if ("90s".equals(category)) {
            return ["90", "91", "92", "93", "94", "95", "96", "97", "98", "99"]
        }
        else {
            return ["100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113"]
        }
    }
    


    @Override
    Map<String, List<DisplayRule>> createDisplayRules(
            final CsvColumnMappings csvColumnMappings,
            final LayerAdapter layerAdapter,
            final PointStyle pointStyle) {
        Map<String, List<DisplayRule>> category2DisplayRules = new TreeMap<String, List<DisplayRule>>()

        sortedDataKeys().each { String category ->
            if (!NULL_BIN_NAME.equals(category)) {
                filterValues(category).each { String filterValue ->
                    List displayRules = []
                    displayRules << [
                            layerAdapter.createDisplayRule(
                                    pointStyle,
                                    [layerAdapter.createFilter(
                                            csvColumnMappings.age, StringOperatorEnum.contains, " $filterValue,")]
                            )
                    ]
/*
                    displayRules << [
                            layerAdapter.createDisplayRule(
                                    pointStyle,
                                    [layerAdapter.createFilter(
                                            csvColumnMappings.age, StringOperatorEnum.endsWith, " $filterValue")]
                            )
                    ]
*/
                    category2DisplayRules.put(category, displayRules)
                }
            }
        }
        
        return category2DisplayRules
    }
}