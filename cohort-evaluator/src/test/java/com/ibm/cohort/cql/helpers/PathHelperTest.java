/*
 * (C) Copyright IBM Corp. 2022, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathHelperTest {

    @Test
    public void isInSearchPaths() {
        String searchPath = "subpath";
        Path root = Paths.get("/path/to/directory");
        Path path = root.resolve(searchPath).resolve("filename.txt");
        Assert.assertTrue(PathHelper.isInSearchPaths(root, path, searchPath));
    }

    @Test
    public void isInSearchPaths_relativeDirs() {
        String searchPath = "subpath";
        Path root = Paths.get("path/to/directory");
        Path path = root.resolve(searchPath).resolve("filename.txt");
        Assert.assertTrue(PathHelper.isInSearchPaths(root, path, searchPath));
    }

    @Test
    public void isInSearchPaths_noFilename() {
        String searchPath = "subpath";
        Path root = Paths.get("/path");
        Path path = root.resolve(searchPath);
        Assert.assertFalse(PathHelper.isInSearchPaths(root, path, searchPath));
    }

    @Test
    public void isInSearchPaths_multipleSearchPaths() {
        String searchPath = "subpath";
        Path root = Paths.get("/path/to/directory");
        Path path = root.resolve(searchPath).resolve("filename.txt");
        Assert.assertTrue(PathHelper.isInSearchPaths(
                root,
                path,
                "firstPath",
                "secondPath",
                searchPath,
                "thirdPath"
        ));
    }

    @Test
    public void isInSearchPaths_emptySearchPaths() {
        Path root = Paths.get("/path/to/directory");
        Path path = root.resolve("subpath/filename.txt");
        Assert.assertTrue(PathHelper.isInSearchPaths(root, path));
    }

    @Test
    public void isInSearchPaths_notInSearchPaths() {
        String searchPath = "subpath";
        Path root = Paths.get("/path/to/directory");
        Path path = root.resolve("notsubpath/filename.txt");
        Assert.assertFalse(PathHelper.isInSearchPaths(root, path, searchPath));
    }

    @Test
    public void isInSearchPaths_notInSearchPaths_multipleSearchPaths() {
        String searchPath = "subpath";
        Path root = Paths.get("/path/to/directory");
        Path path = root.resolve("notsubpath/filename.txt");
        Assert.assertFalse(PathHelper.isInSearchPaths(
                root,
                path,
                "firstPath",
                "secondPath",
                searchPath,
                "thirdPath"
        ));
    }

    @Test
    public void isInSearchPaths_differentRoot() {
        String searchPath = "subpath";
        Path root = Paths.get("/path/to/directory");
        Path path = root.resolve("/not/root/filename.txt");
        Assert.assertFalse(PathHelper.isInSearchPaths(root, path, searchPath));
    }

    @Test
    public void isInSearchPaths_differentRoot_relativeDirs() {
        String searchPath = "subpath";
        Path root = Paths.get("root");
        Path path = root.resolve("notroot/filename.txt");
        Assert.assertFalse(PathHelper.isInSearchPaths(root, path, searchPath));
    }

    @Test
    public void isInSearchPaths_differentRoot_relativeDirs_noFilename() {
        String searchPath = "subpath";
        Path root = Paths.get("root");
        Path path = root.resolve("notroot");
        Assert.assertFalse(PathHelper.isInSearchPaths(root, path, searchPath));
    }

}
