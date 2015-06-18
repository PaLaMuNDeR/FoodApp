package polimi.dima.foodapp;

/**
 * Created by Marti on 10/06/2015.
 */
public class Profile {
    int id;
    String name;
    String username;
    String photo;
    String cover;
    String gender;

    String email;

    public Profile(){}


    public Profile(int id, String name, String username, String photo, String cover, String gender, String email) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.photo = photo;
        this.cover = cover;
        this.gender = gender;
        this.email = email;
    }

    public Profile(String name, String username, String photo, String cover, String gender, String email){
        this.name = name;
        this.username = username;
        this.photo = photo;
        this.cover = cover;
        this.gender = gender;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}