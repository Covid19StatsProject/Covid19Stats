package covid19stats;

import view.MainForm;

public class Covid19Stats {

    //Η βασική Φόρμα
    public static MainForm mainForm;

    //Συνδέεται στην διεύθυνση urlToCall, αντλεί τα δεδομένα JSON από το API
    //και τα επιστρέφει σε ένα String
    

    //Το κυρίως πρόγραμμα της εφαρμογής
    public static void main(String[] args) {
        //Δημιουργία της βασικής φόρμας τύπου ΜainForm
        mainForm = new MainForm();
        mainForm.setTitle("Αρχική Οθόνη");
        //Κεντράρισμα της φόρμας και εμφάνιση
        mainForm.setLocationRelativeTo(null); 
        mainForm.setVisible(true);
    }
}
