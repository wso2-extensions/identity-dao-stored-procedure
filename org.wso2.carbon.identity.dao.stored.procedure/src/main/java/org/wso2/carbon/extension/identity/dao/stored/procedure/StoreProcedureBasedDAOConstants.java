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
    protected static final String DBO_IDN_OAUTH_2_SCOPE = "dbo.IDN_OAUTH2_SCOPE";
    protected static final String DBO_IDN_OIDC_SCOPE_CLAIMS = "dbo.IDN_OIDC_SCOPE_CLAIMS";
}
