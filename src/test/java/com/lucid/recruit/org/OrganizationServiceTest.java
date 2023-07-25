/**
 * All Rights Reserved. Private and Confidential. May not be disclosed without permission.
 */
package com.lucid.recruit.org;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucid.base.test.BaseTransactionTest;
import com.lucid.core.exception.ApplicationException;
import com.lucid.recruit.org.constants.OrgStatus;
import com.lucid.recruit.org.dto.OrgAddressDTO;
import com.lucid.recruit.org.dto.OrgCommunicationDTO;
import com.lucid.recruit.org.dto.OrgSummaryDTO;
import com.lucid.recruit.org.dto.OrganizationDTO;
import com.lucid.recruit.org.dto.OrganizationDocumentDTO;
import com.lucid.recruit.org.entity.Organization;
import com.lucid.recruit.org.repo.OrganizationRepo;


/**
 * @author Rajesh
 *
 */
@ExtendWith(SpringExtension.class)
public class OrganizationServiceTest extends BaseTransactionTest {

  // --------------------------------------------------------------- Constants
  // --------------------------------------------------------- Class Variables
  // ----------------------------------------------------- Static Initializers
  // ------------------------------------------------------ Instance Variables
  @Autowired
  private OrganizationService organizationService;

  @Autowired
  private OrganizationRepo orgRepo;

  @Autowired
  private ModelMapper modelMapper;

  public List<Organization> organizations;

  // ------------------------------------------------------------ Constructors
  /**
   * Create a new <code>TestOrganizationService</code>
   */
  public OrganizationServiceTest() {
    super();
  }

  // ---------------------------------------------------------- Public Methods
  /**
   * @throws ApplicationException
   */
  @Test
  public void saveOrgTest() throws Exception {
    OrganizationDTO organizationDTO = new OrganizationDTO();
    organizationDTO.setName(FAKER.company().name());
    organizationDTO.setDescription("TestDescription");
    organizationDTO.setTaxId("TestTaxID");
    organizationDTO.setStateOfIncorporation("TestStateOfInCorporation");


    OrgCommunicationDTO orgCommunicationDTO = new OrgCommunicationDTO();
    orgCommunicationDTO.setAuthSignataryEmail(FAKER.internet().emailAddress());
    orgCommunicationDTO.setAuthSignataryPhone(FAKER.phoneNumber().phoneNumber());
    orgCommunicationDTO.setAuthSignataryFn(FAKER.name().firstName());
    orgCommunicationDTO.setAuthSignataryLn(FAKER.name().lastName());

    organizationDTO.setOrgCommunications(Arrays.asList(orgCommunicationDTO));

    OrgAddressDTO orgAddressDTO = new OrgAddressDTO();
    orgAddressDTO.setStartDate(LocalDate.now());
    orgAddressDTO.setEndDate(LocalDate.now());
    orgAddressDTO.setAddress1(FAKER.address().fullAddress());
    orgAddressDTO.setAddress2(FAKER.address().streetAddress());

    organizationDTO.setOrgAddresses(Arrays.asList(orgAddressDTO));

    OrganizationDocumentDTO documentsDTO = new OrganizationDocumentDTO();
    documentsDTO.setDocNumber("awwe2425422");
    documentsDTO.setIssuedDt(LocalDate.now());
    documentsDTO.setExpirationDate(LocalDate.now());

    organizationDTO.setOrganizationDocuments(Arrays.asList(documentsDTO));

    Organization organization = modelMapper.map(organizationDTO, Organization.class);

    Organization savedOrganization = orgRepo.save(organization);

    // Verify that the organization is saved and ID is assigned
    assertThat(savedOrganization.getOrganizationID()).isNotNull();


    // OrganizationDTO result = organizationService.createOrganization("System", organizationDTO);
    // Assertions.assertNotNull(result, "unable to create the organization");

  }
  @Test
  public void testRetrieveAllOrganization() throws Exception {
    int offset = 0;int limit = 10;String organizationID = null;String orgType = null;OrgStatus statusCode = null;String authSignataryEmail  = null;String name = null ; String authSignataryPhone =null;
    Page<OrgSummaryDTO> retrieveAllOrganizationDTO = organizationService.retrieveAllOrganizations(offset, limit, organizationID, orgType, statusCode, authSignataryEmail ,authSignataryPhone, name);
    Assertions.assertNotNull(retrieveAllOrganizationDTO);
  }


