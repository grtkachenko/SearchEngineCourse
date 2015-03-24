package dev;

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

    private static String DEFAULT_REQUEST = "NOT relevant AND Caesar OR immediately";
    private static Map<String, List<FileWrapper>> index;
    private static int totalNumberOfFiles;

    public static void main(String[] args) throws ParserConfigurationException {
        if (args.length == 0) {
            System.out.println("Searcher : not enough arguments");
            return;
        }
        System.out.println("Deserializing objects...");
        IndexWrapper indexWrapper = Indexer.deserializeIndex(Indexer.CACHE_PATH);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        index = indexWrapper.getIndex();
        totalNumberOfFiles = indexWrapper.getFiles().size();
        DocumentResult result = getDocuments(args[0]);
        if (result.fileWrappers == null) {
            System.out.println("Nothing matched");
            return;
        }
        if (result.needInverse) {
            result.fileWrappers = subLists(indexWrapper.getFiles(), result.fileWrappers);
        }
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        System.out.println("\033[0;1mMatched files : " + result.fileWrappers.size() + "\033[0;0m\n");

        for (FileWrapper fileWrapper : result.fileWrappers) {
            if (result.okTokens.isEmpty()) {
                break;
            }
            boolean fileNamePrinted = false;
            for (String curLine : fileWrapper.getSnippets(builder)) {
                Iterator<String> it = result.okTokens.iterator();
                while (it.hasNext()) {
                    String curToken = it.next().toLowerCase();
                    for (String curLineToken : curLine.split("[^\\w]+")) {
                        if (curLineToken.toLowerCase().equals(curToken)) {
                            if (!fileNamePrinted) {
                                System.out.println("\033[0;33m" + fileWrapper.getAbsolutePath() + "\033[0;0m");
                                fileNamePrinted = true;
                            }
                            int index = curLine.toLowerCase().indexOf(curToken);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(curLine.substring(0, index));
                            stringBuilder.append("\033[0;4m");
                            stringBuilder.append("\033[0;32m");
                            stringBuilder.append(curLine.substring(index, index + curToken.length()));
                            stringBuilder.append("\033[0;0m");
                            stringBuilder.append(curLine.substring(index + curToken.length()));
                            System.out.println(stringBuilder.toString());
                            it.remove();
                            break;
                        }
                    }
                }
            }
            if (fileNamePrinted) {
                System.out.println();
            }
        }
    }

    private static class DocumentResult {
        private List<FileWrapper> fileWrappers;
        private boolean needInverse;
        private Set<String> okTokens;
        private Set<String> badTokens;

        public DocumentResult(List<FileWrapper> fileWrappers, boolean needInverse, Set<String> okTokens, Set<String> badTokens) {
            this.fileWrappers = fileWrappers;
            this.needInverse = needInverse;
            this.okTokens = okTokens;
            this.badTokens = badTokens;
        }
    }

    private static DocumentResult checkIfEmptyResult(DocumentResult result) {
        if (result.fileWrappers.size() == 0 && !result.needInverse ||
                result.fileWrappers.size() == totalNumberOfFiles && result.needInverse) {
            // Nothing matched; clear all tokens
            result.okTokens.clear();
            result.badTokens.clear();
        }
        return result;
    }

    public static DocumentResult getDocuments(String input) {
        input = input.trim();
        int balance = 0;
        // Handling OR
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
                        DocumentResult resLeft = getDocuments(input.substring(0, i));
                        DocumentResult resRight = getDocuments(input.substring(i + 2));
                        resLeft.okTokens.addAll(resRight.okTokens);
                        resLeft.badTokens.addAll(resRight.badTokens);
                        resRight.okTokens = resLeft.okTokens;
                        resRight.badTokens = resLeft.badTokens;

                        if (resLeft.needInverse ^ resRight.needInverse) {
                            if (resLeft.needInverse) {
                                DocumentResult tmp = resLeft;
                                resLeft = resRight;
                                resRight = tmp;
                            }
                            return checkIfEmptyResult(new DocumentResult(subLists(resRight.fileWrappers, resLeft.fileWrappers), true,
                                    resLeft.okTokens, resLeft.badTokens));
                        } else {
                            return checkIfEmptyResult(new DocumentResult(mergeLists(resLeft.fileWrappers, resRight.fileWrappers), resLeft.needInverse,
                                    resLeft.okTokens, resLeft.badTokens));
                        }
                    }
                    break;

            }
        }
        balance = 0;
        // Handling AND
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
                        DocumentResult resLeft = getDocuments(input.substring(0, i));
                        DocumentResult resRight = getDocuments(input.substring(i + 3));
                        resLeft.okTokens.addAll(resRight.okTokens);
                        resLeft.badTokens.addAll(resRight.badTokens);
                        resRight.okTokens = resLeft.okTokens;
                        resRight.badTokens = resLeft.badTokens;

                        if (resLeft.needInverse ^ resRight.needInverse) {
                            if (resLeft.needInverse) {
                                DocumentResult tmp = resLeft;
                                resLeft = resRight;
                                resRight = tmp;
                            }
                            return checkIfEmptyResult(new DocumentResult(subLists(resLeft.fileWrappers, resRight.fileWrappers), false,
                                    resLeft.okTokens, resLeft.badTokens));
                        } else {
                            return checkIfEmptyResult(new DocumentResult(intersectLists(resLeft.fileWrappers, resRight.fileWrappers),
                                    resLeft.needInverse, resLeft.okTokens, resLeft.badTokens));
                        }
                    }
                    break;
            }
        }

        // Handling NOT
        if (input.length() >= 3 && "NOT".equals(input.substring(0, 3))) {
            DocumentResult result = getDocuments(input.substring(3));
            return checkIfEmptyResult(new DocumentResult(result.fileWrappers, !result.needInverse, result.badTokens, result.okTokens));
        }

        // Handling Brackets and Terms
        if (input.charAt(0) != '(') {
            Set<String> okTokens = new TreeSet<String>();
            Set<String> badTokens = new TreeSet<String>();
            okTokens.add(input.toLowerCase());
            if (index.get(input.toLowerCase()) == null) {
                return checkIfEmptyResult(new DocumentResult(new ArrayList<FileWrapper>(), false, okTokens, badTokens));
            }
            return checkIfEmptyResult(new DocumentResult(index.get(input.toLowerCase()), false, okTokens, badTokens));
        } else {
            return checkIfEmptyResult(getDocuments(input.substring(1, input.length() - 1)));
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
                if (first.get(pointerLeft).getId() == second.get(pointerRight).getId()) {
                    pointerLeft++;
                }
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
            if (pointerRight >= second.size() || pointerRight < second.size() && second.get(pointerRight).getId() != first.get(i).getId()) {
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
