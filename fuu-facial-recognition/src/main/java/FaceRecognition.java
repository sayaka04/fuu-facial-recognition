import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

public class FaceRecognition {

	public static void main(String[] args) {
		String trainingDir = "images/train/"; // Directory of training images
		String testImagePath = "images/images2.jpg"; // Path to the test image
		String haarCascadePath = "haarcascade_frontalface_default.xml"; // Absolute or relative path

		List<Mat> faceImages = new ArrayList<>();
		List<Integer> faceLabels = new ArrayList<>();

		File[] trainingFiles = new File(trainingDir).listFiles();
		if (trainingFiles == null || trainingFiles.length == 0) {
			System.out.println("No training files found in directory: " + trainingDir);
			return;
		}

		// Load Haar cascade file
		File cascadeFile = new File(haarCascadePath);
		if (!cascadeFile.exists()) {
			System.err.println("Error: Haar cascade file not found at " + cascadeFile.getAbsolutePath());
			return;
		}
		CascadeClassifier faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
		if (faceDetector.empty()) {
			System.err.println("Error: Could not load Haar cascade file.");
			return;
		}

		// Process training images
		for (File file : trainingFiles) {
			Mat image = imread(file.getAbsolutePath(), IMREAD_GRAYSCALE);
			RectVector faces = new RectVector();
			faceDetector.detectMultiScale(image, faces);

			if (faces.size() > 0) {
				Mat face = new Mat(image, faces.get(0));
				resize(face, face, new Size(128, 128));
				faceImages.add(face);

				try {
					int label = Integer.parseInt(file.getName().split("_")[0]);
					faceLabels.add(label);
				} catch (NumberFormatException e) {
					System.err.println("Error: Invalid label format in file name " + file.getName());
				}
			}
		}

		if (faceImages.isEmpty()) {
			System.out.println("No faces detected in training images.");
			return;
		}

		// Create labels Mat
		int[] labelArray = faceLabels.stream().mapToInt(i -> i).toArray();
		Mat labelsMat = new Mat(labelArray.length, 1, CV_32SC1);
		for (int i = 0; i < labelArray.length; i++) {
			labelsMat.ptr(i).putInt(labelArray[i]);
		}

		// Train the recognizer
		FaceRecognizer recognizer = EigenFaceRecognizer.create();
		recognizer.train(new MatVector(faceImages.toArray(new Mat[0])), labelsMat);

		// Test the recognizer
		Mat testImage = imread(testImagePath, IMREAD_GRAYSCALE);
		RectVector testFaces = new RectVector();
		faceDetector.detectMultiScale(testImage, testFaces);

		if (testFaces.size() > 0) {
			Mat testFace = new Mat(testImage, testFaces.get(0));
			resize(testFace, testFace, new Size(128, 128));

			int[] label = new int[1];
			double[] confidence = new double[1];
			recognizer.predict(testFace, label, confidence);

			System.out.println("Predicted Label: " + label[0]);
			System.out.println("Confidence Level: " + confidence[0]);

			if (confidence[0] < 4500) {
				System.out.println("Faces Match!");
			} else {
				System.out.println("Faces Do Not Match.");
			}
		} else {
			System.out.println("No face detected in the test image.");
		}
	}
}