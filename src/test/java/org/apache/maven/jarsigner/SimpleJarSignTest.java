/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.jarsigner;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.shared.utils.cli.javatool.JavaToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Olivier Lamy
 */
class SimpleJarSignTest extends AbstractJarSignerTest {

    private JarSigner jarSigner;

    @BeforeEach
    public void init()
    {
        jarSigner = new DefaultJarSigner();
    }


    @Test
    void checkSimpleSign() throws Exception {
        Path target = prepareTestJar("simple.jar");

        JarSignerSignRequest jarSignerRequest = new JarSignerSignRequest();
        jarSignerRequest.setArchive(target.toFile());
        jarSignerRequest.setKeystore(new File("src/test/keystore"));
        jarSignerRequest.setVerbose(true);
        jarSignerRequest.setAlias("foo_alias");
        jarSignerRequest.setKeypass("key-passwd");
        jarSignerRequest.setStorepass("changeit");
        jarSignerRequest.setSignedjar(new File("target/ssimple.jar"));

        JavaToolResult jarSignerResult = jarSigner.execute(jarSignerRequest);

        assertEquals(0, jarSignerResult.getExitCode(), "not exit code 0 " + jarSignerResult.getExecutionException());
    }

    @Test
    void simpleSignAndVerify() throws Exception {
        checkSimpleSign();

        JarSignerVerifyRequest request = new JarSignerVerifyRequest();
        request.setCerts(true);
        request.setVerbose(true);
        request.setArchive(Paths.get("target/ssimple.jar").toFile());
        request.setKeystore(new File("src/test/keystore"));
        request.setAlias("foo_alias");
        request.setStorepass("changeit");

        JavaToolResult jarSignerResult = jarSigner.execute(request);

        assertEquals(0, jarSignerResult.getExitCode(), "not exit code 0 " + jarSignerResult.getExecutionException());
    }
}
