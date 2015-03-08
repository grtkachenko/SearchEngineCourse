package dev;

import com.sun.tools.javac.util.Pair;
import dev.utils.FileWrapper;
import dev.utils.IndexWrapper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

/**
 * User: gtkachenko
 * Date: 08/03/15
 */
public class Searcher {

    private static final String REQUEST = "NOT are AND do OR Caesar";
    private static Map<String, List<FileWrapper>> index;
    private static Set<String> tokens = new TreeSet<String>();


    public static void main(String[] args) throws ParserConfigurationException {
        IndexWrapper indexWrapper = Indexer.deserializeIndex(Indexer.CACHE_PATH);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        index = indexWrapper.getIndex();

        Pair<List<FileWrapper>, Boolean> result = getDocuments(REQUEST);
        if (result.snd) {
            System.out.println("Too many results");
        } else {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (result.fst == null) {
                System.out.println("Nothing matched");
                return;
            }
            System.out.println("Matched files : " + result.fst.size());

            for (FileWrapper fileWrapper : result.fst) {
                if (tokens.isEmpty()) {
                    break;
                }
                boolean fileNamePrinted = false;
                for (String curLine : fileWrapper.getSnippets(builder)) {
                    Iterator<String> it = tokens.iterator();
                    while (it.hasNext()) {
                        String curToken = it.next();
                        if (curLine.contains(curToken)) {
                            if (!fileNamePrinted) {
                                System.out.println(fileWrapper.getAbsolutePath());
                                fileNamePrinted = true;
                            }
                            System.out.println(curLine);
                            it.remove();
                        }
                    }
                }
                if (fileNamePrinted) {
                    System.out.println();
                }
            }
        }
    }

    public static Pair<List<FileWrapper>, Boolean> getDocuments(String input) {
        input = input.trim();
        int balance = 0;
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '(':
                    balance++;
                    break;
                case ')':
                    balance--;
                    break;
                default:
                    if (balance == 0 && input.charAt(i) == 'O' && i + 1 < input.length() && input.charAt(i + 1) == 'R') {
                        Pair<List<FileWrapper>, Boolean> resLeft = getDocuments(input.substring(0, i));
                        Pair<List<FileWrapper>, Boolean> resRight = getDocuments(input.substring(i + 2));
                        if (resLeft.snd ^ resRight.snd) {
                            if (resLeft.snd) {
                                Pair<List<FileWrapper>, Boolean> tmp = resLeft;
                                resLeft = resRight;
                                resRight = tmp;
                            }
                            return new Pair<List<FileWrapper>, Boolean>(subLists(resRight.fst, resLeft.fst), true);
                        } else {
                            return new Pair<List<FileWrapper>, Boolean>(mergeLists(resLeft.fst, resRight.fst), resLeft.snd);
                        }
                    }
                    break;

            }
        }
        balance = 0;
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '(':
                    balance++;
                    break;
                case ')':
                    balance--;
                    break;
                default:
                    if (balance == 0 && input.charAt(i) == 'A' && i + 2 < input.length() && input.charAt(i + 1) == 'N' && input.charAt(i + 2) == 'D') {
                        Pair<List<FileWrapper>, Boolean> resLeft = getDocuments(input.substring(0, i));
                        Pair<List<FileWrapper>, Boolean> resRight = getDocuments(input.substring(i + 3));
                        if (resLeft.snd ^ resRight.snd) {
                            if (resLeft.snd) {
                                Pair<List<FileWrapper>, Boolean> tmp = resLeft;
                                resLeft = resRight;
                                resRight = tmp;
                            }
                            return new Pair<List<FileWrapper>, Boolean>(subLists(resLeft.fst, resRight.fst), false);
                        } else {
                            return new Pair<List<FileWrapper>, Boolean>(intersectLists(resLeft.fst, resRight.fst), resLeft.snd);
                        }
                    }
                    break;
            }
        }

        if (input.length() >= 3 && "NOT".equals(input.substring(0, 3))) {
            Pair<List<FileWrapper>, Boolean> result = getDocuments(input.substring(3));
            return new Pair<List<FileWrapper>, Boolean>(result.fst, !result.snd);
        }
        if (input.charAt(0) != '(') {
            tokens.add(input);
            if (index.get(input.toLowerCase()) == null) {
                return new Pair<List<FileWrapper>, Boolean>(new ArrayList<FileWrapper>(), false);
            }
            return new Pair<List<FileWrapper>, Boolean>(index.get(input.toLowerCase()), false);
        } else {
            return getDocuments(input.substring(1, input.length() - 1));
        }
    }

    private static List<FileWrapper> mergeLists(List<FileWrapper> first, List<FileWrapper> second) {
        int pointerLeft = 0;
        int pointerRight = 0;
        List<FileWrapper> resultList = new ArrayList<FileWrapper>();
        while (pointerLeft < first.size() && pointerRight < second.size()) {
            if (first.get(pointerLeft).getId() < second.get(pointerRight).getId()) {
                resultList.add(first.get(pointerLeft++));
            } else {
                resultList.add(second.get(pointerRight++));
            }
        }

        if (pointerLeft < first.size()) {
            for (int i = pointerLeft; i < first.size(); i++) {
                resultList.add(first.get(i));
            }
        } else {
            for (int i = pointerRight; i < second.size(); i++) {
                resultList.add(second.get(i));
            }
        }
        return resultList;
    }

    private static List<FileWrapper> subLists(List<FileWrapper> first, List<FileWrapper> second) {
        List<FileWrapper> resultList = new ArrayList<FileWrapper>();
        int pointerRight = 0;
        for (int i = 0; i < first.size(); i++) {
            while (pointerRight < second.size() && second.get(pointerRight).getId() < first.get(i).getId()) {
                pointerRight++;
            }
            if (pointerRight < second.size() && second.get(pointerRight).getId() != first.get(i).getId()) {
                resultList.add(first.get(i));
            }
        }
        return resultList;
    }

    private static List<FileWrapper> intersectLists(List<FileWrapper> first, List<FileWrapper> second) {
        List<FileWrapper> resultList = new ArrayList<FileWrapper>();
        int pointerRight = 0;
        for (int i = 0; i < first.size(); i++) {
            while (pointerRight < second.size() && second.get(pointerRight).getId() < first.get(i).getId()) {
                pointerRight++;
            }
            if (pointerRight < second.size() && second.get(pointerRight).getId() == first.get(i).getId()) {
                resultList.add(first.get(i));
            }
        }
        return resultList;
    }

}
