package com.example.myapplicationwa;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "city")
public class CityEntity  {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "city")
    private String city;

    @ColumnInfo(name = "temp")
    private String temp;

    public CityEntity() {
    }

    public CityEntity(String city, String temp) {
        this.city = city;
        this.temp = temp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

}
