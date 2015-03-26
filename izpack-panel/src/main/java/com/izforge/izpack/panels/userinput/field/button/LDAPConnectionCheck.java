package com.izforge.izpack.panels.userinput.field.button;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.util.Console;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;



public class LDAPConnectionCheck extends ButtonAction
{

		private static final String ERROR = "error";
		
		public LDAPConnectionCheck(InstallData installData){
			super(installData);
		}
		
		@Override
		public boolean execute()
		{
			
			//LDAP Credentials
			String ldapurl= "";
			String ldapbase = "";
			String username = "";
			String password = "";
			
			/*
			 ldapurl= "172.25.1.46:389";
			 ldapbase = "cn=muflow,dc=ird,dc=mu-sigma,dc=com";
			 username = "cn=Directory Manager";
			 password = "password";
			*/
			
			ldapurl="ldap://"+installData.getVariable("ldapurl");
			ldapbase=installData.getVariable("ldapbase");
			username=installData.getVariable("ldapusername");
			password=installData.getVariable("ldappassword");


				// Setting Ldap configuration
				LdapContextSource cxtSrc = new LdapContextSource();

				/*
				 * cxtSrc.setUrl("ldap://172.25.1.46:389");
				 * cxtSrc.setBase("dc=ird,dc=mu-sigma,dc=com");
				 * cxtSrc.setUserDn("cn=Directory Manager");
				 * cxtSrc.setPassword("password"); 
				 * cxtSrc.afterPropertiesSet();
				 */

				cxtSrc.setUrl(ldapurl);
				cxtSrc.setBase(ldapbase);
				cxtSrc.setUserDn(username);
				cxtSrc.setPassword(password);
				LdapTemplate template = new LdapTemplate(cxtSrc);

				try {
					cxtSrc.afterPropertiesSet();
					template.list("");
					return true;
				
				} catch (Exception e) {
					//String err=e.getMessage()();
					return false;
				}
		}
		
		@Override
		public boolean execute(Console console)
		{
			if(!execute())
			{
			console.println(messages.get(ERROR));
			return false;
			}
			return true;
		}
		
		
		@Override
		public boolean execute(Prompt prompt)
		{
			if(!execute())
			{
			prompt.warn(messages.get(ERROR));
			return false;
			}
			return true;
		}
		
}