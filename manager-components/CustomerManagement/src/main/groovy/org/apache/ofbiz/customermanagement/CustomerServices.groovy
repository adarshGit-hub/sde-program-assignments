package org.apache.ofbiz.customermanagement

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.condition.EntityFieldValue
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.util.EntityQuery
import org.apache.ofbiz.service.ServiceUtil
import java.sql.Timestamp

class CustomerServices {

    public static Map findCustomer(Map context) {
        var delegator = context.delegator
        List conds = []
        conds.add(EntityCondition.makeCondition("partyTypeId", "PERSON"))

        if (context.emailAddress) {
            conds.add(EntityCondition.makeCondition(
                EntityFunction.upper(EntityFieldValue.makeFieldValue("emailAddress")),
                EntityOperator.LIKE,
                EntityFunction.upper("%" + context.emailAddress + "%")
            ))
        }
        if (context.firstName) {
            conds.add(EntityCondition.makeCondition(
                EntityFunction.upper(EntityFieldValue.makeFieldValue("firstName")),
                EntityOperator.LIKE,
                EntityFunction.upper("%" + context.firstName + "%")
            ))
        }
        if (context.lastName) {
            conds.add(EntityCondition.makeCondition(
                EntityFunction.upper(EntityFieldValue.makeFieldValue("lastName")),
                EntityOperator.LIKE,
                EntityFunction.upper("%" + context.lastName + "%")
            ))
        }
        if (context.contactNumber) {
            conds.add(EntityCondition.makeCondition(
                EntityFunction.upper(EntityFieldValue.makeFieldValue("contactNumber")),
                EntityOperator.LIKE,
                EntityFunction.upper("%" + context.contactNumber + "%")
            ))
        }
        if (context.postalAddress) {
            conds.add(EntityCondition.makeCondition(
                EntityFunction.upper(EntityFieldValue.makeFieldValue("address1")),
                EntityOperator.LIKE,
                EntityFunction.upper("%" + context.postalAddress + "%")
            ))
        }

        EntityCondition mainCond = conds ? EntityCondition.makeCondition(conds, EntityOperator.AND) : null

        int viewIndex = context.viewIndex ?: 0
        int viewSize = context.viewSize ?: 10

        List listIt = EntityQuery.use(delegator)
            .from("FindCustomerView")
            .where(mainCond)
            .queryList()

        Map uniqueCustomers = [:]
        for (GenericValue item in listIt) {
            String partyId = item.partyId
            if (!uniqueCustomers.containsKey(partyId)) {
                uniqueCustomers[partyId] = [
                    partyId: item.partyId,
                    firstName: item.firstName,
                    lastName: item.lastName,
                    emailAddress: item.emailAddress,
                    contactNumber: item.contactNumber,
                    toName: item.toName,
                    address1: item.address1,
                    city: item.city,
                    postalCode: item.postalCode,
                    stateProvinceGeoId: item.stateProvinceGeoId
                ]
            } else {
                Map existing = uniqueCustomers[partyId]
                if (!existing.emailAddress && item.emailAddress) existing.emailAddress = item.emailAddress
                if (!existing.contactNumber && item.contactNumber) existing.contactNumber = item.contactNumber
                if (!existing.address1 && item.address1) {
                    existing.toName = item.toName
                    existing.address1 = item.address1
                    existing.city = item.city
                    existing.postalCode = item.postalCode
                    existing.stateProvinceGeoId = item.stateProvinceGeoId
                }
            }
        }

        List customerList = new ArrayList(uniqueCustomers.values())
        int listSize = customerList.size()

        int start = viewIndex * viewSize
        int end = Math.min(start + viewSize, listSize)
        List paginatedList = []
        if (start < listSize) {
            paginatedList = customerList.subList(start, end)
        }

        Map result = ServiceUtil.returnSuccess()
        result.customerList = paginatedList
        result.listSize = listSize
        return result
    }

