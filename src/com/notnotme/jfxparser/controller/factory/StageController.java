package com.notnotme.jfxparser.controller.factory;

import javafx.application.Application;
import javafx.stage.Stage;

public class StageController extends Controller {

	private final Stage mStage;

	public StageController(Application application, Stage stage) {
		super(application);
		mStage = stage;
	}

	public Stage getStage() {
		return mStage;
	}

}
