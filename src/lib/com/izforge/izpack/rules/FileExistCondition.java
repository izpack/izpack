package com.izforge.izpack.rules;

import java.io.File;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.VariableSubstitutor;


public class FileExistCondition extends Condition
{

    protected String fileName;
    
    public FileExistCondition()
    {
        super();
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition)
    {
        try
        {
            this.fileName = xmlcondition.getFirstChildNamed("file").getContent();
        }
        catch (Exception e)
        {
            Debug.log("missing element file in <condition type=\"FileExistCondition\"/>");
        }

    }

    @Override
    public boolean isTrue()
    {
        try
        {
            VariableSubstitutor substitutor = new VariableSubstitutor(this.installdata.getVariables());

            File file = new File (substitutor.substitute( this.fileName, VariableSubstitutor.PLAIN));
            
            return file.isFile() && file.exists();
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
        nameEl.setContent(this.fileName);               
        conditionRoot.addChild(nameEl);

    }
    
    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.id);
        details.append(" depends on existence of the file <b>");
        details.append(this.fileName);
        details.append("</b><br/>");
        return details.toString();
    }
    

    
    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }

    
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

}