    public static Map createCustomer(Map context) {
        var delegator = context.delegator
        var dispatcher = context.dispatcher

        // Verify if user already exists
        Map findRes = dispatcher.runSync("findCustomer", [emailAddress: context.emailAddress, userLogin: context.userLogin])
        if (findRes.customerList) {
            return ServiceUtil.returnError("Customer with email address [${context.emailAddress}] already exists.")
        }

        String partyId = delegator.getNextSeqId("Party")

        // Create Party
        GenericValue party = delegator.makeValue("Party", [
            partyId: partyId,
            partyTypeId: "PERSON",
            statusId: "PARTY_ENABLED"
        ])
        party.create()

        // Create Person
        GenericValue person = delegator.makeValue("Person", [
            partyId: partyId,
            firstName: context.firstName,
            lastName: context.lastName
        ])
        person.create()

        // Create Primary Email ContactMech
        String emailCmId = delegator.getNextSeqId("ContactMech")
        GenericValue emailCm = delegator.makeValue("ContactMech", [
            contactMechId: emailCmId,
            contactMechTypeId: "EMAIL_ADDRESS",
            infoString: context.emailAddress
        ])
        emailCm.create()

        GenericValue emailPcm = delegator.makeValue("PartyContactMech", [
            partyId: partyId,
            contactMechId: emailCmId,
            fromDate: UtilDateTime.nowTimestamp()
        ])
        emailPcm.create()

        GenericValue emailPcmp = delegator.makeValue("PartyContactMechPurpose", [
            partyId: partyId,
            contactMechId: emailCmId,
            contactMechPurposeTypeId: "EmailPrimary",
            fromDate: UtilDateTime.nowTimestamp()
        ])
        emailPcmp.create()

        // Create Phone ContactMech
        if (context.contactNumber) {
            String phoneCmId = delegator.getNextSeqId("ContactMech")
            GenericValue phoneCm = delegator.makeValue("ContactMech", [
                contactMechId: phoneCmId,
                contactMechTypeId: "TELECOM_NUMBER"
            ])
            phoneCm.create()

            GenericValue telecom = delegator.makeValue("TelecomNumber", [
                contactMechId: phoneCmId,
                contactNumber: context.contactNumber
            ])
            telecom.create()

            GenericValue phonePcm = delegator.makeValue("PartyContactMech", [
                partyId: partyId,
                contactMechId: phoneCmId,
                fromDate: UtilDateTime.nowTimestamp()
            ])
            phonePcm.create()

            GenericValue phonePcmp = delegator.makeValue("PartyContactMechPurpose", [
                partyId: partyId,
                contactMechId: phoneCmId,
                contactMechPurposeTypeId: "PRIMARY_PHONE",
                fromDate: UtilDateTime.nowTimestamp()
            ])
            phonePcmp.create()
        }

        // Create Postal Address ContactMech
        if (context.postalAddress) {
            String postalCmId = delegator.getNextSeqId("ContactMech")
            GenericValue postalCm = delegator.makeValue("ContactMech", [
                contactMechId: postalCmId,
                contactMechTypeId: "POSTAL_ADDRESS"
            ])
            postalCm.create()

            GenericValue postal = delegator.makeValue("PostalAddress", [
                contactMechId: postalCmId,
                toName: context.firstName + " " + context.lastName,
                address1: context.postalAddress,
                city: context.city ?: "",
                postalCode: context.postalCode ?: "",
                stateProvinceGeoId: context.stateProvinceGeoId ?: ""
            ])
            postal.create()

            GenericValue postalPcm = delegator.makeValue("PartyContactMech", [
                partyId: partyId,
                contactMechId: postalCmId,
                fromDate: UtilDateTime.nowTimestamp()
            ])
            postalPcm.create()

            GenericValue postalPcmp = delegator.makeValue("PartyContactMechPurpose", [
                partyId: partyId,
                contactMechId: postalCmId,
                contactMechPurposeTypeId: "PRIMARY_LOCATION",
                fromDate: UtilDateTime.nowTimestamp()
            ])
            postalPcmp.create()
        }

        Map result = ServiceUtil.returnSuccess("Customer created successfully with Party ID: " + partyId)
        result.partyId = partyId
        return result
    }

