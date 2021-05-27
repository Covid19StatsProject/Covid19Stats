package view;

import controller.Controller;
import static covid19stats.Covid19Stats.mainForm;
import static utils.Utils.getStringFromDate;
import static utils.Utils.getDateFromString;
import covid19stats.GoogleMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import model.Country;
import model.Coviddata;

//Η φόρμα για την επιλογή χωρών που θα προβληθούν στο χάρτη
public class GoogleMapForm extends javax.swing.JFrame {
    //Η βασική χώρα που επιλέγεται από το comboBox1
    private Country mainCountry;
    //Το μοντέλο της λίστας για επιλογή επιπλέον χωρών
    private DefaultListModel listModel;
    //Η λίστα με τις επιπλέον χώρες που έχουν επιλεγεί
    private List<Country> otherCountries = new ArrayList<>();
    //Το εύρος ημερομηνιών που έχουν επιλεγεί στα comboBox2, comboBox3
    private Date fromDate, toDate;

    public GoogleMapForm() {
        initComponents();
        
        //Ενημέρωση των κειμένων των ετικετών και των πλήκτρων
        jLabel1.setText("Επιλογή βασικής χώρας:");
        jLabel2.setText("Επιλογή επιπλέον χωρών:");
        jLabel3.setText("Από ημερομηνία:");
        jLabel4.setText("Έως ημερομηνία:");
        jButton1.setText("Προβολή σε χάρτη");
        jButton2.setText("Επιστροφή");
        //Δημιουργία του μοντέλου της λίστας επιλογής άλλων χωρών
        listModel = new DefaultListModel();
        jList1.setModel(listModel);
        
        //Οι χώρες που υπάρχουν στη βάση δεδομένων
        List<Country> countries = Controller.getInstance().selectAllCountries();
        //Προσθήκη όλων των χωρών στο comboBox1 και στην λίστα jList1
        for (Country c : countries) {
            jComboBox1.addItem(c.getName());
            listModel.addElement(c.getName());
        }
        //Αρχικά δεν επιλέγονται χώρες
        jComboBox1.setSelectedIndex(-1);
        jList1.setSelectedIndex(-1);
        //Προσθήκη τριών listeners για τα τρία comboBox (επιλεγμένης χώρας και ημερομηνιών)
        addListeners();
    }


