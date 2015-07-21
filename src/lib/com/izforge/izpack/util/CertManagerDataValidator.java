package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;

import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import sun.security.x509.X500Name;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.ssl.CertificateVerifier;


public class CertManagerDataValidator implements DataValidator
{
    
    private String strMessage = "";
    public static final String strMessageId = "messageid";
    public static final String strMessageValue = "message.oldvalue"; // not to be stored
    
    private KeyPair pairCA = null;
    private X509Certificate cacert = null;
    

    public String getErrorMessageId()
    {
        return strMessageId;
    }

    public String getWarningMessageId()
    {
        return strMessageId;
    }

    public boolean getDefaultAnswer()
    {
        return false;
    }

    

    public Status validateData(AutomatedInstallData adata)
    {
        Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));
        Boolean mongoSSL = Boolean.valueOf(adata.getVariable("mongodb.ssl.enable"));
        
        Status statusReturn = Status.OK;
        try
        {

            ///////////////////////////////////////////////////////////////////////////////////////////
            // first Syracuse part
            // setup only when not update mode 
            if (!modifyinstallation)
            {
                String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();
                String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+localHOST_NAME;
                String hostname = "";
                Boolean certCreate = Boolean.valueOf(adata.getVariable("syracuse.certificate.install"));
                
                if (certCreate)
                {
                    certCreate (adata);
                    hostname = adata.getVariable("syracuse.certificate.hostname").toLowerCase();
                }
                else
                {

                    adata.setVariable("syracuse.certificate.certtool",adata.getVariable("INSTALL_PATH") + File.separator + "syracuse" + File.separator + "certs_tools");

                    
                    readCerts (adata);
                    
                    CertificateFactory factory = CertificateFactory.getInstance("X.509");
                    InputStream inPemCertFile = new FileInputStream(adata.getVariable("syracuse.ssl.certfile"));
                    X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);
                    
                    X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName()); 
                    hostname = x500Name.getCommonName().toLowerCase();                    
                    adata.setVariable("syracuse.certificate.serverpassphrase",adata.getVariable("syracuse.ssl.pemkeypassword"));
                }

                // set for mongodb install
                adata.setVariable("mongodb.ssl.server.serverpassphrase",adata.getVariable("syracuse.certificate.serverpassphrase"));
                adata.setVariable("mongodb.ssl.server.certfile",strCertPath + File.separator + hostname+".crt");
                adata.setVariable("mongodb.ssl.server.pemkeyfile",strCertPath + File.separator + hostname+".key");
                adata.setVariable("mongodb.ssl.server.pemcafile",strCertPath + File.separator + "ca.cacrt");
                

            }
            

            if (mongoSSL)
            {
            ///////////////////////////////////////////////////////////////////////////////////////////
            // Second MongoDB part
            if (!modifyinstallation)
            {
                Boolean mongodbInstall = Boolean.valueOf(adata.getVariable("mongodb.service.install"));
                Boolean certCreate = Boolean.valueOf(adata.getVariable("syracuse.certificate.install"));
                if (certCreate)
                {
                    if (mongodbInstall)
                    {
                        clientCertCreate (adata);
                        adata.setVariable("mongodb.service.hostname",adata.getVariable("syracuse.certificate.hostname").toLowerCase());
                    }
                    else
                    {
                        clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
                        
                    }
                }
                else
                {
                    if (mongodbInstall)
                    {
                        clientPutInPlace (adata, adata.getVariable("syracuse.ssl.pemcafile"));
                        CertificateFactory factory = CertificateFactory.getInstance("X.509");
                        InputStream inPemCertFile = new FileInputStream(adata.getVariable("syracuse.ssl.certfile"));
                        X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);
                        
                        X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName()); 
                        
                        adata.setVariable("mongodb.service.hostname",x500Name.getCommonName().toLowerCase());
                    }
                    else
                    {
                        clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
                    }
                }
                
            }
            else
            {
                clientPutInPlace (adata, adata.getVariable("mongodb.ssl.pemcafile"));
            }
            }
            
            
            return Status.OK;
        
        }
        catch (Exception ex)
        {
            strMessage = ex.getMessage();
            adata.setVariable(strMessageValue, strMessage);
        }

    return Status.ERROR;
    }

    public void  clientPutInPlace (AutomatedInstallData adata, String strFieldCa) throws Exception
    {
        // client cert is provided with CA
        // only need to copy if not already in place
        
        //mongodb.ssl.client.certfile
        //mongodb.ssl.client.pemkeyfile
        //mongodb.ssl.pemcafile

        String fieldPemCertFile = adata.getVariable("mongodb.ssl.client.certfile");
        String fieldPemKeyFile = adata.getVariable("mongodb.ssl.client.pemkeyfile");
        String fieldPemCaFile = strFieldCa;
        

        String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+"mongodb";
        File dirCerts =  new File (strCertPath);
        if (!dirCerts.exists()) dirCerts.mkdirs();

        File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
        File certsServerCRT = new File (strCertPath + File.separator + "client.crt");
        File certsServerKey = new File (strCertPath + File.separator + "client.key");
        File certsServerPem = new File (strCertPath + File.separator + "client.pem");
        
        // copy CA in output directory
        if (fieldPemCaFile!= null && !"".equals(fieldPemCaFile) && !fieldPemCaFile.equals(strCertPath + File.separator + "ca.cacrt"))
        {
            File sourceCaCRT = new File (fieldPemCaFile);
            Files.copy(sourceCaCRT.toPath(), certsCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        if (!fieldPemCertFile.equals(strCertPath + File.separator + "client.crt"))
        {
            File sourceServerCRT = new File (fieldPemCertFile);
            Files.copy(sourceServerCRT.toPath(), certsServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        if (!fieldPemKeyFile.equals(strCertPath + File.separator + "client.key"))
        {
            File sourceServerKey = new File (fieldPemKeyFile);
            Files.copy(sourceServerKey.toPath(), certsServerKey.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        KeyPairGeneratorDataValidator.mergeFiles(new File[]{certsServerCRT,certsServerKey}, certsServerPem);
        
        // set variables for future use
        adata.setVariable("mongodb.ssl.client.certfile",strCertPath + File.separator + "client.crt");
        adata.setVariable("mongodb.ssl.client.pemkeyfile",strCertPath + File.separator + "client.key");
        adata.setVariable("mongodb.ssl.pemcafile",strCertPath + File.separator + "ca.cacrt");    }    
    
    
    public void  clientCertCreate (AutomatedInstallData adata) throws Exception
    {
        String countryCode = adata.getVariable("syracuse.certificate.countrycode");
        String state = adata.getVariable("syracuse.certificate.state");
        String city = adata.getVariable("syracuse.certificate.city");
        String organization = adata.getVariable("syracuse.certificate.organization");
        String organizationalUnit = adata.getVariable("syracuse.certificate.organisationalunit");
        String name = adata.getVariable("syracuse.certificate.name");
        String email = adata.getVariable("syracuse.certificate.email");
        int validity = Integer.parseInt(adata.getVariable("syracuse.certificate.validity"));
        String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();

        // need to create client cert
        // then  create a client cert
        KeyPair pairClient = CreateCertsValidator.generateRSAKeyPair(2048);
        X509Certificate clientcert = CreateCertsValidator.generateClientV3Certificate(pairClient, countryCode, organization, organizationalUnit,
                state, city, name, email, validity, cacert , pairCA);
        
        String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+"mongodb";
        File dirCerts =  new File (strCertPath);
        if (!dirCerts.exists()) dirCerts.mkdirs();

        File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
        File certsServerCRT = new File (strCertPath + File.separator + "client.crt");
        File certsServerKey = new File (strCertPath + File.separator + "client.key");
        File certsServerPem = new File (strCertPath + File.separator + "client.pem");
        
        FileWriter cacertfile = new FileWriter(certsCaCRT);
        PEMWriter pem = new PEMWriter(cacertfile);
        pem.writeObject(cacert);
        pem.close();
        
        FileWriter clientcertfile = new FileWriter(certsServerCRT);
        pem = new PEMWriter(clientcertfile);
        pem.writeObject(clientcert);
        pem.close();

        KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + "client.key", pairClient, null);

        KeyPairGeneratorDataValidator.mergeFiles(new File[]{certsServerCRT,certsServerKey}, certsServerPem);
        
        // set variables for future use
        adata.setVariable("mongodb.ssl.client.certfile",strCertPath + File.separator + "client.crt");
        adata.setVariable("mongodb.ssl.client.pemkeyfile",strCertPath + File.separator + "client.key");
        adata.setVariable("mongodb.ssl.pemcafile",strCertPath + File.separator + "ca.cacrt");

    }    
    
    public void  readCerts (AutomatedInstallData adata) throws Exception
    {
        String fieldPemCertFile = adata.getVariable("syracuse.ssl.certfile");
        String fieldPemKeyFile = adata.getVariable("syracuse.ssl.pemkeyfile");
        String fieldPemKeyPassword = adata.getVariable("syracuse.ssl.pemkeypassword");
        String fieldPemCaFile = adata.getVariable("syracuse.ssl.pemcafile");
        String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();
        
        // prepare directory tree for syracuse
        String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+localHOST_NAME;
        File dirCerts =  new File (strCertPath);
        if (!dirCerts.exists()) dirCerts.mkdirs();
        String strCertToolPath = adata.getVariable("syracuse.certificate.certtool");
        File dirCertToolPath =  new File (strCertToolPath);
        if (!dirCertToolPath.exists()) dirCertToolPath.mkdirs();
        File dirCertToolOutputPath =  new File (strCertToolPath+File.separator+"output");
        if (!dirCertToolOutputPath.exists()) dirCertToolOutputPath.mkdirs();
        File dirCertToolPrivatePath =  new File (strCertToolPath+File.separator+"private");
        if (!dirCertToolPrivatePath.exists()) dirCertToolPrivatePath.mkdirs();
        
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        
        // load CA for later use
        InputStream inPemCaFile = new FileInputStream(fieldPemCaFile);
        cacert = (X509Certificate) factory.generateCertificate(inPemCaFile);
        
        // copy CA in output directory
        File sourceCaCRT = new File (fieldPemCaFile);
        File certToolOutputCaCRT = new File (strCertToolPath+File.separator+"output" + File.separator + "ca.cacrt");
        File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
        Files.copy(sourceCaCRT.toPath(), certToolOutputCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceCaCRT.toPath(), certsCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        

        InputStream inPemCertFile = new FileInputStream(fieldPemCertFile);
        X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);
        
        X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName()); 
        String cn = x500Name.getCommonName().toLowerCase();        
        
        // copy Cert in output directory
        File sourceserverCRT = new File (fieldPemCertFile);
        File certToolOutputServerCRT = new File (strCertToolPath+File.separator+"output" + File.separator + cn +".crt");
        File certsServerCRT = new File (strCertPath + File.separator + cn + ".crt");
        Files.copy(sourceserverCRT.toPath(), certToolOutputServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceserverCRT.toPath(), certsServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // copy key in output directory
        File sourceserverKey = new File (fieldPemKeyFile);
        File certToolOutputServerkey = new File (strCertToolPath+File.separator+"output" + File.separator + cn +".key");
        File certsServerkey = new File (strCertPath + File.separator + cn + ".key");
        Files.copy(sourceserverKey.toPath(), certToolOutputServerkey.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceserverKey.toPath(), certsServerkey.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // public key in output directoru
        PublicKey pubKey = cert.getPublicKey();
        File certToolOutputServerPub = new File (strCertToolPath+File.separator+"output" + File.separator + cn+".pem");
        FileWriter serverpubfile = new FileWriter(certToolOutputServerPub);
        PEMWriter pem = new PEMWriter(serverpubfile);
        pem.writeObject(pubKey);
        pem.close();
        
        // public key in x3runtime
        Boolean setx3runtime = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3runtime"));
        if (setx3runtime)
        {
            String strX3RuntimePath = adata.getVariable("syracuse.certificate.x3runtime");
            String pemName = cn.replace('@', '_').replace('$', '_').replace('.', '_');
            File x3ServerPub = new File (strX3RuntimePath+File.separator+"keys" + File.separator + pemName+".pem");
            Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // public key in x3webserver
        Boolean setx3webserver = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3webserver"));
        if (setx3webserver)
        {
            String strX3WebserverPath = adata.getVariable("syracuse.certificate.x3webserverdata");
            String pemName = cn.replace('@', '_').replace('$', '_').replace('.', '_');
            File x3ServerPub = new File (strX3WebserverPath + File.separator + pemName+".pem");
            Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        
// do I really need to load private key ?
//         InputStream inPemKeyFile = new FileInputStream(fieldPemKeyFile);
//        // Then check the private key
//        PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
//        Object object = pemParser.readObject();
//        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
//        KeyPair kp;
//        if (object instanceof PEMEncryptedKeyPair)
//        {
//            // Encrypted key - we will use provided password
//            PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
//            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(fieldPemKeyPassword.toCharArray());
//            kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
//        }
//        else
//        {
//            // Unencrypted key - no password needed
//            PEMKeyPair ukp = (PEMKeyPair) object;
//            kp = converter.getKeyPair(ukp);
//        }
        
        
        
        
        
        
    }    
    
    public void  certCreate (AutomatedInstallData adata) throws Exception
    {
        
        String countryCode = adata.getVariable("syracuse.certificate.countrycode");
        String state = adata.getVariable("syracuse.certificate.state");
        String city = adata.getVariable("syracuse.certificate.city");
        String organization = adata.getVariable("syracuse.certificate.organization");
        String organizationalUnit = adata.getVariable("syracuse.certificate.organisationalunit");
        String name = adata.getVariable("syracuse.certificate.name");
        String email = adata.getVariable("syracuse.certificate.email");
        int validity = Integer.parseInt(adata.getVariable("syracuse.certificate.validity"));
        String localHOST_NAME = adata.getVariable("HOST_NAME").toLowerCase();
        
        // prepare directory tree for syracuse
        String strCertPath = adata.getVariable("syracuse.dir.certs")+File.separator+localHOST_NAME;
        File dirCerts =  new File (strCertPath);
        if (!dirCerts.exists()) dirCerts.mkdirs();
        String strCertToolPath = adata.getVariable("syracuse.certificate.certtool");
        File dirCertToolPath =  new File (strCertToolPath);
        if (!dirCertToolPath.exists()) dirCertToolPath.mkdirs();
        File dirCertToolOutputPath =  new File (strCertToolPath+File.separator+"output");
        if (!dirCertToolOutputPath.exists()) dirCertToolOutputPath.mkdirs();
        File dirCertToolPrivatePath =  new File (strCertToolPath+File.separator+"private");
        if (!dirCertToolPrivatePath.exists()) dirCertToolPrivatePath.mkdirs();
        
        // first create CA
        pairCA = CreateCertsValidator.generateRSAKeyPair(4096);

        cacert = CreateCertsValidator.generateCAV3Certificate(pairCA, countryCode, organization, organizationalUnit,
                state, city, name, email, validity);

        // copy in certs directory
        File certsCaCRT = new File (strCertPath + File.separator + "ca.cacrt");
        FileWriter cacertfile = new FileWriter(certsCaCRT);
        PEMWriter pem = new PEMWriter(cacertfile);
        pem.writeObject(cacert);
        pem.close();
        
        // copy in output directory
        File certToolOutputCaCRT = new File (strCertToolPath+File.separator+"output" + File.separator + "ca.cacrt");
        Files.copy(certsCaCRT.toPath(), certToolOutputCaCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // ca private key in private directory
        String capassphrase = adata.getVariable("syracuse.certificate.capassphrase");
        KeyPairGeneratorDataValidator.writePrivateKey(strCertToolPath+File.separator+"private" + File.separator + "ca.cakey", pairCA, capassphrase.toCharArray());
        
        
        // then create server cert
        KeyPair pairServer = CreateCertsValidator.generateRSAKeyPair(2048);
        String hostname = adata.getVariable("syracuse.certificate.hostname").toLowerCase();

        X509Certificate servercert = CreateCertsValidator.generateServerV3Certificate(pairServer, countryCode, organization, organizationalUnit,
                state, city, hostname, null, validity, cacert , pairCA);
        
        // copy in certs directory
        File certsServerCRT = new File (strCertPath + File.separator + hostname+".crt");
        FileWriter servercertfile = new FileWriter(certsServerCRT);
        pem = new PEMWriter(servercertfile);
        pem.writeObject(servercert);
        pem.close();

        // copy in output directory
        File certToolOutputServerCRT = new File (strCertToolPath+File.separator+"output" + File.separator + hostname+".crt");
        Files.copy(certsServerCRT.toPath(), certToolOutputServerCRT.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        
        // private key in certs directory and output
        String serverpassphrase = adata.getVariable("syracuse.certificate.serverpassphrase");
        KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + hostname + ".key", pairServer, serverpassphrase.toCharArray());
        File certToolOutputServerkey = new File (strCertToolPath+File.separator+"output" + File.separator + hostname+".key");
        File certsServerkey = new File (strCertPath + File.separator + hostname + ".key");
        Files.copy(certsServerkey.toPath(), certToolOutputServerkey.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // public key in output
        File certToolOutputServerPub = new File (strCertToolPath+File.separator+"output" + File.separator + hostname+".pem");
        FileWriter serverpubfile = new FileWriter(certToolOutputServerPub);
        pem = new PEMWriter(serverpubfile);
        pem.writeObject(pairServer.getPublic());
        pem.close();
        
        // public key in x3runtime
        Boolean setx3runtime = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3runtime"));
        if (setx3runtime)
        {
            String strX3RuntimePath = adata.getVariable("syracuse.certificate.x3runtime");
            String pemName = hostname.replace('@', '_').replace('$', '_').replace('.', '_');
            File x3ServerPub = new File (strX3RuntimePath+File.separator+"keys" + File.separator + pemName+".pem");
            Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // public key in x3webserver
        Boolean setx3webserver = Boolean.valueOf(adata.getVariable("syracuse.certificate.setx3webserver"));
        if (setx3webserver)
        {
            String strX3WebserverPath = adata.getVariable("syracuse.certificate.x3webserverdata");
            String pemName = hostname.replace('@', '_').replace('$', '_').replace('.', '_');
            File x3ServerPub = new File (strX3WebserverPath + File.separator + pemName+".pem");
            Files.copy(certToolOutputServerPub.toPath(), x3ServerPub.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
    }


}
