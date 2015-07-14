package record_video;

//import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
//import static org.bytedeco.javacpp.opencv_core.CV_AA;
//import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
//import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
//import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
//import static org.bytedeco.javacpp.opencv_core.cvPoint;
//import static org.bytedeco.javacpp.opencv_core.cvRectangle;
//import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
//import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
//import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;
//import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
//import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;
//import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
//import static org.bytedeco.javacpp.opencv_video.*;
//import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.swing.JFrame;
import javax.xml.crypto.URIReferenceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bytedeco.javacpp.avcodec;
//import org.bytedeco.javacpp.opencv_core.CvMemStorage;
//import org.bytedeco.javacpp.opencv_core.CvRect;
//import org.bytedeco.javacpp.opencv_core.CvScalar;
//import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import utils.Idioma;
import utils.LoggerUtils;

//import org.bytedeco.javacv.Blobs;

/**
 * Web service implementation
 * 
 * @author Arturo Mendoza
 * @version 1.0
 * @since 01-07-2014<br>
 *        <p>
 *        Libraries used
 *        </p>
 * @see - log4j it is possible to enable logging at runtime without modifying
 *      the application binary. {@link Logger} <br>
 *      - OpenCv {@link OpenCVFrameGrabber} <br>
 *      - CvHaarClassifierCascade {@link CvHaarClassifierCascade} <br>
 */
