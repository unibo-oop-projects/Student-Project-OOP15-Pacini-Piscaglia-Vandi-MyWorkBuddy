package it.unibo.oop.myworkoutbuddy.view.handlers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

/**
 * Handler of the accessView. It show user statistics fetched from the database.
 */
public final class StatisticsHandler {

    @FXML
    private LineChart<String, String> weightChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private void viewPie() {
        pieChart.setLabelLineLength(10);
        pieChart.setLegendSide(Side.LEFT);
        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Chest", 13),
                new PieChart.Data("Legs", 25),
                new PieChart.Data("Arms", 10),
                new PieChart.Data("Shoulders", 22),
                new PieChart.Data("Crunch", 30)));
    }

    @FXML
    private void viewWeightChart() {
        final Series<String, String> series = new XYChart.Series<>();
        series.setName("Serie");
        series.getData().add(new XYChart.Data("Jan", 23));
        series.getData().add(new XYChart.Data("Feb", 14));
        series.getData().add(new XYChart.Data("Mar", 15));
        series.getData().add(new XYChart.Data("Apr", 24));
        series.getData().add(new XYChart.Data("May", 34));
        series.getData().add(new XYChart.Data("Jun", 36));
        series.getData().add(new XYChart.Data("Jul", 22));
        series.getData().add(new XYChart.Data("Aug", 45));
        series.getData().add(new XYChart.Data("Sep", 43));
        series.getData().add(new XYChart.Data("Oct", 17));
        series.getData().add(new XYChart.Data("Nov", 29));
        series.getData().add(new XYChart.Data("Dec", 25));
        weightChart.getData().add(series);
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     */
    public void initialize() {
        viewPie();
        viewWeightChart();
    }

}
