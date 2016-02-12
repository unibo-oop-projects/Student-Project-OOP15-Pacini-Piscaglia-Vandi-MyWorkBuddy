package it.unibo.oop.myworkoutbuddy.view.handlers;

import it.unibo.oop.myworkoutbuddy.view.AccessView;
import it.unibo.oop.myworkoutbuddy.view.AppViews;
import it.unibo.oop.myworkoutbuddy.view.CreateRoutineView;
import it.unibo.oop.myworkoutbuddy.view.RegistrationView;
import it.unibo.oop.myworkoutbuddy.view.SelectRoutineView;
import it.unibo.oop.myworkoutbuddy.view.UserSettingsView;
import it.unibo.oop.myworkoutbuddy.view.ViewsObserver;
import it.unibo.oop.myworkoutbuddy.view.factory.FxWindowFactory;

/**
 * 
 * All views of the application to pass to Controller.
 *
 */
public final class ViewsHandler implements AppViews {

    private static ViewsObserver observer;

    @Override
    public AccessView getAccessView() {
        return (AccessView) FxWindowFactory.getController();
    }

    @Override
    public CreateRoutineView getCreateRoutineView() {
        return (CreateRoutineView) FxWindowFactory.getController();
    }

    @Override
    public RegistrationView getRegistrationView() {
        return (RegistrationView) FxWindowFactory.getController();
    }

    @Override
    public SelectRoutineView getSelectRoutineView() {
        return (SelectRoutineView) FxWindowFactory.getController();
    }

    @Override
    public UserSettingsView getUserSettingsView() {
        return (UserSettingsView) FxWindowFactory.getController();
    }

    @Override
    public void setViewsObserver(final ViewsObserver viewObserver) {
        observer = viewObserver;
    }

    /**
     * Allows each GUI to get Controller reference.
     * 
     * @return view
     *         observer.
     */
    public static ViewsObserver getObserver() {
        return observer;
    }

}