public class Video extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1653464046270736821L;
	final private static int FRAME_RATE = 30;
	final private static int GOP_LENGTH_IN_FRAMES = 60;

	private OpenCVFrameGrabber grabber = null;
	// private CvHaarClassifierCascade classifierFrontalFace = null;
	// private CvHaarClassifierCascade classifierFrontalMouth = null;
	private OpenCVFrameConverter.ToIplImage converter = null;
	private CanvasFrame canvasFrame = null;
	private IplImage grabbedImage = null;
	private FFmpegFrameRecorder recorder = null;
	private Configuration config = null;

	private static long startTime = 0;
	private static long videoTS = 0;

	final static Logger logger = Logger.getLogger(Video.class);

	public static void main(String[] args) throws Exception,
			org.bytedeco.javacv.FrameRecorder.Exception, IOException {
		Video video = new Video();
		try {
			video.startCamera("DEMO");
		} catch (URIReferenceException e) {
			logger.error("Error al abrir URI" + e.getMessage());
		} catch (URISyntaxException e) {
			logger.error("Error al abrir URI" + e.getMessage());
		} catch (MalformedURLException e) {
			logger.error("Error al abrir URI" + e.getMessage());
		}
	}

	public Video() {
		BasicConfigurator.configure();
	}

	public void startCamera(String sessionId) throws Exception,
			org.bytedeco.javacv.FrameRecorder.Exception, IOException,
			URISyntaxException, URIReferenceException, MalformedURLException {

		if (sessionId == null) {
			sessionId = UUID.randomUUID().toString();
		}

		try {
			Idioma idioma = new Idioma();
			logger.trace("Set Idioma:start()");
			config = idioma.setIdioma("espanol");
			logger.trace("Set Idioma:end()");
		} catch (ConfigurationException e) {
			logger.error(e.getMessage());
		}

		LoggerUtils loggerUtils = new LoggerUtils();

		loggerUtils.load(config.getString("pathGlobal"), sessionId,
				config.getString("synchronizer.ruta.log"),
				config.getString("pathLog4j"), "synchronizer.log",
				"synchronizer.html");

		logger.info("****************** Inicio de Recorder Video ***************** ");
		logger.info("*********** " + sessionId + " *********** ");
		logger.info("************************************************************ ");

		String localPath = config.getString("pathGlobal") + sessionId;

		File framePath = new File(localPath
				+ config.getString("webcam.ruta.frame"));

		File generalPath = new File(localPath
				+ config.getString("webcam.ruta.video"));

		if (!generalPath.exists()) {
			generalPath.mkdir();
		}

		if (!framePath.exists()) {
			framePath.mkdir();
		}

		int captureWidth = config.getInt("webcam.ancho.de.captura");
		int captureHeight = config.getInt("webcam.alto.de.captura");

		// OpenCVUtils opencv = new OpenCVUtils();

		// opencv.loadHaarClassifier(config
		// .getString("tipo.filtro.haarcascades.face"));
		// classifierFrontalFace = opencv.getClassifier();

		// opencv.loadHaarClassifier(config
		// .getString("tipo.filtro.haarcascades.mouth"));
		// classifierFrontalMouth = opencv.getClassifier();

		grabber = new OpenCVFrameGrabber(config.getInt("webcam.device.index"));
		grabber.setImageWidth(captureWidth);
		grabber.setImageHeight(captureHeight);
		logger.trace("Inicio Grabador:start()");
		grabber.start();
		logger.trace("Inicio Grabador:end()");

		converter = new OpenCVFrameConverter.ToIplImage();

		grabbedImage = converter.convert(grabber.grab());
		// IplImage grayImage = IplImage.create(grabber.grab().imageWidth,
		// grabber.grab().imageHeight, IPL_DEPTH_8U, 1);

		recorder = new FFmpegFrameRecorder(localPath
				+ config.getString("webcam.ruta.video")
				+ config.getString("webcam.nombre.video") + "."
				+ config.getString("webcam.format"), captureWidth,
				captureHeight, 2);

		recorder.setInterleaved(true);
		recorder.setVideoOption("tune", "zerolatency");
		recorder.setVideoOption("preset", "ultrafast");
		recorder.setVideoOption("crf", "28");
		recorder.setVideoBitrate(config.getInt("webcam.bitrate"));
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setFormat(config.getString("webcam.format"));
		recorder.setFrameRate(FRAME_RATE);
		recorder.setGopSize(GOP_LENGTH_IN_FRAMES);

		logger.trace("Inicio Recorder:start()");
		recorder.start();
		logger.trace("Inicio Recorder:end()");

		canvasFrame = new CanvasFrame(config.getString("webcam.titulo"),
				CanvasFrame.getDefaultGamma() / grabber.getGamma());
		canvasFrame.setAlwaysOnTop(true);
		canvasFrame.pack();

		Boolean isVisible = Boolean.parseBoolean(config
				.getString("webcam.isVisible"));

		if (!sessionId.equals("DEMO")) {
			canvasFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			canvasFrame.setVisible(isVisible);
		}else{
			canvasFrame.setVisible(true);
			isVisible = true;
		}
		
		canvasFrame.setLocation(450, 0);
		
//		CvMemStorage storage = CvMemStorage.create();

		boolean isVisibleDefault = true;
		while (isVisibleDefault
				&& (grabbedImage = converter.convert(grabber.grab())) != null) {

//			cvClearMemStorage(storage);
			//
			// cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
			//
			// CvSeq faces = cvHaarDetectObjects(grayImage,
			// classifierFrontalFace,
			// storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
			//
			// CvSeq mouth = cvHaarDetectObjects(grayImage,
			// classifierFrontalMouth, storage, 1.1, 3,
			// CV_HAAR_DO_CANNY_PRUNING);
			//
			// int totalFaces = faces.total();
			// int totalMouth = mouth.total();
			//
			// cvSaveImage("Working.jpg", grabbedImage);
			// for (int i = 0; i < totalFaces; i++) {
			// CvRect cvRectFace = new CvRect(cvGetSeqElem(faces, i));
			// int xFace = cvRectFace.x(), yFace = cvRectFace.y(), wFace =
			// cvRectFace
			// .width(), hFace = cvRectFace.height();
			// cvRectangle(grabbedImage, cvPoint(cvRectFace.x(),
			// cvRectFace.y()),
			// cvPoint(xFace + wFace, yFace + hFace), CvScalar.RED, 1,
			// CV_AA, 0);
			//
			// for (int j = 0; j < totalMouth; j++) {
			// CvRect cvRectMouth = new CvRect(cvGetSeqElem(mouth, j));
			// int xMouth = cvRectMouth.x(), yMouth = cvRectMouth.y(), wMouth =
			// cvRectMouth
			// .width(), hMouth = cvRectMouth.height();
			//
			// if (yMouth > yFace + hFace * 3 / 5
			// && yMouth + hMouth < yFace + hFace
			// && Math.abs((xMouth + wMouth / 2))
			// - (xFace + wFace / 2) < wFace / 10) {
			// cvRectangle(grabbedImage, cvPoint(xMouth, yMouth),
			// cvPoint(xMouth + wMouth, yMouth + hMouth),
			// CvScalar.GREEN, 1, CV_AA, 0);
			// }
			// }
			// }
			// cvThreshold(grayImage, grayImage, 64, 255, CV_THRESH_BINARY);
			if(isVisible){
				canvasFrame.showImage(converter.convert(grabbedImage));
				isVisible = isVisibleDefault = canvasFrame.isVisible();
			}

			cvSaveImage(framePath.getPath() + "\\" + System.currentTimeMillis()
					+ ".jpg", grabbedImage);

			/**
			 * Define our start time needs to be initialized as close to when
			 * we'll use it as possible as the delta from assignment to computed
			 * time could be too high
			 */
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			logger.trace("Inicio time:" + startTime + " :start()");

			/** Create timestamp for this frame */
			videoTS = 1000 * (System.currentTimeMillis() - startTime);
			logger.trace("Create timestamp for video: 1000 * (currentTime - starTime)"
					+ videoTS + " :process()");

			logger.trace("Get Timestamp for video: " + recorder.getTimestamp()
					+ " :process()");
			if (videoTS > recorder.getTimestamp()) {
				logger.trace("Lip-flap correction: " + videoTS + " : "
						+ recorder.getTimestamp() + " -> "
						+ (videoTS - recorder.getTimestamp()));

				/** We tell the recorder to write this frame at this timestamp */
				recorder.setTimestamp(videoTS);
			} else {
				logger.trace("Video menor: " + videoTS + " :else()");
			}

			recorder.record(converter.convert(grabbedImage));
		}

		stopCamera(sessionId);
	}

	public void stopCamera(String sessionId)
			throws org.bytedeco.javacv.FrameRecorder.Exception, Exception {
		logger.trace("Stop Frame:start()");
		canvasFrame.dispose();
		logger.trace("Stop Frame:end()");
		logger.trace("Stop Recorder:start()");
		recorder.stop();
		logger.trace("Stop Recorder:end()");
		logger.trace("Stop Grabador:start()");
		grabber.stop();
		logger.trace("Stop Grabador:end()");
	}
}
