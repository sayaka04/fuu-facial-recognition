
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class CameraGUI {
	// testing
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV library
	}

	private JFrame frame;
	private JLabel imageLabel;
	private int counter = 0;

	public CameraGUI() {
		frame = new JFrame("Camera GUI - Red Spot Counter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		imageLabel = new JLabel();
		frame.add(imageLabel);
		frame.setVisible(true);
	}

	public void startCamera() {
		VideoCapture camera = new VideoCapture(0); // Open default camera

		if (!camera.isOpened()) {
			System.out.println("Error: Camera not available!");
			return;
		}

		// Load Haar cascade for face detection
		String haarCascadePath = "haarcascade_frontalface_default.xml"; // Ensure this file exists in your working
																		// directory
		CascadeClassifier faceDetector = new CascadeClassifier(haarCascadePath);

		// Load Haar cascade for hand detection
		String handCascadePath = "haarcascade_frontalface_default.xml"; // Ensure this file exists in your working
																		// directory
		CascadeClassifier handDetector = new CascadeClassifier(handCascadePath);

		if (handDetector.empty()) {
			System.out.println("Error: Failed to load Haar cascade file for hands!");
			return;
		}

		Mat frameMat = new Mat();
		Mat flippedMat = new Mat();

		while (true) {
			if (camera.read(frameMat)) {
				Core.flip(frameMat, flippedMat, 1); // Flip horizontally

				// Detect faces
				Mat gray = new Mat();
				Imgproc.cvtColor(flippedMat, gray, Imgproc.COLOR_BGR2GRAY);
				Imgproc.equalizeHist(gray, gray); // Enhance contrast

				MatOfRect faces = new MatOfRect();
				faceDetector.detectMultiScale(gray, faces);

				for (Rect face : faces.toArray()) {
					Imgproc.rectangle(flippedMat, face.tl(), face.br(), new Scalar(0, 255, 0), 2); // Green box
				}

				// Detect hands
				boolean handDetected = detectHands(flippedMat, handDetector);
				if (handDetected) {
					System.out.println("Hand detected!");
				}

				// Display the frame
				ImageIcon image = new ImageIcon(matToBufferedImage(flippedMat));
				imageLabel.setIcon(image);
				imageLabel.repaint();
			}
		}
	}

	private boolean detectHands(Mat frame, CascadeClassifier handDetector) {
		// Convert frame to grayscale for detection
		Mat gray = new Mat();
		Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(gray, gray); // Enhance contrast

		// Detect hands using the Haar cascade
		MatOfRect hands = new MatOfRect();
		handDetector.detectMultiScale(gray, hands);

		// Draw rectangles around detected hands
		boolean isHandDetected = false;
		for (Rect hand : hands.toArray()) {
			Imgproc.rectangle(frame, hand.tl(), hand.br(), new Scalar(255, 0, 0), 2); // Blue box
			isHandDetected = true;
		}

		return isHandDetected; // Return true if at least one hand is detected
	}

	private BufferedImage matToBufferedImage(Mat mat) {
		int width = mat.width();
		int height = mat.height();
		int channels = mat.channels();
		byte[] source = new byte[width * height * channels];
		mat.get(0, 0, source);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		image.getRaster().setDataElements(0, 0, width, height, source);
		return image;
	}

	public static void main(String[] args) {
		CameraGUI cameraGUI = new CameraGUI();
		cameraGUI.startCamera();
	}
}

//import java.awt.image.BufferedImage;
//
//import javax.swing.ImageIcon;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//
//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfRect;
//import org.opencv.core.Rect;
//import org.opencv.core.Scalar;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.objdetect.CascadeClassifier;
//import org.opencv.videoio.VideoCapture;
//
//public class CameraGUI {
//	static {
//		// Replace with the actual path to your OpenCV DLL file
//		System.load(
//				"C:\\Users\\Acer\\OneDrive\\Documents\\.Programming\\Java\\WorkPlace\\2025_1st_Q\\fuu-facial-recognition\\opencv_java455.dll");
//	}
//
//	private JFrame frame;
//	private JLabel imageLabel;
//
//	public CameraGUI() {
//		frame = new JFrame("Camera GUI");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(800, 600);
//
//		imageLabel = new JLabel();
//		frame.add(imageLabel);
//		frame.setVisible(true);
//	}
//
//	public void startCamera() {
//		VideoCapture camera = new VideoCapture(0); // Open default camera (0)
//
//		if (!camera.isOpened()) {
//			System.out.println("Error: Camera not available!");
//			return;
//		}
//
//		// Load Haar cascade for face detection
//		String haarCascadePath = "haarcascade_frontalface_default.xml"; // Ensure this file exists in your working
//																		// directory
//		CascadeClassifier faceDetector = new CascadeClassifier(haarCascadePath);
//
//		Mat frameMat = new Mat();
//		Mat flippedMat = new Mat();
//
//		while (true) {
//			if (camera.read(frameMat)) {
//				// Mirror the frame
//				Core.flip(frameMat, flippedMat, 1); // Flip horizontally
//
//				// Detect faces
//				Mat gray = new Mat();
//				Imgproc.cvtColor(flippedMat, gray, Imgproc.COLOR_BGR2GRAY);
//				Imgproc.equalizeHist(gray, gray); // Enhance contrast
//
//				MatOfRect faces = new MatOfRect();
//				faceDetector.detectMultiScale(gray, faces);
//
//				// Iterate through detected faces
//				for (Rect face : faces.toArray()) {
//					Imgproc.rectangle(flippedMat, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
//				}
//
//				// Display the frame
//				ImageIcon image = new ImageIcon(matToBufferedImage(flippedMat));
//				imageLabel.setIcon(image);
//				imageLabel.repaint();
//			}
//		}
//	}
//
//	private BufferedImage matToBufferedImage(Mat mat) {
//		int width = mat.width();
//		int height = mat.height();
//		int channels = mat.channels();
//		byte[] source = new byte[width * height * channels];
//		mat.get(0, 0, source);
//
//		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//		image.getRaster().setDataElements(0, 0, width, height, source);
//		return image;
//	}
//
//	public static void main(String[] args) {
//		CameraGUI cameraGUI = new CameraGUI();
//		cameraGUI.startCamera();
//	}
//}