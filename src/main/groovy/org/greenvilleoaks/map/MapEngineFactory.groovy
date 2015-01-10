package org.greenvilleoaks.map

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.mapsengine.MapsEngine
import com.google.api.services.mapsengine.MapsEngineScopes
import com.google.maps.clients.BackOffWhenRateLimitedRequestInitializer
import com.google.maps.clients.HttpRequestInitializerPipeline
import groovy.util.logging.Log4j


/** A factory for creating MapEngines */
@Log4j
class MapEngineFactory {
    public MapsEngine createMapEngine(
            final HttpTransport httpTransport,
            final JsonFactory jsonFactory,
            final String applicationName,
            final File secretsFile) {
        log.info("Authorizing")
        GoogleCredential credential = new Auth().authorizeService(
                httpTransport, jsonFactory, Arrays.asList(MapsEngineScopes.MAPSENGINE), secretsFile)
        log.info("Authorization successful for user '" + credential.getServiceAccountUser() +
                "' and service account ID '" + credential.getServiceAccountId() + "'")


        // Set up the required initializers to 1) authenticate the request and 2) back off if we
        // start hitting the server too quickly.
        HttpRequestInitializer requestInitializers = new HttpRequestInitializerPipeline(
                Arrays.asList(credential, new BackOffWhenRateLimitedRequestInitializer()));


        // The MapsEngine object will be used to perform the requests.
        log.info("Creating MapEngine for $applicationName")
        return new MapsEngine.Builder(httpTransport, jsonFactory, requestInitializers)
                .setApplicationName(applicationName)
                .build();
    }
}
