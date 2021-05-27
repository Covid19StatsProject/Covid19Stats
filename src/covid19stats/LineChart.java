/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------
 * LineChartDemo1.java
 * -------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: LineChartDemo1.java,v 1.27 2004/05/27 09:10:42 mungady Exp $
 *
 * Changes
 * -------
 * 08-Apr-2002 : Version 1 (DG);
 * 30-May-2002 : Modified to display values on the chart (DG);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package covid19stats;

import static utils.Utils.getStringFromDate;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import model.Coviddata;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

//Χρησιμοποιούμε τον κώδικα που μας δίνεται (LineChartDemo1.java) για την κατασκευή του 
//διαγράμματος και κάνουμε κάποιες τροποποιήσεις
//Βάλαμε extends JFrame αντί για extends ApplicationFrame ώστε όταν κλείνει το παράθυρο του 
//γραφήματος να μην κλείνει και η εφαρμογή μας
public class LineChart extends JFrame {

    //Ο constructor δέχεται ως παραμέτρους τον τίτλο του γραφήματος, το όνομα της επιλεγμένης χώρας, τα 3 dataset της επιλεγμένης
    //χώρας, και επιπλέον 4 boolean παραμέτρους που καθορίζουν ποια δεδομενα θα χρησιμοποιηθούν στο διάγραμμα 
    public LineChart(final String title, String countryName, List<Coviddata> data1, List<Coviddata> data2, List<Coviddata> data3, 
                     boolean includeDeaths, boolean includeRecovered, boolean includeConfirmed, boolean aggregation) {
        super(title);
        //Εφόσον η παράμετρος includeDeaths είναι false δεν χρησιμοποιούμε το πρώτο dataset
        if (includeDeaths == false) {
            data1 = null;
        }
        //Εφόσον η παράμετρος includeRecovered είναι false δεν χρησιμοποιούμε το δεύτερο dataset
        if (includeRecovered == false) {
            data2 = null;
        }
        //Εφόσον η παράμετρος includeConfirmed είναι false δεν χρησιμοποιούμε το τρίτο dataset
        if (includeConfirmed == false) {
            data3 = null;
        }
        CategoryDataset dataset = createDataset(data1, data2, data3, aggregation);
        final JFreeChart chart = createChart(dataset, countryName);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }


    private CategoryDataset createDataset(List<Coviddata> data1, List<Coviddata> data2, List<Coviddata> data3, boolean aggregation) {

        //Ορίζουμε τις τρεις χρονοσειρές
        final String series1 = "Deaths";
        final String series2 = "Recovered";
        final String series3 = "Confirmed";

        //Η λίστα με τα δεδομένα
        ArrayList<String> type = new ArrayList<>();

        //Τοποθετούμε τις ημερομηνίες ελέγχοντας αρχικά από ποιο dataset θα τις πάρουμε 
        //(θα πρέπει το dataset να έχει επιλεγεί από το χρήστη) 
        if (data1 != null) {
            for (Coviddata d : data1) {
                String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
                type.add(date);
            }
        } else if (data2 != null) {
            for (Coviddata d : data2) {
                String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
                type.add(date);
            }
        } else {
            for (Coviddata d : data3) {
                String date = getStringFromDate(d.getTrndate(), "dd/MM/yy");
                type.add(date);
            }
        }

        //Δημιουργία του dataset
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();


        //Προσθέτουμε τα στοιχεία από το κάθε dataset εφόσον το dataset έχει επιλεγεί 
        //από τον χρήστη. Αν ο χρήστης δεν έχει επιλέξει σωρευτικά δεδομένα (aggregation = false)
        //βάζουμε τα ημερήσια δεδομένα, διαφορετικά βάζουμε τα σωρευτικά δεδομένα
        if (data1 != null) {
            for (int i = 0; i < data1.size(); i++) {
                if (aggregation == false) {
                    dataset.addValue(data1.get(i).getQty(), series1, type.get(i));
                } else {
                    dataset.addValue(data1.get(i).getProodqty(), series1, type.get(i));
                }
            }
        }

        if (data2 != null) {
            for (int i = 0; i < data2.size(); i++) {
                if (aggregation == false) {
                    dataset.addValue(data2.get(i).getQty(), series2, type.get(i));
                } else {
                    dataset.addValue(data2.get(i).getProodqty(), series2, type.get(i));
                }
            }
        }

        if (data3 != null) {
            for (int i = 0; i < data3.size(); i++) {
                if (aggregation == false) {
                    dataset.addValue(data3.get(i).getQty(), series3, type.get(i));
                } else {
                    dataset.addValue(data3.get(i).getProodqty(), series3, type.get(i));
                }
            }
        }
        return dataset;

    }


    private JFreeChart createChart(final CategoryDataset dataset, String countryName) {

        // Δημιουργία του διαγράμματος
        final JFreeChart chart = ChartFactory.createLineChart(
                countryName + " - Στοιχεία Covid19",    // Τίτλος διαγράμματος
                "Ημερομηνία",                           // Τίτλος οριζόντιου άξονα
                "Πλήθος κρουσμάτων",                    // Τίτλος κατακόρυφου άξονα
                dataset,                                // data
                PlotOrientation.VERTICAL,               // orientation
                true,                                   // include legend
                true,                                   // tooltips
                false                                   // urls
        );

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);

        // customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);

        // ****************************************************************************
        // * JFREECHART DEVELOPER GUIDE                                               *
        // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
        // * to purchase from Object Refinery Limited:                                *
        // *                                                                          *
        // * http://www.object-refinery.com/jfreechart/guide.html                     *
        // *                                                                          *
        // * Sales are used to provide funding for the JFreeChart project - please    * 
        // * support us so that we can continue developing free software.             *
        // ****************************************************************************
        
        // customise the renderer...
        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();


        renderer.setSeriesStroke(
                0, new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[]{10.0f, 6.0f}, 0.0f
                )
        );
        renderer.setSeriesStroke(
                1, new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[]{6.0f, 6.0f}, 0.0f
                )
        );
        renderer.setSeriesStroke(
                2, new BasicStroke(
                        2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1.0f, new float[]{2.0f, 6.0f}, 0.0f
                )
        );
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;
    }

}
