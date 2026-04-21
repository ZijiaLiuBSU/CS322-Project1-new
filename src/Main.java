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

        System.out.println("Filename: " + fileName);
        System.out.println("Bars: " + barCount);
        System.out.println("Total samples: " + samples.length);
    }
}