package org.greenvilleoaks.storage

class FileUtils {
    public static boolean createParentDirs(final String fileName) {
        File parentDir = new File(fileName).toPath().parent.toFile()
        return (parentDir.exists()) ? true : parentDir.mkdirs()
    }
}
