package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean

/** Members grouped by the city they live in */
@ToString
@EqualsAndHashCode
final class DistanceView extends View {
    public DistanceView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member ->
            if (member.commuteDistance2CentralPointInMeters == null) return NULL_BIN_NAME
            int miles = member.commuteDistance2CentralPointInMeters * 0.000621371d
            return (miles == 0) ? "0" : Integer.toString(miles-1) + "-" + Integer.toString(miles)
        })
    }
}