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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

public class DialPadComposite extends Composite
{

    private Button button1 = null;
    private Button button2 = null;
    private Button button3 = null;
    private Button button4 = null;
    private Button button5 = null;
    private Button button6 = null;
    private Button button7 = null;
    private Button button8 = null;
    private Button button9 = null;
    private Button buttonStar = null;
    private Button button0 = null;
    private Button buttonPound = null;

    public DialPadComposite(Composite parent, int style)
    {
        super(parent, style);
        initialize();
    }

    private void initialize()
    {
        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = GridData.FILL;
        gridData11.grabExcessHorizontalSpace = true;
        gridData11.grabExcessVerticalSpace = true;
        gridData11.verticalAlignment = GridData.FILL;
        GridData gridData10 = new GridData();
        gridData10.grabExcessHorizontalSpace = true;
        gridData10.horizontalAlignment = GridData.FILL;
        gridData10.verticalAlignment = GridData.FILL;
        gridData10.grabExcessVerticalSpace = true;
        GridData gridData9 = new GridData();
        gridData9.horizontalAlignment = GridData.FILL;
        gridData9.grabExcessHorizontalSpace = true;
        gridData9.grabExcessVerticalSpace = true;
        gridData9.verticalAlignment = GridData.FILL;
        GridData gridData8 = new GridData();
        gridData8.horizontalAlignment = GridData.FILL;
        gridData8.grabExcessHorizontalSpace = true;
        gridData8.grabExcessVerticalSpace = true;
        gridData8.verticalAlignment = GridData.FILL;
        GridData gridData7 = new GridData();
        gridData7.horizontalAlignment = GridData.FILL;
        gridData7.grabExcessHorizontalSpace = true;
        gridData7.grabExcessVerticalSpace = true;
        gridData7.verticalAlignment = GridData.FILL;
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = GridData.FILL;
        gridData6.grabExcessHorizontalSpace = true;
        gridData6.grabExcessVerticalSpace = true;
        gridData6.verticalAlignment = GridData.FILL;
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = GridData.FILL;
        gridData5.grabExcessHorizontalSpace = true;
        gridData5.grabExcessVerticalSpace = true;
        gridData5.verticalAlignment = GridData.FILL;
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.grabExcessVerticalSpace = true;
        gridData4.verticalAlignment = GridData.FILL;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.grabExcessVerticalSpace = true;
        gridData3.verticalAlignment = GridData.FILL;
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.verticalAlignment = GridData.FILL;
        gridData2.grabExcessVerticalSpace = true;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.grabExcessVerticalSpace = true;
        gridData1.verticalAlignment = GridData.FILL;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        button1 = new Button(this, SWT.NONE);
        button1.setText("1");
        button1.setLayoutData(gridData);
        button1.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(1);
            }
        });
        button2 = new Button(this, SWT.NONE);
        button2.setText("2 abc");
        button2.setLayoutData(gridData1);
        button2.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(2);
            }
        });
        button3 = new Button(this, SWT.NONE);
        button3.setText("3 def");
        button3.setLayoutData(gridData2);
        button3.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(3);
            }
        });
        button4 = new Button(this, SWT.NONE);
        button4.setText("4 ghi");
        button4.setLayoutData(gridData3);
        button4.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(4);
            }
        });
        button5 = new Button(this, SWT.NONE);
        button5.setText("5 jkl");
        button5.setLayoutData(gridData4);
        button5.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(5);
            }
        });
        button6 = new Button(this, SWT.NONE);
        button6.setText("6 mno");
        button6.setLayoutData(gridData5);
        button6.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(6);
            }
        });
        button7 = new Button(this, SWT.NONE);
        button7.setText("7 pqrs");
        button7.setLayoutData(gridData6);
        button7.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(7);
            }
        });
        button8 = new Button(this, SWT.NONE);
        button8.setText("8 tuv");
        button8.setLayoutData(gridData7);
        button8.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(8);
            }
        });
        button9 = new Button(this, SWT.NONE);
        button9.setText("9 wxyz");
        button9.setLayoutData(gridData8);
        button9.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(9);
            }
        });
        buttonStar = new Button(this, SWT.NONE);
        buttonStar.setText("*");
        buttonStar.setLayoutData(gridData9);
        buttonStar.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(10);
           }
        });
        button0 = new Button(this, SWT.NONE);
        button0.setText("0");
        button0.setLayoutData(gridData10);
        button0.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        { 
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(0);
            }
        });
        buttonPound = new Button(this, SWT.NONE);
        buttonPound.setText("#");
        buttonPound.setLayoutData(gridData11);
        buttonPound.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                UserAgent.getInstance().onDialPad(11);
            }
        });
        this.setLayout(gridLayout);
        this.setSize(new Point(140, 133));
    }
    

}  //  @jve:decl-index=0:visual-constraint="10,10"
