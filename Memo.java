/**
 * Memoクラスは、メモのタイトルと内容を保持するデータモデルです。
 */
public class Memo {
    private String title;
    private String content;

    /**
     * Memoのコンストラクタ。
     * 
     * @param title メモのタイトル
     * @param content メモの内容
     */
    public Memo(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * メモのタイトルを取得します。
     * 
     * @return メモのタイトル
     */
    public String getTitle() {
        return title;
    }

    /**
     * メモのタイトルを設定します。
     * 
     * @param title メモのタイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * メモの内容を取得します。
     * 
     * @return メモの内容
     */
    public String getContent() {
        return content;
    }

    /**
     * メモの内容を設定します。
     * 
     * @param content メモの内容
     */
    public void setContent(String content) {
        this.content = content;
    }
}

