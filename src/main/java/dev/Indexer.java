package dev;

import dev.utils.FileWrapper;
import dev.utils.IndexWrapper;
import org.jetbrains.annotations.NotNull;

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
        if (args.length == 0) {
            System.out.println("Indexer : not enough arguments");
            return;
        }
        File rootDir = new File(args[0]);
        List<FileWrapper> files = buildXmlFileList(rootDir);
        System.out.println("Files to be processed : " + files.size());

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
            System.out.println("Serializing objects...");
            serializeIndex(CACHE_PATH, index, files);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
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

    public static void serializeIndex(String path, Map<String, List<FileWrapper>> index, List<FileWrapper> files) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.writeObject(files);
            oos.close();
            fos.close();
            System.out.println("Serialized data is saved in " + path);
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
            indexWrapper.setFiles((List) ois.readObject());
            ois.close();
            fis.close();
            System.out.println("Done deserializing");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }
        return indexWrapper;
    }
}
