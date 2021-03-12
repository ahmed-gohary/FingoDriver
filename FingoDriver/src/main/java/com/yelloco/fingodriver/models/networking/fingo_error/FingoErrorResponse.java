package com.yelloco.fingodriver.models.networking.fingo_error;

import java.util.ArrayList;
import java.util.List;

public class FingoErrorResponse
{
    private List<FingoErrorObject> fingoErrorList;

    public FingoErrorResponse(){
        this.fingoErrorList = new ArrayList<>();
    }

    public FingoErrorResponse(List<FingoErrorObject> fingoErrorList) {
        this.fingoErrorList = fingoErrorList;
    }

    public List<FingoErrorObject> getFingoErrorList() {
        return fingoErrorList;
    }

    public void setFingoErrorList(List<FingoErrorObject> fingoErrorList) {
        this.fingoErrorList = fingoErrorList;
    }
}
