package it.unibo.oop.myworkoutbuddy.controller;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.validator.routines.EmailValidator;

import it.unibo.oop.myworkoutbuddy.controller.db.MongoService;
import it.unibo.oop.myworkoutbuddy.model.MyWorkoutBuddyModel;
import it.unibo.oop.myworkoutbuddy.view.AppViews;
import it.unibo.oop.myworkoutbuddy.view.ViewsObserver;

/**
 * Controller.
 */
public class Controller implements ViewsObserver {

    private final MyWorkoutBuddyModel model;
    private final AppViews views;

    // TODO: remove
    private Optional<String> currentUser;

    /**
     * Constructs a new controller instance.
     * 
     * @param model
     *            The reference to the model
     * @param views
     *            The refernce to the views
     */
    public Controller(final MyWorkoutBuddyModel model, final AppViews views) {
        this.model = requireNonNull(model);
        this.views = requireNonNull(views);
        views.setViewsObserver(this);
        currentUser = Optional.empty();
    }

    @Override
    public List<String> loginUser() {
        checkIfUserIsNotLoggedIn();
        final String username = views.getAccessView().getUsername();
        final String password = views.getAccessView().getPassword();
        final Optional<Map<String, Object>> userData = getUserData(username);
        if (!userData.isPresent()) {
            return Arrays.asList(username + " does not exist");
        }
        final Validator validator = new Validator()
                .addValidation(p -> userData.get().get("password").equals(p), password, "Invalid password");
        validator.validate();
        // Login the user
        validator.ifValid(() -> currentUser = Optional.of(username));
        return validator.getErrorMessages();
    }

    @Override
    public List<String> registerUser() {
        checkIfUserIsNotLoggedIn();
        // Fields to validate
        final String username = views.getRegistrationView().getUsername();
        final String password = views.getRegistrationView().getPassword();
        final String passwordConfirm = views.getRegistrationView().getPasswordConfirm();
        final String firstName = views.getRegistrationView().getName();
        final String lastName = views.getRegistrationView().getSurname();
        final String email = views.getRegistrationView().getEmail();
        final int age = views.getRegistrationView().getAge();
        final int height = views.getRegistrationView().getHeight();
        final double weight = views.getRegistrationView().getWeight();

        final Validator validator = new Validator()
                .addValidation(usernameValidator(), username, "Invalid username")
                .addValidation(usernameAlreadyTakenValidator(), username, "Username already taken")
                .addValidation(nameValidator(), firstName, "Invalid first name")
                .addValidation(nameValidator(), lastName, "Invalid last name")
                .addValidation(numberValidator(), age, "Invalid age")
                .addValidation(numberValidator(), weight, "Invalid weight")
                .addValidation(numberValidator(), height, "Invalid height")
                .addValidation(passwordValidator(), password, "Invalid password")
                .addValidation(passwordConfirmValidator(passwordConfirm), password, "The two passwords do not match")
                .addValidation(emailValidator(), email, "Invalid email or email already taken")
                .addValidation(emailAlreadyTakenValidator(), email, "Email already taken");
        validator.validate();
        validator.ifValid(() -> {
            // Add the new user in the database
            final Map<String, Object> newUser = new HashMap<>();
            newUser.put("username", username);
            newUser.put("password", password);
            newUser.put("name", firstName);
            newUser.put("surname", lastName);
            newUser.put("email", email);
            newUser.put("height", height);
            newUser.put("weight", weight);
            newUser.put("age", age);
            newUser.put("favouriteTheme", "");
            getService(ServiceProvider.USERS).create(newUser);
        });
        return validator.getErrorMessages();
    }

    @Override
    public void logoutUser() {
        currentUser = Optional.empty();
    }

    @Override
    public String getFavouriteTheme() {
        return getService(ServiceProvider.USERS)
                .getOneByParams(usernameAsQueryParam(currentUser.get()))
                .map(m -> (String) m.get("favouriteTheme"))
                .orElseGet(String::new);
    }

    @Override
    public void setFavouriteTheme(final String selectedTheme) {
        final Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("favouriteTheme", requireNonNull(selectedTheme));
        getService(ServiceProvider.USERS)
                .updateByParams(usernameAsQueryParam(currentUser.get()), updateParams);
    }

    @Override
    public Map<String, Set<String>> getExercises() {
        final Map<String, Set<String>> exercises = new HashMap<>();
        getService(ServiceProvider.EXERCISES).getAll().forEach(m -> {
            final Set<String> s = new HashSet<>();
            s.add((String) m.get("name"));
            exercises.merge((String) m.get("mainTarget"), s, (o, n) -> {
                o.addAll(n);
                return o;
            });
        });
        return exercises;
    }

