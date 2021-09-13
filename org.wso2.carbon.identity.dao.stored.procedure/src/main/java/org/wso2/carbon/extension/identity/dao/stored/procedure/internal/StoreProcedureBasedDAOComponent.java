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

package org.wso2.carbon.extension.identity.dao.stored.procedure.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.extension.identity.dao.stored.procedure.StoreProcedureBasedScopeClaimMappingDAOImpl;
import org.wso2.carbon.identity.core.persistence.IdentityDBInitializer;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.openidconnect.dao.ScopeClaimMappingDAO;

import java.sql.Connection;
import java.util.Map;

/**
 * The component for the Store Procedure Based DAO.
 */
@Component(
        name = "extension.identity.dao.stored.procedure",
        immediate = true
)
public class StoreProcedureBasedDAOComponent {

    private static final Log log = LogFactory.getLog(StoreProcedureBasedDAOComponent.class);
    private static final String OIDC_SCOPE_CLAIM_MAPPING_DAO_NAME = "oidc_scope_claim_mapping";
    private static final String MSSQL = "mssql";

    @Activate
    protected void activate(ComponentContext componentContext) {

        Map<String, Boolean> storeProcedureBasedDAOConfig = IdentityUtil.getStoreProcedureBasedDAOConfigurationHolder();
        Boolean scopeClaimMappingDAOEnabled = storeProcedureBasedDAOConfig.get(OIDC_SCOPE_CLAIM_MAPPING_DAO_NAME);
        if (scopeClaimMappingDAOEnabled) {
            Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
            try {
                String databaseType = IdentityDBInitializer.getDatabaseType(dbConnection);
                if (MSSQL.equals(databaseType)) {
                    componentContext.getBundleContext().registerService(ScopeClaimMappingDAO.class.getName(),
                            new StoreProcedureBasedScopeClaimMappingDAOImpl(), null);
                } else {
                    log.warn("The Store Procedure Based Scope Claim Mapping DAO implementation is not support for " +
                            databaseType + ". So the default DAO implementation will be used");
                }
            } finally {
                IdentityDatabaseUtil.closeConnection(dbConnection);
            }
        } else {
            log.info("The Store Procedure Based Scope Claim Mapping DAO implementation is not registered. " +
                    " As oidc_scope_claim_mapping is not enabled with the configuration.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Store Procedure Based DAO Component Activated.");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Store Procedure Based DAO Component Deactivated.");
        }
    }

    @Reference(
            name = "identity.core.init.event.service",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {

        /* Reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {

        /* Reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }
}
