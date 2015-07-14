package central_smile;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

import record_video.Video;
import utils.Idioma;
import utils.LoggerUtils;
import utils.PlaceholderTextField;

import com.theeyetribe.load.TET;
import com.wet.wired.jsr.recorder.DesktopScreenRecorder;
import com.wet.wired.jsr.recorder.FileHelper;
import com.wet.wired.jsr.recorder.ScreenRecorder;
import com.wet.wired.jsr.recorder.ScreenRecorderListener;

public class JSynchronizer extends JFrame implements ScreenRecorderListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3632890854887717254L;
	private JOptionPane contentPane;
	private PlaceholderTextField textUsuario;
	private JTextField textHH;
	private JTextField textMM;
	private JTextField textSS;
	private JTextField textMSS;
	private JLabel label;
	private JLabel label_1;
	private JLabel lblMm;
	private JLabel lblSs;
	private JLabel lblHh;
	private JLabel label_2;
	private JLabel lblMss;
	private JLabel lblFrameCapturado;

	private Video _video = null;
	private JButton btnDetener = null;
	private JButton btnSincronizar = null;
	private int hora, minutos, segundos, msegundos;
	private Calendar calendario = null;

	private ScreenRecorder recorder;
	private File temp;
	private boolean shuttingDown = false;
	private int frameCount = 0;
	private JTextField textFrameCapturado;

	private TET eyetriber;
	private String sessionId;
	private String nameSessionId;
	private Configuration config = null;
	final static Logger logger = Logger.getLogger(JSynchronizer.class);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JSynchronizer frame = new JSynchronizer();
					frame.setVisible(true);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JSynchronizer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BasicConfigurator.configure();

		initialize();
		initReloj();
	}

	private void initialize() {
		sessionId = UUID.randomUUID().toString();

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

		logger.info("****************** Inicio de Sincronizador ***************** ");
		logger.info("*********** " + sessionId + " *********** ");
		logger.info("************************************************************ ");

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});

		logger.trace("Load Interfaz:start()");
		/*
		 * Set variables container main panel
		 */
		setTitle(config.getString("sincronizador.titulo"));
		setSize(450, 250);

		contentPane = new JOptionPane();
		contentPane.setLayout(null);
		setContentPane(contentPane);

		JLabel lblUsuario = new JLabel(
				config.getString("sincronizador.usuario"));
		lblUsuario.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblUsuario.setBounds(10, 70, 46, 14);
		contentPane.add(lblUsuario);

		textUsuario = new PlaceholderTextField();
		textUsuario.setBounds(97, 65, 327, 25);
		textUsuario.setColumns(10);
		textUsuario.setPlaceholder(config
				.getString("sincronizador.ingresar.usuario"));
		contentPane.add(textUsuario);

		btnSincronizar = new JButton(
				config.getString("sincronizador.sincronizar"));
		btnSincronizar.setForeground(Color.BLUE);
		btnSincronizar.setBounds(100, 100, 115, 43);
		btnSincronizar.addActionListener(new ActionListener() {
			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 *      Action: Sincronizar <br>
			 *      Funtion: <br>
			 *      1.- instance video camera <br>
			 *      2.- instance screen recorder <br>
			 */
			public void actionPerformed(ActionEvent e) {
				if (textUsuario.getText().trim().isEmpty()) {
					textUsuario.setFocusable(true);
					JOptionPane.showMessageDialog(null,
							"Ingrese nombre de usuario para continuar");
				} else {
					nameSessionId = textUsuario.getText();
					logger.info("*********** " + nameSessionId
							+ " *********** ");

					CameraSwingWorker cameraSwingWorker = new CameraSwingWorker();

					/*
					 * Init video camera
					 */
					logger.trace("Invoke execute start video camera:start()");
					cameraSwingWorker.execute();
					logger.trace("Invoke execute start video camera:end()");
					try {
						/*
						 * Init screen recorder
						 */
						logger.trace("Invoke execute start screen recorder:start()");
						startRecording();
						logger.trace("Invoke execute start screen recorder:end()");

						toggleButton(false, true, true);
					} catch (IOException e1) {
						logger.error(e1.getMessage());
					}

					eyetriber = new TET(sessionId);
				}
			}
		});
		contentPane.add(btnSincronizar);

		btnDetener = new JButton(config.getString("sincronizador.detener"));
		btnDetener.setForeground(Color.RED);
		btnDetener.setBounds(240, 100, 115, 43);
		btnDetener.setEnabled(false);
		btnDetener.addActionListener(new ActionListener() {
			/**
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 *      Action: Detener <br>
			 *      Funtion: <br>
			 *      1.- stop video camera <br>
			 *      2.- stop screen recorder <br>
			 */
			public void actionPerformed(ActionEvent e) {
				try {
					logger.trace("Invoke execute stop video camera:start()");
					_video.stopCamera(sessionId);
					logger.trace("Invoke execute stop video camera:stop()");

					logger.trace("Invoke execute stop screen recorder:start()");
					recorder.stopRecording();
					logger.trace("Invoke execute stop screen recorder:end()");

					eyetriber.stop();

					toggleButton(true, false, false);
				} catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
					logger.error(e1.getMessage());
				} catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
					logger.error(e1.getMessage());
				}
			}
		});
		contentPane.add(btnDetener);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 47, 414, 2);
		contentPane.add(separator);

		JLabel lblSincronizadorYClasificador = new JLabel(
				config.getString("sincronizador.bienvenida"));
		lblSincronizadorYClasificador.setForeground(Color.DARK_GRAY);
		lblSincronizadorYClasificador
				.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblSincronizadorYClasificador.setBounds(10, 11, 414, 37);
		contentPane.add(lblSincronizadorYClasificador);

		textHH = new JTextField();
		textHH.setFont(new Font("Tahoma", Font.PLAIN, 13));
		textHH.setEditable(false);
		textHH.setBounds(200, 170, 29, 32);
		textHH.setColumns(10);
		textHH.setVisible(false);
		contentPane.add(textHH);

		textMM = new JTextField();
		textMM.setFont(new Font("Tahoma", Font.PLAIN, 13));
		textMM.setEditable(false);
		textMM.setColumns(10);
		textMM.setBounds(240, 170, 29, 32);
		textMM.setVisible(false);
		contentPane.add(textMM);

		textSS = new JTextField();
		textSS.setFont(new Font("Tahoma", Font.PLAIN, 13));
		textSS.setEditable(false);
		textSS.setColumns(10);
		textSS.setBounds(280, 170, 29, 32);
		textSS.setVisible(false);
		contentPane.add(textSS);

		label = new JLabel(":");
		label.setBounds(233, 178, 10, 14);
		label.setVisible(false);
		contentPane.add(label);

		label_1 = new JLabel(":");
		label_1.setBounds(273, 178, 10, 14);
		label_1.setVisible(false);
		contentPane.add(label_1);

		label_2 = new JLabel(":");
		label_2.setBounds(313, 178, 10, 14);
		label_2.setVisible(false);
		contentPane.add(label_2);
		
		lblHh = new JLabel(config.getString("sincronizador.hh"));
		lblHh.setForeground(new Color(0, 128, 0));
		lblHh.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblHh.setBounds(205, 150, 19, 14);
		lblHh.setVisible(false);
		contentPane.add(lblHh);

		lblMm = new JLabel(config.getString("sincronizador.mm"));
		lblMm.setForeground(new Color(0, 128, 0));
		lblMm.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblMm.setBounds(245, 150, 19, 14);
		lblMm.setVisible(false);
		contentPane.add(lblMm);

		lblSs = new JLabel(config.getString("sincronizador.ss"));
		lblSs.setForeground(new Color(0, 128, 0));
		lblSs.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblSs.setBounds(285, 150, 19, 14);
		lblSs.setVisible(false);
		contentPane.add(lblSs);

		textMSS = new JTextField();
		textMSS.setFont(new Font("Tahoma", Font.PLAIN, 13));
		textMSS.setEditable(false);
		textMSS.setColumns(10);
		textMSS.setBounds(320, 170, 43, 32);
		textMSS.setVisible(false);
		contentPane.add(textMSS);

		lblMss = new JLabel(config.getString("sincronizador.mss"));
		lblMss.setForeground(new Color(0, 128, 0));
		lblMss.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblMss.setBounds(325, 150, 29, 14);
		lblMss.setVisible(false);
		contentPane.add(lblMss);

		lblFrameCapturado = new JLabel(
				config.getString("sincronizador.frame.capturado"));
		lblFrameCapturado.setForeground(new Color(0, 128, 0));
		lblFrameCapturado.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblFrameCapturado.setBounds(10, 150, 164, 14);
		lblFrameCapturado.setVisible(false);
		contentPane.add(lblFrameCapturado);

		textFrameCapturado = new JTextField();
		textFrameCapturado.setEditable(false);
		textFrameCapturado.setBounds(10, 170, 164, 32);
		textFrameCapturado.setVisible(false);
		textFrameCapturado.setColumns(10);
		contentPane.add(textFrameCapturado);

		logger.trace("Load Interfaz:end()");
	}

	/**
	 * Method: initReloj params: none function: See you reloj
	 */
	@SuppressWarnings("static-access")
	public void initReloj() {
		javax.swing.Timer timer = new javax.swing.Timer(1000,
				new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent ae) {
						calendario = new java.util.GregorianCalendar();
						calendario.add(calendario.HOUR_OF_DAY, -1);

						hora = calendario.get(Calendar.HOUR_OF_DAY);
						minutos = calendario.get(Calendar.MINUTE);
						segundos = calendario.get(Calendar.SECOND);
						msegundos = calendario.get(Calendar.MILLISECOND);

						textHH.setText(Integer.toString(hora));
						textMM.setText(Integer.toString(minutos));
						textSS.setText(Integer.toString(segundos));
						textMSS.setText(Integer.toString(msegundos));
					}
				});
		timer.start();
	}

	/**
	 * Method toggleButton input: Boolean isEnabledNext, <br>
	 * Boolean isEnabledFinalizar, <br>
	 * Boolean isEnabledToo <br>
	 */
	private void toggleButton(Boolean isEnabledNext,
			Boolean isEnabledFinalizar, Boolean isEnabledTool) {
		btnSincronizar.setEnabled(isEnabledNext);
		btnDetener.setEnabled(isEnabledFinalizar);

		textHH.setVisible(isEnabledTool);
		textMM.setVisible(isEnabledTool);
		textSS.setVisible(isEnabledTool);
		label.setVisible(isEnabledTool);
		label_1.setVisible(isEnabledTool);
		lblMm.setVisible(isEnabledTool);
		lblSs.setVisible(isEnabledTool);
		label_2.setVisible(isEnabledTool);
		textMSS.setVisible(isEnabledTool);
		lblMss.setVisible(isEnabledTool);
		lblFrameCapturado.setVisible(isEnabledTool);
		textFrameCapturado.setVisible(isEnabledTool);
	}

	class CameraSwingWorker extends SwingWorker<String, Object> {
		@Override
		public String doInBackground() throws Exception {
			try {
				_video = new Video();
				logger.trace("start video camera:start()");
				_video.startCamera(sessionId);
				logger.trace("start video camera:end()");
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			return null;
		}

		@Override
		protected void done() {
			try {
				logger.trace("done video camera");
				toggleButton(true, false, false);
			} catch (Exception ignore) {
				logger.error(ignore.getMessage());
			}
		}
	}

	public void startRecording() throws IOException {
		temp = File.createTempFile("temp", "rec");

		String fileName = temp.getAbsolutePath();

		try {
			logger.trace("Delay Camera video to screen recorder:start() - time:"
					+ config.getInt("sincronizador.espera.componente.recorder"));
			Thread.sleep(config
					.getInt("sincronizador.espera.componente.recorder"));
			logger.trace("Delay Camera video to screen recorder:end()");
		} catch (InterruptedException e1) {
			logger.error(e1.getMessage());
		}

		if (recorder != null) {
			return;
		}

		try {
			FileOutputStream oStream = new FileOutputStream(fileName);
			temp = new File(fileName);
			recorder = new DesktopScreenRecorder(oStream, this, sessionId);
			logger.trace("Start screen recorder:start()");
			recorder.startRecording(sessionId);
			logger.trace("Start screen recorder:stop()");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void frameRecorded(boolean fullFrame) throws IOException {
		frameCount++;
		if (textFrameCapturado != null) {
			textFrameCapturado.setText(config.getString("sincronizador.frame")
					+ frameCount);
		}
	}

	public void recordingStopped() {
		if (!shuttingDown) {
			File target = new File(config.getString("pathGlobal") + sessionId
					+ config.getString("screen.ruta")
					+ config.getString("screen.nombre.archivo") + ".cap");

			logger.trace("Copy file screen recorder:start()" + target);
			FileHelper.copy(temp, target);
			logger.trace("Copy file screen recorder:end()");

			FileHelper.delete(temp);
			recorder = null;
			frameCount = 0;

			textFrameCapturado.setText(config
					.getString("sincronizador.prepara.grabacion"));
		} else
			FileHelper.delete(temp);
	}

	public void shutdown() {
		shuttingDown = true;

		if (recorder != null) {
			logger.trace("Stop screen recorder:start() ");
			recorder.stopRecording();
			logger.trace("Stop screen recorder:end() ");
		}

		dispose();
	}
}