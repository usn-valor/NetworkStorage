package server;

public class User {

    private String nickName;
    private boolean isAuthorized;

    public User(String nickName, boolean isAuthorized) {
        this.nickName = nickName;
        this.isAuthorized = isAuthorized;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }
}
