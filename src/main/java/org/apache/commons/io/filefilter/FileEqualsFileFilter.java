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

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Accepts only an exact {@link File} object match. You can use this filter to visit the start directory when walking a
 * file tree with
 * {@link java.nio.file.Files#walkFileTree(java.nio.file.Path, java.util.Set, int, java.nio.file.FileVisitor)}.
 *
 * @since 2.9.0
 */
public class FileEqualsFileFilter extends AbstractFileFilter {

    private final File file;
    private final Path path;

    /**
     * Constructs a new instance for the given {@link File}.
     *
     * @param file The file to match.
     */
    public FileEqualsFileFilter(final File file) {
        this.file = Objects.requireNonNull(file, "file");
        this.path = file.toPath();
    }

    @Override
    public boolean accept(final File file) {
        return Objects.equals(this.file, file);
    }

    @Override
    public FileVisitResult accept(final Path path, final BasicFileAttributes attributes) {
        return toFileVisitResult(Objects.equals(this.path, path));
    }
}
