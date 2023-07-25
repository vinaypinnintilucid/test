/**
 * All Rights Reserved. Private and Confidential. May not be disclosed without permission.
 */
package com.lucid.recruit.org;

import com.lucid.core.azure.AzureDocumentUtility;
import com.lucid.core.exception.EntityNotFoundException;
import com.lucid.core.exception.ErrorCodes;
import com.lucid.core.exception.InvalidDataException;
import com.lucid.recruit.org.constants.OrgStatus;
import com.lucid.recruit.org.customRepo.OrganizationCustomRepo;
import com.lucid.recruit.org.dto.*;
import com.lucid.recruit.org.entity.*;
import com.lucid.recruit.org.exception.OrgNotFoundException;
import com.lucid.recruit.org.repo.*;
import com.lucid.recruit.org.validation.OrgValidation;
import com.lucid.recruit.person.dto.PersonAddressDTO;
import com.lucid.recruit.person.dto.PersonLegalDTO;
import com.lucid.recruit.person.entity.PersonAddress;
import com.lucid.recruit.worker.entity.Worker;
import com.lucid.util.FileUtility;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.apache.commons.validator.routines.UrlValidator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lucid.core.base.BaseServiceImpl;
import com.lucid.core.exception.ApplicationException;
import jakarta.persistence.EntityManager;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * @author sgutti
 * @date 16-Mar-2023 6:25:34 am
 */
@Component(OrganizationServiceImpl.SERVICE_NAME)
public class OrganizationServiceImpl extends BaseServiceImpl implements OrganizationService {

    // --------------------------------------------------------------- Constants
    public static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    public static final String SERVICE_NAME = "organizationService";
    public static final String ERROR_ORG_0001 = "ERROR_ORG_0001";
    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    // --------------------------------------------------------- Class Variables
    // ----------------------------------------------------- Static Initializers
    // ------------------------------------------------------ Instance Variables
    @Autowired
    private OrganizationRepo organizationRepo;

    @Autowired
    private OrgCommunicationRepo orgCommunicationRepo;

    @Autowired
    private OrgAddressRepo orgAddressRepo;


    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrganizationDocumentRepo organizationDocumentRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OrganizationCustomRepo organizationCustomRepo;

    @Autowired
    private OrgDomainRepo orgDomainRepo;
    @Autowired
    private AzureDocumentUtility azureDocumentUtility;
    @Autowired
    private FileUtility fileUtility;

    @Autowired
    private OrgValidation orgValidation;
    @Autowired
    private Validator validator;

    //@Value("${azure.doc.upload.organization.container}")
    private String containerName;


    // ------------------------------------------------------------ Constructors

    /**
     * Create a new <code>OrganizationServiceImpl</code>
     */
    public OrganizationServiceImpl() {
        super();
    }

