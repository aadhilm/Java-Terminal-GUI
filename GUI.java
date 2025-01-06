import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class GUI {
    private JTextArea commandField;
    private JTextArea outputArea;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    private File currentDirectory;
    private ArrayList<String> commandHistory;
    private JSpinner fontSizeSpinner;

    public GUI() {
        currentDirectory = new File(System.getProperty("user.home"));
        commandHistory = new ArrayList<>();

        JFrame frame = new JFrame("Enhanced Terminal GUI");

        commandField = new JTextArea(3, 30);
        commandField.setFont(new Font("Monospaced", Font.BOLD, 14));
        commandField.setLineWrap(true);
        commandField.setBackground(Color.BLACK);
        commandField.setForeground(Color.GREEN);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setBackground(Color.BLACK);
        historyList.setForeground(Color.GREEN);
        historyList.setSelectionBackground(Color.DARK_GRAY);
        historyList.setSelectionForeground(Color.GREEN);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        frame.add(new JScrollPane(commandField), BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        controlPanel.add(new JLabel("Command History:"));
        controlPanel.add(new JScrollPane(historyList));

        JButton newWindowButton = new JButton("New Terminal");
        JButton bgColorButton = new JButton("Change Background Color");
        JButton textColorButton = new JButton("Change Text Color");
        JButton clearOutputButton = new JButton("Clear Output");
        JButton clearHistoryButton = new JButton("Clear History");
        JButton createFolderButton = new JButton("Create Folder");
        JButton createFileButton = new JButton("Create File");
        JButton deleteFileButton = new JButton("Delete File/Folder");
        JButton listFilesButton = new JButton("List Files");

        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(12, 8, 72, 1));
        fontSizeSpinner.addChangeListener(e -> updateFontSize((int) fontSizeSpinner.getValue()));

        controlPanel.add(newWindowButton);
        controlPanel.add(bgColorButton);
        controlPanel.add(textColorButton);
        controlPanel.add(clearOutputButton);
        controlPanel.add(clearHistoryButton);
        controlPanel.add(new JLabel("Font Size:"));
        controlPanel.add(fontSizeSpinner);
        controlPanel.add(createFolderButton);
        controlPanel.add(createFileButton);
        controlPanel.add(deleteFileButton);
        controlPanel.add(listFilesButton);

        frame.add(controlPanel, BorderLayout.NORTH);

        newWindowButton.addActionListener(e -> new GUI());
        bgColorButton.addActionListener(e -> changeColor(commandField, outputArea, frame, "background"));
        textColorButton.addActionListener(e -> changeColor(commandField, outputArea, frame, "text"));
        clearOutputButton.addActionListener(e -> outputArea.setText(""));
        clearHistoryButton.addActionListener(e -> clearHistory());
        
        // Folder and file operations
        createFolderButton.addActionListener(e -> createFolder());
        createFileButton.addActionListener(e -> createFile());
        deleteFileButton.addActionListener(e -> deleteFileOrFolder());
        listFilesButton.addActionListener(e -> listFiles());

        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String command = commandField.getText().trim();
                    if (!command.isEmpty()) {
                        commandHistory.add(command);
                        historyModel.addElement(command);
                    }
                    if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
                        System.exit(0);
                    } else {
                        outputArea.setText("");
                        executeCommand(command);
                    }
                    commandField.setText("");
                    e.consume();
                }
            }
        });

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void changeColor(JTextArea commandField, JTextArea outputArea, JFrame frame, String type) {
        Color color = JColorChooser.showDialog(frame, "Choose " + type + " color", commandField.getBackground());
        if (color != null) {
            if (type.equals("background")) {
                commandField.setBackground(color);
                outputArea.setBackground(color);
            } else {
                commandField.setForeground(color);
                outputArea.setForeground(color);
            }
        }
    }

    private void clearHistory() {
        commandHistory.clear();
        historyModel.clear();
    }

    private void updateFontSize(int size) {
        Font commandFont = commandField.getFont().deriveFont((float) size);
        commandField.setFont(commandFont);
        outputArea.setFont(commandFont);
    }

    private void executeCommand(String command) {
        try {
            if (command.startsWith("cd ")) {
                String dir = command.substring(3).trim();
                File newDir = new File(currentDirectory, dir);
                if (newDir.isDirectory()) {
                    currentDirectory = newDir;
                    outputArea.append("Changed directory to: " + newDir.getAbsolutePath() + "\n");
                } else {
                    outputArea.append("No such directory: " + dir + "\n");
                }
                return;
            }

            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            builder.directory(currentDirectory);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outputArea.append(line + "\n");
            }
            while ((line = errorReader.readLine()) != null) {
                outputArea.append(line + "\n");
            }

            process.waitFor();
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    // Folder and file functions
    private void createFolder() {
        String folderName = JOptionPane.showInputDialog("Enter folder name:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            File newFolder = new File(currentDirectory, folderName);
            if (newFolder.mkdir()) {
                outputArea.append("Folder created: " + newFolder.getAbsolutePath() + "\n");
            } else {
                outputArea.append("Failed to create folder.\n");
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
                } else {
                    outputArea.append("File already exists.\n");
                }
            } catch (IOException e) {
                outputArea.append("Error creating file: " + e.getMessage() + "\n");
            }
        }
    }

    private void deleteFileOrFolder() {
        String name = JOptionPane.showInputDialog("Enter file/folder name to delete:");
        if (name != null && !name.trim().isEmpty()) {
            File target = new File(currentDirectory, name);
            if (target.exists() && target.delete()) {
                outputArea.append("Deleted: " + target.getAbsolutePath() + "\n");
            } else {
                outputArea.append("Failed to delete: " + name + "\n");
            }
        }
    }

    private void listFiles() {
        File[] files = currentDirectory.listFiles();
        if (files != null) {
            outputArea.append("Files in directory " + currentDirectory.getAbsolutePath() + ":\n");
            for (File file : files) {
                outputArea.append(file.getName() + (file.isDirectory() ? " (Folder)" : " (File)") + "\n");
            }
        } else {
            outputArea.append("Failed to list files.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
