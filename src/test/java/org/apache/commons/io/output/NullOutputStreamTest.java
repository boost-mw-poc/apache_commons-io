/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.output;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link NullOutputStream}.
 */
class NullOutputStreamTest {

    private void process(final NullOutputStream nos) throws IOException {
        nos.write("string".getBytes());
        nos.write("some string".getBytes(), 3, 5);
        nos.write(1);
        nos.write(0x0f);
        nos.flush();
        nos.close();
        nos.write("allowed".getBytes());
        nos.write(255);
    }

    @Test
    void testNewInstance() throws IOException {
        try (NullOutputStream nos = NullOutputStream.INSTANCE) {
            process(nos);
        }
    }

    @Test
    void testSingleton() throws IOException {
        try (NullOutputStream nos = NullOutputStream.INSTANCE) {
            process(nos);
        }
    }

}