    @Override
    public boolean saveRoutine() {
        final Map<String, Object> routine = new HashMap<>();
        final Service routines = getService(ServiceProvider.ROUTINES);
        routine.put("username", currentUser.get());
        routine.put("routineIndex", routines.getAll().stream()
                .mapToInt(m -> (int) m.get("routineIndex"))
                .max()
                .orElse(0) + 1);
        routine.put("description", views.getCreateRoutineView().getRoutineDescription());
        final List<Map<String, Object>> workouts = views.getCreateRoutineView()
                .getRoutine().entrySet().stream()
                .map(w -> {
                    final Map<String, Object> workout = new HashMap<>();
                    workout.put("name", w.getKey());
                    workout.put("exercises", w.getValue().entrySet().stream()
                            .map(e -> {
                        final Map<String, Object> exercise = new HashMap<>();
                        exercise.put("exerciseName", e.getKey());
                        exercise.put("repetitions", e.getValue());
                        return exercise;
                    })
                            .collect(Collectors.toList()));
                    return workout;
                })
                .collect(Collectors.toList());
        routine.put("workouts", workouts);
        return routines.create(routine);
    }

    @Override
    public Map<String, String> getExerciseInfo(final String exerciseName) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", exerciseName);
        final Map<String, String> exerciseInfo = new HashMap<>();
        getService(ServiceProvider.EXERCISES)
                .getOneByParams(params)
                .ifPresent(m -> {
                    exerciseInfo.put("description", (String) m.get("description"));
                    exerciseInfo.put("videoURL", (String) m.get("videoURL"));
                });
        return exerciseInfo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Triple<Integer, String, Map<String, Map<String, List<Integer>>>>> getRoutines() {
        final Set<Triple<Integer, String, Map<String, Map<String, List<Integer>>>>> routines = new HashSet<>();
        getService(ServiceProvider.ROUTINES)
                .getByParams(usernameAsQueryParam(currentUser.get())).forEach(r -> {
                    final Map<String, Map<String, List<Integer>>> workouts = new HashMap<>();
                    ((List<Map<String, Object>>) r.get("workouts")).forEach(w -> {
                        final Map<String, List<Integer>> exercises = new HashMap<>();
                        ((List<Map<String, Object>>) w.get("exercises")).forEach(e -> {
                            exercises.put(
                                    (String) e.get("exerciseName"),
                                    ((List<Object>) e.get("repetitions")).stream()
                                            .map(Object::toString)
                                            .map(Integer::valueOf)
                                            .collect(Collectors.toList()));
                        });
                        workouts.put((String) w.get("name"), exercises);
                    });
                    routines.add(new ImmutableTriple<>(
                            (int) r.get("routineIndex"),
                            (String) r.get("description"),
                            workouts));
                });
        return routines;
    }

    @Override
    public boolean addResults() {
        final Map<String, Pair<List<Integer>, Integer>> userResults = views
                .getSelectRoutineView()
                .getUserResults();
        final List<Map<String, Object>> results = userResults.entrySet().stream()
                .map(e -> {
                    final Map<String, Object> result = new HashMap<>();
                    result.put("username", currentUser.get());
                    result.put("exerciseName", e.getKey());
                    final Pair<List<Integer>, Integer> v = e.getValue();
                    result.put("repetitions", v.getLeft());
                    result.put("weight", v.getRight());
                    return result;
                })
                .collect(Collectors.toList());
        return getService(ServiceProvider.RESULTS).create(results);
    }

    @Override
    public boolean deleteRoutine() {
        final int routineIndex = views.getSelectRoutineView().getRoutineIndex();
        final Map<String, Object> deleteParams = usernameAsQueryParam(currentUser.get());
        deleteParams.put("routineIndex", routineIndex);
        return getService(ServiceProvider.ROUTINES).deleteByParams(deleteParams) > 0;
    }

    @Override
    public Map<String, Map<String, Number>> getChartsData() {
        final Map<String, Map<String, Number>> chartsData = new HashMap<>();
        chartsData.put("weightChart", getChartData("pieChartData"));
        chartsData.put("line", getChartData("pieChartData"));
        return chartsData;
    }

    @Override
    public Map<String, Object> getUserData() {
        return getUserData(currentUser.get()).get();
    }

