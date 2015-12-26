package org.skellig.feature;

public class InitDetails implements TestPreRequisites<InitDetails> {

    private String id;
    private String filePath;

    public InitDetails(String id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public InitDetails getDetails() {
        return this;
    }
}