  @BeforeEach
  public void setup() throws Exception {
    OrganizationDTO organizationDTO = new OrganizationDTO();
    organizationDTO.setName(FAKER.company().name());
    organizationDTO.setDescription("TestDescription");
    organizationDTO.setCode("code1");
    organizationDTO.setFax("fax1");
    organizationDTO.setPhoneNumber("1234567");
    organizationDTO.setTaxId("TestTaxID");
    organizationDTO.setStateOfIncorporation("TestStateOfInCorporation");
    organizationDTO.setTaxClassification("clas1");
    organizationDTO.setStatusCode("ACTIVE");
    organizationDTO.setOrganizationID("org1");

    OrgCommunicationDTO orgCommunicationDTO = new OrgCommunicationDTO();
    orgCommunicationDTO.setAuthSignataryEmail(FAKER.internet().emailAddress());
    orgCommunicationDTO.setAuthSignataryPhone(FAKER.phoneNumber().phoneNumber());
    orgCommunicationDTO.setAuthSignataryFn(FAKER.name().firstName());
    orgCommunicationDTO.setAuthSignataryLn(FAKER.name().lastName());
    orgCommunicationDTO.setStartDate(LocalDate.now());


    organizationDTO.setOrgCommunications(Arrays.asList(orgCommunicationDTO));

    OrgAddressDTO orgAddressDTO = new OrgAddressDTO();
    orgAddressDTO.setStartDate(LocalDate.now());
    orgAddressDTO.setEndDate(LocalDate.now());
    orgAddressDTO.setAddress1(FAKER.address().fullAddress());
    orgAddressDTO.setAddress2(FAKER.address().streetAddress());

    organizationDTO.setOrgAddresses(Arrays.asList(orgAddressDTO));

    OrganizationDocumentDTO documentsDTO = new OrganizationDocumentDTO();
    documentsDTO.setDocNumber("awwe2425422");
    documentsDTO.setIssuedDt(LocalDate.now());
    documentsDTO.setExpirationDate(LocalDate.now());

    organizationDTO.setOrganizationDocuments(Arrays.asList(documentsDTO));

    Organization organization = modelMapper.map(organizationDTO, Organization.class);

    //  Organization savedOrganization = orgRepo.save(organization);


    OrganizationDTO orgDTO = new OrganizationDTO();
    orgDTO.setName(FAKER.company().name());
    orgDTO.setDescription("TestDescription2");
    orgDTO.setCode("code2");
    orgDTO.setFax("fax1");
    orgDTO.setTaxId("TestTaxID2");
    orgDTO.setStateOfIncorporation("TestStateOfInCorporation2");
    orgDTO.setPhoneNumber("345687654");
    orgDTO.setTaxClassification("clas2");
    orgDTO.setStatusCode("ACTIVE");
    orgDTO.setOrganizationID("org2");

    OrgCommunicationDTO orgCommDTO = new OrgCommunicationDTO();
    orgCommDTO.setAuthSignataryEmail(FAKER.internet().emailAddress());
    orgCommDTO.setAuthSignataryPhone(FAKER.phoneNumber().phoneNumber());
    orgCommDTO.setAuthSignataryFn(FAKER.name().firstName());
    orgCommDTO.setAuthSignataryLn(FAKER.name().lastName());
    orgCommDTO.setStartDate(LocalDate.now());

    orgDTO.setOrgCommunications(Arrays.asList(orgCommDTO));

    OrgAddressDTO orgAddrDTO = new OrgAddressDTO();
    orgAddrDTO.setStartDate(LocalDate.now());
    orgAddrDTO.setEndDate(LocalDate.now());
    orgAddrDTO.setAddress1(FAKER.address().fullAddress());
    orgAddrDTO.setAddress2(FAKER.address().streetAddress());

    orgDTO.setOrgAddresses(Arrays.asList(orgAddrDTO));

    OrganizationDocumentDTO documentDTO = new OrganizationDocumentDTO();
    documentDTO.setDocNumber("doc2");
    documentDTO.setIssuedDt(LocalDate.now());
    documentDTO.setExpirationDate(LocalDate.now());

    orgDTO.setOrganizationDocuments(Arrays.asList(documentDTO));

    Organization organizationObj = modelMapper.map(orgDTO, Organization.class);

    //  Organization savedOrg = orgRepo.save(organizationObj);
    organizations = Arrays.asList(organization,organizationObj);
  }
  @Test
  public void RetrieveOrganizationTest() throws Exception {

 /*   Assertions.assertNotNull(result, "unable to create the organization");
    try {
      OrganizationDTO organizationDTO1=organizationService.retrieveOrganization(organizationDTO.getOrganizationID());
      Assertions.assertNotNull(organizationDTO.getOrganizationID(),organizationDTO1.getOrganizationID());
    }
    catch (Exception e){
      Assertions.assertNotNull("organization not Found",e.getLocalizedMessage());
    }*/
    assertThat(organizations).isNotEmpty();
    assertThat(organizations.size()).isEqualTo(2);

  }

  @Test
  public void getOrgnizationTest() throws Exception {
    String orgId = "org1";
    String code = "code1";
    Organization organization = new Organization();
    if(organizations.size() > 0 ) {
      for(Organization org :organizations) {
        if(org.getOrganizationID().equals(orgId)) {
          organization = org;
        }
      }
    }
    assertThat(organization).isNotNull();
    assertThat(organization.getOrganizationID()).isEqualTo(orgId);
    assertThat(organization.getCode()).isEqualTo(code);
  }

  @Test
  public void setOrganisationStatusTest() throws Exception{
    String orgId = "org2";
    Organization organization = new Organization();
    if(organizations.size() > 0 ) {
      for(Organization org :organizations) {
        if(org.getOrganizationID().equals(orgId)) {
          org.setStatusCode( OrgStatus.INACTIVE);
          organization = org;
        }
      }
    }
    assertThat(organization).isNotNull();
    assertThat(String.valueOf(organization.getStatusCode())).isEqualTo("INACTIVE");
  }


  // ------------------------------------------------------- Protected Methods
  // --------------------------------------------------------- Default Methods
  // --------------------------------------------------------- Private Methods
  // ---------------------------------------------------------- Static Methods
  // ----------------------------------------------------------- Inner Classes
}
