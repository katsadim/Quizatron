package me.twodee.quizatron.Console;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import me.twodee.quizatron.Component.Mediator;
import me.twodee.quizatron.Component.Presentation;
import me.twodee.quizatron.Console.View.ConfigLoaderView;
import me.twodee.quizatron.Model.Service.QuizDataService;
import me.twodee.quizatron.Presentation.IView;

import javax.inject.Inject;
import javafx.event.ActionEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PanelPresenter {

    private Presentation presentation;
    private IView view;
    private FXMLLoader fxmlLoader;
    private Mediator mediator;
    private QuizDataService quizDataService;

    @FXML private JFXToggleButton fullScreenToggleBtn;
    @FXML private AnchorPane rootNode;
    @FXML private AnchorPane dashboard;
    @FXML private JFXTextField configFileLbl;
    @FXML private Label loadedQuizNameLbl;
    @FXML private JFXButton startBtn;


    @Inject
    public PanelPresenter(Presentation presentation,
                          FXMLLoader fxmlLoader,
                          Mediator mediator,
                          QuizDataService quizDataService) throws Exception {

        this.fxmlLoader = fxmlLoader;
        this.presentation = presentation;
        this.mediator = mediator;
        this.quizDataService = quizDataService;
    }

    @FXML
    public void openMediaTabAction(MouseEvent event) throws Exception {

        FXMLLoader loader = this.fxmlLoader;
        loader.setLocation(getClass().getResource("Dashboard/media-player.fxml"));
        AnchorPane mediaPlayerPane = loader.load();
        dashboard.getScene().getStylesheets().add(getClass().getResource("/Stylesheets/media.css").toExternalForm());
        dashboard.getChildren().add(mediaPlayerPane);
    }


    @FXML
    private void loadSavedState(MouseEvent event) {

        Path file = getFile("Open quiz saved file");
        mediator.request(() -> loadStateFromFile(file));
        loadFeedBack();
    }

    @FXML
    private void saveStateAction(MouseEvent event) {

        mediator.request(this::saveStateToFile);
    }


    @FXML
    private void loadConfigTxtAction(ActionEvent event) {

        String location = configFileLbl.getText();
        Path file = Paths.get(location);
        loadFromConfigFile(file);
        loadFeedBack();
    }

    @FXML
    private void loadConfigBtnAction(ActionEvent event)  {

        try {

            Path file = getFile("Open quiz configuration file");
            loadFromConfigFile(file);
            loadFeedBack();

            String source = file.toAbsolutePath().toString();
            configFileLbl.setText(source);
        }
        catch (NullPointerException e) {
            System.out.println("No file chosen");
        }
    }

    @FXML
    private void startQuizAction(ActionEvent event) {

        presentation.show();
        presentation.getScene().setCursor(Cursor.NONE);
        this.view = presentation.getView();
    }

    @FXML
    public void toggleFullScreen(ActionEvent event) {

        if (fullScreenToggleBtn.isSelected()) {

            presentation.getStage().setFullScreenExitHint("");
            presentation.getStage().setFullScreen(true);
        }
        else {
            presentation.getStage().setFullScreen(false);
        }
    }

    private void loadFromConfigFile(Path file) {

        mediator.request(() -> loadConfig(file));
    }

    private void loadConfig(Path file) {

        try {

            quizDataService.loadConfig(file);
        }
        catch (FileNotFoundException e) {

            mediator.setError("The file you entered couldn't be found");
        }
    }

    private void loadStateFromFile(Path file) {

        try {
            quizDataService.loadData(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveStateToFile() {

        try {
            quizDataService.saveData();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFeedBack() {

        ConfigLoaderView configLoaderView = new ConfigLoaderView(mediator, quizDataService);
        configLoaderView.setOutput(loadedQuizNameLbl, startBtn);
        mediator.respond(configLoaderView);
    }

    private Path getFile(String title) {

        FileChooser fileChooser = new FileChooser();

        if (quizDataService.getInitialDirectory() != null) {

            Path homePath = quizDataService.getInitialDirectory();
            fileChooser.setInitialDirectory(homePath.toFile());
        }

        fileChooser.setTitle(title);
        Path file = fileChooser.showOpenDialog(rootNode.getScene().getWindow()).toPath();
        return file;
    }
}