    /**
     * Creates a new organization with the provided user ID and organization data.
     *
     * @param userID          The ID of the user creating the organization.
     * @param organizationDTO The DTO containing the organization data.
     * @return The created organization DTO.
     * @throws DataAccessException  If there is an error accessing the data.
     * @throws PersistenceException If there is an error persisting the data.
     * @desc This method creates an organization by mapping and saving its details, including addresses,
     * communications, domains, and documents. The key points include populating and saving associated
     * entities, updating foreign key relationships, and handling exceptions.
     */
    @Override
    @Transactional
    public OrganizationDTO createOrganization(String userID, OrganizationDTO organizationDTO) {

        orgValidation.validateOrganization(organizationDTO);
        // Map the organizationDTO to an Organization entity
        Organization organization = modelMapper.map(organizationDTO, Organization.class);

        // Set created by and created date
        organization.setCreatedBy(userID);
        organization.setCreatedDt(LocalDate.now());

        // Set initial values
        organization.setCode("TestCode");
        organization.setUpdatedBy(userID);
        //organization.setUpdatedDt(LocalDate.now());
        organization.setStatusCode(OrgStatus.ACTIVE);

        /**
         * @Desc   :  Collecting all orgAddresses from organisation DTO and
         * adding those it in a collection object by converting into DB entities
         *
         */

        if (organizationDTO.getOrgAddresses() != null && !organizationDTO.getOrgAddresses().isEmpty()) {
            List<OrgAddress> orgAddresses = new ArrayList<>();
            organizationDTO.getOrgAddresses().forEach(orgAddressDTO -> {
                // Map orgAddressDTO to OrgAddress entity
                OrgAddress orgAddress = modelMapper.map(orgAddressDTO, OrgAddress.class);
                orgAddress.setOrganization(organization);
                orgAddresses.add(orgAddress);
            });
            organization.setOrgAddresses(orgAddresses.isEmpty() ? null : orgAddresses);
        }

        /**
         * @Desc   :  Collecting all orgCommunications from organisation DTO and
         * adding those it in a collection object by converting into DB entities
         *
         */
        if (organizationDTO.getOrgCommunications() != null && !organizationDTO.getOrgCommunications().isEmpty()) {
            List<OrgCommunication> orgCommunications = new ArrayList<>();
            organizationDTO.getOrgCommunications().forEach(orgCommunicationDTO -> {
                // Map orgCommunicationDTO to OrgCommunication entity
                OrgCommunication orgCommunication = modelMapper.map(orgCommunicationDTO, OrgCommunication.class);
                orgCommunication.setOrganization(organization);
                orgCommunication.setCreatedBy(userID);
                orgCommunication.setCreatedDt(LocalDate.now());
                orgCommunications.add(orgCommunication);
            });
            organization.setOrgCommunications(orgCommunications.isEmpty() ? null : orgCommunications);
        }

        /**
         * @Desc   :  Collecting all orgDomains from organisation DTO and adding those it in a collection
         *  object by converting into DB entities
         *
         */
        if (organizationDTO.getOrgDomains() != null && !organizationDTO.getOrgDomains().isEmpty()) {
            List<OrgDomain> orgDomains = new ArrayList<>();
            organizationDTO.getOrgDomains().forEach(orgDomainDTO -> {
                // Map orgDomainDTO to OrgDomain entity
                OrgDomain orgDomain = modelMapper.map(orgDomainDTO, OrgDomain.class);
                orgDomain.setDomainStatus("Active");
                orgDomain.setOrganization(organization);
                orgDomain.setCreatedBy(userID);
                orgDomain.setCreatedDt(LocalDate.now());
                orgDomains.add(orgDomain);
            });
            organization.setOrgDomains(orgDomains.isEmpty() ? null : orgDomains);
        }

        /**
         * @Desc   :  Collecting all organizationDocuments from organisation DTO and adding those
         *  it in a collection object by converting into DB entities
         *
         */
        if (organizationDTO.getOrganizationDocuments() != null && !organizationDTO.getOrganizationDocuments().isEmpty()) {
            List<OrganizationDocument> organizationDocuments = new ArrayList<>();
            organizationDTO.getOrganizationDocuments().forEach(organizationDocumentDTO -> {
                // Map organizationDocumentDTO to OrganizationDocument entity
                OrganizationDocument organizationDocument = modelMapper.map(organizationDocumentDTO, OrganizationDocument.class);
                organizationDocument.setCreatedBy(userID);
                organizationDocument.setCreatedDt(LocalDate.now());
                organizationDocument.setOrganization(organization);
                organizationDocuments.add(organizationDocument);
            });
            organization.setOrganizationDocuments(organizationDocuments.isEmpty() ? null : organizationDocuments);
        }

        // Saving the organization object
        organizationRepo.save(organization);

        // Map the saved organization back to an OrganizationDTO and return
        return modelMapper.map(organization, OrganizationDTO.class);
    }

    /**
     * @param organizationID
     * @return retrieveOrganization based on Id
     * @throws ApplicationException
     */
    @Override
    public OrganizationDTO retrieveOrganization(String organizationID) {
        if (organizationID == null || organizationID.isEmpty()) {
            throw new InvalidDataException("ERROR_INVALID_DATA", "Organization", "organizationID", organizationID, "Invalid organization ID");
        }

        Optional<Organization> optionalOrganization = organizationRepo.findById(organizationID);
        if (!optionalOrganization.isPresent()) {
            // LOGGER.info("No organization found in the search");
            throw new InvalidDataException("ORG_NOT_FOUND", "Organization", "ID", organizationID, "Organization not found with: " + organizationID);

        }

        Organization organization = optionalOrganization.get();
        return modelMapper.map(organization, OrganizationDTO.class);
    }


