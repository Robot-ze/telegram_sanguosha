package components;

import java.util.List;
import java.util.ArrayList;

 

public class TextShowUnit {
    //private static final int SHOWTEXT_LINECOUNT = config.SHOWTEXT_LINECOUNT;

    //private static final int SHOWTEXT_LINECOUNT = 5;
    private int lineLimit;
    List<StringBuilder> wordList;
    int currentRowCount = 0;

    public TextShowUnit(int lineLimit,boolean firstLine) {
        this.lineLimit=lineLimit;
        wordList = new ArrayList<>(2);
        StringBuilder lastStringBuilder = new StringBuilder();
        if(firstLine){
            lastStringBuilder.append("    ├");
        }else{
            lastStringBuilder.append("\n    ├");
        }
      
        wordList.add(lastStringBuilder);
    }

    /**
     * 
     * @param htmlName
     * @param preLen 预估显示长度
     */
    public void append(String htmlName, int preLen) {
        StringBuilder lastStringBuilder;
      
        if (preLen + currentRowCount > lineLimit) {
            lastStringBuilder = new StringBuilder();
            lastStringBuilder.append("\n    ├");
            wordList.add(lastStringBuilder);
            currentRowCount = 0;
        } else {
            lastStringBuilder = wordList.get(wordList.size() - 1);
        }
        currentRowCount += preLen;
        lastStringBuilder.append(htmlName);

    }

    @Override
    public String toString() {
        StringBuilder allStringBuilder = new StringBuilder();
        for (StringBuilder s : wordList) {
            allStringBuilder.append(s);
        }
        return allStringBuilder.toString();
    }
}
