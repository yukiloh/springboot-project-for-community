package xyz.murasakichigo.community.dto;

import java.sql.Date;

public class CommunityUser {


    private Integer id;
    private String username;
    private String github_account_id;
    private String token;
    private String gmt_create;
    private String gmt_modified;
    private String gmt_last_login;
    private String avatar_url;


    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    @Override
    public String toString() {
        return "CommunityUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", github_account_id='" + github_account_id + '\'' +
                ", token='" + token + '\'' +
                ", gmt_create=" + gmt_create +
                ", gmt_modified=" + gmt_modified +
                ", gmt_last_login=" + gmt_last_login +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGithub_account_id() {
        return github_account_id;
    }

    public void setGithub_account_id(String github_account_id) {
        this.github_account_id = github_account_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getGmt_create() {
        return gmt_create;
    }

    public void setGmt_create(String gmt_create) {
        this.gmt_create = gmt_create;
    }

    public String getGmt_modified() {
        return gmt_modified;
    }

    public void setGmt_modified(String gmt_modified) {
        this.gmt_modified = gmt_modified;
    }

    public String getGmt_last_login() {
        return gmt_last_login;
    }

    public void setGmt_last_login(String gmt_last_login) {
        this.gmt_last_login = gmt_last_login;
    }
}
