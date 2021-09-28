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


CREATE PROCEDURE InitClaimConfig(
    @tenantId INTEGER,
    @CLAIM_DIALECTS AS DT_IDN_CLAIM_DIALECT READONLY,
    @CLAIMS AS DT_IDN_CLAIM READONLY,
    @CLAIM_MAPPED_ATTRIBUTE AS DT_IDN_CLAIM_MAPPED_ATTRIBUTE READONLY,
    @CLAIM_PROPERTY AS DT_IDN_CLAIM_PROPERTY READONLY,
    @CLAIM_MAPPING AS DT_CLAIM_MAPPING READONLY
    )
    AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    SAVE TRANSACTION transactionSavePoint;
    BEGIN TRY
        -- Table to store claim dialect ID.
        DECLARE @CLAIM_DIALECTS_RESULT table (DIALET_ID INTEGER , GEN_ID INTEGER);
        -- Table to store claim ID.
        DECLARE @CLAIM_RESULT table (DIALET_ID INTEGER, CLAIM_ID INTEGER , GEN_ID INTEGER, CLAIM_URI VARCHAR (255));


        -- Looping through each claim dialect.
        DECLARE @dialectRowCnt INT = (SELECT COUNT(DIALECT_ID) FROM @CLAIM_DIALECTS);
        DECLARE @dialectId INT = 0;
        WHILE @dialectId < @dialectRowCnt
        BEGIN
            -- Inserting the claim dialect and get the dialectId
            INSERT INTO IDN_CLAIM_DIALECT (DIALECT_URI, TENANT_ID) OUTPUT @dialectId, Inserted.ID into @CLAIM_DIALECTS_RESULT SELECT DIALECT_URI, @tenantId FROM @CLAIM_DIALECTS WHERE DIALECT_ID  = @dialectId;

            DECLARE @genDialectId INT = (SELECT GEN_ID FROM @CLAIM_DIALECTS_RESULT WHERE DIALET_ID = @dialectId);

            -- Looping through each claims.
            DECLARE @claimRowCnt INT = (SELECT COUNT(CLAIM_ID) FROM @CLAIMS WHERE DIALECT_ID = @dialectId);
            DECLARE @claimId INT = 0;
            WHILE @claimId < @claimRowCnt
            BEGIN
                -- Inserting the claim and get the claimId
                INSERT INTO IDN_CLAIM (DIALECT_ID, CLAIM_URI, TENANT_ID) OUTPUT @dialectId, @claimId, Inserted.ID, Inserted.CLAIM_URI  into @CLAIM_RESULT SELECT @genDialectId, CLAIM_URI, @tenantId FROM @CLAIMS WHERE DIALECT_ID =  @dialectId AND CLAIM_ID = @claimID;

                DECLARE @genClaimId INT = (SELECT GEN_ID FROM @CLAIM_RESULT WHERE DIALET_ID = @dialectId AND CLAIM_ID = @claimId);

                -- Adding claim mapping for the remote claims
                DECLARE @mappedClaimURI VARCHAR (255) = (SELECT MAPPED_CLAIM_URI FROM @CLAIM_MAPPING WHERE DIALECT_ID = @dialectId AND CLAIM_ID = @claimId);
                IF ((@mappedClaimURI IS NOT NULL) OR (LEN(@mappedClaimURI) > 0))
                BEGIN
                    DECLARE @mappedClaimId INT = (SELECT GEN_ID FROM @CLAIM_RESULT WHERE DIALET_ID = 0 AND CLAIM_URI = @mappedClaimURI);
                    IF ((@mappedClaimId IS NOT NULL) OR (LEN(@mappedClaimId) > 0))
                    BEGIN
                        INSERT INTO IDN_CLAIM_MAPPING (MAPPED_LOCAL_CLAIM_ID, EXT_CLAIM_ID, TENANT_ID) VALUES (@mappedClaimId, @genClaimId, @tenantId);
                    END
                    ELSE BEGIN
                        -- Mapped Claim id is not found for the mapped local claim URI throw exception
                        DECLARE @msg NVARCHAR(2048) = 'The  Mapped Claim id is not found for the mapped local claim URI ' + @mappedClaimURI;
                        THROW 51000, @msg, 1;
                    END
                END

                -- Looping through each claim mapped attribute.
                DECLARE @attributeRowCnt INT = (SELECT COUNT(CLAIM_ID) FROM @CLAIM_MAPPED_ATTRIBUTE WHERE DIALECT_ID = @dialectId AND CLAIM_ID = @claimId);
                DECLARE @attributeId INT = 0;
                WHILE @attributeId < @attributeRowCnt
                BEGIN
                    -- Inserting the claim mapped attribute.
                    INSERT INTO IDN_CLAIM_MAPPED_ATTRIBUTE (LOCAL_CLAIM_ID, USER_STORE_DOMAIN_NAME, ATTRIBUTE_NAME, TENANT_ID) SELECT @genClaimId, USER_STORE_DOMAIN_NAME, ATTRIBUTE_NAME, @tenantId FROM @CLAIM_MAPPED_ATTRIBUTE WHERE DIALECT_ID =  @dialectId AND CLAIM_ID = @claimID AND ID = @attributeId;
                    SET @attributeId += 1;
                END

                -- Looping through each claim property.
                DECLARE @propertyRowCnt INT = (SELECT COUNT(CLAIM_ID) FROM @CLAIM_PROPERTY WHERE DIALECT_ID = @dialectId AND CLAIM_ID = @claimId);
                DECLARE @propertyId INT = 0;
                WHILE @propertyId < @propertyRowCnt
                BEGIN
                    -- Inserting the claim property.
                    INSERT INTO IDN_CLAIM_PROPERTY (LOCAL_CLAIM_ID, PROPERTY_NAME, PROPERTY_VALUE, TENANT_ID) SELECT @genClaimId, PROPERTY_NAME, PROPERTY_VALUE, @tenantId FROM @CLAIM_PROPERTY WHERE DIALECT_ID =  @dialectId AND CLAIM_ID = @claimID AND ID = @propertyId;
                    SET @propertyId += 1;
                END

                SET @claimId += 1;
            END
            SET @dialectId += 1;
        END
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
        BEGIN
            ROLLBACK TRANSACTION transactionSavePoint; -- rollback to MySavePoint
        END;
        THROW;
    END CATCH
END;
