import javax.swing.JOptionPane;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(
                null,
                "Enter filename and number of bars to display",
                "assets/cardinal_trim.wav 100"
        );

        if (input == null) {
            return;
        }

        input = input.trim();

        if (input.length() == 0) {
            System.out.println("No input provided.");
            return;
        }

        String[] parts = splitInput(input);

        if (parts == null) {
            System.out.println("Please enter exactly a filename and a number of bars.");
            return;
        }

        String fileName = parts[0];
        String barText = parts[1];

        if (containsBlockedText(fileName) || containsBlockedText(barText)) {
            System.out.println("Blocked unsafe input.");
            return;
        }

        Path filePath = validateFilePath(fileName);
        if (filePath == null) {
            return;
        }

        int barCount;

        try {
            barCount = Integer.parseInt(barText);
        } catch (NumberFormatException e) {
            System.out.println("The number of bars must be an integer.");
            return;
        }

        double[] samples = readSamples(filePath.toString());
        if (samples == null) {
            return;
        }

        if (barCount <= 0) {
            System.out.println("The number of bars must be greater than 0.");
            return;
        }

        if (barCount > samples.length) {
            System.out.println("The number of bars is too large for this sound file.");
            return;
        }

        double[] heights = buildBars(samples, barCount);
        playAndDraw(filePath.toString(), heights, samples.length);
    }

    private static String[] splitInput(String input) {
        int splitIndex = -1;

        for (int i = input.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(input.charAt(i))) {
                splitIndex = i;
                break;
            }
        }

        if (splitIndex == -1) {
            return null;
        }

        String fileName = input.substring(0, splitIndex).trim();
        String barText = input.substring(splitIndex + 1).trim();

        if (fileName.length() == 0 || barText.length() == 0) {
            return null;
        }

        if (barText.contains(" ") || barText.contains("\t")) {
            return null;
        }

        return new String[]{fileName, barText};
    }

    private static boolean containsBlockedText(String text) {
        return text.contains("..")
                || text.contains("|")
                || text.contains("&")
                || text.contains(";")
                || text.contains(">")
                || text.contains("<")
                || text.contains("`");
    }

    private static Path validateFilePath(String fileName) {
        if (!fileName.toLowerCase().endsWith(".wav")) {
            System.out.println("Only .wav files are allowed.");
            return null;
        }

        try {
            Path basePath = Paths.get("").toAbsolutePath().normalize();
            Path inputPath = Paths.get(fileName);

            if (inputPath.isAbsolute()) {
                System.out.println("Absolute paths are not allowed.");
                return null;
            }

            Path fullPath = basePath.resolve(inputPath).normalize();

            if (!fullPath.startsWith(basePath)) {
                System.out.println("Paths outside the project are not allowed.");
                return null;
            }

            if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
                System.out.println("The file does not exist.");
                return null;
            }

            return fullPath;
        } catch (InvalidPathException e) {
            System.out.println("The filename is not valid.");
            return null;
        }
    }

    private static double[] readSamples(String fileName) {
        double[] samples;

        try {
            samples = StdAudio.read(fileName);
        } catch (Exception e) {
            System.out.println("The file could not be read as a valid wav file.");
            return null;
        }

        if (samples == null || samples.length == 0) {
            System.out.println("The sound file is empty or invalid.");
            return null;
        }

        for (double sample : samples) {
            if (Double.isNaN(sample) || Double.isInfinite(sample)) {
                System.out.println("The sound file contains invalid sample values.");
                return null;
            }

            if (sample < -1.0 || sample > 1.0) {
                System.out.println("The sound file contains invalid sample values.");
                return null;
            }
        }

        return samples;
    }

    private static double[] buildBars(double[] samples, int barCount) {
        int groupSize = samples.length / barCount;
        double[] heights = new double[barCount];
        int index = 0;

        for (int i = 0; i < barCount; i++) {
            double max = 0.0;

            for (int j = 0; j < groupSize; j++) {
                double value = Math.abs(samples[index]);
                if (value > max) {
                    max = value;
                }
                index++;
            }

            heights[i] = max;
        }

        return heights;
    }

    private static void playAndDraw(String fileName, double[] heights, int sampleCount) {
        StdDraw.setCanvasSize(1400, 350);
        StdDraw.setTitle("File: " + fileName);
        StdDraw.setXscale(0, heights.length);
        StdDraw.setYscale(-1.2, 1.2);
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenRadius(0.003);

        int groupSize = sampleCount / heights.length;
        int pauseTime = (int) ((groupSize * 1000.0) / StdAudio.SAMPLE_RATE);

        if (pauseTime < 1) {
            pauseTime = 1;
        }

        StdAudio.playInBackground(fileName);

        for (int i = 0; i < heights.length; i++) {
            StdDraw.clear();

            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.text(heights.length / 2.0, 1.0, "File: " + fileName);
            StdDraw.line(0, 0.72, heights.length, 0.72);

            for (int j = 0; j <= i; j++) {
                setBarColor(j);
                double x = j + 0.5;
                double height = heights[j] * 0.45;
                StdDraw.line(x, -height, x, height);
            }

            StdDraw.show();
            StdDraw.pause(pauseTime);
        }
    }

    private static void setBarColor(int index) {
        int colorIndex = index % 6;

        if (colorIndex == 0) {
            StdDraw.setPenColor(StdDraw.RED);
        } else if (colorIndex == 1) {
            StdDraw.setPenColor(StdDraw.ORANGE);
        } else if (colorIndex == 2) {
            StdDraw.setPenColor(StdDraw.GREEN);
        } else if (colorIndex == 3) {
            StdDraw.setPenColor(StdDraw.BLUE);
        } else if (colorIndex == 4) {
            StdDraw.setPenColor(StdDraw.MAGENTA);
        } else {
            StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        }
    }
}