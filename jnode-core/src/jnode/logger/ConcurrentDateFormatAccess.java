package jnode.logger;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class ConcurrentDateFormatAccess {

    private final String dateFormat;

    public ConcurrentDateFormatAccess(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    private ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat> () {

        @Override
        public DateFormat get() {
            return super.get();
        }

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(dateFormat);
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

    public Date convertStringToDate(String dateString) throws ParseException {
        return df.get().parse(dateString);
    }

    public String convertDateToString(Date date){
        return df.get().format(date);
    }
}
