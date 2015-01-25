package org.greenvilleoaks.view

import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.map.LayerAdapter
import org.greenvilleoaks.map.NumOperatorEnum

/** Members grouped by the city they live in */
@ToString
@EqualsAndHashCode
final class DurationView extends View {
    public DurationView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member ->
            if (member.commuteTime2CentralPointInSeconds == null) return NULL_BIN_NAME

            int minutes = seconds2Minutes(member.commuteTime2CentralPointInSeconds)

            if (minutes < 5) {
                return "0-5"
            }
            else if (minutes < 10) {
                return "5-10"
            }
            else if (minutes < 15) {
                return "10-15"
            }
            else if (minutes < 20) {
                return "15-20"
            }
            else if (minutes < 25) {
                return "20-25"
            }
            else if (minutes < 30) {
                return "25-30"
            }
            else if (minutes < 35) {
                return "30-35"
            }
            else if (minutes < 40) {
                return "35-40"
            }
            else if (minutes < 45) {
                return "40-45"
            }
            else if (minutes < 50) {
                return "45-50"
            }
            else if (minutes < 55) {
                return "50-55"
            }
            else if (minutes < 60) {
                return "55-60"
            }
            else {
                return "over 60"
            }
        })
    }

    private int seconds2Minutes(long seconds) { return seconds / 60.0d }

    private int minutes2Seconds(long minutes)  { return minutes * 60 }



    @Override
    Map<String, List<DisplayRule>> createDisplayRules(
            final CsvColumnMappings csvColumnMappings,
            final LayerAdapter layerAdapter,
            final PointStyle pointStyle) {
        Map<String, List<DisplayRule>> category2DisplayRules = new TreeMap<String, List<DisplayRule>>()

        sortedDataKeys().each { String category ->
            if (!NULL_BIN_NAME.equals(category)) {
                List displayRules = []

                if ("over 60".equals(category)) {
                    displayRules << [
                            layerAdapter.createDisplayRule(
                                    pointStyle,
                                    [layerAdapter.createFilter(csvColumnMappings.commuteDistance2CentralPointInMeters,
                                            NumOperatorEnum.greaterThanEqual, minutes2Seconds(60))]
                            ),
                    ]
                }
                else {
                    long lb = Long.valueOf(category.substring(0, category.indexOf("-")))
                    long ub = Long.valueOf(category.substring(category.indexOf("-") + 1))
                    displayRules << [
                            layerAdapter.createDisplayRule(
                                    pointStyle,
                                    [layerAdapter.createFilter(csvColumnMappings.commuteDistance2CentralPointInMeters,
                                            NumOperatorEnum.greaterThanEqual, minutes2Seconds(lb))]
                            ),
                            layerAdapter.createDisplayRule(
                                    pointStyle,
                                    [layerAdapter.createFilter(csvColumnMappings.commuteDistance2CentralPointInMeters,
                                            NumOperatorEnum.lessThanEqual, minutes2Seconds(ub))]
                            ),
                    ]
                }
                category2DisplayRules.put(category, displayRules)
            }
        }

        return category2DisplayRules
    }
}