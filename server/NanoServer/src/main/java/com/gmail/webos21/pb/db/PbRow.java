package com.gmail.webos21.pb.db;

import java.util.Date;

public class PbRow {
    private Long id;
    private String siteUrl;
    private String siteName;
    private String siteType;
    private String myId;
    private String myPw;
    private Date regDate;
    private Date fixDate;
    private String memo;

    public PbRow(Long id, String siteUrl, String siteName, String siteType, String myId, String myPw,
                 Long regDate, Long fixDate, String memo) {
        this.id = id;
        this.siteUrl = siteUrl;
        this.siteName = siteName;
        this.siteType = siteType;
        this.myId = myId;
        this.myPw = myPw;
        this.regDate = new Date(regDate);
        this.fixDate = new Date(fixDate);
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

    public Date getFixDate() {
        return fixDate;
    }

    public void setFixDate(Date fixDate) {
        this.fixDate = fixDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String toJson() {
    	StringBuilder sb = new StringBuilder();

    	sb.append('{').append('\n');
    	sb.append("  \"id\": ").append(id).append(",\n");
    	sb.append("  \"siteUrl\": \"").append(siteUrl).append("\",\n");
    	sb.append("  \"siteName\": \"").append(siteName).append("\",\n");
    	sb.append("  \"siteType\": \"").append(siteType).append("\",\n");
    	sb.append("  \"myId\": \"").append(myId).append("\",\n");
    	sb.append("  \"myPw\": \"").append(myPw).append("\",\n");
    	sb.append("  \"regDate\": ").append(regDate.getTime()).append(",\n");
    	sb.append("  \"fixDate\": ").append(fixDate).append(",\n");
    	sb.append("  \"memo\": \"").append(memo).append("\",\n");
    	sb.append('}').append('\n');

    	return sb.toString();
    }
}