    public static Map updateCustomer(Map context) {
        var delegator = context.delegator
        var dispatcher = context.dispatcher

        // Verify if customer exists
        GenericValue findView = EntityQuery.use(delegator)
            .from("FindCustomerView")
            .where("emailAddress", context.emailAddress)
            .queryFirst()
        if (!findView) {
            return ServiceUtil.returnError("Customer with email address [${context.emailAddress}] does not exist.")
        }
        String partyId = findView.partyId

        // Update Phone
        if (context.contactNumber != null) {
            GenericValue phonePurp = EntityQuery.use(delegator)
                .from("PartyContactMechPurpose")
                .where("partyId", partyId, "contactMechPurposeTypeId", "PRIMARY_PHONE")
                .filterByDate()
                .queryFirst()
            if (phonePurp) {
                GenericValue telecom = EntityQuery.use(delegator)
                    .from("TelecomNumber")
                    .where("contactMechId", phonePurp.contactMechId)
                    .queryOne()
                if (telecom) {
                    telecom.contactNumber = context.contactNumber
                    telecom.store()
                }
            } else if (context.contactNumber) {
                String phoneCmId = delegator.getNextSeqId("ContactMech")
                GenericValue phoneCm = delegator.makeValue("ContactMech", [
                    contactMechId: phoneCmId,
                    contactMechTypeId: "TELECOM_NUMBER"
                ])
                phoneCm.create()

                GenericValue telecom = delegator.makeValue("TelecomNumber", [
                    contactMechId: phoneCmId,
                    contactNumber: context.contactNumber
                ])
                telecom.create()

                GenericValue phonePcm = delegator.makeValue("PartyContactMech", [
                    partyId: partyId,
                    contactMechId: phoneCmId,
                    fromDate: UtilDateTime.nowTimestamp()
                ])
                phonePcm.create()

                GenericValue phonePcmp = delegator.makeValue("PartyContactMechPurpose", [
                    partyId: partyId,
                    contactMechId: phoneCmId,
                    contactMechPurposeTypeId: "PRIMARY_PHONE",
                    fromDate: UtilDateTime.nowTimestamp()
                ])
                phonePcmp.create()
            }
        }

        // Update Postal Address
        if (context.postalAddress != null || context.city != null || context.postalCode != null || context.stateProvinceGeoId != null) {
            GenericValue postalPurp = EntityQuery.use(delegator)
                .from("PartyContactMechPurpose")
                .where("partyId", partyId, "contactMechPurposeTypeId", "PRIMARY_LOCATION")
                .filterByDate()
                .queryFirst()
            if (postalPurp) {
                GenericValue postal = EntityQuery.use(delegator)
                    .from("PostalAddress")
                    .where("contactMechId", postalPurp.contactMechId)
                    .queryOne()
                if (postal) {
                    if (context.postalAddress != null) postal.address1 = context.postalAddress
                    if (context.city != null) postal.city = context.city
                    if (context.postalCode != null) postal.postalCode = context.postalCode
                    if (context.stateProvinceGeoId != null) postal.stateProvinceGeoId = context.stateProvinceGeoId
                    postal.store()
                }
            } else {
                String postalCmId = delegator.getNextSeqId("ContactMech")
                GenericValue postalCm = delegator.makeValue("ContactMech", [
                    contactMechId: postalCmId,
                    contactMechTypeId: "POSTAL_ADDRESS"
                ])
                postalCm.create()

                GenericValue person = EntityQuery.use(delegator).from("Person").where("partyId", partyId).queryOne()
                String toName = person ? (person.firstName + " " + person.lastName) : ""

                GenericValue postal = delegator.makeValue("PostalAddress", [
                    contactMechId: postalCmId,
                    toName: toName,
                    address1: context.postalAddress ?: "",
                    city: context.city ?: "",
                    postalCode: context.postalCode ?: "",
                    stateProvinceGeoId: context.stateProvinceGeoId ?: ""
                ])
                postal.create()

                GenericValue postalPcm = delegator.makeValue("PartyContactMech", [
                    partyId: partyId,
                    contactMechId: postalCmId,
                    fromDate: UtilDateTime.nowTimestamp()
                ])
                postalPcm.create()

                GenericValue postalPcmp = delegator.makeValue("PartyContactMechPurpose", [
                    partyId: partyId,
                    contactMechId: postalCmId,
                    contactMechPurposeTypeId: "PRIMARY_LOCATION",
                    fromDate: UtilDateTime.nowTimestamp()
                ])
                postalPcmp.create()
            }
        }

        Map result = ServiceUtil.returnSuccess("Customer updated successfully.")
        result.partyId = partyId
        return result
    }

