
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

public class SettingsDialog
{

    private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
    private Label Username = null;
    private Text usernameCtl = null;
    private Label password = null;
    private Text passwordCtl = null;
    private Button okBtn = null;
    private Label label = null;
    private String usernameText;
    private String passwordText;
    private static SettingsDialog instance;  //  @jve:decl-index=0:
    private Label sipProxyLabel = null;
    private Text proxyCtl = null;
    public static synchronized SettingsDialog getInstance()
    {
        if (null == instance)
        {
            instance = new SettingsDialog();
        }
        return instance;
    }
    
    private SettingsDialog()
    {
        
    }
 
    public void show()
    {
        //Display display = Display.getDefault();
        createSShell();
        sShell.open();
        //sShell.moveAbove(null);
        //while (!sShell.isDisposed())
//        {
//            if (!display.readAndDispatch())
//                display.sleep();
//        }
//        display.dispose();
        populate();
    }    
    /**
     * This method initializes sShell
     */
    private void createSShell()
    {
        
        GridData gridData11 = new GridData();
        gridData11.grabExcessHorizontalSpace = true;
        gridData11.verticalAlignment = GridData.CENTER;
        gridData11.horizontalAlignment = GridData.FILL;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.END;
        gridData3.widthHint = 50;
        gridData3.verticalAlignment = GridData.CENTER;
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.verticalAlignment = GridData.CENTER;
        gridData2.horizontalAlignment = GridData.FILL;
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.verticalAlignment = GridData.CENTER;
        gridData1.horizontalAlignment = GridData.FILL;
        GridData gridData = new GridData();
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        sShell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
        sShell.setText("Flibble Settings");
        sShell.setLayout(gridLayout);
        sShell.setSize(new Point(300, 143));
        Username = new Label(sShell, SWT.NONE);
        Username.setText("Username");
        Username.setLayoutData(gridData);
        usernameCtl = new Text(sShell, SWT.BORDER);
        usernameCtl.setLayoutData(gridData2);
        Label filler9 = new Label(sShell, SWT.NONE);
        Label filler2 = new Label(sShell, SWT.NONE);
        password = new Label(sShell, SWT.NONE);
        password.setText("Password");
        passwordCtl = new Text(sShell, SWT.PASSWORD);
        passwordCtl.setLayoutData(gridData1);
        Label filler6 = new Label(sShell, SWT.NONE);
        Label filler = new Label(sShell, SWT.NONE);
        sipProxyLabel = new Label(sShell, SWT.NONE);
        sipProxyLabel.setText("SIP Proxy");
        proxyCtl = new Text(sShell, SWT.BORDER);
        proxyCtl.setLayoutData(gridData11);
        label = new Label(sShell, SWT.NONE);
        label.setText("");
        Label filler8 = new Label(sShell, SWT.NONE);
        Label filler1 = new Label(sShell, SWT.NONE);
        okBtn = new Button(sShell, SWT.NONE);
        okBtn.setText("OK");
        okBtn.setLayoutData(gridData3);
        okBtn.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                Settings.getInstance().setUsername(usernameCtl.getText().trim());
                Settings.getInstance().setPassword(passwordCtl.getText().trim());
                Settings.getInstance().setProxy(proxyCtl.getText().trim());
                Settings.getInstance().save();
                sShell.close();
                UserAgent.getInstance().init();
            }
        });
        
    }
    private void populate()
    {
        Settings.getInstance().load();
        if (null != Settings.getInstance().getUsername())
        {
            usernameCtl.setText(Settings.getInstance().getUsername());
        }
        if (null != Settings.getInstance().getPassword())
        {
            passwordCtl.setText(Settings.getInstance().getPassword());
        }
        if (null != Settings.getInstance().getProxy())
        {
            proxyCtl.setText(Settings.getInstance().getProxy());
        }
    }
}
