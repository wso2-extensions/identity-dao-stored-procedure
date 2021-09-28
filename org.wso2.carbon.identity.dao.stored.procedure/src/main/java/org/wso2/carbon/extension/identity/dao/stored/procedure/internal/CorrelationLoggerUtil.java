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

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Util to print the correlation log for the Stored procedure based DAO SQL queries. Because
 * the kernel CorrelationLogInterceptor will not be invoked for these operations as in these DAO,
 * we are directly invoking the specific database driver implementations.
 */
public class CorrelationLoggerUtil {

    private static final Log correlationLog = LogFactory.getLog("correlation");
    private static final String CORRELATION_LOG_CALL_TYPE_VALUE = "jdbc";
    private static final String CORRELATION_LOG_SEPARATOR = "|";
    private static final String CORRELATION_LOG_SYSTEM_PROPERTY = "enableCorrelationLogs";
    private static final String EXECUTE_STORED_PROCEDURE = "executeStoredProcedure";
    private static final boolean isEnableCorrelationLogs =
            Boolean.parseBoolean(System.getProperty(CORRELATION_LOG_SYSTEM_PROPERTY));

    /**
     * Logs the details of the stored procedure calls.
     *
     * @param callableStatement     SQL Server callable statement.
     * @param query                 Executed stored procedure query.
     * @param delta                 Time taken for the execution.
     * @param start                 Start time of the execution.
     * @throws SQLException SQL Exception.
     */
    public static void logQueryDetails(SQLServerCallableStatement callableStatement, String query, long delta,
                                       long start) throws SQLException {

        if (!isEnableCorrelationLogs) {
            return;
        }

        DatabaseMetaData metaData = callableStatement.getConnection().getMetaData();
        if (correlationLog.isInfoEnabled()) {
            List<String> logPropertiesList = new ArrayList<>();
            logPropertiesList.add(Long.toString(delta));
            logPropertiesList.add(CORRELATION_LOG_CALL_TYPE_VALUE);
            logPropertiesList.add(Long.toString(start));
            logPropertiesList.add(EXECUTE_STORED_PROCEDURE);
            logPropertiesList.add(query);
            logPropertiesList.add(metaData.getURL());
            correlationLog.info(createFormattedLog(logPropertiesList));
        }
    }

    /**
     * Creates the log line that should be printed.
     *
     * @param logPropertiesList Contains the log values that should be printed in the log.
     * @return The log line
     */
    private static String createFormattedLog(List<String> logPropertiesList) {

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String property : logPropertiesList) {
            sb.append(property);
            if (count < logPropertiesList.size() - 1) {
                sb.append(CORRELATION_LOG_SEPARATOR);
            }
            count++;
        }
        return sb.toString();
    }
}
