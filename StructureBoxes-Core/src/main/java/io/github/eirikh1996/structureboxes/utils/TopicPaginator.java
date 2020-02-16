package io.github.eirikh1996.structureboxes.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopicPaginator {
    private final String title;
    private final List<String> lines = new ArrayList<>();
    public TopicPaginator(String title){
        this.title = title;
    }

    public boolean addLine(String line){
        boolean result = lines.add(line);
        Collections.sort(lines);
        return result;
    }

    public String[] getPage(int pageNumber){
        if(!isInBounds(pageNumber))
            throw new IndexOutOfBoundsException("Page number " + pageNumber + " exceeds bounds <" + 1 + "," + getPageCount() + ">");
        String[] tempLines = new String[pageNumber == getPageCount() ? (lines.size()%10) + 1 : 10];
        tempLines[0] = "§5========[§6 " + title + " §5]===[§6Page " + pageNumber +  "/" + getPageCount() +" §5]=========";
        for(int i = 0; i< tempLines.length-1; i++)
            tempLines[i+1] = lines.get(((9) * (pageNumber-1)) + i);
        return tempLines;
    }

    public int getPageCount(){
        return (int)Math.ceil(((double)lines.size())/(9));
    }

    public boolean isInBounds(int pageNumber){
        return pageNumber > 0 && pageNumber <= getPageCount();
    }

    public boolean isEmpty(){
        return lines.isEmpty();
    }
}
