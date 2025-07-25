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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ChunkedWriter}.
 */
class ChunkedWriterTest {

    @SuppressWarnings("resource") // closed by caller.
    private OutputStreamWriter getOutputStreamWriter(final AtomicInteger numWrites) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new OutputStreamWriter(baos) {
            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
                numWrites.incrementAndGet();
                super.write(cbuf, off, len);
            }
        };
    }

    @Test
    void testNegative_chunkSize_not_permitted() {
        assertThrows(IllegalArgumentException.class,
               () -> new ChunkedWriter(new OutputStreamWriter(new ByteArrayOutputStream()), 0));
    }

    @Test
    void testWrite_four_chunks() throws Exception {
        final AtomicInteger numWrites = new AtomicInteger();
        try (OutputStreamWriter osw = getOutputStreamWriter(numWrites)) {
            try (ChunkedWriter chunked = new ChunkedWriter(osw, 10)) {
                chunked.write("0123456789012345678901234567891".toCharArray());
                chunked.flush();
                assertEquals(4, numWrites.get());
            }
        }
    }

    @Test
    void testWrite_two_chunks_default_constructor() throws Exception {
        final AtomicInteger numWrites = new AtomicInteger();
        try (OutputStreamWriter osw = getOutputStreamWriter(numWrites)) {
            try (ChunkedWriter chunked = new ChunkedWriter(osw)) {
                chunked.write(new char[IOUtils.DEFAULT_BUFFER_SIZE + 1]);
                chunked.flush();
                assertEquals(2, numWrites.get());
            }
        }
    }
}
