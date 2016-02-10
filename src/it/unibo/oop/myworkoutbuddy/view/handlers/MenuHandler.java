package it.unibo.oop.myworkoutbuddy.view.handlers;

import it.unibo.oop.myworkoutbuddy.view.factory.FxWindowFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * Handler of the view menu, it allows user to switch view.
 *
 */
public class MenuHandler {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Button btnSelect;

    @FXML
    private Button btnStatistics;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnCreate;

    @FXML
    private Label menuTitle;

    private Button lastPressed;

    private final static double WIDTH_BUTTON_ANIMATION = 135.0;

    private final String cssSelectStyle = "-fx-background-color: yellow; -fx-font: bold 10pt 'Serif';";

    /*
     * private final EventHandler<ActionEvent> mouseAnimationHandler = i -> {
     * Button btn = (Button) i.getSource();
     * btn.setStyle("-fx-font-weight: bold");
     * btn.setMaxWidth(170.0);
     * };
     */

    /**
     * Set createRoutine view in the menu center.
     */
    @FXML
    private void setCreateRoutineView() {
        sceneSwitch("CreateRoutine.fxml", btnCreate, "Create your routine");
    }

    /**
     * Set selectRoutine view in the menu center.
     */
    @FXML
    private void setSelectRoutineView() {
        sceneSwitch("SelectRoutine.fxml", btnSelect, "Select your routine");
    }

    /**
     * Set user statistics view in the menu center.
     */
    @FXML
    private void setStatisticsView() {
        sceneSwitch("Statistics.fxml", btnStatistics, "Statistics");
    }

    /**
     * Set user settings view in the menu center.
     */
    @FXML
    private void setUserSettingsView() {
        sceneSwitch("UserSettings.fxml", btnSettings, "User Settings");
    }

    /**
     * Terminates application.
     */
    @FXML
    private void quitApp() {
        System.exit(0);
    }

    /**
     * Return to login view.
     */
    @FXML
    private void logout() {
        new FxWindowFactory().openWindow("Access.fxml", false);
        final Stage stageAccess = (Stage) menuTitle.getScene().getWindow();
        stageAccess.close();
    }

    /**
     * Animation when user move mouse on a button
     */
    @FXML
    private void moveMouseAnimation() {
    }

    /**
     * Animation when user move mouse on a button
     */
    @FXML
    private void exitMouseAnimation() {
        btnCreate.setStyle("-fx-font: 13px 'Serif'; -fx-padding: 10;");
        btnCreate.setMaxWidth(WIDTH_BUTTON_ANIMATION);
    }

    /**
     * Set the initial style.
     */
    private void resetButtonStyle() {
        if (lastPressed != null) {
            lastPressed.setStyle("-fx-font: 13px 'Serif'; -fx-padding: 10;");
        }
    }

    /**
     * Factored code of scene switch and button management.
     */
    private void sceneSwitch(final String fxmlFile, final Button btnPressed, final String menuLabel) {
        if (lastPressed != btnPressed) {
            mainPane.setCenter(new FxWindowFactory().openWindow(fxmlFile, true));
            menuTitle.setText(menuLabel);
            btnPressed.setStyle(cssSelectStyle);
            resetButtonStyle();
            lastPressed = btnPressed;
        }
    }
}
