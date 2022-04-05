/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.helpers;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.file.Path;

/**
 * A set of utility functions for interacting with file paths.
 */
public class PathHelper {

    public static boolean isInSearchPaths(Path root, Path path, String... searchPaths) {
        if (ArrayUtils.isEmpty(searchPaths)) {
            return true;
        }
        Path prefix = root.relativize(path).getParent();
        return prefix != null && startsWith(prefix.toString(), searchPaths);
    }

    public static boolean startsWith(String path, String... searchPaths) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        for (String searchPath : searchPaths) {
            if (path.startsWith(searchPath)) {
                return true;
            }
        }

        return false;
    }

}
