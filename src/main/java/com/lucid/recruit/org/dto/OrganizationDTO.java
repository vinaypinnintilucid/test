package com.lucid.recruit.org.dto;


import com.lucid.core.dto.BaseDTO;
import com.lucid.recruit.org.entity.OrganizationDocument;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.aspectj.bridge.Message;

import java.util.List;

public class OrganizationDTO extends BaseDTO {

    private String organizationID;


    @NotBlank(message ="name need to fill" )
    private String name;


    @NotBlank(message = "phoneNumber need to fill")
   // @Pattern(regexp = "^[0-9()\\-]+$")
    private String phoneNumber;

    @NotBlank(message = "fax need to fill")
    private String fax;

    @NotBlank(message = "description need to fill")
    private String description;

    @NotBlank(message = "taxId need to fill")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Tax ID must contain only numbers and alphanumeric characters")
    private String taxId;

    private String code;

    private String statusCode;

    @Valid
    @NotEmpty
    private List<OrgDomainDTO> orgDomains;

    @NotBlank(message = "taxClassification need to fill")
    private String taxClassification;
    @NotBlank
    private String stateOfIncorporation;

    @Valid
    @NotEmpty
    private List<OrgCommunicationDTO> orgCommunications;

    @Valid
    @NotNull
    @NotEmpty(message = "orgAddresses need to fill")
    private List<OrgAddressDTO> orgAddresses;

    @Valid
    @NotEmpty(message = "organizationDocuments need to fill")
    private List<OrganizationDocumentDTO> organizationDocuments;
    public OrganizationDTO(String organizationID) {
        this.organizationID = organizationID;
    }

    public OrganizationDTO() {
        super();
    }

    public OrganizationDTO(String organizationID, String name, String description, String taxId) {
        super();
        this.organizationID = organizationID;
        this.name = name;
        this.description = description;
        this.taxId = taxId;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(String organizationID) {
        this.organizationID = organizationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getStateOfIncorporation() {
        return stateOfIncorporation;
    }

    public void setStateOfIncorporation(String stateOfIncorporation) {
        this.stateOfIncorporation = stateOfIncorporation;
    }

    public List<OrgCommunicationDTO> getOrgCommunications() {
        return orgCommunications;
    }

    public void setOrgCommunications(List<OrgCommunicationDTO> orgCommunications) {
        this.orgCommunications = orgCommunications;
    }

    public List<OrgAddressDTO> getOrgAddresses() {
        return orgAddresses;
    }

    public void setOrgAddresses(List<OrgAddressDTO> orgAddresses) {
        this.orgAddresses = orgAddresses;
    }

    public List<OrganizationDocumentDTO> getOrganizationDocuments() {
        return organizationDocuments;
    }

    public void setOrganizationDocuments(List<OrganizationDocumentDTO> organizationDocuments) {
        this.organizationDocuments = organizationDocuments;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public List<OrgDomainDTO> getOrgDomains() {
        return orgDomains;
    }

    public void setOrgDomains(List<OrgDomainDTO> orgDomains) {
        this.orgDomains = orgDomains;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getTaxClassification() {
        return taxClassification;
    }

    public void setTaxClassification(String taxClassification) {
        this.taxClassification = taxClassification;
    }

   /* @Override
    public String toString() {
        return "OrganizationDTO{" +
                "organizationID='" + organizationID + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", fax='" + fax + '\'' +
                ", description='" + description + '\'' +
                ", taxId='" + taxId + '\'' +
                ", code='" + code + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", orgDomains=" + orgDomains +
                ", taxClassification='" + taxClassification + '\'' +
                ", stateOfIncorporation='" + stateOfIncorporation + '\'' +
                ", orgCommunications=" + orgCommunications +
                ", orgAddresses=" + orgAddresses +
                ", organizationDocuments=" + organizationDocuments +
                '}';
    }*/
}
