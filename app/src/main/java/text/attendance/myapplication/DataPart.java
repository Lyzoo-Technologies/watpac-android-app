package text.attendance.myapplication;

public class DataPart {
    private String fileName;
    private byte[] content;
    private String type;

    public DataPart(String fileName, byte[] content, String type) {
        this.fileName = fileName;
        this.content = content;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public String getType() {
        return type;
    }
}
