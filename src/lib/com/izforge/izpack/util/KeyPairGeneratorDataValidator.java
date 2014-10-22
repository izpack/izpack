package com.izforge.izpack.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator.Status;


public class KeyPairGeneratorDataValidator implements com.izforge.izpack.installer.DataValidator
{

    public Status validateData(AutomatedInstallData adata)
    {

        Status bReturn = Status.ERROR;
        try
        {
        
            String passPhrase = adata.getVariable("tool.passphrase");
            String keysDirectory = adata.getVariable("tool.dir.keypath");
//            File publicKeyFile = new File(keysDirectory,"public.pem");
//            File privateKeyFile = new File(keysDirectory,"private.pem");
            File publicKeyFile = File.createTempFile("public", ".pem");
            File privateKeyFile = File.createTempFile("private", ".pem");
            String publicKeyFileName = publicKeyFile.getAbsolutePath();
            String privateKeyFileName = privateKeyFile.getAbsolutePath();
            
            adata.setVariable("tool.temp.publickeyfile", publicKeyFileName);
            adata.setVariable("tool.temp.privatekeyfile", privateKeyFileName);
            
            // generate key pair
            KeyPairGenerator keyGen = KeyPairGenerator
                    .getInstance("RSA", "SunJSSE");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            KeyPair pair = keyGen.generateKeyPair();
            byte[] pub = pair.getPublic().getEncoded();
            byte[] priv = pair.getPrivate().getEncoded();
            
            
            // encrypt private key with passphrase
            byte[] iv = new byte[8];
            random.nextBytes(iv);
            byte[] passphraseB = new byte[passPhrase.length()];
            for (int i = 0; i<passPhrase.length(); i++) { // take only lower byte 
                passphraseB[i] = (byte) (passPhrase.charAt(i) % 256);
            }
            
            byte[] ds = new byte[24];
            int cnt = 0;
            while (cnt < 24) 
            {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(ds, 0, cnt);
                md.update(passphraseB);
                md.update(iv);
                byte[] digest = md.digest();
                int copyCount = Math.min(digest.length, ds.length-cnt);
                System.arraycopy(digest, 0, ds, cnt, copyCount);
                cnt += digest.length;           
            }
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
            SecretKey sk = skf.generateSecret(new DESedeKeySpec(ds));
            cipher.init(Cipher.ENCRYPT_MODE, sk, new IvParameterSpec(iv));
            priv = cipher.doFinal(priv);
            
            
            // write output
            writeKey(pub, publicKeyFile, "PUBLIC", null);
            writeKey(priv, privateKeyFile, "PRIVATE", iv);
        
            bReturn = Status.OK;
        
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
    
    static private void writeKey(byte[] encoded, File file, String type, byte[] iv) throws IOException
    {
        String text = DatatypeConverter.printBase64Binary(encoded);     
        FileWriter fw = new FileWriter(file);
        
        fw.write("-----BEGIN "+type+" KEY-----\n"); 
        if (iv != null) 
        {
            fw.write("Proc-Type: 4,ENCRYPTED\n");
            fw.write("DEK-Info: DES-EDE3-CBC,"+ DatatypeConverter.printHexBinary(iv)+"\n\n");
        }       
        int i;
        for (i = 0; i < text.length() - 64; i += 64) // insert line breaks in base64 
            fw.write(text.substring(i, i+64) + '\n');
        fw.write(text.substring(i) + "\n-----END " + type + " KEY-----\n");
        fw.close();
    }

    

}
