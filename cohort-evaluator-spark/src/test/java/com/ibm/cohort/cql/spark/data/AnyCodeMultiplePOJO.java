package com.ibm.cohort.cql.spark.data;

import java.io.Serializable;

public class AnyCodeMultiplePOJO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codeStr1;
    private String system1;
    private String display1;

    private String codeStr2;
    private String system2;
    private String display2;

    private String codeStr3;
    private String system3;
    private String display3;

    public void setCodeStr1(String codeStr1, String system1, String display1) {
        this.codeStr1 = codeStr1;
        this.system1 = system1;
        this.display1 = display1;
    }

    public void setCodeStr2(String codeStr2, String system2, String display2) {
        this.codeStr2 = codeStr2;
        this.system2 = system2;
        this.display2 = display2;
    }

    public void setCodeStr3(String codeStr3, String system3, String display3) {
        this.codeStr3 = codeStr3;
        this.system3 = system3;
        this.display3 = display3;
    }

    public String getCodeStr1() {
        return codeStr1;
    }

    public String getSystem1() {
        return system1;
    }

    public String getDisplay1() {
        return display1;
    }

    public String getCodeStr2() {
        return codeStr2;
    }

    public String getSystem2() {
        return system2;
    }

    public String getDisplay2() {
        return display2;
    }

    public String getCodeStr3() {
        return codeStr3;
    }

    public String getSystem3() {
        return system3;
    }

    public String getDisplay3() {
        return display3;
    }
}
