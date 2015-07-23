package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.DataValidator.Status;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoIterable;


public class MongoDBDataValidator implements DataValidator
{
	
//	// Ignore differences between given hostname and certificate hostname
//    private static HostnameVerifier hostVerifier = new HostnameVerifier() {
//        public boolean verify(String hostname, SSLSession session) { return true; }
//    };	

    public Status validateData(AutomatedInstallData adata)
    {
        Status bReturn = Status.OK;
        try
        {
        
            Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));

            //String userName = adata.getVariable("mongodb.url.username");
            //String passWord = adata.getVariable("mongodb.url.password");
            
            String hostName = new String (adata.getVariable("mongodb.service.hostname"));
            String hostPort = adata.getVariable("mongodb.service.port");
            boolean sslEnabled = "true".equalsIgnoreCase(adata.getVariable("mongodb.ssl.enable"));
            String certFile = adata.getVariable("mongodb.ssl.client.certfile");
            String pemkeyFile = adata.getVariable("mongodb.ssl.client.pemkeyfile");
            String pemcaFile = adata.getVariable("mongodb.ssl.pemcafile");
            
            if (!sslEnabled)
            {
            
                MongoClient mongoClient = new MongoClient( hostName , Integer.parseInt(hostPort) );
                
                //if (bImportMode) bReturn = Status.WARNING;
                //else bReturn = Status.OK; 
    
                // test if syracuse db already exists
                MongoIterable<String> lstDb = mongoClient.listDatabaseNames();
                
                for (String dbb : lstDb)
                {
                    if (dbb.equals("syracuse"))
                    {
                        if (modifyinstallation) bReturn = Status.OK;
                        else bReturn = Status.WARNING;
                        break;
                    }
                }
                
                mongoClient.close();
            }
            else
            {
                
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());        

                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                ArrayList chainArray = new ArrayList();
                
                if (pemcaFile !=null && !"".equals(pemcaFile))
                {
                    //create truststore
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(null, null);
                    InputStream inPemCaFile = new FileInputStream(pemcaFile);
                    X509Certificate cacert = (X509Certificate) factory.generateCertificate(inPemCaFile);
                    trustStore.setCertificateEntry("root", cacert);
                    File trustStoreFile = File.createTempFile("tru", null);
                    FileOutputStream trustStoreFileOutputStream = new FileOutputStream(trustStoreFile);
                    trustStore.store(trustStoreFileOutputStream, "truststore".toCharArray());
                    trustStoreFileOutputStream.close();
                    
                    chainArray.add(cacert);
                    
                    System.setProperty("javax.net.ssl.trustStore", trustStoreFile.getAbsolutePath());
                    System.setProperty("javax.net.ssl.trustStorePassword", "truststore");
                    
                }
                
                //create keystore
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);

                InputStream inPemCertFile = new FileInputStream(certFile);
                X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);
                
                InputStream inPemKeyFile = new FileInputStream(pemkeyFile);
                PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
                Object object = pemParser.readObject();
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                PEMKeyPair ukp = (PEMKeyPair) object;
                KeyPair kp = converter.getKeyPair(ukp);                
                
                keyStore.setCertificateEntry(cert.getSubjectX500Principal().toString(), cert);
                
                chainArray.add (cert);
                
                X509Certificate[] chain = new X509Certificate[chainArray.size()];
                chain[0] = cert;
                if (chainArray.size()>1) chain[1]= (X509Certificate) chainArray.get(0);
                
                keyStore.setKeyEntry("importkey", kp.getPrivate(), "keystore".toCharArray(), chain);
                
                File keyStoreFile = File.createTempFile("key", null);
                FileOutputStream keyStoreFileOutputStream = new FileOutputStream(keyStoreFile);
                keyStore.store(keyStoreFileOutputStream, "keystore".toCharArray());
                keyStoreFileOutputStream.close();
                
                System.setProperty("javax.net.ssl.keyStore", keyStoreFile.getAbsolutePath());
                System.setProperty("javax.net.ssl.keyStorePassword", "keystore");
                //System.setProperty("javax.net.debug", "ssl");
                
                System.setProperty("jdk.tls.trustNameService","true");
                
        		
                //MongoClientURI cliUri = new MongoClientURI ("mongodb://"+hostName+":"+hostPort+"/syracuse?ssl=true", new MongoClientOptions.Builder().sslEnabled(true).build());
                
                
                MongoClient mongoClient = new MongoClient( new ServerAddress(hostName, Integer.parseInt(hostPort)) , new MongoClientOptions.Builder().sslEnabled(true).build());
                //MongoClient mongoClient = new MongoClient(cliUri);
                
                // test if syracuse db already exists
                MongoIterable<String> lstDb = mongoClient.listDatabaseNames();
                
                for (String dbb : lstDb)
                {
                    if (dbb.equals("syracuse"))
                    {
                        if (modifyinstallation) bReturn = Status.OK;
                        else bReturn = Status.WARNING;
                        break;
                    }
                }
                
                mongoClient.close();
            }
            

        }
        catch (Exception ex)
        {
            Debug.trace(ex.getMessage());
            bReturn = Status.ERROR; 
        }

        return bReturn;
    }

    public String getErrorMessageId()
    {
        return "mongodbtesterror";
    }

    public String getWarningMessageId()
    {
        return "mongodbtestwarn";
    }

    public boolean getDefaultAnswer()
    {
        return false;
    }

}
