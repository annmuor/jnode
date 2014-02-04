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

package jnode.altrssposter;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public final class Cleaner {
    private Cleaner() {
    }

    public static String clean(final String src, boolean isTitle){
        if (src == null){
            return null;
        }


        StringBuilder sb = new StringBuilder(src);

        if (!isTitle){
            replaceAll(sb, "<br>", "\n");
            replaceAll(sb, "<br/>", "\n");
            replaceAll(sb, "<br />", "\n");
            replaceAll(sb, "<p>", "\n");
        }
        replaceAll(sb, "<b>", "*");
        replaceAll(sb, "</b>", "*");
        replaceAll(sb, "<strong>", "*");
        replaceAll(sb, "</strong>", "*");
        replaceAll(sb, "<i>", "/");
        replaceAll(sb, "</i>", "/");
        replaceAll(sb, "<em>", "/");
        replaceAll(sb, "</em>", "/");
        replaceAll(sb, "<s>", "[зачеркнуто]");
        replaceAll(sb, "</s>", "[/зачеркнуто]");
        replaceAll(sb, "<strike>", "[зачеркнуто]");
        replaceAll(sb, "</strike>", "[/зачеркнуто]");

        if (isTitle){
            replaceAll(sb, "<", "'");
            replaceAll(sb, "&lt;", "'");
            replaceAll(sb, "&gt;", "'");
            replaceAll(sb, ">", "'");
            replaceAll(sb, "«", "'");
            replaceAll(sb, "»", "'");
            replaceAll(sb, "&laquo;", "'");
            replaceAll(sb, "&raquo;", "'");
            replaceAll(sb, "\\", "'");
            replaceAll(sb, "&#8220;", "'");
            replaceAll(sb, "&#8221;", "'");
            replaceAll(sb, "&#171;", "'");
            replaceAll(sb, "&#171;", "'");
            replaceAll(sb, "“", "'");
            replaceAll(sb, "”", "'");

        } else {
            replaceAll(sb, "<", "\"");
            replaceAll(sb, "&lt;", "\"");
            replaceAll(sb, "&gt;", "\"");
            replaceAll(sb, ">", "\"");
            replaceAll(sb, "«", "\"");
            replaceAll(sb, "»", "\"");
            replaceAll(sb, "&laquo;", "\"");
            replaceAll(sb, "&raquo;", "\"");
            replaceAll(sb, "&#8220;", "\"");
            replaceAll(sb, "&#8221;", "\"");
            replaceAll(sb, "&#171;", "\"");
            replaceAll(sb, "&#187;", "\"");
            replaceAll(sb, "“", "\"");
            replaceAll(sb, "”", "\"");
        }

        replaceAll(sb, "\u0261", "-");

        replaceAll(sb, "&quot;", "\"");
        replaceAll(sb, "\"blockquote\"", ">");
        replaceAll(sb, "\"/blockquote\"", " ");
        replaceAll(sb, "&nbsp;", " ");
        replaceAll(sb, "&ndash;", "-");
        replaceAll(sb, "&mdash;", "-");
        replaceAll(sb, "&#38;", "&");
        replaceAll(sb, "&#39;", "'");
        replaceAll(sb, "&#039;", "'");
        replaceAll(sb, "&#8211;", "-");
        replaceAll(sb, "&#8212", "-");
        replaceAll(sb, "&#8216;", "'");
        replaceAll(sb, "&#8217;", "'");

        replaceAll(sb, "&#8230;", "...");
        replaceAll(sb, "&hellip;", "...");
        replaceAll(sb, "\u2013", "-");
        replaceAll(sb, "\u2014", "-");

        if (isTitle){
            replaceAll(sb, "\r", "");
            replaceAll(sb, "\n", "");
        }

        return sb.toString();
    }

    public static void replaceAll(StringBuilder builder, String from, String to)
    {
        int index = builder.indexOf(from);
        while (index != -1)
        {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }
}
