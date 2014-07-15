package com.izforge.izpack.event;

import java.awt.HeadlessException;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;

class AntActionInputHandler extends DefaultInputHandler
{
    @Override
    public void handleInput(InputRequest request) throws BuildException
    {
        String response;

        try
        {
            if (request instanceof MultipleChoiceInputRequest)
            {
                response = getMultipleChoiceInput(request);
            }
            else
            {
                response = getTextInput(request);
            }
            request.setInput((response != null) ? response : "");
        }
        catch (HeadlessException e)
        {
            super.handleInput(request);
        }
    }

    private String getTextInput(InputRequest request) throws HeadlessException
    {
        String response = JOptionPane.showInputDialog(null, request.getPrompt(), request.getDefaultValue());

        return response;
    }

    private String getMultipleChoiceInput(InputRequest request) throws HeadlessException
    {
        MultipleChoiceInputRequest req = (MultipleChoiceInputRequest) request;

        Vector<String> choices = req.getChoices();
        String defaultValue = req.getDefaultValue();

        String response = (String) JOptionPane.showInputDialog(null, request.getPrompt(),
                null, JOptionPane.QUESTION_MESSAGE, null, choices.toArray(),
                defaultValue);

        return response;
    }

}
