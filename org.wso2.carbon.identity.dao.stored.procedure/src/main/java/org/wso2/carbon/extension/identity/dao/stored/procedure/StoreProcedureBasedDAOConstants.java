/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.extension.identity.dao.stored.procedure;

/**
 * Constants used for the Stored Procedure Based DAOs.
 */
public class StoreProcedureBasedDAOConstants {

    // Constants for OIDC Scope Claim Mapping DAO.
    protected static final String SCOPE_ID = "SCOPE_ID";
    protected static final String NAME = "NAME";
    protected static final String DISPLAY_NAME = "DISPLAY_NAME";
    protected static final String DESCRIPTION = "DESCRIPTION";
    protected static final String SCOPE_TYPE = "SCOPE_TYPE";
    protected static final String CLAIM_ID = "CLAIM_ID";
    protected static final String CLAIM = "CLAIM";
    protected static final String CALL_INIT_SCOPE_CLAIM_MAPPING = "{call dbo.InitScopeClaimMapping(?, ?, ?)}";
    protected static final String ARG_TENANT_ID = "tenantId";
    protected static final String ARG_SCOPES = "SCOPES";
    protected static final String ARG_CLAIMS = "CLAIMS";
    protected static final String DATA_TYPE_IDN_OAUTH_2_SCOPE = "DT_IDN_OAUTH2_SCOPE";
    protected static final String DATA_TYPE_IDN_OIDC_SCOPE_CLAIMS = "DT_IDN_OIDC_SCOPE_CLAIMS";

    // Constants for Scope Config Init DAO.
    protected static final String DIALECT_ID = "DIALECT_ID";
    protected static final String DIALECT_URI = "DIALECT_URI";
    protected static final String CLAIM_URI = "CLAIM_URI";
    protected static final String MAPPED_CLAIM_URI = "MAPPED_CLAIM_URI";
    protected static final String ID = "ID";
    protected static final String USER_STORE_DOMAIN_NAME = "USER_STORE_DOMAIN_NAME";
    protected static final String ATTRIBUTE_NAME = "ATTRIBUTE_NAME";
    protected static final String PROPERTY_NAME = "PROPERTY_NAME";
    protected static final String PROPERTY_VALUE = "PROPERTY_VALUE";
    protected static final String CALL_INIT_CLAIM_CONFIG = "{call dbo.InitClaimConfig(?, ?, ?, ?, ?, ?)}";
    protected static final String ARG_CLAIM_DIALECTS = "CLAIM_DIALECTS";
    protected static final String ARG_CLAIM_MAPPED_ATTRIBUTE = "CLAIM_MAPPED_ATTRIBUTE";
    protected static final String ARG_CLAIM_PROPERTY = "CLAIM_PROPERTY";
    protected static final String ARG_CLAIM_MAPPING = "CLAIM_MAPPING";
    protected static final String DATA_TYPE_IDN_CLAIM_DIALECT = "DT_IDN_CLAIM_DIALECT";
    protected static final String DATA_TYPE_IDN_CLAIM = "DT_IDN_CLAIM";
    protected static final String DATA_TYPE_IDN_CLAIM_MAPPED_ATTRIBUTE = "DT_IDN_CLAIM_MAPPED_ATTRIBUTE";
    protected static final String DATA_TYPE_IDN_CLAIM_PROPERTY = "DT_IDN_CLAIM_PROPERTY";
    protected static final String DATA_TYPE_IDN_CLAIM_MAPPING = "DT_CLAIM_MAPPING";
}
