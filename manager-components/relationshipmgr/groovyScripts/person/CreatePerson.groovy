import org.apache.ofbiz.service.ServiceUtil

def createPerson() {
    if (!parameters.partyId) {
        // Create Party first if not supplied
        Map createPartyCtx = dispatcher.getDispatchContext().makeValidContext("createParty", "IN", parameters)
        Map createPartyRes = dispatcher.runSync("createParty", createPartyCtx)
        if (ServiceUtil.isError(createPartyRes)) return createPartyRes
        parameters.partyId = createPartyRes.partyId
    }
    
    // Create Person record
    Map createPersonCtx = dispatcher.getDispatchContext().makeValidContext("createPersonEntityAuto", "IN", parameters)
    Map createPersonRes = dispatcher.runSync("createPersonEntityAuto", createPersonCtx)
    if (ServiceUtil.isError(createPersonRes)) return createPersonRes
    
    Map result = ServiceUtil.returnSuccess()
    result.partyId = parameters.partyId
    return result
}
