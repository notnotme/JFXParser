package com.notnotme.jfxparser;

import com.notnotme.jfxparser.controller.ui.MainWindowController;
import javafx.application.Application;
import javafx.stage.Stage;

public class JFXParser extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		MainWindowController.create(this, stage);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
