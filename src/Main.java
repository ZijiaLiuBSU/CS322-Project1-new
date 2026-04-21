import javax.swing.JOptionPane;

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

        String[] parts = input.split("\\s+");

        if (parts.length != 2) {
            System.out.println("Please enter exactly a filename and a number of bars.");
            return;
        }

        String fileName = parts[0];
        String barText = parts[1];
        int barCount;

        try {
            barCount = Integer.parseInt(barText);
        } catch (NumberFormatException e) {
            System.out.println("The number of bars must be an integer.");
            return;
        }

        double[] samples;

        try {
            samples = StdAudio.read(fileName);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not read the audio file.");
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
        playAndDraw(fileName, heights, samples.length);
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