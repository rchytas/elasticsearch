/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.idp.saml.support;

import org.elasticsearch.Version;
import org.elasticsearch.common.ValidationException;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.SerializationTestUtils;
import org.elasticsearch.test.VersionUtils;
import org.elasticsearch.xpack.idp.saml.test.IdpSamlTestCase;
import org.hamcrest.Matchers;
import org.opensaml.saml.saml2.core.NameID;

import java.io.IOException;

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class SamlAuthenticationStateTests extends IdpSamlTestCase {

    public void testValidation() {
        final SamlAuthenticationState state = new SamlAuthenticationState();
        final ValidationException validationException = state.validate();
        assertThat(validationException, notNullValue());
        assertThat(validationException.validationErrors(), not(emptyIterable()));
        assertThat(validationException.validationErrors(), Matchers.containsInAnyOrder(
            "field [entity_id] is required, but was [null]",
            "field [acs_url] is required, but was [null]"
        ));
    }

    public void testXContentRoundTrip() throws Exception {
        final SamlAuthenticationState state1 = generateAuthnState();
        final SamlAuthenticationState state2 = assertXContentRoundTrip(state1);
        assertThat(assertXContentRoundTrip(state2), equalTo(state1));
    }

    public void testNullStateXContentRoundTrip() throws Exception {
        final SamlAuthenticationState state1 = generateMinimalAuthnState();
        final SamlAuthenticationState state2 = assertXContentRoundTrip(state1);
        assertThat(assertXContentRoundTrip(state2), equalTo(state1));
    }

    public void testStreamRoundTrip() throws Exception {
        final SamlAuthenticationState state1 = generateAuthnState();
        final SamlAuthenticationState state2 = assertSerializationRoundTrip(state1);
        assertThat(assertSerializationRoundTrip(state2), equalTo(state1));
    }

    public void testNullStateStreamRoundTrip() throws Exception {
        final SamlAuthenticationState state1 = generateMinimalAuthnState();
        final SamlAuthenticationState state2 = assertSerializationRoundTrip(state1);
        assertThat(assertSerializationRoundTrip(state2), equalTo(state1));
    }

    private SamlAuthenticationState generateAuthnState() {
        SamlAuthenticationState authnState = new SamlAuthenticationState();
        authnState.setRequestedAcsUrl("https://" + randomAlphaOfLengthBetween(4, 8) + "." + randomAlphaOfLengthBetween(4, 8) + "/saml/acs");
        authnState.setEntityId("urn:" + randomAlphaOfLengthBetween(4, 8) + "." + randomAlphaOfLengthBetween(4, 8));
        authnState.setAuthnRequestId(randomAlphaOfLength(12));
        authnState.setRequestedNameidFormat(randomFrom(NameID.TRANSIENT, NameID.PERSISTENT, NameID.EMAIL));
        return authnState;
    }

    private SamlAuthenticationState generateMinimalAuthnState() {
        SamlAuthenticationState authnState = new SamlAuthenticationState();
        authnState.setRequestedAcsUrl("https://" + randomAlphaOfLengthBetween(4, 8) + "." + randomAlphaOfLengthBetween(4, 8) + "/saml/acs");
        authnState.setEntityId("urn:" + randomAlphaOfLengthBetween(4, 8) + "." + randomAlphaOfLengthBetween(4, 8));
        authnState.setAuthnRequestId(null);
        authnState.setRequestedNameidFormat(null);
        return authnState;
    }

    private SamlAuthenticationState assertXContentRoundTrip(SamlAuthenticationState obj1) throws IOException {
        final XContentType xContentType = randomFrom(XContentType.values());
        final boolean humanReadable = randomBoolean();
        final BytesReference bytes1 = XContentHelper.toXContent(obj1, xContentType, humanReadable);
        try (XContentParser parser = XContentHelper.createParser(
            NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, bytes1, xContentType)) {
            final SamlAuthenticationState obj2 = SamlAuthenticationState.fromXContent(parser);
            assertThat(obj2, equalTo(obj1));

            final BytesReference bytes2 = XContentHelper.toXContent(obj2, xContentType, humanReadable);
            assertThat(bytes2, equalTo(bytes1));

            return obj2;
        }
    }

    private SamlAuthenticationState assertSerializationRoundTrip(SamlAuthenticationState obj1) throws IOException {
        final Version version = VersionUtils.randomVersionBetween(random(), Version.V_7_7_0, Version.CURRENT);
        return SerializationTestUtils.assertRoundTrip(obj1, SamlAuthenticationState::new, version);
    }
}