    @Override
    public List<String> setUserData() {
        final String newFirstName = views.getUserSettingsView().getNewName();
        final String newLastName = views.getUserSettingsView().getNewSurname();
        final String newPassword = views.getUserSettingsView().getNewPassword();
        final int newAge = views.getUserSettingsView().getNewAge();
        final String newPasswordasswordConfirm = views.getUserSettingsView().getPasswordConfirm();
        final String newEmail = views.getUserSettingsView().getNewEmail();

        final Validator validator = new Validator()
                .addValidation(nameValidator(), newFirstName, "Invalid first name")
                .addValidation(nameValidator(), newLastName, "Invalid last name")
                .addValidation(numberValidator(), newAge, "Invalid age")
                .addValidation(emailValidator(), newEmail, "Invalid email")
                .addValidation(passwordValidator(), newPassword, "Invalid password")
                .addValidation(passwordConfirmValidator(newPasswordasswordConfirm), newPassword,
                        "The two passwords do not match")
                .addValidation(emailAlreadyTakenValidator().or(e -> getUserData().get("email").equals(e)), newEmail,
                        "Email already taken");
        validator.validate();
        validator.ifValid(() -> {
            // Update the current user data
            final Map<String, Object> newUserData = new HashMap<>();
            newUserData.put("password", newPassword);
            newUserData.put("name", newFirstName);
            newUserData.put("surname", newLastName);
            newUserData.put("email", newEmail);
            newUserData.put("age", newAge);
            getService(ServiceProvider.USERS).updateByParams(
                    usernameAsQueryParam(currentUser.get()),
                    newUserData);
        });
        return validator.getErrorMessages();
    }

    @Override
    public Map<String, Number> getIndexes() {
        // TODO
        return null;
    }

    private void checkIfUserIsNotLoggedIn() {
        checkState(!currentUser.isPresent());
    }

    private static Map<String, Object> usernameAsQueryParam(final String username) {
        final Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        return params;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Number> getChartData(final String chartName) {
        final Optional<Map<String, Object>> chartData = getService(ServiceProvider.CHARTS)
                .getByParams(usernameAsQueryParam(currentUser.get()))
                .stream().findFirst();
        return chartData.isPresent()
                ? ((List<Map<String, Object>>) chartData.get().get(chartName))
                        .stream()
                        .map(m -> new SimpleImmutableEntry<>(
                                (String) m.get("bodyPart"),
                                (Number) m.get("times")))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
                : Collections.emptyMap();
    }

    private static boolean checkIfUserExists(final String username) {
        return getUserData(username).isPresent();
    }

    private static boolean checkIfEmailExists(final String email) {
        final Map<String, Object> param = new HashMap<>();
        param.put("email", email);
        return getService(ServiceProvider.USERS).getOneByParams(param).isPresent();
    }

    private static Optional<Map<String, Object>> getUserData(final String username) {
        return getService(ServiceProvider.USERS).getOneByParams(usernameAsQueryParam(username));
    }

    // Validation strategies

    private static Predicate<String> usernameValidator() {
        final int minUsernameLength = 8;
        final int maxUsernameLength = 15;
        return u -> !isNull(u) && (u.length() >= minUsernameLength && u.length() <= maxUsernameLength);
    }

    private static Predicate<String> usernameAlreadyTakenValidator() {
        return u -> !checkIfUserExists(u);
    }

    private static Predicate<String> emailValidator() {
        return e -> !isNull(e) && EmailValidator.getInstance().isValid(e);
    }

    private static Predicate<String> emailAlreadyTakenValidator() {
        return e -> !checkIfEmailExists(e);
    }

    private static Predicate<String> nameValidator() {
        return n -> !isNull(n) && n.length() > 0;
    }

    private static Predicate<Number> numberValidator() {
        return n -> !isNull(n) && Double.compare(n.doubleValue(), 0) > 0;
    }

    private static Predicate<String> passwordValidator() {
        final int minPasswordLength = 6;
        return p -> !isNull(p) && p.length() > minPasswordLength;
    }

    private static Predicate<String> passwordConfirmValidator(final String other) {
        return p -> p.equals(other);
    }

    private static enum ServiceProvider {

        EXERCISES, ROUTINES, CHARTS, USERS, RESULTS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

    }

    private static final Map<ServiceProvider, Service> SERVICES;

    private static Service getService(final ServiceProvider service) {
        return SERVICES.get(service);
    }

    static {
        final Map<ServiceProvider, Service> services = new EnumMap<>(ServiceProvider.class);
        services.put(ServiceProvider.EXERCISES, new MongoService(ServiceProvider.EXERCISES.toString()));
        services.put(ServiceProvider.ROUTINES, new MongoService(ServiceProvider.ROUTINES.toString()));
        services.put(ServiceProvider.CHARTS, new MongoService(ServiceProvider.CHARTS.toString()));
        services.put(ServiceProvider.USERS, new MongoService(ServiceProvider.USERS.toString()));
        services.put(ServiceProvider.RESULTS, new MongoService(ServiceProvider.RESULTS.toString()));
        SERVICES = Collections.unmodifiableMap(services);
    }

}
