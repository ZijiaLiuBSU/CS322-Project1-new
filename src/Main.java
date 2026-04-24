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
                "Input",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input == null) {
            return;
        }

        input = input.trim();

        if (input.length() == 0) {
            System.out.println("No input provided.");
            return;
        }

        String[] parts = input.split("\\s+");

        if (parts.length != 2) {
            System.out.println("Please enter exactly a filename and a number of bars.");
            return;
        }

        String fileName = parts[0];
        String barText = parts[1];

        if (hasUnsafeText(fileName) || hasUnsafeText(barText)) {
            System.out.println("Blocked unsafe input.");
            return;
        }

        Path filePath = checkFile(fileName);

        if (filePath == null) {
            return;
        }

        int barCount;

        try {
            barCount = Integer.parseInt(barText);
        } catch (NumberFormatException e) {
            System.out.println("The number of bars must be a number.");
            return;
        }

        if (barCount <= 0) {
            System.out.println("The number of bars must be greater than 0.");
            return;
        }

        if (barCount > 1000) {
            System.out.println("The number of bars is too large.");
            return;
        }

        double[] samples = readSound(filePath.toString());

        if (samples == null) {
            return;
        }

        if (barCount > samples.length) {
            System.out.println("The number of bars is too large for this sound file.");
            return;
        }

        double[] bars = makeBars(samples, barCount);

        playAndDraw(fileName, samples, bars);
    }

    private static boolean hasUnsafeText(String text) {
        if (text.contains("..")) {
            return true;
        }

        if (text.contains("/")) {
            return true;
        }

        if (text.contains("\\")) {
            return true;
        }

        if (text.contains("|")) {
            return true;
        }

        if (text.contains("&")) {
            return true;
        }

        if (text.contains(";")) {
            return true;
        }

        if (text.contains(">")) {
            return true;
        }

        if (text.contains("<")) {
            return true;
        }

        if (text.contains("`")) {
            return true;
        }

        return false;
    }

    private static Path checkFile(String fileName) {
        if (!fileName.toLowerCase().endsWith(".wav")) {
            System.out.println("Only .wav files are allowed.");
            return null;
        }

        try {
            Path basePath = Paths.get("").toAbsolutePath().normalize();
            Path assetsPath = basePath.resolve("assets").normalize();
            Path inputPath = Paths.get(fileName);

            if (inputPath.isAbsolute()) {
                System.out.println("Absolute paths are not allowed.");
                return null;
            }

            Path fullPath = assetsPath.resolve(inputPath).normalize();

            if (!fullPath.startsWith(assetsPath)) {
                System.out.println("Paths outside the assets folder are not allowed.");
                return null;
            }

            if (!Files.exists(fullPath)) {
                System.out.println("The file does not exist.");
                return null;
            }

            if (!Files.isRegularFile(fullPath)) {
                System.out.println("The input is not a regular file.");
                return null;
            }

            return fullPath;
        } catch (InvalidPathException e) {
            System.out.println("The filename is not valid.");
            return null;
        }
    }

    private static double[] readSound(String fileName) {
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

        for (int i = 0; i < samples.length; i++) {
            if (Double.isNaN(samples[i]) || Double.isInfinite(samples[i])) {
                System.out.println("The sound file contains invalid sample values.");
                return null;
            }

            if (samples[i] < -1.0 || samples[i] > 1.0) {
                System.out.println("The sound file contains invalid sample values.");
                return null;
            }
        }

        return samples;
    }

    private static double[] makeBars(double[] samples, int barCount) {
        double[] bars = new double[barCount];
        int groupSize = samples.length / barCount;
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

            bars[i] = max;
        }

        return bars;
    }

    private static void playAndDraw(String fileName, double[] samples, double[] bars) {
        StdDraw.setCanvasSize(1000, 100);
        StdDraw.setTitle("File: assets/" + fileName);
        StdDraw.setXscale(0, bars.length);
        StdDraw.setYscale(-1.0, 1.0);
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenRadius(0.002);

        Thread soundThread = new Thread(new Runnable() {
            public void run() {
                StdAudio.play(samples);
                StdAudio.drain();
            }
        });

        soundThread.start();

        int groupSize = samples.length / bars.length;
        int pauseTime = (int) ((groupSize * 1000.0) / 44100);

        if (pauseTime < 1) {
            pauseTime = 1;
        }

        for (int i = 0; i < bars.length; i++) {
            drawBars(fileName, bars, i);
            StdDraw.pause(pauseTime);
        }
    }

    private static void drawBars(String fileName, double[] bars, int currentBar) {
        StdDraw.clear();

        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(bars.length / 2.0, 0.85, "File: assets/" + fileName);
        StdDraw.line(0, 0, bars.length, 0);

        for (int i = 0; i <= currentBar; i++) {
            setBarColor(i);

            double x = i + 0.5;
            double height = bars[i];

            StdDraw.line(x, -height, x, height);
        }

        StdDraw.show();
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