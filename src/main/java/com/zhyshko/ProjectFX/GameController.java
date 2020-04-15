package com.zhyshko.ProjectFX;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class GameController implements Initializable {

	@FXML
	Label lbl_user1;
	@FXML
	Label lbl_user2;
	@FXML
	Label lbl_data;

	public String username = App.username;

	public static AtomicBoolean running = new AtomicBoolean(true);

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			Task<String> task = new Task<String>() {
				@Override
				public String call() {
					GameController.running.set(true);
					System.out.println("Task called");
					try {
						App.out.write("getplayers\n");
						App.out.flush();
						while (GameController.running.get()) {
							System.out.println("Running: " + running.get());
							String serverResponse = App.in.readLine();
							System.out.println("Service text: " + serverResponse);
							if (serverResponse.contains("updateplayers")) {
								System.out.println("Requsted update");
								App.out.write("getplayers" + "\n");
								App.out.flush();
								serverResponse = "";
							} else if (serverResponse.contains("players:")) {
								System.out.println("Received update");
								String data = serverResponse.split(":")[1];
								String[] players = data.split(";");
								this.updateValue(!players[0].equals(" ") ? players[0] : "Waiting...");
								this.updateMessage(!players[1].equals(" ") ? players[1] : "Waiting...");
							} else if (serverResponse.contains("ok")) {
								GameController.running.set(false);
								Platform.runLater(() -> {
									try {
										Stage stage = (Stage) (lbl_user1.getScene().getWindow());
										AnchorPane ap = (AnchorPane) (new FXMLLoader(App.class.getResource("Menu.fxml"))
												.load());
										Scene scene = new Scene(ap, 600, 340);
										scene.getStylesheets()
												.add(getClass().getResource("application.css").toExternalForm());
										Label lbl = (Label) scene.lookup("#lbl_hello");
										lbl.setText("Hello, " + App.username);
										stage.setScene(scene);
										stage.show();
										System.out.println("leave ok");
									} catch (Exception e) {
										e.printStackTrace();
									}
								});
							}
						}
						System.out.println("Task finished");
					} catch (Exception e) {
						Platform.exit();
						e.printStackTrace();
					}
					return "";
				}
			};

			Thread tr = new Thread(task);
			tr.setDaemon(true);
			tr.start();

			lbl_user1.textProperty().bind(task.valueProperty());
			lbl_user2.textProperty().bind(task.messageProperty());

		} catch (Exception e) {

		}

		
	}

	@FXML
	private void leaveGame(ActionEvent event) {
		try {
			App.out.write("leavegame" + "\n");
			App.out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
