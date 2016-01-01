/*
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
package org.cryse.unifystorage.io.comparator;
import java.io.Serializable;
import java.util.Comparator;
import org.cryse.unifystorage.io.FilenameUtils;
import org.cryse.unifystorage.io.IOCase;
import org.cryse.unifystorage.AbstractFile;

/**
 * Compare the file name <b>extensions</b> for order
 * (see {@link FilenameUtils#getExtension(String)}).
 * <p>
 * This comparator can be used to sort lists or arrays of files
 * by their file extension either in a case-sensitive, case-insensitive or
 * system dependant case sensitive way. A number of singleton instances
 * are provided for the various case sensitivity options (using {@link IOCase})
 * and the reverse of those options.
 * <p>
 * Example of a <i>case-sensitive</i> file extension sort using the
 * {@link #EXTENSION_COMPARATOR} singleton instance:
 * <pre>
 *       List&lt;File&gt; list = ...
 *       Collections.sort(list, ExtensionFileComparator.EXTENSION_COMPARATOR);
 * </pre>
 * <p>
 * Example of a <i>reverse case-insensitive</i> file extension sort using the
 * {@link #EXTENSION_INSENSITIVE_REVERSE} singleton instance:
 * <pre>
 *       File[] array = ...
 *       Arrays.sort(array, ExtensionFileComparator.EXTENSION_INSENSITIVE_REVERSE);
 * </pre>
 * <p>
 *
 * @version $Revision: 609243 $ $Date: 2008-01-06 00:30:42 +0000 (Sun, 06 Jan 2008) $
 * @since Commons IO 1.4
 */
public class ExtensionFileComparator implements Comparator<AbstractFile>, Serializable {
    /** Case-sensitive extension comparator instance (see {@link IOCase#SENSITIVE}) */
    public static final Comparator<AbstractFile> EXTENSION_COMPARATOR = new ExtensionFileComparator();
    /** Reverse case-sensitive extension comparator instance (see {@link IOCase#SENSITIVE}) */
    public static final Comparator<AbstractFile> EXTENSION_REVERSE = new ReverseComparator<AbstractFile>(EXTENSION_COMPARATOR);
    /** Case-insensitive extension comparator instance (see {@link IOCase#INSENSITIVE}) */
    public static final Comparator<AbstractFile> EXTENSION_INSENSITIVE_COMPARATOR = new ExtensionFileComparator(IOCase.INSENSITIVE);
    /** Reverse case-insensitive extension comparator instance (see {@link IOCase#INSENSITIVE}) */
    public static final Comparator<AbstractFile> EXTENSION_INSENSITIVE_REVERSE
            = new ReverseComparator<AbstractFile>(EXTENSION_INSENSITIVE_COMPARATOR);
    /** System sensitive extension comparator instance (see {@link IOCase#SYSTEM}) */
    public static final Comparator<AbstractFile> EXTENSION_SYSTEM_COMPARATOR = new ExtensionFileComparator(IOCase.SYSTEM);
    /** Reverse system sensitive path comparator instance (see {@link IOCase#SYSTEM}) */
    public static final Comparator<AbstractFile> EXTENSION_SYSTEM_REVERSE = new ReverseComparator<AbstractFile>(EXTENSION_SYSTEM_COMPARATOR);
    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;
    /**
     * Construct a case sensitive file extension comparator instance.
     */
    public ExtensionFileComparator() {
        this.caseSensitivity = IOCase.SENSITIVE;
    }
    /**
     * Construct a file extension comparator instance with the specified case-sensitivity.
     *
     * @param caseSensitivity how to handle case sensitivity, null means case-sensitive
     */
    public ExtensionFileComparator(IOCase caseSensitivity) {
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }
    /**
     * Compare the extensions of two files the specified case sensitivity.
     *
     * @param file1 The first file to compare
     * @param file2 The second file to compare
     * @return a negative value if the first file's extension
     * is less than the second, zero if the extensions are the
     * same and a positive value if the first files extension
     * is greater than the second file.
     *
     */
    public int compare(AbstractFile file1, AbstractFile file2) {
        if (file1.isDirectory() && !file2.isDirectory())
            return -1;
        else if (!file1.isDirectory() && file2.isDirectory())
            return 1;
        else {
            String suffix1 = FilenameUtils.getExtension(file1.getName());
            String suffix2 = FilenameUtils.getExtension(file2.getName());
            return caseSensitivity.checkCompareTo(suffix1, suffix2);
        }
    }
}