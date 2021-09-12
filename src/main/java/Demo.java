import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

    public static void main(String[] args) throws Exception {
        readWords();
        read();
    }

    public static void read() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\86150\\Documents\\Tencent Files\\1173084467\\FileRecv\\org.txt"));
        String contentLine;
        ArrayList<String> pinyinlist = new ArrayList<>();
        ArrayList<String> originallist = new ArrayList<>();
        int lineNumber = 0;
        ArrayList<String> result = new ArrayList<>();
        regexMatch(result);
        while ((contentLine = reader.readLine()) != null) {
            lineNumber++;
//            使用正则匹配;
//            String patten1 = "法.+功";
//            Pattern pattern1 = Pattern.compile(patten1);
//            Matcher matcher1 = pattern1.matcher(contentLine);
//            while (matcher1.find()) {
//                String group = matcher1.group();
//                if (!group.equals("法轮功") && group.length() < 20) {
//                    result.add("Line" + lineNumber + "<法轮功> " + group);
//                    break;
//                }
//            }
//            String patten2 = "邪.+教";
//            Pattern pattern2 = Pattern.compile(patten2);
//            Matcher matcher2 = pattern2.matcher(contentLine);
//            while (matcher2.find()) {
//                String group = matcher2.group();
//                if (!group.equals("邪教") && group.length() < 10) {
//                    result.add("Line" + lineNumber + "<邪教> " + group);
//                    break;
//                }
//            }
//            String patten3 = "f.+k";
//            Pattern pattern3 = Pattern.compile(patten3, Pattern.CASE_INSENSITIVE);
//            Matcher matcher3 = pattern3.matcher(contentLine);
//            while (matcher3.find()) {
//                String group = matcher3.group();
//                if (group.length() < 10) {
//                    result.add("Line" + lineNumber + "<fuck> " + group);
//                    break;
//                }
//            }


            List<Line> indexes = getPinyinIndex(contentLine);
            char[] chars = contentLine.toCharArray();


            for (Line index : indexes) {
                String s = new String(chars, index.index, index.charNum);
                result.add("line" + lineNumber + "<" + index.kind + "> " + s);
            }
        }
        result.stream().forEach(ele -> System.out.println(ele));


    }

    private static void regexMatch(ArrayList<String> result) throws IOException {
        for (String word : phaseList) {
            char[] Chinese = word.toCharArray();
            String regex = Chinese[0] + ".+" + Chinese[Chinese.length - 1];
            Pattern compile = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            BufferedReader reader1 = new BufferedReader(new FileReader("C:\\Users\\86150\\Documents\\Tencent Files\\1173084467\\FileRecv\\org.txt"));
            String contentLine1;
            Integer lineNumber1=0;
            while ((contentLine1 = reader1.readLine()) != null) {
                lineNumber1++;
                Matcher matcher = compile.matcher(contentLine1);
                while (matcher.find()) {
                    String group = matcher.group();
                    if (!group.equals(word) && group.length() < 15) {
                        result.add("Line" + lineNumber1 + "<" + word + "> " + group);
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
                    //匹配中文字符的正则表达式：[\\u4E00-\\u9FA5]
                    //至少匹配一个汉字的写法。
                    //这两个unicode值正好是Unicode表中的汉字的头和尾。
                    //"[]"代表里边的值出现一个就可以，后边的“+”代表至少出现1次，合起来即至少匹配一个汉字。
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

//                if (arrays.length - i > 3 && (test(arrays[i], formart, "fa")
//                        && (test(arrays[i + 1], formart, "lun"))
//                        && (test(arrays[i + 2], formart, "gong")))) {
//                    line = new Line(3, i, "法轮功");
//                }
//                if (arrays.length - i > 4 && (new String(arrays, i, 2)).equals("fa")
//                        && (test(arrays[i + 3], formart, "lun"))
//                        && test(arrays[i + 4], formart, "gong")) {
//                    line = new Line(4, i, "法轮功");
//                }
//
//                if (arrays.length - i > 5 && test(arrays[i], formart, "fa")
//                        && (new String(arrays, i + 1, 3).equals("lun"))
//                        && (test(arrays[i + 4], formart, "gong"))) {
//                    line = new Line(5, i, "法轮功");
//                }
//
//                if (arrays.length - i > 6 && test(arrays[i], formart, "fa")
//                        && (test(arrays[i + 1], formart, "lun"))
//                        && ((new String(arrays, i + 2, 4)).equals("gong"))) {
//                    line = new Line(6, i, "法轮功");
//                }
//                if (arrays.length - i > 6 && ((new String(arrays, i, 2)).equals("fa"))
//                        && ((new String(arrays, i + 2, 3)).equals("lun"))
//                        && ((new String(arrays, i + 5, 4)).equals("gong"))) {
//                    line = new Line(9, i, "法轮功");
//                }
//                if (arrays.length - i > 2 && (test(arrays[i], formart, "xie")
//                        && (test(arrays[i + 1], formart, "jiao")))) {
//                    line = new Line(2, i, "邪教");
//                }
//                if (arrays.length - i > 4 && (new String(arrays, i, 3).equals("xie")
//                        && (test(arrays[i + 1], formart, "jiao")))) {
//                    line = new Line(2, i, "邪教");
//                }
//                if (arrays.length - i > 5 && (test(arrays[i], formart, "xie")
//                        && ((new String(arrays, i + 1, 4).equals("jiao"))))) {
//                    line = new Line(5, i, "邪教");
//                }
//                if (arrays.length - i > 7 && (new String(arrays, i, 3).equals("xie")
//                        && ((new String(arrays, i + 3, 4).equals("jiao"))))) {
//                    line = new Line(2, i, "邪教");
//                }
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
            //判断是否是中文
            char[] charArray = word.toCharArray();
            for (char c : charArray) {
                if (!Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                ArrayList<Pinyin> list = new ArrayList<>();
                //遍历关键词里的每个字
                for (char letter : charArray) {

                    //得到这个字对应的拼音
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(letter, formart);
                    //把每个字和对应的拼音放进map

                    list.add(new Pinyin(temp[0], temp[0].length()));
                }
                //把每个关键词的map放入list
                wordslist.put(word, list);
            }
        }
    }

    private static boolean test(char a, HanyuPinyinOutputFormat format, String key) throws BadHanyuPinyinOutputFormatCombination {
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
            BufferedReader wordReader = new BufferedReader(new FileReader("C:\\Users\\86150\\Documents\\Tencent Files\\1173084467\\FileRecv\\words.txt"));
            String contentLine;
            phaseList = new ArrayList<>();
            while ((contentLine = wordReader.readLine()) != null) {
                phaseList.add(contentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
