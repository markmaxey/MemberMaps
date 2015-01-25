package org.greenvilleoaks.view

import com.google.api.services.mapsengine.model.DisplayRule
import com.google.api.services.mapsengine.model.PointStyle
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.greenvilleoaks.beans.MemberBean
import org.greenvilleoaks.config.CsvColumnMappings
import org.greenvilleoaks.map.LayerAdapter

import java.util.regex.Matcher
import java.util.regex.Pattern

/** Members grouped by the grade they are in */
@ToString
@EqualsAndHashCode
final class GradeView extends View {
    private static final Pattern pattern = Pattern.compile("^[a-zA-Z -]*([0-9]+).*");
    
    private static final Comparator gradeComparator = new Comparator() {
        @Override
        int compare(Object o1, Object o2) {
            String gradeStr1 = o1
            String gradeStr2 = o2

            if (gradeStr1 == gradeStr2) {
                return 0
            }
            else if (gradeStr1 == null) {
                return -1
            }
            else if (gradeStr2 == null) {
                return 1
            }
            else if ("Graduated".equalsIgnoreCase(gradeStr1)) {
                return 1
            }
            else if ("Graduated".equalsIgnoreCase(gradeStr2)) {
                return -1
            }
            else {
                Matcher m1 = pattern.matcher(gradeStr1);
                Matcher m2 = pattern.matcher(gradeStr2);

                if ((m1.matches() && m2.matches()) ||
                        gradeStr1.toLowerCase().contains("kinder") || gradeStr2.toLowerCase().contains("kinder")) {
                    
                    int gradeNum1, gradeNum2
                    
                    if (gradeStr1.toLowerCase().contains("kinder") && gradeStr2.toLowerCase().contains("kinder")) {
                        return 0
                    }
                    else if (gradeStr1.toLowerCase().contains("kinder") && m2.matches()) {
                        gradeNum1 = 0
                        gradeNum2 = Integer.valueOf(m2.group(1))
                    }
                    else if (gradeStr2.toLowerCase().contains("kinder") && m1.matches()) {
                        gradeNum1 = Integer.valueOf(m1.group(1))
                        gradeNum2 = 0
                    }
                    else if (m1.matches() && m2.matches()) {
                        gradeNum1 = Integer.valueOf(m1.group(1))
                        gradeNum2 = Integer.valueOf(m2.group(1))
                    }
                    else if (!m1.matches() && !m2.matches()) {
                        return gradeStr1.compareTo(gradeStr2)
                    }
                    
                    if (gradeStr1.toLowerCase().contains("pre") && gradeStr2.toLowerCase().contains("pre")) {
                        return gradeNum1 - gradeNum2
                    }
                    else if (gradeStr1.toLowerCase().contains("pre")) {
                        return -1
                    }
                    else if (gradeStr2.toLowerCase().contains("pre")) {
                        return 1
                    }
                    else {
                        return gradeNum1 - gradeNum2
                    }

                }
                else {
                    return gradeStr1.compareTo(gradeStr2)
                }
            }
        }
    }


    public GradeView(final String name, final List<MemberBean> members) {
        super(name, members, gradeComparator)
    }


    public Map<String, List<MemberBean>> createViewData(final List<MemberBean> members) {
        return create(members, { MemberBean member -> member.grade ?: NULL_BIN_NAME})
    }


    @Override
    Map<String, List<DisplayRule>> createDisplayRules(CsvColumnMappings csvColumnMappings, LayerAdapter layerAdapter, PointStyle pointStyle) {
        return createContainsDisplayRules(csvColumnMappings.grade, layerAdapter, pointStyle)
    }


    protected List<String> sortedDataKeys() {
        List keys = []
        data.keySet().each { keys << it }
        Collections.sort(keys, comparator.get())
        return keys
    }
}