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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimConfigInitDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.claim.ClaimKey;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ARG_CLAIMS;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .ARG_CLAIM_DIALECTS;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .ARG_CLAIM_MAPPED_ATTRIBUTE;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ARG_CLAIM_MAPPING;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .ARG_CLAIM_PROPERTY;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ARG_TENANT_ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ATTRIBUTE_NAME;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .CALL_INIT_CLAIM_CONFIG;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.CLAIM_ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.CLAIM_URI;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DATA_TYPE_IDN_CLAIM;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DATA_TYPE_IDN_CLAIM_DIALECT;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DATA_TYPE_IDN_CLAIM_MAPPED_ATTRIBUTE;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DATA_TYPE_IDN_CLAIM_MAPPING;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .DATA_TYPE_IDN_CLAIM_PROPERTY;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.DIALECT_ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.DIALECT_URI;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.ID;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.MAPPED_CLAIM_URI;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.PROPERTY_NAME;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants.PROPERTY_VALUE;
import static org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedDAOConstants
        .USER_STORE_DOMAIN_NAME;

/**
 * Stored Procedure Based Claim configuration initialize DAO.
 */
public class StoreProcedureBasedClaimConfigInitDAO implements ClaimConfigInitDAO {

    private static final Log log = LogFactory.getLog(StoreProcedureBasedClaimConfigInitDAO.class);

    @Override
    public void initClaimConfig(ClaimConfig claimConfig, int tenantId) throws ClaimMetadataException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            SQLServerDataTable claimDialectDT = new SQLServerDataTable();
            claimDialectDT.addColumnMetadata(DIALECT_ID, Types.INTEGER);
            claimDialectDT.addColumnMetadata(DIALECT_URI, Types.NVARCHAR);

            SQLServerDataTable claimDT = new SQLServerDataTable();
            claimDT.addColumnMetadata(DIALECT_ID, Types.INTEGER);
            claimDT.addColumnMetadata(CLAIM_ID, Types.INTEGER);
            claimDT.addColumnMetadata(CLAIM_URI, Types.NVARCHAR);

            SQLServerDataTable claimMappingDT = new SQLServerDataTable();
            claimMappingDT.addColumnMetadata(DIALECT_ID, Types.INTEGER);
            claimMappingDT.addColumnMetadata(CLAIM_ID, Types.INTEGER);
            claimMappingDT.addColumnMetadata(MAPPED_CLAIM_URI, Types.NVARCHAR);

            SQLServerDataTable claimMappedAttributeDT = new SQLServerDataTable();
            claimMappedAttributeDT.addColumnMetadata(DIALECT_ID, Types.INTEGER);
            claimMappedAttributeDT.addColumnMetadata(CLAIM_ID, Types.INTEGER);
            claimMappedAttributeDT.addColumnMetadata(ID, Types.INTEGER);
            claimMappedAttributeDT.addColumnMetadata(USER_STORE_DOMAIN_NAME, Types.NVARCHAR);
            claimMappedAttributeDT.addColumnMetadata(ATTRIBUTE_NAME, Types.NVARCHAR);

            SQLServerDataTable claimPropertyDT = new SQLServerDataTable();
            claimPropertyDT.addColumnMetadata(DIALECT_ID, Types.INTEGER);
            claimPropertyDT.addColumnMetadata(CLAIM_ID, Types.INTEGER);
            claimPropertyDT.addColumnMetadata(ID, Types.INTEGER);
            claimPropertyDT.addColumnMetadata(PROPERTY_NAME, Types.NVARCHAR);
            claimPropertyDT.addColumnMetadata(PROPERTY_VALUE, Types.NVARCHAR);

            int claimDialectCounter = 1;
            Map<String, Integer> claimDialectIds = new HashMap<>();
            Map<String, Integer> claimCountPerDialect = new HashMap<>();
            String primaryDomainName = IdentityUtil.getPrimaryDomainName();

