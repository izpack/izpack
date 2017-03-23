package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bson.Document;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoIterable;



public class MongoDBDataValidator implements DataValidator
{

	//	// Ignore differences between given hostname and certificate hostname
	//    private static HostnameVerifier hostVerifier = new HostnameVerifier() {
	//        public boolean verify(String hostname, SSLSession session) { return true; }
	//    };

	@Override
	public Status validateData(AutomatedInstallData adata)
	{
		Status bReturn = Status.OK;
		try
		{

			final Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(AutomatedInstallData.MODIFY_INSTALLATION));

			//String userName = adata.getVariable("mongodb.url.username");
			//String passWord = adata.getVariable("mongodb.url.password");

			final String hostName = new String (adata.getVariable("mongodb.service.hostname"));
			final String hostPort = adata.getVariable("mongodb.service.port");
			final boolean sslEnabled = "true".equalsIgnoreCase(adata.getVariable("mongodb.ssl.enable"));
			final String certFile = adata.getVariable("mongodb.ssl.client.certfile");
			final String pemkeyFile = adata.getVariable("mongodb.ssl.client.pemkeyfile");
			final String pemcaFile = adata.getVariable("mongodb.ssl.pemcafile");

			if (!sslEnabled)
			{

				final MongoClient mongoClient = new MongoClient( hostName , Integer.parseInt(hostPort) );

				//String version = mongoClient.getDB("test").command("buildInfo").getString("version");
				final String version = mongoClient.getDatabase("test").runCommand(new Document("buildInfo", 1)).getString("version");

				if (!version.startsWith("3."))
				{
					bReturn = Status.ERROR;
				}
				else
				{

					// test if syracuse db already exists
					final MongoIterable<String> lstDb = mongoClient.listDatabaseNames();

					for (final String dbb : lstDb)
					{
						if (dbb.equals("syracuse"))
						{
							if (modifyinstallation) {
								bReturn = Status.OK;
							} else {
								bReturn = Status.WARNING;
							}
							break;
						}
					}
				}



				mongoClient.close();
			}
			else
			{

				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

				final CertificateFactory factory = CertificateFactory.getInstance("X.509");
				final ArrayList chainArray = new ArrayList();

				if (pemcaFile !=null && !"".equals(pemcaFile))
				{
					//create truststore
					final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
					trustStore.load(null, null);
					final InputStream inPemCaFile = new FileInputStream(pemcaFile);
					final X509Certificate cacert = (X509Certificate) factory.generateCertificate(inPemCaFile);
					trustStore.setCertificateEntry("root", cacert);
					final File trustStoreFile = File.createTempFile("tru", null);
					final FileOutputStream trustStoreFileOutputStream = new FileOutputStream(trustStoreFile);
					trustStore.store(trustStoreFileOutputStream, "truststore".toCharArray());
					trustStoreFileOutputStream.close();

					chainArray.add(cacert);

					System.setProperty("javax.net.ssl.trustStore", trustStoreFile.getAbsolutePath());
					System.setProperty("javax.net.ssl.trustStorePassword", "truststore");

				}

				//create keystore
				final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(null, null);

				final InputStream inPemCertFile = new FileInputStream(certFile);
				final X509Certificate cert = (X509Certificate) factory.generateCertificate(inPemCertFile);

				final InputStream inPemKeyFile = new FileInputStream(pemkeyFile);
				final PEMParser pemParser = new PEMParser(new InputStreamReader(inPemKeyFile));
				final Object object = pemParser.readObject();
				final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
				final PEMKeyPair ukp = (PEMKeyPair) object;
				final KeyPair kp = converter.getKeyPair(ukp);

				keyStore.setCertificateEntry(cert.getSubjectX500Principal().toString(), cert);

				chainArray.add (cert);

				final X509Certificate[] chain = new X509Certificate[chainArray.size()];
				chain[0] = cert;
				if (chainArray.size()>1) {
					chain[1]= (X509Certificate) chainArray.get(0);
				}

				keyStore.setKeyEntry("importkey", kp.getPrivate(), "keystore".toCharArray(), chain);

				final File keyStoreFile = File.createTempFile("key", null);
				final FileOutputStream keyStoreFileOutputStream = new FileOutputStream(keyStoreFile);
				keyStore.store(keyStoreFileOutputStream, "keystore".toCharArray());
				keyStoreFileOutputStream.close();

				System.setProperty("javax.net.ssl.keyStore", keyStoreFile.getAbsolutePath());
				System.setProperty("javax.net.ssl.keyStorePassword", "keystore");
				//System.setProperty("javax.net.debug", "ssl");

				System.setProperty("jdk.tls.trustNameService","true");

				final MongoClientOptions.Builder opts = MongoClientOptions.builder();
				opts.sslEnabled(true);
				opts.serverSelectionTimeout(60000);

				//final MongoClient mongoClient = new MongoClient( new ServerAddress(hostName, Integer.parseInt(hostPort)) , new MongoClientOptions.Builder().sslEnabled(true).build());
				//MongoClient mongoClient = new MongoClient(cliUri);
				final MongoClient mongoClient = new MongoClient( new ServerAddress(hostName, Integer.parseInt(hostPort)) , opts.build());

				final String version = mongoClient.getDatabase("test").runCommand(new Document("buildInfo", 1)).getString("version");

				if (!version.startsWith("3."))
				{
					bReturn = Status.ERROR;
				}
				else
				{
					// test if syracuse db already exists
					final MongoIterable<String> lstDb = mongoClient.listDatabaseNames();

					for (final String dbb : lstDb)
					{
						if (dbb.equals("syracuse"))
						{
							if (modifyinstallation) {
								bReturn = Status.OK;
							} else {
								bReturn = Status.WARNING;
							}
							break;
						}
					}
				}

				mongoClient.close();
			}


		}
		catch (final Exception ex)
		{
			Debug.trace(ex.getMessage());
			bReturn = Status.ERROR;
		}

		return bReturn;
	}

	@Override
	public String getErrorMessageId()
	{
		return "mongodbtesterror";
	}

	@Override
	public String getWarningMessageId()
	{
		return "mongodbtestwarn";
	}

	@Override
	public boolean getDefaultAnswer()
	{
		return false;
	}

}
