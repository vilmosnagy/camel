/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl;
import org.junit.Before;

import org.junit.Test;

import java.io.File;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;

/**
 * @version 
 */
public class GzipDataFormatFileUnmarshalDeleteTest extends ContextTestSupport {

    @Override
    @Before
    public void setUp() throws Exception {
        deleteDirectory("target/gzip");
        super.setUp();
    }

    @Test
    public void testGzipFileUnmarshalDelete() throws Exception {
        NotifyBuilder notify = new NotifyBuilder(context).whenDone(2).create();

        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");

        template.sendBodyAndHeader("file:target/gzip", "Hello World", Exchange.FILE_NAME, "hello.txt");

        assertMockEndpointsSatisfied();
        notify.matchesMockWaitTime();

        File in = new File("target/gzip/hello.txt");
        assertFalse("Should have been deleted " + in, in.exists());

        File out = new File("target/gzip/out/hello.txt.gz");
        assertFalse("Should have been deleted " + out, out.exists());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:target/gzip?initialDelay=0&delay=10&delete=true")
                    .marshal().gzip()
                    .to("file:target/gzip/out?fileName=${file:name}.gz");

                from("file:target/gzip/out?initialDelay=0&delay=10&delete=true")
                    .unmarshal().gzip()
                    .to("mock:result");
            }
        };
    }
}