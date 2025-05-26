package com.example.wofi;

/**
 * מחלקת מודל המשתמש
 * מייצגת משתמש במערכת (בעל מקצוע או לקוח)
 * מכילה את כל המידע הבסיסי על המשתמש
 */
public class User {
    /** שם המשתמש */
    public String username;
    
    /** כתובת דוא"ל */
    public String email;
    
    /** מספר טלפון */
    public String phone;
    
    /** סוג המשתמש (בעל מקצוע/לקוח) */
    public String userType;
    
    /** מקצוע (רלוונטי רק לבעלי מקצוע) */
    public String profession;
    
    /** כתובת */
    public String address;
    
    /** תיאור */
    public String description;
    
    /** מזהה ייחודי של המשתמש */
    private String userId;

    /**
     * בנאי ריק - נדרש על ידי Firebase
     * משמש ליצירת אובייקט ריק בעת טעינת נתונים מהשרת
     */
    public User() {
    }

    /**
     * בנאי מלא ליצירת משתמש חדש
     * @param username שם המשתמש
     * @param email כתובת דוא"ל
     * @param phone מספר טלפון
     * @param userType סוג המשתמש
     * @param profession מקצוע (רלוונטי רק לבעלי מקצוע)
     */
    public User(String username, String email, String phone, String userType, String profession) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.profession = profession;
    }

    /**
     * מחזיר את מזהה המשתמש
     * @return מזהה המשתמש
     */
    public String getUserId() {
        return userId;
    }

    /**
     * מעדכן את מזהה המשתמש
     * @param userId מזהה המשתמש החדש
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * מחזיר את שם המשתמש
     * @return שם המשתמש
     */
    public String getUsername() {
        return username;
    }

    /**
     * מעדכן את שם המשתמש
     * @param username שם המשתמש החדש
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * מחזיר את מספר הטלפון
     * @return מספר הטלפון
     */
    public String getPhone() {
        return phone;
    }

    /**
     * מעדכן את מספר הטלפון
     * @param phone מספר הטלפון החדש
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
