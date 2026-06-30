<style>
  .pm-container {
    font-family: 'Outfit', 'Inter', sans-serif;
    padding: 20px;
    background: #f8fafc;
    color: #1e293b;
  }
  .pm-header {
    margin-bottom: 24px;
    background: linear-gradient(135deg, #0284c7, #0369a1);
    padding: 24px;
    border-radius: 12px;
    color: white;
    box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
  }
  .pm-header h1 { margin: 0; font-size: 28px; font-weight: 700; }
  .pm-header p { margin: 8px 0 0 0; opacity: 0.9; font-size: 14px; }
  
  .pm-alert {
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    font-weight: 500;
  }
  .pm-alert-success { background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; }
  .pm-alert-error { background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; }

  .pm-grid {
    display: grid;
    grid-template-columns: 2.3fr 1.7fr;
    gap: 24px;
  }
  @media (max-width: 1200px) {
    .pm-grid { grid-template-columns: 1fr; }
  }

  .pm-card {
    background: white;
    border-radius: 12px;
    box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
    border: 1px solid #e2e8f0;
    margin-bottom: 24px;
    overflow: hidden;
  }
  .pm-card-header {
    padding: 16px 20px;
    background: #f1f5f9;
    border-bottom: 1px solid #e2e8f0;
    font-weight: 600;
    font-size: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .pm-card-body { padding: 20px; }

  .pm-form-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
  }
  .pm-form-group {
    display: flex;
    flex-direction: column;
    margin-bottom: 12px;
  }
  .pm-form-group label {
    margin-bottom: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #475569;
  }
  .pm-form-control {
    padding: 8px 12px;
    border-radius: 6px;
    border: 1px solid #cbd5e1;
    font-size: 14px;
    outline: none;
    transition: border-color 0.15s ease;
    background-color: white;
  }
  .pm-form-control:focus { border-color: #0284c7; }

  .pm-btn {
    padding: 8px 16px;
    border-radius: 6px;
    font-weight: 600;
    font-size: 14px;
    cursor: pointer;
    border: none;
    transition: all 0.15s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
  .pm-btn-primary { background: #0284c7; color: white; }
  .pm-btn-primary:hover { background: #0369a1; }
  .pm-btn-secondary { background: #64748b; color: white; }
  .pm-btn-secondary:hover { background: #475569; }
  .pm-btn-success { background: #10b981; color: white; }
  .pm-btn-success:hover { background: #059669; }
  .pm-btn-sm { padding: 4px 8px; font-size: 12px; }

  .pm-table-wrapper {
    overflow-x: auto;
  }
  .pm-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
  }
  .pm-table th, .pm-table td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #e2e8f0;
  }
  .pm-table th { background: #f8fafc; font-weight: 600; color: #475569; }
  .pm-table tr:hover { background: #f1f5f9; }

  .pm-badge {
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 700;
  }
  .pm-badge-email { background: #e0f2fe; color: #0369a1; }
  .pm-badge-address { background: #f3e8ff; color: #6b21a8; }

  .pm-tabs { display: flex; border-bottom: 1px solid #e2e8f0; margin-bottom: 16px; background: #f8fafc; padding: 4px 4px 0 4px; border-radius: 8px 8px 0 0; }
  .pm-tab-btn {
    padding: 10px 16px;
    cursor: pointer;
    border: none;
    background: none;
    font-weight: 600;
    font-size: 13px;
    color: #64748b;
    border-bottom: 2px solid transparent;
    transition: all 0.15s ease;
  }
  .pm-tab-btn:hover { color: #0284c7; }
  .pm-tab-btn.active { color: #0284c7; border-bottom-color: #0284c7; }

  .pm-pagination {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    border-top: 1px solid #e2e8f0;
    background: #f8fafc;
  }
  
  .pm-empty {
    padding: 40px;
    text-align: center;
    color: #64748b;
    font-style: italic;
  }
</style>

<div class="pm-container">
  <!-- Header Banner -->
  <div class="pm-header">
    <h1>Customer Management</h1>
    <p>Manage customer profiles, search using dynamic primary email index, and maintain party relations.</p>
  </div>

  <!-- Error/Success Alerts -->
  <#assign errorMessage = request.getAttribute("_ERROR_MESSAGE_")!>
  <#assign successMessage = request.getAttribute("_EVENT_MESSAGE_")!>
  <#if errorMessage?has_content>
    <div class="pm-alert pm-alert-error">
      <strong>Error:</strong> ${errorMessage}
    </div>
  </#if>
  <#if successMessage?has_content>
    <div class="pm-alert pm-alert-success">
      <strong>Success:</strong> ${successMessage}
    </div>
  </#if>

  <!-- Search Card -->
  <div class="pm-card">
    <div class="pm-card-header">Search Customers</div>
    <div class="pm-card-body">
      <form method="post" action="<@ofbizUrl>FindCustomer</@ofbizUrl>">
        <div class="pm-form-grid">
          <div class="pm-form-group">
            <label for="search_emailAddress">Email Address</label>
            <input type="text" id="search_emailAddress" name="emailAddress" value="${parameters.emailAddress!}" class="pm-form-control" placeholder="e.g. john@example.com"/>
          </div>
          <div class="pm-form-group">
            <label for="search_firstName">First Name</label>
            <input type="text" id="search_firstName" name="firstName" value="${parameters.firstName!}" class="pm-form-control" placeholder="e.g. John"/>
          </div>
          <div class="pm-form-group">
            <label for="search_lastName">Last Name</label>
            <input type="text" id="search_lastName" name="lastName" value="${parameters.lastName!}" class="pm-form-control" placeholder="e.g. Doe"/>
          </div>
          <div class="pm-form-group">
            <label for="search_contactNumber">Phone Number</label>
            <input type="text" id="search_contactNumber" name="contactNumber" value="${parameters.contactNumber!}" class="pm-form-control" placeholder="e.g. 555-0199"/>
          </div>
          <div class="pm-form-group">
            <label for="search_postalAddress">Address Line</label>
            <input type="text" id="search_postalAddress" name="postalAddress" value="${parameters.postalAddress!}" class="pm-form-control" placeholder="e.g. 123 Main St"/>
          </div>
        </div>
        <div style="margin-top: 16px; text-align: right;">
          <a href="<@ofbizUrl>FindCustomer</@ofbizUrl>" class="pm-btn pm-btn-secondary" style="margin-right: 8px; text-decoration: none;">Clear Filters</a>
          <button type="submit" class="pm-btn pm-btn-primary">Search Customers</button>
        </div>
      </form>
    </div>
  </div>

  <div class="pm-grid">
    <!-- Left: Results List -->
    <div>
      <div class="pm-card">
        <div class="pm-card-header">
          <span>Search Results (${listSize!0} records found)</span>
        </div>
        <div class="pm-table-wrapper">
          <#if customerList?has_content>
            <table class="pm-table">
              <thead>
                <tr>
                  <th>Party ID</th>
                  <th>Customer Name</th>
                  <th>Email Address</th>
                  <th>Phone</th>
                  <th>Postal Address</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <#list customerList as cust>
                  <tr>
                    <td><strong>${cust.partyId}</strong></td>
                    <td>${cust.firstName!} ${cust.lastName!}</td>
                    <td>
                      <#if cust.emailAddress?has_content>
                        <span class="pm-badge pm-badge-email">${cust.emailAddress}</span>
                      <#else>
                        <span style="color:#94a3b8;">N/A</span>
                      </#if>
                    </td>
                    <td>${cust.contactNumber!"-"}</td>
                    <td>
                      <#if cust.address1?has_content>
                        <span class="pm-badge pm-badge-address" title="${cust.address1}, ${cust.city!}, ${cust.stateProvinceGeoId!}, ${cust.postalCode!}">
                          ${cust.address1} (${cust.city!})
                        </span>
                      <#else>
                        -
                      </#if>
                    </td>
                    <td>
                      <button class="pm-btn pm-btn-success pm-btn-sm" 
                              onclick="editCustomer('${cust.partyId}', '${cust.emailAddress?js_string!}', '${cust.contactNumber!}', '${cust.address1?js_string!}', '${cust.city?js_string!}', '${cust.postalCode!}', '${cust.stateProvinceGeoId!}')">
                        Edit
                      </button>
                    </td>
                  </tr>
                </#list>
              </tbody>
            </table>
          <#else>
            <div class="pm-empty">No customers found matching the search criteria.</div>
          </#if>
        </div>
        
        <!-- Pagination controls -->
        <#if customerList?has_content>
          <div class="pm-pagination">
            <span>
              Showing Page <strong>${(viewIndex!0) + 1}</strong> of <strong>${((listSize!0 - 1) / (viewSize!10))?floor + 1}</strong>
            </span>
            <div>
              <#if (viewIndex!0) gt 0>
                <a href="<@ofbizUrl>FindCustomer?VIEW_INDEX=${viewIndex - 1}&VIEW_SIZE=${viewSize}&emailAddress=${parameters.emailAddress!}&firstName=${parameters.firstName!}&lastName=${parameters.lastName!}&contactNumber=${parameters.contactNumber!}&postalAddress=${parameters.postalAddress!}</@ofbizUrl>" class="pm-btn pm-btn-secondary pm-btn-sm" style="text-decoration:none;">Previous</a>
              </#if>
              <#if ((viewIndex!0) + 1) * (viewSize!10) lt (listSize!0)>
                <a href="<@ofbizUrl>FindCustomer?VIEW_INDEX=${viewIndex + 1}&VIEW_SIZE=${viewSize}&emailAddress=${parameters.emailAddress!}&firstName=${parameters.firstName!}&lastName=${parameters.lastName!}&contactNumber=${parameters.contactNumber!}&postalAddress=${parameters.postalAddress!}</@ofbizUrl>" class="pm-btn pm-btn-secondary pm-btn-sm" style="text-decoration:none; margin-left:8px;">Next</a>
              </#if>
            </div>
          </div>
        </#if>
      </div>
    </div>

    <!-- Right: Operations Panel -->
    <div>
      <div class="pm-card">
        <div class="pm-tabs">
          <button class="pm-tab-btn active" onclick="selectTab('tab-create')">Create</button>
          <button class="pm-tab-btn" onclick="selectTab('tab-update')">Update</button>
          <button class="pm-tab-btn" onclick="selectTab('tab-link')">Link Parties</button>
          <button class="pm-tab-btn" onclick="selectTab('tab-rel')">Relationships</button>
        </div>
        <div class="pm-card-body" style="padding-top: 0;">
          
          <!-- TAB 1: CREATE CUSTOMER -->
          <div id="tab-create" class="pm-tab-content">
            <h3 style="margin-top: 0; font-size:16px;">Create New Customer</h3>
            <form method="post" action="<@ofbizUrl>createCustomer</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="create_emailAddress">Email Address (Primary Unique)*</label>
                <input type="email" id="create_emailAddress" name="emailAddress" required class="pm-form-control" placeholder="e.g. customer@example.com"/>
              </div>
              <div class="pm-form-group">
                <label for="create_firstName">First Name*</label>
                <input type="text" id="create_firstName" name="firstName" required class="pm-form-control" placeholder="e.g. John"/>
              </div>
              <div class="pm-form-group">
                <label for="create_lastName">Last Name*</label>
                <input type="text" id="create_lastName" name="lastName" required class="pm-form-control" placeholder="e.g. Doe"/>
              </div>
              <div class="pm-form-group">
                <label for="create_contactNumber">Phone Number</label>
                <input type="text" id="create_contactNumber" name="contactNumber" class="pm-form-control" placeholder="e.g. 555-0199"/>
              </div>
              <div class="pm-form-group">
                <label for="create_postalAddress">Postal Address (Street)</label>
                <input type="text" id="create_postalAddress" name="postalAddress" class="pm-form-control" placeholder="e.g. 123 Main St"/>
              </div>
              <div class="pm-form-group">
                <label for="create_city">City</label>
                <input type="text" id="create_city" name="city" class="pm-form-control" placeholder="e.g. Salt Lake City"/>
              </div>
              <div class="pm-form-group">
                <label for="create_postalCode">Postal Code</label>
                <input type="text" id="create_postalCode" name="postalCode" class="pm-form-control" placeholder="e.g. 84101"/>
              </div>
              <div class="pm-form-group">
                <label for="create_stateProvinceGeoId">State / Province</label>
                <select id="create_stateProvinceGeoId" name="stateProvinceGeoId" class="pm-form-control">
                  <option value="">-- Select State --</option>
                  <#list statesList as state>
                    <option value="${state.geoId}">${state.geoName!state.geoId}</option>
                  </#list>
                </select>
              </div>
              <button type="submit" class="pm-btn pm-btn-primary" style="width: 100%; margin-top: 12px;">Create Customer</button>
            </form>
          </div>

          <!-- TAB 2: UPDATE CUSTOMER -->
          <div id="tab-update" class="pm-tab-content" style="display: none;">
            <h3 style="margin-top: 0; font-size:16px;">Update Customer Contact Mech</h3>
            <form method="post" action="<@ofbizUrl>updateCustomer</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="update_emailAddress">Email Address (ReadOnly Unique)*</label>
                <input type="email" id="update_emailAddress" name="emailAddress" readonly required class="pm-form-control" style="background:#f1f5f9; cursor:not-allowed;" placeholder="Select a customer from the list"/>
              </div>
              <div class="pm-form-group">
                <label for="update_contactNumber">Phone Number</label>
                <input type="text" id="update_contactNumber" name="contactNumber" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_postalAddress">Postal Address (Street)</label>
                <input type="text" id="update_postalAddress" name="postalAddress" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_city">City</label>
                <input type="text" id="update_city" name="city" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_postalCode">Postal Code</label>
                <input type="text" id="update_postalCode" name="postalCode" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_stateProvinceGeoId">State / Province</label>
                <select id="update_stateProvinceGeoId" name="stateProvinceGeoId" class="pm-form-control">
                  <option value="">-- Select State --</option>
                  <#list statesList as state>
                    <option value="${state.geoId}">${state.geoName!state.geoId}</option>
                  </#list>
                </select>
              </div>
              <button type="submit" class="pm-btn pm-btn-success" style="width: 100%; margin-top: 12px;">Save Changes</button>
            </form>
          </div>

          <!-- TAB 3: LINK PARTIES / RELATIONSHIP -->
          <div id="tab-link" class="pm-tab-content" style="display: none;">
            <h3 style="margin-top: 0; font-size:16px;">Link Two Parties</h3>
            <form method="post" action="<@ofbizUrl>createCustomerRelationship</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="link_partyIdFrom">Party ID From (Source)*</label>
                <select id="link_partyIdFrom" name="partyIdFrom" required class="pm-form-control">
                  <option value="">-- Select Party From --</option>
                  <#list partyList as p>
                    <option value="${p.partyId}">${p.lastName!""}, ${p.firstName!""} [${p.partyId}]</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="link_partyIdTo">Party ID To (Target)*</label>
                <select id="link_partyIdTo" name="partyIdTo" required class="pm-form-control">
                  <option value="">-- Select Party To --</option>
                  <#list partyList as p>
                    <option value="${p.partyId}">${p.lastName!""}, ${p.firstName!""} [${p.partyId}]</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="link_partyRelationshipTypeId">Relationship Type*</label>
                <select id="link_partyRelationshipTypeId" name="partyRelationshipTypeId" required class="pm-form-control">
                  <option value="">-- Select Type --</option>
                  <#list relationshipTypes as rt>
                    <option value="${rt.partyRelationshipTypeId}">${rt.description!rt.partyRelationshipTypeId}</option>
                  </#list>
                  <option value="CUSTOMER_REL">Customer Relationship</option>
                  <option value="AGENT">Agent Relationship</option>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="link_roleTypeIdFrom">Role Type From</label>
                <input type="text" id="link_roleTypeIdFrom" name="roleTypeIdFrom" class="pm-form-control" value="CUSTOMER"/>
              </div>
              <div class="pm-form-group">
                <label for="link_roleTypeIdTo">Role Type To</label>
                <input type="text" id="link_roleTypeIdTo" name="roleTypeIdTo" class="pm-form-control" value="CONTACT"/>
              </div>
              <div class="pm-form-group">
                <label for="link_statusId">Status</label>
                <input type="text" id="link_statusId" name="statusId" class="pm-form-control" value="PARTYREL_ACTIVE"/>
              </div>
              <button type="submit" class="pm-btn pm-btn-primary" style="width: 100%; margin-top: 12px;">Create Relationship</button>
            </form>
          </div>

          <!-- TAB 4: UPDATE RELATIONSHIP -->
          <div id="tab-rel" class="pm-tab-content" style="display: none;">
            <h3 style="margin-top: 0; font-size:16px;">Modify Existing Relationship</h3>
            <form method="post" action="<@ofbizUrl>updateCustomerRelationship</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="rel_partyIdFrom">Party ID From (Source)*</label>
                <select id="rel_partyIdFrom" name="partyIdFrom" required class="pm-form-control">
                  <option value="">-- Select Party From --</option>
                  <#list partyList as p>
                    <option value="${p.partyId}">${p.lastName!""}, ${p.firstName!""} [${p.partyId}]</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="rel_partyIdTo">Party ID To (Target)*</label>
                <select id="rel_partyIdTo" name="partyIdTo" required class="pm-form-control">
                  <option value="">-- Select Party To --</option>
                  <#list partyList as p>
                    <option value="${p.partyId}">${p.lastName!""}, ${p.firstName!""} [${p.partyId}]</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="rel_partyRelationshipTypeId">Relationship Type*</label>
                <select id="rel_partyRelationshipTypeId" name="partyRelationshipTypeId" required class="pm-form-control">
                  <option value="">-- Select Type --</option>
                  <#list relationshipTypes as rt>
                    <option value="${rt.partyRelationshipTypeId}">${rt.description!rt.partyRelationshipTypeId}</option>
                  </#list>
                  <option value="CUSTOMER_REL">Customer Relationship</option>
                  <option value="AGENT">Agent Relationship</option>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="rel_roleTypeIdFrom">Role Type From</label>
                <input type="text" id="rel_roleTypeIdFrom" name="roleTypeIdFrom" class="pm-form-control" value="CUSTOMER"/>
              </div>
              <div class="pm-form-group">
                <label for="rel_roleTypeIdTo">Role Type To</label>
                <input type="text" id="rel_roleTypeIdTo" name="roleTypeIdTo" class="pm-form-control" value="CONTACT"/>
              </div>
              <div class="pm-form-group">
                <label for="rel_fromDate">From Date (Timestamp Required)*</label>
                <input type="text" id="rel_fromDate" name="fromDate" required class="pm-form-control" placeholder="YYYY-MM-DD HH:MM:SS.SSS"/>
              </div>
              <div class="pm-form-group">
                <label for="rel_thruDate">Thru Date (Timestamp)</label>
                <input type="text" id="rel_thruDate" name="thruDate" class="pm-form-control" placeholder="YYYY-MM-DD HH:MM:SS.SSS"/>
              </div>
              <div class="pm-form-group">
                <label for="rel_statusId">Status</label>
                <input type="text" id="rel_statusId" name="statusId" class="pm-form-control" value="PARTYREL_TERMINATED"/>
              </div>
              <button type="submit" class="pm-btn pm-btn-success" style="width: 100%; margin-top: 12px;">Update Relationship</button>
            </form>
          </div>

        </div>
      </div>
    </div>
  </div>
</div>

<script>
  function selectTab(tabId) {
    document.querySelectorAll('.pm-tab-content').forEach(function(el) {
      el.style.display = 'none';
    });
    document.querySelectorAll('.pm-tab-btn').forEach(function(el) {
      el.classList.remove('active');
    });
    document.getElementById(tabId).style.display = 'block';
    // Find matching button
    var btn = Array.from(document.querySelectorAll('.pm-tab-btn')).find(b => b.getAttribute('onclick').includes(tabId));
    if (btn) btn.classList.add('active');
  }

  function editCustomer(partyId, emailAddress, contactNumber, address1, city, postalCode, stateProvinceGeoId) {
    document.getElementById('update_emailAddress').value = emailAddress;
    document.getElementById('update_contactNumber').value = contactNumber || '';
    document.getElementById('update_postalAddress').value = address1 || '';
    document.getElementById('update_city').value = city || '';
    document.getElementById('update_postalCode').value = postalCode || '';
    if (stateProvinceGeoId) {
      document.getElementById('update_stateProvinceGeoId').value = stateProvinceGeoId;
    } else {
      document.getElementById('update_stateProvinceGeoId').value = '';
    }
    selectTab('tab-update');
    document.getElementById('update_contactNumber').focus();
  }
</script>
