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
package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link LineIterator}.
 */
class LineIteratorTest {

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    @TempDir
    public File temporaryFolder;

    private void assertLines(final List<String> lines, final LineIterator iterator) {
        try {
            for (int i = 0; i < lines.size(); i++) {
                final String line = iterator.nextLine();
                assertEquals(lines.get(i), line, "nextLine() line " + i);
            }
            assertFalse(iterator.hasNext(), "No more expected");
        } finally {
            IOUtils.closeQuietly(iterator);
        }
    }

    /**
     * Creates a test file with a specified number of lines.
     *
     * @param file target file
     * @param lineCount number of lines to create
     * @throws IOException If an I/O error occurs
     */
    private List<String> createLinesFile(final File file, final int lineCount) throws IOException {
        final List<String> lines = createStringLines(lineCount);
        FileUtils.writeLines(file, lines);
        return lines;
    }

    /**
     * Creates a test file with a specified number of lines.
     *
     * @param file target file
     * @param encoding the encoding to use while writing the lines
     * @param lineCount number of lines to create
     * @throws IOException If an I/O error occurs
     */
    private List<String> createLinesFile(final File file, final String encoding, final int lineCount) throws IOException {
        final List<String> lines = createStringLines(lineCount);
        FileUtils.writeLines(file, encoding, lines);
        return lines;
    }

    /**
     * Creates String data lines.
     *
     * @param lineCount number of lines to create
     * @return a new lines list.
     */
    private List<String> createStringLines(final int lineCount) {
        final List<String> lines = new ArrayList<>();
        for (int i = 0; i < lineCount; i++) {
            lines.add("LINE " + i);
        }
        return lines;
    }

    /**
     * Utility method to create and test a file with a specified number of lines.
     *
     * @param lineCount the lines to create in the test file
     * @throws IOException If an I/O error occurs while creating the file
     */
    private void doTestFileWithSpecifiedLines(final int lineCount) throws IOException {
        final String encoding = UTF_8;

        final String fileName = "LineIterator-" + lineCount + "-test.txt";
        final File testFile = new File(temporaryFolder, fileName);
        final List<String> lines = createLinesFile(testFile, encoding, lineCount);

        try (LineIterator iterator = FileUtils.lineIterator(testFile, encoding)) {
            assertThrows(UnsupportedOperationException.class, iterator::remove);

            int idx = 0;
            while (iterator.hasNext()) {
                final String line = iterator.next();
                assertEquals(lines.get(idx), line, "Comparing line " + idx);
                assertTrue(idx < lines.size(), "Exceeded expected idx=" + idx + " size=" + lines.size());
                idx++;
            }
            assertEquals(idx, lines.size(), "Line Count doesn't match");

            // try calling next() after file processed
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThrows(NoSuchElementException.class, iterator::nextLine);
        }
    }

