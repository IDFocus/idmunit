LDAP Connector
---------------
The LdapConnector provides methods to allow you to execute LDAP operations against an LDAP directory and optionally validate objects and attributes from IdMUnit tests.

OPERATIONS
----------
AddAttr:
* dn - LDAP DN of the object to update
* [column names] - column values to be added as attributes to the object

AddObject:
* dn - LDAP DN of the object to create
* [column names] - column values to be added as attributes of the object

ClearAttr:
* dn - LDAP DN of the object to update or an LDAP filter that will result in the object to update
* [column names] - specify an '*' as the value for attributes to be cleared

DeleteObject:
* dn - LDAP DN of the object to delete or an LDAP filter that will result in the object to delete

MoveObject:
* dn - LDAP DN of the object to move
* newdn - New LDAP DN for the object

RemoveAttr:
* dn - LDAP DN of the object to update or an LDAP filter that will result in the object to update
* [column names] - column values to be removed from attributes of the object. If DirXML-Associations is specified as the column name then only the driver DN portion of the column value is compared to the current values of the object to determine whether or not to remove a value. 

RenameObject:
* dn - LDAP DN of the object to rename
* newdn - New LDAP DN for the object

ReplaceAttr:
* dn - LDAP DN of the object to update
* [column names] - column values will replace the attribute values of the object

AttrDoesNotExist:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* [column names] - column values to compare with attributes of the object

ValidateObject:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* [column names] - column values to compare with attributes of the object

ValidateObjectDoesNotExist:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* [column names] - column values to compare with attributes of the object

ValidatePassword:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* userPassword - password to be validated

CONFIGURATION
-------------
To configure this connector you need to specify a server, user, and password.

    <connection>
        <name>IDV</name>
        <description>Connector to an LDAP server</description>
        <type>com.trivir.idmunit.connector.LdapConnector</type>
        <server>192.168.1.3</server>
        <user>cn=admin,o=services</user>
        <password>B2vPD2UsfKc=</password>
        <multiplier/>
        <substitutions/>
        <data-injections/>
    </connection>

The following configuration parameters are optional:

* port: specifies the port to be used to connect to the server. If this is not specified, 389 will be used unless use-tls is true, in which case 636 will be used.
* use-tls (true|false): specifies whether or not TLS will be used
* keystore-path: path to keystore containing the certificate for the server or the certificate of the CA that signed the server's certificate. If this option is specified, trust-all-certs and trusted-cert-file are ignored.
* trusted-cert-file: path to a file containing the certificate for the server or the certificate of the CA that signed the server's certificate. 
* trust-all-certs (true|false):

If TLS is enabled and the certificate chain validation fails then the certificate chain is written to a file. If a value for trusted-cert-file is specified, that will be used as the file name. Otherwise, the file name will be the [server].cer if the port is 636 or [server]_[port].cer if a port other than 636 is specified.