    /**
     * @param organizations
     * @param organizationList
     * @return list of OrgSummaryDTO
     */
    private List<OrgSummaryDTO> getSummary(List<Organization> organizations, List<OrgSummaryDTO> organizationList) {
        organizations.forEach(organization -> {
            OrgSummaryDTO orgSummaryDTO = new OrgSummaryDTO();
            orgSummaryDTO.setOrganizationID(organization.getOrganizationID());
            orgSummaryDTO.setName(organization.getName());
            orgSummaryDTO.setCreatedOn(organization.getCreatedDt());
            //set ceo name and phone
            if (organization.getOrgCommunications() != null && !organization.getOrgCommunications().isEmpty()) {
                OrgCommunication communication = organization.getOrgCommunications().get(0);
                StringBuilder ceoNameBuilder = new StringBuilder();
                if (communication.getAuthSignataryFn() != null) {
                    ceoNameBuilder.append(communication.getAuthSignataryFn());
                }
                if (communication.getAuthSignataryLn() != null) {
                    ceoNameBuilder.append(" " + communication.getAuthSignataryLn());
                }
                orgSummaryDTO.setCeoName(ceoNameBuilder.toString());
                orgSummaryDTO.setCeoPhone(communication.getAuthSignataryPhone());
            }
            organizationList.add(orgSummaryDTO);
        });
        return organizationList;
    }


//    /**
//     * @param organizationID
//     * @param status
//     * @return
//     */
//    @Override
//    public String updateStatus(String organizationID, String status) {
//        if (organizationDocumentRepo.existsById(organizationID)) {
//            int updatedOrganization = organizationRepo.updateOrgStatus(organizationID, status);
//            if (updatedOrganization != 0) {
//                return "Organization status updated to '" + status + "' successfully";
//            }
//        }
//        throw new OrgNotFoundException(" Organization with id " + organizationID + "not found", ERROR_ORG_0001);
//
//    }

    /**
     * @param url
     * @return
     * @throws MalformedURLException
     */

    private boolean isValidURL(String url) throws MalformedURLException {
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
    }

    /**
     * @param organizationId
     * @param taxId
     * @return
     */

    @Override
    public Organization updateTaxId(String organizationId, String taxId) {
        Organization organization = organizationRepo.findById(organizationId).orElseThrow(() -> new RuntimeException("Organization not found"));
        organization.setTaxId(taxId);
        return organizationRepo.save(organization);
    }

    /**
     * @param webAddress
     * @return
     */
    public String getDomainName(String webAddress) {
        URI uri = null;
        try {
            uri = new URI(webAddress);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String host = uri.getHost();
        String domainName = host.startsWith("www.") ? host.substring(4) : host;
        return domainName;
    }

    /**
     * Retrieves a page of organizations based on the provided criteria and filters.
     *
     * @param offset             the offset of the page
     * @param limit              the maximum number of organizations to retrieve
     * @param organizationID     the organization ID to filter by (optional)
     * @param orgType            the organization type to filter by (optional)
     * @param statusCode         the status code to filter by (optional)
     * @param authSignataryEmail the email of the authorized signatory to filter by (optional)
     * @param name               the name of the organization to filter by (optional)
     * @param authSignataryPhone the phone number of the authorized signatory to filter by (optional)
     * @param statusCode         additional filter criteria as a string (optional)
     * @return a page of organization summaries matching the provided criteria
     */
    @Override
    public Page<OrgSummaryDTO> retrieveAllOrganizations(int offset, int limit, String organizationID, String orgType, OrgStatus statusCode, String authSignataryEmail, String name, String authSignataryPhone) {
        Pageable pageable = PageRequest.of(offset, limit);
        Specification<Organization> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(organizationID)) {
                predicates.add(criteriaBuilder.equal(root.get("organizationID"), organizationID));
            }
            if (!StringUtils.isEmpty(orgType)) {
                predicates.add(criteriaBuilder.equal(root.get("orgType"), orgType));
            }
            if (!StringUtils.isEmpty(authSignataryEmail)) {
                predicates.add(criteriaBuilder.equal(root.get("authSignataryEmail"), authSignataryEmail));
            }
            if (!StringUtils.isEmpty(authSignataryPhone)) {
                predicates.add(criteriaBuilder.equal(root.get("authSignataryPhone"), authSignataryPhone));
            }
            if (!StringUtils.isEmpty(name)) {
                predicates.add(criteriaBuilder.equal(root.get("name"), name));
            }
        /*if (!StringUtils.isEmpty(statusCode)) {
            // Example: filter by status code
            predicates.add(criteriaBuilder.equal(root.get("statusCode"), statusCode));
        }*/
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Organization> organizations = organizationRepo.findAll(specification, pageable);
        List<OrgSummaryDTO> summaryDTOS = organizations.getContent().stream()
                .map(this::getSummary)
                .collect(Collectors.toList());

        return new PageImpl<>(summaryDTOS, pageable, organizations.getTotalElements());
    }


