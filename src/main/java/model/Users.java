package model;

public class Users {
    private long userID;
    private String userName;
    private String email;
    private String password;
    private String roleId;

    public Users(long userID, String userName, String email, String password, String roleId) {
        this.userID = userID;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
