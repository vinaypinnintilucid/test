package com.lucid.recruit.org.validation;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.lucid.core.dto.BaseAddressDTO;
import com.lucid.core.embeddable.GeoCode;
import com.lucid.core.exception.InvalidDataException;
import com.lucid.recruit.common.repo.PublicDomainRepository;
import com.lucid.recruit.docs.entity.BaseDocument;
import com.lucid.recruit.org.dto.*;
import com.lucid.recruit.org.entity.OrgAddress;
import com.lucid.recruit.org.entity.Organization;
import com.lucid.recruit.org.repo.OrgAddressRepo;
import com.lucid.recruit.org.repo.OrganizationRepo;
import com.lucid.util.Strings;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.Address;
import org.apache.commons.validator.routines.UrlValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class OrgValidation {

    @Autowired
    private OrganizationRepo organizationRepo;

    @Autowired
    private PublicDomainRepository publicDomainRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OrgAddressRepo orgAddressRepo;

    public void validateOrganization(OrganizationDTO organizationDTO) {

        // Name    Validation
        if (StringUtils.isNotBlank(organizationDTO.getName())) {
            String trimmedName = organizationDTO.getName().trim().replaceAll("\\s+", " ");
            if (trimmedName.length() > 100) {
                throw new InvalidDataException("", "INVALID_NAME", "name", trimmedName, "Name should be under 100 Characters Limit");
            }
            organizationDTO.setName(trimmedName);
        }
        // Check if the description field of organizationDTO is not blank
        //Remove Extra Spaces
        if (StringUtils.isNotBlank(organizationDTO.getDescription())) {
            String trimmedDescription = organizationDTO.getDescription().trim();
            String cleanedDescription = trimmedDescription.replaceAll("\\s+", " ");
            organizationDTO.setDescription(cleanedDescription);
        }

        //validate the PhoneNumber
        validatePhoneNumber(organizationDTO);


        // Extra If WeNeed  Any  validations...


        //validateTaxId
        if (StringUtils.isNotBlank(organizationDTO.getOrganizationID())) {
            validateTaxId(organizationDTO.getTaxId(), organizationDTO.getOrganizationID());
        } else {
            validateTaxId(organizationDTO.getTaxId(), null);
        }
        // validate Domain
        if (organizationDTO.getOrgDomains() != null && !organizationDTO.getOrgDomains().isEmpty()) {
            validateDomains(organizationDTO.getOrgDomains());
        }
        if (organizationDTO.getOrgCommunications() != null && !organizationDTO.getOrgCommunications().isEmpty()) {
            List<String> resourceEmails = organizationDTO.getOrgCommunications().stream()
                    .map(OrgCommunicationDTO::getAuthSignataryEmail)
                    .collect(Collectors.toList());
            List<String> domainExt = organizationDTO.getOrgDomains().stream()
                    .map(OrgDomainDTO::getDomainExt)
                    .collect(Collectors.toList());
            validateResourceEmails(resourceEmails, domainExt);
        }

        if (organizationDTO.getOrgCommunications() != null && !organizationDTO.getOrgCommunications().isEmpty()) ;


        /*if(organizationDTO.getOrganizationID() == null) {
            organizationDTO.getOrgAddresses().forEach(orgAddressDTO -> {
                if (getAddress(orgAddressDTO)) {
                    throw new InvalidDataException("BAD_REQUEST", "Address", "name", "", "Address already exists with the given name " + organizationDTO.getName());
                }
            });*/
    }




    public void validateTaxId(String taxId, String organizationId) {
        if (StringUtils.isBlank(taxId)) {
            throw new InvalidDataException("INVALID_DATA", "Organization", "taxId", " ", "Need To Fill Mandatory Field");
        }

        if (StringUtils.isNotBlank(organizationId)) {
            Organization existingOrganization = organizationRepo.findOrganizationById(organizationId);
            if (existingOrganization != null && !existingOrganization.getTaxId().equals(taxId)) {
                throw new InvalidDataException("INVALID_TAX_ID", "Organization", "taxId", taxId, "Editing the tax ID is not authorized");
            }
        } else {
            Organization organization = organizationRepo.findOrganizationByTaxId(taxId);
            if (organization != null) {
                throw new InvalidDataException("INVALID_DATA", "Organization", "taxId", taxId, "Tax ID is already connected with the organization named " + organization.getName() + " with status " + organization.getStatusCode());
            }
        }
    }


    public void validateName(String name) {
        // Trim leading and trailing spaces from the name
        name = name.trim();

        // Check if the name exceeds 100 characters
        if (name.length() > 100) {
            throw new InvalidDataException("INVALID_DATA", "Organization", "name", name,
                    "Name cannot exceed 100 characters.");
        }
    }

    public void validateDomains(List<OrgDomainDTO> domains) {
        List<String> invalidDomains = new ArrayList<>();
        UrlValidator validator = new UrlValidator();
        if (domains != null && !domains.isEmpty()) {
            domains.forEach(orgDomain -> {
                if (!Strings.isNullOrEmpty(orgDomain.getDomain())) {
                    if (validator.isValid(orgDomain.getDomain())) {
                        URL url = null;
                        try {
                            url = new URL(orgDomain.getDomain());
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                        String hostAddress = url.getHost();
                        String[] splitHost = hostAddress.split("\\.");
                        StringBuilder builder = new StringBuilder();
                        int splitHostLength = splitHost.length;
                        if (splitHostLength > 2 && splitHost[0].startsWith("www")) {
                            builder.append(splitHost[splitHostLength - 4]);
                            builder.append(".");
                            builder.append(splitHost[splitHostLength - 3]);
                            builder.append(".");
                            builder.append(splitHost[splitHostLength - 2]);
                            builder.append(".");
                            builder.append(splitHost[splitHostLength - 1]);
                        } else {
                            builder.append(hostAddress);
                        }
                        orgDomain.setDomainExt(builder.toString());
                    } else {
                        invalidDomains.add(orgDomain.getDomain());
                    }
                } else {
                    invalidDomains.add(orgDomain.getDomain());
                }
            });
            if (!invalidDomains.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (String invalidDomain : invalidDomains) {
                    if (i == invalidDomains.size() - 1) {
                        builder.append(invalidDomain);
                    } else {
                        builder.append(invalidDomain + ",");
                    }
                    i++;
                }
                throw new InvalidDataException("INVALID_DOMAIN", "OrgDomainDTO", "domain", builder.toString(), "Invalid WebAddresses");
            }
        }
    }

    public void validateResourceEmails(List<String> emails, List<String> domainExts) {
        if (emails != null && !emails.isEmpty() && (domainExts == null || domainExts.isEmpty())) {
            throw new InvalidDataException("NO_WEB_ADDRESSES", "Emails", null, null, "No WebAddresses Found");
        }
        emails.forEach(email -> {
            if (Strings.isNullOrEmpty(email) || email.split("@").length < 2) {
                throw new InvalidDataException("INVALID_EMAIL", "Emails", null, email, "Invalid email");
            } else if (!domainExts.contains(email.split("@")[1])) {
                throw new InvalidDataException("UNSUPPORTED_EMAIL", "Emails", null, email, "Domain is not wait listed");
            }
        });
    }


    private boolean isValidHost(String hostAddress) {
        boolean invalidHost = false;
        List<String> splitHost = Arrays.stream(hostAddress.split("\\.")).toList();
        if (splitHost.size() > 0) {
            for (int i = 0; i < splitHost.size(); i++) {
                StringBuilder tempBuilder = new StringBuilder();
                for (int count = i; count < splitHost.size(); count++) {
                    if (count != splitHost.size() - 1)
                        tempBuilder.append(splitHost.get(count) + ".");
                    else
                        tempBuilder.append(splitHost.get(count));
                }
                if (publicDomainRepository.existsById(tempBuilder.toString())) {
                    invalidHost = true;
                    i = splitHost.size();
                }
            }
        }
        return invalidHost;
    }


    // Validating phoneNumber
    public void validatePhoneNumber(OrganizationDTO organizationDTO) {
        String phoneNumber = organizationDTO.getPhoneNumber();

        if (StringUtils.isBlank(phoneNumber)) {
            throw new InvalidDataException("INVALID_PHONE_NUMBER", "Organization", "phoneNumber", null, "Phone number is mandatory and it must contain only numbers.");
        }

        // Remove any non-digit characters from the phone number
        phoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        if (StringUtils.isBlank(phoneNumber)) {
            throw new InvalidDataException("INVALID_PHONE_NUMBER", "Organization", "phoneNumber", null, "Phone number is mandatory and must contain only numbers.");
        }

        if (phoneNumber.length() > 15) {
            throw new InvalidDataException("INVALID_PHONE_NUMBER_LENGTH", "Organization", "phoneNumber", phoneNumber, "Phone number must be at most 15 characters long.");
        }

        organizationDTO.setPhoneNumber(phoneNumber);
    }


    public static boolean validateDocumentName(BaseDocument document) {
        String documentName = document.getDocumentName();

        // Check if the document name is empty or null
        if (documentName == null || documentName.isEmpty()) {
            return false;
        }

        // Perform additional validation logic based on the document name
        if (documentName.equalsIgnoreCase("Aadhar Card")) {
            // Aadhar card does not have an expiration date
            if (document.getExpirationDate() == null) {
                return false; // Invalid: Aadhar card should not have an expiration date
            }
        } else if (documentName.equalsIgnoreCase("License")) {
            // License should have both issue date and expiration date
            if (document.getIssuedDt() != null || document.getExpirationDate() != null) {
                return false; // Invalid: License should have both issue date and expiration date
            }
        }

        // Perform other validations as needed

        return true;
    }


//    public Boolean getAddress(OrgAddressDTO orgAddress) {
//
//        Boolean isAddressAvailable = false;
//        OrgAddress orgAddr = orgAddressRepo.getOrgAddress(orgAddress.getAddress1(), orgAddress.getAddress2(),
//                orgAddress.getAddress3(), orgAddress.getAddress4(), orgAddress.getAddress5(), orgAddress.getCity(),
//                orgAddress.getState());
//        if(Objects.nonNull(orgAddr)) {
//            if (orgAddr.getPostalCode().equals(orgAddress.getPostalCode()) && orgAddr.getPostOfficeBox().equals(orgAddress.getPostOfficeBox())) {
//                isAddressAvailable=true;
//            }
//        }else{
//            isAddressAvailable = false;
//        }
//
//       // OrgAddressDTO orgDTO = modelMapper.map(orgAddr, OrgAddressDTO.class);
//
//       return isAddressAvailable;
//    }



    public boolean isAddressUnique(BaseAddressDTO newAddress, List<BaseAddressDTO> existingAddresses) {
        for (BaseAddressDTO existingAddress : existingAddresses) {
            if (isSameAddress(newAddress, existingAddress)) {
                return false; // Address already exists, not unique
            }
        }
        return true; // Address is unique
    }

    private boolean isSameAddress(BaseAddressDTO address1, BaseAddressDTO address2) {
        return address1.getAddress1().equals(address2.getAddress1())
                && address1.getAddress2().equals(address2.getAddress2())
                && address1.getCity().equals(address2.getCity())
                && address1.getPostalCode().equals(address2.getPostalCode())
                && address1.getState().equals(address2.getState())
                && address1.getCountryCode().equals(address2.getCountryCode());
    }


//    private List<BaseAddressDTO> getAddressListForOrganization(OrganizationDTO organizationDTO) {
//        // Assuming you have a data access object (DAO) for accessing the database
//        OrgAddressDTO addressDAO = new OrgAddressDTO();
//
//        // Retrieve the addresses for the organization using the organizationDTO's ID
//        List<Address> addresses = addressDAO.getAddressesByOrganizationId(organizationDTO.getId());
//
//        // Convert the retrieved addresses to BaseAddressDTO objects
//        List<BaseAddressDTO> baseAddresses = new ArrayList<>();
//        for (Address address : addresses) {
//            BaseAddressDTO baseAddress = new BaseAddressDTO();
//            baseAddress.setId(address.getId());
//            baseAddress.setStreet(address.getStreet());
//            baseAddress.setCity(address.getCity());
//            // Set other properties as needed
//            baseAddresses.add(baseAddress);
//        }
//
//        return baseAddresses;
//    }


}
