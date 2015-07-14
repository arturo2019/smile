package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LoggerUtils {
	final static Logger logger = Logger.getLogger(LoggerUtils.class);

	public LoggerUtils() {
		BasicConfigurator.configure();
	}

	public void load(String pathGlobal, String sessionId, String pathLog,
			String pathProperties, String logFile1, String logFile2) {
		Properties propertie = new Properties();
		try {
			File fileLog = new File(pathGlobal + sessionId + pathLog);

			if (!fileLog.exists()) {
				fileLog.mkdir();
			}

			propertie.load(new FileInputStream(pathProperties));
			propertie.setProperty("log4j.appender.LOG1.File", fileLog.getPath()
					+ "\\" + logFile1);
			propertie.setProperty("log4j.appender.LOG2.File", fileLog.getPath()
					+ "\\" + logFile2);
		} catch (IOException e3) {
			logger.error("error al cargar log4j.properties" + e3.getMessage());
		}

		PropertyConfigurator.configure(propertie);
	}
}
