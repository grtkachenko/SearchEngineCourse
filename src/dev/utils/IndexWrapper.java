package dev.utils;

import java.util.List;
import java.util.Map;

/**
 * User: gtkachenko
 * Date: 08/03/15
 */
public class IndexWrapper {
    private Map<String, List<FileWrapper>> index;
    private List<FileWrapper> files;

    public IndexWrapper() {
    }

    public void setIndex(Map<String, List<FileWrapper>> index) {
        this.index = index;
    }

    public Map<String, List<FileWrapper>> getIndex() {
        return index;
    }

    public List<FileWrapper> getFiles() {
        return files;
    }

    public void setFiles(List<FileWrapper> files) {
        this.files = files;
    }
}
