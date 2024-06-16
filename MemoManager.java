import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MemoManager {
    private List<Memo> memos;

    public MemoManager() {
        this.memos = new ArrayList<>();
    }

    public void addMemo(String title, String content) {
        memos.add(new Memo(title, content));
    }

    public void deleteMemo(String title) {
        memos.removeIf(memo -> memo.getTitle().equals(title));
    }

    public Memo getMemo(String title) {
        for (Memo memo : memos) {
            if (memo.getTitle().equals(title)) {
                return memo;
            }
        }
        return null;
    }

    public List<Memo> getAllMemos() {
        return new ArrayList<>(memos);
    }

    public void saveMemoToFile(Memo memo, String category) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = new File(fileChooser.getSelectedFile().getParent(), category + "_" + fileChooser.getSelectedFile().getName());
            try (PrintWriter out = new PrintWriter(new FileWriter(selectedFile))) {
                out.print(memo.getContent());
                JOptionPane.showMessageDialog(null, "Memo saved to: " + selectedFile.getName());
            }
        }
    }

    public Memo loadMemoFromFile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedReader in = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return new Memo(selectedFile.getName(), content.toString());
            }
        }
        return null;
    }
}
