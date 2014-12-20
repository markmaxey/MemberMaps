package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** Members grouped by the city they live in */
@ToString
@EqualsAndHashCode
final class DistanceView extends View {
    public DistanceView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        return create(members, { Member member ->
            int miles = member.distanceInMeters * 0.000621371d
            return Integer.toString(miles)
        })
    }
}