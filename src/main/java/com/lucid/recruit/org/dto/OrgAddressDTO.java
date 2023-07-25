package com.lucid.recruit.org.dto;

import com.lucid.core.dto.BaseAddressDTO;
import com.lucid.recruit.org.entity.OrgCommunication;

import java.time.LocalDate;
import java.util.Date;

public class OrgAddressDTO extends BaseAddressDTO {

    private String orgAddressID;

    private LocalDate startDate;

    private LocalDate endDate;

    public String getOrgAddressID() {
        return orgAddressID;
    }

    public void setOrgAddressID(String orgAddressID) {
        this.orgAddressID = orgAddressID;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "OrgAddressDTO{}";
    }
}
