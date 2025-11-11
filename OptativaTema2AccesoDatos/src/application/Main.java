package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
                Main.class.getResource("ui/main_view.fxml")
        );
        Scene sc = new Scene(root, 1000, 640);
        stage.setTitle("Taller — Clientes");
        stage.setScene(sc);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
