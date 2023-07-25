package com.lucid.recruit.org.dto;


import com.lucid.recruit.contract.dto.SignableUserDTO;
import com.lucid.recruit.docs.dto.BaseDocumentDTO;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class OrganizationDocumentDTO extends BaseDocumentDTO {
    private String organizationDocID;


    //if documentType is signable then weNeed multipleInd true or false
    private Boolean multipleInd;

    //if documentType is signable we need signableRequestable userDeatils

    private List<SignableUserDTO> signableUsers;
    private String docNumber;


    public String getOrganizationDocID() {
        return organizationDocID;
    }

    public void setOrganizationDocID(String contractDocID) {
        this.organizationDocID = contractDocID;
    }



    public Boolean getMultipleInd() {
        return multipleInd;
    }

    public void setMultipleInd(Boolean multipleInd) {
        this.multipleInd = multipleInd;
    }

    public List<SignableUserDTO> getSignableUsers() {
        return signableUsers;
    }

    public void setSignableUsers(List<SignableUserDTO> signableUsers) {
        this.signableUsers = signableUsers;
    }




    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }
}
