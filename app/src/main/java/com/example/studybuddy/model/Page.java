package com.example.studybuddy.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Page implements Serializable {

    private String pageId;
    private String pageName;
    private String pagePhoto;
    private String pageDescription;
    private String search;
    private Map<String, String> members;

    public Page() {
        members = new HashMap<>();
    }

    public Page(String pageId, String pageName, String pagePhoto, String pageDescription, String search, Map<String, String> members) {
        this.pageId = pageId;
        this.pageName = pageName;
        this.pagePhoto = pagePhoto;
        this.pageDescription = pageDescription;
        this.search = search;
        this.members = members;
    }

    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getPagePhoto() { return pagePhoto; }
    public void setPagePhoto(String pagePhoto) { this.pagePhoto = pagePhoto; }

    public String getPageDescription() { return pageDescription; }
    public void setPageDescription(String pageDescription) { this.pageDescription = pageDescription; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public Map<String, String> getMembers() { return members; }
    public void setMembers(Map<String, String> members) { this.members = members; }
}
