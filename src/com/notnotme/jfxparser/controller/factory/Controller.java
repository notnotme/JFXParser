package com.notnotme.jfxparser.controller.factory;

import javafx.application.Application;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private final Application mApplication;
    private ResourceBundle mResources;

    Controller(Application application) {
        mApplication = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mResources = resources;
    }

    protected Application getApplication() {
        return mApplication;
    }

    protected ResourceBundle getResources() {
        return mResources;
    }

}
