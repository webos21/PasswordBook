package com.gmail.webos21.passwordbook.db;

import java.util.Date;

public class PbRow {
    private Long id;
    private String siteUrl;
    private String siteName;
    private String siteType;
    private String myId;
    private String myPw;
    private Date regDate;
    private String memo;

    public PbRow(Long id, String siteUrl, String siteName, String siteType, String myId, String myPw, Long regDate, String memo) {
        this.id = id;
        this.siteUrl = siteUrl;
        this.siteName = siteName;
        this.siteType = siteType;
        this.myId = myId;
        this.myPw = myPw;
        this.regDate = new Date(regDate);
        this.memo = memo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String getMyPw() {
        return myPw;
    }

    public void setMyPw(String myPw) {
        this.myPw = myPw;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
