/*******************************************************************************
 *   Copyright 2007 SIP Response
 *   Copyright 2007 Michael D. Cohen
 *
 *      mike _AT_ sipresponse.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class MainForm extends Composite
{

    private DialPadComposite dialPad = null;
    private Text dialStringCtl = null;
    private Button Dial = null;
    private Button answer = null;
    private StyledText styledText = null;
    public static MainForm instance;
    public static synchronized MainForm getInstance()
    {
        return instance;
    }
    
    public MainForm(Composite parent, int style)
    {
        super(parent, style);
        instance = this;
        initialize();
    }

    private void initialize()
    {
        this.setLayout(null);
        createDialPad();
        setSize(new Point(151, 242));
        dialStringCtl = new Text(this, SWT.BORDER);
        dialStringCtl.setLocation(new Point(5, 78));
        dialStringCtl.setSize(new Point(140, 19));
        Dial = new Button(this, SWT.NONE);
        Dial.setText("^");
        Dial.setLocation(new Point(4, 37));
        Dial.setSize(new Point(56, 31));
        Dial.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onCallButtonPressed(dialStringCtl.getText());
            }
        });
        answer = new Button(this, SWT.NONE);
        answer.setLocation(new Point(89, 37));
        answer.setText("x");
        answer.setSize(new Point(56, 31));
        answer.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().bye();
            }
        });
        styledText = new StyledText(this, SWT.BORDER | SWT.SINGLE);
        styledText.setForeground(new Color(Display.getCurrent(), 0, 0, 205));
        styledText.setBounds(new Rectangle(4, 6, 142, 20));
        
        UserAgent.getInstance().init();
    }


    /**
     * This method initializes dialPad	
     *
     */
    private void createDialPad()
    {
        dialPad = new DialPadComposite(this, SWT.NONE);
        dialPad.setLayout(null);
        dialPad.setBounds(new Rectangle(4, 103, 141, 135));
    }
    
    public void addToDialString(String s)
    {
        dialStringCtl.setText(dialStringCtl.getText() + s);
    }
    public void setIncomingCallerId(final String callerId)
    {
        new Thread(
                new Runnable()
                {
                    public void run()
                    {
                          try { Thread.sleep(1000); } catch (Exception e) { }
                          Display.getDefault().asyncExec(
                                  new Runnable()
                                  {
                                      public void run()
                                      {
                                          getShell().forceActive();
                                          styledText.setText(callerId);
                                      }
                                  });
                    }
                }).start();        
    }

}  //  @jve:decl-index=0:visual-constraint="12,5"
