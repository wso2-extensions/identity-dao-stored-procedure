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

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.oauth.dto.ScopeDTO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.Oauth2ScopeConstants;
import org.wso2.carbon.identity.openidconnect.dao.ScopeClaimMappingDAOImpl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ARG_CLAIMS;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ARG_SCOPES;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ARG_TENANT_ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .CALL_INIT_SCOPE_CLAIM_MAPPING;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.CLAIM;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.CLAIM_ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DBO_IDN_OAUTH_2_SCOPE;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DBO_IDN_OIDC_SCOPE_CLAIMS;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.DESCRIPTION;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.DISPLAY_NAME;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.NAME;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.SCOPE_ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.SCOPE_TYPE;

/**
 * Store Procedure Based OIDC Scope Claim mapping DAO.
 */
public class StoreProcedureBasedScopeClaimMappingDAOImpl extends ScopeClaimMappingDAOImpl {

    private static final Log log = LogFactory.getLog(StoreProcedureBasedScopeClaimMappingDAOImpl.class);

    @Override
    public void initScopeClaimMapping(int tenantId, List<ScopeDTO> scopeClaimsList) throws IdentityOAuth2Exception {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            SQLServerDataTable scopesDataTable = new SQLServerDataTable();
            scopesDataTable.addColumnMetadata(SCOPE_ID, Types.INTEGER);
            scopesDataTable.addColumnMetadata(NAME, Types.NVARCHAR);
            scopesDataTable.addColumnMetadata(DISPLAY_NAME, Types.NVARCHAR);
            scopesDataTable.addColumnMetadata(DESCRIPTION, Types.NVARCHAR);
            scopesDataTable.addColumnMetadata(SCOPE_TYPE, Types.NVARCHAR);

            SQLServerDataTable claimsDataTable = new SQLServerDataTable();
            claimsDataTable.addColumnMetadata(SCOPE_ID, Types.INTEGER);
            claimsDataTable.addColumnMetadata(CLAIM_ID, Types.INTEGER);
            claimsDataTable.addColumnMetadata(CLAIM, Types.NVARCHAR);
            
            for (int i = 0; i < scopeClaimsList.size(); i++) {
                ScopeDTO scopeDTO = scopeClaimsList.get(i);
                String[] claims = scopeDTO.getClaim();
                scopesDataTable.addRow(i, scopeDTO.getName(), scopeDTO.getDisplayName(), scopeDTO.getDescription(),
                        Oauth2ScopeConstants.SCOPE_TYPE_OIDC);
                for (int j = 0; j < claims.length; j++) {
                    claimsDataTable.addRow(i, j, claims[j]);
                }
            }
            try (CallableStatement stmt = dbConnection.prepareCall(CALL_INIT_SCOPE_CLAIM_MAPPING)) {
                if (stmt.isWrapperFor(SQLServerCallableStatement.class)) {
                    // The CallableStatement object can unwrap to SQLServerCallableStatement.
                    SQLServerCallableStatement sqlCstmt = stmt.unwrap(SQLServerCallableStatement.class);
                    sqlCstmt.setInt(ARG_TENANT_ID, tenantId);
                    sqlCstmt.setStructured(ARG_SCOPES, DBO_IDN_OAUTH_2_SCOPE, scopesDataTable);
                    sqlCstmt.setStructured(ARG_CLAIMS, DBO_IDN_OIDC_SCOPE_CLAIMS, claimsDataTable);
                    sqlCstmt.execute();
                } else {
                    String errorMessage = "Cannot process scope init for the tenant: " + tenantId;
                    throw new IdentityOAuth2Exception(errorMessage);
                }
            }
            dbConnection.commit();
            if (log.isDebugEnabled()) {
                log.debug("The scopes successfully inserted for the tenant: " + tenantId);
            }
        } catch (SQLException e) {
            String errorMessage = "Error while persisting claims for the scope for the tenant: " + tenantId;
            throw new IdentityOAuth2Exception(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }
}
