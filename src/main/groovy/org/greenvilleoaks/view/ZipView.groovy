package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean

/** Members grouped by the zip code they live in */
@ToString
@EqualsAndHashCode
final class ZipView extends View {
    public ZipView(final String name, final List<MemberBean> members) { super(name, members) }

    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member -> member.zip ?: NULL_BIN_NAME})
    }
}