    /**
     * @param organization
     * @return
     */
    private OrgSummaryDTO getSummary(Organization organization) {
        OrgSummaryDTO summaryDTO = new OrgSummaryDTO();
        summaryDTO.setOrganizationID(organization.getOrganizationID());
        summaryDTO.setName(organization.getName());
        summaryDTO.setCreatedOn(organization.getCreatedDt());
        System.out.println("lopp " + organization.getOrganizationID());
        if (organization.getOrgCommunications() != null && !organization.getOrgCommunications().isEmpty()) {
            OrgCommunication communication = organization.getOrgCommunications().get(0);
            String ceoName = communication.getAuthSignataryFn();
            if (communication.getAuthSignataryLn() != null) {
                ceoName += " " + communication.getAuthSignataryLn();
            }
            summaryDTO.setCeoName(ceoName);
            summaryDTO.setCeoPhone(communication.getAuthSignataryPhone());
        }

        return summaryDTO;
    }


    /**
     * Edits an organization based on the provided organization ID and updates its details with the
     * given OrganizationDTO.
     *
     * @param userID          The ID of the user performing the edit.
     * @param organizationID  The ID of the organization to be edited.
     * @param organizationDTO The DTO containing the updated organization details.
     * @return The edited OrganizationDTO.
     * @throws Exception if an error occurs during the organization editing process.
     */


