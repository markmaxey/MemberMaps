package org.greenvilleoaks.map

import com.google.api.services.mapsengine.model.MapFolder
import com.google.api.services.mapsengine.model.MapItem
import com.google.api.services.mapsengine.model.MapLayer
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by Mark on 1/11/2015.
 */
class RemoveLayerIdsFromMapSpec extends Specification {
    @Shared MapAdapter cut = new MapAdapter(engine: null)


    def "Empty LayerIds"() {
        setup:
        List<MapItem> mapContents = [
                new MapLayer().setId("1")
        ]

        when:
        List<MapItem> newContents = cut.removeLayerIdsFromMap([], mapContents)

        then:
        newContents.size() == 1
        newContents[0].id == "1"
    }


    def "Null LayerIds"() {
        setup:
        List<MapItem> mapContents = [
                new MapLayer().setId("1")
        ]
        List<String> layerIds = null
        
        when:
        List<MapItem> newContents = cut.removeLayerIdsFromMap(layerIds, mapContents)
        
        then:
        newContents.size() == 1
        newContents[0].id == "1"
    }


    def "Empty Map Contents"() {
        when:
        List<MapItem> newContents = cut.removeLayerIdsFromMap(["1"], [])

        then:
        newContents.size() == 0
    }


    def "Null Map Contents"() {
        when:
        List<MapItem> newContents = cut.removeLayerIdsFromMap(["1"], null)

        then:
        newContents.size() == 0
    }


    def "Flat Map Content"() {
        setup:
        List<MapItem> mapContents = [
                new MapLayer().setId("1"),
                new MapLayer().setId("2"),
                new MapLayer().setId("3"),
        ]

        when:
        List<MapItem> newContents = cut.removeLayerIdsFromMap(["2"], mapContents)

        then:
        newContents.size() == 2
        newContents[0].id == "1"
        newContents[1].id == "3"
    }




    def "Nested Map Content"() {
        setup:
        List<MapItem> mapContents = [
                new MapLayer().setId("1"),
                new MapLayer().setId("2"),
                new MapFolder().setContents([
                        new MapLayer().setId("3"),
                        new MapLayer().setId("4"),
                        new MapLayer().setId("5"),
                        new MapFolder().setContents([
                                new MapLayer().setId("8"),
                                new MapLayer().setId("9"),
                                new MapLayer().setId("10"),
                        ]),
                        new MapLayer().setId("11"),
                ]),
                new MapFolder().setContents([
                        new MapLayer().setId("6"),
                ]),
                new MapLayer().setId("7"),
        ]

        when:
        List<MapItem> newContents = cut.removeLayerIdsFromMap(["2", "3", "6", "10"], mapContents)

        then:
        newContents.size() == 3
        newContents[0].id == "1"

        ((MapFolder)newContents[1]).getContents().size() == 4
        ((MapFolder)newContents[1]).getContents()[0].id == "4"
        ((MapFolder)newContents[1]).getContents()[1].id == "5"

        ((MapFolder)((MapFolder)newContents[1]).getContents()[2]).getContents().size() == 2
        ((MapFolder)((MapFolder)newContents[1]).getContents()[2]).getContents()[0].id == "8"
        ((MapFolder)((MapFolder)newContents[1]).getContents()[2]).getContents()[1].id == "9"

        ((MapFolder)newContents[1]).getContents()[3].id == "11"

        newContents[2].id == "7"
    }
}
