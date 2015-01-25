package org.greenvilleoaks.config

import groovy.transform.ToString
import org.greenvilleoaks.map.StockIconNames

@ToString(includeNames = true, includeFields = true)
class MapInfo {
    private final boolean isPublic

    /** The qualifier appended to layers & maps to differentiate between private & public versions */
    public String privatePublicQualifier
    
    /** HTML describing the contents of the info windows that pop up when you click on an address */
    public String featureInfoContent
    
    /**
     * The icon to use on the map 
       https://developers.google.com/maps-engine/documentation/reference/v1/layers/create#IconStyle.json
       https://www.google.com/fusiontables/DataSource?dsrcid=308519#map:id=3
    */
    public String stockIconName

    /**
     * Four numbers (west, south, east, north) which defines the rectangular bounding box
     * of the default viewport. The numbers represent latitude and longitude in decimal degrees.
     * These values must be overridden
     */
    public double west, south, east, north
    public List<Double> defaultViewport = []


    public MapInfo(final boolean isPublic) {
        this.isPublic = isPublic
    }


    public init(final String centralPointName, final List<String> memberRoleCommuteList) {
        // TODO: Move GO specific values out to a config file
        west=-97.60259928515632
        south=32.82666786882835
        east=-95.72668375781257
        north=33.43846521541789
        
        if (west != 0 && south != 0 && east != 0 && north != 0) defaultViewport = [west, south, east, north]
        
        String roleCommute = ""
        memberRoleCommuteList.each { String role ->
            roleCommute += """
<b>Minimum Distance to $role:</b> {Minimum Commute Distance to $role}<br>
<b>Minimum Time to $role:</b> {Minimum Commute Time to $role}<br>
<b>Closest $role:</b> {Minimum Commute to $role}<br>
"""
        }
        
        if (isPublic) {
            privatePublicQualifier = " (public)"
            stockIconName = StockIconNames.gx_measle_blue.toString()

            featureInfoContent = """
<div class='googeb-info-window' style='font-family: sans-serif'>
<h1><b>Name</b></h1>
<b>Directory Name:</b> {Directory Name}<br>
<b>Last Name:</b> {Last Name}<br>
<b>Preferred Name:</b> {Preferred Name}<br>
<h1><b>Info</b></h1>
<b>Formatted Address:</b> {Formatted Address}<br>
<b>Number in Household:</b> {Number in Household}<br>
<b>Birth Date:</b> {Birth Date}<br>
<b>Age:</b> {Age}<br>
<b>School Grade:</b> {School Grade}<br>
<b>Role:</b> {Role}<br>
<h1><b>Commute</b></h1>
<b>Distance to $centralPointName:</b> {Commute Distance to $centralPointName}<br>
<b>Time to $centralPointName:</b> {Commute Time to $centralPointName}<br>
$roleCommute
</div>"""
        }
        else {
            privatePublicQualifier = " (private)"
            stockIconName = StockIconNames.gx_measle_turquoise.toString()

            featureInfoContent = """
<div class='googeb-info-window' style='font-family: sans-serif'>
<h1><b>Name</b></h1>
<b>Directory Name:</b> {Directory Name}<br>
<b>Last Name:</b> {Last Name}<br>
<b>Preferred Name:</b> {Preferred Name}<br>
<h1><b>Info</b></h1>
<b>Formatted Address:</b> {Formatted Address}<br>
<b>Number in Household:</b> {Number in Household}<br>
<b>School Grade:</b> {School Grade}<br>
<b>Role:</b> {Role}<br>
<h1><b>Commute</b></h1>
<b>Distance to $centralPointName:</b> {Commute Distance to $centralPointName}<br>
<b>Time to $centralPointName:</b> {Commute Time to $centralPointName}<br>
$roleCommute
</div>"""
        }
    }
}