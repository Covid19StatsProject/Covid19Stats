package covid19stats;

import controller.Controller;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Country;
import model.Coviddata;
import static utils.Utils.getStringFromDate;

//Η κλάση δημιουργεί ένα html αρχείο με κώδικα για την προβολή των επιλεγμένων χωρών
//σε χάρτη Google Map και τον εμφανίζει στον web browser
public class GoogleMap {

    //Το όνομα του αρχείου html που δημιουργούμε
    private String htmlFile;
    //Η βασική χώρα που έχει επιλεγεί. Η εστίαση θα γίνει σε αυτή
    private Country mainCountry;
    //Οι υπόλοιπες χώρες που έχουν επιλεγεί
    private List<Country> otherCountries;
    //Το εύρος ημερομηνιών που έχει επιλεγεί
    private Date fromDate;
    private Date toDate;
    //Ο κώδικας html που δημιουργούμε
    private String html;

    //constructor 
    public GoogleMap(String htmlFile, Country mainCountry, List<Country> otherCountries, Date fromDate, Date toDate) {

        //Αρχικοποίηση πεδίων
        this.htmlFile = htmlFile;
        this.mainCountry = mainCountry;
        this.otherCountries = otherCountries;
        this.fromDate = fromDate;
        this.toDate = toDate;

        //Τα τμήματα του html κώδικα που χρησιμοποιούμε ως έχουν
        String html1 = "<!DOCTYPE html>\n"
                + "<html> \n"
                + "<head> \n"
                + "  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /> \n"
                + "  <title>Google Maps Multiple Markers</title> \n"
                + "  <script src=\"http://maps.google.com/maps/api/js?sensor=false\" \n"
                + "          type=\"text/javascript\"></script>\n"
                + "</head> \n"
                + "<body>\n"
                + "  <div id=\"map\" style=\"width: 500px; height: 400px;\"></div>\n"
                + "\n"
                + "  <script type=\"text/javascript\">\n";

        String html2 = "\n"
                + "    var map = new google.maps.Map(document.getElementById('map'), {\n"
                + "      zoom: 5,\n";

        String html3 = "      mapTypeId: google.maps.MapTypeId.ROADMAP\n"
                + "    });\n"
                + "\n"
                + "    var infowindow = new google.maps.InfoWindow();\n"
                + "\n"
                + "    var marker, i;\n"
                + "\n"
                + "    for (i = 0; i < locations.length; i++) {  \n"
                + "      marker = new google.maps.Marker({\n"
                + "        position: new google.maps.LatLng(locations[i][1], locations[i][2]),\n"
                + "        map: map\n"
                + "      });\n"
                + "\n"
                + "      google.maps.event.addListener(marker, 'click', (function(marker, i) {\n"
                + "        return function() {\n"
                + "          infowindow.setContent(locations[i][0]);\n"
                + "          infowindow.open(map, marker);\n"
                + "        }\n"
                + "      })(marker, i));\n"
                + "    }\n"
                + "  </script>\n"
                + "</body>\n"
                + "</html>";

        //Το τμήμα του html κώδικα που αφορά το σημείο εστίασης
        String center = "      center: new google.maps.LatLng(" + mainCountry.getLat() + ", " + mainCountry.getLong1() + "),\n";

        //Σύνθεση του συνολικού html κώδικα στο String html
        html = html1 + createLocationsList() + html2 + center + html3;
    }

    //Η μέθοδος ανοίγει την σελίδα htmlFile στον web browser
    public void showMap() {
        try {
            Runtime.getRuntime().exec("cmd /c start " + htmlFile);
        } catch (IOException e) {
            //Εμφάνιση μηνύματος αν συμβεί IOException 
            System.out.println(e.getMessage());
        }
    }