    @Test
    void testCloseEarly() throws Exception {
        final String encoding = UTF_8;

        final File testFile = new File(temporaryFolder, "LineIterator-closeEarly.txt");
        createLinesFile(testFile, encoding, 3);

        try (LineIterator iterator = FileUtils.lineIterator(testFile, encoding)) {
            // get
            assertNotNull("Line expected", iterator.next());
            assertTrue(iterator.hasNext(), "More expected");

            // close
            iterator.close();
            assertFalse(iterator.hasNext(), "No more expected");
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThrows(NoSuchElementException.class, iterator::nextLine);
            // try closing again
            iterator.close();
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThrows(NoSuchElementException.class, iterator::nextLine);
        }
    }

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new LineIterator(null));
    }

    private void testFiltering(final List<String> lines, final Reader reader) throws IOException {
        try (LineIterator iterator = new LineIterator(reader) {
            @Override
            protected boolean isValidLine(final String line) {
                final char c = line.charAt(line.length() - 1);
                return (c - 48) % 3 != 1;
            }
        }) {
            assertThrows(UnsupportedOperationException.class, iterator::remove);

            int idx = 0;
            int actualLines = 0;
            while (iterator.hasNext()) {
                final String line = iterator.next();
                actualLines++;
                assertEquals(lines.get(idx), line, "Comparing line " + idx);
                assertTrue(idx < lines.size(), "Exceeded expected idx=" + idx + " size=" + lines.size());
                idx++;
                if (idx % 3 == 1) {
                    idx++;
                }
            }
            assertEquals(9, lines.size(), "Line Count doesn't match");
            assertEquals(9, idx, "Line Count doesn't match");
            assertEquals(6, actualLines, "Line Count doesn't match");

            // try calling next() after file processed
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThrows(NoSuchElementException.class, iterator::nextLine);
        }
    }

    @Test
    void testFilteringBufferedReader() throws Exception {
        final String encoding = UTF_8;

        final String fileName = "LineIterator-Filter-test.txt";
        final File testFile = new File(temporaryFolder, fileName);
        final List<String> lines = createLinesFile(testFile, encoding, 9);

        final Reader reader = new BufferedReader(Files.newBufferedReader(testFile.toPath()));
        testFiltering(lines, reader);
    }

    @Test
    void testFilteringFileReader() throws Exception {
        final String encoding = UTF_8;

        final String fileName = "LineIterator-Filter-test.txt";
        final File testFile = new File(temporaryFolder, fileName);
        final List<String> lines = createLinesFile(testFile, encoding, 9);

        final Reader reader = Files.newBufferedReader(testFile.toPath());
        testFiltering(lines, reader);
    }

    @Test
    void testInvalidEncoding() throws Exception {
        final String encoding = "XXXXXXXX";

        final File testFile = new File(temporaryFolder, "LineIterator-invalidEncoding.txt");
        createLinesFile(testFile, UTF_8, 3);

        assertThrows(UnsupportedCharsetException.class, () -> FileUtils.lineIterator(testFile, encoding));
    }

    @Test
    void testMissingFile() throws Exception {
        final File testFile = new File(temporaryFolder, "dummy-missing-file.txt");
        assertThrows(NoSuchFileException.class, () -> FileUtils.lineIterator(testFile, UTF_8));
    }

    @Test
    void testNextLineOnlyDefaultEncoding() throws Exception {
        final File testFile = new File(temporaryFolder, "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile);
        assertLines(lines, iterator);
    }

    @Test
    void testNextLineOnlyNullEncoding() throws Exception {
        final String encoding = null;

        final File testFile = new File(temporaryFolder, "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        assertLines(lines, iterator);
    }

    @Test
    void testNextLineOnlyUtf8Encoding() throws Exception {
        final String encoding = UTF_8;

        final File testFile = new File(temporaryFolder, "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, encoding, 3);

        final LineIterator iterator = FileUtils.lineIterator(testFile, encoding);
        assertLines(lines, iterator);
    }

    @Test
    void testNextOnly() throws Exception {
        final String encoding = null;

        final File testFile = new File(temporaryFolder, "LineIterator-nextOnly.txt");
        final List<String> lines = createLinesFile(testFile, encoding, 3);

        try (LineIterator iterator = FileUtils.lineIterator(testFile, encoding)) {
            for (int i = 0; i < lines.size(); i++) {
                final String line = iterator.next();
                assertEquals(lines.get(i), line, "next() line " + i);
            }
            assertFalse(iterator.hasNext(), "No more expected");
        }
    }

    @Test
    void testNextWithException() throws Exception {
        final Reader reader = new BufferedReader(new StringReader("")) {
            @Override
            public String readLine() throws IOException {
                throw new IOException("hasNext");
            }
        };
        try (LineIterator li = new LineIterator(reader)) {
            assertThrows(IllegalStateException.class, li::hasNext);
        }
    }

    @Test
    void testOneLines() throws Exception {
        doTestFileWithSpecifiedLines(1);
    }

    @Test
    void testThreeLines() throws Exception {
        doTestFileWithSpecifiedLines(3);
    }

    @Test
    void testTwoLines() throws Exception {
        doTestFileWithSpecifiedLines(2);
    }

    @Test
    void testValidEncoding() throws Exception {
        final String encoding = UTF_8;

        final File testFile = new File(temporaryFolder, "LineIterator-validEncoding.txt");
        createLinesFile(testFile, encoding, 3);

        try (LineIterator iterator = FileUtils.lineIterator(testFile, encoding)) {
            int count = 0;
            while (iterator.hasNext()) {
                assertNotNull(iterator.next());
                count++;
            }
            assertEquals(3, count);
        }
    }

    @Test
    void testZeroLines() throws Exception {
        doTestFileWithSpecifiedLines(0);
    }

}
