package org.xpertss.jarsigner;

import org.junit.jupiter.api.Test;
import org.xpertss.crypto.pkcs.pkcs7.ContentInfo;
import org.xpertss.crypto.pkcs.pkcs7.SignedData;
import org.xpertss.crypto.pkcs.tsp.TSTokenInfo;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class TsaSignerTest {


    private static final byte[] SIGNATURE = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
            0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F
    };



    @Test
    public void testToStringFull() throws Exception
    {
        TsaSigner signer = TsaSigner.Builder.of(URI.create("https://tsa.openworld.com/tsa"))
                                    .policyId("0.0").digestAlgorithm("SHA-256").build();
        assertEquals("[uri=https://tsa.openworld.com/tsa, policyId=0.0, digest=SHA-256]", signer.toString());
    }

    @Test
    public void testToStringShort() throws Exception
    {
        TsaSigner signer = TsaSigner.Builder.of(URI.create("https://tsa.openworld.com/tsa")).build();
        assertEquals("[uri=https://tsa.openworld.com/tsa]", signer.toString());
    }

    @Test
    public void testFull() throws Exception
    {
        TsaSigner signer = TsaSigner.Builder.of(URI.create("http://timestamp.digicert.com"))
                                        .policyId("2.16.840.1.114412.7.1")
                                        .digestAlgorithm("SHA-384").build();

        ContentInfo ts = signer.stamp(SIGNATURE);
        assertEquals("1.2.840.113549.1.7.2", ts.getContentType().toString());
        SignedData signedData = (SignedData) ts.getContent();
        assertEquals("1.2.840.113549.1.9.16.1.4", signedData.getContentType().toString());
        TSTokenInfo tstInfo = (TSTokenInfo) signedData.getContent();
        assertNotNull(tstInfo);

    }

}