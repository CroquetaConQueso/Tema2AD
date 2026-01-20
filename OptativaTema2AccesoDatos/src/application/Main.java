package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = Main.class.getResource("/application/ui/main_view.fxml");
        if (fxml == null) {
            throw new IllegalStateException("No se encontró /application/ui/main_view.fxml en el classpath");
        }

        Parent root = FXMLLoader.load(fxml);
        Scene sc = new Scene(root, 1000, 640);
        stage.setTitle("Taller — Clientes");
        stage.setScene(sc);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
