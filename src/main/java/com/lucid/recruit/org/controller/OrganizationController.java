package com.lucid.recruit.org.controller;

import com.lucid.recruit.org.OrganizationService;
import com.lucid.recruit.org.constants.OrgStatus;
import com.lucid.recruit.org.dto.OrgSummaryDTO;
import com.lucid.recruit.org.dto.OrganizationDTO;
import com.lucid.recruit.org.entity.Organization;
import com.lucid.recruit.org.validation.OrgValidation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/raves")
public class OrganizationController {

    private static final String ORG_CTRL_ERROR_00001 = "ORG_CTRL_ERROR_00001";

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrgValidation orgValidation;

    /**
     * @author : Rajesh
     * @Desc   : This API is used to save organisation and its nested entities
     *           upon receiving request from client as DTO is going to be validated
     *           based on the necessary checks and converted to DataBase entity in
     *           organisationServiceImpl class will be saved in respective DB tables  as new records.
     * @request: Organisation DTO
     */
    @PostMapping("v1/organization")
    public ResponseEntity<?> createOrganization(@RequestBody @Valid OrganizationDTO organizationDTO) throws Exception {
        OrganizationDTO  response= organizationService.createOrganization("System", organizationDTO);
        System.err.println("fdfb "+response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * @author : Rajesh
     * @Desc   : This API is used to update organisation and its nested entities
     *           upon receiving request from client as DTO is going to be validated
     *           based on the necessary checks and converted to DataBase entity in
     *           organisationServiceImpl class  will be updated in respective DB tables
     *           with Modified data.If organisation id is not found it will return validation
     *            logic itself without entering into serviceImpl.
     * @request: Organisation DTO with organisation Id
     * @param  : organisation Id
     */
    @PatchMapping("v1/organization/{id}")
    public ResponseEntity<Object> editOrganization(@RequestBody @Valid OrganizationDTO organizationDTO, @PathVariable("id") String organizationId) throws Exception {
        organizationDTO.setOrganizationID(organizationId);
        orgValidation.validateOrganization(organizationDTO);
        OrganizationDTO updatedOrganization = organizationService.editOrganization("System",organizationId ,organizationDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedOrganization);
    }


    /**
     * @author : Rajesh
     * @Desc   : This API is used to get the organisation record based on existing organisation Id
     * @param  : organizationId
     */
    @GetMapping("v1/organization/{id}")
    public ResponseEntity<Object> retrieveOrganization(@PathVariable(name = "id") String organizationId) throws Exception {
        return ResponseEntity.ok(organizationService.retrieveOrganization(organizationId));
    }

    /**
     * @author : Rajesh
     * @Desc   : This API is used to get all the organisations with filtering few attributes and pagination
     * @param  : organisationId,offset,limit,orgType,statusCode,emailId,name
     */
    @GetMapping("v1/organization")
    public ResponseEntity<Page<OrgSummaryDTO>> retrieveAllOrganizations(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "organizationID", required = false) String organizationID,
            @RequestParam(value = "orgType", required = false) String orgType,
            @RequestParam(value = "statusCode", required = false) OrgStatus statusCode,
            @RequestParam(value = "authSignataryEmail", required = false) String authSignataryEmail,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "authSignataryPhone", required = false) String authSignataryPhone
    ) {
        Page<OrgSummaryDTO> organizations = organizationService.retrieveAllOrganizations(offset, limit, organizationID, orgType, statusCode, authSignataryEmail, name, authSignataryPhone);
        return ResponseEntity.ok(organizations);
    }

//    /**
//     * @author : Rajesh
//     * @Desc   : This API is used to update the organisation status which considerably
//     * called as soft delete of organisation
//     * @param  : organisationId,status
//     */
//    @PostMapping("v1/organization/{id}/{status}")
//    public ResponseEntity<Object> statusCode(@RequestParam String organizationID, @RequestParam String status) {
//        String result = organizationService.updateStatus(organizationID, status);
//
//        return ResponseEntity.ok(organizationService.updateStatus(organizationID, status));
//    }

    /**
     * @author : Rajesh
     * @Desc   : This API is used to update the organisation tax
     * @param  : organisationId,taxId
     */
    @PatchMapping("v1/organization/{organizationId}/{taxId}")
    public ResponseEntity<String> updateTaxId(@PathVariable(name = "organizationId") String organizationId, @PathVariable("taxId") String taxId) {
        Organization updatedOrganization = organizationService.updateTaxId(organizationId, taxId);
        return ResponseEntity.status(HttpStatus.OK).body("Tax ID updated successfully");

    }

    /**
     * @author : Rajesh
     * @Desc   : dummy API
     *
     */
    @GetMapping("v1/webAddress")
    public String getOrganization(Model model) {
        String webAddress = "https://www.example.com";
        String domainName = organizationService.getDomainName(webAddress);
        model.addAttribute("domainName", domainName);
        return "organization";
    }

    /**
     * @author : Rajesh
     * @Desc   : This API is used to update the organisation status which considerably
     * called as soft delete of organisation
     * @param  : status and organizationId
     */
    @PutMapping("v1/status/{organizationID}/{status}")
    public String setOrganizationStatus(@PathVariable String organizationID, @PathVariable String status) {
        return  organizationService.setOrganizationStatus(organizationID, status);
    }


}

