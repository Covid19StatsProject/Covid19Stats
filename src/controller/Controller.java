package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import static utils.Utils.getDateFromString;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.JOptionPane;
import model.Country;
import model.Coviddata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Controller {

    //Δημιουργία του entity manager για την διαχείριση της βάσης δεδομένων
    public EntityManagerFactory emf;
    public EntityManager em;

    private static Controller instance;

    private Controller() {
        emf = Persistence.createEntityManagerFactory("Covid19StatsPU");
        em = emf.createEntityManager();
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    public String retrieveJSONData(String urlToCall) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(urlToCall).build();
        String responseString = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                responseString = response.body().string();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        //Μήνυμα εφόσον δεν μπορεί να πάρει δεδομένα από την διεύθυνση 
        if (responseString == null) {
            JOptionPane.showMessageDialog(null, "Αδυναμία λήψης δεδομένων από τη διεύθυνση " + urlToCall, "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
        //Επιστρέφει σε μορφή συμβολοσειράς τα δεδομένα (ή null αν δεν μπόρεσε να πάρει δεδομένα)
        return responseString;
    }

    //Δέχεται μία συμβολοσειρά json και το όνομα της χρονοσειράς timeSeries 
    //(με τιμή deaths, recovered ή confirmed), αντλεί μέσα από το json το JsonArray
    //που αντιστοιχεί στην συγκεκριμένη χρονοσειρά και το επιστρέφει
    public JsonArray getCovidData(String json, String timeSeries) {
        JsonArray jsonArray = null;
        //Έλεγχος αν συμβολοσειρά json δεν είναι κενή
        if (json != null) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(json).getAsJsonObject();
            //Παίρνει το JsonArray της χρονοσειράς timeSeries
            jsonArray = jsonObject.get(timeSeries).getAsJsonArray();
        }
        //Επιστρέφει το JsonArray 
        return jsonArray;
    }

    //Δέχεται μία συμβολοσειρά json, το όνομα μιας χώρας countryName και το όνομα της χρονοσειράς timeSeries,
    //αντλεί μέσα από το json το jsonObject με τα δεδομένα τύπου timeSeries της χώρας countryName
    //και το επιστρέφει
    public JsonObject getCountryData(String json, String countryName, String timeSeries) {
        //Το JsonArray που αφορά τη χρονοσειρά timeSeries
        JsonArray jsonArray = getCovidData(json, timeSeries);
        //Εφόσον δεν είναι κενό
        if (jsonArray != null) {
            //Διατρέχουμε τα στοιχεία του
            for (JsonElement countryData : jsonArray) {
                //Τα δεδομένα τύπου timeseries της τρέχουσας χώρας
                JsonObject data = countryData.getAsJsonObject();
                //Από κάθε στοιχείο του JsonArray παίρνουμε το όνομα της χώρας name
                //και το όνομα της επαρχίας(province/state)
                String name = data.get("Country/Region").getAsString();
                String provinence = countryData.getAsJsonObject().get("Province/State").getAsString();
                //Εφόσον το όνομα της επαρχίας δεν είναι κενό το προσθέτουμε στο τέλος του ονόματος 
                //της χώρας, δηλαδή δημιουργούμε όνομα χώρας της μορφής "Χώρα Επαρχία"
                if (!provinence.equals("")) {
                    name += " " + provinence;
                }
                //Αν το όνομα είναι το όνομα της χώρας που θέλουμε(countryName)
                //επιστρέφουμε τα δεδομένα της χώρας
                if (name.equals(countryName)) {
                    return data;
                }
            }
        }
        //Επιστρέφουμε null αν δεν βρεθούν δεδομένα για την χώρα 
        return null;
    }

    //Αποθηκεύει στον πίνακα COUNTRY όλες τις χώρες που βρίσκει στην χρονοσειρά timeSeries του api
    //και δεν είναι ήδη αποθηκευμένες
    //Επιστρέφει τη συμβολοσειρά json της συγκεκριμένης χρονοσειράς
    public String storeCountries(String timeSeries) {
        //Η διεύθυνση (url) της χρονοσειράς timeSeries
        String urlToCall = "https://covid2019-api.herokuapp.com/timeseries/" + timeSeries;
        //Η συμβολοσειρά json της χρονοσειράς
        String json = retrieveJSONData(urlToCall);
        //Το JsonArray με τα δεδομένα της χρονοσειράς
        JsonArray jsonArray = getCovidData(json, timeSeries);
        //Εφόσον το JsonArray δεν είναι κενό
        if (jsonArray != null) {
            //Παίρνουμε τις αποθηκευμένες χώρες από την βάση
            //και αποθηκεύουμε τα ονόματα τους στη λίστα storedCountriesNames
            List<Country> storedCountries = selectAllCountries();
            List<String> storedCountriesNames = new ArrayList<>();
            for (Country c : storedCountries) {
                storedCountriesNames.add(c.getName());
            }
            //Για κάθε στοιχείο του JsonArray
            for (JsonElement countryData : jsonArray) {
                //data είναι το στοιχείο ως JsonObject
                JsonObject data = countryData.getAsJsonObject();
                //Παίρνουμε το όνομα της χώρας name
                //και το όνομα της επαρχίας(province/state)
                String name = data.get("Country/Region").getAsString();
                String provinence = countryData.getAsJsonObject().get("Province/State").getAsString();
                //Εφόσον το όνομα της επαρχίας δεν είναι κενό το προσθέτουμε στο τέλος του ονόματος 
                //της χώρας, δηλαδή δημιουργούμε όνομα χώρας της μορφής "Χώρα Επαρχία")
                if (!provinence.equals("")) {
                    name += " " + provinence;
                }
                //Εφόσον το όνομα της χώρας δεν ανήκει σε χώρα που είναι ήδη αποθηκευμένη στη βάση
                if (!storedCountriesNames.contains(name)) {
                    //Δημιουργούμε ένα νέο αντικείμενο τύπου Country
                    //(Δίνοντας τιμή 0 στο πεδίο country διότι είναι auto-incremented, άρα θα μεγαλώνει κάθε φορά κατά 1 αυτόματα) 
                    Country country = new Country(0, name);
                    //Εφόσον υπάρχει τιμή για γεωγραφικό πλάτος την θέτουμε στο νέο αντικείμενο 
                    if (!data.get("Lat").getAsString().equals("")) {
                        double lat = data.get("Lat").getAsDouble();
                        country.setLat(lat);
                    }
                    //Εφόσον υπάρχει τιμή για γεωγραφικό μήκος την θέτουμε στο νέο αντικείμενο 
                    if (!data.get("Long").getAsString().equals("")) {
                        double lon = data.get("Long").getAsDouble();
                        country.setLong1(lon);
                    }
                    //και αποθηκεύουμε την χώρα ως εγγραφή στον πίνακα COUNTRY
                    em.persist(country);
                }
            }
        }
        //Επιστρέφουμε την συμβολοσειρά json
        return json;
    }

    //Αποθηκεύει στον πίνακα COVIDDATA όλα τα δεδομένα της χώρας country
    //που βρίσκει στην χρονοσειρά timeSeries του api και δεν είναι ήδη αποθηκευμένα
    //Χρησιμοποιεί τη λίστα storedCountryData για να βρίσκει ποιά δεδομένα είναι ήδη αποθηκευμένα
    public void storeCountryData(String json, Country country, String timeSeries, List<Coviddata> storedCountryData) {
        //Η τιμή κρουσμάτων της προηγούμενης μέρας (αρχικά 0)
        int previousDayQty = 0;
        //data είναι τα δεδομένα τύπου timeSeries της χώρας countryName από το json
        JsonObject data = getCountryData(json, country.getName(), timeSeries);
        //Αν δεν υπάρχουν δεδομένα η συνάρτηση επιστρέφει χωρίς να κάνει αποθήκευση
        if (data == null) {
            return;
        }
        //dataKind είναι ο τύπος δεδομένων (1: deaths, 2: recovered, 3:confirmed)
        short dataKind;
        if (timeSeries.equals("deaths")) {
            dataKind = 1;
        } else if (timeSeries.equals("recovered")) {
            dataKind = 2;
        } else {
            dataKind = 3;
        }
        //Για κάθε εγγραφή μέσα στα δεδομένα data
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            //Αν η τιμή της δεν αφορά όνομα χώρας/επαρχίας ή γεωγραφικό πλάτος/μήκος
            //τότε είναι εγγραφή που αφορά αθροιστικά κρούσματα (της μορφής Κλειδί (ημερομηνία) : Τιμή (πλήθος αθροιστικών κρουσμάτων))
            if (!entry.getKey().equals("Province/State")
                    && !entry.getKey().equals("Country/Region")
                    && !entry.getKey().equals("Lat")
                    && !entry.getKey().equals("Long")) {
                //date είναι αντικείμενο ημερομηνίας με βάση την ημερομηνία κλειδί
                Date date = getDateFromString(entry.getKey(), "MM/dd/yy");
                //proodQty είναι η τιμή των αθροιστικών κρουσμάτων της ημερομηνίας κλειδί
                int proodQty = entry.getValue().getAsInt();
                //qty είναι η τιμή των ημερήσιων κρουσμάτων της ημερομηνίας κλειδί
                //(την υπολογίσαμε αφαιρώντας τα κρούσματα της προηγούμενης μέρας)
                int qty = proodQty - previousDayQty;
                //ενημερώνουμε την τιμή κρουσμάτων της προηγούμενης μέρας για την επόμενη επανάληψη
                previousDayQty = proodQty;
                //Αναζητουμε αν η τρέχουσα εγγραφή που διαβάσαμε υπάρχει ήδη αποθηκευμένη στην βάση δεδομένων
                //(αρχικά δεωρούμε ότι δεν υπάρχει ήδη)
                boolean found = false;
                //Διατρέχουμε όλα τα αποθηκευμένα στοιχεία covid19
                for (Coviddata c : storedCountryData) {
                    //και αν κάποιο είναι ίδιου τύπου και της ίδιας χώρας και έχει ίδια ημερομηνία με την τρέχουσα εγγραφή
                    //τότε η εγγραφή είναι ήδη αποθηκευμένη στη βάση οπότε σταματάμε την αναζήτηση
                    if (c.getDatakind() == dataKind && c.getCountry().equals(country) && c.getTrndate().equals(date)) {
                        //και ενημερώνουμε το flag found σε true 
                        found = true;
                        break;
                    }
                }
                //Αν η εγγραφή δεν είναι ήδη αποθηκευμένη στη βάση 
                if (found != true) {
                    //Δημιουργούμε ένα νέο αντικείμενο τύπου Coviddata με τα στοιχεία του
                    //(Δίνοντας τιμή 0 στο πεδίο coviddata διότι είναι auto-incremented, άρα θα μεγαλώνει κάθε φορά κατά 1 αυτόματα) 
                    Coviddata coviddata = new Coviddata(0, date, dataKind, qty, proodQty);
                    //Προσθέτουμε το νέο αντικείμενο που δημιουργήσαμε στη λίστα coviddataList της χώρας στην οποία ανήκει
                    country.getCoviddataList().add(coviddata);
                    //θέτουμε και το πεδίο country του νέου αντικειμένου με την τιμή της χώρας στη οποία ανήκει
                    coviddata.setCountry(country);
                    //και αποθηκεύουμε το νέο αντικείμενο ως εγγραφή στον πίνακα COVIDDATA
                    em.persist(coviddata);
                }
            }
        }
    }

    

    //Query που επιστρέφει το αντικείμενο Country που αφορά την χώρα με όνομα name
    //(ή null αν δεν βρεθεί στον πινάκα COUNTRY)
    public Country selectCountryWithName(String name) {
        Query q = em.createNamedQuery("Country.findByName", Country.class);
        q.setParameter("name", name);
        try {
            Country result = (Country) q.getSingleResult();
            return result;
        } catch (NoResultException ex) {
            return null;
        }
    }

    //Query που επιστρέφει όλες τις χώρες που είναι αποθηκευμενες στον πίνακα COUNTRY
    //Τα αποτελέσματα είναι ταξινομημένα ως προς το όνομα χώρας
    public List<Country> selectAllCountries() {
        Query q = em.createQuery("SELECT c FROM Country c ORDER BY c.name");
        List<Country> result = q.getResultList();
        return result;
    }

    //Query που επιστρέφει όλα τα στοιχεία covid19 τύπου dataKind για την χώρα country
    //που είναι αποθηκευμένα στο πίνακα COVIDDATA
    //Τα αποτελέσματα είναι ταξινομημένα ως προς την ημερομηνία του κρούσματος
    public List<Coviddata> selectCovidData(Country country, short dataKind) {
        Query q = em.createQuery("SELECT c FROM Coviddata c WHERE c.country = :p1 AND c.datakind = :p2 ORDER BY c.trndate");
        q.setParameter("p1", country);
        q.setParameter("p2", dataKind);
        List<Coviddata> result = q.getResultList();
        return result;
    }

    //Query που επιστρέφει όλα τα στοιχεία covid19 τύπου dataKind για την χώρα country
    //μεταξύ των ημερομηνιών fromDate και toDate που είναι αποθηκευμένα στο πίνακα COVIDDATA
    //Τα αποτελέσματα είναι ταξινομημένα ως προς την ημερομηνία του κρούσματος
    public List<Coviddata> selectCovidData(Country country, short dataKind, Date fromDate, Date toDate) {
        Query q = em.createQuery("SELECT c FROM Coviddata c WHERE c.country = :p1 AND c.datakind = :p2 AND c.trndate >= :p3 AND c.trndate <= :p4 ORDER BY c.trndate");
        q.setParameter("p1", country);
        q.setParameter("p2", dataKind);
        q.setParameter("p3", fromDate);
        q.setParameter("p4", toDate);
        List<Coviddata> result = q.getResultList();
        return result;
    }

    //Query που επιστρέφει όλα τα στοιχεία covid19 που είναι αποθηκευμένα στο πίνακα COVIDDATA
    public List<Coviddata> selectAllCovidData() {
        Query q = em.createNamedQuery("Coviddata.findAll", Coviddata.class);
        List<Coviddata> result = q.getResultList();
        return result;
    }

    //Query που διαγράφει από τον πίνακα COVIDDATA όλα τα στοιχεία covid19 που είναι τύπου dataKind 
    public void deleteCovidDataOfKind(short dataKind) {
        em.getTransaction().begin();
        Query q = em.createQuery("DELETE FROM Coviddata c WHERE c.datakind = :p");
        q.setParameter("p", dataKind);
        q.executeUpdate();
        em.getTransaction().commit();
    }

    //Query που διαγράφει από τον πίνακα COVIDDATA όλα τα στοιχεία covid19 που αφορούν την χώρα country 
    public void deleteCovidDataOfCountry(Country country) {
        em.getTransaction().begin();
        Query q = em.createQuery("DELETE FROM Coviddata c WHERE c.country = :p");
        q.setParameter("p", country);
        q.executeUpdate();
        em.getTransaction().commit();
    }

    //Query που διαγράφει από τον πίνακα COUNTRY την χώρα με όνομα countryName 
    public void deleteCountry(String countryName) {
        Query q = em.createQuery("DELETE FROM Country c WHERE c.name = :p");
        q.setParameter("p", countryName);
        q.executeUpdate();
    }

    public String importAllCountries(String timeSeries) {
        String json = null;
        //Αποθήκευση στη βάση δεδομένων όλων των χωρών που υπάρχουν στο api 
        //και δεν υπάρχουν ήδη στη βάση μας. Τα δεδομένα ανήκουν στη χρονοσειρά (τύπο δεδομένων) timeSeries
        em.getTransaction().begin();
        //Η μέθοδος storeCountries αποθηκεύει της χώρες που βρίσκει στο api τύπου timeSeries
        //επιστέφει το json που έχει λάβει από το api
        json = Controller.getInstance().storeCountries(timeSeries);
        em.getTransaction().commit();
        return json;
    }

    public void deleteAllCountriesIfPossible() {
         em.getTransaction().begin();
            //Αν δεν υπάρχουν καθόλου δεδομένα στον πίνακα COVIDDATA
            if (selectAllCovidData().size() == 0) {
                //τότε διαγράφουμε όλες τις χώρες
                em.createQuery("delete from Country").executeUpdate();
            //Αλλιώς διαγράφουμε τις χώρες που δεν περιέχουν δεδομένα
            } else {
                //Για κάθε αποθηκευμένη χώρα
                for (Country country : selectAllCountries()) {
                    //Αν η λίστα δεδομένων της είναι κενή την διαγράφουμε από την βάση δεδομένων
                    if (country.getCoviddataList().size() == 0) {
                        deleteCountry(country.getName());
                    }
                }
            }
            em.getTransaction().commit();
    }

    public void storeCovidDataPerCountry(String timeSeries, String json) {
        //Στη συνέχεια γίνεται αποθήκευση των δεδομένων covid19 του επιλεγμένου τύπου
        //για τις χώρες που έχουν αποθηκευτεί
        em.getTransaction().begin();
        //Για κάθε χώρα που υπάρχει στη βάση δεδομένων
        for (Country country : Controller.getInstance().selectAllCountries()) {
           
            //Βρίσκουμε τα ήδη αποθηκευμένα δεδομένα της τρέχουσας χώρας
            List<Coviddata> storedCountryData = country.getCoviddataList();
            //Καλούμε την storeCountryData για να αποθηκεύσει τα δεδομένα της χώρας από το json στη βάση δεδομένων
            //Η μέθοδος λαβάνει ως παράμετρο και τα ήδη αποθηκευμένα δεδομένα (storedCountryData) της χώρας
            //ώστε να μην επιχειρήσει να τα αποθηκεύσει ξανά στη βάση
            Controller.getInstance().storeCountryData(json, country, timeSeries, storedCountryData);
        }
        em.getTransaction().commit();
    }

}