            // Adding local claim dialect URI as the first entry.
            claimDialectDT.addRow(0, ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
            claimDialectIds.put(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, 0);

            if (claimConfig.getClaimMap() != null) {

                for (Map.Entry<ClaimKey, ClaimMapping> claimEntry : claimConfig.getClaimMap().entrySet()) {

                    ClaimKey claimKey = claimEntry.getKey();
                    ClaimMapping claimMapping = claimEntry.getValue();
                    String claimDialectURI = claimMapping.getClaim().getDialectURI();

                    int claimDialectId;
                    Integer availableClaimDialectId = claimDialectIds.get(claimDialectURI);
                    if (availableClaimDialectId == null) {
                        claimDialectId = claimDialectCounter;
                        claimDialectDT.addRow(claimDialectCounter, claimDialectURI);
                        claimDialectIds.put(claimDialectURI, claimDialectCounter++);
                    } else {
                        claimDialectId = availableClaimDialectId;
                    }

                    claimCountPerDialect.putIfAbsent(claimDialectURI, 0);
                    int claimId = claimCountPerDialect.get(claimDialectURI);
                    claimCountPerDialect.put(claimDialectURI, claimId + 1);

                    String claimURI = claimKey.getClaimUri();
                    claimDT.addRow(claimDialectId, claimId, claimURI);

                    if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {
                        String mappedLocalClaimURI = claimConfig.getPropertyHolderMap().get(claimKey)
                                .get(ClaimConstants.MAPPED_LOCAL_CLAIM_PROPERTY);
                        claimMappingDT.addRow(claimDialectId, claimId, mappedLocalClaimURI);

                    }

                    if (StringUtils.isNotBlank(claimMapping.getMappedAttribute())) {
                        claimMappedAttributeDT.addRow(claimDialectId, claimId, 0,
                                primaryDomainName, claimMapping.getMappedAttribute());
                        int id = 1;
                        for (Map.Entry<String, String> entry : claimMapping.getMappedAttributes().entrySet()) {
                            claimMappedAttributeDT.addRow(claimDialectId, claimId, id++,
                                    entry.getKey(), entry.getValue());
                        }
                    }

                    Map<String, String> properties = fillClaimProperties(claimConfig, claimKey);
                    int id = 0;
                    for (Map.Entry<String, String> entry : properties.entrySet()) {
                        claimPropertyDT.addRow(claimDialectId, claimId, id++, entry.getKey(), entry.getValue());
                    }
                }

                try (CallableStatement stmt = dbConnection.prepareCall(CALL_INIT_CLAIM_CONFIG)) {
                    if (stmt.isWrapperFor(SQLServerCallableStatement.class)) {
                        // The CallableStatement object can unwrap to SQLServerCallableStatement.
                        SQLServerCallableStatement sqlCstmt = stmt.unwrap(SQLServerCallableStatement.class);
                        sqlCstmt.setInt(ARG_TENANT_ID, tenantId);
                        sqlCstmt.setStructured(ARG_CLAIM_DIALECTS, DATA_TYPE_IDN_CLAIM_DIALECT, claimDialectDT);
                        sqlCstmt.setStructured(ARG_CLAIMS, DATA_TYPE_IDN_CLAIM, claimDT);
                        sqlCstmt.setStructured(ARG_CLAIM_MAPPED_ATTRIBUTE, DATA_TYPE_IDN_CLAIM_MAPPED_ATTRIBUTE,
                                claimMappedAttributeDT);
                        sqlCstmt.setStructured(ARG_CLAIM_PROPERTY, DATA_TYPE_IDN_CLAIM_PROPERTY, claimPropertyDT);
                        sqlCstmt.setStructured(ARG_CLAIM_MAPPING, DATA_TYPE_IDN_CLAIM_MAPPING, claimMappingDT);
                        sqlCstmt.execute();
                    } else {
                        String errorMessage = "Cannot process claim configuration init for the tenant: " + tenantId;
                        throw new ClaimMetadataException(errorMessage);
                    }
                }
                IdentityDatabaseUtil.commitTransaction(dbConnection);
                if (log.isDebugEnabled()) {
                    log.debug("The claim configuration successfully inserted for the tenant: " + tenantId);
                }
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            String errorMessage = "Error while persisting claims configuration for the tenant: " + tenantId;
            throw new ClaimMetadataException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    private Map<String, String> fillClaimProperties(ClaimConfig claimConfig, ClaimKey claimKey) {

        Map<String, String> claimProperties = claimConfig.getPropertyHolderMap().get(claimKey);
        claimProperties.remove(ClaimConstants.DIALECT_PROPERTY);
        claimProperties.remove(ClaimConstants.CLAIM_URI_PROPERTY);
        claimProperties.remove(ClaimConstants.ATTRIBUTE_ID_PROPERTY);

        if (!claimProperties.containsKey(ClaimConstants.DISPLAY_NAME_PROPERTY)) {
            claimProperties.put(ClaimConstants.DISPLAY_NAME_PROPERTY, "0");
        }

        if (claimProperties.containsKey(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY)) {
            if (StringUtils.isBlank(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY))) {
                claimProperties.put(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY, "true");
            }
        }

        if (claimProperties.containsKey(ClaimConstants.READ_ONLY_PROPERTY)) {
            if (StringUtils.isBlank(claimProperties.get(ClaimConstants.READ_ONLY_PROPERTY))) {
                claimProperties.put(ClaimConstants.READ_ONLY_PROPERTY, "true");
            }
        }

        if (claimProperties.containsKey(ClaimConstants.REQUIRED_PROPERTY)) {
            if (StringUtils.isBlank(claimProperties.get(ClaimConstants.REQUIRED_PROPERTY))) {
                claimProperties.put(ClaimConstants.REQUIRED_PROPERTY, "true");
            }
        }
        return claimProperties;
    }
}