    @Override
    public OrganizationDTO editOrganization(String userID, String organizationID, OrganizationDTO organizationDTO) throws Exception {
        // Method to edit an organization's details based on the provided organization ID

        OrganizationDTO result = null; // Initialize the result object as null

        // Retrieve existing organization details
        Organization existingOrganization = organizationRepo.findById(organizationID)
                .orElseThrow(() -> new OrgNotFoundException("organization by id " + organizationID + " not found", ERROR_ORG_0001));

        // Create a new organization object from organizationDTO
        Organization updateOrganization = modelMapper.map(organizationDTO, Organization.class);

        // Retrieve existing organization communication, domain, and document lists
        List<OrgCommunication> existingOrgCommList = existingOrganization.getOrgCommunications();
        List<OrgDomain> existingOrgDomainList = existingOrganization.getOrgDomains();
        List<OrganizationDocument> existingOrgDocList = existingOrganization.getOrganizationDocuments();

        // collecting all the orgAddress objects along with existing orgAddrId if have or else will create a new address object
        List<OrgAddress> orgAddresses = new ArrayList<>();

        //need to write separate  api for the all  orgAddress ,orgDocuments , orgCommunication ,orgDomain update
        if (organizationDTO.getOrgAddresses() != null && !organizationDTO.getOrgAddresses().isEmpty()) {
            // Map and update each orgAddress from organizationDTO to the new orgAddress object
            organizationDTO.getOrgAddresses().forEach(orgAddressDTO -> {
                OrgAddress orgAddress = modelMapper.map(orgAddressDTO, OrgAddress.class);
                // Set start and end dates for orgAddress
                if (Objects.nonNull(orgAddressDTO.getStartDate())) {
                    orgAddress.setStartDate(LocalDate.now());
                }
                if (Objects.nonNull(orgAddressDTO.getEndDate())) {
                    orgAddress.setEndDate(LocalDate.now());
                }
                orgAddress.setOrganization(existingOrganization);
                orgAddresses.add(orgAddress);
            });
            if (!orgAddresses.isEmpty()) {
                existingOrganization.setOrgAddresses(orgAddresses);
            } else {
                existingOrganization.setOrgAddresses(null);
            }
        }

        // collecting all the orgCommunication objects along with existing
        // orgDomainId if have or else will create a new Communication object
        if (organizationDTO.getOrgCommunications() != null && !organizationDTO.getOrgCommunications().isEmpty()) {
            List<OrgCommunication> orgCommunicationList = new ArrayList<>();
            // Map and update each orgCommunication from organizationDTO to the new orgCommunication object
            organizationDTO.getOrgCommunications().forEach(orgCommunicationDTO -> {
                OrgCommunication orgCommunication = modelMapper.map(orgCommunicationDTO, OrgCommunication.class);
                // Set start and end dates for orgCommunication
                if (Objects.nonNull(orgCommunicationDTO.getStartDate())) {
                    orgCommunication.setStartDate(LocalDate.now());
                }
                if (Objects.nonNull(orgCommunicationDTO.getEndDate())) {
                    orgCommunication.setEndDate(LocalDate.now());
                }
                orgCommunication.setOrganization(existingOrganization);
                orgCommunication.setUpdatedBy(userID);
                orgCommunication.setUpdatedDt(LocalDate.now());
                // Check if the existing orgCommunication list has entries
                if (existingOrgCommList.size() > 0) {
                    existingOrgCommList.forEach(existingOrgComm -> {
                        // Update created date and created by information if the orgCommunication already exists
                        if (StringUtils.isNotBlank(orgCommunication.getOrgCmncID()) && existingOrgComm.getOrgCmncID().equals(orgCommunication.getOrgCmncID())) {
                            orgCommunication.setCreatedDt(existingOrgComm.getCreatedDt());
                            orgCommunication.setCreatedBy(existingOrgComm.getCreatedBy());
                        } else {
                            // Set created date and created by information if the orgCommunication is newly created
                            orgCommunication.setCreatedBy(userID);
                            orgCommunication.setCreatedDt(LocalDate.now());
                        }
                    });
                } else {
                    // Set created date and created by information if the
                    // orgCommunication is newly created and there are no existing entries
                    orgCommunication.setCreatedBy(userID);
                    orgCommunication.setCreatedDt(LocalDate.now());
                }
                orgCommunicationList.add(orgCommunication);
            });
            if (!orgCommunicationList.isEmpty()) {
                existingOrganization.setOrgCommunications(orgCommunicationList);
            } else {
                existingOrganization.setOrgCommunications(null);
            }
        }

        // collecting all the orgDomain objects along with existing orgDomainId
        // if have or else will create a new orgDomain object
        if (organizationDTO.getOrgDomains() != null && !organizationDTO.getOrgDomains().isEmpty()) {
            List<OrgDomain> orgDomainsList = new ArrayList<>();
            // Map and update each orgDomain from organizationDTO to the new orgDomain object
            organizationDTO.getOrgDomains().forEach(orgDomainDTO -> {
                OrgDomain orgDomain = modelMapper.map(orgDomainDTO, OrgDomain.class);
                orgDomain.setDomainStatus("Active");
                orgDomain.setOrganization(existingOrganization);
                orgDomain.setUpdatedBy(userID);
                orgDomain.setUpdatedDt(LocalDate.now());
                // Check if the existing orgDomain list has entries
                if (existingOrgDomainList.size() > 0) {
                    existingOrgDomainList.forEach(existingOrgDomain -> {
                        // Update created date and created by information if the orgDomain already exists
                        if (StringUtils.isNotBlank(orgDomain.getDomain()) && existingOrgDomain.getDomain().equals(orgDomain.getDomain())) {
                            orgDomain.setCreatedDt(existingOrgDomain.getCreatedDt());
                            orgDomain.setCreatedBy(existingOrgDomain.getCreatedBy());
                        } else {
                            // Set created date and created by information if the orgDomain is newly created
                            orgDomain.setCreatedDt(LocalDate.now());
                            orgDomain.setCreatedBy(userID);
                        }
                    });
                } else {
                    // Set created date and created by information if the orgDomain is
                    // newly created and there are no existing entries
                    orgDomain.setCreatedDt(LocalDate.now());
                    orgDomain.setCreatedBy(userID);
                }
                orgDomainsList.add(orgDomain);
            });
            if (!orgDomainsList.isEmpty()) {
                existingOrganization.setOrgDomains(orgDomainsList);
            } else {
                existingOrganization.setOrgDomains(null);
            }
        }

        // collecting all the orgDocument objects along with existing orgDocumentId if
        // have or else will create a new orgDocument object
        List<OrganizationDocument> organizationDocumentList = new ArrayList<>();
        if (organizationDTO.getOrganizationDocuments() != null && !organizationDTO.getOrganizationDocuments().isEmpty()) {
            // Map and update each orgDocument from organizationDTO to the new orgDocument object
            organizationDTO.getOrganizationDocuments().forEach(orgDocumentsDTO -> {
                OrganizationDocument orgDocuments = modelMapper.map(orgDocumentsDTO, OrganizationDocument.class);
                orgDocuments.setUpdatedBy(userID);
                orgDocuments.setUpdatedDt(LocalDate.now());
                orgDocuments.setOrganization(existingOrganization);
                // Check if the existing orgDocument list has entries
                if (existingOrgDocList.size() > 0) {
                    existingOrgDocList.forEach(existingOrgDoc -> {
                        // Update created date and created by information if the orgDocument already exists
                        if (StringUtils.isNotBlank(orgDocuments.getOrganizationDocID()) && existingOrgDoc.getOrganizationDocID().equals(orgDocuments.getOrganizationDocID())) {
                            orgDocuments.setCreatedDt(existingOrgDoc.getCreatedDt());
                            orgDocuments.setCreatedBy(existingOrgDoc.getCreatedBy());
                        } else {
                            // Set created date and created by information if the orgDocument is newly created
                            orgDocuments.setCreatedBy(userID);
                            orgDocuments.setCreatedDt(LocalDate.now());
                        }
                    });
                } else {
                    // Set created date and created by information if the orgDocument is
                    // newly created and there are no existing entries
                    orgDocuments.setCreatedBy(userID);
                    orgDocuments.setCreatedDt(LocalDate.now());
                }
                organizationDocumentList.add(orgDocuments);
            });
            existingOrganization.setOrganizationDocuments(null);
        }

        // Update other existing organization details
        existingOrganization.setUpdatedDt(LocalDate.now());
        existingOrganization.setUpdatedBy(userID);
        existingOrganization.setName(updateOrganization.getName());
        existingOrganization.setCreatedDt(existingOrganization.getCreatedDt());
        existingOrganization.setCreatedBy(existingOrganization.getCreatedBy());
        existingOrganization.setFax(updateOrganization.getFax());
        existingOrganization.setPhoneNumber(updateOrganization.getPhoneNumber());
        existingOrganization.setDescription(updateOrganization.getDescription());
        existingOrganization.setCode(updateOrganization.getCode());
        existingOrganization.setStateofIncorporation(updateOrganization.getStateofIncorporation());
        existingOrganization.setTaxClassification(updateOrganization.getTaxClassification());
        if (null != updateOrganization.getStatusCode()) {
            existingOrganization.setStatusCode(updateOrganization.getStatusCode());
        } else {
            existingOrganization.setStatusCode(existingOrganization.getStatusCode());
        }

        // Call the updateOrganization method
        //orgValidation.updateOrganization(existingOrganization);

        // Save the updated organization details
        Organization organisationRes = organizationRepo.save(existingOrganization);

        // Update orgDocuments and retrieve the updated organization details

        organizationDocumentList.forEach(orgDoc -> {
            OrganizationDocument orgDocs = organizationDocumentRepo.save(orgDoc);
            organizationDocumentRepo.updateOrgDocs(organisationRes.getOrganizationID(), orgDocs.getOrganizationDocID());
        });

        // Map the updated organization details to OrganizationDTO and set it as the result
        result = modelMapper.map(organisationRes, OrganizationDTO.class);

        // Return the result
        return result;
    }


