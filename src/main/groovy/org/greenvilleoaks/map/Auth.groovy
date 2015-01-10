package org.greenvilleoaks.map

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.PemReader
import com.google.api.client.util.SecurityUtils
import groovy.transform.Immutable
import groovy.util.logging.Log4j

import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Authorize to Google by creating credentials used in all Google Map API services
 */
@Immutable
@Log4j
class Auth {
    /**
     * Authorize using service account credentials.
     *
     * @param httpTransport The HTTP transport to use for network requests.
     * @param jsonFactory The JSON factory to use for serialization / de-serialization.
     * @param scopes The scopes for which this app should authorize.
     * @param secretsFile The JSON file containing the private_key_id, private_key, client_email, client_id, and type.
     */
    public GoogleCredential authorizeService(
            final HttpTransport httpTransport,
            final JsonFactory jsonFactory,
            final Collection<String> scopes,
            final File secretsFile) throws IOException {
        if (!secretsFile.exists()) {
            log.error("Private key file not found.\n"
                    + "Follow the instructions at https://developers.google"
                    + ".com/maps-mapsEngine/documentation/oauth/serviceaccount#creating_a_service_account\n"
                    + "and save the generated JSON key to " + secretsFile.getAbsolutePath());
            System.exit(1);
        }

        try {
            // Load the client secret details from file.
            GenericJson secrets = jsonFactory.fromReader(new FileReader(secretsFile), GenericJson.class);

            // Extract the raw key from the supplied JSON file
            String privateKeyString = (String) secrets.get("private_key");
            byte[] keyBytes = new PemReader(new StringReader(privateKeyString))
                    .readNextSection()
                    .getBase64DecodedBytes();

            // Turn it into a PrivateKey
            PrivateKey privateKey = SecurityUtils.getRsaKeyFactory()
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

            // And lastly, turn that into a GoogleCredential
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId((String) secrets.get("client_email"))
                    .setServiceAccountPrivateKey(privateKey)
                    .setServiceAccountScopes(scopes)
                    .setServiceAccountUser()
                    .build();

            // Force a first-time update, so we have a fresh key
            credential.refreshToken();
            return credential;
        } catch (FileNotFoundException e) {
            throw new AssertionError("File not found should already be handled.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Encountered an unexpected algorithm when "
                    + "processing the supplied private key.", e);
        } catch (InvalidKeySpecException e) {
            throw new AssertionError("Encountered an invalid key specification when "
                    + "processing the supplied private key.", e);
        }
    }


}