    //Προσθέτει τους listeners στα jComboBox
    public void addListeners() {

        //Ο listener για το jComboBox1 (ενεργοποιείται όταν επιλέγεται μία χώρα στο jComboBox1) 
        //Ο listener ενημερώνει κατάλληλα το πεδίο mainCounrty της φόρμας με την βασική χώρα που έχει επιλεγεί
        //και εμφανίζει τα στοιχεία της στην φόρμα 
        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Εφόσον έχει επιλεγεί βασική χώρα
                if (jComboBox1.getSelectedIndex() != -1) {
                    //Ενημέρωση του πεδίου mainCountry
                    String countryName = jComboBox1.getSelectedItem().toString();
                    mainCountry = Controller.getInstance().selectCountryWithName(countryName);
                    //Ενημέρωση των λιστών data1, data2, data3 με τα τρία dataset της χώρας
                    List<Coviddata> data1, data2, data3;
                    data1 = Controller.getInstance().selectCovidData(mainCountry, (short) 1);
                    data2 = Controller.getInstance().selectCovidData(mainCountry, (short) 2);
                    data3 = Controller.getInstance().selectCovidData(mainCountry, (short) 3);
                    
                    //Παίρνουμε τα δεδομένα από κάποιο μη κενό dataset
                    //και τα βάζουμε στη λίστα data
                    //(εάν είναι και τα τρία dataset κενά η λίστα data θα είναι κενή) 
                    List<Coviddata> data;
                    if (data1.size() != 0) {
                        data = data1;
                    } else if (data2.size() != 0) {
                        data = data2;
                    } else {
                        data = data3;
                    }
                   
                    //Εφόσον δεν υπάρχουν ήδη ημερομηνίες στο δεύτερο comboBox (με τις ημερομηνίες-από)
                    //παίρνουμε τις ημερομηνίες από τη λίστα data και τις προσθέτουμε στο comboBox
                    if (jComboBox2.getItemCount() == 0) {
                        if (data.size() != 0) {
                            for (Coviddata d : data) {
                                String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
                                jComboBox2.addItem(date);
                            }
                            //Θέτουμε ως ημερομηνία-από την πρώτη ημερομηνία που υπάρχει
                            jComboBox2.setSelectedIndex(0);
                        }
                    }
                    //Εφόσον δεν υπάρχουν ήδη ημερομηνίες στο τρίτο comboBox (με τις ημερομηνίες-έως)
                    //παίρνουμε τις ημερομηνίες από τη λίστα data και τις προσθέτουμε στο comboBox
                    if (jComboBox3.getItemCount() == 0) {
                        if (data.size() != 0) {
                            for (Coviddata d : data) {
                                String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
                                jComboBox3.addItem(date);
                            }
                            //Θέτουμε ως ημερομηνία-έως την τελευταία ημερομηνία που υπάρχει 
                            jComboBox3.setSelectedIndex(jComboBox3.getItemCount() - 1);
                        }
                    }
                }
            }
        });

        //Ο listener για το jComboBox2 (ενεργοποιείται όταν επιλέγεται μία ημερομηνία-από στο jComboBox2) 
        //Eνημερώνει το πεδίο fromDate 
        jComboBox2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox2.getSelectedIndex() != -1) {
                    fromDate = getDateFromString(jComboBox2.getSelectedItem().toString(), "dd/MM/yy");
                    //Μήνυμα αν η ημερομηνία-από είναι μεγαλύτερη της ημερομηνίας-έως
                    if (toDate != null && fromDate.after(toDate)) {
                        JOptionPane.showMessageDialog(null, "Λάθος εύρος ημερομηνιών", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        //Ο listener για το jComboBox3 (ενεργοποιείται όταν επιλέγεται μία ημερομηνία-έως στο jComboBox3) 
        //Eνημερώνει το πεδίο toDate 
        jComboBox3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox3.getSelectedIndex() != -1) {
                    toDate = getDateFromString(jComboBox3.getSelectedItem().toString(), "dd/MM/yy");
                    //Μήνυμα αν η ημερομηνία-από είναι μεγαλύτερη της ημερομηνίας-έως
                    if (fromDate != null && fromDate.after(toDate)) {
                        JOptionPane.showMessageDialog(null, "Λάθος εύρος ημερομηνιών", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("jLabel1");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = {};
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jLabel2.setText("jLabel2");

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("jButton2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));

        jLabel3.setText("jLabel3");

        jLabel4.setText("jLabel4");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
            .addGroup(layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addGap(8, 8, 8)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(36, 36, 36))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Πάτημα πλήκτρου "Επιστροφή"
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Απόκρυψη της φόρμας προβολής σε χάρτη
        this.setVisible(false);
        //Εμφάνιση της βασικής φόρμας
        mainForm.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    //Πάτημα πλήκτρου "Προβολή σε χάρτη"
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Μήνυμα αν δεν έχει επιλεγεί βασική χώρα
        if (mainCountry == null) { 
            JOptionPane.showMessageDialog(null, "Πρέπει να επιλέξετε βασική χώρα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
        //Αλλιώς εμφάνιση χάρτη για τις επιλεγμένες χώρες
        } else {    
            //Τα ονόματα των επιπλέον χωρών που έχουν επιλεγεί στη λίστα jList1
            List<String> otherCountriesNames = jList1.getSelectedValuesList();
            //Η λίστα με τις επιπλέον χώρες
            otherCountries = new ArrayList<>();
            //Κάθε χώρα που έχει επιλεγεί στη λίστα jList1 προστίθεται στη λίστα με τις επιπλέον χώρες
            for (String name : otherCountriesNames) {
                Country country = Controller.getInstance().selectCountryWithName(name);
                //Έλεγχος πριν προστεθεί αν είναι ίδια με την βασική ή αν υπάρχει ήδη στη λίστα
                if (country != mainCountry && !otherCountries.contains(country)) {
                    otherCountries.add(country);
                }
            }
            //Δημιουργία χάρτη με βασική χώρα country, επιπλέον χώρες otherCountries, για τις επιλεγμένες ημερομηνίες
            GoogleMap googleMap = new GoogleMap("mappage.html", mainCountry, otherCountries, fromDate, toDate);
            //Δημιουργία html αρχείου
            googleMap.createHtmlFile();
            //Εμφάνιση χάρτη
            googleMap.showMap();
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
