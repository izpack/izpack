package com.izforge.izpack.panels.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class WritingPropertiesfile {
	
	public void run(AbstractUIProcessHandler handler, String[] args) {
		try {
 
			String content = "";
 
			File file = new File("muflow.properties");
			
			content+=("#muFlow Properties file\n");
			content+=("#jdbc.url=jdbc:{DB_TYPE}://{DB_SERVER_IP}:(DB_PORT_NO)/{DB_NAME} \n");
			content+=("jdbc.url=jdbc:"+args[1]+"://"+args[2]+":"+args[3]+"/"+args[4]+"\n");
			content+=("#Credentials to authenticate the DB user\n");
			content+=("jdbc.username="+args[5]+"\njdbc.password="+args[6]+"\n");
			content+=("#LDAP Credentials \n");
			content+=("ldap.url=ldap://"+args[7]+"\n");
			content+=("ldap.base="+args[8]+"\n");
			content+=("ldap.userdn="+args[9]+"\n");
			content+=("ldap.password="+args[10]+"\n");
			content+=("#Set MuFlow Environment Variables.\n");
			content+=("workspacePath="+args[11]+"\n");
			content+=("systemTempLocation="+args[12]+"\n");
			content+=("metaNodeDirectory="+args[13]+"\n");
			content+=("muflowLogPath="+args[14]+"\n");

			
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			
			File newfile = new File("target.tmp");
			if (!newfile.exists()) {
				newfile.createNewFile();
			}
			
			
			FileWriter newfw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter newbw = new BufferedWriter(newfw);
			newbw.write(args[0]);
			newbw.close();
			
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}