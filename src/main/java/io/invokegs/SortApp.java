package io.invokegs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Random;

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
    private static final int MAX_NUMBER_VALUE = 1000;
    private static final int MIN_SPECIAL_NUMBER = 30;
    private static final int ROWS_PER_COLUMN = 10;
    private static final int MAX_NUMBER_COUNT = 1000;
    private static final int SORT_DELAY = 300;

    private final Random random = new Random();
    private final JPanel mainPanel = new JPanel(new CardLayout());
    private final JPanel introPanel = new JPanel();
    private final JPanel sortPanel = new JPanel();
    private JButton sortButton;
    private JTextField inputField;
    private Integer[] numbers;
    private JButton[] numberButtons;
    private int numberCount = 0;
    private Timer timer;
    private boolean descending = false;

    private JPanel numbersPanel;

    private final Deque<int[]> swapDeque = new ArrayDeque<>();
    private Integer[] planningArray;

    public SortApp() {
        setTitle("Sort Application");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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

        JButton enterButton = createEnterButton();
        gbc.gridy++;
        introPanel.add(enterButton, gbc);
    }

    private JButton createEnterButton() {
        JButton enterButton = new JButton("Enter");
        enterButton.setBackground(BLUE_COLOR);
        enterButton.setForeground(Color.WHITE);
        enterButton.addActionListener(e -> {
            try {
                numberCount = Integer.parseInt(inputField.getText().trim());
                if (numberCount <= 0 || numberCount > MAX_NUMBER_COUNT) {
                    JOptionPane.showMessageDialog(SortApp.this, "Please enter a number between 1 and " + MAX_NUMBER_COUNT + ".");
                    return;
                }
                showSortScreen();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(SortApp.this, "Please enter a valid positive number.");
            }
        });
        return enterButton;
    }

    private void setupSortScreen() {
        sortPanel.setLayout(new BorderLayout());

        numbersPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(numbersPanel);
        sortPanel.add(scrollPane, BorderLayout.CENTER);

        sortButton = new JButton("Sort");
        sortButton.setBackground(GREEN_COLOR);
        sortButton.setForeground(Color.WHITE);
        sortButton.setSize(100, 50);

        JButton resetButton = new JButton("Reset");
        resetButton.setBackground(GREEN_COLOR);
        resetButton.setForeground(Color.WHITE);
        resetButton.setSize(100, 50);

        sortButton.addActionListener(e -> {
            if (numbers != null && timer == null) {
                descending = !descending;
                sortButton.setEnabled(false);
                swapDeque.clear();

                Comparator<Integer> comparator = descending ? Comparator.reverseOrder() : Comparator.naturalOrder();
                quickSortPlan(planningArray, 0, planningArray.length - 1, comparator);

                executePlannedSwaps(() -> sortButton.setEnabled(true), this::updateNumbersOnScreen);
            }
        });

        resetButton.addActionListener(e -> {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            showIntroScreen();
        });

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
        generateRandomNumbers();
        createNumberButtons();
        updateNumbersOnScreen();
        sortButton.setEnabled(true);
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "Sort");
    }

    private void generateRandomNumbers() {
        boolean hasLessThanThirty = false;
        numbers = new Integer[numberCount];
        for (int i = 0; i < numberCount; i++) {
            numbers[i] = random.nextInt(MAX_NUMBER_VALUE) + 1;
            if (numbers[i] <= MIN_SPECIAL_NUMBER) hasLessThanThirty = true;
        }

        if (!hasLessThanThirty) {
            numbers[random.nextInt(numberCount)] = random.nextInt(MIN_SPECIAL_NUMBER) + 1;
        }

        planningArray = numbers.clone();
    }

    private void createNumberButtons() {
        numbersPanel.removeAll();
        numbersPanel.setLayout(new GridBagLayout());
        numberButtons = new JButton[numberCount];

        for (int i = 0; i < numberCount; i++) {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(80, 30));
            button.setBackground(BLUE_COLOR);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);

            Integer number = numbers[i];
            button.addActionListener(e -> onNumberButtonClick(number));

            numberButtons[i] = button;
        }

        updateNumbersOnScreen();
        updateButtonsInPanel();
    }

    private void onNumberButtonClick(int number) {
        if (timer != null) {
            JOptionPane.showMessageDialog(this, "Please wait until the sorting is done.");
            return;
        }

        if (number <= MIN_SPECIAL_NUMBER) {
            numberCount = number;
            generateRandomNumbers();
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

    private void updateButtonsInPanel() {
        numbersPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        for (int i = 0; i < numberButtons.length; i++) {
            gbc.gridx = i / ROWS_PER_COLUMN;
            gbc.gridy = i % ROWS_PER_COLUMN;
            numbersPanel.add(numberButtons[i], gbc);
        }

        numbersPanel.revalidate();
        numbersPanel.repaint();
    }

    private <T> void quickSortPlan(T[] array, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pi = partitionPlan(array, low, high, comparator);

            quickSortPlan(array, low, pi - 1, comparator);
            quickSortPlan(array, pi + 1, high, comparator);
        }
    }

    private <T> int partitionPlan(T[] array, int low, int high, Comparator<T> comparator) {
        T pivot = array[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                if (i != j) {
                    swapDeque.add(new int[]{i, j});
                    swap(array, i, j);
                }
            }
        }

        swapDeque.add(new int[]{i + 1, high});
        swap(array, i + 1, high);

        return i + 1;
    }

    private void executePlannedSwaps(Runnable onFinish, Runnable onStep) {
        timer = new Timer(SORT_DELAY, e -> {
            if (!swapDeque.isEmpty()) {
                int[] swapPair = swapDeque.pop();
                int i = swapPair[0];
                int j = swapPair[1];

                swap(numbers, i, j);
                swapButtons(i, j);

                updateButtonsInPanel();
                highlightSwap(i, j);

                onStep.run();
            } else {
                ((Timer) e.getSource()).stop();
                timer = null;
                onFinish.run();
            }
        });

        timer.start();
    }

    private void swapButtons(int i, int j) {
        JButton tempButton = numberButtons[i];
        numberButtons[i] = numberButtons[j];
        numberButtons[j] = tempButton;
    }

    private void highlightSwap(int i, int j) {
        numberButtons[i].setBackground(Color.GRAY);
        numberButtons[j].setBackground(Color.GRAY);

        Timer highlightTimer = new Timer(SORT_DELAY / 2, e -> {
            numberButtons[i].setBackground(BLUE_COLOR);
            numberButtons[j].setBackground(BLUE_COLOR);
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
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
