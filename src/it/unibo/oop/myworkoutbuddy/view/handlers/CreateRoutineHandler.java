package it.unibo.oop.myworkoutbuddy.view.handlers;

import java.util.Set;

import it.unibo.oop.myworkoutbuddy.view.CreateRoutineView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

/**
 * 
 * Handler of the CreateRoutineView. It collects user routine.
 */
public class CreateRoutineHandler implements CreateRoutineView {

    @FXML
    private Button btnInsertExercise;

    @FXML
    private AnchorPane anchorEx;

    @FXML
    private Button btnSaveRoutine;

    @Override
    public Set<String> getRoutine() {
        return null;
    }

}
