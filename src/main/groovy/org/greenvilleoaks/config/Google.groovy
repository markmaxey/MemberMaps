package org.greenvilleoaks.config

import com.google.maps.GeoApiContext
import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class Google {
    /** The Google API context used for all Google API calls */
    public GeoApiContext context = new GeoApiContext()

    /** The Google Maps Engine Project ID */
    public String mapsEngineProjectId = "01824222381788524396"

    /** The Google APIs project number */
    public String apisProjectNumber = "297047284747"

    /** The public access key created for the organization required to authenticate to Google Map APIs */
    public String apiKey = "AIzaSyBqAYLqYrV9ArcEsU3MNi3ffHbf-BQ3F1s"

    /** The name of the generated OAuth JSON service account private key */
    public String jsonKeyFileName =
            System.properties.getProperty("user.home") + "\\Documents\\GO_Members_Map\\\\Greenville Oaks-0a1fa0b78eac.json"
    
    
    public Google init() {
        context.apiKey = apiKey
        return this
    }
}
