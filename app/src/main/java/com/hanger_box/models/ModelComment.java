package com.hanger_box.models;

public class ModelComment {
    String uid;
    String uavatar;

    String comment;

    String ptime;
    String repid;
    String repuavatar;
    String repcomment;
    String repptime;

    String readstatus;

    public String getuid() {
        return uid;
    }

    public void setuid(String uid) {
        this.uid = uid;
    }

    public String getrepid() {
        return repid;
    }

    public void setrepid(String repid) {
        this.repid = repid;
    }
    public String getuavatar() {
        return uavatar;
    }

    public void setuavatar(String uavatar) {
        this.uavatar = uavatar;
    }

    public String getrepuavatar() {
        return repuavatar;
    }

    public void setrepuavatar(String repuavatar) {
        this.repuavatar = repuavatar;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getrepComment() {
        return repcomment;
    }

    public void setrepComment(String repcomment) {
        this.repcomment = repcomment;
    }

    public String getPtime() {
        return ptime;
    }

    public void setPtime(String ptime) {
        this.ptime = ptime;
    }

    public String getrepPtime() {
        return repptime;
    }

    public void setrepPtime(String repptime) {
        this.repptime = repptime;
    }

    public String getreadstatus() {
        return readstatus;
    }

    public void setreadstatus(String readstatus) {
        this.readstatus = readstatus;
    }

    public ModelComment() {
    }

    public ModelComment(String uid, String uavatar, String comment, String ptime, String repid, String repuavatar, String repcomment, String repptime, String readstatus) {
        this.uid = uid;
        this.uavatar = uavatar;
        this.comment = comment;
        this.ptime = ptime;
        this.repid = repid;
        this.repuavatar = repuavatar;
        this.repcomment = repcomment;
        this.repptime = repptime;
        this.readstatus = readstatus;
    }
}
