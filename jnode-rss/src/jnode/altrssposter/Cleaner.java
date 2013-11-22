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

        if (!isTitle){
            replaceAll(sb, "\r", " ");
            replaceAll(sb, "\n", " ");
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
