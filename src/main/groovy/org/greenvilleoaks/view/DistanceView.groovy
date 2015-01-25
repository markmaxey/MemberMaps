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
final class DistanceView extends View {
    public DistanceView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member ->
            if (member.commuteDistance2CentralPointInMeters == null) return NULL_BIN_NAME
            return categoryName(meters2Miles(member.commuteDistance2CentralPointInMeters))
        })
    }


    private String categoryName(int miles) {
        return (miles == 0) ? "00" : String.format("%02d", miles-1) + "-" + String.format("%02d", miles)
    }

    private int meters2Miles(long meters) { return meters * 0.000621371d }

    private int miles2Meters(long miles)  { return miles * 1609.34d }



    @Override
    Map<String, List<DisplayRule>> createDisplayRules(
            final CsvColumnMappings csvColumnMappings,
            final LayerAdapter layerAdapter,
            final PointStyle pointStyle) {
        Map<String, List<DisplayRule>> category2DisplayRules = new TreeMap<String, List<DisplayRule>>()

        sortedDataKeys().each { String category ->
            if (!NULL_BIN_NAME.equals(category)) {
                List displayRules = []

                if ("0".equals(category)) {
                    displayRules << [
                            layerAdapter.createDisplayRule(
                                    pointStyle,
                                    [layerAdapter.createFilter(csvColumnMappings.commuteDistance2CentralPointInMeters,
                                            NumOperatorEnum.lessThanEqual, miles2Meters(1))]
                            ),
                    ]
                }
                else {
                    if ("00".equals(category)) {
                        displayRules << [
                                layerAdapter.createDisplayRule(
                                        pointStyle,
                                        [layerAdapter.createFilter(csvColumnMappings.commuteDistance2CentralPointInMeters,
                                                NumOperatorEnum.lessThanEqual, 1579)]
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
                                                NumOperatorEnum.greaterThanEqual, miles2Meters(lb))]
                                ),
                                layerAdapter.createDisplayRule(
                                        pointStyle,
                                        [layerAdapter.createFilter(csvColumnMappings.commuteDistance2CentralPointInMeters,
                                                NumOperatorEnum.lessThanEqual, miles2Meters(ub))]
                                ),
                        ]
                    }
                }
                category2DisplayRules.put(category, displayRules)
            }
        }

        return category2DisplayRules
    }
}