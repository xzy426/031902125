import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Demo {
    //关键词的list
    static ArrayList<String> phaseList = null;
    static Map<String, List<Pinyin>> wordslist = null;
    static String wordsFile = null;
    static String testFile = null;
    static String outputFile = null;
    static String finalString = "";
    public static void main(String[] args) {
        if (args.length >= 3) {
            wordsFile = args[0];
            testFile = args[1];
            outputFile = args[2];
        } else {
            System.out.println("输入有误");
            return;
        }
        readWords();
        try {
            read();
        } catch (Exception e) {
            System.out.println("找不到敏感词文件");
            return;
        }
    }
    public static void read() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String contentLine;
        ArrayList<String> pinyinlist = new ArrayList<>();
        ArrayList<String> originallist = new ArrayList<>();
        int lineNumber = 0;
        ArrayList<String> result = new ArrayList<>();
        regexMatch(result);
        while ((contentLine = reader.readLine()) != null) {
            lineNumber++;
            List<Line> indexes = getPinyinIndex(contentLine);
            char[] chars = contentLine.toCharArray();
            for (Line index : indexes) {
                String s = new String(chars, index.index, index.charNum);
                result.add("line" + lineNumber + "<" + index.kind + "> " + s + "\r\n");
            }
        }
        for (String s : result) {
            finalString += s;
        }
        File file = new File(outputFile);
        FileWriter fileWriter = new FileWriter(outputFile);
        fileWriter.write(finalString);
    }
    private static void regexMatch(ArrayList<String> result) throws IOException {
        for (String word : phaseList) {
            char[] Chinese = word.toCharArray();
            String regex = Chinese[0] + ".+" + Chinese[Chinese.length - 1];
            Pattern compile = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            BufferedReader reader1 = new BufferedReader(new FileReader(testFile));
            String contentLine1;
            Integer lineNumber1 = 0;
            while ((contentLine1 = reader1.readLine()) != null) {
                lineNumber1++;
                Matcher matcher = compile.matcher(contentLine1);
                while (matcher.find()) {
                    String group = matcher.group();
                    if (!group.equals(word) && group.length() < 15) {
                        result.add("Line" + lineNumber1 + "<" + word + "> " + group+"\r\n");
                    }
                }
            }
        }
    }
    public static List<Line> getPinyinIndex(String china) throws Exception {
        HanyuPinyinOutputFormat formart = new HanyuPinyinOutputFormat();
        formart.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        formart.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        formart.setVCharType(HanyuPinyinVCharType.WITH_V);
        ArrayList<Line> integers = new ArrayList<>();
        char[] arrays = china.trim().toCharArray();
        createMap(formart);
        testPinyin(formart, integers, arrays);
        return integers;
    }
    private static void testPinyin(HanyuPinyinOutputFormat formart, ArrayList<Line> integers, char[] arrays) {
        try {
            for (int i = 0; i < arrays.length; i++) {
                Line line = null;
                for (String s : phaseList) {
                    boolean flag = true;
                    if (!Character.toString(s.toCharArray()[0]).matches("[\\u4e00-\\u9fa5]")) {
                        flag = false;
                        continue;
                    }
                    if (flag == true) {
                        if (s.length() == 3) {
                            String firstLetter = wordslist.get(s).get(0).pinyinOfEachLetter;
                            String secondLetter = wordslist.get(s).get(1).pinyinOfEachLetter;
                            String thirdLetter = wordslist.get(s).get(2).pinyinOfEachLetter;
                            Integer firstLength = wordslist.get(s).get(0).length;
                            Integer secondLength = wordslist.get(s).get(1).length;
                            Integer thirdLength = wordslist.get(s).get(2).length;
                            if (arrays.length - i > s.length()
                                    && (test(arrays[i], formart, firstLetter))
                                    && (test(arrays[i + 1], formart, secondLetter))
                                    && (test(arrays[i + 2], formart, thirdLetter))) {
                                line = new Line(s.length(), i, s);
                            }
                            if (arrays.length - i > firstLength + s.length() - 1
                                    && (new String(arrays, i, firstLength)).equals(firstLetter)
                                    && (test(arrays[i + firstLength + 1], formart, secondLetter))
                                    && (test(arrays[i + firstLength + 2], formart, thirdLetter))) {
                                line = new Line(firstLength + s.length() - 1, i, s);
                            }
                            if (arrays.length - i > secondLength + s.length() - 1
                                    && test(arrays[i], formart, firstLetter)
                                    && (new String(arrays, i + 1, secondLength).equals(secondLetter))
                                    && (test(arrays[i + 1 + secondLength], formart, thirdLetter))) {
                                line = new Line(secondLength + s.length() - 1, i, s);
                            }
                            if (arrays.length - i > thirdLength + s.length() - 1
                                    && (test(arrays[i], formart, firstLetter))
                                    && (test(arrays[i + 1], formart, secondLetter))
                                    && ((new String(arrays, i + 2, thirdLength)).equals(thirdLetter))) {
                                line = new Line(thirdLength + s.length() - 1, i, s);
                            }
                            if (arrays.length - i > firstLength + secondLength + thirdLength
                                    && ((new String(arrays, i, firstLength)).equals(firstLetter))
                                    && ((new String(arrays, i + firstLength, secondLength)).equals(secondLetter))
                                    && ((new String(arrays, i + firstLength + secondLength, thirdLength)).equals(thirdLetter))) {
                                line = new Line(firstLength + secondLength + thirdLength, i, s);
                            }
                        } else if (s.length() == 2) {
                            String firstLetter = wordslist.get(s).get(0).pinyinOfEachLetter;
                            String secondLetter = wordslist.get(s).get(1).pinyinOfEachLetter;
                            Integer firstLength = wordslist.get(s).get(0).length;
                            Integer secondLength = wordslist.get(s).get(1).length;
                            if (arrays.length - i > s.length() && (test(arrays[i], formart, firstLetter)
                                    && (test(arrays[i + 1], formart, secondLetter)))) {
                                line = new Line(s.length(), i, s);
                            }
                            if (arrays.length - i > s.length() + firstLength - 1
                                    && (new String(arrays, i, firstLength).equals(firstLetter)
                                    && (test(arrays[i + firstLength], formart, secondLetter)))) {
                                line = new Line(s.length() + firstLength - 1, i, s);
                            }
                            if (arrays.length - i > s.length() - 1 + secondLength
                                    && (test(arrays[i], formart, firstLetter)
                                    && ((new String(arrays, i + 1, secondLength).equals(secondLetter))))) {
                                line = new Line(s.length() - 1 + secondLength, i, s);
                            }
                            if (arrays.length - i > firstLength + secondLength
                                    && (new String(arrays, i, firstLength).equals(firstLetter)
                                    && ((new String(arrays, i + firstLength, secondLength).equals(secondLetter))))) {
                                line = new Line(firstLength + secondLength, i, s);
                            }
                        }
                    }
                }
                if (line != null) {
                    integers.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void createMap(HanyuPinyinOutputFormat formart) throws BadHanyuPinyinOutputFormatCombination {
        wordslist = new HashMap<>();
        //遍历每个关键词
        for (String word : phaseList) {
            Pinyin pinyinOfEachLetter = null;
            boolean flag = true;
            //判断是否是中⽂
            char[] charArray = word.toCharArray();
            for (char c : charArray) {
                if (!Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                ArrayList<Pinyin> list = new ArrayList<>();
                //遍历关键词⾥的每个字
                for (char letter : charArray) {
                    //得到这个字对应的拼⾳
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(letter, formart);
                    //把每个字和对应的拼⾳放进map
                    list.add(new Pinyin(temp[0], temp[0].length()));
                }
                //把每个关键词的map放⼊list
                wordslist.put(word, list);
            }
        }
    }
    private static boolean test(char a, HanyuPinyinOutputFormat format, String key) throws
            BadHanyuPinyinOutputFormatCombination {
        if (Character.toString(a).matches("[\\u4e00-\\u9fa5]")) {
            String[] temp = PinyinHelper.toHanyuPinyinStringArray(a, format);
            if (temp[0].equals(key)) {
                return true;
            }
        }
        return false;
    }
    public static void readWords() {
        try {
            BufferedReader wordReader = new BufferedReader(new FileReader(wordsFile));
            String contentLine;
            phaseList = new ArrayList<>();
            while ((contentLine = wordReader.readLine()) != null) {
                phaseList.add(contentLine);
            }
        } catch (Exception e) {
            System.out.println("找不到要检测的⽂件");
            System.exit(0);
        }
    }
    static class Pinyin {
        String pinyinOfEachLetter;
        Integer length;
        public Pinyin(String pinyin, Integer length) {
            this.pinyinOfEachLetter = pinyin;
            this.length = length;
        }
    }
    static class Line {
        Integer charNum;
        Integer index;
        String kind;
        public Line() {
        }
        public Line(Integer charNum, Integer index, String kind) {
            this.charNum = charNum;
            this.index = index;
            this.kind = kind;
        }
    }
}
