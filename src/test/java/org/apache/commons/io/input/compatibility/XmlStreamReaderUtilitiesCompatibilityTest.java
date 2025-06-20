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
package org.apache.commons.io.input.compatibility;

import java.io.IOException;

import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.input.XmlStreamReaderUtilitiesTest;

/**
 * Test compatibility of the original XmlStreamReader (before all the refactoring).
 */
class XmlStreamReaderUtilitiesCompatibilityTest extends XmlStreamReaderUtilitiesTest {

    /** Mock {@link XmlStreamReader} implementation */
    private static final class MockXmlStreamReader extends XmlStreamReader {
        MockXmlStreamReader(final String defaultEncoding) throws IOException {
            super(CharSequenceInputStream.builder().setCharSequence("").get(), null, true, defaultEncoding);
        }
    }
    @Override
    protected String calculateHttpEncoding(final String httpContentType, final String bomEnc, final String xmlGuessEnc,
            final String xmlEnc, final boolean lenient, final String defaultEncoding) throws IOException {
        try (MockXmlStreamReader mock = new MockXmlStreamReader(defaultEncoding)) {
                return mock.calculateHttpEncoding(
                        XmlStreamReader.getContentTypeMime(httpContentType),
                        XmlStreamReader.getContentTypeEncoding(httpContentType),
                        bomEnc, xmlGuessEnc, xmlEnc, null, lenient);
        }
    }

    @Override
    protected String calculateRawEncoding(final String bomEnc, final String xmlGuessEnc, final String xmlEnc,
            final String defaultEncoding) throws IOException {
        try (MockXmlStreamReader mock = new MockXmlStreamReader(defaultEncoding)) {
            return mock.calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc, null);
        }
    }
}
