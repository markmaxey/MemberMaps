package org.greenvilleoaks.view

import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.map.LayerAdapter

/** Members grouped by the zip code they live in */
@ToString
@EqualsAndHashCode
final class ZipView extends View {
    public ZipView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member -> member.zip ?: NULL_BIN_NAME})
    }

    @Override
    Map<String, List<DisplayRule>> createDisplayRules(CsvColumnMappings csvColumnMappings, LayerAdapter layerAdapter, PointStyle pointStyle) {
        return createIdentityDisplayRules(csvColumnMappings.zip, layerAdapter, pointStyle)
    }
}