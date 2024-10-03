package io.invokegs;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;

/// # Test Task Assignment
///
/// Create a single page application. The app has 2 main screens, Intro & Sort:
///  - Input field for the number of random numbers to generate.
///  - Clicking “enter” button takes to next screen, Sort.
///  - Sort screen has a list of numbers, a sort button, and a reset button.
///
/// Numbers buttons:
///  - Show X random numbers (depends on data entered by user in the previous screen)
///  - The max number value is 1000
///  - At least one value should be equal or less than 30
///  - Present maximum 10 numbers in a column. If there are more numbers, add another column
///
/// Sort button:
///  - Clicking the sort button will sort the presented numbers in a descending order
///  - Clicking the sort button again, will change it to increasing order
///  - The screen should be updated after each iteration of quick-sort
/// (i.e. re-implement quick-sort; copy/paste of existing implementation is permitted).
///
/// Reset button:
///  - Takes back to intro screen.
///
/// Clicking one of the numbers button:
///  - If the clicked value is equal or less than 30, present X new random numbers on the screen
///  - If the clicked value is more than 30, pop up a message “Please select a value smaller or equal to 30.”
public class SortApp extends JFrame {
    private static final Color BLUE_COLOR = Color.decode("#4472c4");
    private static final Color GREEN_COLOR = Color.decode("#00b050");

    private final JPanel mainPanel = new JPanel(new CardLayout());
    private final JPanel introPanel = new JPanel();
    private final JPanel sortPanel = new JPanel();
    private JButton enterButton;
    private JButton sortButton;
    private JButton resetButton;
    private JTextField inputField;
    private Integer[] numbers;
    private JButton[] numberButtons;
    private int numberCount = 0;
    private Timer timer;
    private boolean descending = false;

    private JScrollPane scrollPane;
    private JPanel numbersPanel;

    public SortApp() {
        setTitle("Sort Application");
        setSize(600, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        setupIntroScreen();
        setupSortScreen();

        mainPanel.add(introPanel, "Intro");
        mainPanel.add(sortPanel, "Sort");

        add(mainPanel);
    }

    private void setupIntroScreen() {
        introPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel promptLabel = new JLabel("How many numbers to display?");
        gbc.gridx = 0;
        gbc.gridy = 0;
        introPanel.add(promptLabel, gbc);

        inputField = new JTextField(5);
        gbc.gridy++;
        introPanel.add(inputField, gbc);

        enterButton = new JButton("Enter");
        enterButton.setBackground(BLUE_COLOR);
        enterButton.setForeground(Color.WHITE);
        enterButton.addActionListener(e -> {
            try {
                numberCount = Integer.parseInt(inputField.getText().trim());
                if (numberCount <= 0) {
                    throw new NumberFormatException();
                }
                showSortScreen();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SortApp.this, "Please enter a valid positive number.");
            }
        });
        gbc.gridy++;
        introPanel.add(enterButton, gbc);
    }

    private void setupSortScreen() {
        sortPanel.setLayout(new BorderLayout());

        // Create number buttons panel
        numbersPanel = new JPanel();
        scrollPane = new JScrollPane(numbersPanel);
        sortPanel.add(scrollPane, BorderLayout.CENTER);

        sortButton = new JButton("Sort");
        sortButton.setBackground(GREEN_COLOR);
        sortButton.setForeground(Color.WHITE);
        sortButton.setSize(100, 50);
        resetButton = new JButton("Reset");
        resetButton.setBackground(GREEN_COLOR);
        resetButton.setForeground(Color.WHITE);
        resetButton.setSize(100, 50);

        sortButton.addActionListener(e -> {
            if (numbers != null && timer == null) {
                sortButton.setEnabled(false); // Disable the sort button during sorting
                descending = !descending;
                sort(numbers,
                        descending ? Comparator.reverseOrder() : Comparator.naturalOrder(),
                        100,
                        () -> sortButton.setEnabled(true),
                        this::updateNumbersOnScreen
                );
            }
        });

        resetButton.addActionListener(e -> {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            showIntroScreen();
        });

        // Create a right side panel with buttons at the top
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(sortButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(resetButton);
        rightPanel.add(buttonPanel, BorderLayout.NORTH);

        sortPanel.add(rightPanel, BorderLayout.EAST);
    }

    private void showIntroScreen() {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "Intro");
        inputField.setText("");
    }

    private void showSortScreen() {
        numbers = generateRandomNumbers();
        createNumberButtons();
        updateNumbersOnScreen();
        sortButton.setEnabled(true);
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "Sort");
    }

    private Integer[] generateRandomNumbers() {
        Random random = new Random();

        boolean hasLessThanThirty = false;
        numbers = new Integer[numberCount];
        for (int i = 0; i < numberCount; i++) {
            numbers[i] = random.nextInt(1000) + 1;
            if (numbers[i] <= 30) hasLessThanThirty = true;
        }

        if (!hasLessThanThirty) {
            numbers[random.nextInt(numberCount)] = random.nextInt(30) + 1;
        }

        return numbers;
    }

    private void createNumberButtons() {
        numbersPanel.removeAll();
        numbersPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int rowsPerColumn = 10;
        numberButtons = new JButton[numberCount];

        for (int i = 0; i < numberCount; i++) {
            numberButtons[i] = new JButton();
            numberButtons[i].setPreferredSize(new Dimension(80, 30));
            numberButtons[i].setBackground(BLUE_COLOR);
            numberButtons[i].setForeground(Color.WHITE);
            numberButtons[i].setFocusPainted(false);
            int index = i;
            numberButtons[i].addActionListener(e -> onNumberButtonClick(numbers[index]));

            gbc.gridx = i / rowsPerColumn;
            gbc.gridy = i % rowsPerColumn;
            numbersPanel.add(numberButtons[i], gbc);
        }

        numbersPanel.revalidate();
        numbersPanel.repaint();
    }


    private void onNumberButtonClick(int number) {
        if (timer != null) {
            JOptionPane.showMessageDialog(this, "Please wait until the sorting is done.");
            return;
        }

        if (number <= 30) {
            numbers = generateRandomNumbers();
            createNumberButtons();
            updateNumbersOnScreen();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a value smaller or equal to 30.");
        }
    }

    private void updateNumbersOnScreen() {
        for (int i = 0; i < numberButtons.length; i++) {
            numberButtons[i].setText(numbers[i].toString());
        }
    }

    private <T> void sort(T[] array, Comparator<T> comparator, int delay,
                          Runnable onFinish, Runnable onStep) {

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, array.length - 1});

        timer = new Timer(delay, e -> {
            if (!stack.isEmpty()) {
                int[] range = stack.pop();
                int low = range[0];
                int high = range[1];

                if (low < high) {
                    int pi = partition(array, low, high, comparator);

                    if (pi + 1 < high) {
                        stack.push(new int[]{pi + 1, high});
                    }
                    if (low < pi - 1) {
                        stack.push(new int[]{low, pi - 1});
                    }
                }
                onStep.run();
            } else {
                ((Timer) e.getSource()).stop();
                timer = null;
                onFinish.run();
            }
        });

        timer.start();
    }

    private static <T> int partition(T[] array, int low, int high, Comparator<T> comparator) {
        T pivot = array[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(T[] array, int i, int j) {
        if (i == j) return;
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SortApp app = new SortApp();
            app.setVisible(true);
        });
    }
}
