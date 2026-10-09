package com.izforge.izpack.panels.userinput.gui.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.file.AbstractFileField;
import com.izforge.izpack.panels.userinput.field.file.FileFieldView;
import java.io.File;
import org.junit.jupiter.api.Test;

public class FileInputFieldTest {

    @Test
    public void testEmptyFieldValidation()
    {
        AbstractFileField field = mock(AbstractFileField.class);
        when(field.getAbsoluteFile("")).thenReturn(new File("/"));
        when(field.getAllowEmptyValue()).thenReturn(Boolean.TRUE);

        IzPanel parent = mock(IzPanel.class);
        GUIInstallData installDataGUI = mock(GUIInstallData.class);
        Messages messages = mock(Messages.class);
        when(installDataGUI.getMessages()).thenReturn(messages);

        FileFieldView view = new FileFieldView(field, null);

        FileInputField inputField = new FileInputField(view, parent, installDataGUI);
        assertThat(inputField.validateField()).isTrue();

        assertThat(inputField.getSelectedFile()).isNull();
    }
}