    /**
     * @param organizationID
     * @param statusCode
     * @return
     */

    public String setOrganizationStatus(String organizationID, String statusCode) {
        Organization organization = organizationRepo.findOrganizationById(organizationID);
        if (organization == null) {
            throw new InvalidDataException("ORG_NOT_FOUND", "Organization", "organizationID", organizationID, "Organization not found");
        }

        OrgStatus orgStatus;
        if (statusCode.equalsIgnoreCase("INACTIVE")) {
            orgStatus = OrgStatus.INACTIVE;
        } else if (statusCode.equalsIgnoreCase("ACTIVE")) {
            orgStatus = OrgStatus.ACTIVE;
        } else {
            throw new InvalidDataException("INVALID_STATUS", "Organization", "statusCode", statusCode, "Invalid status code");
        }

        organization.setStatusCode(orgStatus);
        organizationRepo.save(organization);
        return statusCode + " status updated successfully";
    }


   /* public OrganizationDTO updateOrganizationDetails(String organizationID, OrganizationDTO organizationDTO) throws Exception {
        com.lucid.recruit.org.entity.Organization existingOrganization = organizationRepo.findById(organizationID)
                .orElseThrow(() -> new OrgNotFoundException("Organization with ID " + organizationID + " not found", ERROR_ORG_0001));

        com.lucid.recruit.org.entity.Organization updatedOrganization = mapOrganizationDTOToEntity(organizationDTO);
        updateOrganizationDetails(existingOrganization, updatedOrganization);

        com.lucid.recruit.org.entity.Organization savedOrganization = organizationRepo.save(existingOrganization);
        return mapEntityToOrganizationDTO(savedOrganization);
    }

    private OrganizationDTO mapEntityToOrganizationDTO(Organization savedOrganization) {
        return null;
    }

    private Organization mapOrganizationDTOToEntity(OrganizationDTO organizationDTO) {
        return null;
    }

    private void updateOrganizationDetails(Organization existingOrganization, Organization updatedOrganization) {
        existingOrganization.setName(updatedOrganization.getName());
        existingOrganization.setFax(updatedOrganization.getFax());
        existingOrganization.setPhoneNumber(updatedOrganization.getPhoneNumber());
        existingOrganization.setDescription(updatedOrganization.getDescription());
        existingOrganization.setCode(updatedOrganization.getCode());
        existingOrganization.setStateofIncorporation(updatedOrganization.getStateofIncorporation());
        existingOrganization.setTaxClassification(updatedOrganization.getTaxClassification());
        if (updatedOrganization.getStatusCode() != null) {
            existingOrganization.setStatusCode(updatedOrganization.getStatusCode());
        }
    }*/


