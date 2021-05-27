package utils;


import java.text.SimpleDateFormat;
import java.util.Date;


public class Utils {
//Η μέθοδος δέχεται μία συμβολοσειρά stringDate που περιέχει μία ημερομηνία 
    //και μία συμβολοσειρά με την μορφοποιήση που χρησιμοποιεί και επιστρέφει 
    //ένα αντικείμενο τύπου Date με αυτή την ημερομηνία (ή null αν η ημερονηνία είναι λανθασμένη)
    public static Date getDateFromString(String stringDate, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(stringDate);
        } catch (java.text.ParseException ex) {
            System.out.println(ex.getMessage());
        }
        return date;
    }

    //Η μέθοδος δέχεται μία μεταβλητή τύπου Date που περιέχει μία ημερομηνία 
    //και μία συμβολοσειρά με την μορφοποιήση που χρησιμοποιεί και επιστρέφει 
    //μία συμβολοσειρά με αυτή την ημερομηνία (ή κενή συμβολοσειρά αν η ημερονηνία είναι λανθασμένη)
    public static String getStringFromDate(Date date, String format) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        }
    }
    
}
