package org.apache.ofbiz.customermanagement.test

import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.service.ServiceUtil
import org.apache.ofbiz.service.testtools.OFBizTestCase
import org.apache.ofbiz.base.util.UtilDateTime

class CustomerManagementTests extends OFBizTestCase {

    CustomerManagementTests(String name) {
        super(name)
    }

    void testCustomerManagementFlow() {
        // Seed EmailPrimary purpose type if not already seeded
        GenericValue emailType = from("ContactMechPurposeType").where("contactMechPurposeTypeId", "EmailPrimary").queryOne()
        if (!emailType) {
            delegator.create("ContactMechPurposeType", [
                contactMechPurposeTypeId: "EmailPrimary",
                description: "Primary Email Address",
                hasTable: "N"
            ])
            delegator.create("ContactMechTypePurpose", [
                contactMechPurposeTypeId: "EmailPrimary",
                contactMechTypeId: "EMAIL_ADDRESS"
            ])
        }

        // 1. Create first customer
        Map createCtx = [
            emailAddress: "testcust1@example.com",
            firstName: "Alice",
            lastName: "Smith",
            contactNumber: "555-0101",
            postalAddress: "100 Pine St",
            city: "Salt Lake City",
            postalCode: "84101",
            stateProvinceGeoId: "UT",
            userLogin: userLogin
        ]
        Map createResult = dispatcher.runSync("createCustomer", createCtx)
        assert ServiceUtil.isSuccess(createResult)
        String partyId1 = createResult.partyId
        assert partyId1 != null

        // 2. Validate email uniqueness constraint on createCustomer
        Map createDuplicateCtx = [
            emailAddress: "testcust1@example.com", // Same email
            firstName: "Bob",
            lastName: "Smith",
            userLogin: userLogin
        ]
        Map createDuplicateResult = dispatcher.runSync("createCustomer", createDuplicateCtx)
        assert ServiceUtil.isError(createDuplicateResult)

        // 3. Update customer details
        Map updateCtx = [
            emailAddress: "testcust1@example.com",
            contactNumber: "555-9999",
            postalAddress: "200 Oak Ave",
            city: "Provo",
            postalCode: "84601",
            stateProvinceGeoId: "UT",
            userLogin: userLogin
        ]
        Map updateResult = dispatcher.runSync("updateCustomer", updateCtx)
        assert ServiceUtil.isSuccess(updateResult)

        // Verify updated contact number in database
        GenericValue phonePurp = from("PartyContactMechPurpose")
            .where("partyId", partyId1, "contactMechPurposeTypeId", "PRIMARY_PHONE")
            .filterByDate()
            .queryFirst()
        assert phonePurp != null
        GenericValue telecom = from("TelecomNumber").where("contactMechId", phonePurp.contactMechId).queryOne()
        assert telecom != null
        assert telecom.contactNumber == "555-9999"

        // 4. Test findCustomer
        Map findCtx = [
            emailAddress: "testcust1",
            firstName: "Alice",
            userLogin: userLogin
        ]
        Map findResult = dispatcher.runSync("findCustomer", findCtx)
        assert ServiceUtil.isSuccess(findResult)
        List customerList = findResult.customerList
        assert customerList != null
        assert customerList.size() > 0
        assert customerList.any { it.partyId == partyId1 }

        // 5. Create another customer to link
        Map createCtx2 = [
            emailAddress: "testcust2@example.com",
            firstName: "Bob",
            lastName: "Jones",
            userLogin: userLogin
        ]
        Map createResult2 = dispatcher.runSync("createCustomer", createCtx2)
        assert ServiceUtil.isSuccess(createResult2)
        String partyId2 = createResult2.partyId

        // 6. Establish party relationship
        Map assocCtx = [
            partyIdFrom: partyId1,
            partyIdTo: partyId2,
            roleTypeIdFrom: "CUSTOMER",
            roleTypeIdTo: "CONTACT",
            partyRelationshipTypeId: "CUSTOMER_REL",
            statusId: "PARTYREL_ACTIVE",
            userLogin: userLogin
        ]
        Map assocResult = dispatcher.runSync("createCustomerRelationship", assocCtx)
        assert ServiceUtil.isSuccess(assocResult)

        // Verify relationship in DB
        GenericValue relGv = from("PartyRelationship")
            .where("partyIdFrom", partyId1, "partyIdTo", partyId2, "roleTypeIdFrom", "CUSTOMER", "roleTypeIdTo", "CONTACT", "partyRelationshipTypeId", "CUSTOMER_REL")
            .filterByDate()
            .queryFirst()
        assert relGv != null
        assert relGv.statusId == "PARTYREL_ACTIVE"

        // 7. Update party relationship
        Map updateAssocCtx = [
            partyIdFrom: partyId1,
            partyIdTo: partyId2,
            roleTypeIdFrom: "CUSTOMER",
            roleTypeIdTo: "CONTACT",
            partyRelationshipTypeId: "CUSTOMER_REL",
            fromDate: relGv.fromDate,
            statusId: "PARTYREL_TERMINATED",
            userLogin: userLogin
        ]
        Map updateAssocResult = dispatcher.runSync("updateCustomerRelationship", updateAssocCtx)
        assert ServiceUtil.isSuccess(updateAssocResult)

        // Verify updated status in DB
        GenericValue relUpdatedGv = from("PartyRelationship")
            .where("partyIdFrom", partyId1, "partyIdTo", partyId2, "roleTypeIdFrom", "CUSTOMER", "roleTypeIdTo", "CONTACT", "partyRelationshipTypeId", "CUSTOMER_REL")
            .queryFirst()
        assert relUpdatedGv != null
        assert relUpdatedGv.statusId == "PARTYREL_TERMINATED"
    }
}
