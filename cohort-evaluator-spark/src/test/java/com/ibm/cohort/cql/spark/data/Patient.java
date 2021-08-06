package com.ibm.cohort.cql.spark.data;

import java.time.LocalDate;
import java.util.Random;

public class Patient {
    private String id;
    private String gender;
    private LocalDate birthDate;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public LocalDate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public static Patient randomInstance() {
        Random random = new Random();
        
        Patient p = new Patient();
        p.setId(String.valueOf(random.nextInt()));
        p.setGender( random.nextBoolean() ? "male" : "female" );
        p.setBirthDate( LocalDate.of(1970 + random.nextInt(30), 1 + random.nextInt(11), 1 + random.nextInt(27)));
        return p;
    }
}
