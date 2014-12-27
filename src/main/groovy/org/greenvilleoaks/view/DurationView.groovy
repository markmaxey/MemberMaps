package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean

/** Members grouped by the city they live in */
@ToString
@EqualsAndHashCode
final class DurationView extends View {
    public DurationView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member ->
            if (member.commuteTime2CentralPointInSeconds == null) return NULL_BIN_NAME

            int minutes = member.commuteTime2CentralPointInSeconds / 60.0d
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
                return "> 60"
            }
        })
    }
}