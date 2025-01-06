import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class JavaTerminal {
    private JTextArea commandField;
    private JTextArea outputArea;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    private File currentDirectory;
    private ArrayList<String> commandHistory;
    private JLabel dateTimeLabel;
    private JList<File> fileList;
    private JButton timeDayButton; // New button for displaying time and day
    private Color textColor = Color.GREEN; // Default text color
    private Font currentFont; // Variable to hold the current font
    private JSpinner fontSizeSpinner; // Spinner for font size

    // New toggle buttons
    private JToggleButton hideHistoryButton;
    private JToggleButton hideFilesButton;

    public JavaTerminal() {
        currentDirectory = new File(System.getProperty("user.home"));
        commandHistory = new ArrayList<>();
        currentFont = new Font("Consolas", Font.PLAIN, 12); // Initialize font

        JFrame frame = new JFrame("Enhanced Terminal GUI");
        frame.setLayout(new BorderLayout());

        commandField = new JTextArea(3, 30);
        commandField.setFont(currentFont.deriveFont(Font.BOLD, 14)); // Bold for command field
        commandField.setLineWrap(true);
        commandField.setBackground(Color.BLACK);
        commandField.setForeground(textColor); // Set text color here

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(currentFont); // Set font to current font
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(textColor); // Set text color here

        // Command history list
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(currentFont); // Set font to current font
        historyList.setBackground(Color.BLACK);
        historyList.setForeground(textColor); // Set text color here
        historyList.setSelectionBackground(Color.DARK_GRAY);
        historyList.setSelectionForeground(textColor); // Set selected text color
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setPreferredSize(new Dimension(200, 200));

        dateTimeLabel = new JLabel(getCurrentDateTime());
        dateTimeLabel.setForeground(Color.WHITE);
        dateTimeLabel.setBackground(Color.BLUE); // Highlight background
        dateTimeLabel.setOpaque(true); // Make background visible
        dateTimeLabel.setFont(currentFont.deriveFont(Font.PLAIN, 16)); // Increase font size for better visibility

        // New JButton to display time and day
        timeDayButton = new JButton(getCurrentTimeAndDay());
        timeDayButton.setPreferredSize(new Dimension(150, 30)); // Set button size
        timeDayButton.setForeground(textColor);
        timeDayButton.setBackground(Color.BLACK); // Set background color
        timeDayButton.setFont(currentFont); // Set font to current font
        timeDayButton.setFocusable(false); // Make it non-focusable

        // File List with icons
        fileList = new JList<>();
        fileList.setCellRenderer(new FileCellRenderer());
        fileList.setFont(currentFont); // Set font to current font
        JScrollPane fileScrollPane = new JScrollPane(fileList);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add buttons for commands
        JButton newWindowButton = new JButton("New Terminal");
        JButton bgColorButton = new JButton("Change Background Color");
        JButton textColorButton = new JButton("Change Text Color");
        JButton clearOutputButton = new JButton("Clear Output");
        JButton clearHistoryButton = new JButton("Clear History");
        JButton createFolderButton = new JButton("Create Folder");
        JButton createFileButton = new JButton("Create File");
        JButton deleteFileButton = new JButton("Delete File/Folder");
        JButton listFilesButton = new JButton("List Files");

        // Set font to buttons
        newWindowButton.setFont(currentFont);
        bgColorButton.setFont(currentFont);
        textColorButton.setFont(currentFont);
        clearOutputButton.setFont(currentFont);
        clearHistoryButton.setFont(currentFont);
        createFolderButton.setFont(currentFont);
        createFileButton.setFont(currentFont);
        deleteFileButton.setFont(currentFont);
        listFilesButton.setFont(currentFont);
        timeDayButton.setFont(currentFont); // Button for time and day

        // Create spinner for font size
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(12, 8, 72, 1)); // Start at 12, min 8, max 72
        fontSizeSpinner.setPreferredSize(new Dimension(60, 30));
        fontSizeSpinner.setFont(currentFont);
        fontSizeSpinner.addChangeListener(e -> {
            int size = (Integer) fontSizeSpinner.getValue();
            currentFont = currentFont.deriveFont((float) size); // Update current font size
            updateFont(); // Update font for all components
        });

        // New toggle buttons for hiding history and files
        hideHistoryButton = new JToggleButton("Hide History");
        hideFilesButton = new JToggleButton("Hide Files");
        hideHistoryButton.setFont(currentFont);
        hideFilesButton.setFont(currentFont);

        controlPanel.add(newWindowButton);
        controlPanel.add(bgColorButton);
        controlPanel.add(textColorButton);
        controlPanel.add(new JLabel("Font Size:")); // Label for font size
        controlPanel.add(fontSizeSpinner); // Add font size spinner
        controlPanel.add(clearOutputButton);
        controlPanel.add(clearHistoryButton);
        controlPanel.add(createFolderButton);
        controlPanel.add(createFileButton);
        controlPanel.add(deleteFileButton);
        controlPanel.add(listFilesButton);
        controlPanel.add(timeDayButton); // Add the timeDayButton to the control panel
        controlPanel.add(hideHistoryButton); // Add hide history toggle
        controlPanel.add(hideFilesButton); // Add hide files toggle

        // Add components to frame
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(createLabeledScrollPane(outputArea, "Output Box"), BorderLayout.CENTER);
        frame.add(createLabeledScrollPane(commandField, "Input Box"), BorderLayout.SOUTH);
        frame.add(createLabeledScrollPane(historyList, "History Box"), BorderLayout.EAST);
        frame.add(dateTimeLabel, BorderLayout.WEST);
        frame.add(createLabeledScrollPane(fileList, "File List"), BorderLayout.WEST);

        // Action Listeners
        newWindowButton.addActionListener(e -> new EdexStyledGUI());
        bgColorButton.addActionListener(e -> changeColor("background"));
        textColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(frame, "Choose Text Color", textColor);
            if (newColor != null) {
                textColor = newColor; // Update the color variable
                setAllTextColor(textColor); // Update text colors
            }
        });
        clearOutputButton.addActionListener(e -> outputArea.setText(""));
        clearHistoryButton.addActionListener(e -> clearHistory());
        createFolderButton.addActionListener(e -> createFolder());
        createFileButton.addActionListener(e -> createFile());
        deleteFileButton.addActionListener(e -> deleteFileOrFolder());
        listFilesButton.addActionListener(e -> updateFileList());

        // Update command history
        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String command = commandField.getText().trim();
                    if (!command.isEmpty()) {
                        commandHistory.add(command);
                        historyModel.addElement(command);
                        executeCommand(command);
                    }
                    commandField.setText("");
                    e.consume();
                }
            }
        });

        // Action listener for hiding history
        hideHistoryButton.addActionListener(e -> {
            historyList.setVisible(!hideHistoryButton.isSelected());
            hideHistoryButton.setText(hideHistoryButton.isSelected() ? "Show History" : "Hide History");
        });

        // Action listener for hiding files
        hideFilesButton.addActionListener(e -> {
            fileList.setVisible(!hideFilesButton.isSelected());
            hideFilesButton.setText(hideFilesButton.isSelected() ? "Show Files" : "Hide Files");
        });

        // Frame settings
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Timer to update date and time every second
        Timer timer = new Timer(1000, e -> {
            dateTimeLabel.setText(getCurrentDateTime());
            timeDayButton.setText(getCurrentTimeAndDay());
        });
        timer.start();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd:MM:yyyy");
        return dateFormat.format(new Date());
    }

    private String getCurrentTimeAndDay() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(new Date()) + " " + new SimpleDateFormat("EEEE").format(new Date());
    }

    private void changeColor(String type) {
        Color color = JColorChooser.showDialog(null, "Choose " + type + " Color", Color.WHITE);
        if (color != null) {
            if (type.equals("background")) {
                outputArea.setBackground(color);
                commandField.setBackground(color);
            }
        }
    }

    private void clearHistory() {
        commandHistory.clear();
        historyModel.clear();
    }

    private void executeCommand(String command) {
        try {
            if (command.startsWith("cd ")) {
                String dir = command.substring(3);
                File newDir = new File(currentDirectory, dir);
                if (newDir.isDirectory()) {
                    currentDirectory = newDir;
                    outputArea.append("Changed directory to: " + currentDirectory.getAbsolutePath() + "\n");
                    updateFileList(); // Update file list display
                } else {
                    outputArea.append("No such directory: " + dir + "\n");
                }
            } else {
                // Execute other commands as necessary
                outputArea.append("Executing: " + command + "\n");
                // Simulate command execution or use Runtime.exec() if applicable
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    outputArea.append(line + "\n");
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    outputArea.append("Command exited with code: " + exitCode + "\n");
                }
            }
        } catch (IOException | InterruptedException e) {
            outputArea.append("Error executing command: " + e.getMessage() + "\n");
        }
    }

    private void createFolder() {
        String folderName = JOptionPane.showInputDialog("Enter folder name:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            File newFolder = new File(currentDirectory, folderName);
            if (newFolder.mkdir()) {
                outputArea.append("Folder created: " + newFolder.getAbsolutePath() + "\n");
                updateFileList(); // Update file list display
            } else {
                outputArea.append("Failed to create folder. It may already exist.\n");
            }
        }
    }

    private void createFile() {
        String fileName = JOptionPane.showInputDialog("Enter file name:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            File newFile = new File(currentDirectory, fileName);
            try {
                if (newFile.createNewFile()) {
                    outputArea.append("File created: " + newFile.getAbsolutePath() + "\n");
                    updateFileList(); // Update file list display
                } else {
                    outputArea.append("Failed to create file. It may already exist.\n");
                }
            } catch (IOException e) {
                outputArea.append("Error creating file: " + e.getMessage() + "\n");
            }
        }
    }

    private void deleteFileOrFolder() {
        String name = JOptionPane.showInputDialog("Enter file/folder name to delete:");
        if (name != null && !name.trim().isEmpty()) {
            File fileToDelete = new File(currentDirectory, name);
            if (fileToDelete.exists()) {
                if (fileToDelete.isDirectory()) {
                    // If it's a directory, delete recursively
                    try {
                        Files.walk(fileToDelete.toPath())
                                .sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                        outputArea.append("Folder deleted: " + fileToDelete.getAbsolutePath() + "\n");
                    } catch (IOException e) {
                        outputArea.append("Error deleting folder: " + e.getMessage() + "\n");
                    }
                } else {
                    if (fileToDelete.delete()) {
                        outputArea.append("File deleted: " + fileToDelete.getAbsolutePath() + "\n");
                    } else {
                        outputArea.append("Failed to delete file.\n");
                    }
                }
                updateFileList(); // Update file list display
            } else {
                outputArea.append("No such file/folder: " + name + "\n");
            }
        }
    }

    private void updateFileList() {
        File[] files = currentDirectory.listFiles();
        DefaultListModel<File> fileListModel = new DefaultListModel<>();
        if (files != null) {
            for (File file : files) {
                fileListModel.addElement(file);
            }
        }
        fileList.setModel(fileListModel);
    }

    private JScrollPane createLabeledScrollPane(Component component, String label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(new JScrollPane(component), BorderLayout.CENTER);
        return new JScrollPane(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EdexStyledGUI::new);
    }

    private class FileCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            File file = (File) value;
            setText(file.getName());
            setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
            setBackground(isSelected ? Color.LIGHT_GRAY : Color.BLACK);
            setForeground(textColor); // Use consistent text color here
            return this;
        }
    }

    private void updateFont() {
        commandField.setFont(currentFont.deriveFont(Font.BOLD, 14)); // Bold for command field
        outputArea.setFont(currentFont);
        historyList.setFont(currentFont);
        fileList.setFont(currentFont);
        timeDayButton.setFont(currentFont);
        dateTimeLabel.setFont(currentFont.deriveFont(Font.PLAIN, 16)); // Adjust date/time label font size
        setAllTextColor(textColor); // Ensure text color is maintained after font change
    }

    private void setAllTextColor(Color color) {
        commandField.setForeground(color);
        outputArea.setForeground(color);
        historyList.setForeground(color);
        fileList.setForeground(color);
        dateTimeLabel.setForeground(color);
        timeDayButton.setForeground(color);
        hideHistoryButton.setForeground(color);
        hideFilesButton.setForeground(color);
    }
}
