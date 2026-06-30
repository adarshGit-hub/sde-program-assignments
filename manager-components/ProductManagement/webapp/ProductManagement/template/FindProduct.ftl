<style>
  .pm-container {
    font-family: 'Outfit', 'Inter', sans-serif;
    padding: 20px;
    background: #f8fafc;
    color: #1e293b;
  }
  .pm-header {
    margin-bottom: 24px;
    background: linear-gradient(135deg, #4f46e5, #7c3aed);
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
    grid-template-columns: 2.5fr 1.5fr;
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
  .pm-form-control:focus { border-color: #6366f1; }

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
  .pm-btn-primary { background: #4f46e5; color: white; }
  .pm-btn-primary:hover { background: #4338ca; }
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
    text-transform: uppercase;
  }
  .pm-badge-virtual { background: #dbeafe; color: #1e40af; }
  .pm-badge-variant { background: #fef3c7; color: #92400e; }
  .pm-badge-category { background: #f3e8ff; color: #6b21a8; margin-right: 4px; display: inline-block; }
  .pm-badge-feature { background: #e0f2fe; color: #075985; margin-right: 4px; display: inline-block; }

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
  .pm-tab-btn:hover { color: #4f46e5; }
  .pm-tab-btn.active { color: #4f46e5; border-bottom-color: #4f46e5; }

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
    <h1>Product Management</h1>
    <p>Design view-entities, search products dynamically, and manage virtual/variant relations.</p>
  </div>

  <!-- Messages Panel -->
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

  <!-- Main Search Panel -->
  <div class="pm-card">
    <div class="pm-card-header">Search Filters</div>
    <div class="pm-card-body">
      <form method="post" action="<@ofbizUrl>FindProduct</@ofbizUrl>">
        <div class="pm-form-grid">
          <div class="pm-form-group">
            <label for="search_productId">Product ID</label>
            <input type="text" id="search_productId" name="productId" value="${parameters.productId!}" class="pm-form-control" placeholder="e.g. 10000"/>
          </div>
          <div class="pm-form-group">
            <label for="search_productName">Product Name</label>
            <input type="text" id="search_productName" name="productName" value="${parameters.productName!}" class="pm-form-control" placeholder="e.g. Gizmo"/>
          </div>
          <div class="pm-form-group">
            <label for="search_productCategoryId">Category</label>
            <select id="search_productCategoryId" name="productCategoryId" class="pm-form-control">
              <option value="">-- All Categories --</option>
              <#list productCategories as cat>
                <option value="${cat.productCategoryId}" <#if parameters.productCategoryId! == cat.productCategoryId>selected</#if>>
                  ${cat.categoryName!cat.description!cat.productCategoryId}
                </option>
              </#list>
            </select>
          </div>
          <div class="pm-form-group">
            <label for="search_price">Max Price (USD)</label>
            <input type="number" step="0.01" id="search_price" name="price" value="${parameters.price!}" class="pm-form-control" placeholder="e.g. 50.00"/>
          </div>
          <div class="pm-form-group">
            <label for="search_productFeatureId">Feature</label>
            <select id="search_productFeatureId" name="productFeatureId" class="pm-form-control">
              <option value="">-- All Features --</option>
              <#list productFeatures as feat>
                <option value="${feat.productFeatureId}" <#if parameters.productFeatureId! == feat.productFeatureId>selected</#if>>
                  ${feat.description!feat.productFeatureId} [${feat.productFeatureTypeId!}]
                </option>
              </#list>
            </select>
          </div>
        </div>
        <div style="margin-top: 16px; text-align: right;">
          <a href="<@ofbizUrl>FindProduct</@ofbizUrl>" class="pm-btn pm-btn-secondary" style="margin-right: 8px; text-decoration: none;">Clear Filters</a>
          <button type="submit" class="pm-btn pm-btn-primary">Search Products</button>
        </div>
      </form>
    </div>
  </div>

  <div class="pm-grid">
    <!-- Left: Results Table -->
    <div>
      <div class="pm-card">
        <div class="pm-card-header">
          <span>Search Results (${listSize!0} records found)</span>
        </div>
        <div class="pm-table-wrapper">
          <#if productList?has_content>
            <table class="pm-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Internal Name</th>
                  <th>Product Name</th>
                  <th>Virtual/Variant</th>
                  <th>Price</th>
                  <th>Categories</th>
                  <th>Features</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <#list productList as product>
                  <tr>
                    <td><strong>${product.productId}</strong></td>
                    <td>${product.internalName!}</td>
                    <td>${product.productName!}</td>
                    <td>
                      <#if product.isVirtual == 'Y'>
                        <span class="pm-badge pm-badge-virtual">Virtual</span>
                      </#if>
                      <#if product.isVariant == 'Y'>
                        <span class="pm-badge pm-badge-variant">Variant</span>
                      </#if>
                    </td>
                    <td>
                      <#if product.price??>
                        $${product.price?string("0.00")}
                      <#else>
                        <span style="color:#94a3b8;">N/A</span>
                      </#if>
                    </td>
                    <td>
                      <#if product.categories?has_content>
                        <#list product.categories as cat>
                          <span class="pm-badge pm-badge-category">${cat.categoryName}</span>
                        </#list>
                      <#else>
                        -
                      </#if>
                    </td>
                    <td>
                      <#if product.features?has_content>
                        <#list product.features as feat>
                          <span class="pm-badge pm-badge-feature" title="${feat.productFeatureTypeId}">${feat.description}</span>
                        </#list>
                      <#else>
                        -
                      </#if>
                    </td>
                    <td>
                      <!-- Setup Edit button -->
                      <#assign firstCategoryId = "">
                      <#if product.categories?has_content>
                        <#assign firstCategoryId = product.categories[0].productCategoryId>
                      </#if>
                      <#assign priceString = "">
                      <#if product.price??>
                        <#assign priceString = product.price?string("0.00")>
                      </#assign>
                      <button class="pm-btn pm-btn-success pm-btn-sm" 
                              onclick="editProduct('${product.productId}', '${product.internalName!}', '${product.productName?js_string!}', '${product.description?js_string!}', '${product.isVirtual!}', '${product.isVariant!}', '${firstCategoryId}', '${priceString}')">
                        Edit
                      </button>
                    </td>
                  </tr>
                </#list>
              </tbody>
            </table>
          <#else>
            <div class="pm-empty">No products matched the filter criteria.</div>
          </#if>
        </div>
        
        <!-- Pagination -->
        <#if productList?has_content>
          <div class="pm-pagination">
            <span>
              Showing Page <strong>${(viewIndex!0) + 1}</strong> of <strong>${((listSize!0 - 1) / (viewSize!10))?floor + 1}</strong>
            </span>
            <div>
              <#if (viewIndex!0) gt 0>
                <a href="<@ofbizUrl>FindProduct?VIEW_INDEX=${viewIndex - 1}&VIEW_SIZE=${viewSize}&productId=${parameters.productId!}&productName=${parameters.productName!}&productCategoryId=${parameters.productCategoryId!}&price=${parameters.price!}&productFeatureId=${parameters.productFeatureId!}</@ofbizUrl>" class="pm-btn pm-btn-secondary pm-btn-sm" style="text-decoration:none;">Previous</a>
              </#if>
              <#if ((viewIndex!0) + 1) * (viewSize!10) lt (listSize!0)>
                <a href="<@ofbizUrl>FindProduct?VIEW_INDEX=${viewIndex + 1}&VIEW_SIZE=${viewSize}&productId=${parameters.productId!}&productName=${parameters.productName!}&productCategoryId=${parameters.productCategoryId!}&price=${parameters.price!}&productFeatureId=${parameters.productFeatureId!}</@ofbizUrl>" class="pm-btn pm-btn-secondary pm-btn-sm" style="text-decoration:none; margin-left:8px;">Next</a>
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
          <button class="pm-tab-btn" onclick="selectTab('tab-assoc')">Associate</button>
          <button class="pm-tab-btn" onclick="selectTab('tab-variant')">Variant Features</button>
        </div>
        <div class="pm-card-body" style="padding-top: 0;">
          
          <!-- TAB 1: CREATE PRODUCT -->
          <div id="tab-create" class="pm-tab-content">
            <h3 style="margin-top: 0; font-size:16px;">Create New Product</h3>
            <form method="post" action="<@ofbizUrl>createProduct</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="create_productId">Product ID (Optional)</label>
                <input type="text" id="create_productId" name="productId" class="pm-form-control" placeholder="Leave empty for auto-ID"/>
              </div>
              <div class="pm-form-group">
                <label for="create_internalName">Internal Name (Unique)*</label>
                <input type="text" id="create_internalName" name="internalName" required class="pm-form-control" placeholder="e.g. EX_PRODUCT_01"/>
              </div>
              <div class="pm-form-group">
                <label for="create_productName">Product Name</label>
                <input type="text" id="create_productName" name="productName" class="pm-form-control" placeholder="e.g. Example Widget"/>
              </div>
              <div class="pm-form-group">
                <label for="create_description">Description</label>
                <input type="text" id="create_description" name="description" class="pm-form-control" placeholder="Product details"/>
              </div>
              <div class="pm-form-group">
                <label for="create_isVirtual">Is Virtual?</label>
                <select id="create_isVirtual" name="isVirtual" class="pm-form-control">
                  <option value="N">No (Variant / Standard)</option>
                  <option value="Y">Yes (Virtual Parent)</option>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="create_isVariant">Is Variant?</label>
                <select id="create_isVariant" name="isVariant" class="pm-form-control">
                  <option value="N">No</option>
                  <option value="Y">Yes</option>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="create_productCategoryId">Initial Category</label>
                <select id="create_productCategoryId" name="productCategoryId" class="pm-form-control">
                  <option value="">-- None --</option>
                  <#list productCategories as cat>
                    <option value="${cat.productCategoryId}">${cat.categoryName!cat.description!cat.productCategoryId}</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="create_price">Default Price (USD)</label>
                <input type="number" step="0.01" id="create_price" name="price" class="pm-form-control" placeholder="e.g. 19.99"/>
              </div>
              <button type="submit" class="pm-btn pm-btn-primary" style="width: 100%; margin-top: 12px;">Create Product</button>
            </form>
          </div>

          <!-- TAB 2: UPDATE PRODUCT -->
          <div id="tab-update" class="pm-tab-content" style="display: none;">
            <h3 style="margin-top: 0; font-size:16px;">Update Product</h3>
            <form method="post" action="<@ofbizUrl>updateProduct</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="update_productId">Product ID*</label>
                <input type="text" id="update_productId" name="productId" readonly required class="pm-form-control" style="background:#f1f5f9; cursor:not-allowed;" placeholder="Select a product from the list"/>
              </div>
              <div class="pm-form-group">
                <label for="update_internalName">Internal Name (Unique)*</label>
                <input type="text" id="update_internalName" name="internalName" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_productName">Product Name</label>
                <input type="text" id="update_productName" name="productName" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_description">Description</label>
                <input type="text" id="update_description" name="description" class="pm-form-control"/>
              </div>
              <div class="pm-form-group">
                <label for="update_isVirtual">Is Virtual?</label>
                <select id="update_isVirtual" name="isVirtual" class="pm-form-control">
                  <option value="N">No</option>
                  <option value="Y">Yes</option>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="update_isVariant">Is Variant?</label>
                <select id="update_isVariant" name="isVariant" class="pm-form-control">
                  <option value="N">No</option>
                  <option value="Y">Yes</option>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="update_productCategoryId">Category</label>
                <select id="update_productCategoryId" name="productCategoryId" class="pm-form-control">
                  <option value="">-- No Change --</option>
                  <#list productCategories as cat>
                    <option value="${cat.productCategoryId}">${cat.categoryName!cat.description!cat.productCategoryId}</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="update_price">Default Price (USD)</label>
                <input type="number" step="0.01" id="update_price" name="price" class="pm-form-control"/>
              </div>
              <button type="submit" class="pm-btn pm-btn-success" style="width: 100%; margin-top: 12px;">Save Changes</button>
            </form>
          </div>

          <!-- TAB 3: ASSOCIATE VIRTUAL / VARIANT -->
          <div id="tab-assoc" class="pm-tab-content" style="display: none;">
            <h3 style="margin-top: 0; font-size:16px;">Associate Variant to Virtual</h3>
            <form method="post" action="<@ofbizUrl>assocProductToVirtual</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="assoc_virtualProductId">Virtual Parent Product*</label>
                <select id="assoc_virtualProductId" name="virtualProductId" required class="pm-form-control">
                  <option value="">-- Select Virtual Product --</option>
                  <#list virtualProducts as vp>
                    <option value="${vp.productId}">${vp.internalName!} [${vp.productId}]</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="assoc_variantProductId">Variant Product*</label>
                <select id="assoc_variantProductId" name="variantProductId" required class="pm-form-control">
                  <option value="">-- Select Variant Product --</option>
                  <#list variantProducts as vr>
                    <option value="${vr.productId}">${vr.internalName!} [${vr.productId}]</option>
                  </#list>
                </select>
              </div>
              <button type="submit" class="pm-btn pm-btn-primary" style="width: 100%; margin-top: 12px;">Associate Products</button>
            </form>
          </div>

          <!-- TAB 4: UPDATE VARIANT FEATURES -->
          <div id="tab-variant" class="pm-tab-content" style="display: none;">
            <h3 style="margin-top: 0; font-size:16px;">Manage Variant Details</h3>
            <form method="post" action="<@ofbizUrl>updateProductVariant</@ofbizUrl>">
              <div class="pm-form-group">
                <label for="var_productId">Variant Product*</label>
                <select id="var_productId" name="productId" required class="pm-form-control">
                  <option value="">-- Select Variant --</option>
                  <#list variantProducts as vr>
                    <option value="${vr.productId}">${vr.internalName!} [${vr.productId}]</option>
                  </#list>
                </select>
              </div>
              <div class="pm-form-group">
                <label for="var_price">Variant Specific Price (USD)</label>
                <input type="number" step="0.01" id="var_price" name="price" class="pm-form-control" placeholder="e.g. 21.99"/>
              </div>
              <div class="pm-form-group">
                <label for="var_productFeatureId">Assign Standard Feature</label>
                <select id="var_productFeatureId" name="productFeatureId" class="pm-form-control">
                  <option value="">-- Select Feature --</option>
                  <#list productFeatures as feat>
                    <option value="${feat.productFeatureId}">${feat.description!feat.productFeatureId} [${feat.productFeatureTypeId!}]</option>
                  </#list>
                </select>
              </div>
              <button type="submit" class="pm-btn pm-btn-success" style="width: 100%; margin-top: 12px;">Update Variant</button>
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
    // Find the button triggering this and add active
    var btn = Array.from(document.querySelectorAll('.pm-tab-btn')).find(b => b.getAttribute('onclick').includes(tabId));
    if (btn) btn.classList.add('active');
  }

  function editProduct(productId, internalName, productName, description, isVirtual, isVariant, categoryId, price) {
    document.getElementById('update_productId').value = productId;
    document.getElementById('update_internalName').value = internalName;
    document.getElementById('update_productName').value = productName;
    document.getElementById('update_description').value = description;
    document.getElementById('update_isVirtual').value = isVirtual;
    document.getElementById('update_isVariant').value = isVariant;
    if (categoryId) {
      document.getElementById('update_productCategoryId').value = categoryId;
    } else {
      document.getElementById('update_productCategoryId').value = '';
    }
    if (price) {
      document.getElementById('update_price').value = price;
    } else {
      document.getElementById('update_price').value = '';
    }
    selectTab('tab-update');
    document.getElementById('update_internalName').focus();
  }
</script>
