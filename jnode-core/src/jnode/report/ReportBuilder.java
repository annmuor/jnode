/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.report;

import jnode.core.ConcurrentDateFormatAccess;

import java.util.*;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class ReportBuilder {
    private final StringBuilder sb = new StringBuilder();
    private final Map<String, FieldFormatter> formatters = new HashMap<>();
    private List<String> columns;
    private List<Integer> colLength;
    private int width;
    private boolean isHeaderPrinted;
    private List<String> formats;

    public ReportBuilder() {
        formatters.put("S", new FieldFormatter() {
            @Override
            public String formatValue(Object s) {
                return s == null ? "" : String.valueOf(s);
            }
        });
        formatters.put("D", new FieldFormatter() {
            private final ConcurrentDateFormatAccess dateFormat = new ConcurrentDateFormatAccess("dd.MM.yyyy");

            @Override
            public String formatValue(Object s) {

                if (!(s instanceof String)) {
                    return "";
                }

                String str = (String) s;

                long time;
                try {
                    time = Long.parseLong(str);
                } catch (NumberFormatException e) {
                    return "Bad date(1)";
                }

                if (time <= 0) {
                    return "Bad date(2)";
                }

                Date date = new Date(time);

                return dateFormat.format(date);
            }
        });

    }

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

    private static boolean isEmptyStr(String s) {
        return s == null || s.length() == 0;
    }

    static List<String> asStrList(String data, String delims) {
        if (isEmptyStr(data) || isEmptyStr(delims)) {
            return null;
        }

        String[] items = data.split(delims);
        return Arrays.asList(items);
    }

    static List<Integer> asIntList(String data, String delims) {
        List<String> temp = asStrList(data, delims);
        if (temp == null) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        for (String item : temp) {
            result.add(Integer.valueOf(item));
        }
        return result;
    }

    static void newLine(StringBuilder sb) {
        sb.append('\n');
    }

    private static void horLine(StringBuilder sb, int len) {
        for (int i = 0; i < len; ++i) {
            sb.append('-');
        }
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        if (formats == null) {
            this.formats = null;
        } else {
            this.formats = new ArrayList<>(formats);
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

    public void setColLength(String colLen, String delim) {
        setColLength(asIntList(colLen, delim));
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

    public void setColumns(String cols, String delim) {
        setColumns(asStrList(cols, delim));
    }

    public void setFormats(String formats, String delim) {
        setFormats(asStrList(formats, delim));
    }

    public StringBuilder getText() {
        if (sb.length() != 0) {
            printHorLine();
        }
        return sb;
    }

    public void printLine(String... args) {
        checks();
        if (args == null) {
            throw new IllegalArgumentException("bad args");
        }
        if (args.length != getColLength().size()) {
            throw new IllegalArgumentException("bad args count");
        }
        if (!isHeaderPrinted) {
            printHeader();
        }

        for (int i = 0; i < args.length; ++i) {
            addVer(sb);
            addItem(sb, convert(args[i] != null ? args[i] : "", i), getColLength().get(i));
        }
        addVer(sb);
        newLine(sb);
    }

    String convert(String item, int convNum) {
        if (getFormats() == null) {
            return item;
        }

        final String formatterKey = getFormats().get(convNum);
        if (formatters.containsKey(formatterKey)) {
            return formatters.get(formatterKey).formatValue(item);
        } else {
            return item;
        }
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
        if (getFormats() != null && getFormats().size() != getColumns().size()) {
            throw new IllegalStateException("getFormats().size() !=  getColumns().size()");
        }
    }

    public void printHorLine() {
        checks();
        if (!isHeaderPrinted) {
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

    interface FieldFormatter {
        String formatValue(Object s);
    }
}
