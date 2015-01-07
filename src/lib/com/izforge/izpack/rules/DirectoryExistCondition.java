package com.izforge.izpack.rules;

import java.io.File;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.VariableSubstitutor;


public class DirectoryExistCondition extends Condition
{
    protected String directoryName;

    public DirectoryExistCondition()
    {
        super ();
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            this.directoryName = xmlcondition.getFirstChildNamed("directory").getContent();
        }
        catch (Exception e)
        {
            Debug.log("missing element directory in <condition type=\"DirectoryExistCondition\"/>");
        }

    }

    @Override
    public boolean isTrue()
    {
        try
        {
            VariableSubstitutor substitutor = new VariableSubstitutor(this.installdata.getVariables());
            File file = new File (substitutor.substitute( this.directoryName, VariableSubstitutor.PLAIN));
            
            return file.isDirectory() && file.exists();
        }
        catch (Exception ex)
        {
            Debug.log (ex);
            return false;
        }
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        XMLElementImpl nameEl = new XMLElementImpl("file",conditionRoot);
        nameEl.setContent(this.directoryName);               
        conditionRoot.addChild(nameEl);

    }
    
    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on existence of the directory <b>");
        details.append(this.directoryName);
        details.append("</b><br/>");
        return details.toString();
    }

    
    /**
     * @return the directoryName
     */
    public String getDirectoryName()
    {
        return directoryName;
    }

    
    /**
     * @param directoryName the directoryName to set
     */
    public void setDirectoryName(String directoryName)
    {
        this.directoryName = directoryName;
    }
    

    

}
