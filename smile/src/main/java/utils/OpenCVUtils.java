package utils;

import static org.bytedeco.javacpp.opencv_core.cvLoad;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

public class OpenCVUtils {
	private CvHaarClassifierCascade classifier = null;
	
	final static Logger logger = Logger.getLogger(OpenCVUtils.class);
	static Configuration config = null;

	public OpenCVUtils() {
		PropertyConfigurator
				.configure("C:\\smile\\properties\\log4j.properties");

		Idioma idioma = new Idioma();
		try {
			logger.trace("OpenCV: load class Idioma:start();");
			config = idioma.setIdioma("espanol");
			logger.trace("OpenCV: load class Idioma:end();");
		} catch (ConfigurationException e) {
			logger.error("OpenCV: Error loading language" + e.getMessage());
		}
	}

	public CvHaarClassifierCascade loadHaarClassifier(String file)
			throws IOException {
		logger.info("Load file Classifier by string: " + file);
		File filetmp = new File(file);

		return loadClassifier(filetmp);
	}

	public CvHaarClassifierCascade loadHaarClassifier(URL url)
			throws IOException {
		logger.info("Load file Classifier by URL: " + url);
		File filetmp = Loader.extractResource(url, null, "classifier", ".xml");

		loadClassifier(filetmp);
		logger.info("Delete file temporal");
		filetmp.delete();

		return getClassifier();
	}

	private CvHaarClassifierCascade loadClassifier(File file)
			throws IOException {
		logger.info("Load file Classifier by File:" + file);
		if (file == null || file.length() <= 0) {
			logger.error("Could not extract file Classifier: " + file);
			throw new IOException("Could not extract file Classifier.");
		}

		logger.info("Load opencv detect");
		Loader.load(opencv_objdetect.class);

		logger.info("Set Classifier with openCV");
		setClassifier(new CvHaarClassifierCascade(
				cvLoad(file.getAbsolutePath())));

		if (getClassifier().isNull()) {
			logger.error("Could not extract content file the Classifier: "
					+ file);
			throw new IOException(
					"Could not extract content file the Classifier.");
		}

		return getClassifier();
	}

	public CvHaarClassifierCascade getClassifier() {
		return classifier;
	}

	public void setClassifier(CvHaarClassifierCascade classifier) {
		this.classifier = classifier;
	}
}
