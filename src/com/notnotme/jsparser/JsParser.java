package com.notnotme.jsparser;

import com.notnotme.jsparser.controller.ui.MainWindowController;
import javafx.application.Application;
import javafx.stage.Stage;

public class JsParser extends Application {
	
	public final static String TAG = JsParser.class.getSimpleName();

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
