package dev.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: gtkachenko
 * Date: 08/03/15
 */
public class FileWrapper extends File {
    private final int id;

    public FileWrapper(File file, int id) {
        super(file.getPath());
        this.id = id;
    }

    public int buildTokens(DocumentBuilder builder, Map<String, List<FileWrapper>> index) {
        int numTokens = 0;
        try {
            Document document = builder.parse(this);
            NodeList nodeList = document.getDocumentElement().getElementsByTagName("p");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node curNode = nodeList.item(i);
                for (String curToken : curNode.getTextContent().split("[^\\w]+")) {
                    curToken = curToken.toLowerCase();
                    if (curToken.length() == 0) {
                        continue;
                    }
                    if (index.get(curToken) == null) {
                        index.put(curToken, new ArrayList<FileWrapper>());
                    }
                    int size = index.get(curToken).size();
                    if (size == 0 || index.get(curToken).get(size - 1).getId() != getId()) {
                        index.get(curToken).add(this);
                    }
                    numTokens++;
                }

            }
            builder.reset();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numTokens;
    }

    public List<String> getSnippets(DocumentBuilder builder) {
        try {
            Document document = builder.parse(this);
            NodeList nodeList = document.getDocumentElement().getElementsByTagName("p");
            List<String> result = new ArrayList<String>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node curNode = nodeList.item(i);
                result.add(curNode.getTextContent());
            }
            builder.reset();
            return result;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getId() {
        return id;
    }
}