package org.greenvilleoaks.view

import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.map.LayerAdapter

/** Members grouped by the role(s) they serve in */
@ToString
@EqualsAndHashCode
final class RoleView extends View {
    public RoleView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        // TODO: Support members having multiple roles
        return create(members, { MemberBean member -> member.role ?: NULL_BIN_NAME })
    }


    @Override
    Map<String, List<DisplayRule>> createDisplayRules(CsvColumnMappings csvColumnMappings, LayerAdapter layerAdapter, PointStyle pointStyle) {
        return createContainsDisplayRules(csvColumnMappings.role, layerAdapter, pointStyle)
    }
}