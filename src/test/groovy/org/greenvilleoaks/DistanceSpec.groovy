package org.greenvilleoaks

import com.google.maps.model.DistanceMatrixElement
import com.google.maps.model.Duration
import org.greenvilleoaks.mocks.GoogleFaultCode
import org.greenvilleoaks.mocks.GoogleMock
import spock.lang.Shared
import spock.lang.Specification

class DistanceSpec extends Specification {
    // The Class Under Test (CUT)
    @Shared Distance distanceWithoutGoogle = new Distance(null)
    @Shared Distance distanceWithGoogle = new Distance(new GoogleMock(
            [
                    GoogleFaultCode.none,
                    GoogleFaultCode.none,
                    GoogleFaultCode.none,
                    GoogleFaultCode.none,
                    GoogleFaultCode.none,
                    GoogleFaultCode.none,
                    GoogleFaultCode.none,
            ],
            [] as Set<String>))

    @Shared List<DistanceBean> distanceCache = [
            [
                    address1: "5",
                    address2: "2",
                    distanceInMeters: 3,
                    distanceHumanReadable: "3",
                    durationInSeconds: 3,
                    durationHumanReadable: "3"
            ] as DistanceBean
    ]
    
    @Shared DistanceMatrixElement cachedDistanceMatrixElement = [
            distance: [
                    inMeters: 3,
                    humanReadable: "3"
            ] as com.google.maps.model.Distance,

            duration: [
                    inSeconds: 3,
                    humanReadable: "3"
            ] as Duration,
    ] as DistanceMatrixElement
    
    
    private boolean dmeEquals(final DistanceMatrixElement dme1, final DistanceMatrixElement dme2) {
        return dme1 && dme2 && dme1.distance && dme2.distance && dme1.duration && dme2.duration &&
                dme1.distance.inMeters      == dme2.distance.inMeters &&
                dme1.distance.humanReadable == dme2.distance.humanReadable &&
                dme1.duration.inSeconds     == dme2.duration.inSeconds &&
                dme1.duration.humanReadable == dme2.duration.humanReadable
    }
    
    
    def "Verify dmeEquals"() {
        setup:
        DistanceMatrixElement dme0 = [
                distance: [
                        inMeters: 3,
                        humanReadable: "3"
                ] as com.google.maps.model.Distance,

                duration: [
                        inSeconds: 3,
                        humanReadable: "3"
                ] as Duration,
        ] as DistanceMatrixElement

        DistanceMatrixElement dme1 = [
                distance: [
                        inMeters: 1,
                        humanReadable: "3"
                ] as com.google.maps.model.Distance,

                duration: [
                        inSeconds: 3,
                        humanReadable: "3"
                ] as Duration,
        ] as DistanceMatrixElement

        DistanceMatrixElement dme2 = [
                distance: [
                        inMeters: 3,
                        humanReadable: "1"
                ] as com.google.maps.model.Distance,

                duration: [
                        inSeconds: 3,
                        humanReadable: "3"
                ] as Duration,
        ] as DistanceMatrixElement

        DistanceMatrixElement dme3 = [
                distance: [
                        inMeters: 3,
                        humanReadable: "3"
                ] as com.google.maps.model.Distance,

                duration: [
                        inSeconds: 1,
                        humanReadable: "3"
                ] as Duration,
        ] as DistanceMatrixElement

        DistanceMatrixElement dme4 = [
                distance: [
                        inMeters: 3,
                        humanReadable: "3"
                ] as com.google.maps.model.Distance,

                duration: [
                        inSeconds: 3,
                        humanReadable: "1"
                ] as Duration,
        ] as DistanceMatrixElement
        
        expect:
        dmeEquals(cachedDistanceMatrixElement, dme0)
        !dmeEquals(cachedDistanceMatrixElement, dme1)
        !dmeEquals(cachedDistanceMatrixElement, dme2)
        !dmeEquals(cachedDistanceMatrixElement, dme3)
        !dmeEquals(cachedDistanceMatrixElement, dme4)
    }


    def "Find the distance for a cached address specified in both orders"() {
        expect:
        dmeEquals(distanceWithoutGoogle.findDistance("5", "2", distanceCache), cachedDistanceMatrixElement)
        dmeEquals(distanceWithoutGoogle.findDistance("2", "5", distanceCache), cachedDistanceMatrixElement)
        distanceCache.size() == 1
    }

    
    def "Find the distance for an addressed that isn't cached"() {
        setup:
        DistanceMatrixElement uncachedDistanceMatrixElement = [
                distance: [
                        inMeters: 4,
                        humanReadable: "4"
                ] as com.google.maps.model.Distance,

                duration: [
                        inSeconds: 4,
                        humanReadable: "4"
                ] as Duration,
        ] as DistanceMatrixElement
        
        expect:
        dmeEquals(distanceWithGoogle.findDistance("6", "2", distanceCache), uncachedDistanceMatrixElement)
        dmeEquals(distanceWithGoogle.findDistance("2", "6", distanceCache), uncachedDistanceMatrixElement)
        distanceCache.size() == 2
        distanceCache[0].distanceHumanReadable == "4" || distanceCache[0].distanceHumanReadable == "3"
        distanceCache[1].distanceHumanReadable == "4" || distanceCache[1].distanceHumanReadable == "3"
    }
}
