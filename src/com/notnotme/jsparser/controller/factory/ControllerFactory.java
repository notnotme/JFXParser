package com.notnotme.jsparser.controller.factory;

import com.notnotme.jsparser.controller.processor.ParserFileType;
import com.notnotme.jsparser.controller.ui.EditorTabController;
import java.lang.reflect.InvocationTargetException;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Callback;

public final class ControllerFactory implements Callback<Class<?>, Object> {

	private final Application mApplication;
	private final Stage mStage;
	private final ParserFileType mProcessorFileType;

	public ControllerFactory(Application application) {
		mApplication = application;
		mStage = null;
		mProcessorFileType = null;
	}
	
	public ControllerFactory(Application application, Stage stage) {
		mApplication = application;
		mStage = stage;
		mProcessorFileType = null;
	}

	public ControllerFactory(Application application, Stage stage, ParserFileType type) {
		mApplication = application;
		mProcessorFileType = type;
		mStage = stage;
	}

	@Override
	public Object call(Class<?> type) {
		try {
			if (EditorTabController.class.isAssignableFrom(type)) {
				// EditorTabController is final so we don't need to guess the ctor
				return new EditorTabController(mApplication, mStage, mProcessorFileType);
			} else if (StageController.class.isAssignableFrom(type)) {
				// Call the class first ctor
				return type.getConstructors()[0].newInstance(mApplication, mStage);
			} else if (Controller.class.isAssignableFrom(type)) {
				// Call the class first ctor
				return type.getConstructors()[0].newInstance(mApplication);
			} 
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		throw new RuntimeException("No contructor found");
	}

}