package com.notnotme.jsparser.controller.factory;

import javafx.application.Application;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

	private final Application mApplication;
	private ResourceBundle mResources;

	public Controller(Application application) {
		mApplication = application;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		mResources = resources;
	}

	public Application getApplication() {
		return mApplication;
	}

	public ResourceBundle getResources() {
		return mResources;
	}

}
