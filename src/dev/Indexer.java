package dev;

import com.sun.istack.internal.NotNull;
import dev.utils.FileWrapper;
import dev.utils.IndexWrapper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: gtkachenko
 * Date: 08/03/15
 */
public class Indexer {
    public static final String CACHE_PATH = "index.ser";
    private static final String DATA_PATH = "data/";
    private static int curFileId = 0;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        File rootDir = new File(DATA_PATH);
        List<FileWrapper> files = buildXmlFileList(rootDir);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            Map<String, List<FileWrapper>> index = new HashMap<String, List<FileWrapper>>();
            int tokens = 0;
            DocumentBuilder builder = factory.newDocumentBuilder();
            for (FileWrapper fileWrapper : files) {
                tokens += fileWrapper.buildTokens(builder, index);
            }
            System.out.println("Tokens : " + tokens + "; Terms : " + index.size());
            serializeIndex(CACHE_PATH, index);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    private static List<FileWrapper> buildXmlFileList(@NotNull File file) {
        List<FileWrapper> resultList = new ArrayList<FileWrapper>();
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                resultList.addAll(buildXmlFileList(child));
            }
        } else {
            if (file.getName().endsWith("xml")) {
                resultList.add(new FileWrapper(file, curFileId++));
            }
        }
        return resultList;
    }

    public static void serializeIndex(String path, Map<String, List<FileWrapper>> index) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.close();
            fos.close();
            System.out.println("Serialized HashMap data is saved in " + path);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static IndexWrapper deserializeIndex(String path) {
        IndexWrapper indexWrapper = new IndexWrapper();
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            indexWrapper.setIndex((HashMap) ois.readObject());
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }
        return indexWrapper;
    }
}
