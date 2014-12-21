package org.greenvilleoaks.view

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.Member

/**
 * A perspective of the create.
 */
@ToString
@EqualsAndHashCode
abstract class View {
    public static final String NULL_BIN_NAME = "Unspecified"

    /** The name of the perspective */
    final String name

    /** The perspective data */
    final Map<String, List<Map<String, Object>>> data

    /** The column headers and map keys */
    final String[] headers = ["Category", "Number of Members", "Percentage of Members"]


    public View(final String name, final List<Member> members) {
        this.name = name
        this.data = createViewData(members)
    }


    abstract Map<String, List<Map<String, Object>>> createViewData(final List<Member> members);


    /**
     * @return A histogram of the member's perspective
     */
    public List<Map<String, Object>> createStats() {
        List<Map<String, String>> stats = []
        int totalNumMembers = 0
        data.values().each { List<Map<String, Object>> members -> totalNumMembers += members.size() }

        int memberSizeWidth = 0
        data.values().each { List<Map<String, Object>> members ->
            if (memberSizeWidth < members.size()) memberSizeWidth = members.size()
        }

        data.each { String category, List<Map<String, Object>> members ->
            double percentage = (double)members.size() / (double)totalNumMembers
            stats << [
                    "$headers[0]" : category,
                    "$headers[1]" : String.format("%${memberSizeWidth}d", members.size()),
                    "$headers[2]" : String.format("%3d", (int)(percentage * 100))
            ]
        }

        return stats
    }


    protected static void addValue(
            final String filterKey,
            final Map<String, List<Member>> view,
            final Member member) {
        List<Member> entryList = view.get(filterKey)
        if (!entryList) {
            entryList = []
            view.put(filterKey, entryList)
        }

        entryList << member
    }


    protected Map<String, List<Member>> create(
            List<Member> members,
            Closure binName) {
        Map<String, List<Member>> view = [:]

        members.each { Member member ->
            addValue((String)binName.call(member), view, member)
        }

        return view
    }
}
