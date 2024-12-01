package com.example.autone;

import android.widget.ImageView;

public class Identity_View {
    private String Name;
    private String Date_of_Birth;
    private String Gender;
    private String Identification_Number;
    private String Address;
    private String Date_of_Expiration;

    public Identity_View(){}

    //여기서부터 get,set 함수를 사용하는데 이부분을 통해 값을 가져옴
    public String getName() {return Name;}

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getDate_of_Birth() {return Date_of_Birth;}

    public void setDate_of_Birth(String Date_of_Birth) {
        this.Date_of_Birth = Date_of_Birth;
    }

    public String getGender() {return Gender;}

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public String Identification_Number() {
        return Identification_Number;
    }

    public void setIdentification_Number(String Identification_Number) { this.Identification_Number = Identification_Number;}

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getDate_of_Expiration() {
        return Date_of_Expiration;
    }

    public void setDate_of_Expiration(String Date_of_Expiration) {this.Date_of_Expiration = Date_of_Expiration;}

    //이거는 그룹을 생성할때 사용하는 부분
    public Identity_View(String Name, String Date_of_Birth, String Gender, String Identification_Number, String Address, String Date_of_Expiration) {
        this.Name = Name;
        this.Date_of_Birth = Date_of_Birth;
        this.Gender = Gender;
        this.Identification_Number = Identification_Number;
        this.Address = Address;
        this.Date_of_Expiration = Date_of_Expiration;
    }
}
