package com.izforge.izpack.panels.userinput.gui.file;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.file.AbstractFileField;
import com.izforge.izpack.panels.userinput.field.file.FileFieldView;

public class FileInputFieldTest
{

    @Test
    public void testEmptyFieldValidation()
    {
        AbstractFileField field = Mockito.mock(AbstractFileField.class);
        Mockito.when(field.getAbsoluteFile("")).thenReturn(new File("/"));
        Mockito.when(field.getAllowEmptyValue()).thenReturn(Boolean.TRUE);

        IzPanel parent = Mockito.mock(IzPanel.class);
        GUIInstallData installDataGUI = Mockito.mock(GUIInstallData.class);
        Messages messages = Mockito.mock(Messages.class);
        Mockito.when(installDataGUI.getMessages()).thenReturn(messages);

        FileFieldView view = new FileFieldView(field, null);

        FileInputField inputField = new FileInputField(view, parent, installDataGUI);
        Assert.assertTrue(inputField.validateField());

        Assert.assertNull(inputField.getSelectedFile());
    }
}
