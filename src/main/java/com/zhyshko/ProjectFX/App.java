package com.zhyshko.ProjectFX;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class App extends Application {

	@FXML
	TextField text_username;
	@FXML
	Button btn_login;
	@FXML
	Label lbl_hello;
	@FXML
	Button btn_createGame;
	@FXML
	Button btn_joinGame;

	static Socket client;
	static BufferedReader in;
	static BufferedWriter out;

	public static String username;

	static {
		try {
			client = new Socket("192.168.88.44", 9000);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane ap = (AnchorPane) (new FXMLLoader(App.class.getResource("Login.fxml")).load());
			Scene scene = new Scene(ap, 600, 340);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(event -> {
				try {
					App.out.write("disconnect");
					App.out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	


	@FXML
	private void login(ActionEvent event) {
		Button b = (Button) (event.getSource());
		Stage stage = (Stage) (b.getScene().getWindow());
		String username = this.text_username.getText();
		if (username == null) {
			alert("Username empty").show();
			return;
		}

		if (username.isEmpty()) {
			alert("Username empty").show();
			text_username.setText("");
			return;
		}
		try {
			out.write("login:" + username + "\n");
			out.flush();
			boolean ok = false;
			String response = in.readLine();
			if (response.equals("exists")) {
				alert("Such user exists: " + username).show();
				text_username.setText("");

			} else if (response.equals("ok")) {
				ok = true;
			}
			if (ok) {
				App.username = username;
				AnchorPane ap = (AnchorPane) (new FXMLLoader(App.class.getResource("Menu.fxml")).load());
				Scene scene = new Scene(ap, 600, 340);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				stage.setScene(scene);
				stage.show();
				Label lbl = (Label) scene.lookup("#lbl_hello");
				lbl.setText("Hello, " + App.username);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void createGame(ActionEvent event) throws IOException {
		Button b = (Button) (event.getSource());
		Stage stage = (Stage) (b.getScene().getWindow());
		String gameName = ((TextField) (stage.getScene().lookup("#text_gameName"))).getText();
		if (gameName == null) {
			return;
		}
		if (gameName.isEmpty()) {
			return;
		}

		out.write("creategame:" + gameName + "\n");
		out.flush();

		String response = in.readLine();
		if (response.contains("ok")) {
			GridPane ap = (GridPane) (new FXMLLoader(App.class.getResource("SeaBattle.fxml")).load());
			Scene scene = new Scene(ap, 480, 340);
			scene.getStylesheets().add(getClass().getResource("battle.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} else if (response.contains("error")) {
			alert("Game with such name exists, try with another name").show();
			return;
		}
	}

	@FXML
	private void joinGame(ActionEvent event) throws IOException {
		Button b = (Button) (event.getSource());
		Stage stage = (Stage) (b.getScene().getWindow());
		ListView<String> list = (ListView<String>) stage.getScene().lookup("#list_games");
		String chosen = list.getFocusModel().getFocusedItem().toString();
		out.write("joingame:" + chosen + "\n");
		out.flush();
		String response = in.readLine();
		System.out.println("resp: " + response);
		if (response.trim().equals("ok")) {
			GridPane ap = (GridPane) (new FXMLLoader(App.class.getResource("SeaBattle.fxml")).load());
			Scene scene = new Scene(ap, 480, 340);
			scene.getStylesheets().add(getClass().getResource("battle.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} else {
			alert("This game is no longer available").show();
			out.write("getgames\n");
			out.flush();
			String games = in.readLine();
			List<String> gamesList = Arrays.asList(games.split(","));
			list.getItems().clear();
			gamesList.forEach(e -> list.getItems().add(e));
			return;
		}
	}

	@FXML
	private void backToMenu(ActionEvent event) throws IOException {
		Button b = (Button) (event.getSource());
		Stage stage = (Stage) (b.getScene().getWindow());
		AnchorPane ap = (AnchorPane) (new FXMLLoader(App.class.getResource("Menu.fxml")).load());
		Scene scene = new Scene(ap, 600, 340);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		Label lbl = (Label) scene.lookup("#lbl_hello");
		lbl.setText("Hello, " + App.username);
	}

	@FXML
	private void openWindowCreateGame(ActionEvent event) throws IOException {
		Button b = (Button) (event.getSource());
		Stage stage = (Stage) (b.getScene().getWindow());
		AnchorPane ap = (AnchorPane) (new FXMLLoader(App.class.getResource("CreateGame.fxml")).load());
		Scene scene = new Scene(ap, 600, 340);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}

	@FXML
	private void openWindowJoinGame(ActionEvent event) throws IOException {
		Button b = (Button) (event.getSource());
		Stage stage = (Stage) (b.getScene().getWindow());
		AnchorPane ap = (AnchorPane) (new FXMLLoader(App.class.getResource("JoinGame.fxml")).load());
		Scene scene = new Scene(ap, 600, 340);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		ListView<String> list = ((ListView<String>) scene.lookup("#list_games"));
		out.write("getgames\n");
		out.flush();
		String games = in.readLine();
		List<String> gamesList = Arrays.asList(games.split(","));
		gamesList.forEach(e -> list.getItems().add(e));
	}

	private Alert alert(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Login");
		alert.setHeaderText(null);
		alert.setContentText(message);
		return alert;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
