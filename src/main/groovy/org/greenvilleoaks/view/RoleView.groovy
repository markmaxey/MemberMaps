package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/** Members grouped by the role(s) they serve in */
@ToString
@EqualsAndHashCode
final class RoleView extends View {
    public RoleView(final String name, final List<Member> members) { super(name, members) }

    public Map<String, List<Member>> createViewData(final List<Member> members) {
        // TODO: Support members having multiple roles
        return create(members, { Member member -> member.role ?: NULL_BIN_NAME })
    }
}