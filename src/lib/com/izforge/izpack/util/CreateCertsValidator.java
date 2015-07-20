package com.izforge.izpack.util;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;


public class CreateCertsValidator implements DataValidator
{

    private String strMessage = "";
    public static final String strMessageId = "messageid";
    public static final String strMessageValue = "message.oldvalue"; // not to be stored

    public Status validateData(AutomatedInstallData adata)
    {

        try
        {
            // first create CA
            KeyPair pairCA = generateRSAKeyPair(4096);
            
            String countryCode = adata.getVariable("mongodb.ssl.certificate.countrycode");
            String state = adata.getVariable("mongodb.ssl.certificate.state");
            String city = adata.getVariable("mongodb.ssl.certificate.city");
            String organization = adata.getVariable("mongodb.ssl.certificate.organization");
            String organizationalUnit = adata.getVariable("mongodb.ssl.certificate.organisationalunit");
            String name = adata.getVariable("mongodb.ssl.certificate.name");
            String email = adata.getVariable("mongodb.ssl.certificate.email");
            int validity = Integer.parseInt(adata.getVariable("mongodb.ssl.certificate.validity"));

            X509Certificate cacert = generateCAV3Certificate(pairCA, countryCode, organization, organizationalUnit,
                    state, city, name, email, validity);
            
            String strCertPath = adata.getVariable("mongodb.dir.certs");
            File dirCerts =  new File (strCertPath);
            if (!dirCerts.exists()) dirCerts.mkdirs();
            
            FileWriter cacertfile = new FileWriter(strCertPath + File.separator + "ca.cacrt");
            PEMWriter pem = new PEMWriter(cacertfile);
            pem.writeObject(cacert);
            pem.close();
            
            String capassphrase = adata.getVariable("mongodb.ssl.capassphrase");
            KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + "ca.key", pairCA, capassphrase.toCharArray());
            
            
            // then create server cert
            KeyPair pairServer = generateRSAKeyPair(2048);
            
            String hostname = adata.getVariable("mongodb.ssl.certificate.hostname");
            X509Certificate servercert = generateServerV3Certificate(pairServer, countryCode, organization, organizationalUnit,
                    state, city, hostname, null, validity, cacert , pairCA);
            
            FileWriter servercertfile = new FileWriter(strCertPath + File.separator + hostname + ".crt");
            pem = new PEMWriter(servercertfile);
            pem.writeObject(servercert);
            pem.close();
            
            String serverpassphrase = adata.getVariable("mongodb.ssl.serverpassphrase");
            KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + hostname + ".key", pairServer, serverpassphrase.toCharArray());
           
            adata.setVariable("mongodb.ssl.usecafile", "true");
            
            File pemKeyFile = new File(strCertPath + File.separator + hostname + ".pem");
            File certFile = new File(strCertPath + File.separator + hostname + ".crt");
            File privKeyFile = new File(strCertPath + File.separator + hostname + ".key");
            
            KeyPairGeneratorDataValidator.mergeFiles(new File[]{certFile,privKeyFile}, pemKeyFile);

            // then  create a client cert
            KeyPair pairClient = generateRSAKeyPair(2048);
            X509Certificate clientcert = generateClientV3Certificate(pairClient, countryCode, organization, organizationalUnit,
                    state, city, name, email, validity, cacert , pairCA);
            
            FileWriter clientcertfile = new FileWriter(strCertPath + File.separator + "client.crt");
            pem = new PEMWriter(clientcertfile);
            pem.writeObject(clientcert);
            pem.close();
            
            //String serverpassphrase = adata.getVariable("mongodb.ssl.serverpassphrase");
            KeyPairGeneratorDataValidator.writePrivateKey(strCertPath + File.separator + "client.key", pairClient, null);

            File pemClientKeyFile = new File(strCertPath + File.separator + "client.pem");
            File certClientFile = new File(strCertPath + File.separator + "client.crt");
            File privClientKeyFile = new File(strCertPath + File.separator + "client.key");
            
