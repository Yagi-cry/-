import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MemoAppFrameクラスは、メモ帳アプリケーションのメインフレームを表します。
 * メモの作成、保存、読み込み、削除、およびリマインダー設定機能を提供します。
 * また、経過時間とリアルタイム時計を表示します。
 */
public class MemoAppFrame extends JFrame {
    private JList<String> memoList;
    private DefaultListModel<String> listModel;
    private JTextArea memoContent;
    private JLabel timerLabel;
    private Timer timer;
    private int seconds;
    private JLabel realTimeLabel;
    private Timer realTimeTimer;
    private JDialog reminderDialog;
    private JSpinner dateSpinner;
    private JTextField reminderMessageField;
    private Timer reminderTimer;
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private DefaultComboBoxModel<String> categoryModel;

    /**
     * MemoAppFrameのコンストラクタ。
     * フレームの初期設定、GUIコンポーネントの配置、イベントリスナーの設定を行います。
     */
    public MemoAppFrame() {
        super("Memo App");

        // Timer setup for elapsed time
        timerLabel = new JLabel("Time: 0 sec");
        seconds = 0;
        timer = new Timer(1000, e -> {
            seconds++;
            timerLabel.setText("Time: " + seconds + " sec");
        });
        timer.start();

        // Real-time clock setup
        realTimeLabel = new JLabel();
        realTimeTimer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            realTimeLabel.setText("現在の日本の時刻!!: " + sdf.format(new Date()));
        });
        realTimeTimer.start();

        // Panel for status (timer and real-time clock)
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(timerLabel, BorderLayout.WEST);
        statusPanel.add(realTimeLabel, BorderLayout.EAST);

        // Add status panel to frame
        add(statusPanel, BorderLayout.SOUTH);

        // Memo List
        listModel = new DefaultListModel<>();
        memoList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(memoList);

        // Memo Content
        memoContent = new JTextArea();
        JScrollPane contentScrollPane = new JScrollPane(memoContent);

        // Search Field
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchMemoContent();
            }
        });

        // Category ComboBox
        categoryModel = new DefaultComboBoxModel<>();
        categoryComboBox = new JComboBox<>(categoryModel);
        categoryComboBox.addItem("All");
        categoryComboBox.addActionListener(e -> filterMemosByCategory());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchField, BorderLayout.NORTH);
        topPanel.add(categoryComboBox, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, contentScrollPane);
        splitPane.setDividerLocation(200);
        topPanel.add(splitPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem reminderItem = new JMenuItem("Set Reminder");
        JMenuItem addCategoryItem = new JMenuItem("Add Category");

        fileMenu.add(newItem);
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.add(deleteItem);
        fileMenu.add(reminderItem);
        fileMenu.addSeparator();
        fileMenu.add(addCategoryItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Event Listeners
        newItem.addActionListener(e -> createNewMemo());
        saveItem.addActionListener(e -> saveMemoToFile());
        loadItem.addActionListener(e -> loadMemoFromFile());
        deleteItem.addActionListener(e -> deleteMemo());
        exitItem.addActionListener(e -> System.exit(0));
        reminderItem.addActionListener(e -> showReminderDialog());
        addCategoryItem.addActionListener(e -> addNewCategory());

        // Add a list selection listener
        memoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadMemo();
            }
        });

        // Handle window closing event
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                minimizeToTray();
            }
        });
    }

    /**
     * 新しいメモを作成します。
     * ユーザにタイトルを入力させ、それをリストに追加します。
     */
    private void createNewMemo() {
        String title = JOptionPane.showInputDialog(this, "Enter memo title:");
        if (title != null && !title.trim().isEmpty()) {
            listModel.addElement(title);
            memoContent.setText("");
        }
    }

    /**
     * メモをファイルに保存します。
     * ユーザに保存先を選ばせ、選択されたファイルにメモ内容を書き込みます。
     */
    private void saveMemoToFile() {
        String category = (String) categoryComboBox.getSelectedItem();
        if (category == null || category.equals("All")) {
            JOptionPane.showMessageDialog(this, "Please select a category before saving.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = new File(fileChooser.getSelectedFile().getParent(), category + "_" + fileChooser.getSelectedFile().getName());
            try (PrintWriter out = new PrintWriter(new FileWriter(selectedFile))) {
                out.print(memoContent.getText());
                JOptionPane.showMessageDialog(this, "Memo saved to: " + selectedFile.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving memo: " + e.getMessage());
            }
        }
    }

    /**
     * ファイルからメモを読み込みます。
     * ユーザに読み込み先を選ばせ、選択されたファイルからメモ内容を読み込みます。
     */
    private void loadMemoFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedReader in = new BufferedReader(new FileReader(selectedFile))) {
                memoContent.read(in, null);
                listModel.addElement(selectedFile.getName());
                JOptionPane.showMessageDialog(this, "Memo loaded from: " + selectedFile.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading memo from file: " + e.getMessage());
            }
        }
    }

    /**
     * リストで選択されたメモを読み込みます。
     */
    private void loadMemo() {
        int selectedIndex = memoList.getSelectedIndex();
        if (selectedIndex != -1) {
            String title = listModel.get(selectedIndex);
            try (BufferedReader in = new BufferedReader(new FileReader(title + ".txt"))) {
                memoContent.read(in, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading memo: " + e.getMessage());
            }
        }
    }

    /**
     * リストで選択されたメモを削除します。
     */
    private void deleteMemo() {
        int selectedIndex = memoList.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
            memoContent.setText("");
            JOptionPane.showMessageDialog(this, "Memo deleted.");
        }
    }

    /**
     * リマインダー設定ダイアログを表示します。
     * ユーザにリマインダーの日時とメッセージを入力させます。
     */
    private void showReminderDialog() {
        reminderDialog = new JDialog(this, "Set Reminder", true);
        reminderDialog.setSize(400, 200);
        reminderDialog.setLayout(new BorderLayout());

        JLabel reminderLabel = new JLabel("Enter reminder date and time:");
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(dateSpinner, "yyyy/MM/dd HH:mm:ss");
        dateSpinner.setEditor(timeEditor);

        JLabel reminderMessageLabel = new JLabel("Reminder message:");
        reminderMessageField = new JTextField();

        JButton setReminderButton = new JButton("Set Reminder");
        setReminderButton.addActionListener(e -> setReminder());

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(reminderLabel);
        panel.add(dateSpinner);
        panel.add(reminderMessageLabel);
        panel.add(reminderMessageField);
        panel.add(new JLabel()); // Placeholder
        panel.add(setReminderButton);

        reminderDialog.add(panel, BorderLayout.CENTER);
        reminderDialog.setVisible(true);
    }

    /**
     * リマインダーを設定します。
     * 指定された日時にリマインダーメッセージを表示するタイマーを設定します。
     */
    private void setReminder() {
        Date reminderDate = (Date) dateSpinner.getValue();
        String reminderMessage = reminderMessageField.getText();

        long delay = reminderDate.getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            JOptionPane.showMessageDialog(this, "Please set a future date and time for the reminder.");
            return;
        }

        reminderTimer = new Timer((int) delay, e -> JOptionPane.showMessageDialog(this, reminderMessage));
        reminderTimer.setRepeats(false);
        reminderTimer.start();

        reminderDialog.dispose();
        JOptionPane.showMessageDialog(this, "Reminder set for: " + reminderDate);
    }

    /**
     * トレイアイコンを使ってアプリケーションをトレイに最小化します。
     */
    private void minimizeToTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Memo App");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
                tray.remove(trayIcon);
            });

            try {
                tray.add(trayIcon);
                setVisible(false);
            } catch (AWTException e) {
                System.err.println("Error adding to tray: " + e.getMessage());
            }
        } else {
            System.err.println("System tray not supported!");
        }
    }

    /**
     * メモの内容を検索フィールドの入力に基づいてハイライトします。
     */
    private void searchMemoContent() {
        Highlighter highlighter = memoContent.getHighlighter();
        highlighter.removeAllHighlights();
        String searchText = searchField.getText().toLowerCase();
        String content = memoContent.getText().toLowerCase();

        int index = content.indexOf(searchText);
        while (index >= 0) {
            try {
                int endIndex = index + searchText.length();
                highlighter.addHighlight(index, endIndex, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                index = content.indexOf(searchText, endIndex);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * メモのカテゴリをフィルタリングします。
     * 選択されたカテゴリに基づいてリストを更新します。
     */
    private void filterMemosByCategory() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        listModel.clear();
        // Here you would typically load the memos for the selected category from your data source
        // For now, just demonstrating with a simple example
        if ("All".equals(selectedCategory)) {
            // Load all memos
        } else {
            // Load memos for the selected category
        }
    }

    /**
     * 新しいカテゴリを追加します。
     * ユーザにカテゴリ名を入力させ、それをコンボボックスに追加します。
     */
    private void addNewCategory() {
        String newCategory = JOptionPane.showInputDialog(this, "Enter new category:");
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            categoryModel.addElement(newCategory);
            JOptionPane.showMessageDialog(this, "Category added: " + newCategory);
        }
    }
}

