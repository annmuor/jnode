package jnode.report;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class ReportBuilder {
    private List<String> columns;
    private List<Integer> colLength;
    private int width;
    private final StringBuilder sb = new StringBuilder();
    private boolean isHeaderPrinted;

    static void addVer(StringBuilder sb) {
        sb.append('|');
    }

    private static void addCross(StringBuilder sb) {
        sb.append('+');
    }

    static void addItem(StringBuilder sb, String item, int len) {
        if (item == null || len == 0) {
            return;
        }

        if (item.length() > len) {
            addItem(sb, item.substring(0, len), len);
        } else {
            sb.append(item);
            for (int i = item.length(); i < len; i++) {
                sb.append(' ');
            }
        }

    }

    static void addCenterItem(StringBuilder sb, String item, int len) {
        if (item == null || len == 0) {
            return;
        }

        if (item.length() > len) {
            addCenterItem(sb, item.substring(0, len), len);
        } else {

            int rest = len - item.length();
            int left = rest / 2;
            int right = rest - left;

            for (int i = 0; i < left; i++) {
                sb.append(' ');
            }
            sb.append(item);
            for (int i = 0; i < right; i++) {
                sb.append(' ');
            }
        }

    }

    static void newLine(StringBuilder sb) {
        sb.append('\n');
    }

    private static void horLine(StringBuilder sb, int len) {
        for (int i = 0; i < len; ++i) {
            sb.append('-');
        }
    }

    public int getWidth() {
        return width;
    }

    private List<Integer> getColLength() {
        return colLength;
    }

    public void setColLength(List<Integer> colLength) {
        if (colLength == null) {
            this.colLength = null;
            width = 0;
        } else {
            this.colLength = new ArrayList<>(colLength);
            width = 0;
            for (Integer aColLength : colLength) {
                ++width;
                width += aColLength;
            }
            ++width;
        }
    }

    private List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        if (columns == null) {
            this.columns = null;
        } else {
            this.columns = new ArrayList<>(columns);
        }
    }

    public StringBuilder getText() {
        if (sb.length() != 0){
            printHorLine();
        }
        return sb;
    }

    public void printLine(String... args){
        checks();
        if (args == null){
            throw new IllegalArgumentException("bad args");
        }
        if (args.length != getColLength().size() ){
            throw new IllegalArgumentException("bad args count");
        }
        if (!isHeaderPrinted){
            printHeader();
        }

        for (int i = 0; i < args.length; ++i) {
            addVer(sb);
            addItem(sb, args[i] != null ? args[i] : "", getColLength().get(i));
        }
        addVer(sb);
        newLine(sb);
    }

    private void checks() {
        if (getColumns() == null) {
            throw new IllegalStateException("columns == null");
        }
        if (getColLength() == null) {
            throw new IllegalStateException("colLength == null");
        }
        if (getColumns().size() != getColLength().size()) {
            throw new IllegalStateException("columns.size() != colLength.size()");
        }
    }

    public void printHorLine(){
        checks();
        if (!isHeaderPrinted){
            printHeader();
        }
        horLine(sb);
    }

    void horLine(StringBuilder sb) {
        for (int i = 0; i < getColLength().size(); ++i) {
            addCross(sb);
            horLine(sb, getColLength().get(i));
        }
        addCross(sb);
        newLine(sb);
    }

    private void printHeader() {
        horLine(sb);

        for (int i = 0; i < getColumns().size(); ++i) {
            addVer(sb);
            addCenterItem(sb, getColumns().get(i), getColLength().get(i));
        }
        addVer(sb);
        newLine(sb);

        horLine(sb);

        isHeaderPrinted = true;
    }
}
