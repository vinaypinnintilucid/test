/**
 * All Rights Reserved. Private and Confidential. May not be disclosed without permission.
 */
package com.lucid.recruit.org.repo;

import com.lucid.recruit.org.dto.OrgAddressDTO;
import com.lucid.recruit.org.entity.OrgRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lucid.recruit.org.entity.Organization;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * @author sgutti
 * @date 16-Mar-2023 6:00:40 am
 */
public interface OrganizationRepo extends JpaRepository<Organization, String> {

    Organization findOrganizationByTaxId(String organizationTaxId);


    @Modifying
    @Query("UPDATE Organization o SET o.statusCode =:status WHERE o.organizationID = :organizationID")
    int updateOrgStatus(@Param("organizationID") String organizationID, @Param("status") String status);

    @Modifying
    @Query("UPDATE Organization o SET o.taxId = :newTaxId WHERE o.organizationID = :organizationId")
    void updateTaxId(String organizationId, String newTaxId);

    Page<Organization> findAll(Specification<Organization> specification, Pageable pageable);

    @Query(value="select * from o_organization where org_id=:organizationId",nativeQuery = true)
    Organization findOrganizationById(@Param("organizationId") String organizationId);


   // Organization findOrganizationByAddress(String name, List<OrgAddressDTO> addresses);


//    Organization findOrganizationByNameAndAddress();
//
//    Organization findOrganizationByAddress(String name, List<OrgAddressDTO> address);

//    Organization findOrganizationByName(String organizationTaxId);
//
//    List<Organization> findOrganizationsByName(String name);
//
//    Organization findOrgByTaxIdAndOrganizationID(String organizationId, String taxId);


//    @Query("SELECT o FROM Organization o WHERE o.name <> :name AND o.address IN :address")
//    Organization findOrganizationByAddress(@Param("name") String name, @Param("address") List<OrgAddressDTO> address);
//    boolean isPhoneNumberExists(String phoneNumber);
//
//    boolean findByPhoneNumberOrganization(String phoneNumber);
//
//
//      Organization findOrganizationById(String organizationId, String orgAddresses);


    // --------------------------------------------------------------- Constants
    // --------------------------------------------------------- Class Variables
    // ----------------------------------------------------- Static Initializers
    // ------------------------------------------------------ Instance Variables
    // ------------------------------------------------------------ Constructors
    // ---------------------------------------------------------- Public Methods
    // ------------------------------------------------------- Protected Methods
    // --------------------------------------------------------- Default Methods
    // --------------------------------------------------------- Private Methods
    // ---------------------------------------------------------- Static Methods
    // ----------------------------------------------------------- Inner Classes
}
