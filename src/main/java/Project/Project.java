package Project;

import java.sql.Timestamp;

public class Project {
    private int projectId;
    private int userId;
    private String title;
    private String language;
    private String codeContent;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Project(int projectId, int userId, String title, String language, String codeContent, Timestamp createdAt, Timestamp updatedAt) {
        this.projectId = projectId;
        this.userId = userId;
        this.title = title;
        this.language = language;
        this.codeContent = codeContent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getProjectId() { return projectId; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getLanguage() { return language; }
    public String getCodeContent() { return codeContent; }
    public void setCodeContent(String codeContent) { this.codeContent = codeContent; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}