    //Η μέθοδος δημιουργεί το τμήμα του html κώδικα που αφορά τις τοποθεσίες που 
    //θα προβληθούν χρησιμοποιώντας τις επιλεγμένες χώρες (βασική και υπόλοιπες)
    public String createLocationsList() {
        //Λίστα με όλες τις χώρες που θα προβληθούν στον χάρτη
        List<Country> allCountries = new ArrayList<>();
        //Προσθέτουμε στην λίστα την βασική χώρα 
        allCountries.add(mainCountry);
        //και τις υπόλοιπες χώρες (αν έχουν επιλεγεί)
        if (otherCountries != null) {
            allCountries.addAll(otherCountries);
        }
        //Δημιουργούμε το τμήμα html κώδικα που αφορά τις τοποθεσίες
        String locations = "    var locations = [\n";
        //Μετρητής χωρών που βάζουμε στον πίνακα locations του html κώδικα
        int count = 0;
        //Διατρέχουμε όλες τις επιλεγμένες χώρες
        for (Country country : allCountries) {
            //και παίρνουμε τα δεδομένα που περιέχουν για τις τρεις κατηγορίες
            //στο εύρος των επιλεγμένων ημερομηνιών (αν έχουν επιλεγεί)
            List<Coviddata> deaths, recovered, confirmed;
            if (fromDate != null && toDate != null) {
                deaths = Controller.getInstance().selectCovidData(country, (short) 1, fromDate, toDate);
                recovered = Controller.getInstance().selectCovidData(country, (short) 2, fromDate, toDate);
                confirmed = Controller.getInstance().selectCovidData(country, (short) 3, fromDate, toDate);
            } else {
                deaths = Controller.getInstance().selectCovidData(country, (short) 1, fromDate, toDate);
                recovered = Controller.getInstance().selectCovidData(country, (short) 2, fromDate, toDate);
                confirmed = Controller.getInstance().selectCovidData(country, (short) 3, fromDate, toDate);
            }
            //Διατρέχουμε τα δεδομένα deaths (εφόσον υπάρχουν) και αθροίζουμε τους θανάτους
            //για το συγκεκριμένο εύρος ημερομηνιών
            int deathsNumber = 0;
            if (deaths != null) {
                for (Coviddata d : deaths) {
                    deathsNumber += d.getQty();
                }
            }
            //Διατρέχουμε τα δεδομένα recovered (εφόσον υπάρχουν) και αθροίζουμε 
            //το πλήθος όσων έχουν ανακάμψει για το συγκεκριμένο εύρος ημερομηνιών
            int recoveredNumber = 0;
            if (recovered != null) {
                for (Coviddata d : recovered) {
                    recoveredNumber += d.getQty();
                }
            }
            //Διατρέχουμε τα δεδομένα confirmed (εφόσον υπάρχουν) και αθροίζουμε 
            //το πλήθος των επιβεβαιωμένων κρουσμάτων για το συγκεκριμένο εύρος ημερομηνιών
            int confirmedNumber = 0;
            if (confirmed != null) {
                for (Coviddata d : confirmed) {
                    confirmedNumber += d.getQty();
                }
            }
            //Δημιουργούμε και προσθέτουμε ένα νέο στοιχείο χώρας στον κώδικα html
            //με τα στοιχεία της τρέχουσας χώρας που διατρέχουμε
            //Κάνουμε χρήση της μεθόδου getStringFromDate για να διαμορφώσουμε 
            //τις ημερομηνίες στην μορρφή "dd/MM/yy"
            locations += "      ['" + country.getName()
                    + ", Από: " + getStringFromDate(fromDate, "dd/MM/yy")
                    + ", Μέχρι: " + getStringFromDate(toDate, "dd/MM/yy")
                    + ", Θάνατοι =" + deathsNumber
                    + ", Ασθενείς που έχουν ανακάμψει = " + recoveredNumber
                    + ", Κρούσματα = " + confirmedNumber
                    + "'," + country.getLat()
                    + "," + country.getLong1() + ", 1]";
            //Eνημέρωση πλήθους χωρών
            count++;
            //Eφόσον δεν είμαστε στην τελευταία βάζουμε κόμμα στον κώδικα html 
            //διότι ακολουθεί και άλλη χώρα
            if (count != allCountries.size()) {
                locations += ",\n";
            }

        }
        //Εδώ κλείνει ο πίνακας locations του html κώδικα 
        locations += "\n    ];";
        //Επιστρέφουμε το τμήμα του html κώδικα που αφορά τις τοποθεσίες
        return locations;
    }

    //Η μέθοδος δημιουργεί το αρχείο htmlFile και γράφει μέσα σε αυτό τον κώδικα html που έχει παραχθεί
    public void createHtmlFile() {
        try {
            //Αντικείμενο FileWriter για το αρχείο htmlFile 
            FileWriter fw = new FileWriter(htmlFile);
            //Εγγραφή του κώδικα html μέσα στο αρχείο
            fw.write(html);
            //Κλείσιμο του FileWriter
            fw.close();
        } catch (IOException e) {
            //Εμφάνιση μηνύματος αν συμβεί IOException 
            System.out.println(e.getMessage());
        }
    }

}
