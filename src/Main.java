import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog(
                null,
                "Enter filename and number of bars to display"
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

        if (parts.length < 2) {
            System.out.println("Please enter a filename and a number.");
            return;
        }

        String fileName = parts[0];
        String barCount = parts[1];

        System.out.println("Filename: " + fileName);
        System.out.println("Bars: " + barCount);
    }
}