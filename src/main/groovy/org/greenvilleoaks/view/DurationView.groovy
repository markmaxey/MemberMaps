package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** Members grouped by the city they live in */
@ToString
@EqualsAndHashCode
final class DurationView extends View {
    public DurationView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        return create(members, { Member member ->
            int minutes = member.durationInSeconds / 60.0d
            if (minutes < 5) {
                return "< 5"
            }
            else if (minutes < 10) {
                return "< 10"
            }
            else if (minutes < 15) {
                return "< 15"
            }
            else if (minutes < 20) {
                return "< 20"
            }
            else if (minutes < 25) {
                return "< 25"
            }
            else if (minutes < 30) {
                return "< 30"
            }
            else if (minutes < 35) {
                return "< 35"
            }
            else if (minutes < 40) {
                return "< 40"
            }
            else if (minutes < 45) {
                return "< 45"
            }
            else if (minutes < 50) {
                return "< 50"
            }
            else if (minutes < 55) {
                return "< 55"
            }
            else if (minutes < 60) {
                return "< 60"
            }
            else {
                return "over an hour"
            }
        })
    }
}