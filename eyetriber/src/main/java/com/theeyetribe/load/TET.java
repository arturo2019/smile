package com.theeyetribe.load;

import java.io.Serializable;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import utils.Idioma;
import utils.LoggerUtils;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.data.GazeData;

public class TET implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3258619283736689377L;

	final static Logger logger = Logger.getLogger(TET.class);

	private Configuration config = null;

	private static GazeManager gazeManager;
	
	private static GazeListener gazeListener;

	public TET(String sessionId) {
		BasicConfigurator.configure();

		try {
			Idioma idioma = new Idioma();
			logger.trace("Set Idioma:start()");
			config = idioma.setIdioma("espanol");
			logger.trace("Set Idioma:end()");
		} catch (ConfigurationException e2) {
			logger.error(e2.getMessage());
		}

		LoggerUtils loggerUtils = new LoggerUtils();

		loggerUtils.load(config.getString("pathGlobal"), sessionId,
				config.getString("synchronizer.ruta.log"),
				config.getString("pathLog4j"), "synchronizer.log",
				"synchronizer.html");

		gazeManager = GazeManager.getInstance();
		gazeManager.activate(ApiVersion.VERSION_1_0, ClientMode.PUSH);

		gazeListener = new GazeListener();
		gazeManager.addGazeListener(gazeListener);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				gazeManager.removeGazeListener(gazeListener);
				gazeManager.deactivate();
			}
		});
	}

	public void stop(){
		Runtime.getRuntime().exit(1);
		gazeManager.removeGazeListener(gazeListener);
		gazeManager.deactivate();
	}
	
	private static class GazeListener implements IGazeListener {
		@Override
		public void onGazeUpdate(GazeData gazeData) {
			if (gazeData.state == GazeData.STATE_TRACKING_LOST) {
				logger.trace("timeStamp=" + gazeData.timeStamp
						+ "|timeStampString=" + gazeData.timeStampString + "|"
						+ "** Enlace de usuario perdido.**");
			} else {
				logger.trace("timeStamp=" + gazeData.timeStamp
						+ "|timeStampString=" + gazeData.timeStampString
						+ "|rawCoordinates.x=" + gazeData.rawCoordinates.x
						+ "|rawCoordinates.y=" + gazeData.rawCoordinates.y
						+ "|smoothedCoordinates.x="
						+ gazeData.smoothedCoordinates.x
						+ "|smoothedCoordinates.y="
						+ gazeData.smoothedCoordinates.y
						+ "|leftEye.pupilSize=" + gazeData.leftEye.pupilSize
						+ "|leftEye.pupilCenterCoordinates="
						+ gazeData.leftEye.pupilCenterCoordinates
						+ "|leftEye.smoothedCoordinates.x="
						+ gazeData.leftEye.smoothedCoordinates.x
						+ "|leftEye.smoothedCoordinates.y="
						+ gazeData.leftEye.smoothedCoordinates.y
						+ "|leftEye.rawCoordinates.x="
						+ gazeData.leftEye.rawCoordinates.x
						+ "|leftEye.rawCoordinates.y="
						+ gazeData.leftEye.rawCoordinates.y
						+ "|rightEye.pupilSize=" + gazeData.rightEye.pupilSize
						+ "|rightEye.pupilCenterCoordinates="
						+ gazeData.rightEye.pupilCenterCoordinates
						+ "|rightEye.smoothedCoordinates.x="
						+ gazeData.rightEye.smoothedCoordinates.x
						+ "|rightEye.smoothedCoordinates.y="
						+ gazeData.rightEye.smoothedCoordinates.y
						+ "|rightEye.rawCoordinates.x="
						+ gazeData.rightEye.rawCoordinates.x
						+ "|rightEye.rawCoordinates.y="
						+ gazeData.rightEye.rawCoordinates.y + "|isFixated="
						+ gazeData.isFixated + "|state=" + gazeData.state);
			}

		}
	}
}
