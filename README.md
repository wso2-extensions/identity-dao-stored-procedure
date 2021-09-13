# identity-dao-stored-procedure
This will be an extension of the wso2-is. It will support Store Procedure based DAO Implementation for bulk database operations.

**Note : Currently store procedure based DAO implementation is support for Microsoft SQL Database only.**


# How to build and configure with WSO2 Identity Server
This supported from wso2 Identity Server 5.12.0 for the creating the OIDC scope claim mapping.

- Build the project using maven.

    ```mvn clean install```

- Copy the `org.wso2.carbon.identity.dao.stored.procedure/target/org.wso2.carbon.identity.dao.stored.procedure-<version>.jar`
  to `<IS_HOME>/repository/components/dropins`
  
- Configure Microsoft SQL Database as Identity DB of the Server by following https://is.docs.wso2.com/en/latest/setup/changing-to-mssql/#changing-to-mssql
  
- Create the Data types in the Microsoft SQL Database using the `scripts/mssql/data-types.sql`

- Create the Store Procedure in the Microsoft SQL Database using the `scripts/mssql/procedures.sql`
  
- Add the following configuration to `<IS_HOME>/repository/config/deployment.toml` to enable the Store Procedure 
  based DAO Implementation for the OIDC Scope Claim Mapping.
    ```
    [stored_procedure_dao]
    oidc_scope_claim_mapping = true
    ```
