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
package org.apache.commons.io.filefilter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractIOFileFilterTest {

    final class TesterFalseFileFilter extends FalseFileFilter {

        private static final long serialVersionUID = -3603047664010401872L;
        private boolean invoked;

        @Override
        public boolean accept(final File file) {
            setInvoked(true);
            return super.accept(file);
        }

        @Override
        public boolean accept(final File file, final String str) {
            setInvoked(true);
            return super.accept(file, str);
        }

        public boolean isInvoked() {
            return this.invoked;
        }

        public void reset() {
            setInvoked(false);
        }

        public void setInvoked(final boolean invoked) {
            this.invoked = invoked;
        }
    }

    final class TesterTrueFileFilter extends TrueFileFilter {

        private static final long serialVersionUID = 1828930358172422914L;
        private boolean invoked;

        @Override
        public boolean accept(final File file) {
            setInvoked(true);
            return super.accept(file);
        }

        @Override
        public boolean accept(final File file, final String str) {
            setInvoked(true);
            return super.accept(file, str);
        }

        public boolean isInvoked() {
            return this.invoked;
        }

        public void reset() {
            setInvoked(false);
        }

        public void setInvoked(final boolean invoked) {
            this.invoked = invoked;
        }
    }

    public static void assertFalseFiltersInvoked(final int testNumber, final TesterFalseFileFilter[] filters, final boolean[] invoked) {
        for (int i = 1; i < filters.length; i++) {
            assertEquals(invoked[i - 1], filters[i].isInvoked(), "test " + testNumber + " filter " + i + " invoked");
        }
    }

    public static void assertFileFiltering(final int testNumber, final IOFileFilter filter, final File file, final boolean expected) {
        assertEquals(expected, filter.accept(file),
                "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);
        assertEquals(expected, filter.matches(file.toPath()),
                "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);
    }

    public static void assertFilenameFiltering(final int testNumber, final IOFileFilter filter, final File file, final boolean expected) {
        // Assumes file has a parent and is not passed as null
        assertEquals(expected, filter.accept(file.getParentFile(), file.getName()),
                "test " + testNumber + " Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file);
    }

    public static void assertFiltering(final int testNumber, final IOFileFilter filter, final File file, final boolean expected) {
        // Note. This only tests the (File, String) version if the parent of
        //       the File passed in is not null
        assertEquals(expected, filter.accept(file),
            "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);
        assertEquals(expected, filter.accept(file.toPath(), null),
                "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);

        if (file.getParentFile() != null) {
            assertEquals(expected, filter.accept(file.getParentFile(), file.getName()),
                    "test " + testNumber + " Filter(File, String) " + filter.getClass().getName() + " not " + expected + " for " + file);
            assertEquals(expected, filter.matches(file.toPath()),
                    "test " + testNumber + " Filter(File) " + filter.getClass().getName() + " not " + expected + " for " + file);
        }
    }

    public static void assertTrueFiltersInvoked(final int testNumber, final TesterTrueFileFilter[] filters, final boolean[] invoked) {
        for (int i = 1; i < filters.length; i++) {
            assertEquals(invoked[i - 1], filters[i].isInvoked(), "test " + testNumber + " filter " + i + " invoked");
        }
    }

    public static File determineWorkingDirectoryPath(final String key, final String defaultPath) {
        // Look for a system property to specify the working directory
        final String workingPathName = System.getProperty(key, defaultPath);
        return new File(workingPathName);
    }

    public static void resetFalseFilters(final TesterFalseFileFilter[] filters) {
        Stream.of(filters).filter(Objects::nonNull).forEach(TesterFalseFileFilter::reset);
    }

    public static void resetTrueFilters(final TesterTrueFileFilter[] filters) {
        Stream.of(filters).filter(Objects::nonNull).forEach(TesterTrueFileFilter::reset);
    }
}
