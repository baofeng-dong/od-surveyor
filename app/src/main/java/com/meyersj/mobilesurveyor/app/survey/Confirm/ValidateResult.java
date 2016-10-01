package com.meyersj.mobilesurveyor.app.survey.Confirm;

/**
 * Created by dongb on 10/1/2016.
 */

public class ValidateResult {

    private Boolean isValid = true;
    private Integer tabIndex = -1;
    private String error = "";

    public ValidateResult() {
        this.isValid = true;
        this.tabIndex = -1;
        this.error = "";
    }

    public ValidateResult(Boolean isValid, Integer tabIndex, String error) {
        this.isValid = isValid;
        this.tabIndex = tabIndex;
        this.error = error;
    }

    public Integer getTabIndex() {
        return tabIndex;
    }

    public Boolean getValid() {
        return isValid;
    }

    public String getError() {
        return error;
    }
}
