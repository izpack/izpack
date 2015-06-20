package com.izforge.izpack.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator.Status;


public class KeyPairGeneratorDataValidator implements com.izforge.izpack.installer.DataValidator
{

    public Status validateData(AutomatedInstallData adata)
    {

        Status bReturn = Status.ERROR;
        try
        {
        
            String passPhrase = "";
            if (adata.getVariable("tool.passphrase")!=null) passPhrase = adata.getVariable("tool.passphrase");
            File publicKeyFile = File.createTempFile("public", ".pem");
            File privateKeyFile = File.createTempFile("private", ".pem");
            String publicKeyFileName = publicKeyFile.getAbsolutePath();
            String privateKeyFileName = privateKeyFile.getAbsolutePath();
            
            
            // generate key pair
            KeyPairGenerator keyGen = KeyPairGenerator
                    .getInstance("RSA", "SunJSSE");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            KeyPair pair = keyGen.generateKeyPair();
            
            // write output
            writePublic(publicKeyFileName, pair.getPublic());
            writePrivateKey(privateKeyFileName, pair, passPhrase.toCharArray());
        
            bReturn = Status.OK;

            adata.setVariable("tool.temp.publickeyfile", publicKeyFileName);
            adata.setVariable("tool.temp.privatekeyfile", privateKeyFileName);

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
        // TODO Auto-generated method stub
        return "keypairgenerationerror";
    }

    public String getWarningMessageId()
    {
        // TODO Auto-generated method stub
        return "keypairgenerationwarn";
    }

    public boolean getDefaultAnswer()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    static private void writePublic(String filename, PublicKey publicKey) throws IOException {
        PEMWriter pemWriter = new PEMWriter(new PrintWriter(new FileWriter(
                filename)));
        try {
            pemWriter.writeObject(publicKey);
            pemWriter.flush();
        } finally {
            pemWriter.close();
        }
    }
    
    static void writePrivateKey(String filename, KeyPair key, char[] passphrase)
            throws IOException {
        Writer writer = new FileWriter(filename);
        JcePEMEncryptorBuilder jeb = new JcePEMEncryptorBuilder("DES-EDE3-CBC");
        jeb.setProvider("SunJCE");
        
        PEMWriter pemWriter = new PEMWriter(new PrintWriter(writer));
        try {
            
            if (passphrase!=null && passphrase.length>0)
            {
                PEMEncryptor pemEncryptor = jeb.build(passphrase);
                pemWriter.writeObject(key, pemEncryptor);
            }
            else
            {
                pemWriter.writeObject(key);
            }
            pemWriter.flush();
        } finally {
            pemWriter.close();
        }
    }
    
    public static void mergeFiles(File[] files, File mergedFile) throws IOException {

        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(mergedFile);

            for (File f : files) {
                in = new FileInputStream(f);
                int c;
    
                while ((c = in.read()) != -1) {
                    out.write(c);
                }
                
                in.close();
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }    
    
}