    private int updateOrganizationAddress(Organization updatableOrganization, OrganizationDTO newOrganization) {
        int returnStatus = 0;
        List<OrgAddressDTO> orgAddressList = newOrganization.getOrgAddresses();
        if (orgAddressList != null && !orgAddressList.isEmpty()) {
            // Multiple addresses can be updated
            // Iterate through the list and set the updated values
            for (Iterator<OrgAddressDTO> iterator = orgAddressList.iterator(); iterator.hasNext();) {
                OrgAddressDTO newOrgAddress = iterator.next();
                Set<ConstraintViolation<OrgAddressDTO>> violations = validator.validate(newOrgAddress);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                // Check if the organization address ID is passed, indicating an update to an existing address
                OrgAddress updatableOrgAddress = null;
                if (newOrgAddress.getOrgAddressID() != null) {
                    for (OrgAddress orgAddress : updatableOrganization.getOrgAddresses()) {
                        // Find the corresponding address by matching the ID
                        if (newOrgAddress.getOrgAddressID().equals(orgAddress.getOrgAddressID())) {
                            returnStatus = 1;
                            updatableOrgAddress = orgAddress;
                            break;
                        }
                    }
                    if (updatableOrgAddress == null) {
                        // Address record with the given ID not found
                        log.error("Organization Address with ID " + newOrgAddress.getOrgAddressID() + " not found");
                        throw new EntityNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND_404,
                                "Organization Address with ID " + newOrgAddress.getOrgAddressID() + " not found");
                    } else {
                        // Ignore the address type, as it cannot be changed
                        updateOrgAddress(updatableOrgAddress, newOrgAddress);
                    }
                } else {
                    log.info("New organization address added");
                    OrgAddress address = convertToEntity(newOrgAddress);
                    address.setOrganization(updatableOrganization);
                    updatableOrganization.getOrgAddresses().add(address);
                }
            }
        }
        return returnStatus;
    }

    private OrgAddress convertToEntity(OrgAddressDTO newOrgAddress) {
        OrgAddress o = null;
        return o;
    }


    /**
     * @param updatableOA
     * @param newOrgAddress
     */
    private void updateOrgAddress(OrgAddress updatableOA, OrgAddressDTO newOrgAddress) {

        // Check if the address name is different and update if necessary
        if (newOrgAddress.getName() != null && !newOrgAddress.getName().equals(updatableOA.getAddressName())) {
            updatableOA.setAddressName(newOrgAddress.getName());
        }

        // Check if the address1 is different and update if necessary
        if (newOrgAddress.getAddress1() != null && !newOrgAddress.getAddress1().equals(updatableOA.getAddress1())) {
            updatableOA.setAddress1(newOrgAddress.getAddress1());
        }

        // Check if the address2 is different and update if necessary
        if (newOrgAddress.getAddress2() != null && !newOrgAddress.getAddress2().equals(updatableOA.getAddress2())) {
            updatableOA.setAddress2(newOrgAddress.getAddress2());
        }

        // Check if the address3 is different and update if necessary
        if (newOrgAddress.getAddress3() != null && !newOrgAddress.getAddress3().equals(updatableOA.getAddress3())) {
            updatableOA.setAddress3(newOrgAddress.getAddress3());
        }

        // Check if the address4 is different and update if necessary
        if (newOrgAddress.getAddress4() != null && !newOrgAddress.getAddress4().equals(updatableOA.getAddress4())) {
            updatableOA.setAddress4(newOrgAddress.getAddress4());
        }

        // Check if the address5 is different and update if necessary
        if (newOrgAddress.getAddress5() != null && !newOrgAddress.getAddress5().equals(updatableOA.getAddress5())) {
            updatableOA.setAddress5(newOrgAddress.getAddress5());
        }

        // Check if the city is different and update if necessary
        if (newOrgAddress.getCity() != null && !newOrgAddress.getCity().equals(updatableOA.getCity())) {
            updatableOA.setCity(newOrgAddress.getCity());
        }

        // Check if the state is different and update if necessary
        if (newOrgAddress.getState() != null && !newOrgAddress.getState().equals(updatableOA.getState())) {
            updatableOA.setState(newOrgAddress.getState());
        }

        // Check if the postal code is different and update if necessary
        if (newOrgAddress.getPostalCode() != null && !newOrgAddress.getPostalCode().equals(updatableOA.getPostalCode())) {
            updatableOA.setPostalCode(newOrgAddress.getPostalCode());
        }

        // Check if the post office box is different and update if necessary
        if (newOrgAddress.getPostOfficeBox() != null && !newOrgAddress.getPostOfficeBox().equals(updatableOA.getPostOfficeBox())) {
            updatableOA.setPostOfficeBox(newOrgAddress.getPostOfficeBox());
        }

        // Check if the country code is different and update if necessary
        if (newOrgAddress.getCountryCode() != null && !newOrgAddress.getCountryCode().equals(updatableOA.getCountryCode())) {
            updatableOA.setCountryCode(newOrgAddress.getCountryCode());
        }

        /*// Check if the geo code is different and update if necessary
        if (newOrgAddress.getGeoCode() != null && !newOrgAddress.getGeoCode().equals(updatableOA.getGeoCode())) {
            updatableOA.setGeoCode(newOrgAddress.getGeoCode());
        }*/

        // Check if the start date is different and update if necessary
        if (newOrgAddress.getStartDate() != null && !newOrgAddress.getStartDate().equals(updatableOA.getStartDate())) {
            updatableOA.setStartDate(newOrgAddress.getStartDate());
        }

        // Check if the end date is different and update if necessary
        if (newOrgAddress.getEndDate() != null && !newOrgAddress.getEndDate().equals(updatableOA.getEndDate())) {
            updatableOA.setEndDate(newOrgAddress.getEndDate());
        }
    }
        // ---------------------------------------------------------- Public Methods
        // ------------------------------------------------------- Protected Methods
        // --------------------------------------------------------- Default Methods
        // --------------------------------------------------------- Private Methods
        // ---------------------------------------------------------- Static Methods
        // ----------------------------------------------------------- Inner Classes


    }

