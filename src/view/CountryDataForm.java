package view;


import controller.Controller;
import static covid19stats.Covid19Stats.mainForm;

import static utils.Utils.getStringFromDate;
import static utils.Utils.getDateFromString;
import covid19stats.GoogleMap;
import covid19stats.LineChart;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Country;
import model.Coviddata;
import org.jfree.ui.RefineryUtilities;

public class CountryDataForm extends javax.swing.JFrame {

    //Τα μοντέλα των τριών πινάκων που χρησιμοποιούμε
    private DefaultTableModel tableModel1, tableModel2, tableModel3;
    //Η επιλεγμένη χώρα
    private Country country;
    //Τα δεδομένα των τριών τύπων για την επιλεγμένη χώρα(datasets)
    private List<Coviddata> data1 = new ArrayList<>();
    private List<Coviddata> data2 = new ArrayList<>();
    private List<Coviddata> data3 = new ArrayList<>();
    //Οι επιλεγμένες ημερομηνίες (από, έως)
    private Date fromDate, toDate;

    public CountryDataForm() {
        initComponents();

        //Προσθήκη κειμένων στις ετικέτες
        jLabel1.setText("Επιλογή Χώρας:");
        jLabel2.setText("Από ημερομηνία:");
        jLabel3.setText("Έως ημερομηνία:");
        jLabel4.setText("Επιλογή δεδομένων διαγράμματος:");

        //Προσθήκη κειμένων στις καρτέλες(tabs)
        jTabbedPane1.setTitleAt(0, "Θάνατοι");
        jTabbedPane1.setTitleAt(1, "Ασθενείς που έχουν ανακάμψει");
        jTabbedPane1.setTitleAt(2, "Επιβεβαιωμένα κρούσματα");

        //Προσθήκη κειμένου στο check box
        jCheckBox1.setText("Σωρευτικά Δεδομένα");
        //Αρχικά απενεργοποιημένο
        jCheckBox1.setSelected(false);

        //Προσθήκη κειμένων στα κουμπιά
        jButton1.setText("Προβολή σε διάγραμμα");
        jButton2.setText("Προβολή σε χάρτη");
        jButton3.setText("Διαγραφή δεδομένων");
        jButton4.setText("Επιστροφή");

        //Προσθήκη χωρών στο πρώτο comboBox
        List<Country> countries = Controller.getInstance().selectAllCountries();
        for (Country c : countries) {
            jComboBox1.addItem(c.getName());
        }
        //Αρχικά δεν έχει επιλεγεί χώρα στο combo box
        jComboBox1.setSelectedIndex(-1);

        //Προσθήκη στοιχείων στο comboBox που αφορά τα δεδομένα που 
        //χρησιμοποιούνται στο διάγραμμα
        jComboBox4.addItem("Όλα");
        jComboBox4.addItem("Θάνατοι");
        jComboBox4.addItem("Ασθενείς που έχουν ανακάμψει");
        jComboBox4.addItem("Επιβεβαιωμένα κρούσματα");

        //Δημιουργία και προετοιμασία του μοντέλου του jTable1
        tableModel1 = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; //ώστε να μην είναι editable
            }
        };

        //Δημιουργία και προετοιμασία του μοντέλου του jTable2
        tableModel2 = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; //ώστε να μην είναι editable
            }
        };

        //Δημιουργία και προετοιμασία του μοντέλου του jTable3
        tableModel3 = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; //ώστε να μην είναι editable
            }
        };

        //Προσθήκη στηλών στο μοντέλο του πίνακα του πρώτου tab (Θάνατοι)
        tableModel1.addColumn("Ημερομηνία");
        tableModel1.addColumn("Θάνατοι");
        tableModel1.addColumn("Θάνατοι αθροιστικά");

        //Προσθήκη στηλών στο μοντέλο του πίνακα του δεύτερου tab (ασθενείς που έχουν ανακάμψει)
        tableModel2.addColumn("Ημερομηνία");
        tableModel2.addColumn("Ασθενείς που έχουν ανακάμψει");
        tableModel2.addColumn("Ασθενείς που έχουν ανακάμψει αθροιστικά");

        //Προσθήκη στηλών στο μοντέλο του πίνακα του τρίτου tab (επιβεβαιωμένα κρούσματα)
        tableModel3.addColumn("Ημερομηνία");
        tableModel3.addColumn("Επιβεβαιωμένα κρούσματα");
        tableModel3.addColumn("Επιβεβαιωμένα κρούσματα αθροιστικά");

        //Ορισμός του μοντέλου του κάθε πίνακα
        jTable1.setModel(tableModel1);
        jTable2.setModel(tableModel2);
        jTable3.setModel(tableModel3);

        //Προσθήκη τριών listeners για τα τρία comboBox (επιλεγμένης χώρας και ημερομηνιών)
        addListeners();
    }

    //Η μέθοδος προσθέτει στα comboBox των ημερομηνιών τις ημερομηνίες που βρίσκει μέσα
    //στα δεδομένα της επιλεγμένης χώρας
    public void addDates() {
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

    //Η μέθοδος ενημερώνει τις τρεις λίστες δεδομένων (datasets) με τα δεδομένα της
    //επιλεγμένης χώρας. Εφόσον υπάρχει καθορισμένο εύρος ημερομηνιών τα δεδομένα είναι
    //φιλτραρισμένα με βάση το εύρος
    public void updateData() {
        //Βρίσκουμε την επιλεγμένη χώρα
        String countryName = jComboBox1.getSelectedItem().toString();
        country = Controller.getInstance().selectCountryWithName(countryName);

        //Εφόσον υπάρχει επιλεγμένη ημερομηνία-από, ενημερώνουμε το πεδίο fromDate
        if (jComboBox2.getSelectedIndex() != -1) {
            fromDate = getDateFromString(jComboBox2.getSelectedItem().toString(), "dd/MM/yy");
        }
        //Εφόσον υπάρχει επιλεγμένη ημερομηνία-έως, ενημερώνουμε το πεδίο toDate
        if (jComboBox3.getSelectedIndex() != -1) {
            toDate = getDateFromString(jComboBox3.getSelectedItem().toString(), "dd/MM/yy");
        }
        //Εφόσον υπάρχει επιλεγμένο εύρος ημερομηνιών παίρνουμε τα δεδομένα για τις συγκεκριμένες ημερομηνίες
        if (fromDate != null && toDate != null) {
            //Εάν η ημερομηνία-από είναι μεγαλύτερη της ημερομηνίας-έως, εμφανίζεται κατάλληλο μήνυμα
            if (fromDate.after(toDate)) {
                JOptionPane.showMessageDialog(null, "Λάθος εύρος ημερομηνιών", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
            }
            data1 = Controller.getInstance().selectCovidData(country, (short) 1, fromDate, toDate);
            data2 = Controller.getInstance().selectCovidData(country, (short) 2, fromDate, toDate);
            data3 = Controller.getInstance().selectCovidData(country, (short) 3, fromDate, toDate);
        //Διαφορετικά παίρνουμε όλα τα δεδομένα 
        } else {
            data1 = Controller.getInstance().selectCovidData(country, (short) 1);
            data2 = Controller.getInstance().selectCovidData(country, (short) 2);
            data3 = Controller.getInstance().selectCovidData(country, (short) 3);
        }
    }

    //Η μέθοδος ενημερώνει τα μοντέλα των τριών πινάκων ώστε να προβάλονται στις
    //τρεις καρτέλες τα σωστά δεδομένα
    //Επίσης ενημερώνει τα comboBox2, comboBox3 με τις ημερομηνίες που παίρνει από 
    //την επιλεγμένη χώρα
    public void updateTableModels() {
        //Προσθέτουμε τις ημερομηνίες στα comboBox (τις παίρνουμε από την επιλεγμένη χώρα)
        addDates(); 
        //Αδειάζουμε τα μοντέλα των τριών πινάκων
        tableModel1.setRowCount(0);
        tableModel2.setRowCount(0);
        tableModel3.setRowCount(0);

        //Και γεμίζουμε το καθένα με τα δεδομένα από την αντίστοιχη λίστα δεδομένων(dataset)
        //(δημιουργούμε συμβολοσειρές με την μορφοποίηση "dd/MM/yy" για τις ημερομηνίες)
        for (Coviddata d : data1) {
            String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
            tableModel1.addRow(new Object[]{date, d.getQty(), d.getProodqty()});
        }
        for (Coviddata d : data2) {
            String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
            tableModel2.addRow(new Object[]{date, d.getQty(), d.getProodqty()});
        }
        for (Coviddata d : data3) {
            String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
            tableModel3.addRow(new Object[]{date, d.getQty(), d.getProodqty()});
        } 
 
    }

    //Προσθέτει τους listeners στα jComboBox
    public void addListeners() {

        //Ο listener για το jComboBox1 (ενεργοποιείται όταν επιλέγεται μία χώρα στο jComboBox1) 
        //Kαλείται η μέθοδος updateData για να ενημερωθούν τα πεδία country, fromDate, toDate, data1, data2, data3 
        //Καλείται η μέθοδος updateTableModels για να ενημερωθούν τα μοντέλα των πινάκων και να εμφανιστούν τα
        //σωστά δεδομένα σε αυτούς 
        jComboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox1.getSelectedIndex() != -1) {
                    updateData();
                    updateTableModels();
                }
            }
        });

        //Ο listener για το jComboBox2 (ενεργοποιείται όταν επιλέγεται μία ημερομηνία-από στο jComboBox2) 
        //Αντίστοιχα ενημερώνονται τα πεδία και τα δεδομένα των πινάκων
        jComboBox2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox2.getSelectedIndex() != -1) {
                    updateData();
                    updateTableModels();
                }
            }
        });

        //Ο listener για το jComboBox3 (ενεργοποιείται όταν επιλέγεται μία ημερομηνία-έως στο jComboBox3) 
        //Αντίστοιχα ενημερώνονται τα πεδία και τα δεδομένα των πινάκων
        jComboBox3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox3.getSelectedIndex() != -1) {
                    updateData();
                    updateTableModels();
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

        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jComboBox4 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));

        jLabel1.setText("jLabel1");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {}
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", jPanel1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {}
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab2", jPanel2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {}
        ));
        jScrollPane3.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab3", jPanel3);

        jLabel2.setText("jLabel2");

        jLabel3.setText("jLabel3");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("jCheckBox4");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));

        jLabel4.setText("jLabel4");

        jButton2.setText("jButton2");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("jButton3");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("jButton4");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 790, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1)
                    .addComponent(jButton1))
                .addGap(26, 26, 26)
                .addComponent(jButton2)
                .addGap(26, 26, 26)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(21, 21, 21))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("tab2");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Πάτημα πλήκτρου "Προβολή σε διάγραμμα"
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Μήνυμα αν δεν έχει επιλεγεί χώρα
        if (country == null) {
            JOptionPane.showMessageDialog(null, "Πρέπει να επιλέξετε χώρα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
            //Μήνυμα αν η χώρα δεν περιέχει δεδομένα
        } else if (country.getCoviddataList().size() == 0) {
            JOptionPane.showMessageDialog(null, "Η χώρα " + country.getName() + " δεν περιέχει δεδομένα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
            //Αλλιώς εμφάνιση διαγράμματος για την επιλεγμένη χώρα 
        } else {
            //Το όνομα της επιλεγμένης χώρας μπαίνει στον τίτλο του διαγράμματος
            String countryName = jComboBox1.getSelectedItem().toString();
            String chartTitle = countryName + " - Στοιχεία Covid19";
            //Βρίσκουμε ποια δεδομένα πρέπει να συμπεριληφθούν στο διάγραμμα ανάλογα με το
            //επιλεγμένο στοιχείο του comboBox4
            //Αρχικά θα περιληφθούν όλα τα δεδομένα 
            boolean includeDeaths = true;
            boolean includeRecovered = true;
            boolean includeConfirmed = true;
            //Αν έχει επιλεγεί η δεύτερη επιλογή θα περιληφθούν μόνο στοιχεία θανάτων
            if (jComboBox4.getSelectedIndex() == 1) {
                includeRecovered = false;
                includeConfirmed = false;
            }
            //Αν έχει επιλεγεί η τρίτη επιλογή θα περιληφθούν μόνο στοιχεία ασθενών που έχουν ανακάμψει
            if (jComboBox4.getSelectedIndex() == 2) {
                includeDeaths = false;
                includeConfirmed = false;
            }
            //Αν έχει επιλεγεί η τεταρτη επιλογή θα περιληφθούν μόνο στοιχεία επιβεβαιωμένων κρουσμάτων
            if (jComboBox4.getSelectedIndex() == 3) {
                includeDeaths = false;
                includeRecovered = false;
            }
            //Αν έχει επιλεγεί το checkBox θα περιληφθούν σωρευτικά δεδομένα
            boolean aggregation = jCheckBox1.isSelected();
            //Δημιουργία του διαγράμματος με βάση τις παραπάνω επιλογές
            final LineChart chart = new LineChart(chartTitle, countryName, data1, data2, data3,
                    includeDeaths, includeRecovered, includeConfirmed, aggregation);
            //Εμφάνιση διαγράμματος
            chart.pack();
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    //Πάτημα πλήκτρου "Προβολή σε χάρτη"
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Μήνυμα αν δεν έχει επιλεγεί χώρα
        if (country == null) {
            JOptionPane.showMessageDialog(null, "Πρέπει να επιλέξετε χώρα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
        //Μήνυμα αν η χώρα δεν περιέχει δεδομένα
        } else if (country.getCoviddataList().size() == 0) {
            JOptionPane.showMessageDialog(null, "Η χώρα " + country.getName() + " δεν περιέχει δεδομένα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
        //Αλλιώς εμφάνιση χάρτη για την επιλεγμένη χώρα
        } else {
            //Δημιουργία χάρτη με βασική χώρα country, χωρίς επιπλέον χώρες, για τις επιλεγμένες ημερομηνίες
            GoogleMap googleMap = new GoogleMap("mappage.html", country, null, fromDate, toDate);
            //Δημιουργία html αρχείου
            googleMap.createHtmlFile();
            //Εμφάνιση χάρτη 
            googleMap.showMap();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    //Πάτημα πλήκτρου "Διαγραφή δεδομένων"
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        //Μήνυμα αν δεν έχει επιλεγεί χώρα
        if (country == null) {
            JOptionPane.showMessageDialog(null, "Πρέπει να επιλέξετε χώρα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
            //Μήνυμα αν η χώρα δεν περιέχει δεδομένα
        } else if (country.getCoviddataList().size() == 0) {
            JOptionPane.showMessageDialog(null, "Η χώρα " + country.getName() + " δεν περιέχει δεδομένα", "Μήνυμα", JOptionPane.WARNING_MESSAGE);
        } else {
            //Εμφάνιση πλαισίου διαλόγου με ερώτηση για διαγραφή δεδομένων
            int answer = JOptionPane.showConfirmDialog(null, "Διαγραφή όλων των δεδομένων της χώρας " + country.getName() + ";", "Διαγραφή δεδομένων χώρας " + country.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            //Αν πατηθεί το πλήκτρο OK
            if (answer == JOptionPane.OK_OPTION) {
                //Η χώρα της οποίας τα δεδομένα θα διαγραφούν
                Country c = Controller.getInstance().selectCountryWithName(country.getName());
                //Διαγραφή της λίστας με τα δεδομένα της χώρας
                if (c != null) {
                    c.getCoviddataList().clear();
                }
                //Διαγραφή των δεδομένων της χώρας από τη βάση δεδομένων
                Controller.getInstance().deleteCovidDataOfCountry(country);
                //Ενημέρωση πεδίων και των μοντέλων των πινάκων
                updateData();
                updateTableModels();
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    //Πάτημα πλήκτρου "Επιστροφή"
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        //Απόκρυψη της φόρμας της χώρας
        this.setVisible(false);
        //Εμφάνιση της βασικής φόρμας
        mainForm.setVisible(true);

    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    // End of variables declaration//GEN-END:variables
}
