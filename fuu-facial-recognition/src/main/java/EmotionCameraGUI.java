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

public class EmotionCameraGUI {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV library
	}

	private JFrame frame;
	private JLabel imageLabel;
	private CascadeClassifier faceDetector;
	private CascadeClassifier smileDetector;

	public EmotionCameraGUI(String faceCascadePath, String smileCascadePath) {
		faceDetector = new CascadeClassifier(faceCascadePath);
		smileDetector = new CascadeClassifier(smileCascadePath);

		if (faceDetector.empty()) {
			System.out.println("Error: Face cascade failed to load!");
		}
		if (smileDetector.empty()) {
			System.out.println("Error: Smile cascade failed to load!");
		}

		frame = new JFrame("Emotion Detection");
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

		Mat frameMat = new Mat();
		Mat flippedMat = new Mat();

		while (true) {
			if (camera.read(frameMat)) {
				Core.flip(frameMat, flippedMat, 1); // Flip horizontally
				Mat gray = new Mat();
				Imgproc.cvtColor(flippedMat, gray, Imgproc.COLOR_BGR2GRAY);
				Imgproc.equalizeHist(gray, gray);

				MatOfRect faces = new MatOfRect();
				faceDetector.detectMultiScale(gray, faces);

				for (Rect face : faces.toArray()) {
					Mat faceROI = gray.submat(face);

					// Perform smile detection
					String emotion = detectEmotion(faceROI);

					// Draw rectangle and label
					Imgproc.rectangle(flippedMat, face.tl(), face.br(), new Scalar(0, 255, 0), 2);
					Imgproc.putText(flippedMat, emotion, face.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0,
							new Scalar(0, 255, 0), 2);
				}

				// Display the frame
				ImageIcon image = new ImageIcon(matToBufferedImage(flippedMat));
				imageLabel.setIcon(image);
				imageLabel.repaint();
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String detectEmotion(Mat faceROI) {
		MatOfRect smiles = new MatOfRect();
		// Tune parameters for better smile detection
		smileDetector.detectMultiScale(faceROI, smiles, 1.1, 15, 0, new org.opencv.core.Size(25, 25),
				new org.opencv.core.Size());

		if (smiles.toArray().length > 0) {
			System.out.println("Smile detected!");
			return "Smiling"; // Label for detected emotion
		}
		System.out.println("No smile detected.");
		return "Neutral"; // Default label if no smile is detected
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
		// Provide XML file paths
		String faceCascadePath = "haarcascade_frontalface_default.xml"; // Replace with your file path
		String smileCascadePath = "haarcascade_smile.xml"; // Replace with your file path

		EmotionCameraGUI cameraGUI = new EmotionCameraGUI(faceCascadePath, smileCascadePath);
		cameraGUI.startCamera();
	}
}