            KeyPairGeneratorDataValidator.mergeFiles(new File[]{certClientFile,privClientKeyFile}, pemClientKeyFile);
            
            // we need to says that this step was done at least one time
            adata.setVariable("mongodb.ssl.alreadydone", "true");
            
            return Status.OK;
            
        }
        catch (Exception ex)
        {
            strMessage = ex.getMessage();
            adata.setVariable(strMessageValue, strMessage);
        }

        return Status.ERROR;
    }

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

    public static X509Certificate generateCAV3Certificate(KeyPair pair, String country, String organization, String organizationalUnit,
            String state, String locality, String name, String email, int validity) throws Exception 
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        X500NameBuilder builder=new X500NameBuilder(BCStyle.INSTANCE);

        if (country !=null && !country.equals(""))
            builder.addRDN(BCStyle.C,country);
        if (organization !=null && !organization.equals(""))
            builder.addRDN(BCStyle.O,organization);
        if (organizationalUnit !=null && !organizationalUnit.equals(""))
            builder.addRDN(BCStyle.OU,organizationalUnit);
        if (state !=null && !state.equals(""))
            builder.addRDN(BCStyle.ST,state);
        if (locality !=null && !locality.equals(""))
            builder.addRDN(BCStyle.L,locality);
        if (name !=null && !name.equals(""))
            builder.addRDN(BCStyle.CN,name);
        if (email !=null && !email.equals(""))
            builder.addRDN(BCStyle.E,email);
        
        Date notBefore=new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(notBefore);
        cal.add(Calendar.DAY_OF_YEAR, validity);
        Date notAfter = cal.getTime();
        
        BigInteger serial=BigInteger.valueOf(System.currentTimeMillis());        

        X509v3CertificateBuilder certGen=new JcaX509v3CertificateBuilder(builder.build(),serial,notBefore,notAfter,builder.build(),pair.getPublic());
        
        certGen.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certGen.addExtension(Extension.keyUsage, true, new KeyUsage(
                KeyUsage.keyCertSign
                | KeyUsage.cRLSign ));
        
        SubjectKeyIdentifierStructure keyid = new SubjectKeyIdentifierStructure(pair.getPublic());
        certGen.addExtension(Extension.authorityKeyIdentifier, false, 
                new AuthorityKeyIdentifier(keyid.getKeyIdentifier()));
        certGen.addExtension(Extension.subjectKeyIdentifier, false, keyid);

        ContentSigner sigGen=new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).build(pair.getPrivate());
        X509Certificate cert=new JcaX509CertificateConverter().setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).getCertificate(certGen.build(sigGen));
        cert.checkValidity(new Date());
        //cert.verify(cert.getPublicKey());      
        cert.verify(pair.getPublic());      
        
        return cert;
    }

    public static X509Certificate generateServerV3Certificate(KeyPair pair, String country, String organization, String organizationalUnit,
            String state, String locality, String name, String email, int validity, X509Certificate certCA, KeyPair pairCA) throws Exception 
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        X500NameBuilder builder=new X500NameBuilder(BCStyle.INSTANCE);

        if (country !=null && !country.equals(""))
            builder.addRDN(BCStyle.C,country);
        if (organization !=null && !organization.equals(""))
            builder.addRDN(BCStyle.O,organization);
        if (organizationalUnit !=null && !organizationalUnit.equals(""))
            builder.addRDN(BCStyle.OU,organizationalUnit);
        if (state !=null && !state.equals(""))
            builder.addRDN(BCStyle.ST,state);
        if (locality !=null && !locality.equals(""))
            builder.addRDN(BCStyle.L,locality);
        if (name !=null && !name.equals(""))
            builder.addRDN(BCStyle.CN,name);
        if (email !=null && !email.equals(""))
            builder.addRDN(BCStyle.E,email);
        
        Date notBefore=new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(notBefore);
        cal.add(Calendar.DAY_OF_YEAR, validity);
        Date notAfter = cal.getTime();
        
        BigInteger serial=BigInteger.valueOf(System.currentTimeMillis());        

        X509v3CertificateBuilder certGen=new JcaX509v3CertificateBuilder(certCA,serial,notBefore,notAfter,builder.build(),pair.getPublic());
        
        certGen.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        certGen.addExtension(Extension.keyUsage, true, new KeyUsage(
                KeyUsage.digitalSignature
                | KeyUsage.keyEncipherment
                | KeyUsage.dataEncipherment
                | KeyUsage.keyAgreement));
        
        SubjectKeyIdentifierStructure keyid = new SubjectKeyIdentifierStructure(pair.getPublic());
        certGen.addExtension(Extension.subjectKeyIdentifier, false, keyid);

        AuthorityKeyIdentifierStructure keyidCA = new AuthorityKeyIdentifierStructure(certCA);
        certGen.addExtension(Extension.authorityKeyIdentifier, false, keyidCA);

        ContentSigner sigGen=new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).build(pairCA.getPrivate());
        X509Certificate cert=new JcaX509CertificateConverter().setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).getCertificate(certGen.build(sigGen));
        
        cert.checkValidity(new Date());
        //cert.verify(cert.getPublicKey());      
        cert.verify(pairCA.getPublic());      
        
        return cert;
    }

    public static X509Certificate generateClientV3Certificate(KeyPair pair, String country, String organization, String organizationalUnit,
            String state, String locality, String name, String email, int validity, X509Certificate certCA, KeyPair pairCA) throws Exception 
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        
        X500NameBuilder builder=new X500NameBuilder(BCStyle.INSTANCE);

        if (country !=null && !country.equals(""))
            builder.addRDN(BCStyle.C,country);
        if (organization !=null && !organization.equals(""))
            builder.addRDN(BCStyle.O,organization);
        if (organizationalUnit !=null && !organizationalUnit.equals(""))
            builder.addRDN(BCStyle.OU,organizationalUnit);
        if (state !=null && !state.equals(""))
            builder.addRDN(BCStyle.ST,state);
        if (locality !=null && !locality.equals(""))
            builder.addRDN(BCStyle.L,locality);
        //if (name !=null && !name.equals(""))
            builder.addRDN(BCStyle.CN,"client");
        //if (email !=null && !email.equals(""))
        //    builder.addRDN(BCStyle.E,email);
        
        Date notBefore=new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(notBefore);
        cal.add(Calendar.DAY_OF_YEAR, validity);
        Date notAfter = cal.getTime();
        
        BigInteger serial=BigInteger.valueOf(System.currentTimeMillis());        

        X509v3CertificateBuilder certGen=new JcaX509v3CertificateBuilder(certCA,serial,notBefore,notAfter,builder.build(),pair.getPublic());
        
        certGen.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        certGen.addExtension(Extension.keyUsage, true, new KeyUsage(
                KeyUsage.digitalSignature
                | KeyUsage.dataEncipherment
                | KeyUsage.keyAgreement));
        certGen.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));
        
        SubjectKeyIdentifierStructure keyid = new SubjectKeyIdentifierStructure(pair.getPublic());
        certGen.addExtension(Extension.subjectKeyIdentifier, false, keyid);

        AuthorityKeyIdentifierStructure keyidCA = new AuthorityKeyIdentifierStructure(certCA);
        certGen.addExtension(Extension.authorityKeyIdentifier, false, keyidCA);

        ContentSigner sigGen=new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).build(pairCA.getPrivate());
        X509Certificate cert=new JcaX509CertificateConverter().setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).getCertificate(certGen.build(sigGen));
        
        cert.checkValidity(new Date());
        //cert.verify(cert.getPublicKey());      
        cert.verify(pairCA.getPublic());      
        
        return cert;
    }

    public static KeyPair generateRSAKeyPair(int nSize) throws Exception {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(nSize, new SecureRandom());
        return kpGen.generateKeyPair();
    }    
}