    public static Map createCustomerRelationship(Map context) {
        var delegator = context.delegator

        String roleTypeIdFrom = context.roleTypeIdFrom ?: "CUSTOMER"
        String roleTypeIdTo = context.roleTypeIdTo ?: "CONTACT"
        String partyRelationshipTypeId = context.partyRelationshipTypeId

        // Ensure relationship type exists
        GenericValue relType = EntityQuery.use(delegator).from("PartyRelationshipType").where("partyRelationshipTypeId", partyRelationshipTypeId).queryOne()
        if (!relType) {
            delegator.create("PartyRelationshipType", [
                partyRelationshipTypeId: partyRelationshipTypeId,
                description: "Relationship Type: " + partyRelationshipTypeId
            ])
        }

        // Ensure party roles exist
        GenericValue prFrom = EntityQuery.use(delegator).from("PartyRole").where("partyId", context.partyIdFrom, "roleTypeId", roleTypeIdFrom).queryOne()
        if (!prFrom) {
            delegator.create("PartyRole", [partyId: context.partyIdFrom, roleTypeId: roleTypeIdFrom])
        }
        GenericValue prTo = EntityQuery.use(delegator).from("PartyRole").where("partyId", context.partyIdTo, "roleTypeId", roleTypeIdTo).queryOne()
        if (!prTo) {
            delegator.create("PartyRole", [partyId: context.partyIdTo, roleTypeId: roleTypeIdTo])
        }

        Timestamp fromDate = context.fromDate ?: UtilDateTime.nowTimestamp()

        GenericValue rel = delegator.makeValue("PartyRelationship", [
            partyIdFrom: context.partyIdFrom,
            partyIdTo: context.partyIdTo,
            roleTypeIdFrom: roleTypeIdFrom,
            roleTypeIdTo: roleTypeIdTo,
            fromDate: fromDate,
            partyRelationshipTypeId: partyRelationshipTypeId,
            statusId: context.statusId ?: "PARTYREL_ACTIVE"
        ])
        rel.create()

        return ServiceUtil.returnSuccess("Party relationship created successfully.")
    }

    public static Map updateCustomerRelationship(Map context) {
        var delegator = context.delegator

        String roleTypeIdFrom = context.roleTypeIdFrom ?: "CUSTOMER"
        String roleTypeIdTo = context.roleTypeIdTo ?: "CONTACT"

        GenericValue rel = EntityQuery.use(delegator)
            .from("PartyRelationship")
            .where(
                "partyIdFrom", context.partyIdFrom,
                "partyIdTo", context.partyIdTo,
                "roleTypeIdFrom", roleTypeIdFrom,
                "roleTypeIdTo", roleTypeIdTo,
                "fromDate", context.fromDate,
                "partyRelationshipTypeId", context.partyRelationshipTypeId
            ).queryOne()

        if (!rel) {
            return ServiceUtil.returnError("Party relationship not found.")
        }

        if (context.thruDate != null) rel.thruDate = context.thruDate
        if (context.statusId != null) rel.statusId = context.statusId
        rel.store()

        return ServiceUtil.returnSuccess("Party relationship updated successfully.")
    }
}
