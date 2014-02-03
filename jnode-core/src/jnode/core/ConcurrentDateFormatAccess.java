package jnode.core;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ConcurrentDateFormatAccess {

    private final String dateFormat;
    private final Locale locale;

    public ConcurrentDateFormatAccess(String dateFormat, Locale locale) {
        this.dateFormat = dateFormat;
        this.locale = locale;
    }

    public ConcurrentDateFormatAccess(String dateFormat) {
        this.dateFormat = dateFormat;
        this.locale = null;
    }

    private ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat> () {

        @Override
        public DateFormat get() {
            return super.get();
        }

        @Override
        protected DateFormat initialValue() {
            if (locale == null){
                return new SimpleDateFormat(dateFormat);
            } else {
                return new SimpleDateFormat(dateFormat, locale);
            }
        }

        @Override
        public void remove() {
            super.remove();
        }

        @Override
        public void set(DateFormat value) {
            super.set(value);
        }

    };

    public Date parse(String dateString) throws ParseException {
        return df.get().parse(dateString);
    }

    public String format(Date date){
        return df.get().format(date);
    }

    public String currentDateAsString(){
        return format(new Date());
    }
}
