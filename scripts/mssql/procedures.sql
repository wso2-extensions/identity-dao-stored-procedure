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

CREATE PROCEDURE InitScopeClaimMapping(
    @tenantId INTEGER,
    @SCOPES AS DT_IDN_OAUTH2_SCOPE READONLY,
    @CLAIMS AS DT_IDN_OIDC_SCOPE_CLAIMS READONLY)
    AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    SAVE TRANSACTION transactionSavePoint;
    BEGIN TRY

        DECLARE @RESULT table (NAME VARCHAR(255), ID INTEGER);

        DECLARE @scopeRowCnt INT = (SELECT COUNT(SCOPE_ID) FROM @SCOPES);
        DECLARE @scopeId INT = 0;

        -- Looping through each OIDC scope.
        WHILE @scopeId < @scopeRowCnt
        BEGIN
            -- Inserting the OIDC scope.
            INSERT INTO IDN_OAUTH2_SCOPE (NAME, DISPLAY_NAME, DESCRIPTION, TENANT_ID, SCOPE_TYPE) OUTPUT Inserted.NAME, Inserted.SCOPE_ID into @RESULT SELECT NAME, DISPLAY_NAME, DESCRIPTION, @tenantId, SCOPE_TYPE FROM @SCOPES WHERE SCOPE_ID = @scopeId;

            -- Getting the auto generated scope id of the inserted scope.
            DECLARE @generatedScopeId int = (SELECT TOP 1 ID FROM @RESULT WHERE NAME IN (SELECT NAME FROM @SCOPES WHERE SCOPE_ID = @scopeId));

            DECLARE @claimRowCnt INT = (SELECT COUNT(*) FROM @CLAIMS WHERE SCOPE_ID = @scopeId);
            DECLARE @claimId INT = 0;
            DECLARE @claim NVARCHAR(255);

            -- Looping through claims of the inserted scope.
            WHILE @claimId < @claimRowCnt
            BEGIN
                -- The claim which needs to be added for this iteration.
                SELECT @claim = CLAIM FROM @CLAIMS WHERE CLAIM_ID = @claimId AND SCOPE_ID = @scopeId;

                -- Inserting the claim entry for the scope.
                INSERT INTO IDN_OIDC_SCOPE_CLAIM_MAPPING (SCOPE_ID, EXTERNAL_CLAIM_ID) SELECT @generatedScopeId, IDN_CLAIM.ID FROM IDN_CLAIM LEFT JOIN IDN_CLAIM_DIALECT ON IDN_CLAIM_DIALECT.ID = IDN_CLAIM.DIALECT_ID WHERE CLAIM_URI=@claim AND IDN_CLAIM_DIALECT.DIALECT_URI='http://wso2.org/oidc/claim' AND IDN_CLAIM_DIALECT.TENANT_ID=@tenantId

                SET @claimId += 1;
            END;
            SET @scopeId += 1;
        END;
        -- Committing the transaction if all the scope claims mapping added correctly.
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
        BEGIN
        -- If error occurred and rollback to saved transaction point.
        ROLLBACK TRANSACTION transactionSavePoint;
        END;
            THROW;
    END CATCH
END;
